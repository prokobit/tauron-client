package io.prokobit.tauron.client;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.givenThat;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import io.prokobit.tauron.client.data.Chart;
import io.prokobit.tauron.client.data.ChartValue;
import io.prokobit.tauron.client.exception.ClientException;
import io.prokobit.tauron.client.exception.LimitException;
import io.prokobit.tauron.client.exception.ResponseException;
import io.prokobit.tauron.client.form.DataForm;
import io.prokobit.tauron.client.form.Sampling;
import io.prokobit.tauron.client.model.SessionContext;
import io.prokobit.tauron.client.model.SessionId;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@WireMockTest
class TauronClientTest {

  @RegisterExtension
  private static final WireMockExtension wm =
      WireMockExtension.newInstance()
          .options(wireMockConfig().dynamicPort())
          .configureStaticDsl(true)
          .build();

  private static final SessionId SESSION_ID =
      SessionId.of(SessionId.SESSION_ID_HEADER + "12345678");

  private static final SessionContext SESSION_CONTEXT = SessionContext.of(SESSION_ID, "87654321");
  private final String BASE_TEST_URL = String.format("http://localhost:%s", wm.getPort());

  private final TauronClient.Config config =
      new TauronClient.Config(
          BASE_TEST_URL, BASE_TEST_URL + "/index/charts", BASE_TEST_URL + "/login");
  private final TauronClient underTest = new TauronClientImpl(config);

  @Test
  void shouldInitDefaultClient() {
    // expects
    Assertions.assertNotNull(TauronClient.create());
  }

  @Test
  void shouldReturnClientException() {
    // given
    var client =
        new TauronClientImpl(
            new TauronClient.Config("http://unknown", "http://unknown", "http://unknown"));
    // expects
    Assertions.assertThrows(ClientException.class, () -> client.login("test", "pass"));
  }

  @Test
  void shouldReturnSessionContextAfterLogin() {

    // given
    givenThat(
        post(urlEqualTo("/login"))
            .withHeader("Cookie", equalTo(SessionId.SESSION_ID_HEADER))
            .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
            .withRequestBody(
                equalTo(
                    "username=test&password=pass&service="
                        + URLEncoder.encode(config.getServiceUrl(), StandardCharsets.UTF_8)))
            .willReturn(ok().withHeader("set-cookie", SESSION_ID.getValue())));

    givenThat(
        get(urlEqualTo("/"))
            .withHeader("Cookie", equalTo(SESSION_ID.getValue()))
            .willReturn(ok().withBodyFile("index.html")));

    // when
    final SessionContext result = underTest.login("test", "pass");

    // then
    Assertions.assertNotNull(result);
    Assertions.assertEquals(SESSION_ID, result.getSessionId());
    Assertions.assertEquals(SESSION_CONTEXT.getCounterId(), result.getCounterId());
  }

  @Test
  void shouldReturnExceptionIfLoginLimitExceeded() {
    // given
    givenThat(
        post(urlEqualTo("/login"))
            .withHeader("Cookie", equalTo(SessionId.SESSION_ID_HEADER))
            .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
            .withRequestBody(
                equalTo(
                    "username=test&password=pass&service="
                        + URLEncoder.encode(config.getServiceUrl(), StandardCharsets.UTF_8)))
            .willReturn(ok().withHeader("set-cookie", SESSION_ID.getValue())));

    givenThat(
        get(urlEqualTo("/"))
            .withHeader("Cookie", equalTo(SESSION_ID.getValue()))
            .willReturn(ok().withBody(TauronClientImpl.LIMIT_EXCEEDED_MESSAGE)));

    // expects
    LimitException exception =
        Assertions.assertThrows(LimitException.class, () -> underTest.login("test", "pass"));

    Assertions.assertEquals("Login limit exceeded", exception.getMessage());
  }

  @Test
  void shouldReturnExceptionIfCanNotFindSessionId() {
    // given
    givenThat(
        post(urlEqualTo("/login"))
            .withHeader("Cookie", equalTo(SessionId.SESSION_ID_HEADER))
            .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
            .withRequestBody(
                equalTo(
                    "username=test&password=pass&service="
                        + URLEncoder.encode(config.getServiceUrl(), StandardCharsets.UTF_8)))
            .willReturn(ok()));

    // expects
    RuntimeException exception =
        Assertions.assertThrows(
            RuntimeException.class,
            () -> {
              underTest.login("test", "pass");
            });

    Assertions.assertEquals("Can not find session Id", exception.getMessage());
  }

