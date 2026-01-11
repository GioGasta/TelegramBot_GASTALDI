package org.projectATB.session;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserSession
{

    private UserState state = UserState.IDLE;

    private Set<String> pendingAdd = new HashSet<>();
    private Set<String> pendingWatched = new HashSet<>();
    private Set<String> pendingRemove = new HashSet<>();
    private Set<Integer> pendingIndexes = new HashSet<>();
    private List<String> cachedList = new ArrayList<>(); // snapshot della lista

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

    public boolean hasPendingActions() {
        return !pendingAdd.isEmpty() || !pendingWatched.isEmpty() || !pendingRemove.isEmpty() || !pendingIndexes.isEmpty();
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

    public void clearPending() {
        pendingAdd.clear();
        pendingWatched.clear();
        pendingRemove.clear();
        pendingIndexes.clear();
        cachedList.clear();
        setState(UserState.IDLE);
    }

    public void clear() {
        clearPending();
    }
}