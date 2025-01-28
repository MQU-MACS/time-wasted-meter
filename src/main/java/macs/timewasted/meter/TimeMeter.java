package main.java.macs.timewasted.meter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import main.java.macs.timewasted.checker.MilestoneManager;
import main.java.macs.timewasted.command.MilestoneAggregate;
import main.java.macs.timewasted.meter.BarPreferences.Prefs;
import main.java.macs.timewasted.util.Util;

public class TimeMeter implements Listener, Runnable {

	private static final int SWITCH_RATE = 5 * 20; // 5 seconds = 100 ticks, defines how often (in ticks) the plugin should switch the display mode of the boss bars.
	
	// i know there's a better way to do this, idgaf
	private static final BarDisplay[] DISPLAY_SEQUENCE = {
		BarDisplay.TIME_WASTED, BarDisplay.NEXT_MILESTONE, BarDisplay.CMD_STATS,
		BarDisplay.TIME_WASTED, BarDisplay.NEXT_MILESTONE, BarDisplay.CMD_LEADERBOARD,
		BarDisplay.TIME_WASTED, BarDisplay.NEXT_MILESTONE, BarDisplay.CMD_CUSTOMIZE
	};
	
	private final Map<UUID, BossBar> bossBars = new HashMap<>();
	private final FileConfiguration config;
	private final MilestoneManager milestones;
	private final BarPreferences prefs;
	private final Scoreboard scoreboard;
	private Objective objective;
	
	private int displayIndex = 0;
	private int displayCounter = 0;

	public TimeMeter(FileConfiguration config, MilestoneManager milestones, BarPreferences prefs) {
		this.config     = config;
		this.milestones = milestones;
		this.prefs      = prefs;
		this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		this.objective  = this.scoreboard.getObjective("time");
	}
	
	@Override
	public void run() {
		if(this.objective == null) {
			// if objective doesn't exist, keep querying for it
			this.objective = this.scoreboard.getObjective("time");
			
			// if it's still null, do nothing
			if(this.objective == null) {
				return;
			}
		}
		
		// advance display index (if necessary)
		if(this.displayCounter >= SWITCH_RATE) {
			// reset counter and advance index
			this.displayCounter = 0;
			this.displayIndex = (this.displayIndex + 1) % DISPLAY_SEQUENCE.length;
		} else {
			// increase counter by 1 tick
			this.displayCounter++;
		}
		
		// loop over each boss bar
		for(UUID uuid : this.bossBars.keySet()) {
			// try to resolve the player
			OfflinePlayer player = Util.resolvePlayerForUUID(uuid);
			boolean hidden = this.prefs.isHiddenFor(uuid);
			if(player == null || !player.isOnline() || hidden) continue;
			
			// update the bar
			refreshBar(player, this.bossBars.get(uuid));
		}
	}
	
	private void refreshBar(OfflinePlayer player, BossBar bar) {
		// get progress data for player
		MilestoneAggregate aggregate = MilestoneAggregate.aggregateFor(player, this.objective, this.milestones);
		double percentage = (double)aggregate.progress / (double)aggregate.difference;
					
		// set bar percentage
		bar.setProgress(Util.clamp(percentage, 0d, 1d));
		
		// calculate bar text
		BarDisplay display = DISPLAY_SEQUENCE[this.displayIndex];
		switch(display) {
		
			case CMD_STATS:
				bar.setTitle(Util.getString("strings.bar.stats", this.config));
				break;
			case CMD_LEADERBOARD:
				bar.setTitle(Util.getString("strings.bar.leaderboard", this.config));
				break;
			case CMD_CUSTOMIZE:
				bar.setTitle(ChatColor.GOLD + "Customise the bar! " + ChatColor.BOLD + "/timemeter");
				break;
			
			case NEXT_MILESTONE:
				float remaining = (float)(aggregate.difference - aggregate.progress) / 20f / 3600f;
				String ticksString = String.format("%,d", aggregate.nextMilestone);
				String remainString = Util.formatHourMinute(remaining);
				bar.setTitle(String.format(Util.getString("strings.bar.milestone", this.config), ticksString, remainString));
				break;
			
			case TIME_WASTED:
			default:
				float hours = ((float)aggregate.rawScore / 20f) / 3600f;
				String scoreString = String.format("%,d", aggregate.rawScore);
				String hourString  = String.format("%.1f", hours);
				bar.setTitle(String.format(Util.getString("strings.command.second", this.config), scoreString, hourString));
				break;
		}
	}
	
	public void updatePlayerPrefs(Player player, Prefs p) {
		BossBar bar = this.bossBars.get(player.getUniqueId());
		if(bar != null) {
			bar.setColor(p.color);
			bar.setStyle(p.style);
			if(p.hidden) {
				bar.removeAll();
			} else {
				bar.addPlayer(player);
			}
		}
	}
	
	@EventHandler
	public void onPlayerJoined(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		
		BossBar bar = this.bossBars.get(uuid);
		Prefs p = this.prefs.forPlayer(player);
		
		if(bar == null) {
			bar = Bukkit.createBossBar(event.getPlayer().getName(), p.color, p.style);
			bar.removeAll();
			this.bossBars.put(uuid, bar);
		}
		
		if(!p.hidden) bar.addPlayer(player);
		if(this.objective != null) refreshBar(player, bar);
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		Player player = event.getPlayer();
		UUID uuid = player.getUniqueId();
		
		if(this.bossBars.containsKey(uuid)) {
			this.bossBars.get(uuid).removeAll();
		}
	}

}