package net.lecigne.somafm.client.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Builder
@Value
public class RecentBroadcastsDto {
  String channel;
  List<BroadcastDto> recentBroadcasts;
}
