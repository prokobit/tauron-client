package io.prokobit.tauron.client.form;

import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Map;

abstract class AbstractBodyPublisher {

  protected abstract Map<String, String> form();

  public HttpRequest.BodyPublisher bodyPublisher() {
    var builder = new StringBuilder();
    for (Map.Entry<String, String> entry : form().entrySet()) {
      if (builder.length() > 0) {
        builder.append("&");
      }
      builder.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
      builder.append("=");
      builder.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
    }
    return HttpRequest.BodyPublishers.ofString(builder.toString());
  }
}
