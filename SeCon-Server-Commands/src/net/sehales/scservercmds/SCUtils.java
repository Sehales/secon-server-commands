package net.sehales.scservercmds;

import java.util.HashMap;
import java.util.Map;

import net.sehales.secon.SeCon;
import net.sehales.secon.utils.ChatUtils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SCUtils {

	private ServerCmdCollection sc;
	private ChatUtils           chat     = SeCon.getAPI().getChatUtils();
	private Map<String, String> replyMap = new HashMap<String, String>();

	SCUtils(ServerCmdCollection sc) {
		this.sc = sc;
	}

	public boolean reply(CommandSender sender, String message) {
		if (replyMap.containsKey(sender.getName())) {

			String name = replyMap.get(sender.getName());
			if (name.equals(Bukkit.getConsoleSender().getName())) {
				sendPrivateMessage(sender, Bukkit.getConsoleSender(), message);
				return true;
			}

			Player receiver = Bukkit.getPlayer(name);
			if (receiver == null)
				return false;

			sendPrivateMessage(sender, Bukkit.getConsoleSender(), message);
			return true;

		}
		return false;
	}

	public void sendPrivateMessage(CommandSender sender, CommandSender receiver, String message) {
		replyMap.put(receiver.getName(), sender.getName());

		chat.sendFormattedMessage(sender, sc.getLanguageInfoNode("message.sender-msg").replace("<sender>", sender.getName()).replace("<receiver", receiver.getName()).replace("<message>", message));
		chat.sendFormattedMessage(receiver, sc.getLanguageInfoNode("message.msg").replace("<sender>", sender.getName()).replace("<receiver", receiver.getName()).replace("<message>", message));

	}

	public void setWeatherClear(Player player, int duration) {
		World w = player.getWorld();
		w.setThundering(false);
		w.setStorm(false);
		w.setWeatherDuration(duration * 60 * 20);
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
