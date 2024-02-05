package drm.bot;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.util.List;
import java.util.Objects;

public class MessageListener extends ListenerAdapter {

    private final String mirrorChannelId;

    public MessageListener(String mirrorChannelId) {
        this.mirrorChannelId = mirrorChannelId;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        // Ignore bot messages and non-webhook messages
        if (event.getAuthor().isBot() || !event.isWebhookMessage()) {
            return;
        }

        // Filtering Logic
        List<MessageEmbed> embeds = event.getMessage().getEmbeds();
        if (embeds.isEmpty()) {
            // This webhook message does not contain embeds, so we ignore it
            return;
        }

        // For simplicity, we're just going to mirror the first embed
        MessageEmbed originalEmbed = embeds.getFirst();

        // Extract data from the original embed
        String title = originalEmbed.getTitle(); // The title of the embed
        String imageUrl = originalEmbed.getImage() != null ? originalEmbed.getImage().getUrl() : null; // The URL of the embed image
        String footer = originalEmbed.getFooter() != null ? originalEmbed.getFooter().getText() : ""; // The footer text
        assert footer != null;
        String botName = determineBotName(footer); // Determine the bot name from the footer

        // Now create a new embed with the reformatted information
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(title)
                .setColor(Color.BLUE) // Set a color for the embed
                .setFooter(botName); // Set the bot name in the footer

        // If there's an image, add it to the new embed
        if (imageUrl != null) {
            embedBuilder.setImage(imageUrl);
        }
        Objects.requireNonNull(event.getJDA().getTextChannelById(mirrorChannelId)).sendMessageEmbeds(embedBuilder.build()).queue();
    }

    private String determineBotName(String footer) {
        // Your logic to determine the bot name from the footer
        if (footer.contains("enven")) {
            return "Enven";
        } else if (footer.contains("frozen")) {
            return "Frozen";
        } else if (footer.contains("origin")) {
            return "Origin";
        } else if (footer.contains("prism")) {
            return "Refract";
        }
        return "Unknown Bot";
    }
}
