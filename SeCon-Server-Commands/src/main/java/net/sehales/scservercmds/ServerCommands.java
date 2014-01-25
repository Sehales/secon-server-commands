package net.sehales.scservercmds;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import net.sehales.secon.SeCon;
import net.sehales.secon.VaultManager;
import net.sehales.secon.addon.SeConCommand;
import net.sehales.secon.annotations.SeConCommandHandler;
import net.sehales.secon.config.LanguageHelper;
import net.sehales.secon.enums.CommandType;
import net.sehales.secon.obj.PageBuilder;
import net.sehales.secon.player.SeConPlayer;
import net.sehales.secon.utils.ChatUtils;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

public class ServerCommands {

	private ChatUtils           chat = SeCon.getAPI().getChatUtils();
	private ServerCmdCollection sc;
	private SCUtils             utils;

	ServerCommands(ServerCmdCollection sc, SCUtils utils) {
		this.sc = sc;
		this.utils = utils;
	}

	@SeConCommandHandler(name = "ban", help = "<darkaqua>ban a player forever (that is a long time!);<darkaqua>usage: /ban [player] [ban message]", permission = "secon.command.ban")
	public void onBanCmd(CommandSender sender, SeConCommand cmd, String[] args) {
		if (args.length > 0) {
			String reason = null;
			if (args.length > 1)
				reason = chat.getStringOfArray(args, 1);
			String playerName = args[0];
			if (reason == null)
				reason = sc.getLanguageInfoNode("ban.default-reason");
			if (BanHandler.ban(playerName, sender.getName(), reason)) {
				String banMsg = sc.getLanguageInfoNode("ban.banned-msg").replace("<player>", playerName).replace("<sender>", sender.getName()).replace("<reason>", reason);
				Player p = Bukkit.getPlayerExact(playerName);
				if (p != null)
					p.kickPlayer(chat.formatMessage(reason));
				if (sc.getConf().getBoolean("ban.broadcast"))
					chat.broadcastFormattedMessage(banMsg);
				SeCon.getAPI().getLogger().println(null, chat.formatMessage(banMsg));

			} else
				chat.sendFormattedMessage(sender, sc.getLanguageInfoNode("ban.already-banned").replace("<player>", playerName));
		} else
			chat.sendFormattedMessage(sender, LanguageHelper.INFO_WRONG_ARGUMENTS);
	}

	@SeConCommandHandler(name = "baninfo", help = "<darkaqua>check if a user is banned or not;<darkaqua>usage: /baninfo [player]", permission = "secon.command.baninfo", aliases = "isbanned,checkban,bancheck")
	public void onBanInfoCmd(CommandSender sender, SeConCommand cmd, String[] args) {
		if (args.length > 0) {
			BanInfo info = BanHandler.getBanInfo(args[0]);
			if (info != null) {
				if (info.isTempban())
					chat.sendFormattedMessage(
					        sender,
					        sc.getLanguageInfoNode("baninfo.temp-banned-msg")
					                .replace("<player>", args[0])
					                .replace("<enddate>", DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(info.getEndDate()))
					                .replace(
					                        "<date>",
					                        DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(info.getBanDate())
					                                .replace("<date>", DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(info.getBanDate())))
					                .replace("<reason>", info.getReason()));
				else
					chat.sendFormattedMessage(
					        sender,
					        sc.getLanguageInfoNode("baninfo.banned-msg").replace("<player>", args[0])
					                .replace("<date>", DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(info.getBanDate())).replace("<reason>", info.getReason()));
			} else
				chat.sendFormattedMessage(sender, sc.getLanguageInfoNode("baninfo.not-banned-msg").replace("<player>", args[0]));
		}
	}

