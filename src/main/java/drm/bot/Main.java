package drm.bot;

import drm.bot.commands.SetMirrorChannelCommand;
import drm.bot.config.Config;
import drm.bot.utils.MongoDBHandler;
import com.google.gson.Gson;
import drm.bot.utils.Wrapper;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    private static final String CONFIG_FILE = "configdrmbot.json";

    public static void main(String[] args) {
        try {
            Config config = loadOrCreateConfig();
            if (config.getToken() == null || config.getToken().isEmpty()) {
                System.out.println("Bot token is not set in the config file. Please enter the bot token: ");
                Scanner scanner = new Scanner(System.in);
                String token = scanner.nextLine();
                config.setToken(token);
                saveConfig(config);
            }
                // Create MongoDB handlers
                MongoDBHandler mongoDBHandlerE = new MongoDBHandler("mongodb://localhost:27017", "aco-emails", "aco", "aco-statistics");
                MongoDBHandler mongoDBHandlerC = new MongoDBHandler("mongodb://localhost:27017", "channels", "aco", "aco-statistics");

                CommandManager commandManager = new CommandManager(config);
                // Add additional listeners as needed
                SetMirrorChannelCommand mirrorCommand = new SetMirrorChannelCommand(config, mongoDBHandlerC, mongoDBHandlerE);

                JDABuilder builder = JDABuilder.createDefault(config.getToken());
                builder.setActivity(Activity.playing("Type /help"))
                        .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                        .addEventListeners(new CommandListener(commandManager))
                        .addEventListeners(mirrorCommand);
                JDA jda = builder.build();
                jda.awaitReady(); // Wait for JDA to be ready

                // Register commands dynamically
                jda.updateCommands().addCommands(commandManager.getCommandData()).queue();

                Wrapper.setJDA(jda); // Set the JDA instance in JDAHolder
                mongoDBHandlerE.setJDA(jda); // Set the JDA instance in MongoDBHandler for emails
                mongoDBHandlerC.setJDA(jda); // Set the JDA instance in MongoDBHandler for channels

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Config loadOrCreateConfig() throws IOException {
        Gson gson = new Gson();
        Config config;
        if (!Files.exists(Paths.get(CONFIG_FILE))) {
            // Config file does not exist, create a new one with empty token
            config = new Config(); // Create a new instance of your Config class
            saveConfig(config);
            System.out.println("Config file created at " + CONFIG_FILE + ". Please restart the bot after setting the token.");
            System.exit(0); // Exit the program
        } else {
            // Config file exists, load it
            try (Reader reader = new FileReader(CONFIG_FILE)) {
                config = gson.fromJson(reader, Config.class);
            }
        }
        return config;
    }

    private static void saveConfig(Config config) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Writer writer = new FileWriter(CONFIG_FILE)) {
            gson.toJson(config, writer);
        }
    }

}
