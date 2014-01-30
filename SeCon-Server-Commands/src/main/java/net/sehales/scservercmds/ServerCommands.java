
package net.sehales.scservercmds;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import net.sehales.scplayercmds.InvisibilityManager;
import net.sehales.secon.SeCon;
import net.sehales.secon.addon.Addon;
import net.sehales.secon.command.CommandType;
import net.sehales.secon.command.MethodCommandHandler;
import net.sehales.secon.command.SeConCommand;
import net.sehales.secon.config.LanguageConfig;
import net.sehales.secon.player.SCPlayer;
import net.sehales.secon.utils.MiscUtils;
import net.sehales.secon.utils.PageBuilder;
import net.sehales.secon.utils.chat.ChatUtils;
import net.sehales.secon.utils.plugin.PluginUtils;
import net.sehales.secon.utils.plugin.VanishUtils;
import net.sehales.secon.utils.string.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class ServerCommands {
    
    private ServerCmdCollection sc;
    private SCUtils             utils;
    private LanguageConfig      lang = SeCon.getInstance().getLang();
    
    ServerCommands(ServerCmdCollection sc, SCUtils utils) {
        this.sc = sc;
        this.utils = utils;
    }
    
    @MethodCommandHandler(name = "ban", description = "<darkaqua>ban a player forever (that is a long time!)", usage = "<darkaqua>/ban [player] [ban message]", permission = "secon.command.ban")
    public void onBanCmd(CommandSender sender, SeConCommand cmd, String[] args) {
        if (args.length > 0) {
            String reason = null;
            if (args.length > 1) {
                reason = StringUtils.getStringOfArray(args, 1);
            }
            String playerName = args[0];
            if (reason == null) {
                reason = sc.getLanguageNode("ban.default-reason");
            }
            if (BanHandler.ban(playerName, sender.getName(), reason)) {
                String banMsg = sc.getLanguageNode("ban.banned-msg").replace("<player>", playerName).replace("<sender>", sender.getName()).replace("<reason>", reason);
                Player p = Bukkit.getPlayerExact(playerName);
                if (p != null) {
                    p.kickPlayer(ChatUtils.formatMessage(reason));
                }
                if (sc.getConf().getBoolean("ban.broadcast")) {
                    ChatUtils.broadcastFormattedMessage(banMsg);
                }
                ChatUtils.sendFormattedMessage(Bukkit.getConsoleSender(), banMsg);
                
            } else {
                ChatUtils.sendFormattedMessage(sender, sc.getLanguageNode("ban.already-banned").replace("<player>", playerName));
            }
        } else {
            ChatUtils.sendFormattedMessage(sender, lang.NOT_ENOUGH_ARGUMENTS);
        }
    }
    
    @MethodCommandHandler(name = "baninfo", description = "<darkaqua>check if a user is banned or not", usage = "<darkaqua>/baninfo [player]", permission = "secon.command.baninfo", aliases = "isbanned,checkban,bancheck")
    public void onBanInfoCmd(CommandSender sender, SeConCommand cmd, String[] args) {
        if (args.length > 0) {
            BanInfo info = BanHandler.getBanInfo(args[0]);
            if (info != null) {
                if (info.isTempban()) {
                    ChatUtils.sendFormattedMessage(sender, sc.getLanguageNode("baninfo.temp-banned-msg").replace("<player>", args[0]).replace("<enddate>", DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(info.getEndDate())).replace("<date>", DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(info.getBanDate()).replace("<date>", DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(info.getBanDate()))).replace("<reason>", info.getReason()));
                } else {
                    ChatUtils.sendFormattedMessage(sender, sc.getLanguageNode("baninfo.banned-msg").replace("<player>", args[0]).replace("<date>", DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(info.getBanDate())).replace("<reason>", info.getReason()));
                }
            } else {
                ChatUtils.sendFormattedMessage(sender, sc.getLanguageNode("baninfo.not-banned-msg").replace("<player>", args[0]));
            }
        }
    }
    
    @MethodCommandHandler(name = "banlist", description = "<darkaqua>list all banned players", usage = "<darkaqua>/banlist", permission = "secon.command.banlist", aliases = "listbans,bannedplayers")
    public void onBanListCmd(CommandSender sender, SeConCommand cmd, String[] args) {
        List<BanInfo> banInfoList = BanHandler.listBans();
        if (banInfoList != null) {
            List<String> msg = new ArrayList<String>();
            String permMsg = sc.getLanguageNode("banlist.entry-permanent");
            String tmpMsg = sc.getLanguageNode("banlist.entry-temp");
            for (BanInfo info : banInfoList) {
                String entry = null;
                if (info.isTempban()) {
                    entry = tmpMsg.replace("<player>", info.getPlayerName()).replace("<executor>", info.getExecutorName()).replace("<date>", DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(info.getEndDate()));
                } else {
                    entry = permMsg.replace("<player>", info.getPlayerName()).replace("<executor>", info.getExecutorName());
                }
                msg.add(entry);
            }
            
            PageBuilder<String> pb = new PageBuilder<String>(msg);
            pb.setEntrysPerPage(8);
            pb.build();
            int page = 0;
            if (args.length > 0) {
                try {
                    int i = Integer.parseInt(args[0]) - 1;
                    if (i < 0) {
                        i = 0;
                    }
                    page = i;
                } catch (NumberFormatException e) {
                    
                }
            }
            List<String> helpPage = pb.getPage(page);
            
            if (helpPage != null) {
                ChatUtils.sendFormattedMessage(sender, sc.getLanguageNode("banlist.page-header").replace("<page>", Integer.toString(page + 1)).replace("<maxpages>", Integer.toString(pb.size())));
                ChatUtils.sendFormattedMessage(sender, helpPage);
            } else {
                ChatUtils.sendFormattedMessage(sender, sc.getLanguageNode("banlist.page-limit-exceeded").replace("<page>", Integer.toString(page + 1)).replace("<maxpages>", Integer.toString(pb.size())));
            }
        } else {
            ChatUtils.sendFormattedMessage(sender, sc.getLanguageNode("banlist.no-banned-players-msg"));
        }
    }
    
    @MethodCommandHandler(name = "broadcast", description = "<darkaqua>broadcast a server wide message", usage = "<darkaqua>/broadcast [message]", permission = "secon.command.broadcast")
    public void onBroadcastCmd(CommandSender sender, SeConCommand cmd, String[] args) {
        if (args.length > 0) {
            String msg = sc.getLanguageNode("broadcast.prefix") + StringUtils.getStringOfArray(args, 0);
            ChatUtils.broadcastFormattedMessage(msg);
            ChatUtils.sendFormattedMessage(Bukkit.getConsoleSender(), msg);
        } else {
            ChatUtils.sendFormattedMessage(sender, lang.NOT_ENOUGH_ARGUMENTS);
        }
    }
    
    @MethodCommandHandler(name = "fakeop", description = "<darkaqua>send a fake 'you are now op' message to a player", usage = "<darkaqua>/fakeop [player]", permission = "secon.command.fakeop")
    public void onFakeOpCmd(CommandSender sender, SeConCommand cmd, String[] args) {
        if (args.length > 0) {
            Player p = Bukkit.getPlayer(args[0]);
            if (p == null) {
                ChatUtils.sendFormattedMessage(sender, lang.PLAYER_NOT_FOUND.replace("<player>", args[0]));
                return;
            }
            ChatUtils.sendFormattedMessage(p, sc.getLanguageNode("fakeop.fakeop-msg"));
            ChatUtils.sendFormattedMessage(sender, sc.getLanguageNode("fakeop.sender-msg").replace("<player>", p.getName()));
        } else {
            ChatUtils.sendFormattedMessage(sender, lang.NOT_ENOUGH_ARGUMENTS);
        }
    }
    
    @MethodCommandHandler(name = "helpop", description = "<darkaqua>send a message to online staff", usage = "<darkaqua>/helop [message]", permission = "secon.command.helpop", additionalPerms = "receive:secon.command.helpop.receive", type = CommandType.PLAYER)
    public void onHelpOpCmd(Player player, SeConCommand cmd, String[] args) {
        if (args.length > 0) {
            String msg = sc.getLanguageNode("helpop.prefix").replace("<player>", player.getName()) + StringUtils.getStringOfArray(args, 0);
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (MiscUtils.hasPermission(player, cmd.getPermission("receive"), false)) {
                    ChatUtils.sendFormattedMessage(p, msg);
                }
            }
            ChatUtils.sendFormattedMessage(Bukkit.getConsoleSender(), msg);
            ChatUtils.sendFormattedMessage(player, sc.getLanguageNode("helpop.request-sent"));
        } else {
            ChatUtils.sendFormattedMessage(player, lang.NOT_ENOUGH_ARGUMENTS);
        }
    }
    
    @MethodCommandHandler(name = "kick", description = "<darkaqua>kick another player with an optional message", usage = "<darkaqua>/kick [player] [kick message]", permission = "secon.command.kick")
    public void onKickCmd(CommandSender sender, SeConCommand cmd, String[] args) {
        if (args.length > 0) {
            Player p = Bukkit.getPlayer(args[0]);
            if (p == null) {
                ChatUtils.sendFormattedMessage(sender, lang.PLAYER_NOT_FOUND.replace("<player>", args[0]));
                return;
            }
            String kickreason;
            if (args.length > 1) {
                kickreason = StringUtils.getStringOfArray(args, 1);
            } else {
                kickreason = sc.getLanguageNode("kick.default-msg").replace("<sender>", sender.getName());
            }
            p.kickPlayer(ChatUtils.formatMessage(kickreason));
            ChatUtils.sendFormattedMessage(sender, sc.getLanguageNode("kick.kicked-msg").replace("<player>", p.getName()).replace("<message>", kickreason));
            if (sc.getConf().getBoolean("kick.broadcast")) {
                ChatUtils.broadcastFormattedMessage(sc.getLanguageNode("kick.broadcast-msg").replace("<player>", p.getName()).replace("<message>", kickreason));
            }
        } else {
            ChatUtils.sendFormattedMessage(sender, lang.NOT_ENOUGH_ARGUMENTS);
        }
    }
    
    @MethodCommandHandler(name = "list", description = "<darkaqua>get a list of all online players", usage = "<darkaqua>/list", permission = "secon.command.list", aliases = "who,playerlist")
    public void onListCmd(CommandSender sender, SeConCommand cmd, String[] args) {
        StringBuilder sb = new StringBuilder();
        InvisibilityManager invManager = null;
        boolean canSeeHiddenPlayers = true, canSeeVanishedPlayers = true;
        int amount = 0;
        
        Addon addon = SeCon.getInstance().getAddonManager().getAddon("SeCon-Player-Commands");
        if (addon != null) {
            invManager = new InvManagerWrapper(addon).getInstance();
        }
        
        if (invManager != null) {
            canSeeHiddenPlayers = invManager.canSeeOthers(sender);
        }
        
        PluginUtils pu = SeCon.getInstance().getPluginUtils();
        VanishUtils vanishUtils = null;
        if (sender instanceof Player && pu.isVanishEnabled()) {
            vanishUtils = pu.getVanishUtls();
            canSeeVanishedPlayers = vanishUtils.canSeeVanishedPlayers((Player) sender);
        }
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            if (invManager != null) {
                if (!canSeeHiddenPlayers && invManager.isHidden(p)) {
                    continue;
                }
            }
            if (vanishUtils != null) {
                if (!canSeeVanishedPlayers && vanishUtils.isVanished(p)) {
                    continue;
                }
            }
            
            String playerPrefix = null, playerSuffix = null;
            if (pu.isVaultEnabled()) {
                playerPrefix = pu.getVaultUtils().getPlayerPrefix(Bukkit.getWorlds().get(0).getName(), p.getName());
                playerSuffix = pu.getVaultUtils().getPlayerSuffix(Bukkit.getWorlds().get(0).getName(), p.getName());
            }
            
            if (playerPrefix == null || playerPrefix.equals("")) {
                playerPrefix = "";
            }
            if (playerSuffix == null || playerSuffix.equals("")) {
                playerSuffix = "";
            }
            
            sb.append("<grey>" + playerPrefix + p.getName() + playerSuffix + "<grey>, ");
            amount++;
        }
        
        ChatUtils.sendFormattedMessage(sender, sc.getLanguageNode("list.currently-online").replace("<max>", Integer.toString(Bukkit.getServer().getMaxPlayers())).replace("<amount>", Integer.toString(amount)).replace("<playerlist>", sb.toString() != null
                                                                                                                                                                                                                                        && !sb.toString().isEmpty() ? sb.substring(0, sb.length() - 8) : ""));
    }
    
    @MethodCommandHandler(name = "message", description = "<darkaqua>send a message to another player or to the console", usage = "<darkaqua>/message [player name|console]", permission = "secon.command.message", aliases = "msg,m,pm,tell")
    public void onMessageCmd(CommandSender sender, SeConCommand cmd, String[] args) {
        if (args.length > 1) {
            String receiverName = args[0];
            String message = StringUtils.getStringOfArray(args, 1);
            
            if (receiverName.equalsIgnoreCase("console")) {
                utils.sendPrivateMessage(sender, Bukkit.getConsoleSender(), message);
            } else {
                Player p = Bukkit.getPlayer(receiverName);
                if (p == null) {
                    ChatUtils.sendFormattedMessage(sender, lang.PLAYER_NOT_FOUND.replace("<player>", args[0]));
                    return;
                }
                utils.sendPrivateMessage(sender, p, message);
            }
        } else {
            ChatUtils.sendFormattedMessage(sender, lang.NOT_ENOUGH_ARGUMENTS);
        }
    }
    
    @MethodCommandHandler(name = "reply", description = "<darkaqua>reply to the last message sender", usage = "<darkaqua>/reply [message]", permission = "secon.command.reply", aliases = "r")
    public void onReplyCmd(CommandSender sender, SeConCommand cmd, String[] args) {
        if (args.length > 0) {
            String message = StringUtils.getStringOfArray(args, 0);
            if (!utils.reply(sender, message)) {
                ChatUtils.sendFormattedMessage(sender, sc.getLanguageNode("reply.no-receiver"));
            }
        } else {
            ChatUtils.sendFormattedMessage(sender, lang.NOT_ENOUGH_ARGUMENTS);
        }
    }
    
    @MethodCommandHandler(name = "setspawn", description = "set the world's spawn location at where you are standing", usage = "<darkaqua>/setspawn", permission = "secon.command.spawn", type = CommandType.PLAYER)
    public void onSetSpawnCmd(Player sender, SeConCommand cmd, String[] args) {
        World w = sender.getWorld();
        Location l = sender.getLocation();
        int x = l.getBlockX();
        int y = l.getBlockY();
        int z = l.getBlockZ();
        w.setSpawnLocation(x, y, z);
        ChatUtils.sendFormattedMessage(sender, sc.getLanguageNode("spawn.set-msg").replace("<x>", Integer.toString(x)).replace("<y>", Integer.toString(y)).replace("<z>", Integer.toString(z)).replace("<world>", w.getName()));
    }
    
    @MethodCommandHandler(name = "spawn", description = "<darkaqua>teleport yourself or another player to the world's spawn", usage = "<darkaqua>/spawn [player]", additionalPerms = "other:secon.command.spawn.other", permission = "secon.command.spawn")
    public void onSpawnCmd(CommandSender sender, SeConCommand cmd, String[] args) {
        if (args.length > 0) {
            if (MiscUtils.hasPermission(sender, cmd.getPermission("other"), true)) {
                Player p = Bukkit.getPlayer(args[0]);
                if (p == null) {
                    ChatUtils.sendFormattedMessage(sender, lang.PLAYER_NOT_FOUND.replace("<player>", args[0]));
                    return;
                }
                p.teleport(p.getWorld().getSpawnLocation(), TeleportCause.COMMAND);
                ChatUtils.sendFormattedMessage(p, sc.getLanguageNode("spawn.teleported-msg").replace("<world>", p.getWorld().getName()));
                ChatUtils.sendFormattedMessage(sender, sc.getLanguageNode("spawn.sender-teleported-msg").replace("<player>", p.getName()).replace("<world>", p.getWorld().getName()));
            }
            
        } else if (sender instanceof Player) {
            Player p = ((Player) sender).getPlayer();
            p.teleport(p.getWorld().getSpawnLocation(), TeleportCause.COMMAND);
        }
    }
    
    @MethodCommandHandler(name = "spy", description = "<darkaqua>receive all private messages", usage = "<darkaqua>/spy", permission = "secon.command.spy", type = CommandType.PLAYER)
    public void onSpyCmd(Player player, SeConCommand cmd, String[] args) {
        SCPlayer scp = SeCon.getInstance().getPlayerManager().getPlayer(player.getName());
        if (scp.hasData("spymode")) {
            scp.removeData("spymode");
            ChatUtils.sendFormattedMessage(player, sc.getLanguageNode("spy.disabled"));
        } else {
            scp.putData("spymode", "true");
            ChatUtils.sendFormattedMessage(player, sc.getLanguageNode("spy.enabled"));
        }
    }
    
    @MethodCommandHandler(name = "tempban", description = "<darkaqua>temporary ban a player (time in minutes)", usage = "<darkaqua>/tempban [player] [1d2h] [ban message]", permission = "secon.command.tempban")
    public void onTempBanCmd(CommandSender sender, SeConCommand cmd, String[] args) {
        if (args.length > 0) {
            long time = -1;
            String reason = null;
            if (args.length > 1) {
                try {
                    time = TimeUtils.getTimestamp(args[1]);
                } catch (Exception e) {
                    ChatUtils.sendFormattedMessage(sender, lang.NOT_ENOUGH_ARGUMENTS);
                    return;
                }
            }
            if (time == -1) {
                ChatUtils.sendFormattedMessage(sender, lang.NOT_ENOUGH_ARGUMENTS);
                return;
            }
            if (args.length > 2) {
                reason = StringUtils.getStringOfArray(args, 2);
            }
            String playerName = args[0];
            if (reason == null) {
                reason = sc.getLanguageNode("ban.default-reason");
            }
            if (BanHandler.tempban(playerName, sender.getName(), reason, time)) {
                String banMsg = sc.getLanguageNode("ban.banned-msg").replace("<player>", playerName).replace("<sender>", sender.getName()).replace("<reason>", reason);
                Player p = Bukkit.getPlayerExact(playerName);
                if (p != null) {
                    p.kickPlayer(ChatUtils.formatMessage(reason));
                }
                if (sc.getConf().getBoolean("ban.broadcast")) {
                    ChatUtils.broadcastFormattedMessage(banMsg);
                }
                ChatUtils.sendFormattedMessage(Bukkit.getConsoleSender(), banMsg);
            } else {
                ChatUtils.sendFormattedMessage(sender, sc.getLanguageNode("ban.already-banned").replace("<player>", playerName));
            }
        } else {
            ChatUtils.sendFormattedMessage(sender, lang.NOT_ENOUGH_ARGUMENTS);
        }
    }
    
    @MethodCommandHandler(name = "time", description = "<darkaqua>change the time of the world", usage = "<darkaqua>/time [day|noon|evening|night|18000]", permission = "secon.command.time")
    public void onTimeCmd(Player player, SeConCommand cmd, String[] args) {
        World w = player.getWorld();
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("day")) {
                w.setTime(0);
            } else if (args[0].equalsIgnoreCase("noon")) {
                w.setTime(6000);
            } else if (args[0].equalsIgnoreCase("evening")) {
                w.setTime(12000);
            } else if (args[0].equalsIgnoreCase("night")) {
                w.setTime(14000);
            } else {
                try {
                    long time = Long.parseLong(args[0]);
                    w.setTime(time);
                } catch (Exception e) {
                    ChatUtils.sendFormattedMessage(player, lang.NOT_ENOUGH_ARGUMENTS);
                }
            }
            
        } else {
            long time = w.getTime();
            ChatUtils.sendFormattedMessage(player, sc.getLanguageNode("time.time-msg").replace("<realtime>", utils.timeToString(w.getTime())).replace("<timevalue>", Long.toString(time)));
        }
        
    }
    
    @MethodCommandHandler(name = "unban", description = "<darkaqua>unban a player", usage = "<darkaqua>/unban [player]", permission = "secon.command.unban")
    public void onUnBanCmd(CommandSender sender, SeConCommand cmd, String[] args) {
        if (args.length > 0) {
            String playerName = args[0];
            if (!BanHandler.isBanned(playerName)) {
                ChatUtils.sendFormattedMessage(sender, sc.getLanguageNode("ban.not-banned").replace("<player>", playerName));
                return;
            }
            BanHandler.unban(playerName);
            ChatUtils.sendFormattedMessage(Bukkit.getConsoleSender(), sc.getLanguageNode("ban.unbanned-msg").replace("<player>", playerName));
            if (sc.getConf().getBoolean("unban.broadcast")) {
                ChatUtils.broadcastFormattedMessage(sc.getLanguageNode("ban.unbanned-msg").replace("<player>", playerName));
            }
        } else {
            ChatUtils.sendFormattedMessage(sender, lang.NOT_ENOUGH_ARGUMENTS);
        }
    }
    
    @MethodCommandHandler(name = "weatherclear", description = "<darkaqua>clear the weather", usage = "<darkaqua>/weather", permission = "secon.command.weatherclear", aliases = "wclear,clearweather")
    public void onWeatherClearCmd(Player player, SeConCommand cmd, String[] args) {
        if (args.length > 0) {
            int duration;
            try {
                duration = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                ChatUtils.sendFormattedMessage(player, lang.NOT_ENOUGH_ARGUMENTS);
                return;
            }
            utils.setWeatherClear(player, duration);
        } else {
            utils.setWeatherClear(player, 10);
        }
    }
    
    @MethodCommandHandler(name = "weatherrain", description = "<darkaqua>let it rain, default 10 minutes", usage = "<darkaqua>/weatherrain [duration]", permission = "secon.command.weatherrain", aliases = "rain,wrain,letitrain")
    public void onWeatherRainCmd(Player player, SeConCommand cmd, String[] args) {
        if (args.length > 0) {
            int duration;
            try {
                duration = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                ChatUtils.sendFormattedMessage(player, lang.NOT_ENOUGH_ARGUMENTS);
                return;
            }
            utils.setWeatherRain(player, duration);
        } else {
            utils.setWeatherRain(player, 10);
        }
    }
    
    @MethodCommandHandler(name = "weatherstorm", description = "<darkaqua>make the weather stormy, default 10 minutes", usage = "<darkaqua>/weatherstorm [duration]", permission = "secon.command.weatherstorm", aliases = "storm,wstorm")
    public void onWeatherStormCmd(Player player, SeConCommand cmd, String[] args) {
        if (args.length > 0) {
            int duration;
            try {
                duration = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                ChatUtils.sendFormattedMessage(player, lang.NOT_ENOUGH_ARGUMENTS);
                return;
            }
            utils.setWeatherStorm(player, duration);
        } else {
            utils.setWeatherStorm(player, 10);
        }
    }
    
    @MethodCommandHandler(name = "whois", description = "<darkaqua>get some information about a player", usage = "<darkaqua>/whois [player]", permission = "secon.command.whois", aliases = "playerinfo,pinfo")
    public void onWhoisCmd(CommandSender sender, SeConCommand cmd, String[] args) {
        if (args.length > 0) {
            OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);
            if (p == null) {
                ChatUtils.sendFormattedMessage(sender, lang.PLAYER_NOT_FOUND.replace("<player>", args[0]));
                return;
            }
            List<String> info = new ArrayList<String>();
            
            String playerPrefix = null, playerSuffix = null;
            PluginUtils pu = SeCon.getInstance().getPluginUtils();
            if (pu.isVaultEnabled()) {
                playerPrefix = pu.getVaultUtils().getPlayerPrefix(Bukkit.getWorlds().get(0).getName(), p.getName());
                playerSuffix = pu.getVaultUtils().getPlayerSuffix(Bukkit.getWorlds().get(0).getName(), p.getName());
            }
            
            if (playerPrefix == null || playerPrefix.equals("")) {
                playerPrefix = "";
            }
            if (playerSuffix == null || playerSuffix.equals("")) {
                playerSuffix = "";
            }
            
            info.add(sc.getLanguageNode("whois.head"));
            info.add(sc.getLanguageNode("whois.name").replace("<name>", playerPrefix + p.getName() + playerSuffix));
            info.add(sc.getLanguageNode("whois.op").replace("<value>", p.isOp() ? sc.getLanguageNode("whois.yes") : sc.getLanguageNode("whois.no")));
            
            info.add(sc.getLanguageNode("whois.first-online").replace("<time>", DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(p.getFirstPlayed())));
            info.add(sc.getLanguageNode("whois.last-online").replace("<time>", DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(p.getLastPlayed())));
            if (p.isOnline()) {
                info.add(sc.getLanguageNode("whois.ip").replace("<value>", p.getPlayer().getAddress().toString().substring(1)));
            }
            
            ChatUtils.sendFormattedMessage(sender, info);
        } else {
            ChatUtils.sendFormattedMessage(sender, lang.NOT_ENOUGH_ARGUMENTS);
        }
    }
}