	@SeConCommandHandler(name = "banlist", help = "<darkaqua>list all banned players;<darkaqua>usage: /banlist", permission = "secon.command.banlist", aliases = "listbans,bannedplayers")
	public void onBanListCmd(CommandSender sender, SeConCommand cmd, String[] args) {
		List<BanInfo> banInfoList = BanHandler.listBans();
		if (banInfoList != null) {
			List<String> msg = new ArrayList<String>();
			String permMsg = sc.getLanguageInfoNode("banlist.entry-permanent");
			String tmpMsg = sc.getLanguageInfoNode("banlist.entry-temp");
			for (BanInfo info : banInfoList) {
				String entry = null;
				if (info.isTempban())
					entry = tmpMsg.replace("<player>", info.getPlayerName()).replace("<executor>", info.getExecutorName())
					        .replace("<date>", DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(info.getEndDate()));
				else
					entry = permMsg.replace("<player>", info.getPlayerName()).replace("<executor>", info.getExecutorName());
				msg.add(entry);
			}

			PageBuilder<String> pb = new PageBuilder<String>(msg);
			pb.setEntrysPerPage(8);
			pb.build();
			int page = 0;
			if (args.length > 0)
				try {
					int i = Integer.parseInt(args[0]) - 1;
					if (i < 0)
						i = 0;
					page = i;
				} catch (NumberFormatException e) {

				}
			List<String> helpPage = pb.getPage(page);

			if (helpPage != null) {
				chat.sendFormattedMessage(sender, sc.getLanguageInfoNode("banlist.page-header").replace("<page>", Integer.toString(page + 1)).replace("<maxpages>", Integer.toString(pb.size())));
				chat.sendFormattedMessage(sender, helpPage);
			} else
				chat.sendFormattedMessage(sender, sc.getLanguageInfoNode("banlist.page-limit-exceeded").replace("<page>", Integer.toString(page + 1))
				        .replace("<maxpages>", Integer.toString(pb.size())));
		} else
			chat.sendFormattedMessage(sender, sc.getLanguageInfoNode("banlist.no-banned-players-msg"));
	}

	@SeConCommandHandler(name = "broadcast", help = "<darkaqua>broadcast a server wide message;<darkaqua>usage: /broadcast [message]", permission = "secon.command.broadcast")
	public void onBroadcastCmd(CommandSender sender, SeConCommand cmd, String[] args) {
		if (args.length > 0) {
			String msg = sc.getLanguageInfoNode("broadcast.prefix") + chat.getStringOfArray(args, 0);
			chat.broadcastFormattedMessage(msg);
			SeCon.getAPI().getLogger().info(null, chat.formatMessage(msg));
		} else
			chat.sendFormattedMessage(sender, LanguageHelper.INFO_WRONG_ARGUMENTS);
	}

	@SeConCommandHandler(name = "fakeop", help = "<darkaqua>send a fake 'you are now op' message to a player;<darkaqua>usage: /fakeop [player]", permission = "secon.command.fakeop")
	public void onFakeOpCmd(CommandSender sender, SeConCommand cmd, String[] args) {
		if (args.length > 0) {
			Player p = Bukkit.getPlayer(args[0]);
			if (p == null) {
				chat.sendFormattedMessage(sender, LanguageHelper.INFO_PLAYER_NOT_EXIST.replace("<player>", args[0]));
				return;
			}
			chat.sendFormattedMessage(p, sc.getLanguageInfoNode("fakeop.fakeop-msg"));
			chat.sendFormattedMessage(sender, sc.getLanguageInfoNode("fakeop.sender-msg").replace("<player>", p.getName()));
		} else
			chat.sendFormattedMessage(sender, LanguageHelper.INFO_WRONG_ARGUMENTS);
	}

