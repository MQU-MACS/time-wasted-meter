package main.java.macs.timewasted.command;

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
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.google.common.collect.Lists;

import main.java.macs.timewasted.util.Util;

public class CommandLeaderboard implements CommandExecutor, TabCompleter {

	private static final int SCORES_PER_PAGE = 10;
	
	private final FileConfiguration config;
	
	public CommandLeaderboard(FileConfiguration config) {
		this.config = config;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length > 1) {
			sender.sendMessage(ChatColor.RED + "Usage: " + command.getUsage());
		} else {
			// get the objective (if it exists)
			Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
			Objective objective = scoreboard.getObjective("time");
			if(objective == null) {
				sender.sendMessage(ChatColor.RED + "Incorrectly configured, sorry!");
				return true;
			}
			
			// get all players
			OfflinePlayer[] players = Bukkit.getOfflinePlayers();
			
			// calculate number of pages
			int pageNo = args.length == 1 ? Util.tryParseInt(args[0]) - 1 : 0;
			int pages  = (int)Math.ceil((double)players.length / (double)SCORES_PER_PAGE);
			
			// ignore invalid page numbers
			if(pageNo < 0 || pageNo >= pages) {
				pageNo = 0;
			}
			
			// create leaderboard text
			sender.sendMessage(Util.getString("strings.leaderboard.first", this.config));
			
			List<LeaderboardScore> scores = getOrderedScores(players, scoreboard, objective);
			
			int start = pageNo * SCORES_PER_PAGE;
			int end   = Math.min(start + SCORES_PER_PAGE, players.length);
			
			for(int i = start; i < end; i++) {
				LeaderboardScore data = scores.get(i);
				String place = Util.ordinal(i+1);
				String name = data.getDisplayName();
				String score = String.format("%,d", data.getScore());
				
				if(data.isSamePlayer(sender)) {
					sender.sendMessage(String.format(Util.getString("strings.leaderboard.me", this.config), place, name, score));
				} else {
					sender.sendMessage(String.format(Util.getString("strings.leaderboard.other", this.config), place, name, score));
				}
			}
			
			sender.sendMessage(String.format(Util.getString("strings.leaderboard.page", this.config), pageNo+1, pages));
		}
		return true;
	}
	
	private List<LeaderboardScore> getOrderedScores(OfflinePlayer[] players, Scoreboard scoreboard, Objective objective) {
		return Arrays.asList(players)
				.stream()
				.map(p -> new LeaderboardScore(p, scoreboard, objective))
				.sorted()
				.collect(Collectors.toList());
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> list = Lists.newArrayList();
		if(args.length > 1) return list;
		
		OfflinePlayer[] players = Bukkit.getOfflinePlayers();
		int pages = (int)Math.ceil((double)players.length / (double)SCORES_PER_PAGE);
		
		for(int i = 0; i < pages; i++) {
			list.add(String.valueOf(i + 1));
		}
		
		return list.stream()
					.filter(s -> s.startsWith(args[0]))
					.collect(Collectors.toList());
	}

}