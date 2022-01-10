package gq.zielinus.discordwhitelist;

import gq.zielinus.discordwhitelist.listeners.DiscordEventListener;
import gq.zielinus.discordwhitelist.listeners.PlayerConnectionListener;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public final class DiscordWhitelist extends JavaPlugin {

    @Getter
    private JDA discordBot;
    private ConfigurationFile ignFile;

    @Getter @Setter
    private Guild discordServer;

    private List<String> igns = new ArrayList<>();

    @SneakyThrows
    @Override
    public void onEnable() {
        super.onEnable();
        getLogger().info(ChatColor.LIGHT_PURPLE + "DiscordWhitelist enabled!");

        saveDefaultConfig();

        String botToken = getConfig().getString("BOT_TOKEN");
        if (botToken == null) {
            getServer().getPluginManager().disablePlugin(this);
            getLogger().severe(ChatColor.RED + "Please provide a BOT_TOKEN in the config.yml file.");
            return;
        }

        this.discordBot = JDABuilder.createDefault(botToken).build();
        this.discordBot.addEventListener(new DiscordEventListener(this));

        this.ignFile = new ConfigurationFile(this, "igns");
        igns = this.ignFile.getConfiguration().getStringList("igns");

        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
    }

    @Override
    public void onDisable() {
        getLogger().severe(ChatColor.RED + "DiscordWhitelist disabled!");
        discordBot.shutdown();
    }

    public String getRegisterChannel() {
        return getConfigOptionOrDefault("register-channel", "test");
    }

    private String getConfigOptionOrDefault(String key, String defaultValue) {
        String name = getConfig().getString(key);
        if (name == null) {
            return defaultValue;
        } else {
            return name;
        }
    }

    public void registerIGN(String ign) {
        if (igns.contains(ign)) return;

        igns.add(ign);
        ignFile.getConfiguration().set("igns", igns);
        ignFile.save();
    }

    public boolean isWhitelisted(String name) {
        return igns.contains(name);
    }

    public void clearIGNs() {
        igns.clear();
        ignFile.getConfiguration().set("igns", new ArrayList<String>());
        ignFile.save();
    }

    public Role getOrCreateWhitelistedRle() {
        return getOrCreateRole(getWhitelistedRoleName(), "#ff32a7");
    }

    private Role getOrCreateRole(String name, String hexColor) {
        if (discordServer == null) return null;
        List<Role> matches = discordServer.getRolesByName(name, true);

        return matches.size() <= 0 ? discordServer.createRole().setName(name).setColor(Color.decode(hexColor)).complete() : matches.get(0);
    }

    public String getDiscordServerName() {
        return getConfigOptionOrDefault("discord-server-name", "");
    }

    public String getWhitelistedRoleName() {
        return getConfigOptionOrDefault("role-name", "whitelisted");
    }

    public boolean isRegistered(String ign) {
        return igns.contains(ign);
    }
}
