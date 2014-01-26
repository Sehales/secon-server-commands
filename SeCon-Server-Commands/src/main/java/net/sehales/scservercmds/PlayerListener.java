
package net.sehales.scservercmds;

import java.util.Date;

import net.sehales.secon.utils.mc.ChatUtils;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;

public class PlayerListener implements Listener {
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLoginEvent(PlayerLoginEvent e) {
        if (!e.getResult().equals(PlayerLoginEvent.Result.ALLOWED)) {
            return;
        }
        BanInfo info = BanHandler.getBanInfo(e.getPlayer().getName());
        if (info != null) {
            if (info.isTempban()) {
                if (new Date().after(info.getEndDate())) {
                    BanHandler.unban(e.getPlayer().getName());
                    return;
                }
            }
            e.disallow(Result.KICK_BANNED, ChatUtils.formatMessage(info.getReason()));
        }
    }
}
