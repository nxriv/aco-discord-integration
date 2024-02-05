package drm.bot.commands;

import drm.bot.Command;
import drm.bot.utils.MongoDBHandler;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.util.Collections;
import java.util.List;

public class NotifyCommand implements Command {
    private final MongoDBHandler mongoDBHandler;

    public NotifyCommand(MongoDBHandler mongoDBHandler) {
        this.mongoDBHandler = mongoDBHandler;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String action = event.getOption("action").getAsString();

        switch (action.toLowerCase()) {
            case "on":
                mongoDBHandler.setNotifyPreference(event.getUser().getId(), true);
                event.reply("Notifying enabled.").setEphemeral(true).queue();
                break;
            case "off":
                mongoDBHandler.setNotifyPreference(event.getUser().getId(), false);
                event.reply("Notifying disabled.").setEphemeral(true).queue();
                break;
            default:
                event.reply("Invalid action. Use 'on' or 'off'.").setEphemeral(true).queue();
                break;
        }
    }

    @Override
    public String getName() {
        return "notify";
    }

    @Override
    public String getDescription() {
        return "Enable or disable notifying you by message upon checkouts.";
    }

    @Override
    public List<OptionData> getOptions() {
        return Collections.singletonList(
                new OptionData(OptionType.STRING, "action", "Choose 'on' to hide or 'off' for notifying.", true)
                        .addChoice("on", "on")
                        .addChoice("off", "off")
        );
    }
}