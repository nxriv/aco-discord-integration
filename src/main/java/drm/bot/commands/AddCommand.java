package drm.bot.commands;

import drm.bot.Command;
import drm.bot.utils.MongoDBHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.List;

public class AddCommand implements Command {
    private final MongoDBHandler mongoDBHandler;

    public AddCommand(MongoDBHandler mongoDBHandler) {
        this.mongoDBHandler = mongoDBHandler;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping emailOption = event.getOption("email");
        if (emailOption != null) {
            String email = emailOption.getAsString();
            String userId = event.getUser().getId();

            if (mongoDBHandler.isEmailExists(userId, email)) {
                event.reply("Email already exists.").setEphemeral(true).queue();
            } else {
                mongoDBHandler.addEmail(userId, email);

                // Update user statistics
                mongoDBHandler.incrementAccountCount(userId);

                event.reply("Email added successfully.").setEphemeral(true).queue();
            }
        } else {
            event.reply("Email is required.").setEphemeral(true).queue();
        }
    }

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String getDescription() {
        return "Add your email address to the database.";
    }

    @Override
    public List<OptionData> getOptions() {
        return Collections.singletonList(
                new OptionData(OptionType.STRING, "email", "The email address to add", true)
        );
    }

}
