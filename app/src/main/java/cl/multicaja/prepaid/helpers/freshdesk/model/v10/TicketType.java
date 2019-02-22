package cl.multicaja.prepaid.helpers.freshdesk.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum TicketType {

  VALIDACION_IDENTIDAD("Validación de identidad"),
  GENERICO("Genérico"),
  COLAS_NEGATIVAS("Cola Negativa"),
  DEVOLUCION("devolucion"),
  EMERGENCIA("Emergencia");

  private String value;

  TicketType(String value) {
    this.value = value;
  }
  @JsonValue
  public String getValue() {
    return value;
  }

  private static Map<String, TicketType> FORMAT_MAP = Stream
    .of(TicketType.values())
    .collect(Collectors.toMap(s -> s.value, Function.identity()));

  @JsonCreator
  public static TicketType fromValue(String value) {
    return FORMAT_MAP.get(value);
  }

}
