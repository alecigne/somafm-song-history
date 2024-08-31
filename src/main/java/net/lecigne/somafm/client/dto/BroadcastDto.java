package net.lecigne.somafm.client.dto;

import java.time.LocalTime;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class BroadcastDto {
  LocalTime time;
  String artist;
  String title;
  String album;
}
