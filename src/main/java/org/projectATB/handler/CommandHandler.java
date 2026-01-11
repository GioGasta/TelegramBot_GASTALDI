package org.projectATB.handler;

import org.projectATB.model.AnimeEntry;
import org.projectATB.model.AnimeSearchResult;
import org.projectATB.session.SessionManager;
import org.projectATB.session.UserSession;
import org.projectATB.session.UserState;
import org.projectATB.service.JikanApiService;
import org.projectATB.service.UserListService;
import org.projectATB.telegram.KeyboardFactory;
import org.projectATB.telegram.MessageFactory;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CommandHandler {

    private final TelegramClient client;

    public CommandHandler(TelegramClient client) {
        this.client = client;
    }

    public void handle(Update update) throws TelegramApiException {

        String text = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();
        UserSession session = SessionManager.get(chatId);

        /* =====================
           TEXT INPUT
        ===================== */
        if (!text.startsWith("/")) {
            handleFreeText(chatId, text, session);
            return;
        }

        /* =====================
           EXIT
         ===================== */
        if (text.equals("/exit")) {
            handleExit(chatId, session);
            return;
        }

        /* =====================
           DONE
         ===================== */
        if (text.equals("/done")) {
            handleDone(chatId, session);
            return;
        }

        /* =====================
           LIST
        ===================== */
        if (text.equals("/list")) {
            sendList(chatId);
            session.setState(UserState.IDLE);
            return;
        }

        client.execute(MessageFactory.simple(chatId, "Unknown command."));
    }

    /* =====================
       FREE TEXT HANDLER
    ===================== */
    private void handleFreeText(long chatId, String text, UserSession session)
            throws TelegramApiException {

        switch (session.getState()) {

            case LIST_ADD -> {
                List<AnimeSearchResult> results = JikanApiService.searchAnimeWithDetails(text);
                
                if (results.isEmpty()) {
                    client.execute(MessageFactory.simple(chatId,
                            "❌ No anime found for '" + text + "'. Please try a different search term."));
                } else if (JikanApiService.isExactMatch(text, results)) {
                    // Auto-add exact match
                    AnimeSearchResult result = results.get(0);
                    session.getPendingAdd().add(result.getTitle());
                    client.execute(MessageFactory.simple(chatId,
                            "✅ Added \"" + result.getTitle() + "\""));
                } else {
                    // Multiple matches - show selection keyboard
                    session.setState(UserState.ANIME_SEARCH_SELECTING);
                    session.setSearchResults(results);
                    session.setCurrentSearchQuery(text);
                    
                    String message = "📝 Found " + results.size() + " matches for '" + text + "'. Please select:";
                    Set<String> existingTitles = new HashSet<>(UserListService.getAnimeNames(chatId));
                    client.execute(MessageFactory.withKeyboard(chatId, message,
                            KeyboardFactory.animeSearchResults(results, session.getSelectedIndexesFromPending(), existingTitles)));
                }
            }

            case LIST_WATCHED -> {
                session.getPendingWatched().add(text);
                client.execute(MessageFactory.simple(chatId,
                        "Marked \"" + text + "\" as watched (pending)"));
            }

            case LIST_REMOVE -> {
                session.getPendingRemove().add(text);
                client.execute(MessageFactory.simple(chatId,
                        "Will remove \"" + text + "\""));
            }

            case CONFIRM_ADD, CONFIRM_WATCHED, CONFIRM_REMOVE -> {
                handleConfirmationResponse(chatId, text.toLowerCase(), session);
            }

            /*case ANIME_SEARCH_SELECTING -> {
                // Add selected anime from search results to pending list
                Set<Integer> selectedIndexes = session.getSelectedSearchIndexes();
                List<AnimeSearchResult> searchResults = session.getSearchResults();
                
                if (selectedIndexes.isEmpty()) {
                    client.execute(MessageFactory.simple(chatId, "No anime selected. Please select from the options above."));
                    return;
                }
                
                session.clearPending(); // Clear any existing pending adds
                for (Integer index : selectedIndexes) {
                    if (index >= 0 && index < searchResults.size()) {
                        AnimeSearchResult result = searchResults.get(index);
                        session.getPendingAdd().add(result.getTitle());
                    }
                }
                
                String animeNames = formatAnimeList(session.getPendingAdd());
                client.execute(MessageFactory.simple(chatId, 
                    "Do you want to add " + animeNames + " to your list? (yes/y to confirm, no/n to cancel, /exit to exit)"));
                session.setState(UserState.CONFIRM_ADD);
            }*/

            default -> client.execute(
                    MessageFactory.simple(chatId, "I'm not expecting text right now.")
            );
        }
    }

    /* =====================
        EXIT HANDLER
     ===================== */
    private void handleExit(long chatId, UserSession session)
            throws TelegramApiException {

        if (session.getState() == UserState.IDLE) {
            client.execute(MessageFactory.simple(chatId, "No operation to cancel."));
            return;
        }

        session.clearPending();
        client.execute(MessageFactory.simple(chatId, "Operation cancelled. All pending changes have been discarded."));
    }

    /* =====================
        DONE HANDLER
     ===================== */
    private void handleDone(long chatId, UserSession session)
            throws TelegramApiException {

        switch (session.getState()) {

            case LIST_ADD -> {
                if (session.getPendingAdd().isEmpty()) {
                    client.execute(MessageFactory.simple(chatId, "Nothing to add."));
                    return;
                }
                String animeNames = formatAnimeList(session.getPendingAdd());
                client.execute(MessageFactory.simple(chatId, 
                    "Do you want to add " + animeNames + " to your list? (yes/y to confirm, no/n to cancel, /exit to exit)"));
                session.setState(UserState.CONFIRM_ADD);
            }

            case LIST_WATCHED -> {
                Set<String> selectedAnimes = getSelectedAnimes(session);
                if (selectedAnimes.isEmpty()) {
                    client.execute(MessageFactory.simple(chatId, "Nothing to mark as watched."));
                    return;
                }
                String animeNames = formatAnimeList(selectedAnimes);
                client.execute(MessageFactory.simple(chatId, 
                    "Do you want to add " + animeNames + " to watched? (yes/y to confirm, no/n to cancel, /exit to exit)"));
                session.setState(UserState.CONFIRM_WATCHED);
            }

            case LIST_REMOVE -> {
                Set<String> selectedAnimes = getSelectedAnimes(session);
                if (selectedAnimes.isEmpty()) {
                    client.execute(MessageFactory.simple(chatId, "Nothing to remove."));
                    return;
                }
                String animeNames = formatAnimeList(selectedAnimes);
                client.execute(MessageFactory.simple(chatId, 
                    "Do you really want to remove \n" + animeNames + "from the list? (yes/y to confirm, no/n to cancel, /exit to exit)"));
                session.setState(UserState.CONFIRM_REMOVE);
            }

            case CONFIRM_ADD -> {
                UserListService.addAll(chatId, session.getPendingAdd());
                sendSummary(chatId, "Added:", session.getPendingAdd());
                session.clearPending();
            }

            case CONFIRM_WATCHED -> {
                Set<String> selectedAnimes = getSelectedAnimes(session);
                UserListService.markWatched(chatId, selectedAnimes);
                sendSummary(chatId, "Marked as watched:", selectedAnimes);
                session.clearPending();
            }

            case CONFIRM_REMOVE -> {
                Set<String> selectedAnimes = getSelectedAnimes(session);
                UserListService.removeAll(chatId, selectedAnimes);
                sendSummary(chatId, "Removed:", selectedAnimes);
                session.clearPending();
            }

            default -> {
                client.execute(MessageFactory.simple(chatId, "Nothing to confirm."));
                return;
            }
        }
    }

    /* =====================
        CONFIRMATION HANDLER
     ===================== */
    private void handleConfirmationResponse(long chatId, String response, UserSession session)
            throws TelegramApiException {

        if (response.equals("yes") || response.equals("y")) {
            // Execute the action based on current confirmation state
            switch (session.getState()) {
                case CONFIRM_ADD -> {
                    UserListService.addAll(chatId, session.getPendingAdd());
                    sendSummary(chatId, "Added:", session.getPendingAdd());
                }
                case CONFIRM_WATCHED -> {
                    Set<String> selectedAnimes = getSelectedAnimes(session);
                    UserListService.markWatched(chatId, selectedAnimes);
                    sendSummary(chatId, "Marked as watched:", selectedAnimes);
                }
                case CONFIRM_REMOVE -> {
                    Set<String> selectedAnimes = getSelectedAnimes(session);
                    UserListService.removeAll(chatId, selectedAnimes);
                    sendSummary(chatId, "Removed:", selectedAnimes);
                }
            }
            session.clearPending();
        } else if (response.equals("no") || response.equals("n")) {
            client.execute(MessageFactory.simple(chatId, "Operation cancelled. Use /list to see your anime list again."));
            session.clearPending();
        } else {
            client.execute(MessageFactory.simple(chatId, 
                "Please respond with yes/y to confirm, no/n to cancel, or /exit to exit immediately."));
        }
    }

    /* =====================
        HELPERS
     ===================== */
    private Set<String> getSelectedAnimesFromSearch(UserSession session) {
        Set<String> selected = new HashSet<>();
        List<AnimeSearchResult> searchResults = session.getSearchResults();
        if (searchResults != null) {
            for (Integer index : session.getSelectedSearchIndexes()) {
                if (index >= 0 && index < searchResults.size()) {
                    selected.add(searchResults.get(index).getTitle());
                }
            }
        }
        return selected;
    }

    private Set<String> getSelectedAnimes(UserSession session) {
        Set<String> selected = new HashSet<>();
        List<String> cachedList = session.getCachedList();
        if (cachedList != null) {
            for (Integer index : session.getPendingIndexes()) {
                if (index > 0 && index <= cachedList.size()) {
                    selected.add(cachedList.get(index - 1));
                }
            }
        }
        return selected;
    }

    private String formatAnimeList(Set<String> animes) {
        if (animes.isEmpty()) return "";
        if (animes.size() == 1) return animes.iterator().next();
        
        StringBuilder sb = new StringBuilder();
        int i = 0;
        for (String anime : animes) {
            /*if (i > 0) sb.append(", ");
            sb.append(anime);
            i++;*/
            sb.append("- ").append(anime).append("\n");
        }
        return sb.toString();
    }
    private void sendSummary(long chatId, String title, Set<String> items)
            throws TelegramApiException {

        StringBuilder sb = new StringBuilder(title + "\n\n");
        int i = 1;
        for (String s : items) {
            sb.append(i++).append(". ").append(s).append("\n");
        }
        client.execute(MessageFactory.simple(chatId, sb.toString()));
    }

    private void sendList(long chatId) throws TelegramApiException {
        List<AnimeEntry> list = UserListService.getList(chatId);
        String message = "Here's your list: \n\n";

        if (list.isEmpty()) {
            client.execute(MessageFactory.withKeyboard(
                    chatId,
                    message + "oh... it's empty.",
                    KeyboardFactory.listActions()
            ));
            return;
        }

        StringBuilder sb = new StringBuilder();
        int i = 1;
        for (AnimeEntry entry : list) {
            String checkmark = entry.isWatched() ? "✅ " : "";
            sb.append(i++).append(". ").append(checkmark).append(entry.getName()).append("\n");
        }

        client.execute(MessageFactory.withKeyboard(
                chatId,
                message + sb,
                KeyboardFactory.listActions()
        ));
    }
}