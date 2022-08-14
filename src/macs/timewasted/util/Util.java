package macs.timewasted.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Util {
	
	public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

	public static int getNextMilestone(int score) {
		if(score < 1000000) {
			return (int)Math.ceil(score / 250000.0) * 250000;
		} else {
			return (int)Math.ceil(score / 500000.0) * 500000;
		}
	}

	public static void writeToFile(String data, File file) throws IOException {
		FileOutputStream out = new FileOutputStream(file);
		out.write(data.getBytes(StandardCharsets.UTF_8));
		out.flush();
		out.close();
	}
	
	/*
	 * Thanks Bohemian!
	 * Source: https://stackoverflow.com/questions/6810336/is-there-a-way-in-java-to-convert-an-integer-to-its-ordinal-name
	 */
	public static String ordinal(int i) {
	    String[] suffixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
	    switch (i % 100) {
	    case 11:
	    case 12:
	    case 13:
	        return i + "th";
	    default:
	        return i + suffixes[i % 10];

	    }
	}
	
	public static String getString(String key, FileConfiguration config) {
		return ChatColor.translateAlternateColorCodes('&', config.getString(key));
	}
	
	public static String formatHourMinute(float hours) {
		String hourString = null;
		if(hours > 1f) {
			hourString = String.valueOf((int)hours);
			hours -= (int)hours;
		}

		String minuteString = String.valueOf((int)(hours * 60f));
		return (hourString != null ? hourString + " hr " : "") + minuteString + " min";
	}
	
	public static int tryParseInt(String in) {
		try {
			return Integer.parseInt(in);
		} catch(NumberFormatException e) {
			return 0;
		}
	}
	
	public static double clamp(double val, double min, double max) {
		return Math.min(Math.max(val, min), max);
	}
	
	public static boolean isPlayer(CommandSender sender) {
		return sender instanceof Player;
	}
	
	/**
	 * Resolves an OfflinePlayer based on username, but only if they
	 * have played on this server before (to avoid making a web request
	 * for a player's UUID).
	 * @param name The name the resolve.
	 * @return The resolved OfflinePlayer, or null if they have never played
	 * on the server.
	 */
	public static OfflinePlayer resolveOfflinePlayer(String name) {
		for(OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			if(player.getName() != null && player.getName().equalsIgnoreCase(name)) {
				return player;
			}
		}
		return null;
	}
	
	public static String resolveNameForUUID(String uuid) {
		for(OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			if(player.getUniqueId().toString().equals(uuid)) {
				return player.getName();
			}
		}
		return null;
	}
	
	public static OfflinePlayer resolvePlayerForUUID(UUID uuid) {
		for(OfflinePlayer player : Bukkit.getOfflinePlayers()) {
			if(player.getUniqueId().equals(uuid)) {
				return player;
			}
		}
		return null;
	}
	
	/**
	 * Broadcasts a message to the entire server. This is a temporary fix
	 * for this issue: https://github.com/Romejanic/time-wasted-meter/issues/2
	 * @param message The message to broadcast
	 */
	public static void broadcast(String message) {
		Logger.getLogger("Broadcast").info(message);
		for(Player player : Bukkit.getOnlinePlayers()) {
			player.sendMessage(message);
		}
	}
	
}