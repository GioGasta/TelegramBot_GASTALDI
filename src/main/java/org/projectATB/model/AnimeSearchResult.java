package org.projectATB.model;

public class AnimeSearchResult {
    private int malId;
    private String title;
    private String titleEnglish;
    private String type;
    private String year;
    private int episodes;
    private double score;
    private String imageUrl;

    public AnimeSearchResult(int malId, String title, String titleEnglish, String type, 
                           String year, int episodes, double score, String imageUrl) {
        this.malId = malId;
        this.title = title;
        this.titleEnglish = titleEnglish;
        this.type = type;
        this.year = year;
        this.episodes = episodes;
        this.score = score;
        this.imageUrl = imageUrl;
    }

    // Getters
    public int getMalId() {
        return malId;
    }

    public String getTitle() {
        return title;
    }

    public String getTitleEnglish() {
        return titleEnglish;
    }

    public String getType() {
        return type;
    }

    public String getYear() {
        return year;
    }

    public int getEpisodes() {
        return episodes;
    }

    public double getScore() {
        return score;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getDisplayTitle() {
        return titleEnglish != null && !titleEnglish.isEmpty() ? titleEnglish : title;
    }

    @Override
    public String toString() {
        return "AnimeSearchResult{" +
                "malId=" + malId +
                ", title='" + title + '\'' +
                ", titleEnglish='" + titleEnglish + '\'' +
                ", type='" + type + '\'' +
                ", year='" + year + '\'' +
                ", episodes=" + episodes +
                ", score=" + score +
                '}';
    }
}