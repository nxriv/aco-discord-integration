package drm.bot.commands;

import com.google.gson.Gson;
import drm.bot.Command;
import drm.bot.config.Config;
import drm.bot.utils.AdminChecker;
import drm.bot.utils.MongoDBHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.awt.*;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class SetMirrorChannelCommand extends ListenerAdapter implements Command {
    private final Config config;
    private final MongoDBHandler mongoDBHandler;

    private final MongoDBHandler mongoDBHandlerE;

    public SetMirrorChannelCommand(Config config, MongoDBHandler mongoDBHandler, MongoDBHandler mongoDBHandlerE) {
        this.config = config;
        this.mongoDBHandler = mongoDBHandler;
        this.mongoDBHandlerE = mongoDBHandlerE;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!AdminChecker.isAdmin(event.getMember())) {
            event.reply("You must be an admin to use this command.").setEphemeral(true).queue();
            return;
        }

        String channelId = Objects.requireNonNull(event.getOption("channel")).getAsString();
        config.setMirrorChannelId(channelId);
        config.save();
        event.reply("Mirroring started in channel: " + channelId).setEphemeral(true).queue();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!mongoDBHandler.getChannelListeners().contains(event.getChannel().getId())) {
            System.out.println("Message not in channel from database. Skipping.");
            return;
        }

        if (!event.isWebhookMessage()) {
            System.out.println("Message is not a webhook.");
            return;
        }

        String mirrorChannelId = config.getMirrorChannelId();
        if (mirrorChannelId == null) {
            System.out.println("Mirror channel ID is null.");
            return;
        }

        List<MessageEmbed> embeds = event.getMessage().getEmbeds();
        if (embeds.isEmpty()) {
            System.out.println("Embed is null.");
            return;
        }

        for (MessageEmbed originalEmbed : embeds) {
            if (originalEmbed.getAuthor() != null && originalEmbed.getAuthor().getName().contains("Auto-Cancelled")) {
                System.out.println("Auto-Cancelled post detected. Skipping.");
                continue; // Skip posting this embed
            }

            EmbedBuilder embedBuilder = reformatEmbed(originalEmbed);
            String userId = mongoDBHandlerE.getUserIdByEmail(extractEmailFromEmbed(originalEmbed));
            System.out.println("Attempting.");

            event.getJDA().getTextChannelById(mirrorChannelId)
                    .sendMessageEmbeds(embedBuilder.build())
                    .queue();

            boolean notify = userId != null && mongoDBHandlerE.getNotifyPreference(userId);
            System.out.println(notify);
            if (notify) {
                sendPrivateMessageWithEmbed(event.getJDA(), userId, embedBuilder.build());
            }
        }
    }

    private double extractOriginalPrice(MessageEmbed originalEmbed) {
        String botName = determineBotName(originalEmbed.getFooter().getText());
        return switch (botName.toLowerCase()) {
            case "enven" -> parsePrice(extractEnvenOriginalPrice(originalEmbed));
            case "frozen", "refract" -> parsePrice(extractFieldValue(originalEmbed, "Original Price"));
            case "origin" -> parsePrice(extractFieldValue(originalEmbed, "Item Price"));
            default -> {
                System.out.println("Unknown bot type or unable to extract original price.");
                yield 0.0; // Return a default value of 0.0 for unknown bot types
            }
        };
    }

    private double parsePrice(String priceString) {
        if (priceString == null || priceString.isEmpty()) {
            return 0.0; // Return a default value if the string is null or empty
        }
        try {
            // Remove non-numeric characters (except the decimal point) before parsing
            priceString = priceString.replaceAll("[^\\d.]", "");
            return Double.parseDouble(priceString);
        } catch (NumberFormatException e) {
            System.err.println("Unable to parse price: " + priceString);
            return 0.0; // Return a default value if parsing fails
        }
    }

    private EmbedBuilder reformatEmbed(MessageEmbed originalEmbed) {
        String footerText = originalEmbed.getFooter() != null ? originalEmbed.getFooter().getText() : "";
        String botName = determineBotName(footerText);

        // Extract email address from the embed
        String email = extractEmailFromEmbed(originalEmbed);

        // Get Discord user ID associated with the email address
        String discordUserId = mongoDBHandlerE.getUserIdByEmail(email);

        // Determine the user mention or placeholder text
        String userMention = discordUserId != null ? "<@" + discordUserId + ">" : "User not found";

        // Check if the user has hideEmail enabled
        boolean hideEmail = discordUserId != null && mongoDBHandlerE.getHideEmail(discordUserId);

        // Email field value based on hideEmail status
        String emailFieldValue = hideEmail ? "Email hidden" : email;

        // Build embed based on bot name
        return switch (botName.toLowerCase()) {
            case "enven" -> buildEnvenEmbed(originalEmbed, userMention, emailFieldValue);
            case "frozen" -> buildFrozenEmbed(originalEmbed, userMention, emailFieldValue);
            case "origin" -> buildOriginEmbed(originalEmbed, userMention, emailFieldValue);
            case "refract" -> buildRefractEmbed(originalEmbed, userMention, emailFieldValue);
            default -> buildEnvenEmbed(originalEmbed, userMention, emailFieldValue); // Replace with default template
        };
    }

    private EmbedBuilder buildEnvenEmbed(MessageEmbed originalEmbed, String userMention, String emailFieldValue) {
        String footerText = originalEmbed.getFooter() != null ? originalEmbed.getFooter().getText() : "";
        String botName = determineBotName(footerText);
        String originalPrice = extractEnvenOriginalPrice(originalEmbed);
        String pricePaid = extractEnvenPricePaid(originalEmbed);
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(originalEmbed.getTitle())
                .setAuthor("Megoda | Successful Checkout! \uD83C\uDF89", null, "https://media.discordapp.net/attachments/1024889947146162266/1182444916794740828/C49343B6-AEF5-4FA3-A03D-773BFA240859.jpg")
                .setDescription(originalEmbed.getDescription())
                .setUrl(originalEmbed.getUrl())
                .setTimestamp(Instant.now())
                .setColor(new Color(198, 193, 199))
                .setFooter(botName, originalEmbed.getFooter().getIconUrl())
                .setThumbnail(originalEmbed.getThumbnail() != null ? originalEmbed.getThumbnail().getUrl() : null)
                .addField("Original Price", originalPrice, true)
                .addField("Price Paid", pricePaid, true)
                .addField("User", userMention, true)
                .addField("Email","||" + emailFieldValue + "||", true);
        return embedBuilder;
    }

    private EmbedBuilder buildFrozenEmbed(MessageEmbed originalEmbed, String userMention, String emailFieldValue) {
        String footerText = originalEmbed.getFooter() != null ? originalEmbed.getFooter().getText() : "";
        String botName = determineBotName(footerText);
        String originalPrice = extractFieldValue(originalEmbed, "Original Price");
        String pricePaid = formatPrice(extractFieldValue(originalEmbed, "Price Paid"));
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(originalEmbed.getDescription())
                .setAuthor("Megoda | Successful Checkout! \uD83C\uDF89", null, "https://media.discordapp.net/attachments/1024889947146162266/1182444916794740828/C49343B6-AEF5-4FA3-A03D-773BFA240859.jpg")
                //.setDescription(originalEmbed.getDescription())
                .setUrl(originalEmbed.getUrl())
                .setColor(new Color(198, 193, 199))
                .setTimestamp(Instant.now())
                .setFooter(botName, originalEmbed.getFooter().getIconUrl())
                .setThumbnail(originalEmbed.getImage() != null ? originalEmbed.getImage().getUrl() : null)
                .addField("Original Price", originalPrice, true)
                .addField("Price Paid", pricePaid, true)
                .addField("User", userMention, true)
                .addField("Email","||" + emailFieldValue + "||", true);
        return embedBuilder;
    }

    private EmbedBuilder buildOriginEmbed(MessageEmbed originalEmbed, String userMention, String emailFieldValue) {
        String footerText = originalEmbed.getFooter() != null ? originalEmbed.getFooter().getText() : "";
        String botName = determineBotName(footerText);
        String originalPrice = extractFieldValue(originalEmbed, "Item Price");
        String pricePaid = calculateOriginPricePaid(originalEmbed);
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(originalEmbed.getTitle())
                .setAuthor("Megoda | Successful Checkout! \uD83C\uDF89", null, "https://media.discordapp.net/attachments/1024889947146162266/1182444916794740828/C49343B6-AEF5-4FA3-A03D-773BFA240859.jpg")
                .setDescription(originalEmbed.getDescription())
                .setUrl(originalEmbed.getUrl())
                .setColor(new Color(198, 193, 199))
                .setTimestamp(Instant.now())
                .setFooter(botName, originalEmbed.getFooter().getIconUrl())
                .setThumbnail(originalEmbed.getThumbnail() != null ? originalEmbed.getThumbnail().getUrl() : null)
                .addField("Original Price", "$" + originalPrice, true)
                .addField("Price Paid", pricePaid, true)
                .addField("User", userMention, true)
                .addField("Email","||" + emailFieldValue + "||", true);
        return embedBuilder;
    }

    private EmbedBuilder buildRefractEmbed(MessageEmbed originalEmbed, String userMention, String emailFieldValue) {
        String footerText = originalEmbed.getFooter() != null ? originalEmbed.getFooter().getText() : "";
        String botName = determineBotName(footerText);
        String urlFromFirstField = extractUrlFromFirstField(originalEmbed);
        String originalPrice = extractFieldValue(originalEmbed, "Original Price");
        String pricePaid = extractFieldValue(originalEmbed, "Price");
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle(originalEmbed.getTitle())
                .setAuthor("Megoda | Successful Checkout! \uD83C\uDF89", null, "https://media.discordapp.net/attachments/1024889947146162266/1182444916794740828/C49343B6-AEF5-4FA3-A03D-773BFA240859.jpg")
                .setDescription(originalEmbed.getFields().get(0).getValue())
                .setUrl(urlFromFirstField)
                .setColor(new Color(198, 193, 199))
                .setTimestamp(Instant.now())
                .setFooter(botName, originalEmbed.getFooter().getIconUrl())
                .setThumbnail(originalEmbed.getThumbnail() != null ? originalEmbed.getThumbnail().getUrl() : null)
                .addField("Original Price", originalPrice, true)
                .addField("Price Paid", pricePaid, true)
                .addField("User", userMention, true)
                .addField("Email","||" + emailFieldValue + "||", true);
        return embedBuilder;
    }

    // Helper methods for pricing extraction and calculation
    private String extractEnvenOriginalPrice(MessageEmbed originalEmbed) {
        // Extract the original price from Enven's embed
        String priceField = originalEmbed.getFields().isEmpty() ? "" : originalEmbed.getFields().get(2).getValue();
        String[] prices = priceField.split(" ");
        return prices.length > 0 ? prices[0].replace("~", "").trim() : "N/A";
    }

    private String extractEnvenPricePaid(MessageEmbed originalEmbed) {
        // Extract the price paid from Enven's embed
        String priceField = originalEmbed.getFields().isEmpty() ? "" : originalEmbed.getFields().get(2).getValue();
        String[] prices = priceField.split(" ");
        return prices.length > 1 ? prices[1].trim() : "N/A"; // The price paid is the second part
    }

    private String extractFieldValue(MessageEmbed originalEmbed, String fieldName) {
        return originalEmbed.getFields().stream()
                .filter(field -> field.getName().equalsIgnoreCase(fieldName))
                .findFirst()
                .map(MessageEmbed.Field::getValue)
                .orElse("N/A");
    }

    private String calculateOriginPricePaid(MessageEmbed originalEmbed) {
        String itemPrice = extractFieldValue(originalEmbed, "Item Price");
        String discount = extractFieldValue(originalEmbed, "Discount");
        

        try {
            // Replace all non-digit and non-decimal point characters with nothing
            double finalPrice = getFinalPrice(itemPrice, discount);
            return String.format("$%.2f", finalPrice);
        } catch (NumberFormatException e) {
            // Log the exception message
            System.err.println("NumberFormatException: " + e.getMessage());
            return "N/A"; // Return "N/A" if unable to parse
        }
    }

    private static double getFinalPrice(String itemPrice, String discount) {
        String numericItemPrice = itemPrice.replaceAll("[^\\d.]", "");
        String numericDiscount = discount.replaceAll("[^\\d.]", "");


        // Parse the numeric strings as double values
        double price = Double.parseDouble(numericItemPrice);
        double discountPercentage = Double.parseDouble(numericDiscount);

        // Calculate the final price
        return price * (1 - discountPercentage / 100);
    }
    private String formatPrice(String priceString) {
        try {
            double price = Double.parseDouble(priceString.replaceAll("[^\\d.]", ""));
            return String.format("$%.2f", price);
        } catch (NumberFormatException e) {
            System.err.println("Unable to parse price: " + priceString);
            return priceString; // Return the original string if parsing fails
        }
    }

    private String extractEmailFromEmbed(MessageEmbed originalEmbed) {
        // Regex pattern to match an email address
        String emailRegex = "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}";
        Pattern pattern = Pattern.compile(emailRegex);

        // Concatenate the title, description, and all field values into a single string
        StringBuilder rawContent = new StringBuilder();
        if (originalEmbed.getTitle() != null) {
            rawContent.append(originalEmbed.getTitle()).append(" ");
        }
        if (originalEmbed.getDescription() != null) {
            rawContent.append(originalEmbed.getDescription()).append(" ");
        }
        for (MessageEmbed.Field field : originalEmbed.getFields()) {
            rawContent.append(field.getValue()).append(" ");
        }

        // Replace "|" with space to avoid issues with concatenation in the raw data
        String content = rawContent.toString().replace("|", " ");

        // Search for an email address in the concatenated string
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            // Return the first found email address
            return matcher.group();
        }

        // Return null if no email is found
        return null;
    }

    private String extractUrlFromFirstField(MessageEmbed originalEmbed) {
        if (originalEmbed.getFields().isEmpty()) {
            return null; // No fields in the embed to extract from
        }

        // Assume the URL is in the first field's value
        String fieldValue = originalEmbed.getFields().get(0).getValue();

        // Regex pattern for URL extraction
        String urlRegex = "http[s]?://[^\\s]+";
        Pattern pattern = Pattern.compile(urlRegex);
        Matcher matcher = pattern.matcher(fieldValue);

        if (matcher.find()) {
            // Return the first URL found
            return matcher.group();
        }

        return null; // No URL found
    }

    public void sendPrivateMessageWithEmbed(JDA jda, String userId, MessageEmbed embed) {
        jda.openPrivateChannelById(userId)
                .flatMap(channel -> channel.sendMessageEmbeds(embed))
                .queue(
                        success -> System.out.println("Message sent successfully."),
                        failure -> System.out.println("Failed to send message: " + failure.getMessage())
                );
    }



    private String determineBotName(String footerText) {
        if (footerText.toLowerCase().contains("enven")) {
            return "Enven";
        } else if (footerText.toLowerCase().contains("frozen")) {
            return "Frozen";
        } else if (footerText.toLowerCase().contains("origin")) {
            return "Origin";
        } else if (footerText.toLowerCase().contains("prism")) {
            return "Refract";
        }
        return "Unknown Bot";
    }


    @Override
    public String getName() {
        return "set-mirror-channel";
    }

    @Override
    public String getDescription() {
        return "Start mirroring messages to a specified channel.";
    }

    @Override
    public List<OptionData> getOptions() {
        return Collections.singletonList(
                new OptionData(STRING, "channel", "The ID of the channel to start mirroring to", true)
        );
    }
}
