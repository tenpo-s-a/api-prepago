package cl.multicaja.accounting.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum AccountingFileFormatType {

  CSV("CSV"),
  XLS("XLS"),
  XLSX("XLSX");
  String value;

  AccountingFileFormatType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
  public static AccountingFileFormatType valueOfEnum(String name) {
    try {
      return AccountingFileFormatType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }

  private static Map<String, AccountingFileFormatType> FORMAT_MAP = Stream
    .of(AccountingFileFormatType.values())
    .collect(Collectors.toMap(s -> s.value, Function.identity()));

  @JsonCreator
  public static AccountingFileFormatType fromValue(String value) {
    return FORMAT_MAP.get(value);
  }
}
