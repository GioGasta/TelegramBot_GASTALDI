package org.projectATB;

import org.projectATB.bot.AnimeBot;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.projectATB.config.BotConfig;

public class Main {
    public static void main(String[] args) throws Exception {

        String token = BotConfig.getBotToken();
        AnimeBot bot = new AnimeBot(token);

        try (TelegramBotsLongPollingApplication app =
                     new TelegramBotsLongPollingApplication()) {

            app.registerBot(token, bot);
            System.out.println("Bot started");
            Thread.currentThread().join();
        }
    }
}