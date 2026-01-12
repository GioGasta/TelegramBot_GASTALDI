package org.projectATB.dto;

import java.util.List;

public class AnimeDetailsDTO {
    private int malId;
    private String title;
    private String imageUrl;
    private String duration;
    private String season;
    private String year;
    private String releaseDate;
    private List<String> genres;
    private String synopsis;
    private List<String> studios;
    private List<String> authors;

    public AnimeDetailsDTO(int malId, String title, String imageUrl, String duration, String season,
                           String year, String releaseDate, List<String> genres, String synopsis,
                           List<String> studios, List<String> authors) {
        this.malId = malId;
        this.title = title;
        this.imageUrl = imageUrl;
        this.duration = duration;
        this.season = season;
        this.year = year;
        this.releaseDate = releaseDate;
        this.genres = genres;
        this.synopsis = synopsis;
        this.studios = studios;
        this.authors = authors;
    }

    public int getMalId() { return malId; }
    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }
    public String getDuration() { return duration; }
    public String getSeason() { return season; }
    public String getYear() { return year; }
    public String getReleaseDate() { return releaseDate; }
    public List<String> getGenres() { return genres; }
    public String getSynopsis() { return synopsis; }
    public List<String> getStudios() { return studios; }
    public List<String> getAuthors() { return authors; }
}
