package gq.zielinus.discordwhitelist.listeners;

import gq.zielinus.discordwhitelist.DiscordWhitelist;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

@RequiredArgsConstructor
public class PlayerConnectionListener implements Listener {

    private final DiscordWhitelist plugin;

    @EventHandler
    private void onLogin(AsyncPlayerPreLoginEvent event) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(event.getUniqueId());
        if (offlinePlayer.isOp()) return;

        if (!plugin.isWhitelisted(event.getName())) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, ChatColor.RED + "You aren't on the whitelist. " + ChatColor.GREEN + "Add yourself by joining discord server and entering your ign on " + ChatColor.GRAY + "#" + plugin.getRegisterChannel() + ".");
        }
    }
}
