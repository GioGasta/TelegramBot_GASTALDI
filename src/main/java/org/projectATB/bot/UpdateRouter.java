package org.projectATB.bot;

import org.projectATB.handler.CallBackHandler;
import org.projectATB.handler.CommandHandler;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class UpdateRouter {

    private final CommandHandler commandHandler;
    private final CallBackHandler callBackHandler;

    public UpdateRouter(TelegramClient client) {
        this.commandHandler = new CommandHandler(client);
        this.callBackHandler = new CallBackHandler(client);
    }

    public void route(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            commandHandler.handle(update);
        } else if (update.hasCallbackQuery()) {
            callBackHandler.handle(update);
        }
    }
}
