package main.java.macs.timewasted.checker;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

import main.java.macs.timewasted.util.Util;

public class MilestoneManager {

	private final HashMap<Integer, List<String>> milestoneMappings = new HashMap<Integer, List<String>>();
	private final HashMap<Integer, String> records = new HashMap<Integer, String>();
	
	private final Logger logger;
	private final File file;
	
	public MilestoneManager(File pluginFolder, Logger logger) {
		this.file = new File(pluginFolder, "milestones.json");
		this.logger = logger;
		this.load();
	}
	
	private void load() {
		// just quietly exit if file doesn't exist
		if(!this.file.exists()) {
			return;
		}
		
		try {
			// read json from file
			JsonReader reader = new JsonReader(new FileReader(this.file));
			JsonObject data = Util.GSON.fromJson(reader, JsonObject.class);
			JsonObject records = data.get("records").getAsJsonObject();
			reader.close();
			
			// read all player data
			this.milestoneMappings.clear();
			this.records.clear();
			
			// loop over every milestone that there's an entry for
			int milestone = 250000;
			while(data.has(String.valueOf(milestone))) {
				String milestoneStr = String.valueOf(milestone);
				JsonArray array = data.get(milestoneStr).getAsJsonArray();
				List<String> list = new ArrayList<String>();
				
				// copy strings to list
				for(int i = 0; i < array.size(); i++) {
					list.add(array.get(i).getAsString());
				}
				
				// get record for this milestone
				if(milestone >= 1000000) {
					if(records.has(milestoneStr)) {
						this.records.put(milestone, records.get(milestoneStr).getAsString());
					} else if(!list.isEmpty()) {
						// index 0 is usually the first one to reach it
						this.records.put(milestone, list.get(0));
					}
				}
				
				// add list to mappings and move to next milestone
				this.milestoneMappings.put(milestone, list);
				milestone = Util.getNextMilestone(milestone + 1);
			}
			
		} catch (IOException e) {
			this.logger.log(Level.SEVERE, "Failed to read milestone JSON data!", e);
		}
	}
	
	protected void save() {
		JsonObject data = new JsonObject();
		JsonObject records = new JsonObject();
		
		for(Integer milestone : this.milestoneMappings.keySet()) {
			// encode milestone data as json
			JsonArray array = new JsonArray();
			List<String> players = this.milestoneMappings.get(milestone);
			
			for(int i = 0; i < players.size(); i++) {
				array.add(players.get(i));
			}
			
			data.add(String.valueOf(milestone), array);
			
			if(milestone >= 1000000 && this.records.containsKey(milestone)) {
				records.addProperty(String.valueOf(milestone), this.records.get(milestone));
			}
		}
		
		data.add("records", records);
		
		// write to JSON file
		try {
			String json = Util.GSON.toJson(data);
			Util.writeToFile(json, this.file);
		} catch(Exception e) {
			this.logger.log(Level.SEVERE, "Failed to write milestone JSON data!", e);
		}
	}
	
	//-------------------------------------------
	
	/**
	 * Gets the place of this player for the current milestone, if they have
	 * reached it for the first time.
	 * @param player The player to check.
	 * @param milestone The milestone to check against.
	 * @return The place they came for this milestone, or -1 if they already achieved it.
	 */
	public int getMilestonePlace(Player player, int milestone) {
		String uuid = player.getUniqueId().toString();
		if(this.milestoneMappings.containsKey(milestone)) {
			List<String> milestoneMapping = this.milestoneMappings.get(milestone);
			if(!milestoneMapping.contains(uuid)) {
				milestoneMapping.add(player.getUniqueId().toString());
				this.save();
				return milestoneMapping.size();
			} else {
				return -1;
			}
		} else {
			List<String> milestoneMapping = new ArrayList<String>();
			milestoneMapping.add(uuid);
			this.milestoneMappings.put(milestone, milestoneMapping);
			this.records.put(milestone, player.getUniqueId().toString());
			this.save();
			return 1;
		}
	}
	
	public boolean addToMilestone(UUID uuid, int milestone) {
		List<String> players = this.milestoneMappings.get(milestone);
		if(players == null) {
			players = new ArrayList<String>();
			this.milestoneMappings.put(milestone, players);
		} else if(players.contains(uuid.toString())) {
			return false;
		}
		players.add(uuid.toString());
		return true;
	}
	
	public boolean hasAchieved(UUID player, int milestone) {
		if(!this.milestoneMappings.containsKey(milestone)) {
			return false;
		}
		return this.milestoneMappings.get(milestone).contains(player.toString());
	}
	
	public String getRecordHolderUUID(int milestone) {
		if(this.records.containsKey(milestone)) {
			return this.records.get(milestone);
		}
		return null;
	}
	
	public List<Integer> getRecordsHeldBy(OfflinePlayer player) {
		List<Integer> list = new ArrayList<Integer>();
		String uuid = player.getUniqueId().toString();
		
		int milestone = 1000000;
		while(this.records.containsKey(milestone)) {
			if(this.records.get(milestone).equals(uuid)) {
				list.add(milestone);
			}
			milestone = Util.getNextMilestone(milestone + 1);
		}
		
		return list;
	}
	
	public void saveData() {
		this.save();
	}
	
}