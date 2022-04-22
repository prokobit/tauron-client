package io.prokobit.tauron.client.data;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.util.List;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;

@Data
public class Chart {

  @Getter(value = AccessLevel.NONE)
  @SerializedName("dane")
  private ChartData data;

  private boolean isFull;

  @SerializedName("sum")
  private BigDecimal sumConsumption;

  @SerializedName("average")
  private BigDecimal avgConsumption;

  @SerializedName("OZEValue")
  private BigDecimal sumProduction;

  @SerializedName("averageOZE")
  private BigDecimal avgProduction;

  public List<ChartValue> getConsumptions() {
    return data.getConsumptions();
  }

  public List<ChartValue> getProductions() {
    return data.getProductions();
  }
}
