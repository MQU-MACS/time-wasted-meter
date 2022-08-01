package macs.timewasted.command;

import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.Objective;

import macs.timewasted.checker.MilestoneManager;
import macs.timewasted.util.Util;

public class MilestoneAggregate {

	public final int currentMilestone;
	public final int nextMilestone;
	public final int rawScore;
	public final int difference;
	public final int progress;
	
	private MilestoneAggregate(int current, int next, int raw, int difference, int progress) {
		this.currentMilestone = current;
		this.nextMilestone = next;
		this.rawScore = raw;
		this.difference = difference;
		this.progress = progress;
	}
	
	public static MilestoneAggregate aggregateFor(OfflinePlayer player, Objective objective, MilestoneManager milestones) {
		// step 1: find current and next milestones
		int current = 0, next = 250000;
		while(milestones.hasAchieved(player.getUniqueId(), next)) {
			current = next;
			next = Util.getNextMilestone(next + 1);
		}
		
		// step 2: find current score
		int score = objective.getScore(player.getName()).getScore();
		
		// step 3: calculate difference and progress
		int difference = next - current;
		int progress = score - current;
		
		return new MilestoneAggregate(current, next, score, difference, progress);	
	}
	
}