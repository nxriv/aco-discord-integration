package drm.bot.commands;

import drm.bot.Command;
import drm.bot.utils.AdminChecker;
import drm.bot.utils.MongoDBHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.List;

public class HideEmailCommand implements Command {
    private final MongoDBHandler mongoDBHandler;

    public HideEmailCommand(MongoDBHandler mongoDBHandler) {
        this.mongoDBHandler = mongoDBHandler;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String userId = event.getUser().getId();
        String action = event.getOption("action").getAsString();

        switch (action.toLowerCase()) {
            case "on":
                mongoDBHandler.setHideEmail(userId, true);
                event.reply("Email hiding enabled.").setEphemeral(true).queue();
                break;
            case "off":
                mongoDBHandler.setHideEmail(userId, false);
                event.reply("Email hiding disabled.").setEphemeral(true).queue();
                break;
            default:
                event.reply("Invalid action. Use 'on' or 'off'.").setEphemeral(true).queue();
                break;
        }
    }

    @Override
    public String getName() {
        return "hideemail";
    }

    @Override
    public String getDescription() {
        return "Enable or disable email hiding in checkout messages.";
    }

    @Override
    public List<OptionData> getOptions() {
        return Collections.singletonList(
                new OptionData(OptionType.STRING, "action", "Choose 'on' to hide or 'off' to show your email", true)
                        .addChoice("on", "on")
                        .addChoice("off", "off")
        );
    }
}
