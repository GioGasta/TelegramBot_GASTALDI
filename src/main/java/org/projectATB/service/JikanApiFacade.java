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
import org.projectATB.dto.AnimeDetailsDTO;
import org.projectATB.dto.AnimeSummaryDTO;
import org.projectATB.dto.CharacterDTO;
import org.projectATB.dto.EpisodeDTO;
import org.projectATB.dto.StaffDTO;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class JikanApiFacade {

    private static final String JIKAN_API_BASE = "https://api.jikan.moe/v4";
    private static final Gson gson = new Gson();

    public static AnimeDetailsDTO getAnimeDetails(int malId) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(JIKAN_API_BASE + "/anime/" + malId);
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String json = EntityUtils.toString(entity);
                    JsonObject root = gson.fromJson(json, JsonObject.class);
                    if (root.has("data")) {
                        JsonObject data = root.getAsJsonObject("data");
                        int id = malId;
                        String title = data.has("title") ? data.get("title").getAsString() : "";
                        String imageUrl = "";
                        if (data.has("images")) {
                            JsonObject images = data.getAsJsonObject("images");
                            if (images.has("jpg")) {
                                JsonObject jpg = images.getAsJsonObject("jpg");
                                if (jpg.has("image_url")) imageUrl = jpg.get("image_url").getAsString();
                            }
                        }
                        String duration = data.has("duration") && !data.get("duration").isJsonNull() ? data.get("duration").getAsString() : "";
                        String season = data.has("season") && !data.get("season").isJsonNull() ? data.get("season").getAsString() : "";
                        String year = "";
                        if (data.has("aired") && data.getAsJsonObject("aired").has("from")) {
                            String from = data.getAsJsonObject("aired").get("from").getAsString();
                            if (from != null && from.length() >= 4) year = from.substring(0, 4);
                        }
                        String releaseDate = data.has("aired") && data.getAsJsonObject("aired").has("from")
                                ? data.getAsJsonObject("aired").get("from").getAsString() : "";
                        List<String> genres = new ArrayList<>();
                        if (data.has("genres")) {
                            JsonArray g = data.getAsJsonArray("genres");
                            for (JsonElement e : g) {
                                JsonObject jo = e.getAsJsonObject();
                                if (jo.has("name")) genres.add(jo.get("name").getAsString());
                            }
                        }
                        List<String> studios = new ArrayList<>();
                        if (data.has("studios")) {
                            JsonArray s = data.getAsJsonArray("studios");
                            for (JsonElement e : s) {
                                JsonObject so = e.getAsJsonObject();
                                if (so.has("name")) studios.add(so.get("name").getAsString());
                            }
                        }
                        List<String> authors = new ArrayList<>();
                        if (data.has("producers")) {
                            JsonArray a = data.getAsJsonArray("producers");
                            for (JsonElement e : a) {
                                JsonObject ao = e.getAsJsonObject();
                                if (ao.has("name")) authors.add(ao.get("name").getAsString());
                            }
                        }
                        String synopsis = data.has("synopsis") && !data.get("synopsis").isJsonNull()
                                ? data.get("synopsis").getAsString() : "";
                        return new AnimeDetailsDTO(id, title, imageUrl, duration, season, year, releaseDate, genres, synopsis, studios, authors);
                    }
                }
            }
        } catch (IOException | ParseException e) {
            System.err.println("Error fetching anime details: " + e.getMessage());
        }
        return null;
    }

    public static AnimeSummaryDTO getSequel(int malId) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(JIKAN_API_BASE + "/anime/" + malId + "/relations");
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String json = EntityUtils.toString(entity);
                    JsonObject root = gson.fromJson(json, JsonObject.class);
                    if (root.has("data")) {
                        JsonArray relations = root.getAsJsonArray("data");
                        for (JsonElement el : relations) {
                            JsonObject relation = el.getAsJsonObject();
                            String relationType = relation.has("relation") ? relation.get("relation").getAsString() : "";
                            if (relationType.equalsIgnoreCase("Sequel") && relation.has("entry")) {
                                JsonArray entries = relation.getAsJsonArray("entry");
                                if (entries.size() > 0) {
                                    JsonObject first = entries.get(0).getAsJsonObject();
                                    int id = first.has("mal_id") ? first.get("mal_id").getAsInt() : 0;
                                    String t = first.has("name") ? first.get("name").getAsString() : "";
                                    String img = "";
                                    if (first.has("images")) {
                                        JsonObject imgs = first.getAsJsonObject("images");
                                        if (imgs.has("jpg") && imgs.getAsJsonObject("jpg").has("image_url")) {
                                            img = imgs.getAsJsonObject("jpg").get("image_url").getAsString();
                                        }
                                    }
                                    return new AnimeSummaryDTO(id, t, img);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException | ParseException e) {
            System.err.println("Error fetching sequel: " + e.getMessage());
        }
        return null;
    }

    public static AnimeSummaryDTO getPrequel(int malId) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(JIKAN_API_BASE + "/anime/" + malId + "/relations");
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String json = EntityUtils.toString(entity);
                    JsonObject root = gson.fromJson(json, JsonObject.class);
                    if (root.has("data")) {
                        JsonArray relations = root.getAsJsonArray("data");
                        for (JsonElement el : relations) {
                            JsonObject relation = el.getAsJsonObject();
                            String relationType = relation.has("relation") ? relation.get("relation").getAsString() : "";
                            if (relationType.equalsIgnoreCase("Prequel") && relation.has("entry")) {
                                JsonArray entries = relation.getAsJsonArray("entry");
                                if (entries.size() > 0) {
                                    JsonObject first = entries.get(0).getAsJsonObject();
                                    int id = first.has("mal_id") ? first.get("mal_id").getAsInt() : 0;
                                    String t = first.has("name") ? first.get("name").getAsString() : "";
                                    String img = "";
                                    if (first.has("images")) {
                                        JsonObject imgs = first.getAsJsonObject("images");
                                        if (imgs.has("jpg") && imgs.getAsJsonObject("jpg").has("image_url")) {
                                            img = imgs.getAsJsonObject("jpg").get("image_url").getAsString();
                                        }
                                    }
                                    return new AnimeSummaryDTO(id, t, img);
                                }
                            }
                        }
                    }
                }
            }
        } catch (IOException | ParseException e) {
            System.err.println("Error fetching prequel: " + e.getMessage());
        }
        return null;
    }

    public static List<CharacterDTO> getCharacters(int malId, int limit) {
        List<CharacterDTO> chars = new ArrayList<>();
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(JIKAN_API_BASE + "/anime/" + malId + "/characters");
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String json = EntityUtils.toString(entity);
                    JsonObject root = gson.fromJson(json, JsonObject.class);
                    if (root.has("data")) {
                        JsonArray arr = root.getAsJsonArray("data");
                        for (int i = 0; i < Math.min(limit, arr.size()); i++) {
                            JsonObject item = arr.get(i).getAsJsonObject();
                            JsonObject ch = item.getAsJsonObject("character");
                            int id = ch.has("mal_id") ? ch.get("mal_id").getAsInt() : 0;
                            String name = ch.has("name") ? ch.get("name").getAsString() : "";
                            String image = "";
                            if (ch.has("images")) {
                                JsonObject imgs = ch.getAsJsonObject("images");
                                if (imgs.has("jpg") && imgs.getAsJsonObject("jpg").has("image_url")) {
                                    image = imgs.getAsJsonObject("jpg").get("image_url").getAsString();
                                }
                            }
                            chars.add(new CharacterDTO(id, name, image));
                        }
                    }
                }
            }
        } catch (IOException | ParseException e) {
            System.err.println("Error fetching characters: " + e.getMessage());
        }
        return chars;
    }

    public static List<StaffDTO> getStaff(int malId, int limit) {
        List<StaffDTO> staff = new ArrayList<>();
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(JIKAN_API_BASE + "/anime/" + malId + "/staff");
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String json = EntityUtils.toString(entity);
                    JsonObject root = gson.fromJson(json, JsonObject.class);
                    if (root.has("data")) {
                        JsonArray arr = root.getAsJsonArray("data");
                        for (int i = 0; i < Math.min(limit, arr.size()); i++) {
                            JsonObject item = arr.get(i).getAsJsonObject();
                            JsonObject person = item.getAsJsonObject("person");
                            int mid = person.has("mal_id") ? person.get("mal_id").getAsInt() : 0;
                            String name = person.has("name") ? person.get("name").getAsString() : "";
                            String image = "";
                            if (person.has("images")) {
                                JsonObject imgs = person.getAsJsonObject("images");
                                if (imgs.has("jpg") && imgs.getAsJsonObject("jpg").has("image_url")) {
                                    image = imgs.getAsJsonObject("jpg").get("image_url").getAsString();
                                }
                            }
                            staff.add(new StaffDTO(mid, name, image));
                        }
                    }
                }
            }
        } catch (IOException | ParseException e) {
            System.err.println("Error fetching staff: " + e.getMessage());
        }
        return staff;
    }

    public static List<EpisodeDTO> getEpisodes(int malId) {
        List<EpisodeDTO> eps = new ArrayList<>();
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(JIKAN_API_BASE + "/anime/" + malId + "/episodes");
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String json = EntityUtils.toString(entity);
                    JsonObject root = gson.fromJson(json, JsonObject.class);
                    if (root.has("data")) {
                        JsonArray arr = root.getAsJsonArray("data");
                        for (JsonElement e : arr) {
                            JsonObject item = e.getAsJsonObject();
                            int num = item.has("episode") ? item.get("episode").getAsInt() : 0;
                            String title = item.has("title") ? item.get("title").getAsString() : "";
                            eps.add(new EpisodeDTO(num, title));
                        }
                    }
                }
            }
        } catch (IOException | ParseException e) {
            System.err.println("Error fetching episodes: " + e.getMessage());
        }
        return eps;
    }

    public static String resolveEpisodeUrl(String animeTitle, int episode) {
        try {
            String query = URLEncoder.encode(animeTitle + " episode " + episode, StandardCharsets.UTF_8);
            return "https://animepahe.si/search?q=" + query;
        } catch (Exception e) {
            System.err.println("Error encoding episode URL: " + e.getMessage());
            return "https://animepahe.si/";
        }
    }

    public static AnimeDetailsDTO searchAnimeByTitle(String title) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            String encodedQuery = URLEncoder.encode(title, StandardCharsets.UTF_8);
            HttpGet request = new HttpGet(JIKAN_API_BASE + "/anime?q=" + encodedQuery + "&limit=1");
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String json = EntityUtils.toString(entity);
                    JsonObject root = gson.fromJson(json, JsonObject.class);
                    if (root.has("data") && root.getAsJsonArray("data").size() > 0) {
                        JsonObject firstResult = root.getAsJsonArray("data").get(0).getAsJsonObject();
                        int malId = firstResult.get("mal_id").getAsInt();
                        return getAnimeDetails(malId);
                    }
                }
            }
        } catch (IOException | ParseException e) {
            System.err.println("Error searching anime by title: " + e.getMessage());
        }
        return null;
    }
}
