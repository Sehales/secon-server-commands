
package net.sehales.scservercmds;

import net.sehales.secon.SeCon;
import net.sehales.secon.addon.Addon;
import net.sehales.secon.db.Database;
import net.sehales.secon.db.Database.DBType;
import net.sehales.secon.exception.DatabaseException;

import org.bukkit.configuration.file.FileConfiguration;

public class ServerCmdCollection extends Addon {
    
    private SCUtils           scu;
    private InvManagerWrapper invWrapper;
    
    void addConfigNode(String path, Object value) {
        if (!configContains(path)) {
            getConfig().set(path, value);
        }
    }
    
    boolean configContains(String path) {
        return getConfig().contains(path);
    }
    
    FileConfiguration getConf() {
        return getConfig();
    }
    
    public InvManagerWrapper getInvWrapper() {
        return invWrapper;
    }
    
    private void initConfig() {
        addConfigNode("kick.broadcast", true);
        addConfigNode("ban.broadcast", true);
        addConfigNode("unban.broadcast", true);
        
        saveConf();
    }
    
    private void initLanguage() {
        addLanguageNode("kick.default-msg", "<red>You have been kicked by <green><sender>");
        addLanguageNode("kick.broadcast-msg", "<gold><player> has been kicked: <green><message>");
        addLanguageNode("kick.kicked-msg", "<gold>You have kicked <player> with the reason: <message>");
        addLanguageNode("broadcast.prefix", "<gold>[Broadcast]<red>");
        addLanguageNode("spawn.set-msg", "<gold>You have set the spawn of the world <blue><world> <gold>to <gray>x: <blue><x><gray>, y: <blue><y><gray>, z: <blue><z>");
        addLanguageNode("spawn.teleported-msg", "<gold>You have been teleported to the spawn");
        addLanguageNode("spawn.sender-teleported-msg", "<gold>You have teleported <green><player> <gold>to the spawn of world: <green><world>");
        addLanguageNode("ban.default-reason", "You are banned");
        addLanguageNode("ban.banned-msg", "<green><player> <red>has been banned by <gold><sender> <green>reason: <red><reason>");
        addLanguageNode("ban.already-banned", "<green><player> <gold>is already banned");
        addLanguageNode("ban.not-banned", "<green><player> <gold>is not banned");
        addLanguageNode("ban.unbanned-msg", "<green><player> <gold>has been unbanned");
        addLanguageNode("list.currently-online", "<gold>There are currently <green><amount><gold> of <green><max> <gold>Players online: <playerlist>");
        addLanguageNode("whois.head", "<aqua><strikethrough>------------------------------");
        addLanguageNode("whois.yes", "<green>yes");
        addLanguageNode("whois.no", "<red>no");
        addLanguageNode("whois.name", "<darkaqua>Name: <grey><name>");
        addLanguageNode("whois.first-online", "<darkaqua>First-online: <grey><time>");
        addLanguageNode("whois.last-online", "<darkaqua>Last-online: <grey><time>");
        addLanguageNode("whois.op", "<darkaqua>Is operator: <value>");
        addLanguageNode("whois.ip", "<darkaqua>Ip: <grey><value>");
        addLanguageNode("weather.clear-msg", "<gold>You have cleared the weather");
        addLanguageNode("weather.rain-msg", "<gold>It's now raining for <green><duration> <gold>minutes");
        addLanguageNode("weather.storm-msg", "<gold>It's now stormy for <green><duration> <gold>minutes");
        addLanguageNode("time.time-msg", "<gold>It's <green><realtime> <grey>(<timevalue> <gold>in server time<grey>)");
        addLanguageNode("helpop.prefix", "<red>[HELPOP request by <player>] <green>");
        addLanguageNode("helpop.request-sent", "<gold>Your request has been sent to online staff!");
        addLanguageNode("baninfo.banned-msg", "<grey>'<green><player><grey>'<gold> was <red>permanently <gold>banned at <grey><date> <red>reason: <green><reason>");
        addLanguageNode("baninfo.temp-banned-msg", "<grey>'<green><player><grey>'<gold> was banned at <grey><date> <gold>until <grey><enddate> <red>reason: <green><reason>");
        addLanguageNode("baninfo.not-banned-msg", "<grey>'<green><player><grey>'<gold> is not banned");
        addLanguageNode("banlist.no-banned-players-msg", "<gold>There are no banned players");
        addLanguageNode("banlist.header", "<darkaqua>Banned players - Page: <pagenr>");
        addLanguageNode("banlist.entry-permanent", "<gold>Player: <grey><player> <gold>- banned by: <grey><executor> <gold>- <red>permanent");
        addLanguageNode("banlist.entry-temp", "<gold>Player: <grey><player> <gold>- banned by: <executor> <gold>- until: <date>");
        addLanguageNode("banlist.page-header", "<darkaqua>>Banlist - page <page> of <maxpages>");
        addLanguageNode("banlist.page-limit-exceeded", "<red>There are a maximum of <maxpages> pages. You have tried to get page <page>");
        addLanguageNode("fakeop.fakeop-msg", "<yellow>You are now op!");
        addLanguageNode("fakeop.sender-msg", "<gold>You have sent a fakeop message to <green><player>");
        addLanguageNode("message.msg", "<darkaqua>[<sender>] --> [YOU]: <white><message>");
        addLanguageNode("message.sender-msg", "<darkaqua>[YOU] --> [<receiver>]: <white><message>");
        addLanguageNode("reply.no-receiver", "<red>There is no one you can reply to");
        addLanguageNode("spy.header", "<red>[SPY]<darkaqua>[<sender>] --> [<receiver>]: <white><message>");
        addLanguageNode("spy.enabled", "<gold>Spy mode enabled");
        addLanguageNode("spy.disabled", "<gold>Spy mode disabled");
    }
    
    @Override
    protected boolean onEnable() {
        SeCon secon = SeCon.getInstance();
        Database db = null;
        if ((db = secon.getSQLDB()) != null && (db.getType().compareTo(DBType.MYSQL) != 0)) {
            secon.log().warning("Server-Cmd-Collection", "MySQL isn't available, enable mysql and try again");
            return false;
        } else {
            try {
                BanHandler.init(db);
            } catch (DatabaseException e) {
                e.printStackTrace();
                return false;
            }
        }
        
        initConfig();
        initLanguage();
        this.scu = new SCUtils(this);
        registerCommandsFromObject(new ServerCommands(this, this.scu));
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
