package io.prokobit.tauron.client.form;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginForm extends AbstractBodyPublisher {

  private final String login;
  private final String password;
  private final String service;

  protected Map<String, String> form() {
    Map<String, String> data = new LinkedHashMap<>();
    data.put("username", login);
    data.put("password", password);
    data.put("service", service);
    return data;
  }
}
