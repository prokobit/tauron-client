package io.prokobit.tauron.client.model;

import java.util.Objects;
import lombok.Value;

@Value(staticConstructor = "of")
public class SessionId {

  public static final String SESSION_ID_HEADER = "PHPSESSID=";

  String value;

  public static boolean isSessionIdHeader(final String value) {
    return Objects.nonNull(value) && value.startsWith(SESSION_ID_HEADER);
  }
}
