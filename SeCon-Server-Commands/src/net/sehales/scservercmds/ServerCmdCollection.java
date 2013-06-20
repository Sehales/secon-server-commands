package net.sehales.scservercmds;

import net.sehales.secon.SeCon;
import net.sehales.secon.addon.SeConAddon;
import net.sehales.secon.annotations.SeConAddonHandler;
import net.sehales.secon.exception.DatabaseException;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

@SeConAddonHandler()
public class ServerCmdCollection extends SeConAddon {

	private SCUtils scu;

	void addConfigNode(String path, Object value) {
		if (!configContains(path))
			getConfig().set(path, value);
	}

	boolean configContains(String path) {
		return getConfig().contains(path);
	}

	FileConfiguration getConf() {
		return getConfig();
	}

	private void initConfig() {
		addConfigNode("kick.broadcast", true);
		addConfigNode("ban.broadcast", true);
		addConfigNode("unban.broadcast", true);

		saveConf();
	}

	private void initLanguage() {
		addLanguageInfoNode("kick.default-msg", "<red>You have been kicked by <green><sender>");
		addLanguageInfoNode("kick.broadcast-msg", "<gold><player> has been kicked: <green><message>");
		addLanguageInfoNode("kick.kicked-msg", "<gold>You have kicked <player> with the reason: <message>");
		addLanguageInfoNode("broadcast.prefix", "<gold>[Broadcast]<red>");
		addLanguageInfoNode("spawn.set-msg", "<gold>You have set the spawn of the world <blue><world> <gold>to <gray>x: <blue><x><gray>, y: <blue><y><gray>, z: <blue><z>");
		addLanguageInfoNode("spawn.teleported-msg", "<gold>You have been teleported to the spawn");
		addLanguageInfoNode("spawn.sender-teleported-msg", "<gold>You have teleported <green><player> <gold>to the spawn of world: <green><world>");
		addLanguageInfoNode("ban.default-reason", "You are banned");
		addLanguageInfoNode("ban.banned-msg", "<green><player> <red>has been banned by <gold><sender> <green>reason: <red><reason>");
		addLanguageInfoNode("ban.already-banned", "<green><player> <gold>is already banned");
		addLanguageInfoNode("ban.not-banned", "<green><player> <gold>is not banned");
		addLanguageInfoNode("ban.unbanned-msg", "<green><player> <gold>has been unbanned");
		addLanguageInfoNode("list.currently-online", "<gold>There are currently <green><amount><gold> of <green><max> <gold>Players online: <playerlist>");
		addLanguageInfoNode("whois.head", "<aqua><strikethrough>------------------------------");
		addLanguageInfoNode("whois.yes", "<green>yes");
		addLanguageInfoNode("whois.no", "<red>no");
		addLanguageInfoNode("whois.name", "<darkaqua>Name: <grey><name>");
		addLanguageInfoNode("whois.first-online", "<darkaqua>First-online: <grey><time>");
		addLanguageInfoNode("whois.last-online", "<darkaqua>Last-online: <grey><time>");
		addLanguageInfoNode("whois.op", "<darkaqua>Is operator: <value>");
		addLanguageInfoNode("whois.ip", "<darkaqua>Ip: <grey><value>");
		addLanguageInfoNode("weather.clear-msg", "<gold>You have cleared the weather");
		addLanguageInfoNode("weather.rain-msg", "<gold>It's now raining for <green><duration> <gold>minutes");
		addLanguageInfoNode("weather.storm-msg", "<gold>It's now stormy for <green><duration> <gold>minutes");
		addLanguageInfoNode("time.time-msg", "<gold>It's <green><realtime> <grey>(<timevalue> <gold>in server time<grey>)");
		addLanguageInfoNode("helpop.prefix", "<red>[HELPOP request by <player>] <green>");
		addLanguageInfoNode("helpop.request-sent", "<gold>Your request has been sent to online staff!");
		addLanguageInfoNode("baninfo.banned-msg", "<grey>'<green><player><grey>'<gold> was <red>permanently <gold>banned at <grey><date> <red>reason: <green><reason>");
		addLanguageInfoNode("baninfo.temp-banned-msg", "<grey>'<green><player><grey>'<gold> was banned at <grey><date> <gold>until <grey><enddate> <red>reason: <green><reason>");
		addLanguageInfoNode("baninfo.not-banned-msg", "<grey>'<green><player><grey>'<gold> is not banned");
		addLanguageInfoNode("banlist.no-banned-players-msg", "<gold>There are no banned players");
		addLanguageInfoNode("banlist.header", "<darkaqua>Banned players - Page: <pagenr>");
		addLanguageInfoNode("banlist.entry-permanent", "<gold>Player: <grey><player> <gold>- banned by: <grey><executor> <gold>- <red>permanent");
		addLanguageInfoNode("banlist.entry-temp", "<gold>Player: <grey><player> <gold>- banned by: <executor> <gold>- until: <date>");
		addLanguageInfoNode("banlist.page-header", "<darkaqua>>Banlist - page <page> of <maxpages>");
		addLanguageInfoNode("banlist.page-limit-exceeded", "<red>There are a maximum of <maxpages> pages. You have tried to get page <page>");
		addLanguageInfoNode("fakeop.fakeop-msg", "<yellow>You are now op!");
		addLanguageInfoNode("fakeop.sender-msg", "<gold>You have sent a fakeop message to <green><player>");
	}

	@Override
	protected void onDisable() {
	}

	@Override
	protected boolean onEnable(Plugin arg0) {
		if (!SeCon.getAPI().isOnlineMySQL()) {
			SeCon.getAPI().getLogger().warning("Server-Cmd-Collection", "MySQL isn't available, enable mysql and try again");
			return false;
		} else
			try {
				BanHandler.init();
			} catch (DatabaseException e) {
				e.printStackTrace();
				return false;
			}
		initConfig();
		initLanguage();
		this.scu = new SCUtils(this);
		registerCommands(new ServerCommands(this, this.scu));
		registerListener(new PlayerListener());
		return true;
	}

	void reloadConf() {
		reloadConfig();
	}

	void saveConf() {
		saveConfig();
	}

	void setConfigNode(String path, Object value) {
		getConfig().set(path, value);
	}

}
