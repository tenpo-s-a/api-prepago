package cl.multicaja.prepaid.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum ResearchMovementResponsibleStatusType {

  RECONCILIATION_PREPAID("RESPONSABLE_CONCILIACIONES_PREPAGO"),
  OTI_PREPAID("RESPONSABLE_OTI_PREPAGO"),
  RECONCILIATION_MULTICAJA("RESPONSABLE_CONCILIACIONES_MULTICAJA"),
  RECONCIALITION_MULTICAJA_OTI_PREPAGO("RESPONSABLE_CONCILIACIONES_MULTICAJA_OTI_PREPAGO"),
  STATUS_UNDEFINED("ESTADO_INDEFINIDO"),
  IS_TABLE("TABLA_EN_BD_NO_HAY_ARCHIVO");

  public String getValue() {
    return value;
  }

  String value;

  ResearchMovementResponsibleStatusType(String value) {
    this.value = value;
  }

  public static ResearchMovementResponsibleStatusType valueOfEnum(String name) {
    try {
      return ResearchMovementResponsibleStatusType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }
  private static Map<String, ResearchMovementResponsibleStatusType> FORMAT_MAP = Stream
    .of(ResearchMovementResponsibleStatusType.values())
    .collect(Collectors.toMap(s -> s.value, Function.identity()));

  @JsonCreator
  public static ResearchMovementResponsibleStatusType fromValue(String value) {
    return FORMAT_MAP.get(value);
  }

}
