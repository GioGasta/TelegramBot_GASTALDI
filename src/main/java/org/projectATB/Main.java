package org.projectATB;

import org.projectATB.bot.AnimeBot;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;

public class Main {
    public static void main(String[] args) {
        String botToken = "8457446766:AAEVQTXyLaVwldje7oUz5Pmv460gtyuB5zI";
        String token = System.getenv(botToken);
        AnimeBot bot = new AnimeBot(token);
        System.out.println("Bot started");
    }
}
