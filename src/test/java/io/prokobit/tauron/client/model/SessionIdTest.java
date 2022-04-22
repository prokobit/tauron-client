package io.prokobit.tauron.client.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SessionIdTest {

  @Test
  void shouldReturnFalseForNullInput() {
    // expects
    assertFalse(SessionId.isSessionIdHeader(null));
  }

  @Test
  void shouldReturnFalseForWrongSessionIdName() {
    // expects
    assertFalse(SessionId.isSessionIdHeader("FAKE_SESSIONID="));
  }
}
