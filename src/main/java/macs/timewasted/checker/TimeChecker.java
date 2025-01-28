package main.java.macs.timewasted.checker;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import main.java.macs.timewasted.util.Util;
import static main.java.macs.timewasted.util.Util.getString;

public class TimeChecker implements Runnable {

	private final HashMap<UUID, Integer> scoreCache = new HashMap<UUID, Integer>();

	private final FileConfiguration config;
	private final MilestoneManager milestones;
	private final Scoreboard scoreboard;
	private Objective objective;

	public TimeChecker(MilestoneManager milestones, FileConfiguration config) {
		this.config = config;
		this.milestones = milestones;
		this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
		this.objective  = this.scoreboard.getObjective("time");
		if(this.objective != null) {
			this.getInitialScores();
		}
	}

	private void getInitialScores() {
		boolean saveNeeded = false;
		
		for(OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			if(player == null || player.getName() == null)
				continue;
			int score = this.objective.getScore(player.getName()).getScore();
			this.scoreCache.put(player.getUniqueId(), score);
			if(score > 0) {
				saveNeeded = this.backtraceScores(player.getUniqueId(), score) || saveNeeded;
			}
		}
		
		if(saveNeeded) this.milestones.save();
	}
	
	private boolean backtraceScores(UUID player, int score) {
		int milestone = 250000;
		boolean dirty = false;
		while(milestone <= score) {
			// add player entry to the milestone manager
			dirty = this.milestones.addToMilestone(player, milestone) || dirty;
			milestone = Util.getNextMilestone(milestone + 1);
		}
		return dirty;
	}

	@Override
	public void run() {
		if(this.objective == null) {
			// if objective doesn't exist, keep querying for it
			this.objective = this.scoreboard.getObjective("time");
			
			if(this.objective != null) {
				// if it now exists, get the initial scores
				this.getInitialScores();
			} else {
				// otherwise cancel the tasks
				return;
			}
		}
		
		// loop for each online player and check if they've passed any milestones
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(player == null || player.getUniqueId() == null) {
				continue;
			}
		
			// calculate current and cached scores, and calculate next milestone
			int score = this.objective.getScore(player.getName()).getScore();
			int cached = this.getCachedScore(player, score);
			int milestone = Util.getNextMilestone(cached);
			
			if(score > milestone && milestone != 0) {
				int place = this.milestones.getMilestonePlace(player, milestone);
				if(place > -1) {
					this.broadcastMilestone(player, milestone, place);
				}
			}
			
			// update cached score with current score
			if(score > cached) this.scoreCache.put(player.getUniqueId(), score);
		}
	}
	
	private int getCachedScore(Player player, int current) {
		UUID uuid = player.getUniqueId();
		if(!this.scoreCache.containsKey(uuid)) {
			this.scoreCache.put(uuid, current);
		}
		return this.scoreCache.get(uuid);
	}
	
	private void broadcastMilestone(Player player, int milestone, int place) {
		float hours = ((float)milestone / 20f) / 3600;
		String hourString = String.format("%.1f", hours);
		String placeString = Util.ordinal(place);
		
		String firstLine  = getString("strings.milestone." + (place == 1 ? "first-record" : "first"), this.config);
		String secondLine = getString("strings.milestone.second", this.config);
		String thirdLine  = getString("strings.milestone.third", this.config);
		
		Util.broadcast(String.format(firstLine, player.getDisplayName()));
		Util.broadcast(String.format(secondLine, milestone, hourString));
		Util.broadcast(String.format(thirdLine, placeString));
	}

}