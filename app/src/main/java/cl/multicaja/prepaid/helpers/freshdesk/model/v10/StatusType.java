package cl.multicaja.prepaid.helpers.freshdesk.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum StatusType {

  //Antiguos Items - No borrar
  /*OPEN(2),
  PENDING(3),
  RESOLVED(4),
  CLOSED(5);*/

  //Nuevos Items
  //3,2,4,5,11,10,13,14,16,17
  TODOS_LOS_NO_RESUELTOS(3),
  OPEN(2),
  PENDING(4),
  RESOLVED(5),
  CLOSED(11),
  PENDIENTE_RESPUESTA_OPERACIONES(10),
  PENDIENTE_RESPUESTA_PREPAGO(13),
  PENDIENTE_DEVOLUCION(14),
  PENDIENTE_CONFIRMACION_DEVOLUCION(16),
  PENDIENTE_PROCESO_CONTRACARGO_MASTERCOM(17),
  PENDIENTE_RESPUEST_CONTACT_CENTER(18); //Solo aparece en el filtro de freshdesk

  private Integer value;

  StatusType(Integer value) {
    this.value = value;
  }

  @JsonValue
  public Integer getValue() {
    return value;
  }

  private static Map<Integer, StatusType> FORMAT_MAP = Stream
    .of(StatusType.values())
    .collect(Collectors.toMap(s -> s.value, Function.identity()));

  @JsonCreator
  public static StatusType fromValue(Integer value) {
    return FORMAT_MAP.get(value);
  }

}
