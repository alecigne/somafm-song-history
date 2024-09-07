package net.lecigne.somafm.client;

import java.io.IOException;
import lombok.RequiredArgsConstructor;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

@RequiredArgsConstructor
public class UserAgentInterceptor implements Interceptor {

  private final String userAgent;

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request requestWithUserAgent = chain.request().newBuilder()
        .header("User-Agent", userAgent)
        .build();
    return chain.proceed(requestWithUserAgent);
  }

}
