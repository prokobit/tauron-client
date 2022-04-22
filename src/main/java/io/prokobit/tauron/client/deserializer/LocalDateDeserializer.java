package io.prokobit.tauron.client.deserializer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class LocalDateDeserializer implements JsonDeserializer<LocalDate> {

  private static final DateTimeFormatter formatter =
      new DateTimeFormatterBuilder()
          .appendPattern("yyyy-MM")
          .appendOptional(DateTimeFormatter.ofPattern("-dd"))
          .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
          .toFormatter();

  @Override
  public LocalDate deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext)
      throws JsonParseException {
    return LocalDate.parse(jsonElement.getAsJsonPrimitive().getAsString(), formatter);
  }
}
