package org.projectATB.handler;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class CallBackHandler {

    private final TelegramClient client;

    public CallBackHandler(TelegramClient client) {
        this.client = client;
    }

    public void handle(Update update) {
        // placeholder
    }
}