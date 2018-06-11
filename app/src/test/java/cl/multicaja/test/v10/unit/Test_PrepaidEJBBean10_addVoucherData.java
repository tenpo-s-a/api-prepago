package cl.multicaja.test.v10.unit;

import cl.multicaja.prepaid.model.v10.AmountAndCurrency10;
import cl.multicaja.prepaid.model.v10.NewAmountAndCurrency10;
import cl.multicaja.prepaid.model.v10.PrepaidTopup10;
import cl.multicaja.prepaid.model.v10.PrepaidWithdraw10;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author abarazarte
 */
public class Test_PrepaidEJBBean10_addVoucherData extends TestBaseUnit {

  @Test(expected = IllegalStateException.class)
  public void shouldReturnExceptionWhen_TopupNull() throws Exception {
    getPrepaidEJBBean10().addVoucherData(null);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldReturnExceptionWhen_TopupAmountNull() throws Exception {
    PrepaidTopup10 topup = new PrepaidTopup10();
    getPrepaidEJBBean10().addVoucherData(topup);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldReturnExceptionWhen_TopupAmountValueNull() throws Exception {
    PrepaidTopup10 topup = new PrepaidTopup10();
    NewAmountAndCurrency10 amount = new AmountAndCurrency10();
    topup.setAmount(amount);

    getPrepaidEJBBean10().addVoucherData(topup);
  }

  @Test
  public void shouldAddReceiptData_Topup() throws Exception {
    PrepaidTopup10 topup = new PrepaidTopup10();
    NewAmountAndCurrency10 amount = new AmountAndCurrency10();
    amount.setValue(new BigDecimal(1000000));
    topup.setAmount(amount);

    getPrepaidEJBBean10().addVoucherData(topup);

    Assert.assertNotNull("Deberia tener el tipo de voucher", topup.getMcVoucherType());
    Assert.assertEquals("Deberia tener el tipo de voucher", "A", topup.getMcVoucherType());
    Assert.assertNotNull("Deberia tener el data", topup.getMcVoucherData());
    Assert.assertTrue("Deberia tener el data", topup.getMcVoucherData().size() > 0);

    Map<String, String> variableData = topup.getMcVoucherData().get(0);
    Assert.assertNotNull("Deberia tener data", variableData);

    Assert.assertTrue("Deberia tener el atributo name", variableData.containsKey("name"));
    Assert.assertNotNull("Deberia tener el atributo", variableData.get("name"));
    Assert.assertEquals("Deberia tener el atributo name = amount_paid","amount_paid", variableData.get("name"));
    Assert.assertTrue("Deberia tener el atributo value", variableData.containsKey("value"));
    Assert.assertNotNull("Deberia tener el atributo value", variableData.get("value"));
    Assert.assertEquals("Deberia tener el atributo value = 1.000.000","1.000.000", variableData.get("value"));
  }

  @Test(expected = IllegalStateException.class)
  public void shouldReturnExceptionWhen_WithdrawNull() throws Exception {
    getPrepaidEJBBean10().addVoucherData(null);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldReturnExceptionWhen_WithdrawAmountNull() throws Exception {
    PrepaidWithdraw10 withdraw= new PrepaidWithdraw10();
    getPrepaidEJBBean10().addVoucherData(withdraw);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldReturnExceptionWhen_WithdrawAmountValueNull() throws Exception {
    PrepaidWithdraw10 withdraw= new PrepaidWithdraw10();
    NewAmountAndCurrency10 amount = new AmountAndCurrency10();
    withdraw.setAmount(amount);

    getPrepaidEJBBean10().addVoucherData(withdraw);
  }

  @Test
  public void shouldAddReceiptData_Withdraw() throws Exception {
    PrepaidWithdraw10 withdraw= new PrepaidWithdraw10();
    NewAmountAndCurrency10 amount = new AmountAndCurrency10();
    amount.setValue(new BigDecimal(1000000));
    withdraw.setAmount(amount);

    getPrepaidEJBBean10().addVoucherData(withdraw);

    Assert.assertNotNull("Deberia tener el tipo de voucher", withdraw.getMcVoucherType());
    Assert.assertEquals("Deberia tener el tipo de voucher", "A", withdraw.getMcVoucherType());
    Assert.assertNotNull("Deberia tener el data", withdraw.getMcVoucherData());
    Assert.assertTrue("Deberia tener el data", withdraw.getMcVoucherData().size() > 0);

    Map<String, String> variableData = withdraw.getMcVoucherData().get(0);
    Assert.assertNotNull("Deberia tener data", variableData);

    Assert.assertTrue("Deberia tener el atributo name", variableData.containsKey("name"));
    Assert.assertNotNull("Deberia tener el atributo", variableData.get("name"));
    Assert.assertEquals("Deberia tener el atributo name = amount_paid","amount_paid", variableData.get("name"));
    Assert.assertTrue("Deberia tener el atributo value", variableData.containsKey("value"));
    Assert.assertNotNull("Deberia tener el atributo value", variableData.get("value"));
    Assert.assertEquals("Deberia tener el atributo value = 1.000.000","1.000.000", variableData.get("value"));
  }
}
