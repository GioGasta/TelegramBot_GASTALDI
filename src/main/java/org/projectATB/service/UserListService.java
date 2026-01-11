package org.projectATB.service;

import org.projectATB.db.Database;
import org.projectATB.model.AnimeEntry;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UserListService {

public static List<AnimeEntry> getList(long chatId) {

        List<AnimeEntry> list = new ArrayList<>();

        try (Connection c = Database.getConnection();
             PreparedStatement ps =
                     c.prepareStatement(
                             "SELECT anime_name, watched FROM user_anime WHERE chat_id = ?"
                     )) {

            ps.setLong(1, chatId);

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String name = rs.getString("anime_name");
                boolean watched = rs.getBoolean("watched");
                list.add(new AnimeEntry(name, watched));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    public static List<String> getAnimeNames(long chatId) {
        List<AnimeEntry> entries = getList(chatId);
        List<String> names = new ArrayList<>();
        for (AnimeEntry entry : entries) {
            names.add(entry.getName());
        }
        return names;
    }

    public static void addAll(long chatId, Collection<String> animeNames) {

        try (Connection c = Database.getConnection();
             PreparedStatement ps =
                     c.prepareStatement(
                             "INSERT INTO user_anime(chat_id, anime_name) VALUES (?, ?) ON CONFLICT DO NOTHING"
                     )) {

            for (String name : animeNames) {
                ps.setLong(1, chatId);
                ps.setString(2, name);
                ps.addBatch();
            }

            ps.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void markWatched(long chatId, Collection<String> animeNames) {

        try (Connection c = Database.getConnection();
             PreparedStatement ps =
                     c.prepareStatement(
                             "UPDATE user_anime SET watched = true WHERE chat_id = ? AND anime_name = ?"
                     )) {

            for (String name : animeNames) {
                ps.setLong(1, chatId);
                ps.setString(2, name);
                ps.addBatch();
            }

            ps.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeAll(long chatId, Collection<String> animeNames) {

        try (Connection c = Database.getConnection();
             PreparedStatement ps =
                     c.prepareStatement(
                             "DELETE FROM user_anime WHERE chat_id = ? AND anime_name = ?"
                     )) {

            for (String name : animeNames) {
                ps.setLong(1, chatId);
                ps.setString(2, name);
                ps.addBatch();
            }

            ps.executeBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean isEmpty(long chatId) {
        return getList(chatId).isEmpty();
    }
}