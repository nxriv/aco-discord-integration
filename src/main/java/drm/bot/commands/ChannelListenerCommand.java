package drm.bot.commands;

import drm.bot.Command;
import drm.bot.utils.AdminChecker;
import drm.bot.utils.MongoDBHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class ChannelListenerCommand implements Command {
    private final Map<String, MessageChannel> channelListeners;
    private final MongoDBHandler mongoDBHandler;

    public ChannelListenerCommand(MongoDBHandler mongoDBHandler) {
        this.mongoDBHandler = mongoDBHandler;
        this.channelListeners = new ConcurrentHashMap<>();
        //initializeListeners();
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!AdminChecker.isAdmin(event.getMember())) {
            event.reply("You must be an admin to use this command.").setEphemeral(true).queue();
            return;
        }

        OptionMapping actionOption = event.getOption("action");
        if (actionOption == null) {
            event.reply("Action is required.").setEphemeral(true).queue();
            return;
        }

        String action = actionOption.getAsString();
        if ("list".equals(action)) {
            listListeners(event);
        } else {
            OptionMapping channelOption = event.getOption("channel");
            if (channelOption == null) {
                event.reply("Channel ID is required for adding or removing.").setEphemeral(true).queue();
                return;
            }
            String channelId = channelOption.getAsString();

            if ("add".equals(action)) {
                addListener(channelId, event);
            } else if ("remove".equals(action)) {
                removeListener(channelId, event);
            } else {
                event.reply("Invalid action. Use 'add', 'remove', or 'list'.").setEphemeral(true).queue();
            }
        }
    }

    private void addListener(String channelId, SlashCommandInteractionEvent event) {
        MessageChannel channel = event.getJDA().getTextChannelById(channelId);
        if (channel != null && !channelListeners.containsKey(channelId)) {
            channelListeners.put(channelId, channel);
            mongoDBHandler.addChannelListener(channelId);
            event.reply("Listener added to channel: " + channelId).setEphemeral(true).queue();
        } else {
            event.reply("Channel not found or listener already added.").setEphemeral(true).queue();
        }
    }

    private void removeListener(String channelId, SlashCommandInteractionEvent event) {
        if (channelListeners.containsKey(channelId)) {
            channelListeners.remove(channelId);
            mongoDBHandler.removeChannelListener(channelId);
            event.reply("Listener removed from channel: " + channelId).setEphemeral(true).queue();
        } else {
            event.reply("Listener not found on channel: " + channelId).setEphemeral(true).queue();
        }
    }

    private void listListeners(SlashCommandInteractionEvent event) {
        List<String> channels = mongoDBHandler.getChannelListeners();
        if (channels.isEmpty()) {
            event.reply("No channels with listeners.").setEphemeral(true).queue();
        } else {
            String reply = "Channels with listeners:\n" +
                    channels.stream()
                            .map(channelId -> {
                                MessageChannel channel = event.getJDA().getTextChannelById(channelId);
                                return (channel != null) ? channel.getName() + " (ID: " + channelId + ")" : "Unknown Channel (ID: " + channelId + ")";
                            })
                            .collect(Collectors.joining("\n"));
            event.reply(reply).setEphemeral(true).queue();
        }
    }

    @Override
    public String getName() {
        return "channel-listener";
    }

    @Override
    public String getDescription() {
        return "Add or remove a listener from a channel.";
    }

    @Override
    public List<OptionData> getOptions() {
        return Arrays.asList(
                new OptionData(STRING, "action", "The action to perform (add or remove)", true)
                        .addChoice("add", "add")
                        .addChoice("remove", "remove")
                        .addChoice("list", "list"),
                new OptionData(STRING, "channel", "The ID of the channel", false)
        );
    }
}
