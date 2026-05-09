package net.lecigne.somafm.history.domain.model;

import lombok.Builder;

@Builder
public record Song(Long id, String artist, String title, String album) {
}
