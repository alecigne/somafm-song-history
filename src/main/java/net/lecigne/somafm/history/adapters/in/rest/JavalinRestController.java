package net.lecigne.somafm.history.adapters.in.rest;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import java.util.List;
import net.lecigne.somafm.history.application.ports.in.FetchRecentBroadcastsUseCase;
import net.lecigne.somafm.history.application.ports.in.GetBroadcastsUseCase;
import net.lecigne.somafm.history.application.ports.in.GetSongsUseCase;
import net.lecigne.somafm.history.domain.model.Page;
import net.lecigne.somafm.recentlib.Broadcast;
import net.lecigne.somafm.recentlib.Channel;
import net.lecigne.somafm.recentlib.PredefinedChannel;
import net.lecigne.somafm.recentlib.Song;

public class JavalinRestController {

  private static final int DEFAULT_PAGE = 1;
  private static final int DEFAULT_SIZE = 50;

  private final GetBroadcastsUseCase getBroadcastsUseCase;
  private final GetSongsUseCase getSongsUseCase;
  private final FetchRecentBroadcastsUseCase fetchRecentBroadcastsUseCase;

  JavalinRestController(
      GetBroadcastsUseCase getBroadcastsUseCase,
      GetSongsUseCase getSongsUseCase,
      FetchRecentBroadcastsUseCase fetchRecentBroadcastsUseCase
  ) {
    this.getBroadcastsUseCase = getBroadcastsUseCase;
    this.getSongsUseCase = getSongsUseCase;
    this.fetchRecentBroadcastsUseCase = fetchRecentBroadcastsUseCase;
  }

  private void getBroadcasts(Context ctx) {
    int page = parseQueryInt(ctx, "page", DEFAULT_PAGE);
    int size = parseQueryInt(ctx, "size", DEFAULT_SIZE);
    try {
      Page<Broadcast> broadcastsPage = getBroadcastsUseCase.getBroadcasts(page, size);
      ctx.json(BroadcastPageResponseDto.from(broadcastsPage));
    } catch (IllegalArgumentException e) {
      throw new BadRequestResponse(e.getMessage());
    }
  }

  private void getSongs(Context ctx) {
    int page = parseQueryInt(ctx, "page", DEFAULT_PAGE);
    int size = parseQueryInt(ctx, "size", DEFAULT_SIZE);
    try {
      Page<Song> songsPage = getSongsUseCase.getSongs(page, size);
      ctx.json(SongPageResponseDto.from(songsPage));
    } catch (IllegalArgumentException e) {
      throw new BadRequestResponse(e.getMessage());
    }
  }

  private void fetchRecentBroadcasts(Context ctx) {
    String channelAsString = ctx.queryParam("channel");
    try {
      Channel channel = PredefinedChannel
          .getByInternalName(channelAsString)
          .orElseThrow(IllegalArgumentException::new);
      List<Broadcast> broadcasts = fetchRecentBroadcastsUseCase.fetchRecent(channel);
      ctx.json(broadcasts.stream().map(BroadcastResponseDto::from).toList());
    } catch (IllegalArgumentException e) {
      throw new BadRequestResponse(e.getMessage());
    }
  }

  private static int parseQueryInt(Context ctx, String name, int defaultValue) {
    String value = ctx.queryParam(name);
    if (value == null) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      throw new BadRequestResponse("Query parameter '" + name + "' must be an integer");
    }
  }

  public EndpointGroup routes() {
    return () -> {
      path("broadcasts", () -> {
        get(this::getBroadcasts);
        get("recent", this::fetchRecentBroadcasts);
      });
      path("songs", () -> get(this::getSongs));
    };
  }

  public static JavalinRestController init(
      GetBroadcastsUseCase getBroadcastsUseCase, GetSongsUseCase getSongsUseCase,
      FetchRecentBroadcastsUseCase fetchRecentBroadcastsUseCase) {
    return new JavalinRestController(getBroadcastsUseCase, getSongsUseCase, fetchRecentBroadcastsUseCase);
  }

}
