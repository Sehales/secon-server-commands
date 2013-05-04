package net.sehales.scservercmds;

import net.sehales.secon.SeCon;
import net.sehales.secon.utils.ChatUtils;

import org.bukkit.World;
import org.bukkit.entity.Player;

public class SCUtils {

	private ServerCmdCollection sc;
	private ChatUtils           chat = SeCon.getAPI().getChatUtils();

	SCUtils(ServerCmdCollection sc) {
		this.sc = sc;
	}

	public void setWeatherClear(Player player) {
		World w = player.getWorld();
		w.setThundering(false);
		w.setStorm(false);
		chat.sendFormattedMessage(player, sc.getLanguageInfoNode("weather.clear-msg"));
	}

	public void setWeatherRain(Player player, int duration) {
		World w = player.getWorld();
		w.setThundering(false);
		w.setStorm(true);
		w.setWeatherDuration(duration * 60 * 20);
		chat.sendFormattedMessage(player, sc.getLanguageInfoNode("weather.rain-msg").replace("<duration>", Integer.toString(duration)));
	}

	public void setWeatherStorm(Player player, int duration) {
		World w = player.getWorld();
		w.setThundering(true);
		w.setStorm(true);
		w.setWeatherDuration(duration * 60 * 20);
		chat.sendFormattedMessage(player, sc.getLanguageInfoNode("weather.storm-msg").replace("<duration>", Integer.toString(duration)));
	}

	public String timeToString(long time) {
		int hours = (int) ((Math.floor(time / 1000.0) + 8) % 24);
		int minutes = (int) Math.floor(time % 1000 / 1000.0 * 60);
		return String.format("%02d:%02d", hours, minutes);
	}
}
