package drm.bot.commands;

import com.mongodb.client.MongoCursor;
import drm.bot.Command;
import drm.bot.utils.AdminChecker;
import drm.bot.utils.MongoDBHandler;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.dv8tion.jda.api.interactions.commands.OptionType.BOOLEAN;

public class ListCommand implements Command {
    private final MongoDBHandler mongoDBHandler;

    public ListCommand(MongoDBHandler mongoDBHandler) {
        this.mongoDBHandler = mongoDBHandler;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!AdminChecker.isAdmin(event.getMember())) {
            event.reply("You must be an admin to use this command.").setEphemeral(true).queue();
            return;
        }


    }

    private ActionRow getActionRow(int currentPage, boolean hasNextPage) {
        Button previousButton = Button.primary("previous_page:" + currentPage, "Previous Page").withEmoji(Emoji.fromUnicode("◀️"));
        Button nextButton = Button.primary("next_page:" + currentPage, "Next Page").withEmoji(Emoji.fromUnicode("▶️"));

        if (currentPage <= 1) {
            previousButton = previousButton.asDisabled();
        }

        if (!hasNextPage) {
            nextButton = nextButton.asDisabled();
        }

        return ActionRow.of(previousButton, nextButton);
    }

    private String formatPageContent(List<Document> entries) {
        // Format the entries into a String for the message content
        StringBuilder contentBuilder = new StringBuilder();
        for (int i = 0; i < entries.size(); i++) {
            Document entry = entries.get(i);
            contentBuilder.append(i + 1).append(". ").append(entry.getString("email")).append("\n");
        }
        return contentBuilder.toString();
    }


    @Override
    public String getName() {
        return "list";
    }

    @Override
    public String getDescription() {
        return "List all email addresses you have added. Admins can view all emails in the database.";
    }

    @Override
    public List<OptionData> getOptions() {
        return Collections.singletonList(
                new OptionData(BOOLEAN, "all", "View all emails in the database (Admin only)", false)
        );
    }
}
