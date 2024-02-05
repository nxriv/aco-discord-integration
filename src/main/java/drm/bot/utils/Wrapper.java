package drm.bot.utils;

import net.dv8tion.jda.api.JDA;

public class Wrapper {
    private static JDA jdaInstance;

    public static void setJDA(JDA jda) {
        jdaInstance = jda;
    }

    public static JDA getJDA() {
        return jdaInstance;
    }
}
