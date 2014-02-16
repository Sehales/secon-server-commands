
package net.sehales.scservercmds;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sehales.secon.SeCon;
import net.sehales.secon.player.PlayerManager;
import net.sehales.secon.player.SCPlayer;
import net.sehales.secon.utils.chat.ChatUtils;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SCUtils {
    
    private ServerCmdCollection sc;
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
            if (receiver == null) {
                return false;
            }
            
            sendPrivateMessage(sender, receiver, message);
            return true;
            
        }
        return false;
    }
    
    @SuppressWarnings("unchecked")
    public void sendPrivateMessage(CommandSender sender, CommandSender receiver, String message) {
        replyMap.put(receiver.getName(), sender.getName());
        
        ChatUtils.sendFormattedMessage(sender, sc.getLanguageNode("message.sender-msg").replace("<sender>", sender.getName()).replace("<receiver>", receiver.getName()).replace("<message>", message));
        PlayerManager pm = SeCon.getInstance().getPlayerManager();
        SCPlayer scPlayer = pm.getPlayer(sender.getName());
        
        List<String> ignoredByPlayers;
        Object obj = scPlayer.getTransientValue("ignoredByPlayers");
        
        if (obj != null && obj instanceof ArrayList) {
            ignoredByPlayers = (List<String>) obj;
        } else {
            ignoredByPlayers = Collections.emptyList();
        }
        
        if (!ignoredByPlayers.contains(receiver.getName())) {
            ChatUtils.sendFormattedMessage(receiver, sc.getLanguageNode("message.msg").replace("<sender>", sender.getName()).replace("<receiver>", receiver.getName()).replace("<message>", message));
        }
        String spyMessage = sc.getLanguageNode("spy.header").replace("<sender>", sender.getName()).replace("<receiver>", receiver.getName()).replace("<message>", message);
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.equals(receiver) || p.equals(sender)) {
                continue;
            }
            
            SCPlayer scp = pm.getPlayer(p.getName());
            if (scp.hasData("spymode")) {
                ChatUtils.sendFormattedMessage(p, spyMessage);
            }
        }
        
        ChatUtils.sendFormattedMessage(Bukkit.getConsoleSender(), spyMessage);
    }
    
    public void setWeatherClear(Player player, int duration) {
        World w = player.getWorld();
        w.setThundering(false);
        w.setStorm(false);
        w.setWeatherDuration(duration * 60 * 20);
        ChatUtils.sendFormattedMessage(player, sc.getLanguageNode("weather.clear-msg"));
    }
    
    public void setWeatherRain(Player player, int duration) {
        World w = player.getWorld();
        w.setThundering(false);
        w.setStorm(true);
        w.setWeatherDuration(duration * 60 * 20);
        ChatUtils.sendFormattedMessage(player, sc.getLanguageNode("weather.rain-msg").replace("<duration>", Integer.toString(duration)));
    }
    
    public void setWeatherStorm(Player player, int duration) {
        World w = player.getWorld();
        w.setThundering(true);
        w.setStorm(true);
        w.setWeatherDuration(duration * 60 * 20);
        ChatUtils.sendFormattedMessage(player, sc.getLanguageNode("weather.storm-msg").replace("<duration>", Integer.toString(duration)));
    }
    
    public String timeToString(long time) {
        int hours = (int) ((Math.floor(time / 1000.0) + 8) % 24);
        int minutes = (int) Math.floor(time % 1000 / 1000.0 * 60);
        return String.format("%02d:%02d", hours, minutes);
    }
}
