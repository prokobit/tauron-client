package io.prokobit.tauron.client.form;

import io.prokobit.tauron.client.model.SessionContext;
import io.prokobit.tauron.client.model.SessionId;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Builder;

@Builder(access = AccessLevel.PACKAGE)
public class DataForm extends AbstractBodyPublisher {

  private SessionId sessionId;
  private String smartNr;

  private DataType paramType;

  private LocalDate chartDay;

  private Month chartMonth;

  private Integer chartYear;

  private LocalDate startDay;

  private LocalDate endDay;

  private Sampling trybCSV;

  @Builder.Default private String checkOZE = "on";

  @Builder.Default private String chartType = "1";

  @Builder.Default private String paramArea = "";

  public String getSessionId() {
    return sessionId.getValue();
  }

  public static DataForm ofDay(SessionContext context, LocalDate date) {
    return builder()
        .sessionId(context.getSessionId())
        .smartNr(context.getCounterId())
        .chartDay(date)
        .paramType(DataType.day)
        .build();
  }

  public static DataForm ofMonth(SessionContext context, YearMonth yearMonth) {
    return builder()
        .sessionId(context.getSessionId())
        .smartNr(context.getCounterId())
        .chartMonth(yearMonth.getMonth())
        .chartYear(yearMonth.getYear())
        .paramType(DataType.month)
        .build();
  }

  public static DataForm ofYear(SessionContext context, Year year) {
    return builder()
        .sessionId(context.getSessionId())
        .smartNr(context.getCounterId())
        .chartYear(year.getValue())
        .paramType(DataType.year)
        .build();
  }

  public static DataForm ofPeriod(SessionContext context, LocalDate startDay, LocalDate endDay) {
    return builder()
        .sessionId(context.getSessionId())
        .smartNr(context.getCounterId())
        .startDay(startDay)
        .endDay(endDay)
        .paramType(DataType.other)
        .build();
  }

  public static DataForm ofPeriod(
      SessionContext context, LocalDate startDay, LocalDate endDay, Sampling type) {
    return builder()
        .sessionId(context.getSessionId())
        .smartNr(context.getCounterId())
        .startDay(startDay)
        .endDay(endDay)
        .paramType(DataType.csv)
        .trybCSV(type)
        .build();
  }

  @Override
  protected Map<String, String> form() {
    Map<String, String> data = new HashMap<>();
    data.put("dane[smartNr]", smartNr);
    data.put("dane[checkOZE]", checkOZE);
    data.put("dane[paramType]", paramType.name());
    data.put("dane[paramArea]", paramArea);
    data.put("dane[chartType]", chartType);

    Optional.ofNullable(chartDay).ifPresent(day -> data.put("dane[chartDay]", String.valueOf(day)));

    Optional.ofNullable(chartMonth)
        .ifPresent(month -> data.put("dane[chartMonth]", String.valueOf(month.getValue())));

    Optional.ofNullable(chartYear)
        .ifPresent(year -> data.put("dane[chartYear]", String.valueOf(year)));

    Optional.ofNullable(startDay)
        .ifPresent(start -> data.put("dane[startDay]", String.valueOf(start)));

    Optional.ofNullable(endDay).ifPresent(end -> data.put("dane[endDay]", String.valueOf(end)));

    Optional.ofNullable(trybCSV)
        .ifPresent(csvType -> data.put("dane[trybCSV]", csvType.getValue()));

    return data;
  }
}
