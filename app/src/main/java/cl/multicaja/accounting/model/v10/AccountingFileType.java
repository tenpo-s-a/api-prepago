package cl.multicaja.accounting.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum AccountingFileType {

  ACCOUNTING("Accounting"),
  ACCOUNTING_RECONCILIATION("Accounting Reconciliation"),
  CLEARING("Clearing");


  AccountingFileType(String value) {
    this.value = value;
  }

  String value;
  public String getValue() {
    return value;
  }
  public static AccountingFileType valueOfEnum(String name) {
    try {
      return AccountingFileType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }

  private static Map<String, AccountingFileType> FORMAT_MAP = Stream
    .of(AccountingFileType.values())
    .collect(Collectors.toMap(s -> s.value, Function.identity()));

  @JsonCreator
  public static AccountingFileType fromValue(String value) {
    return FORMAT_MAP.get(value);
  }
}
