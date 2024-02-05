package drm.bot.config;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;

public class Config {
    private String token;
    private String mirrorChannelId;

    public void save() {
        Gson gson = new Gson();
        try (Writer writer = new FileWriter(new File(System.getProperty("user.home"), "configdrmbot.json"))) {
            gson.toJson(this, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Getters and setters
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getMirrorChannelId() {
        return mirrorChannelId;
    }

    public void setMirrorChannelId(String mirrorChannelId) {
        this.mirrorChannelId = mirrorChannelId;
    }

    // Other getters and setters for additional fields...
}
