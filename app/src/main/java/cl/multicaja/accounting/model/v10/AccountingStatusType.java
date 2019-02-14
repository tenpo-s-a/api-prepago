package cl.multicaja.accounting.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum AccountingStatusType {

  // GENERALES
  PENDING("PENDING"),
  OK("OK"),
  SENT("SENT"),
  REVERSED("REVERSED"),
  RESEARCH("INVESTIGAR"),

  // ACCOUNTING y ACCOUNTING_STATUS
  SENT_PENDING_CON("SENT_PENDING_CON"),
  NOT_CONFIRMED("NO_CONFIRMADA"),

  // CLEARING
  INITIAL("INITIAL"),
  REJECTED("RECHAZADO"),
  REJECTED_FORMAT("RECHAZADO_FORMATO"),
  NOT_IN_FILE("NOT_IN_FILE"),
  INVALID_INFORMATION("INVALID_INFORMATION");

  String value;

  AccountingStatusType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
  public static AccountingStatusType valueOfEnum(String name) {
    try {
      return AccountingStatusType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }

  private static Map<String, AccountingStatusType> FORMAT_MAP = Stream
    .of(AccountingStatusType.values())
    .collect(Collectors.toMap(s -> s.value, Function.identity()));

  @JsonCreator
  public static AccountingStatusType fromValue(String value) {
    return FORMAT_MAP.get(value);
  }
}
