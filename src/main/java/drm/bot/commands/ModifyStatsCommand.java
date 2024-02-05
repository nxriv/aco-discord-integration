package drm.bot.commands;

import drm.bot.Command;
import drm.bot.utils.AdminChecker;
import drm.bot.utils.MongoDBHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.ArrayList;
import java.util.List;

import static net.dv8tion.jda.api.interactions.commands.OptionType.INTEGER;
import static net.dv8tion.jda.api.interactions.commands.OptionType.USER;

public class ModifyStatsCommand implements Command {
    private final MongoDBHandler mongoDBHandler;

    public ModifyStatsCommand(MongoDBHandler mongoDBHandler) {
        this.mongoDBHandler = mongoDBHandler;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!AdminChecker.isAdmin(event.getMember())) {
            event.reply("You must be an admin to use this command.").setEphemeral(true).queue();
            return;
        }

        String userId = event.getOption("user").getAsUser().getId();
        int numCheckouts = event.getOption("num_checkouts").getAsInt();
        double totalValue = event.getOption("total_value").getAsDouble();

        // Validate input
        if (numCheckouts < 0 || totalValue < 0.0) {
            event.reply("Invalid input: Number of checkouts and total value must be non-negative.").setEphemeral(true).queue();
            return;
        }

        // Update user statistics in the database
        mongoDBHandler.updateUserStatistics(userId, numCheckouts, totalValue);
        event.reply("Statistics updated for user <@" + userId + ">.").setEphemeral(true).queue();
    }

    @Override
    public String getName() {
        return "modifystats";
    }

    @Override
    public String getDescription() {
        return "Modify the statistics of a user (Admin only).";
    }

    @Override
    public List<OptionData> getOptions() {
        List<OptionData> options = new ArrayList<>();
        options.add(new OptionData(USER, "user", "The user to modify statistics for", true));
        options.add(new OptionData(INTEGER, "num_checkouts", "Set the number of checkouts", true));
        options.add(new OptionData(INTEGER, "total_value", "Set the total value of checkouts", true));
        return options;
    }
}
