package org.projectATB.handler;

import org.projectATB.dto.CharacterDTO;
import org.projectATB.dto.EpisodeDTO;
import org.projectATB.dto.StaffDTO;
import org.projectATB.model.AnimeSearchResult;
import org.projectATB.service.JikanApiFacade;
import org.projectATB.service.UserListService;
import org.projectATB.session.SessionManager;
import org.projectATB.session.UserSession;
import org.projectATB.session.UserState;
import org.projectATB.telegram.CallbackDataParser;
import org.projectATB.telegram.KeyboardFactory;
import org.projectATB.telegram.MessageFactory;
import org.projectATB.telegram.MessageUtils;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.util.Set.of;

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
        else if (data.startsWith("ANIME:")) {
            handleAnimeAction(update, session, data);
        }
        else {
            handleMenuAction(chatId, data, session, update);
        }
    }

    /* =========================
       MENU ACTIONS
       ========================= */

    private void handleMenuAction(long chatId, String data, UserSession session, Update update)
            throws TelegramApiException {
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        MessageUtils.removeKeyboard(client, chatId, messageId);

        switch (data) {

            case CallbackDataParser.LIST_ADD -> {
                session.setState(UserState.LIST_ADD);
                MessageUtils.sendAndTrack(
                        client,
                        session,
                        MessageFactory.simple(
                            chatId,
                            "Type the anime name(s) you want to add.\n/done when finished."
                        )
                );
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

        MessageUtils.sendAndTrack(
                client,
                session,
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

        if (isAdded) {
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
        }

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

            MessageUtils.sendAndTrack(
                    client,
                    session,
                    MessageFactory.simple(
                        update.getCallbackQuery().getMessage().getChatId(),
                        "🔍 Please type a different anime name to search again."
                    )
            );
        }
    }

    private void handleAnimeAction(Update update, UserSession session, String data) throws TelegramApiException {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String[] parts = data.split(":");
        String action = parts[1];

        if (action.equals("CHAR")) {
            if (parts.length >= 3) {
                int malId = Integer.parseInt(parts[2]);
                showCharacters(chatId, malId);
            }
        }
        else if (action.equals("SEQUEL")) {
            if (parts.length >= 3) {
                int malId = Integer.parseInt(parts[2]);
                showSequel(chatId, malId);
            }
        }
        else if (action.equals("PREQUEL")) {
            if (parts.length >= 3) {
                int malId = Integer.parseInt(parts[2]);
                showPrequel(chatId, malId);
            }
        }
        else if (action.equals("STAFF")) {
            if (parts.length >= 3) {
                int malId = Integer.parseInt(parts[2]);
                showStaff(chatId, malId);
            }
        }
        else if (action.equals("EPISODES")) {
            if (parts.length >= 3) {
                int malId = Integer.parseInt(parts[2]);
                showEpisodes(chatId, malId);
            }
        }
        else if (action.equals("EPISODES_LINK")) {
            if (parts.length >= 3) {
                int malId = Integer.parseInt(parts[2]);
                String animeTitle = parts[3].replace("_", " ");
                String url = JikanApiFacade.resolveEpisodeUrl(animeTitle, 1);
                String msg = "🔗 Watch on AnimePahe: " + url;
                client.execute(MessageFactory.simple(chatId, msg));
            }
        }
        else if (action.equals("ADD")) {
            if (parts.length >= 3) {
                int malId = Integer.parseInt(parts[2]);
                promptAddAnime(chatId, malId);
            }
        }
        else if (action.equals("ADD_YES")) {
            if (parts.length >= 3) {
                int malId = Integer.parseInt(parts[2]);
                confirmAddAnime(chatId, malId);
            }
        }
        else if (action.equals("ADD_NO")) {
            client.execute(MessageFactory.simple(chatId, "❌ Add cancelled."));
        }
        else if (action.equals("WATCHED")) {
            if (parts.length >= 3) {
                int malId = Integer.parseInt(parts[2]);
                promptMarkWatched(chatId, malId);
            }
        }
        else if (action.equals("WATCHED_YES")) {
            if (parts.length >= 3) {
                int malId = Integer.parseInt(parts[2]);
                confirmMarkWatched(chatId, malId);
            }
        }
        else if (action.equals("WATCHED_NO")) {
            client.execute(MessageFactory.simple(chatId, "❌ Mark as watched cancelled."));
        }
    }

    private void showCharacters(long chatId, int malId) throws TelegramApiException {
        List<CharacterDTO> characters = JikanApiFacade.getCharacters(malId, 7);
        if (characters.isEmpty()) {
            client.execute(MessageFactory.simple(chatId, "No characters found."));
            return;
        }
        StringBuilder sb = new StringBuilder("<b>Characters:</b>\n\n");
        for (int i = 0; i < characters.size(); i++) {
            CharacterDTO c = characters.get(i);
            sb.append(i + 1).append(". ").append(c.getName());
            if (!c.getImageUrl().isEmpty()) sb.append(" 📷");
            sb.append("\n");
        }
        client.execute(MessageFactory.simple(chatId, sb.toString()));
    }

    private void showSequel(long chatId, int malId) throws TelegramApiException {
        var sequel = JikanApiFacade.getSequel(malId);
        if (sequel == null) {
            client.execute(MessageFactory.simple(chatId, "No sequel found."));
            return;
        }
        String msg = "<b>Sequel:</b> " + sequel.getTitle();
        if (!sequel.getImageUrl().isEmpty()) msg += " 🖼️";
        client.execute(MessageFactory.simple(chatId, msg));
    }

    private void showPrequel(long chatId, int malId) throws TelegramApiException {
        var prequel = JikanApiFacade.getPrequel(malId);
        if (prequel == null) {
            client.execute(MessageFactory.simple(chatId, "No prequel found."));
            return;
        }
        String msg = "<b>Prequel:</b> " + prequel.getTitle();
        if (!prequel.getImageUrl().isEmpty()) msg += " 🖼️";
        client.execute(MessageFactory.simple(chatId, msg));
    }

    private void showStaff(long chatId, int malId) throws TelegramApiException {
        List<StaffDTO> staff = JikanApiFacade.getStaff(malId, 7);
        if (staff.isEmpty()) {
            client.execute(MessageFactory.simple(chatId, "No staff found."));
            return;
        }
        StringBuilder sb = new StringBuilder("<b>Staff:</b>\n\n");
        for (int i = 0; i < staff.size(); i++) {
            StaffDTO s = staff.get(i);
            sb.append(i + 1).append(". ").append(s.getName());
            if (!s.getImageUrl().isEmpty()) sb.append(" 📷");
            sb.append("\n");
        }
        client.execute(MessageFactory.simple(chatId, sb.toString()));
    }

    private void showEpisodes(long chatId, int malId) throws TelegramApiException {
        List<EpisodeDTO> episodes = JikanApiFacade.getEpisodes(malId);
        if (episodes.isEmpty()) {
            client.execute(MessageFactory.simple(chatId, "No episodes found."));
            return;
        }
        StringBuilder sb = new StringBuilder("<b>Episodes:</b>\n\n");
        int start = Math.max(0, episodes.size() - 10);
        for (int i = start; i < episodes.size(); i++) {
            EpisodeDTO ep = episodes.get(i);
            sb.append("Episode ").append(ep.getEpisode());
            if (!ep.getTitle().isEmpty()) sb.append(": ").append(ep.getTitle());
            sb.append("\n");
        }
        client.execute(MessageFactory.simple(chatId, sb.toString()));
    }

    private void promptAddAnime(long chatId, int malId) throws TelegramApiException {
        var anime = JikanApiFacade.getAnimeDetails(malId);
        if (anime == null) {
            client.execute(MessageFactory.simple(chatId, "Anime not found."));
            return;
        }
        String msg = "Do you want to add \"" + anime.getTitle() + "\" to your list?";
        client.execute(MessageFactory.withKeyboard(chatId, msg, KeyboardFactory.confirmKeyboard("ADD", String.valueOf(malId))));
    }

    private void confirmAddAnime(long chatId, int malId) throws TelegramApiException {
        var anime = JikanApiFacade.getAnimeDetails(malId);
        if (anime == null) {
            client.execute(MessageFactory.simple(chatId, "Anime not found."));
            return;
        }
        UserListService.addAll(chatId, Set.of(anime.getTitle()));
        client.execute(MessageFactory.simple(chatId, "✅ Added \"" + anime.getTitle() + "\" to your list."));
    }

    private void promptMarkWatched(long chatId, int malId) throws TelegramApiException {
        var anime = JikanApiFacade.getAnimeDetails(malId);
        if (anime == null) {
            client.execute(MessageFactory.simple(chatId, "Anime not found."));
            return;
        }
        String msg = "Do you want to mark \"" + anime.getTitle() + "\" as watched?";
        client.execute(MessageFactory.withKeyboard(chatId, msg, KeyboardFactory.confirmKeyboard("WATCHED", String.valueOf(malId))));
    }

    private void confirmMarkWatched(long chatId, int malId) throws TelegramApiException {
        var anime = JikanApiFacade.getAnimeDetails(malId);
        if (anime == null) {
            client.execute(MessageFactory.simple(chatId, "Anime not found."));
            return;
        }
        UserListService.markWatched(chatId, Set.of(anime.getTitle()));
        client.execute(MessageFactory.simple(chatId, "✅ Marked \"" + anime.getTitle() + "\" as watched."));
    }
}