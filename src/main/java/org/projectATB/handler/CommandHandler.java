package org.projectATB.handler;

import org.projectATB.session.SessionManager;
import org.projectATB.session.UserSession;
import org.projectATB.session.UserState;
import org.projectATB.telegram.KeyboardFactory;
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
        UserSession session = SessionManager.get(chatId);

        if (!text.startsWith("/") && session.getState() == UserState.LIST_ADD) {
            session.getPendingAddNames().add(text);

            client.execute(
                    MessageFactory.simple(
                            chatId,
                            "Added \"" + text + "\" to pending list."
                    )
            );
            return;
        }

        if (text.equals("/list")) {
            client.execute(
                    MessageFactory.withKeyboard(
                            chatId,
                            "Here's your list: oh... it's empty.",
                            KeyboardFactory.listActions()
                    )
            );
        } else {
            client.execute(MessageFactory.simple(chatId, "Unknown command."));
        }
    }
}