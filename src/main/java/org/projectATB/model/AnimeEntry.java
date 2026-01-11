package org.projectATB.model;

public class AnimeEntry
{
    private String name;
    private boolean watched;

    public AnimeEntry(String name, boolean watched) {
        this.name = name;
        this.watched = watched;
    }

    public String getName() {
        return name;
    }

    public boolean isWatched() {
        return watched;
    }
}
