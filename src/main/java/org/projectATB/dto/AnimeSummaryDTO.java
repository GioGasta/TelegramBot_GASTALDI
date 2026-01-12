package org.projectATB.dto;

public class AnimeSummaryDTO {
    private int malId;
    private String title;
    private String imageUrl;

    public AnimeSummaryDTO(int malId, String title, String imageUrl) {
        this.malId = malId;
        this.title = title;
        this.imageUrl = imageUrl;
    }

    public int getMalId() { return malId; }
    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }
}
