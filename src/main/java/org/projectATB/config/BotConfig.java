package org.projectATB.config;

import java.io.InputStream;
import java.util.Properties;

public class BotConfig {

    public static String getBotToken() {
        // 1️. env var has priority
        String token = System.getenv("BOT_TOKEN");
        if (token != null && !token.isBlank()) {
            return token;
        }

        // 2️. fallback to config.properties
        try (InputStream is = BotConfig.class
                .getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (is == null) {
                throw new IllegalStateException("config.properties not found");
            }

            Properties props = new Properties();
            props.load(is);

            token = props.getProperty("bot.token");

            if (token == null || token.isBlank()) {
                throw new IllegalStateException("bot.token not set");
            }

            return token;

        } catch (Exception e) {
            throw new RuntimeException("Failed to load bot token", e);
        }
    }
}
