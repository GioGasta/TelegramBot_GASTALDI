package org.projectATB.db;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.Statement;

public class Database
{
    private static final String URL = "jdbc:sqlite:animebot.db";

    public static Connection getConnection() throws SQLException
    {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {
            
            // Create user_anime table if it doesn't exist
            String sql = "CREATE TABLE IF NOT EXISTS user_anime (" +
                        "chat_id INTEGER NOT NULL, " +
                        "anime_name TEXT NOT NULL, " +
                        "watched BOOLEAN DEFAULT FALSE, " +
                        "PRIMARY KEY (chat_id, anime_name)" +
                        ")";
            stmt.execute(sql);
        }

        return  DriverManager.getConnection(URL);
    }

}
