package drm.bot;

import java.io.*;
import java.nio.file.*;
import com.google.gson.*;
import drm.bot.config.Config;

import java.util.Scanner;

public class ConfigManager {
    private static final String CONFIG_FILE = "config.json";
    private Config config;

    public ConfigManager() {
        loadOrCreateConfig();
    }

    private void loadOrCreateConfig() {
        try {
            if (!Files.exists(Paths.get(CONFIG_FILE))) {
                // Config file does not exist, create a new one
                config = new Config(); // Replace with your Config class
                // Prompt for bot token in the console
                System.out.println("Enter the bot token: ");
                Scanner scanner = new Scanner(System.in);
                String token = scanner.nextLine();
                config.setToken(token);
                // Save the new config to file
                saveConfig();
                System.out.println("Config created. Please restart the bot.");
                System.exit(0); // Exit after creating the config
            } else {
                // Config file exists, load it
                Gson gson = new Gson();
                try (Reader reader = new FileReader(CONFIG_FILE)) {
                    config = gson.fromJson(reader, Config.class);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveConfig() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Writer writer = new FileWriter(CONFIG_FILE)) {
            gson.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Config getConfig() {
        return config;
    }
}
