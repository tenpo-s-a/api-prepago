package cl.multicaja.accounting.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Descripcion de uso de los status
 *
 * Accounting:
 *  - status:
 *    - PENDING
 *    - SENT
 *    - SENT_PENDING_CON
 *    - NOT_SEND
 *  - accountingStatus:
 *    - OK
 *    - NOT_OK
 * -------------------------------
 * Clearing:
 *  - INITIAL
 *  - NOT_SEND
 *  - OK
 *  - REVERSED
 *  - RESEARCH
 *  - REVERSED
 *  - REJECTED
 *  - REJECTED_FORMAT
 *  - NOT_IN_FILE
 *  - INVALID_INFORMATION
 */
public enum AccountingStatusType {

  // Accounting - status
  PENDING("PENDING"),
  SENT("SENT"),
  SENT_PENDING_CON("SENT_PENDING_CON"),
  NOT_SEND("NOT_SEND"),

  // Accounting - accountingStatus
  OK("OK"),
  NOT_OK("NOT_OK"),

  // Clearing - status
  REVERSED("REVERSED"),
  RESEARCH("INVESTIGAR"),
  NOT_CONFIRMED("NO_CONFIRMADA"),
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
