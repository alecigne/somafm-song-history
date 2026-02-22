package net.lecigne.somafm.history.adapters.in.rest;

import java.util.List;
import net.lecigne.somafm.history.domain.model.Page;
import net.lecigne.somafm.recentlib.Broadcast;

record BroadcastPageResponseDto(
    int page,
    int size,
    long totalElements,
    int totalPages,
    List<BroadcastResponseDto> items
) {

  static BroadcastPageResponseDto from(Page<Broadcast> broadcastPage) {
    List<BroadcastResponseDto> items = broadcastPage.items()
        .stream()
        .map(BroadcastResponseDto::from)
        .toList();
    return new BroadcastPageResponseDto(
        broadcastPage.number(),
        broadcastPage.size(),
        broadcastPage.totalElements(),
        broadcastPage.totalPages(),
        items);
  }

}
