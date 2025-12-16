import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class AnimeBot implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;

    // constructor: creates a client with bot token
    public AnimeBot(String botToken) {
        telegramClient = new OkHttpTelegramClient(botToken);
    }

    @Override
    public void consume(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String text = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            // test reply
            SendMessage response = SendMessage
                    .builder()
                    .chatId(chatId)
                    .text("you said: " + text)
                    .build();

            try {
                telegramClient.execute(response);  // send the message
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }
}