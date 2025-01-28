package main.java.macs.timewasted;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import main.java.macs.timewasted.checker.MilestoneManager;
import main.java.macs.timewasted.checker.TimeChecker;
import main.java.macs.timewasted.command.CommandLeaderboard;
import main.java.macs.timewasted.command.CommandRecords;
import main.java.macs.timewasted.command.CommandTimeMeter;
import main.java.macs.timewasted.command.CommandTimeWasted;
import main.java.macs.timewasted.meter.BarPreferences;
import main.java.macs.timewasted.meter.TimeMeter;

public class TimeWastedMeter extends JavaPlugin {

	private MilestoneManager milestones;
	private TimeChecker checker;
	private TimeMeter meter;
	private BarPreferences barPrefs;
	private FileConfiguration config;
	
	private int checkerID;
	private int meterID;
	
	@Override
	public void onEnable() {
		this.saveDefaultConfig();
		this.config = this.getConfig();
		this.milestones = new MilestoneManager(this.getDataFolder(), this.getLogger());
		this.barPrefs = new BarPreferences(this.getDataFolder(), this.getLogger());
		this.checker = new TimeChecker(this.milestones, this.config);
		this.checkerID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this.checker, 0l, 20l);
		if(this.checkerID <= -1) {
			getLogger().log(Level.SEVERE, "Repeating checker task failed to schedule!");
		}
		this.meter = new TimeMeter(this.config, this.milestones, this.barPrefs);
		this.meterID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this, this.meter, 0L, 1L);
		if(this.meterID <= -1) {
			getLogger().log(Level.SEVERE, "Repeating meter task failed to schedule!");
		}
		Bukkit.getPluginManager().registerEvents(this.meter, this);
		getCommand("timewasted").setExecutor(new CommandTimeWasted(this, this.milestones, this.config));
		getCommand("timerecords").setExecutor(new CommandRecords(this.milestones, this.config));
		getCommand("timeleaderboard").setExecutor(new CommandLeaderboard(this.config));
		getCommand("timemeter").setExecutor(new CommandTimeMeter(this.barPrefs, this.meter));
	}
	
	@Override
	public void onDisable() {
		if(this.checkerID > -1) {
			Bukkit.getScheduler().cancelTask(this.checkerID);
		}
		if(this.meterID > -1) {
			Bukkit.getScheduler().cancelTask(this.meterID);
		}
		HandlerList.unregisterAll(this.meter);
	}

}