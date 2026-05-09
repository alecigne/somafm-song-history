package net.lecigne.somafm.history.domain.model;

import java.util.List;

public record SongDetails(Song song, List<SongBroadcast> broadcasts) {
}
