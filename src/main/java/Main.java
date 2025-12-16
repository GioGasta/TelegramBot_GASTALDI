import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Main {
    public static void main(String[] args) {
        String botToken = "8457446766:AAEVQTXyLaVwldje7oUz5Pmv460gtyuB5zI";

        // Register and start bot
        try (TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication()) {
            botsApplication.registerBot(botToken, new AnimeBot(botToken));

            System.out.println("Bot started successfully!");

            // Keep running
            Thread.currentThread().join();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
