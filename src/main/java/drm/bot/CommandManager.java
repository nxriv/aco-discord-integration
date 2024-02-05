package drm.bot;

import drm.bot.commands.*;
import drm.bot.config.Config;
import drm.bot.utils.MongoDBHandler;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

import java.util.ArrayList;
import java.util.List;

public class CommandManager {
    private final List<Command> commands = new ArrayList<>();

    public CommandManager(Config config) {
        // Add this field
        MongoDBHandler mongoDBHandlerE = new MongoDBHandler("mongodb://localhost:27017", "aco-emails", "aco", "aco-statistics");
        MongoDBHandler mongoDBHandlerC = new MongoDBHandler("mongodb://localhost:27017", "channels", "aco", "aco-statistics");
        // Store the config

        commands.add(new PingCommand());
        commands.add(new AddCommand(mongoDBHandlerE));
        commands.add(new RemoveCommand(mongoDBHandlerE));
        commands.add(new ChannelListenerCommand(mongoDBHandlerC));
        commands.add(new SetMirrorChannelCommand(config, mongoDBHandlerC, mongoDBHandlerE));
        // commands.add(new ListCommand(mongoDBHandlerE));

        //Preferences
        commands.add(new HideEmailCommand(mongoDBHandlerE));
        commands.add(new NotifyCommand(mongoDBHandlerE));

        //Stats
        commands.add(new StatsCommand(mongoDBHandlerE));
        commands.add(new ModifyStatsCommand(mongoDBHandlerE));

        //Help
        commands.add(new HelpCommand(commands));
    }

    public List<CommandData> getCommandData() {
        List<CommandData> commandData = new ArrayList<>();
        for (Command command : commands) {
            CommandData data = Commands.slash(command.getName(), command.getDescription())
                    .addOptions(command.getOptions()); // Add command-specific options
            commandData.add(data);
        }
        return commandData;
    }

    public void execute(SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        for (Command command : commands) {
            if (command.getName().equals(commandName)) {
                command.execute(event);
                break;
            }
        }
    }
}