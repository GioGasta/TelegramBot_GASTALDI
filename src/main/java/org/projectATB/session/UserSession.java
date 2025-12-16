package org.projectATB.session;

import java.util.HashSet;
import java.util.Set;

public class UserSession {

    private UserState state = UserState.IDLE;

    private Set<Integer> pendingAdd = new HashSet<>();
    private Set<Integer> pendingWatched = new HashSet<>();
    private Set<Integer> pendingRemove = new HashSet<>();

    public UserState getState() {
        return state;
    }

    public void setState(UserState state) {
        this.state = state;
    }

    public Set<Integer> getPendingAdd() {
        return pendingAdd;
    }
}