package org.projectATB.service;

import org.projectATB.dto.AnimeDetailsDTO;
import org.projectATB.dto.AnimeSummaryDTO;
import org.projectATB.dto.CharacterDTO;
import org.projectATB.dto.EpisodeDTO;
import org.projectATB.dto.StaffDTO;
import java.util.ArrayList;
import java.util.List;

public class JikanApiFacade {

    public static AnimeDetailsDTO getAnimeDetails(int malId) {
        // To be implemented: fetch and map details using Jikan
        return null;
    }

    public static AnimeSummaryDTO getSequel(int malId) {
        // To be implemented
        return null;
    }

    public static AnimeSummaryDTO getPrequel(int malId) {
        // To be implemented
        return null;
    }

    public static List<CharacterDTO> getCharacters(int malId, int limit) {
        return new ArrayList<>();
    }

    public static List<StaffDTO> getStaff(int malId, int limit) {
        return new ArrayList<>();
    }

    public static List<EpisodeDTO> getEpisodes(int malId) {
        return new ArrayList<>();
    }

    public static String resolveEpisodeUrl(String animeTitle, int episode) {
        return "https://animepahe.si/";
    }

    // Additional helpers for search (placeholders – to be wired to real Jikan calls)
    public static List<AnimeSummaryDTO> searchAnimes(String query, int limit) {
        return new ArrayList<>();
    }

    public static List<CharacterDTO> searchCharacters(String query, int limit) {
        return new ArrayList<>();
    }
}
