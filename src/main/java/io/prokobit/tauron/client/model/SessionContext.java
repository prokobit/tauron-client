package io.prokobit.tauron.client.model;

import lombok.Value;

@Value(staticConstructor = "of")
public class SessionContext {
  SessionId sessionId;
  String counterId;
}
