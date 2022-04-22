package io.prokobit.tauron.client.data;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import io.prokobit.tauron.client.deserializer.ChartValuesDeserializer;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChartData {

  @SerializedName("chart")
  @JsonAdapter(ChartValuesDeserializer.class)
  private List<ChartValue> consumptions;

  @SerializedName("OZE")
  @JsonAdapter(ChartValuesDeserializer.class)
  private List<ChartValue> productions;
}
