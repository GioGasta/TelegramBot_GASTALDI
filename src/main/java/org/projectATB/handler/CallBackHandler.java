package org.projectATB.handler;

import org.projectATB.service.UserListService;
import org.projectATB.telegram.KeyboardFactory;
import org.projectATB.telegram.MessageFactory;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import org.projectATB.session.SessionManager;
import org.projectATB.session.UserSession;
import org.projectATB.session.UserState;
import org.projectATB.telegram.CallbackDataParser;
import org.projectATB.telegram.MessageFactory;

import java.util.List;
import java.util.Set;

public class CallBackHandler {

    private final TelegramClient client;

    public CallBackHandler(TelegramClient client) {
        this.client = client;
    }

    public void handle(Update update) throws TelegramApiException
    {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String data = update.getCallbackQuery().getData();
        String callbackId = update.getCallbackQuery().getId();

        UserSession session = SessionManager.get(chatId);

        // Handle anime selection callbacks (W:1, R:2, etc.)
        if (data.startsWith("W:") || data.startsWith("R:")) {
            handleAnimeSelection(chatId, data, session, callbackId);
            return;
        }

        // Answer callback to remove loading state
        AnswerCallbackQuery answerCallback = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackId)
                .build();
        client.execute(answerCallback);

        switch (data) {
            case CallbackDataParser.LIST_ADD -> {
                session.setState(UserState.LIST_ADD);
                client.execute(
                        MessageFactory.simple(
                                chatId,
                                "Type the anime name(s) you want to add.\n /done when finished."
                        )
                );
            }

            case CallbackDataParser.LIST_WATCHED -> {
                List<String> list = UserListService.getAnimeNames(chatId);

                session.clearPending();
                session.setState(UserState.LIST_WATCHED);
                session.setCachedList(list);

                client.execute(
                        MessageFactory.withKeyboard(
                                chatId,
                                "Select animes to mark as watched: \n/done when finished.",
                                KeyboardFactory.animeSelection(list, session.getPendingIndexes(), "W")
                        )
                );
            }

            case CallbackDataParser.LIST_REMOVE -> {
                List<String> list = UserListService.getAnimeNames(chatId);

                session.clearPending();
                session.setState(UserState.LIST_REMOVE);
                session.setCachedList(list);

                client.execute(
                        MessageFactory.withKeyboard(
                                chatId,
                                "Select animes to remove: \n/done when finished.",
                                KeyboardFactory.animeSelection(list, session.getPendingIndexes(), "R")
                        )
                );
            }
        }
    }

    private void handleAnimeSelection(long chatId, String data, UserSession session, String callbackId) throws TelegramApiException {
        String[] parts = data.split(":");
        String action = parts[0];
        int index = Integer.parseInt(parts[1]);

        List<String> list = session.getCachedList();
        if (list == null || index < 1 || index > list.size()) {
            return;
        }

        String animeName = list.get(index - 1);
        
        // Toggle selection
        boolean isNowSelected;
        if (session.getPendingIndexes().contains(index)) {
            session.getPendingIndexes().remove(index);
            isNowSelected = false;
        } else {
            session.getPendingIndexes().add(index);
            isNowSelected = true;
        }

        // Answer callback to remove loading state
        AnswerCallbackQuery answerCallback = AnswerCallbackQuery.builder()
                .callbackQueryId(callbackId)
                .build();
        client.execute(answerCallback);

        // Show feedback message
        String feedback;
        if (action.equals("W")) {
            feedback = isNowSelected ? 
                "✅ \"" + animeName + "\" will be marked as watched" : 
                "❌ \"" + animeName + "\" will NOT be marked as watched";
        } else {
            feedback = isNowSelected ? 
                "🗑️ \"" + animeName + "\" will be removed from your list" : 
                "➡️ \"" + animeName + "\" will NOT be removed";
        }

        // Update keyboard with current selections
        String prefix = action.equals("W") ? "W" : "R";
        String message = action.equals("W") ? 
                "Select animes to mark as watched: \n/done when finished." :
                "Select animes to remove: \n/done when finished.";

        client.execute(
                MessageFactory.withKeyboard(
                        chatId,
                        feedback + "\n\n" + message,
                        KeyboardFactory.animeSelection(list, session.getPendingIndexes(), prefix)
                )
        );
    }
}