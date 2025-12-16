package org.projectATB.telegram;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

public class MessageFactory {

    public static SendMessage simple(long chatId, String text) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
    }
}