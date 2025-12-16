package org.projectATB.session;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {

    private static final Map<Long, UserSession> sessions = new ConcurrentHashMap<>();

    public static UserSession get(long chatId) {
        return sessions.computeIfAbsent(chatId, id -> new UserSession());
    }

    public static void clear(long chatId) {
        sessions.remove(chatId);
    }
}