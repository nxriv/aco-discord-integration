package drm.bot.commands;

import drm.bot.Command;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.Collections;
import java.util.List;

public class HelpCommand implements Command {
    private final List<Command> commands;

    public HelpCommand(List<Command> commands) {
        this.commands = commands;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        StringBuilder helpMessage = new StringBuilder("Available Commands:\n");

        for (Command command : commands) {
            helpMessage.append("* `/").append(command.getName()).append("` - ").append(command.getDescription()).append("\n");

            for (OptionData option : command.getOptions()) {
                helpMessage.append("\t").append(option.getName()).append(": ").append(option.getDescription()).append("\n");
            }
        }

        event.reply(helpMessage.toString()).setEphemeral(true).queue();
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public String getDescription() {
        return "Displays help information for all commands.";
    }

    @Override
    public List<OptionData> getOptions() {
        return Collections.emptyList();
    }
}
