package io.prokobit.tauron.client.form;

public enum Sampling {
  HOURLY("godzin"),
  DAILY("dzien");

  private final String value;

  Sampling(final String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
}
