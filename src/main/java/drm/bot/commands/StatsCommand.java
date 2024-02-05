package drm.bot.commands;

import drm.bot.Command;
import drm.bot.utils.MongoDBHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.time.Instant;
import java.time.temporal.Temporal;
import java.util.Collections;
import java.util.List;

import static net.dv8tion.jda.api.interactions.commands.OptionType.USER;

public class StatsCommand implements Command {
    private final MongoDBHandler mongoDBHandler;

    public StatsCommand(MongoDBHandler mongoDBHandler) {
        this.mongoDBHandler = mongoDBHandler;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String userId = event.getOption("user") != null ? event.getOption("user").getAsUser().getId() : event.getUser().getId();
        MongoDBHandler.UserStatistics stats = mongoDBHandler.getUserStatistics(userId);

        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("User Statistics")
                .setAuthor("Megoda | User Statistics", null, "https://media.discordapp.net/attachments/1024889947146162266/1182444916794740828/C49343B6-AEF5-4FA3-A03D-773BFA240859.jpg")
                .setColor(new Color(198, 193, 199))
                .setFooter("Developed by drm.", "https://media.discordapp.net/attachments/1024889947146162266/1182444916794740828/C49343B6-AEF5-4FA3-A03D-773BFA240859.jpg")
                .setTimestamp(Instant.now())
                .addField("Number of Checkouts", String.valueOf(stats.getNumCheckouts()), true)
                .addField("Number of Accounts", String.valueOf(stats.getNumAccounts()), true)
                .addField("Total Value of Checkouts", "$" + String.format("%.2f", stats.getTotalValue()), true);

        // Optional: Add additional information or formatting
        // embedBuilder.setFooter("Stats provided by [YourBotName]", null);
        // embedBuilder.setThumbnail("URL_to_thumbnail_image");

        event.replyEmbeds(embedBuilder.build()).setEphemeral(true).queue();
    }


    @Override
    public String getName() {
        return "stats";
    }

    @Override
    public String getDescription() {
        return "Displays user statistics.";
    }

    @Override
    public List<OptionData> getOptions() {
        return Collections.singletonList(
                new OptionData(USER, "user", "The user to display statistics for", false)
        );
    }
}
