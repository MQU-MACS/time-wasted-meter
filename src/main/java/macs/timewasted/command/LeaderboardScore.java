package main.java.macs.timewasted.command;

import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class LeaderboardScore implements Comparable<LeaderboardScore> {

	private OfflinePlayer player;
	private Score score;
	private Team team;
	
	public LeaderboardScore(OfflinePlayer player, Scoreboard scoreboard, Objective objective) {
		this.player = player;
		this.score = objective.getScore(player.getName());
		this.team = scoreboard.getEntryTeam(player.getName());
	}
	
	public String getName() {
		return this.player.getName();
	}
	
	public String getDisplayName() {
		if(this.team == null) return getName();
		return this.team.getColor() + getName();
	}
	
	public int getScore() {
		return this.score.getScore();
	}
	
	public boolean isSamePlayer(CommandSender sender) {
		if(sender instanceof Player) {
			return ((Player)sender).getUniqueId().equals(this.player.getUniqueId());
		}
		return false;
	}
	
	@Override
	public int compareTo(LeaderboardScore other) {
		return other.getScore() - getScore();
	}

}