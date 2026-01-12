package org.projectATB.dto;

public class EpisodeDTO {
    private int episode;
    private String title;

    public EpisodeDTO(int episode, String title) {
        this.episode = episode;
        this.title = title;
    }

    public int getEpisode() { return episode; }
    public String getTitle() { return title; }
}
