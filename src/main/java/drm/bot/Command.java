package drm.bot;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.List;

public interface Command {
    void execute(SlashCommandInteractionEvent event);
    String getName();
    String getDescription();
    List<OptionData> getOptions();

}

