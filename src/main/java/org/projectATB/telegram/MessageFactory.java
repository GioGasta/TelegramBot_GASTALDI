package org.projectATB.telegram;

import org.projectATB.dto.AnimeDetailsDTO;
import org.projectATB.dto.CharacterDTO;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.io.Serializable;

public class MessageFactory {

    public static SendMessage simple(long chatId, String text) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .build();
    }

    public static SendMessage withKeyboard(long chatId, String text, InlineKeyboardMarkup keyboard)
    {
        return SendMessage.builder()
                .chatId(chatId)
                .text(text)
                .replyMarkup(keyboard)
                .build();

    }

    public static EditMessageReplyMarkup editKeyboard(Update update, InlineKeyboardMarkup keyboard)
    {
        return EditMessageReplyMarkup.builder()
                .chatId(update.getCallbackQuery().getMessage().getChatId())
                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                .replyMarkup(keyboard)
                .build();
    }

    public static SendPhoto animePhoto(long chatId, String photoUrl, String caption, InlineKeyboardMarkup keyboard) {
        return SendPhoto.builder()
                .chatId(chatId)
                .photo(new InputFile(photoUrl))
                .caption(caption)
                .parseMode("HTML")
                .replyMarkup(keyboard)
                .build();
    }

    public static String animeInfoText(AnimeDetailsDTO anime) {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>").append(anime.getTitle()).append("</b>\n\n");
        if (!anime.getDuration().isEmpty()) sb.append("⏱ Duration: ").append(anime.getDuration()).append("\n");
        if (!anime.getSeason().isEmpty()) sb.append("📅 Season: ").append(anime.getSeason()).append("\n");
        if (!anime.getYear().isEmpty()) sb.append("🗓 Year: ").append(anime.getYear()).append("\n");
        if (!anime.getReleaseDate().isEmpty()) sb.append("📅 Release: ").append(anime.getReleaseDate()).append("\n");
        if (!anime.getGenres().isEmpty()) sb.append("🎭 Genres: ").append(String.join(", ", anime.getGenres())).append("\n");
        if (!anime.getStudios().isEmpty()) sb.append("🎬 Studios: ").append(String.join(", ", anime.getStudios())).append("\n");
        if (!anime.getAuthors().isEmpty()) sb.append("✍️ Authors: ").append(String.join(", ", anime.getAuthors())).append("\n");
        if (!anime.getSynopsis().isEmpty()) {
            sb.append("\n📖 Synopsis:\n").append(anime.getSynopsis());
            if (anime.getSynopsis().length() > 1000) sb.append("...");
        }
        return sb.toString();
    }

    public static String characterInfoText(CharacterDTO character) {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>").append(character.getName()).append("</b>\n\n");
        sb.append("ID: ").append(character.getMalId());
        return sb.toString();
    }
}