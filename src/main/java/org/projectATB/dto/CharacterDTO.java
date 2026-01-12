package org.projectATB.dto;

public class CharacterDTO {
    private int malId;
    private String name;
    private String imageUrl;

    public CharacterDTO(int malId, String name, String imageUrl) {
        this.malId = malId;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public int getMalId() { return malId; }
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
}
