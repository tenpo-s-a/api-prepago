package cl.multicaja.prepaid.model.v10;

import java.util.List;
import java.util.Map;

/**
 * @author abarazarte
 */
public interface IPrepaidTransaction10 {

  NewAmountAndCurrency10 getAmount();

  void setMcVoucherType(String mcVoucherType);

  void setMcVoucherData(List<Map<String, String>> mcVoucherData);

  PrepaidMovementType getMovementType();

  TransactionOriginType getTransactionOriginType();

  String getMerchantCode();

  String getMerchantName();

  Integer getMerchantCategory();

  void setFeeList(List<PrepaidMovementFee10> feeList);

  void setFee(NewAmountAndCurrency10 fee);

  void setTotal(NewAmountAndCurrency10 total);

  Integer getRut();
}
