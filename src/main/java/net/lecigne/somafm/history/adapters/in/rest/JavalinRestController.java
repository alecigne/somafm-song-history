package net.lecigne.somafm.history.adapters.in.rest;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.path;

import io.javalin.Javalin;
import io.javalin.apibuilder.EndpointGroup;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import java.util.List;
import net.lecigne.somafm.history.application.ports.in.FetchRecentBroadcastsUseCase;
import net.lecigne.somafm.history.application.ports.in.GetBroadcastsUseCase;
import net.lecigne.somafm.history.domain.model.Page;
import net.lecigne.somafm.recentlib.Broadcast;
import net.lecigne.somafm.recentlib.Channel;
import net.lecigne.somafm.recentlib.PredefinedChannel;

public class JavalinRestController {

  private static final int DEFAULT_PAGE = 1;
  private static final int DEFAULT_SIZE = 50;

  private final GetBroadcastsUseCase getBroadcastsUseCase;
  private final FetchRecentBroadcastsUseCase fetchRecentBroadcastsUseCase;

  JavalinRestController(
      GetBroadcastsUseCase getBroadcastsUseCase,
      FetchRecentBroadcastsUseCase fetchRecentBroadcastsUseCase
  ) {
    this.getBroadcastsUseCase = getBroadcastsUseCase;
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

  private void fetchRecentBroadcasts(Context ctx) {
    String channelAsString = ctx.queryParam("channel");
    Channel channel = PredefinedChannel.getByInternalName(channelAsString).orElseThrow();
    try {
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

  public void registerRoutes(Javalin app) {
    app.unsafeConfig().router.apiBuilder(routes());
  }

  public EndpointGroup routes() {
    return () -> path("broadcasts", () -> {
      get(this::getBroadcasts);
      get("recent", this::fetchRecentBroadcasts);
    });
  }

  public static JavalinRestController init(
      GetBroadcastsUseCase getBroadcastsUseCase,
      FetchRecentBroadcastsUseCase fetchRecentBroadcastsUseCase) {
    return new JavalinRestController(getBroadcastsUseCase, fetchRecentBroadcastsUseCase);
  }

}
