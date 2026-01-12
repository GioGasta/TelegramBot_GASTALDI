package org.projectATB.telegram;

import org.projectATB.session.UserSession;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

public class MessageUtils {

    public static Message sendAndTrack(
            TelegramClient client,
            UserSession session,
            BotApiMethod<Message> method
    ) throws TelegramApiException {

        // cancella il precedente messaggio del bot
        if (session.getLastBotMessageId() != null) {
            safeDelete(client,
                    session.getChatId(),
                    session.getLastBotMessageId());
        }

        Message msg = client.execute(method);
        session.setLastBotMessageId(msg.getMessageId());
        return msg;
    }

    public static void safeDelete(
            TelegramClient client,
            long chatId,
            Integer messageId
    ) {
        if (messageId == null) return;

        try {
            client.execute(
                    DeleteMessage.builder()
                            .chatId(chatId)
                            .messageId(messageId)
                            .build()
            );
        } catch (TelegramApiException ignored) {
            // ignora:
            // - message already deleted
            // - message can't be deleted
        }
    }

    public static void removeKeyboard(
            TelegramClient client,
            long chatId,
            int messageId
    ) {
        try {
            client.execute(
                    EditMessageReplyMarkup.builder()
                            .chatId(chatId)
                            .messageId(messageId)
                            .replyMarkup(null)
                            .build()
            );
        } catch (TelegramApiException ignored) {
            // keyboard già rimossa o messaggio non modificabile
        }
    }
}