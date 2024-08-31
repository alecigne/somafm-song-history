package net.lecigne.somafm.client.dto;

import java.util.List;
import lombok.Builder;
import lombok.Value;
import net.lecigne.somafm.model.Channel;

@Builder
@Value
public class RecentBroadcastsDto {
  Channel channel;
  List<BroadcastDto> recentBroadcasts;
}
