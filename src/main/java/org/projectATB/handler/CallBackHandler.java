package org.projectATB.handler;

import org.projectATB.model.AnimeSearchResult;
import org.projectATB.service.UserListService;
import org.projectATB.session.SessionManager;
import org.projectATB.session.UserSession;
import org.projectATB.session.UserState;
import org.projectATB.telegram.CallbackDataParser;
import org.projectATB.telegram.KeyboardFactory;
import org.projectATB.telegram.MessageFactory;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CallBackHandler {

    private final TelegramClient client;

    public CallBackHandler(TelegramClient client) {
        this.client = client;
    }

    public void handle(Update update) throws TelegramApiException {

        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String data = update.getCallbackQuery().getData();
        String callbackId = update.getCallbackQuery().getId();

        UserSession session = SessionManager.get(chatId);

        if (data.startsWith("W:") || data.startsWith("R:")) {
            handleListSelection(update, session);
        }
        else if (data.startsWith("ANIME_SEARCH:")) {
            handleAnimeSearch(update, session);
        }
        else {
            handleMenuAction(chatId, data, session);
        }
    }

    /* =========================
       MENU ACTIONS
       ========================= */

    private void handleMenuAction(long chatId, String data, UserSession session)
            throws TelegramApiException {

        switch (data) {

            case CallbackDataParser.LIST_ADD -> {
                session.setState(UserState.LIST_ADD);
                client.execute(MessageFactory.simple(
                        chatId,
                        "Type the anime name(s) you want to add.\n/done when finished."
                ));
            }

            case CallbackDataParser.LIST_WATCHED ->
                    startListMode(chatId, session, UserState.LIST_WATCHED, "W",
                            "Select animes to mark as watched:");

            case CallbackDataParser.LIST_REMOVE ->
                    startListMode(chatId, session, UserState.LIST_REMOVE, "R",
                            "Select animes to remove:");
        }
    }

    private void startListMode(
            long chatId,
            UserSession session,
            UserState state,
            String prefix,
            String message
    ) throws TelegramApiException {

        List<String> list = UserListService.getAnimeNames(chatId);

        session.clearPending();
        session.setState(state);
        session.setCachedList(list);

        client.execute(
                MessageFactory.withKeyboard(
                        chatId,
                        message + "\n/done when finished.",
                        KeyboardFactory.animeSelection(
                                list,
                                session.getPendingIndexes(),
                                prefix
                        )
                )
        );
    }

    /* =========================
       WATCHED / REMOVE SELECTION
       ========================= */

    private void handleListSelection(
            Update update,
            UserSession session
    ) throws TelegramApiException {

        String data = update.getCallbackQuery().getData();

        String[] parts = data.split(":");
        String prefix = parts[0];
        int index = Integer.parseInt(parts[1]);

        List<String> list = session.getCachedList();
        if (list == null || index < 1 || index > list.size()) {
            return;
        }

        toggle(session.getPendingIndexes(), index);

        client.execute(
                MessageFactory.editKeyboard(
                        update,
                        KeyboardFactory.animeSelection(
                                list,
                                session.getPendingIndexes(),
                                prefix
                        )
                )
        );
    }

    private void toggle(Set<Integer> set, int value) {
        if (!set.add(value)) {
            set.remove(value);
        }
    }

    /* =========================
        ANIME SEARCH BUTTON SELECTION
       ========================= */

    private void handleAnimeSearchSelect(
            Update update,
            UserSession session
    ) throws TelegramApiException {

        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String callbackId = update.getCallbackQuery().getId();

        int index = Integer.parseInt(update.getCallbackQuery().getData().split(":")[2]);

        List<AnimeSearchResult> results = session.getSearchResults();
        if (index < 0 || index >= results.size()) return;

        String animeTitle = results.get(index).getTitle();

        // Toggle selection
        boolean isSelected;
        if (!session.getSelectedSearchIndexes().add(index)) {
            session.getSelectedSearchIndexes().remove(index);
            isSelected = false;
        } else {
            isSelected = true;
        }

        boolean isAdded = session.getPendingAdd().add(animeTitle);
        if (!isAdded) session.getPendingAdd().remove(animeTitle);

        client.execute(
                AnswerCallbackQuery.builder()
                        .callbackQueryId(callbackId)
                        .text(isSelected
                                ? "✅ Added " + animeTitle
                                : "❌ Deselected" + animeTitle
                        )
                        .showAlert(false)
                        .build()
        );

        Set<String> existingTitles = new HashSet<>(UserListService.getAnimeNames(chatId));
        client.execute(
                MessageFactory.editKeyboard(
                        update,
                        KeyboardFactory.animeSearchResults(
                                results,
                                session.getSelectedIndexesFromPending(),
                                existingTitles
                        )
                )
        );

        session.setState(UserState.LIST_ADD);
    }

    /* =========================
       JIKAN SEARCH SELECTION
       ========================= */

    private void handleAnimeSearch(
            Update update,
            UserSession session
    ) throws TelegramApiException {

        String data = update.getCallbackQuery().getData();

        if (data.startsWith("ANIME_SEARCH:SELECT")) {
            handleAnimeSearchSelect(update, session);
            return;
        }

        if (data.equals("ANIME_SEARCH:RETRY")) {
            session.clearSearchResults();
            session.setState(UserState.LIST_ADD);

            client.execute(MessageFactory.simple(
                    update.getCallbackQuery().getMessage().getChatId(),
                    "🔍 Please type a different anime name to search again."
            ));
        }
    }
}