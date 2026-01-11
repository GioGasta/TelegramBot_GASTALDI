package org.projectATB.telegram;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    public static InlineKeyboardMarkup animeSelection(List<String> animeList, Set<Integer> selected, String prefix)
    {
        if (animeList == null || animeList.isEmpty()) {
            return InlineKeyboardMarkup.builder().keyboard(List.of()).build();
        }

        List<InlineKeyboardRow> rows = new ArrayList<>();

        for(int i = 0; i < animeList.size(); i++) {
            int index = i + 1;
            boolean checked = selected.contains(index);
            String checkmark = checked ? "✅ " : "";

            InlineKeyboardButton btn = InlineKeyboardButton.builder()
                    .text(checkmark + index + ". " + animeList.get(i))
                    .callbackData(prefix + ":" + index)
                    .build();

            InlineKeyboardRow row = new InlineKeyboardRow();
            row.add(btn);
            rows.add(row);
        }

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }
}