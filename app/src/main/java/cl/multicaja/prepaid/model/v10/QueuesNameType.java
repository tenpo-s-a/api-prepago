package cl.multicaja.prepaid.model.v10;

import com.fasterxml.jackson.annotation.JsonCreator;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum QueuesNameType {

  TOPUP("TopUp"),
  WITHDRAWAL("WithDrawal"),
  CREATE_CARD("PendingCreateCard"),
  REVERSE_TOPUP("ReverseTopup"),
  REVERSE_WITHDRAWAL("ReverseWithdrawal"),
  SEND_MAIL("SendMail"),
  PENDING_EMISSION("PendingEmision"),
  ISSUANCE_FEE("IssuanceFee");

  String value;

  QueuesNameType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
  public static QueuesNameType valueOfEnum(String name) {
    try {
      return QueuesNameType.valueOf(name);
    } catch(Exception ex) {
      return null;
    }
  }

  private static Map<String, QueuesNameType> FORMAT_MAP = Stream
    .of(QueuesNameType.values())
    .collect(Collectors.toMap(s -> s.value, Function.identity()));

  @JsonCreator
  public static QueuesNameType fromValue(String value) {
    return FORMAT_MAP.get(value);
  }
}
