package org.projectATB.dto;

public class StaffDTO {
    private int malId;
    private String name;
    private String imageUrl;

    public StaffDTO(int malId, String name, String imageUrl) {
        this.malId = malId;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public int getMalId() { return malId; }
    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
}