  @Test
  void shouldReturnExceptionForNonSuccessResponse() {
    // given
    final DataForm params = DataForm.ofDay(SESSION_CONTEXT, LocalDate.of(2022, 3, 28));

    // expects
    ResponseException exception =
        Assertions.assertThrows(
            ResponseException.class,
            () -> {
              underTest.getChart(params);
            });

    Assertions.assertEquals(404, exception.getStatusCode());
  }

  @Test
  void shouldReturnDataForDay() {

    // given
    mockChartResponse("day.json");

    final DataForm params = DataForm.ofDay(SESSION_CONTEXT, LocalDate.of(2022, 3, 28));

    // when
    final Chart result = underTest.getChart(params);

    // then
    assertChart(result);
    Assertions.assertTrue(result.isFull());
  }

  @Test
  void shouldReturnDataForMonth() {

    // given
    mockChartResponse("month.json");

    final DataForm params = DataForm.ofMonth(SESSION_CONTEXT, YearMonth.of(2022, Month.MARCH));

    // when
    final Chart result = underTest.getChart(params);

    // then
    assertChart(result);
  }

  @Test
  void shouldReturnDataForYear() {

    // given
    mockChartResponse("year.json");

    final DataForm params = DataForm.ofYear(SESSION_CONTEXT, Year.of(2022));

    // when
    final Chart result = underTest.getChart(params);

    // then
    assertChart(result);
  }

  @Test
  void shouldReturnDataForCustomRange() {

    // given
    mockChartResponse("other.json");

    final DataForm params =
        DataForm.ofPeriod(SESSION_CONTEXT, LocalDate.of(2022, 4, 4), LocalDate.of(2022, 4, 10));

    // when
    final Chart result = underTest.getChart(params);

    // then
    assertChart(result, true);
  }

  @Test
  void shouldReturnCsvSourcePerDay() {

    // given
    mockChartResponse("csv_days.json");

    final DataForm params =
        DataForm.ofPeriod(
            SESSION_CONTEXT, LocalDate.of(2022, 3, 1), LocalDate.of(2022, 3, 31), Sampling.DAILY);

    // when
    final Chart result = underTest.getChart(params);

    // then
    assertChart(result, false, true);
  }

  @Test
  void shouldReturnCsvSourcePerHour() {

    // given
    mockChartResponse("csv_hours.json");

    final DataForm params =
        DataForm.ofPeriod(
            SESSION_CONTEXT, LocalDate.of(2022, 3, 1), LocalDate.of(2022, 3, 31), Sampling.HOURLY);

    // when
    final Chart result = underTest.getChart(params);

    // then
    assertChart(result, true, true);
  }

  private void mockChartResponse(final String responseBodyFile) {
    givenThat(
        post(urlEqualTo("/index/charts"))
            .withHeader("Cookie", equalTo(SESSION_ID.getValue()))
            .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded"))
            // .withRequestBody(equalToJson("{ \"total_results\": 4 }"))
            .willReturn(ok().withBodyFile(responseBodyFile)));
  }

  private void assertChart(final Chart result) {
    assertChart(result, false);
  }

  private void assertChart(final Chart result, boolean withHours) {
    assertChart(result, withHours, false);
  }

  private void assertChart(final Chart result, boolean withHours, boolean shortData) {
    Assertions.assertNotNull(result);

    if (!shortData) {
      Assertions.assertNotNull(result.getSumConsumption());
      Assertions.assertNotNull(result.getSumProduction());
      Assertions.assertNotNull(result.getAvgConsumption());
      Assertions.assertNotNull(result.getAvgProduction());
    }

    List<ChartValue> consumption = result.getConsumptions();
    Assertions.assertNotNull(consumption);
    Assertions.assertFalse(consumption.isEmpty());
    Assertions.assertNotNull(consumption.get(0).getDate());
    Assertions.assertNotNull(consumption.get(0).getSum());

    List<ChartValue> production = result.getProductions();
    Assertions.assertNotNull(production);
    Assertions.assertFalse(production.isEmpty());
    Assertions.assertNotNull(production.get(0).getDate());
    Assertions.assertNotNull(production.get(0).getSum());

    if (withHours) {
      Assertions.assertNotNull(result.getProductions().get(0).getHour());
      Assertions.assertNotNull(result.getConsumptions().get(0).getHour());
    }
  }
}
