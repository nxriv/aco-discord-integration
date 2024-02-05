package drm.bot.commands;

import drm.bot.Command;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import java.util.Collections;
import java.util.List;
import drm.bot.utils.Wrapper; // Import the Wrapper class that holds the JDA instance

public class PingCommand implements Command {
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long ping = Wrapper.getJDA().getGatewayPing(); // Retrieve the gateway ping
        event.reply("Pong! Latency: " + ping + " ms").queue(); // Reply with the latency
    }

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public String getDescription() {
        return "Replies with Pong and shows the bot's latency.";
    }

    @Override
    public List<OptionData> getOptions() {
        return Collections.emptyList(); // No additional options required
    }
}
