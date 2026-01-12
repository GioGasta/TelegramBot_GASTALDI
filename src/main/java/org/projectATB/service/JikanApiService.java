package org.projectATB.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.projectATB.model.AnimeSearchResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JikanApiService {

    private static final String JIKAN_API_BASE = "https://api.jikan.moe/v4";
    private static final Gson gson = new Gson();

    public static boolean validateAnime(String animeName) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String encodedName = animeName.replace(" ", "%20");
            HttpGet request = new HttpGet(JIKAN_API_BASE + "/anime?q=" + encodedName + "&limit=1");
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String json = EntityUtils.toString(entity);
                    JsonObject result = gson.fromJson(json, JsonObject.class);
                    
                    if (result.has("data") && result.getAsJsonArray("data").size() > 0) {
                        return true;
                    }
                }
            } catch (ParseException e)
            {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            System.err.println("Error validating anime: " + e.getMessage());
            return false;
        }
        return false;
    }

    public static List<String> searchAnime(String query, int limit) {
        List<String> results = new ArrayList<>();
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String encodedQuery = query.replace(" ", "%20");
            HttpGet request = new HttpGet(JIKAN_API_BASE + "/anime?q=" + encodedQuery + "&limit=" + limit);
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String json = EntityUtils.toString(entity);
                    JsonObject result = gson.fromJson(json, JsonObject.class);
                    
                    if (result.has("data")) {
                        result.getAsJsonArray("data").forEach(element -> {
                            JsonObject anime = element.getAsJsonObject();
                            if (anime.has("title")) {
                                results.add(anime.get("title").getAsString());
                            }
                        });
                    }
                }
            }
        } catch (IOException | ParseException e) {
            System.err.println("Error searching anime: " + e.getMessage());
        }
        
        return results;
    }

    public static String getExactAnimeTitle(String searchQuery) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String encodedQuery = searchQuery.replace(" ", "%20");
            HttpGet request = new HttpGet(JIKAN_API_BASE + "/anime?q=" + encodedQuery + "&limit=5");
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String json = EntityUtils.toString(entity);
                    JsonObject result = gson.fromJson(json, JsonObject.class);
                    
                    if (result.has("data") && result.getAsJsonArray("data").size() > 0) {
                        JsonObject firstResult = result.getAsJsonArray("data").get(0).getAsJsonObject();
                        return firstResult.get("title").getAsString();
                    }
                }
            } catch (ParseException e)
            {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            System.err.println("Error getting exact anime title: " + e.getMessage());
        }
        
        return null;
    }

    public static List<AnimeSearchResult> searchAnimeWithDetails(String query) {
        List<AnimeSearchResult> results = new ArrayList<>();
        
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String encodedQuery = query.replace(" ", "%20");
            HttpGet request = new HttpGet(JIKAN_API_BASE + "/anime?q=" + encodedQuery + "&limit=10");
            
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String json = EntityUtils.toString(entity);
                    JsonObject result = gson.fromJson(json, JsonObject.class);
                    
                    if (result.has("data")) {
                        JsonArray dataArray = result.getAsJsonArray("data");
                        for (JsonElement element : dataArray) {
                            JsonObject anime = element.getAsJsonObject();
                            
                            // Extract basic info
                            int malId = anime.get("mal_id").getAsInt();
                            String title = anime.get("title").getAsString();
                            String titleEnglish = anime.has("title_english") && 
                                    !anime.get("title_english").isJsonNull() ? 
                                    anime.get("title_english").getAsString() : null;
                            String type = anime.has("type") && !anime.get("type").isJsonNull() ? 
                                    anime.get("type").getAsString() : "Unknown";
                            
                            // Extract year from aired field
                            String year = "Unknown";
                            if (anime.has("aired") && !anime.get("aired").isJsonNull()) {
                                JsonObject aired = anime.getAsJsonObject("aired");
                                if (aired.has("from") && !aired.get("from").isJsonNull()) {
                                    String from = aired.get("from").getAsString();
                                    if (from.length() >= 4) {
                                        year = from.substring(0, 4);
                                    }
                                }
                            }
                            
                            // Extract episodes
                            int episodes = anime.has("episodes") && !anime.get("episodes").isJsonNull() ? 
                                    anime.get("episodes").getAsInt() : 0;
                            
                            // Extract score
                            double score = anime.has("score") && !anime.get("score").isJsonNull() ? 
                                    anime.get("score").getAsDouble() : 0.0;
                            
                            // Extract image URL
                            String imageUrl = "";
                            if (anime.has("images") && !anime.get("images").isJsonNull()) {
                                JsonObject images = anime.getAsJsonObject("images");
                                if (images.has("jpg") && !images.get("jpg").isJsonNull()) {
                                    JsonObject jpg = images.getAsJsonObject("jpg");
                                    if (jpg.has("image_url") && !jpg.get("image_url").isJsonNull()) {
                                        imageUrl = jpg.get("image_url").getAsString();
                                    }
                                }
                            }
                            
                            results.add(new AnimeSearchResult(malId, title, titleEnglish, type, 
                                    year, episodes, score, imageUrl));
                        }
                    }
                }
            }
        } catch (IOException | ParseException e) {
            System.err.println("Error searching anime with details: " + e.getMessage());
        }
        
        return results;
    }

    public static boolean isExactMatch(String input, List<AnimeSearchResult> results) {
        if (results.size() != 1) {
            return false;
        }
        
        AnimeSearchResult result = results.get(0);
        double similarity = calculateSimilarity(input.toLowerCase(), result.getTitle().toLowerCase());
        
        // Also check against English title if available
        if (result.getTitleEnglish() != null && !result.getTitleEnglish().isEmpty()) {
            double englishSimilarity = calculateSimilarity(input.toLowerCase(), result.getTitleEnglish().toLowerCase());
            similarity = Math.max(similarity, englishSimilarity);
        }
        
        return similarity >= 0.9;
    }

    private static double calculateSimilarity(String input, String title) {
        // Simple similarity calculation using Levenshtein distance approximation
        // For better accuracy, we could use a proper library like Apache Commons Text
        
        if (input.equals(title)) {
            return 1.0;
        }
        
        if (input.contains(title) || title.contains(input)) {
            return 0.9;
        }
        
        // Calculate word-based similarity
        String[] inputWords = input.split("\\s+");
        String[] titleWords = title.split("\\s+");
        
        int matchingWords = 0;
        for (String inputWord : inputWords) {
            for (String titleWord : titleWords) {
                if (inputWord.equalsIgnoreCase(titleWord)) {
                    matchingWords++;
                    break;
                }
            }
        }
        
        if (titleWords.length == 0) {
            return 0.0;
        }
        
        double wordSimilarity = (double) matchingWords / titleWords.length;
        
        // Factor in length difference
        double lengthFactor = 1.0 - Math.abs(input.length() - title.length()) / (double) Math.max(input.length(), title.length());
        
        return (wordSimilarity * 0.7) + (lengthFactor * 0.3);
    }
}