	@SeConCommandHandler(name = "helpop", help = "<darkaqua>send a message to online staff;<darkaqua>usage: /helop [message]", permission = "secon.command.helpop", additionalPerms = "receive:secon.command.helpop.receive", type = CommandType.PLAYER)
	public void onHelpOpCmd(Player player, SeConCommand cmd, String[] args) {
		if (args.length > 0) {
			String msg = sc.getLanguageInfoNode("helpop.prefix").replace("<player>", player.getName()) + chat.getStringOfArray(args, 0);
			for (Player p : Bukkit.getOnlinePlayers())
				if (SeCon.getAPI().getSeConUtils().hasPermission(player, cmd.getPermission("receive"), false))
					chat.sendFormattedMessage(p, msg);
			chat.sendFormattedMessage(player, sc.getLanguageInfoNode("helpop.request-sent"));
		} else
			chat.sendFormattedMessage(player, LanguageHelper.INFO_WRONG_ARGUMENTS);
	}

	@SeConCommandHandler(name = "kick", help = "<darkaqua>kick another player with an optional message;<darkaqua>usage: /kick [player] [kick message]", permission = "secon.command.kick")
	public void onKickCmd(CommandSender sender, SeConCommand cmd, String[] args) {
		if (args.length > 0) {
			Player p = Bukkit.getPlayer(args[0]);
			if (p == null) {
				chat.sendFormattedMessage(sender, LanguageHelper.INFO_PLAYER_NOT_EXIST.replace("<player>", args[0]));
				return;
			}
			String kickreason;
			if (args.length > 1)
				kickreason = SeCon.getAPI().getChatUtils().getStringOfArray(args, 1);
			else
				kickreason = sc.getLanguageInfoNode("kick.default-msg").replace("<sender>", sender.getName());
			p.kickPlayer(chat.formatMessage(kickreason));
			chat.sendFormattedMessage(sender, sc.getLanguageInfoNode("kick.kicked-msg").replace("<player>", p.getName()).replace("<message>", kickreason));
			if (sc.getConf().getBoolean("kick.broadcast"))
				chat.broadcastFormattedMessage(sc.getLanguageInfoNode("kick.broadcast-msg").replace("<player>", p.getName()).replace("<message>", kickreason));
		} else
			chat.sendFormattedMessage(sender, LanguageHelper.INFO_WRONG_ARGUMENTS);
	}

	@SeConCommandHandler(name = "list", help = "<darkaqua>get a list of all online players;<darkaqua>usage: /list", permission = "secon.command.list", aliases = "who,playerlist")
	public void onListCmd(CommandSender sender, SeConCommand cmd, String[] args) {
		StringBuilder sb = new StringBuilder();
		InvManager invManager = null;
		boolean canSeeOthers = true;
		int amount = 0;

		if (SeCon.getAPI().isAddonAPIOnline("InvisibilityManager"))
			invManager = new InvManager();

		if (invManager != null)
			canSeeOthers = invManager.getInstance().canSeeOthers(sender);

		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			if (invManager != null)
				if (!canSeeOthers && invManager.getInstance().isHidden(p))
					continue;
			sb.append("<grey>" + SeCon.getAPI().getSeConUtils().getPlayerPrefix(p.getWorld(), p) + p.getName() + SeCon.getAPI().getSeConUtils().getPlayerSuffix(p.getWorld(), p) + "<grey>, ");
			amount++;
		}

