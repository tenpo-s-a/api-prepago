package cl.multicaja.prepaid.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ResearchMovementDescriptionType {

  NOT_RECONCILIATION_TO_PROCESOR("NO_CONCILIADO_CONTRA_PROCESADORA"),
  NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR("NO_CONCILIADO_CONTRA_SWITCH_Y_PROCESADORA"),
  NOT_RECONCILIATION_TO_BANC_AND_PROCESOR("NO_CONCILIADO_CONTRA_BANCO_Y_PROCESADORA"),
  ERROR_STATUS_ON_DB("ERROR_DE_ESTADO_EN_BBDD"),
  MOVEMENT_REJECTED_IN_AUTHORIZATION("MOVIMIENTO_RECHAZADO_EN_AUTORIZACION"),
  MOVEMENT_NOT_FOUND_ON_DB("MOVIMIENTO_NO_ENCONTRADO_EN_BBDD"),
  MOVEMENT_NOT_FOUND_IN_FILE("MOVIMIENTO_NO_ENCONTRADO_EN_ARCHIVO"),
  ERROR_INFO("INFORMACION_ERRONEA"),
  ERROR_UNDEFINED("ERROR_INDEFINIDO"),
  DESCRIPTION_UNDEFINED("DESCRIPCION_INDEFINIDA");


  public String getValue() {
    return value;
  }

  String value;

  ResearchMovementDescriptionType(String value) {
    this.value = value;
  }

  public static ResearchMovementDescriptionType valueOfEnum(String name) {
    try {
      return ResearchMovementDescriptionType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
  private static Map<String, ResearchMovementDescriptionType> FORMAT_MAP = Stream
    .of(ResearchMovementDescriptionType.values())
    .collect(Collectors.toMap(s -> s.value, Function.identity()));

  @JsonCreator
  public static ResearchMovementDescriptionType fromValue(String value) {
    return FORMAT_MAP.get(value);
  }

}
