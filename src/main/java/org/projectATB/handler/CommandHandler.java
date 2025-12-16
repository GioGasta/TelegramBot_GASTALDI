package org.projectATB.handler;

import org.projectATB.telegram.MessageFactory;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class CommandHandler {

    private final TelegramClient client;

    public CommandHandler(TelegramClient client) {
        this.client = client;
    }

    public void handle(Update update) throws TelegramApiException
    {
        String text = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        if (text.equals("/list")) {
            client.execute(MessageFactory.simple(chatId, "Your list is empty."));
        } else {
            client.execute(MessageFactory.simple(chatId, "Unknown command."));
        }
    }
}