package net.lecigne.somafm.model;

import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class Song {
  String artist;
  String title;
  String album;
}
