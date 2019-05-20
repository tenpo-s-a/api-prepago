package cl.multicaja.prepaid.helpers.freshdesk.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum TicketType {

  //Antiguos Items - No borrar
  /*
  VALIDACION_IDENTIDAD("Validación de identidad"),
  GENERICO("Genérico"),
  COLAS_NEGATIVAS("Cola Negativa"),
  DEVOLUCION("devolucion"),
  EMERGENCIA("Emergencia");
   */

  //Nuevos Items
  GENERICO("Genérico"),
  EMERGENCIA("Emergencia"),
  CIERRE_DE_CUENTA("Cierre de cuenta"),
  CONSULTA("Consulta"),
  COLAS_NEGATIVAS("Cola Negativa"),
  DEVOLUCION("devolucion"),
  RECLAMO_CONTRACARGO("Reclamo Contracargo"),
  RECLAMO_TARJETA("Reclamo Tarjeta"),
  CIERRE_TARJETA("Cierre Tarjeta"),
  VALIDACION_IDENTIDAD("Validacion identidad"),
  RECARGA_FALLIDA("Recarga Fallida"),
  CONSULTAS_GENERALES("Consultas Generales"),
  PROMOCIONES_RECARGAS("Promociones Recargas"),
  QUIERO_MI_COMPROBANTE("Quiero mi comprobante"),
  SUGERENCIAS_RECARGAS("Sugerencias Recargas");

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
