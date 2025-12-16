package org.projectATB.bot;

import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class AnimeBot implements LongPollingSingleThreadUpdateConsumer {

    private final UpdateRouter router;

    public AnimeBot(String botToken) {
        TelegramClient client = new OkHttpTelegramClient(botToken);
        this.router = new UpdateRouter(client);
    }

    @Override
    public void consume(Update update) {
        try
        {
            router.route(update);
        } catch (TelegramApiException e)
        {
            throw new RuntimeException(e);
        }
    }
}
