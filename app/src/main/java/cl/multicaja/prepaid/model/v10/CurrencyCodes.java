package cl.multicaja.prepaid.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @autor vutreras
 */
public enum CurrencyCodes {

  CHILE_CLP(152);

  private Integer value;

  CurrencyCodes(Integer value) {
    this.value = value;
  }

  @JsonValue
  public Integer getValue() {
    return value;
  }

  private static Map<Integer, CurrencyCodes> FORMAT_MAP = Stream
    .of(CurrencyCodes.values())
    .collect(Collectors.toMap(s -> s.value, Function.identity()));

  @JsonCreator
  public static CurrencyCodes fromValue(Integer value) {
    return Optional
      .ofNullable(FORMAT_MAP.get(value))
      .orElseThrow(() -> new IllegalArgumentException("value"));
  }
}
