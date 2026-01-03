package org.projectATB.telegram;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.List;

public class KeyboardFactory {

    public static InlineKeyboardMarkup listActions() {

        InlineKeyboardButton add = InlineKeyboardButton.builder()
                .text("➕ Add")
                .callbackData(CallbackDataParser.LIST_ADD)
                .build();

        InlineKeyboardButton watched = InlineKeyboardButton.builder()
                .text("✅ Watched")
                .callbackData(CallbackDataParser.LIST_WATCHED)
                .build();

        InlineKeyboardButton remove = InlineKeyboardButton.builder()
                .text("❌ Remove")
                .callbackData(CallbackDataParser.LIST_REMOVE)
                .build();

        InlineKeyboardRow row = new InlineKeyboardRow();
        row.add(add);
        row.add(watched);
        row.add(remove);

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row))
                .build();
    }
}