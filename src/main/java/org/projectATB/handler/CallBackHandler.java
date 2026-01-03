package org.projectATB.handler;

import org.projectATB.telegram.MessageFactory;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.projectATB.session.SessionManager;
import org.projectATB.session.UserSession;
import org.projectATB.session.UserState;
import org.projectATB.telegram.CallbackDataParser;
import org.projectATB.telegram.MessageFactory;

public class CallBackHandler {

    private final TelegramClient client;

    public CallBackHandler(TelegramClient client) {
        this.client = client;
    }

    public void handle(Update update) throws TelegramApiException
    {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String data = update.getCallbackQuery().getData();

        UserSession session = SessionManager.get(chatId);

        if (data.equals(CallbackDataParser.LIST_ADD)) {
            session.setState(UserState.LIST_ADD);

            client.execute(
                    MessageFactory.simple(
                            chatId,
                            "Type the anime name(s) you want to add.\n /done when finished."
                    )
            );
        }
    }
}