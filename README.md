# time-wasted-meter
The Time wasted meter plugin for the MACS Minecraft server, updated to work with spigot 1.21+

## Prerequisites
- Java 21
- Maven
- A Spigot server running 1.21+

## Installation
1. Clone the repo
```sh
git clone https://github.com/MQU-MACS/time-wasted-meter.git

cd time-wasted-meter
```

2. Build the plugin

```sh
mvn clean package
```
3. Copy the generated JAR file from the `target` directory to your server's `plugins` folder

4. Restart the server

## Plugin commands

### Player Commands
- `/timewasted [player]` - Check your time wasted stats or another player's
- `/timerecords [player]` - View the records set by players
- `/timeleaderboard [page]` - View the leaderboard of scores
- `/timemeter <show|hide|colour|style>` - Configure the time meter on your screen

### Time Meter Customisation
Players can customize their time meter display using:
- `/timemeter show` - Show the time meter
- `/timemeter hide` - Hide the time meter
- `/timemeter colour <colour>` - Change the meter's color
- `/timemeter style <style>` - Change the meter's style

## Credits

Built on [Romejanic's time-wasted-meter](https://github.com/Romejanic/time-wasted-meter.git)
