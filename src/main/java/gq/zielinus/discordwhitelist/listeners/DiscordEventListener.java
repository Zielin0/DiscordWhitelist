package gq.zielinus.discordwhitelist.listeners;

import gq.zielinus.discordwhitelist.DiscordWhitelist;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@RequiredArgsConstructor
public class DiscordEventListener extends ListenerAdapter {

    private final DiscordWhitelist plugin;
    private final List<Message> repliedTo = new ArrayList<>();

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        super.onReady(event);

        List<Guild> guilds = plugin.getDiscordBot().getGuildsByName(plugin.getDiscordServerName(), true);

        if (guilds.size() == 0) {
            Bukkit.getLogger().info("The bot hasn't been added to your server. Please visit discord developer portal and create an invite then add bot to your server. The bot will only work on the server you've setup in the config.yml file.");
        } else {
            plugin.setDiscordServer(guilds.get(0));
        }
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        if (!event.getGuild().getName().equalsIgnoreCase(plugin.getDiscordServerName())) {
            event.getGuild().leave().complete();
        } else {
            plugin.setDiscordServer(event.getGuild());
        }
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        if (event.getGuild().getName().equalsIgnoreCase(plugin.getDiscordServerName())) {
            plugin.setDiscordServer(null);
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        MessageChannel channel = event.getChannel();

        if (channel.getName().equalsIgnoreCase(plugin.getRegisterChannel()) && !event.getAuthor().isBot() && !repliedTo.contains(event.getMessage())) {
            String messageToSend;
            String reactionToSend;

            String message = event.getMessage().getContentRaw();
            boolean validIGN = message.length() <= 16 && message.split(" ").length == 1;

            if (!validIGN) {
                messageToSend = "Invalid minecraft IGN. Try again later.";
                reactionToSend = "U+2757";
            } else {
                if (plugin.isRegistered(message)) {
                    messageToSend = "You've already registered!";
                    reactionToSend = "U+274C";
                } else {
                    messageToSend = "You've registered and added to whitelist! :sunglasses:";
                    reactionToSend = "U+1F60E";
                    Bukkit.getLogger().info(message + " has registered to whitelist.");
                    plugin.registerIGN(message);
                }
            }

            repliedTo.add(event.getMessage());

            channel.sendMessage(Objects.requireNonNull(event.getMember()).getAsMention() + ", " + messageToSend).queue();

            channel.addReactionById(event.getMessageId(), reactionToSend).queue();

            Role whitelistedRole = plugin.getOrCreateWhitelistedRle();
            event.getGuild().addRoleToMember(event.getMember(), whitelistedRole).queue();
        }
    }
}
