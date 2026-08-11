package net.lecigne.somafm.history.adapters.in.rest;

import static io.javalin.apibuilder.ApiBuilder.after;
import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;
import static io.javalin.apibuilder.ApiBuilder.before;

import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import java.util.List;
import net.lecigne.somafm.history.application.model.Page;
import net.lecigne.somafm.history.application.ports.in.FetchRecentBroadcastsUseCase;
import net.lecigne.somafm.history.application.ports.in.GetBroadcastsUseCase;
import net.lecigne.somafm.history.application.ports.in.GetSongDetailsUseCase;
import net.lecigne.somafm.history.application.ports.in.GetSongsUseCase;
import net.lecigne.somafm.history.domain.model.Broadcast;
import net.lecigne.somafm.history.domain.model.Song;
import net.lecigne.somafm.history.domain.model.SongDetails;
import net.lecigne.somafm.recentlib.Channel;
import net.lecigne.somafm.recentlib.PredefinedChannel;

public class JavalinRestController {

  private static final int DEFAULT_PAGE = 1;
  private static final int DEFAULT_SIZE = 50;

  private final GetBroadcastsUseCase getBroadcastsUseCase;
  private final GetSongsUseCase getSongsUseCase;
  private final GetSongDetailsUseCase getSongDetailsUseCase;
  private final FetchRecentBroadcastsUseCase fetchRecentBroadcastsUseCase;

  JavalinRestController(
      GetBroadcastsUseCase getBroadcastsUseCase,
      GetSongsUseCase getSongsUseCase,
      GetSongDetailsUseCase getSongDetailsUseCase,
      FetchRecentBroadcastsUseCase fetchRecentBroadcastsUseCase
  ) {
    this.getBroadcastsUseCase = getBroadcastsUseCase;
    this.getSongsUseCase = getSongsUseCase;
    this.getSongDetailsUseCase = getSongDetailsUseCase;
    this.fetchRecentBroadcastsUseCase = fetchRecentBroadcastsUseCase;
  }

  private void getBroadcasts(Context ctx) {
    int page = parseQueryInt(ctx, "page", DEFAULT_PAGE);
    int size = parseQueryInt(ctx, "size", DEFAULT_SIZE);
    try {
      Page<Broadcast> broadcastsPage = getBroadcastsUseCase.getBroadcasts(page, size);
      ctx.json(BroadcastPageDto.from(broadcastsPage));
    } catch (IllegalArgumentException e) {
      throw new BadRequestResponse(e.getMessage());
    }
  }

  private void getSongs(Context ctx) {
    int page = parseQueryInt(ctx, "page", DEFAULT_PAGE);
    int size = parseQueryInt(ctx, "size", DEFAULT_SIZE);
    try {
      Page<Song> songsPage = getSongsUseCase.getSongs(page, size);
      ctx.json(SongPageDto.from(songsPage));
    } catch (IllegalArgumentException e) {
      throw new BadRequestResponse(e.getMessage());
    }
  }

  private void getSongDetails(Context ctx) {
    long id = parseId(ctx, "id");
    SongDetails songDetails = getSongDetailsUseCase
        .getSongDetails(id)
        .orElseThrow(() -> new NotFoundResponse("Song with ID #" + id + " not found"));
    ctx.json(SongDetailsDto.from(songDetails));
  }

  private void fetchRecentBroadcasts(Context ctx) {
    String channelAsString = ctx.queryParam("channel");
    try {
      Channel channel = PredefinedChannel
          .getByInternalName(channelAsString)
          .orElseThrow(IllegalArgumentException::new);
      List<Broadcast> broadcasts = fetchRecentBroadcastsUseCase.fetchRecent(channel);
      ctx.json(broadcasts.stream().map(BroadcastDto::from).toList());
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

  @SuppressWarnings("SameParameterValue") // name must stay dynamic
  private static long parseId(Context ctx, String name) {
    String value = ctx.pathParam(name);
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException e) {
      throw new BadRequestResponse("Path parameter '" + name + "' must be an integer");
    }
  }

  public EndpointGroup routes() {
    return () -> {
      before(HttpRequestLogging::start);
      after(HttpRequestLogging::finish);
      path("broadcasts", () -> {
        get(this::getBroadcasts);
        get("recent", this::fetchRecentBroadcasts);
      });
      path("songs", () -> {
        get(this::getSongs);
        get("{id}", this::getSongDetails);
      });
    };
  }

  public static JavalinRestController init(
      GetBroadcastsUseCase getBroadcastsUseCase,
      GetSongsUseCase getSongsUseCase,
      GetSongDetailsUseCase getSongDetailsUseCase,
      FetchRecentBroadcastsUseCase fetchRecentBroadcastsUseCase) {
    return new JavalinRestController(getBroadcastsUseCase, getSongsUseCase, getSongDetailsUseCase, fetchRecentBroadcastsUseCase);
  }

}
