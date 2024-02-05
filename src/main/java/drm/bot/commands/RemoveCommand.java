package drm.bot.commands;

import drm.bot.Command;
import drm.bot.utils.MongoDBHandler;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.List;

import static drm.bot.utils.AdminChecker.isAdmin;

public class RemoveCommand implements Command {
    private final MongoDBHandler mongoDBHandler;

    public RemoveCommand(MongoDBHandler mongoDBHandler) {
        this.mongoDBHandler = mongoDBHandler;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        OptionMapping emailOption = event.getOption("email");
        if (emailOption != null) {
            String email = emailOption.getAsString();
            String userId = event.getUser().getId();

            if (isAdmin(event.getMember())) {
                boolean removed = mongoDBHandler.removeEmailByEmail(email);
                if (removed) {
                    event.getJDA().openPrivateChannelById(userId)
                            .flatMap(channel -> channel.sendMessage("Your email `" + email + "` has been removed by an Administrator."))
                            .queue();
                    // Update user statistics
                    mongoDBHandler.decrementAccountCount(userId);

                    event.reply("Email removed successfully by admin.").setEphemeral(true).queue();
                } else {
                    event.reply("Email not found.").setEphemeral(true).queue();
                }
            } else {
                if (mongoDBHandler.isEmailExists(userId, email)) {
                    mongoDBHandler.removeEmail(userId, email);

                    // Update user statistics
                    mongoDBHandler.decrementAccountCount(userId);

                    event.reply("Email removed successfully.").setEphemeral(true).queue();
                } else {
                    event.reply("Email not found.").setEphemeral(true).queue();
                }
            }
        } else {
            event.reply("Email is required.").setEphemeral(true).queue();
        }
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Remove your email address from the database.";
    }

    @Override
    public List<OptionData> getOptions() {
        return Collections.singletonList(
                new OptionData(OptionType.STRING, "email", "The email address to remove", true)
        );
    }

}
