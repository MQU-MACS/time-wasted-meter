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

import main.java.macs.timewasted.checker.MilestoneManager;
import main.java.macs.timewasted.util.Util;

public class CommandRecords implements CommandExecutor, TabCompleter {

	private final MilestoneManager milestones;
	private final FileConfiguration config;
	
	public CommandRecords(MilestoneManager milestone, FileConfiguration config) {
		this.milestones = milestone;
		this.config = config;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length == 0) {
			sender.sendMessage(ChatColor.GOLD + "======[ " + ChatColor.BOLD + "RECORDS" + ChatColor.GOLD + " ]======");
			
			int milestone = 1000000;
			String holderUUID = this.milestones.getRecordHolderUUID(milestone);
			
			if(holderUUID == null) {
				sender.sendMessage(ChatColor.RED + "No record holders yet! Could it be you?");
			} else {
				
				while(holderUUID != null) {
					String name = Util.resolveNameForUUID(holderUUID);
					if(name != null) {
						sender.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + String.format("%,d", milestone));
						sender.sendMessage("    1st: " + ChatColor.GREEN + name);
					}
					
					milestone = Util.getNextMilestone(milestone + 1);
					holderUUID = this.milestones.getRecordHolderUUID(milestone);
				}
				
			}
		} else if(args.length == 1) {
			String targetName = args[0];
			OfflinePlayer target = Util.resolveOfflinePlayer(targetName);
			
			if(target == null) {
				sender.sendMessage(String.format(Util.getString("strings.command.noplayer", this.config), targetName));
			} else {
				sender.sendMessage(ChatColor.GOLD + "======[ " + ChatColor.BOLD + target.getName() + ChatColor.GOLD + " ]======");
				
				List<Integer> records = this.milestones.getRecordsHeldBy(target);
				if(records.isEmpty()) {
					sender.sendMessage(ChatColor.RED + "This person does not currently hold any records!");
				} else {
					for(int milestone : records) {
						String milestoneString = String.format("%,d", milestone);
						sender.sendMessage(ChatColor.GREEN + ChatColor.BOLD.toString() + milestoneString + ": " + ChatColor.RESET + "1st");
					}
				}
				
			}
			
		} else {
			sender.sendMessage(ChatColor.RED + "Usage: /timerecords [player]");
		}
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		if(args.length != 1) {
			return Arrays.asList();
		}
		
		List<String> list = new ArrayList<String>();
		for(OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			if(player.getName() != null) list.add(player.getName());
		}
		return list.stream()
				.filter(s -> s.startsWith(args[0]))
				.collect(Collectors.toList());
	}

}