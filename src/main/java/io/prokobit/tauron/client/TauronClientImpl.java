package io.prokobit.tauron.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.prokobit.tauron.client.data.Chart;
import io.prokobit.tauron.client.deserializer.LocalDateDeserializer;
import io.prokobit.tauron.client.exception.ClientException;
import io.prokobit.tauron.client.exception.LimitException;
import io.prokobit.tauron.client.exception.ResponseException;
import io.prokobit.tauron.client.form.DataForm;
import io.prokobit.tauron.client.form.LoginForm;
import io.prokobit.tauron.client.model.SessionContext;
import io.prokobit.tauron.client.model.SessionId;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class TauronClientImpl implements TauronClient {

  static final String LIMIT_EXCEEDED_MESSAGE = "Przekroczono maksymalną ilość logowań na dobę.";

  private static final String COOKIE_HEADER = "Cookie";

  private static final Pattern COUNTER_ID_REGEX =
      Pattern.compile("<input type=\"hidden\" name=\"smart\" id=\"smartNr\" value=\"(.*?)\" \\/>");

  private final Gson parser =
      new GsonBuilder()
          .registerTypeAdapter(LocalDate.class, new LocalDateDeserializer())
          // .registerTypeAdapter(ChartData.class, new ChartDataDeserializer())
          .create();

  private final HttpClient httpClient =
      HttpClient.newBuilder()
          .version(HttpClient.Version.HTTP_2)
          .followRedirects(HttpClient.Redirect.NEVER)
          .connectTimeout(Duration.ofSeconds(30))
          .build();

  private final TauronClient.Config config;

  TauronClientImpl(final TauronClient.Config config) {
    this.config = config;
  }

  @Override
  public Chart getChart(final DataForm params) {

    final HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(config.getChartsUrl()))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header(COOKIE_HEADER, params.getSessionId())
            .POST(params.bodyPublisher())
            .build();

    final HttpResponse<String> response = doRequest(request);

    validateResponse(response);

    return parser.fromJson(response.body(), Chart.class);
  }

  @Override
  public SessionContext login(final String login, final String password) {
    Objects.requireNonNull(login, "Required login");
    Objects.requireNonNull(password, "Required password");

    final LoginForm loginForm = new LoginForm(login, password, config.getServiceUrl());

    final HttpResponse<String> response = doLogin(loginForm);

    final SessionId sessionId =
        findSessionIdHeader(response)
            .orElseThrow(() -> new IllegalStateException("Can not find session Id"));

    final HttpResponse<String> secResponse =
        doRedirect(
            sessionId, response.headers().firstValue("location").orElse(config.getServiceUrl()));

    validateLogin(secResponse);

    final String counterId =
        Optional.of(COUNTER_ID_REGEX.matcher(secResponse.body()))
            .filter(Matcher::find)
            .map(matcher -> matcher.group(1))
            .orElseThrow(() -> new IllegalStateException("Can not find counter Id (smartNr)"));

    return SessionContext.of(sessionId, counterId);
  }

  private HttpResponse<String> doLogin(final LoginForm loginForm) {
    final HttpRequest request =
        HttpRequest.newBuilder()
            .uri(URI.create(String.format(config.getLoginUrl(), loginForm.getService())))
            .header("Content-Type", "application/x-www-form-urlencoded")
            .header(COOKIE_HEADER, SessionId.SESSION_ID_HEADER)
            .POST(loginForm.bodyPublisher())
            .build();

    return doRequest(request);
  }

  private HttpResponse<String> doRedirect(final SessionId sessionIdHeader, final String location) {
    final HttpRequest requestGet =
        HttpRequest.newBuilder()
            .uri(URI.create(location))
            .header(COOKIE_HEADER, sessionIdHeader.getValue())
            .GET()
            .build();

    return doRequest(requestGet);
  }

  private HttpResponse<String> doRequest(final HttpRequest request) {
    try {
      return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (IOException e) {
      throw new ClientException(e.getMessage(), e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ClientException(e.getMessage(), e);
    }
  }

  private Optional<SessionId> findSessionIdHeader(final HttpResponse<String> response) {
    return response.headers().allValues("set-cookie").stream()
        .filter(SessionId::isSessionIdHeader)
        .map(SessionId::of)
        .findFirst();
  }

  private void validateLogin(final HttpResponse<String> response) {
    validateResponse(response);

    if (response.body().contains(LIMIT_EXCEEDED_MESSAGE))
      throw new LimitException("Login limit exceeded");
  }

  private void validateResponse(final HttpResponse<?> response) {
    if (response.statusCode() != 200)
      throw new ResponseException("Response is not success", response.statusCode());
  }
}
