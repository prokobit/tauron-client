package io.prokobit.tauron.client;

import io.prokobit.tauron.client.data.Chart;
import io.prokobit.tauron.client.data.ChartValue;
import io.prokobit.tauron.client.form.DataForm;
import io.prokobit.tauron.client.form.Sampling;
import io.prokobit.tauron.client.model.SessionContext;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TauronClientIT {
  private static final String LOGIN = System.getProperty("login");
  private static final String PASSWORD = System.getProperty("password");

  private final TauronClient underTest = TauronClient.create();
  private SessionContext sessionContext;

  @Test
  @BeforeEach
  void shouldReturnSessionIdAfterLogin() {
    // when
    SessionContext result = underTest.login(LOGIN, PASSWORD);

    // then
    Assertions.assertNotNull(result);
    Assertions.assertNotNull(result.getSessionId());
    Assertions.assertNotNull(result.getCounterId());
    sessionContext = result;
  }

  @Test
  void shouldReturnDataForDay() {
    // given
    final DataForm params = DataForm.ofDay(sessionContext, LocalDate.now().minusDays(1));

    // when
    Chart result = underTest.getChart(params);

    // then
    Assertions.assertNotNull(result);
    Assertions.assertFalse(result.getConsumptions().isEmpty());
    Assertions.assertFalse(result.getProductions().isEmpty());
  }

  @Test
  void shouldReturnDataForMonth() {
    // given
    final DataForm params = DataForm.ofMonth(sessionContext, YearMonth.of(2022, Month.MARCH));

    // when
    Chart result = underTest.getChart(params);

    // then
    Assertions.assertNotNull(result);
    Assertions.assertFalse(result.getConsumptions().isEmpty());
    Assertions.assertFalse(result.getProductions().isEmpty());

    ChartValue chartValue = result.getConsumptions().get(0);
    Assertions.assertEquals(LocalDate.of(2022, 3, 1), chartValue.getDate());
    Assertions.assertEquals(1, chartValue.getSum().compareTo(BigDecimal.ZERO));
  }

  @Test
  void shouldReturnDataForYear() {
    // given
    final DataForm params = DataForm.ofYear(sessionContext, Year.of(2022));

    // when
    Chart result = underTest.getChart(params);

    // then
    Assertions.assertNotNull(result);
    Assertions.assertFalse(result.getConsumptions().isEmpty());
    Assertions.assertFalse(result.getProductions().isEmpty());
  }

  @Test
  void shouldReturnCsvPerDay() {
    // given
    final DataForm params =
        DataForm.ofPeriod(
            sessionContext, LocalDate.of(2022, 3, 1), LocalDate.of(2022, 3, 31), Sampling.DAILY);

    // when
    Chart result = underTest.getChart(params);

    // then
    Assertions.assertNotNull(result);
  }

  @Test
  void shouldReturnCsvPerHour() {
    // given
    final DataForm params =
        DataForm.ofPeriod(
            sessionContext, LocalDate.of(2022, 3, 1), LocalDate.of(2022, 3, 31), Sampling.HOURLY);

    // when
    Chart result = underTest.getChart(params);

    // then
    Assertions.assertNotNull(result);
  }
}
