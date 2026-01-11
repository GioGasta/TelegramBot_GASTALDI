package org.projectATB.telegram;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import org.projectATB.model.AnimeSearchResult;

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

    public static InlineKeyboardMarkup animeSearchResults(
            List<AnimeSearchResult> results, 
            Set<Integer> indexes
    ) {
        if (results == null || results.isEmpty()) {
            return InlineKeyboardMarkup.builder().keyboard(List.of()).build();
        }

        List<InlineKeyboardRow> rows = new ArrayList<>();

        for (int i = 0; i < results.size(); i++) {
            AnimeSearchResult result = results.get(i);
            boolean isSelected = indexes.contains(i);
            
            String display = String.format("%s %s (%s, %s) ⭐ %.2f",
                    isSelected ? "✅" : "📺",
                    result.getDisplayTitle(),
                    result.getYear(),
                    result.getType(),
                    result.getScore()
            );

            InlineKeyboardButton button = InlineKeyboardButton.builder()
                    .text(display)
                    .callbackData("ANIME_SEARCH:SELECT:" + i)
                    .build();

            InlineKeyboardRow row = new InlineKeyboardRow();
            row.add(button);
            rows.add(row);
        }

        // Add "None of these" option
        InlineKeyboardButton retryButton = InlineKeyboardButton.builder()
                .text("🔄 None of these - try different search")
                .callbackData("ANIME_SEARCH:RETRY")
                .build();

        InlineKeyboardRow retryRow = new InlineKeyboardRow();
        retryRow.add(retryButton);
        rows.add(retryRow);

        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }
}