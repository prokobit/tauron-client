package io.prokobit.tauron.client;

import io.prokobit.tauron.client.data.Chart;
import io.prokobit.tauron.client.form.DataForm;
import io.prokobit.tauron.client.model.SessionContext;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public interface TauronClient {

  SessionContext login(final String login, final String password);

  Chart getChart(final DataForm params);

  static TauronClient create() {
    return new TauronClientImpl(new Config());
  }

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  class Config {
    private String serviceUrl = "https://elicznik.tauron-dystrybucja.pl";

    private String chartsUrl = "https://elicznik.tauron-dystrybucja.pl/index/charts";

    private String loginUrl = "https://logowanie.tauron-dystrybucja.pl/login?service=%s";
  }
}
