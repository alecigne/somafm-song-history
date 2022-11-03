package net.lecigne.somafm.client;

import net.lecigne.somafm.config.SomaFmConfig;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Get the most recent broadcasts for a given SomaFM channel in HTML format.
 */
public interface HtmlBroadcastsClient {
  @GET("/recent/{channel}.html")
  Call<ResponseBody> getHtml(@Path("channel") String channel);

  static HtmlBroadcastsClient create(SomaFmConfig config) {
    return new Retrofit.Builder()
        .baseUrl(config.getSomaFmBaseUrl())
        .client(new OkHttpClient.Builder()
            .addInterceptor(new UserAgentInterceptor(config.getUserAgent()))
            .build())
        .build()
        .create(HtmlBroadcastsClient.class);
  }

}
