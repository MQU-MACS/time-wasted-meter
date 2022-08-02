package macs.timewasted.meter;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import macs.timewasted.util.Util;

public class BarPreferences {

	private static Prefs DEFAULT_PREFS;
	private final Map<UUID, Prefs> prefs = new HashMap<>();
	
	private final Logger logger;
	private final File file;
	
	public BarPreferences(File pluginFolder, Logger logger) {
		this.logger = logger;
		this.file = new File(pluginFolder, "bar-prefs.json");
		this.load();
	}
	
	private void load() {
		try {
			if(!this.file.exists()) {
				this.logger.info("No preferences file, skipping");
				return;
			}
			
			FileReader fr = new FileReader(this.file);
			JsonObject data = Util.GSON.fromJson(fr, JsonObject.class);
			
			this.prefs.clear();
			
			for(Entry<String,JsonElement> e : data.entrySet()) {
				if(!e.getValue().isJsonObject()) continue;
				UUID uuid = UUID.fromString(e.getKey());
				Prefs prefs = new Prefs();
				prefs.read(e.getValue().getAsJsonObject());
				this.prefs.put(uuid, prefs);
			}
		} catch(Exception e) {
			this.logger.log(Level.SEVERE, "Failed to read bar preferences!", e);
		}
	}
	
	public void save() {
		try {
			JsonObject data = new JsonObject();
			for(UUID uuid : this.prefs.keySet()) {
				Prefs prefs = this.prefs.get(uuid);
				this.logger.info(String.valueOf(prefs.same(DEFAULT_PREFS)));
				if(prefs.same(DEFAULT_PREFS)) continue;
				
				JsonObject prefJson = new JsonObject();
				prefs.write(prefJson);
				data.add(uuid.toString(), prefJson);
			}
			
			FileWriter writer = new FileWriter(this.file);
			String json = Util.GSON.toJson(data);
			writer.write(json);
			writer.close();
			this.logger.info("Saved bar preferences");
		} catch(Exception e) {
			this.logger.log(Level.SEVERE, "Failed to write bar preferences!", e);
		}
	}
	
	public Prefs forPlayer(Player player) {
		if(!this.prefs.containsKey(player.getUniqueId())) {
			Prefs prefs = new Prefs();
			DEFAULT_PREFS.copy(prefs);
			this.prefs.put(player.getUniqueId(), prefs);
		}
		return this.prefs.get(player.getUniqueId());
	}
	
	public static class Prefs {
		public boolean hidden;
		public BarColor color;
		public BarStyle style;
		
		public void copy(Prefs prefs) {
			prefs.hidden = hidden;
			prefs.color = color;
			prefs.style = style;
		}
		
		public boolean same(Prefs prefs) {
			return prefs.hidden == hidden &&
					prefs.color.equals(color) &&
					prefs.style.equals(style);
		}
		
		private void read(JsonObject obj) {
			this.hidden = obj.get("hidden").getAsBoolean();
			this.color = BarColor.valueOf(obj.get("color").getAsString());
			this.style = BarStyle.valueOf(obj.get("style").getAsString());
		}
		
		private void write(JsonObject obj) {
			obj.addProperty("hidden", this.hidden);
			obj.addProperty("color", this.color.name());
			obj.addProperty("style", this.style.name());
		}
	}
	
	static {
		DEFAULT_PREFS = new Prefs();
		DEFAULT_PREFS.hidden = true;
		DEFAULT_PREFS.color = BarColor.YELLOW;
		DEFAULT_PREFS.style = BarStyle.SEGMENTED_10;
	}
	
}