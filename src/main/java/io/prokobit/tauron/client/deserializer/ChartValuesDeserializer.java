package io.prokobit.tauron.client.deserializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import io.prokobit.tauron.client.data.ChartValue;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ChartValuesDeserializer implements JsonDeserializer<List<ChartValue>> {

  private final Gson parser =
      new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateDeserializer()).create();

  @Override
  public List<ChartValue> deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    if (jsonElement.isJsonArray()) {
      return parser.fromJson(jsonElement, type);
    }
    return jsonElement.getAsJsonObject().entrySet().stream()
        .map(entry -> parser.fromJson(entry.getValue(), ChartValue.class))
        .collect(Collectors.toList());
  }
}