		chat.sendFormattedMessage(
		        sender,
		        sc.getLanguageInfoNode("list.currently-online").replace("<max>", Integer.toString(Bukkit.getServer().getMaxPlayers())).replace("<amount>", Integer.toString(amount))
		                .replace("<playerlist>", sb.toString() != null && !sb.toString().isEmpty()? sb.substring(0, sb.length() - 8) : ""));
	}

	@SeConCommandHandler(name = "message", help = "<darkaqua>send a message to another player or to the console;<darkaqua>usage: /message [player name|console]", permission = "secon.command.message", aliases = "msg,m,pm,tell")
	public void onMessageCmd(CommandSender sender, SeConCommand cmd, String[] args) {
		if (args.length > 1) {
			String receiverName = args[0];
			String message = chat.getStringOfArray(args, 1);

			if (receiverName.equalsIgnoreCase("console"))
				utils.sendPrivateMessage(sender, Bukkit.getConsoleSender(), message);
			else {
				Player p = Bukkit.getPlayer(receiverName);
				if (p == null) {
					chat.sendFormattedMessage(sender, LanguageHelper.INFO_PLAYER_NOT_EXIST.replace("<player>", args[0]));
					return;
				}
				utils.sendPrivateMessage(sender, p, message);
			}
		} else
			chat.sendFormattedMessage(sender, LanguageHelper.INFO_WRONG_ARGUMENTS);
	}

	@SeConCommandHandler(name = "reply", help = "<darkaqua>reply to the last message sender;<darkaqua>usage: /reply [message]", permission = "secon.command.reply", aliases = "r")
	public void onReplyCmd(CommandSender sender, SeConCommand cmd, String[] args) {
		if (args.length > 0) {
			String message = chat.getStringOfArray(args, 0);
			if (!utils.reply(sender, message))
				chat.sendFormattedMessage(sender, sc.getLanguageInfoNode("reply.no-receiver"));
		} else
			chat.sendFormattedMessage(sender, LanguageHelper.INFO_WRONG_ARGUMENTS);
	}

	@SeConCommandHandler(name = "setspawn", help = "set the world's spawn location at where you are standing;<darkaqua>usage: /setspawn", permission = "secon.command.spawn", type = CommandType.PLAYER)
	public void onSetSpawnCmd(Player sender, SeConCommand cmd, String[] args) {
		World w = sender.getWorld();
		Location l = sender.getLocation();
		int x = l.getBlockX();
		int y = l.getBlockY();
		int z = l.getBlockZ();
		w.setSpawnLocation(x, y, z);
		chat.sendFormattedMessage(sender,
		        sc.getLanguageInfoNode("spawn.set-msg").replace("<x>", Integer.toString(x)).replace("<y>", Integer.toString(y)).replace("<z>", Integer.toString(z)).replace("<world>", w.getName()));
	}

	@SeConCommandHandler(name = "spawn", help = "<darkaqua>teleport yourself or another player to the world's spawn;<darkaqua>usage: /spawn [player]", additionalPerms = "other:secon.command.spawn.other", permission = "secon.command.spawn")
	public void onSpawnCmd(CommandSender sender, SeConCommand cmd, String[] args) {
		if (args.length > 0) {
			if (SeCon.getAPI().getSeConUtils().hasPermission(sender, cmd.getPermission("other"), true)) {
				Player p = Bukkit.getPlayer(args[0]);
				if (p == null) {
					chat.sendFormattedMessage(sender, LanguageHelper.INFO_PLAYER_NOT_EXIST.replace("<player>", args[0]));
					return;
				}
				p.teleport(p.getWorld().getSpawnLocation(), TeleportCause.COMMAND);
				chat.sendFormattedMessage(p, sc.getLanguageInfoNode("spawn.teleported-msg").replace("<world>", p.getWorld().getName()));
				chat.sendFormattedMessage(sender, sc.getLanguageInfoNode("spawn.sender-teleported-msg").replace("<player>", p.getName()).replace("<world>", p.getWorld().getName()));
			}

		} else if (sender instanceof Player) {
			Player p = ((Player) sender).getPlayer();
			p.teleport(p.getWorld().getSpawnLocation(), TeleportCause.COMMAND);
		}
	}

	@SeConCommandHandler(name = "spy", help = "<darkaqua>receive all private messages;<darkaqua>usage: /spy", permission = "secon.command.spy", type = CommandType.PLAYER)
	public void onSpyCmd(Player player, SeConCommand cmd, String[] args) {
		SeConPlayer scp = SeCon.getAPI().getPlayerManager().getPlayer(player.getName());
		if (scp.hasData("spymode")) {
			scp.removeData("spymode");
			chat.sendFormattedMessage(player, sc.getLanguageInfoNode("spy.disabled"));
		} else {
			scp.addData("spymode", true);
			chat.sendFormattedMessage(player, sc.getLanguageInfoNode("spy.enabled"));
		}
	}

	@SeConCommandHandler(name = "tempban", help = "<darkaqua>temporary ban a player (time in minutes);<darkaqua>usage: /tempban [player] [1d2h] [ban message]", permission = "secon.command.tempban")
	public void onTempBanCmd(CommandSender sender, SeConCommand cmd, String[] args) {
		if (args.length > 0) {
			long time = -1;
			String reason = null;
			if (args.length > 1)
				try {
					time = TimeUtils.getTimestamp(args[1]);
				} catch (Exception e) {
					chat.sendFormattedMessage(sender, LanguageHelper.INFO_WRONG_ARGUMENTS);
					return;
				}
			if (time == -1) {
				chat.sendFormattedMessage(sender, LanguageHelper.INFO_WRONG_ARGUMENTS);
				return;
			}
			if (args.length > 2)
				reason = chat.getStringOfArray(args, 2);
			String playerName = args[0];
			if (reason == null)
				reason = sc.getLanguageInfoNode("ban.default-reason");
			if (BanHandler.tempban(playerName, sender.getName(), reason, time)) {
				String banMsg = sc.getLanguageInfoNode("ban.banned-msg").replace("<player>", playerName).replace("<sender>", sender.getName()).replace("<reason>", reason);
				Player p = Bukkit.getPlayerExact(playerName);
				if (p != null)
					p.kickPlayer(chat.formatMessage(reason));
				if (sc.getConf().getBoolean("ban.broadcast"))
					chat.broadcastFormattedMessage(banMsg);
				SeCon.getAPI().getLogger().println(null, chat.formatMessage(banMsg));
			} else
				chat.sendFormattedMessage(sender, sc.getLanguageInfoNode("ban.already-banned").replace("<player>", playerName));
		} else
			chat.sendFormattedMessage(sender, LanguageHelper.INFO_WRONG_ARGUMENTS);
	}

	@SeConCommandHandler(name = "time", help = "<darkaqua>change the time of the world;<darkaqua>usage: /time [day|noon|evening|night]")
	public void onTimeCmd(Player player, SeConCommand cmd, String[] args) {
		World w = player.getWorld();
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("day"))
				w.setTime(0);
			else if (args[0].equalsIgnoreCase("noon"))
				w.setTime(6000);
			else if (args[0].equalsIgnoreCase("evening"))
				w.setTime(12000);
			else if (args[0].equalsIgnoreCase("night"))
				w.setTime(14000);
			else
				try {
					long time = Long.parseLong(args[0]);
					w.setTime(time);
				} catch (Exception e) {
					chat.sendFormattedMessage(player, LanguageHelper.INFO_WRONG_ARGUMENTS);
				}

		} else {
			long time = w.getTime();
			chat.sendFormattedMessage(player, sc.getLanguageInfoNode("time.time-msg").replace("<realtime>", utils.timeToString(w.getTime())).replace("<timevalue>", Long.toString(time)));
		}

	}

	@SeConCommandHandler(name = "unban", help = "<darkaqua>unban a player;<darkaqua>usage: /unban [player]", permission = "secon.command.unban")
	public void onUnBanCmd(CommandSender sender, SeConCommand cmd, String[] args) {
		if (args.length > 0) {
			String playerName = args[0];
			if (!BanHandler.isBanned(playerName)) {
				chat.sendFormattedMessage(sender, sc.getLanguageInfoNode("ban.not-banned").replace("<player>", playerName));
				return;
			}
			BanHandler.unban(playerName);
			SeCon.getAPI().getLogger().println(null, chat.formatMessage(sc.getLanguageInfoNode("ban.unbanned-msg").replace("<player>", playerName)));
			if (sc.getConf().getBoolean("unban.broadcast"))
				chat.broadcastFormattedMessage(sc.getLanguageInfoNode("ban.unbanned-msg").replace("<player>", playerName));
		} else
			chat.sendFormattedMessage(sender, LanguageHelper.INFO_WRONG_ARGUMENTS);
	}

	@SeConCommandHandler(name = "weatherclear", help = "<darkaqua>clear the weather;<darkaqua>usage: /weather", permission = "secon.command.weatherclear", aliases = "wclear,clearweather")
	public void onWeatherClearCmd(Player player, SeConCommand cmd, String[] args) {
		if (args.length > 0) {
			int duration;
			try {
				duration = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				chat.sendFormattedMessage(player, LanguageHelper.INFO_WRONG_ARGUMENTS);
				return;
			}
			utils.setWeatherClear(player, duration);
		} else
			utils.setWeatherClear(player, 10);
	}

	@SeConCommandHandler(name = "weatherrain", help = "<darkaqua>let it rain, default 10 minutes;<darkaqua>usage: /weatherrain [duration]", permission = "secon.command.weatherrain", aliases = "rain,wrain,letitrain")
	public void onWeatherRainCmd(Player player, SeConCommand cmd, String[] args) {
		if (args.length > 0) {
			int duration;
			try {
				duration = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				chat.sendFormattedMessage(player, LanguageHelper.INFO_WRONG_ARGUMENTS);
				return;
			}
			utils.setWeatherRain(player, duration);
		} else
			utils.setWeatherRain(player, 10);
	}

	@SeConCommandHandler(name = "weatherstorm", help = "<darkaqua>make the weather stormy, default 10 minutes;<darkaqua>usage: /weatherstorm [duration]", permission = "secon.command.weatherstorm", aliases = "storm,wstorm")
	public void onWeatherStormCmd(Player player, SeConCommand cmd, String[] args) {
		if (args.length > 0) {
			int duration;
			try {
				duration = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				chat.sendFormattedMessage(player, LanguageHelper.INFO_WRONG_ARGUMENTS);
				return;
			}
			utils.setWeatherStorm(player, duration);
		} else
			utils.setWeatherStorm(player, 10);
	}

	@SeConCommandHandler(name = "whois", help = "<darkaqua>get some information about a player;<darkaqua>usage: /whois [player]", permission = "secon.command.whois", aliases = "playerinfo,pinfo")
	public void onWhoisCmd(CommandSender sender, SeConCommand cmd, String[] args) {
		if (args.length > 0) {
			OfflinePlayer p = Bukkit.getOfflinePlayer(args[0]);
			if (p == null) {
				chat.sendFormattedMessage(sender, LanguageHelper.INFO_PLAYER_NOT_EXIST.replace("<player>", args[0]));
				return;
			}
			List<String> info = new ArrayList<String>();

			info.add(sc.getLanguageInfoNode("whois.head"));
			info.add(sc.getLanguageInfoNode("whois.name").replace("<name>",
			        VaultManager.chat.getPlayerPrefix(Bukkit.getWorlds().get(0), p.getName()) + p.getName() + VaultManager.chat.getPlayerSuffix(Bukkit.getWorlds().get(0), p.getName())));
			info.add(sc.getLanguageInfoNode("whois.op").replace("<value>", p.isOp()? sc.getLanguageInfoNode("whois.yes") : sc.getLanguageInfoNode("whois.no")));

			info.add(sc.getLanguageInfoNode("whois.first-online").replace("<time>", DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(p.getFirstPlayed())));
			info.add(sc.getLanguageInfoNode("whois.last-online").replace("<time>", DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(p.getLastPlayed())));
			if (p.isOnline())
				info.add(sc.getLanguageInfoNode("whois.ip").replace("<value>", p.getPlayer().getAddress().toString().substring(1)));

			chat.sendFormattedMessage(sender, info);
		} else
			chat.sendFormattedMessage(sender, LanguageHelper.INFO_WRONG_ARGUMENTS);
	}
}
