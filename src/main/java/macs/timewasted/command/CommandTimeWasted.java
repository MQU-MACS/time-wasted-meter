package main.java.macs.timewasted.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import main.java.macs.timewasted.checker.MilestoneManager;
import main.java.macs.timewasted.util.Util;

public class CommandTimeWasted implements CommandExecutor, TabCompleter {

	private final JavaPlugin plugin;
	private final MilestoneManager milestones;
	private final FileConfiguration config;
	
	public CommandTimeWasted(JavaPlugin plugin, MilestoneManager milestones, FileConfiguration config) {
		this.plugin = plugin;
		this.milestones = milestones;
		this.config = config;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length > 1) {
			sender.sendMessage(getUsage(sender));
		} else {
			// find the desired target to check
			String targetName = args.length == 1 ? args[0] : null;
			OfflinePlayer target;
			if(targetName == null) {
				// if sender is player, target themselves
				if(sender instanceof Player) {
					target = (Player)sender;
				} else {
					// if sender is console, show usage
					sender.sendMessage(getUsage(sender));
					return true;
				}
			} else {
				// if player provided username, try and resolve it
				target = Util.resolveOfflinePlayer(targetName);
				if(target == null) {
					sender.sendMessage(ChatColor.RED + "No player named \"" + targetName + "\" found!");
					return true;
				}
			}
			
			// asynchronously aggregate the milestone statistics
			Bukkit.getScheduler().runTaskAsynchronously(this.plugin, () -> {
				
				Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
				Objective objective = scoreboard.getObjective("time");
				
				MilestoneAggregate stats = MilestoneAggregate.aggregateFor(target, objective, this.milestones);
				int numRecords = this.milestones.getRecordsHeldBy(target).size();
				
				float hours = ((float)stats.rawScore / 20f) / 3600f;
				float remaining = (float)(stats.difference - stats.progress) / 20f / 3600f;
				
				String scoreString   = String.format("%,d", stats.rawScore);
				String currentString = String.format("%,d", stats.currentMilestone);
				String nextString    = String.format("%,d", stats.nextMilestone);
				String hourString    = String.format("%.1f", hours);
				String remainString  = Util.formatHourMinute(remaining);
				
				// send data as text
				// ======[ jackd5011 ]======
				// Wasted: 1,275,492 (~ 14.2 hours)
				//
				// 1,000,000       2,000,000
				// [=========--------------]
				sender.sendMessage(String.format(Util.getString("strings.command.first", config), target.getName()));
				sender.sendMessage(String.format(Util.getString("strings.command.second", config), scoreString, hourString));
				sender.sendMessage(ChatColor.GREEN + "Time to next milestone: " + ChatColor.BOLD + remainString);
				if(numRecords > 0) {
					String recordsString = String.format("%,d", numRecords);
					sender.sendMessage(ChatColor.GOLD + "This person holds " + ChatColor.BOLD + recordsString + ChatColor.GOLD + " records!");
				}
				sender.sendMessage("");
				sender.sendMessage(Util.getString("strings.command.third", config));
				sender.sendMessage(padToLength(currentString, nextString, 42));
				sender.sendMessage(progressBar(stats.progress, stats.difference, 30));
			});
			
		}
		return true;
	}
	
	private String progressBar(int value, int max, int steps) {
		StringBuilder sb = new StringBuilder();
		sb.append(ChatColor.WHITE + "[");
		sb.append(ChatColor.GREEN);
		
		int midpoint = value * steps / max;
		boolean flag = false;
		for(int i = 0; i < steps; i++) {
			if(i <= midpoint) sb.append("=");
			else {
				if(!flag) {
					flag = true;
					sb.append(ChatColor.GRAY);
				}
				sb.append("-");
			}
		}
		
		sb.append(ChatColor.WHITE + "]");
		return sb.toString();
	}
	
	private String padToLength(String a, String b, int maxlength) {
		StringBuilder sb = new StringBuilder();
		sb.append(a);
		
		int spaces = maxlength - a.length() - b.length();
		for(int i = 0; i < spaces; i++) {
			sb.append(" ");
		}
		
		sb.append(b);
		return sb.toString();
	}
	
	private String getUsage(CommandSender sender) {
		String prefix = ChatColor.RED + "Usage: /timewasted ";
//		if(sender.isOp()) return prefix + " []";
		if(sender instanceof Player) return prefix + " [target]";
		else return prefix + " <target>";
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if(args.length != 1) {
			return Arrays.asList();
		}
		
		List<String> list = new ArrayList<String>();
		
		// add all players
		for(OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			if(player.getName() != null) list.add(player.getName());
		}
		
		// add subcommands (op only)
//		if(sender.isOp()) {
//			list.add("")
//		}
		
		return list.stream()
				.filter(s -> s.startsWith(args[0]))
				.collect(Collectors.toList());
	}

}