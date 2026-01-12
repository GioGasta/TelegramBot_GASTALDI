package org.projectATB.session;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.projectATB.model.AnimeSearchResult;

public class UserSession
{

    private UserState state = UserState.IDLE;

    private Set<String> pendingAdd = new HashSet<>();
    private Set<String> pendingWatched = new HashSet<>();
    private Set<String> pendingRemove = new HashSet<>();
    private Set<Integer> pendingIndexes = new HashSet<>();
    private List<String> cachedList = new ArrayList<>(); // snapshot della lista
    private List<AnimeSearchResult> searchResults = new ArrayList<>();
    private String currentSearchQuery;
    private Set<Integer> selectedSearchIndexes = new HashSet<>();

    private Integer lastBotMessageId;
    private Integer lastUserMessageId;

    public UserState getState()
    {
        return state;
    }

    public void setState(UserState state)
    {
        this.state = state;
    }

    public Set<String> getPendingAdd()
    {
        return pendingAdd;
    }

    public Set<String> getPendingWatched()
    {
        return pendingWatched;
    }

    public Set<String> getPendingRemove()
    {
        return pendingRemove;
    }

    public Set<Integer> getPendingIndexes()
    {
        return pendingIndexes;
    }

    public List<String> getCachedList()
    {
        return cachedList;
    }

    public void setCachedList(List<String> cachedList)
    {
        this.cachedList = new ArrayList<>(cachedList);
    }

    public Integer getLastBotMessageId() {
        return lastBotMessageId;
    }

    public void setLastBotMessageId(Integer lastBotMessageId) {
        this.lastBotMessageId = lastBotMessageId;
    }

    public Integer getLastUserMessageId() {
        return lastUserMessageId;
    }

    public void setLastUserMessageId(Integer lastUserMessageId) {
        this.lastUserMessageId = lastUserMessageId;
    }

    public void clearPending() {
        pendingAdd.clear();
        pendingWatched.clear();
        pendingRemove.clear();
        pendingIndexes.clear();
        cachedList.clear();
        clearSearchResults();
        setState(UserState.IDLE);
    }

    public void clear() {
        clearPending();
    }

    // Search results related methods
    public List<AnimeSearchResult> getSearchResults() {
        return searchResults;
    }

    public void setSearchResults(List<AnimeSearchResult> searchResults) {
        this.searchResults = new ArrayList<>(searchResults);
    }

    public String getCurrentSearchQuery() {
        return currentSearchQuery;
    }

    public void setCurrentSearchQuery(String currentSearchQuery) {
        this.currentSearchQuery = currentSearchQuery;
    }

    public Set<Integer> getSelectedSearchIndexes() {
        return selectedSearchIndexes;
    }

    public Set<Integer> getSelectedIndexesFromPending() {
        Set<Integer> indexes = new HashSet<>();
        if (searchResults != null) {
            for (int i = 0; i < searchResults.size(); i++) {
                if (pendingAdd.contains(searchResults.get(i).getTitle())) {
                    indexes.add(i);
                }
            }
        }
        return indexes;
    }

    public void clearSearchResults() {
        searchResults.clear();
        selectedSearchIndexes.clear();
        currentSearchQuery = null;
    }
}