package net.lecigne.somafm.history.adapters.in.rest;

import java.util.List;
import net.lecigne.somafm.history.domain.model.Page;
import net.lecigne.somafm.recentlib.Broadcast;

record BroadcastPageDto(
    int page,
    int size,
    long totalElements,
    int totalPages,
    List<BroadcastDto> items
) {

  static BroadcastPageDto from(Page<Broadcast> broadcastPage) {
    List<BroadcastDto> items = broadcastPage.items().stream().map(BroadcastDto::from).toList();
    return new BroadcastPageDto(
        broadcastPage.number(),
        broadcastPage.size(),
        broadcastPage.totalElements(),
        broadcastPage.totalPages(),
        items);
  }

}
