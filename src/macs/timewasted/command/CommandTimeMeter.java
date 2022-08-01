package macs.timewasted.command;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import macs.timewasted.meter.BarPreferences;

public class CommandTimeMeter implements CommandExecutor, TabCompleter {

	private BarPreferences prefs;
	
	public CommandTimeMeter(BarPreferences prefs) {
		this.prefs = prefs;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(args.length < 1) {
			sender.sendMessage(ChatColor.RED + "Usage: " + command.getUsage());
			return true;
		}
		if(!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED + "You must be a player to use this command!");
			return true;
		}
		
		
		
		return true;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
		List<String> options = Lists.newArrayList();
		
		switch(args.length) {
		case 1:
			options.addAll(Arrays.asList("show", "hide", "colour", "style"));
			break;
		case 2:
			if(args[0].equalsIgnoreCase("colour")) {
				options.addAll(
					Arrays.asList(BarColor.values())
					.stream()
					.map(c -> c.name().toLowerCase())
					.collect(Collectors.toList())
				);
			} else if(args[0].equalsIgnoreCase("style")) {
				options.addAll(
					Arrays.asList(BarStyle.values())
					.stream()
					.map(c -> c.name().toLowerCase())
					.collect(Collectors.toList())
				);
			}
		default:
			break;
		}
		
		return options.stream()
					.filter(s -> args[args.length-1].toLowerCase().startsWith(s))
					.collect(Collectors.toList());
	}

}