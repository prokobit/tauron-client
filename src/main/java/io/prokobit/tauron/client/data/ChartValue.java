package io.prokobit.tauron.client.data;

import com.google.gson.annotations.SerializedName;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Data;

@Data
public class ChartValue {

  @SerializedName(
      value = "suma",
      alternate = {"EC"})
  private BigDecimal sum;

  @SerializedName("Date")
  private LocalDate date;

  @SerializedName("Hour")
  private Integer hour;
}
