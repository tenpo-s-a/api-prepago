package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.prepaid.model.v10.NewAmountAndCurrency10;
import cl.multicaja.prepaid.model.v10.PrepaidTopup10;
import cl.multicaja.prepaid.model.v10.PrepaidWithdraw10;
import cl.multicaja.prepaid.model.v10.TransactionOriginType;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author abarazarte
 */
public class Test_PrepaidEJBBean10_calculateTopupFeeAndTotal extends TestBaseUnit {

  @Test(expected = BadRequestException.class)
  public void shouldReturnExceptionWhen_TopupNull() throws Exception {
    getPrepaidEJBBean10().calculateFeeAndTotal(null);
  }

  @Test(expected = BadRequestException.class)
  public void shouldReturnExceptionWhen_TopupAmountNull() throws Exception {
    PrepaidTopup10 topup = new PrepaidTopup10();
    getPrepaidEJBBean10().calculateFeeAndTotal(topup);
  }

  @Test(expected = BadRequestException.class)
  public void shouldReturnExceptionWhen_TopupAmountValueNull() throws Exception {
    PrepaidTopup10 topup = new PrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    topup.setAmount(amount);

    getPrepaidEJBBean10().calculateFeeAndTotal(topup);
  }

  @Test(expected = BadRequestException.class)
  public void shouldReturnExceptionWhen_TopupMerchantCodeNull() throws Exception {
    PrepaidTopup10 topup = new PrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setValue(BigDecimal.valueOf(100));
    topup.setAmount(amount);

    getPrepaidEJBBean10().calculateFeeAndTotal(topup);
  }

  /*
    Calcula la comision WEB -> $0
   */
  @Test
  public void shouldCalculateWebTopupFee()  throws Exception{
    PrepaidTopup10 topup = new PrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(BigDecimal.valueOf(5000));
    topup.setAmount(amount);
    topup.setMerchantCode("999999999999991");

    getPrepaidEJBBean10().calculateFeeAndTotal(topup);

    assertEquals("Deberia ser de tipo WEB", TransactionOriginType.WEB, topup.getTransactionOriginType());
    assertNotNull("Deberia tener comision", topup.getFee());
    assertNotNull("Deberia tener total", topup.getTotal());
    assertEquals("Deberia tener monto de comision = 0", BigDecimal.valueOf(0), topup.getFee().getValue());
    assertEquals("Deberia tener total = 5000", BigDecimal.valueOf(5000), topup.getTotal().getValue());
  }

  /*
    Calcula la comision POS -> $100 + IVA
   */
  @Test
  public void shouldCalculatePosTopupFee_100() throws Exception {
    PrepaidTopup10 topup = new PrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(BigDecimal.valueOf(5000));
    topup.setAmount(amount);
    topup.setMerchantCode("1234567890");

    getPrepaidEJBBean10().calculateFeeAndTotal(topup);

    assertEquals("Deberia ser de tipo POS", TransactionOriginType.POS, topup.getTransactionOriginType());
    assertNotNull("Deberia tener comision", topup.getFee());
    assertNotNull("Deberia tener total", topup.getTotal());
    assertEquals("Deberia tener monto de comision = 119", BigDecimal.valueOf(119), topup.getFee().getValue());
    assertEquals("Deberia tener total = 4881", BigDecimal.valueOf(4881), topup.getTotal().getValue());
  }

  /*
    Calcula la comision POS -> (0,5% * TopupAmount) + IVA
   */
  @Test
  public void shouldCalculatePosTopupFee() throws Exception{
    PrepaidTopup10 topup = new PrepaidTopup10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(BigDecimal.valueOf(50000));
    topup.setAmount(amount);
    topup.setMerchantCode("1234567890");

    getPrepaidEJBBean10().calculateFeeAndTotal(topup);

    assertEquals("Deberia ser de tipo POS", TransactionOriginType.POS, topup.getTransactionOriginType());
    assertNotNull("Deberia tener comision", topup.getFee());
    assertNotNull("Deberia tener total", topup.getTotal());
    assertEquals("Deberia tener monto de comision = 297", BigDecimal.valueOf(297), topup.getFee().getValue());
    assertEquals("Deberia tener total = 49703", BigDecimal.valueOf(49703), topup.getTotal().getValue());
  }

  @Test(expected = BadRequestException.class)
  public void shouldReturnExceptionWhen_WithdrawNull() throws Exception {
    getPrepaidEJBBean10().calculateFeeAndTotal(null);
  }

  @Test(expected = BadRequestException.class)
  public void shouldReturnExceptionWhen_WithdrawAmountNull() throws Exception {
    PrepaidWithdraw10 withdraw = new PrepaidWithdraw10();
    getPrepaidEJBBean10().calculateFeeAndTotal(withdraw);
  }

  @Test(expected = BadRequestException.class)
  public void shouldReturnExceptionWhen_WithdrawAmountValueNull() throws Exception {
    PrepaidWithdraw10 withdraw = new PrepaidWithdraw10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    withdraw.setAmount(amount);

    getPrepaidEJBBean10().calculateFeeAndTotal(withdraw);
  }

  @Test(expected = BadRequestException.class)
  public void shouldReturnExceptionWhen_WithdrawMerchantCodeNull() throws Exception {
    PrepaidWithdraw10 withdraw = new PrepaidWithdraw10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setValue(BigDecimal.valueOf(100));
    withdraw.setAmount(amount);

    getPrepaidEJBBean10().calculateFeeAndTotal(withdraw);
  }

  /*
    Calcula la comision WEB -> $0
   */
  @Test
  public void shouldCalculateWebWithdrawFee()  throws Exception{
    PrepaidWithdraw10 withdraw = new PrepaidWithdraw10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(BigDecimal.valueOf(5000));
    withdraw.setAmount(amount);
    withdraw.setMerchantCode("999999999999991");

    getPrepaidEJBBean10().calculateFeeAndTotal(withdraw);

    assertEquals("Deberia ser de tipo WEB", TransactionOriginType.WEB, withdraw.getTransactionOriginType());
    assertNotNull("Deberia tener comision", withdraw.getFee());
    assertNotNull("Deberia tener total", withdraw.getTotal());
    assertEquals("Deberia tener monto de comision = 0", BigDecimal.valueOf(100), withdraw.getFee().getValue());
    assertEquals("Deberia tener total = 5000", BigDecimal.valueOf(5100), withdraw.getTotal().getValue());
  }

  /*
    Calcula la comision POS -> $100 + IVA
   */
  @Test
  public void shouldCalculatePosWithdrawFee_100() throws Exception {
    PrepaidWithdraw10 withdraw = new PrepaidWithdraw10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(BigDecimal.valueOf(5000));
    withdraw.setAmount(amount);
    withdraw.setMerchantCode("1234567890");

    getPrepaidEJBBean10().calculateFeeAndTotal(withdraw);

    assertEquals("Deberia ser de tipo POS", TransactionOriginType.POS, withdraw.getTransactionOriginType());
    assertNotNull("Deberia tener comision", withdraw.getFee());
    assertNotNull("Deberia tener total", withdraw.getTotal());
    assertEquals("Deberia tener monto de comision = 119", BigDecimal.valueOf(119), withdraw.getFee().getValue());
    assertEquals("Deberia tener total = 4881", BigDecimal.valueOf(5119), withdraw.getTotal().getValue());
  }

  /*
    Calcula la comision POS -> (0,5% * TopupAmount) + IVA
   */
  @Test
  public void shouldCalculatePosWithdrawFee() throws Exception{
    PrepaidWithdraw10 withdraw = new PrepaidWithdraw10();
    NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
    amount.setCurrencyCode(CodigoMoneda.CHILE_CLP);
    amount.setValue(BigDecimal.valueOf(50000));
    withdraw.setAmount(amount);
    withdraw.setMerchantCode("1234567890");

    getPrepaidEJBBean10().calculateFeeAndTotal(withdraw);

    assertEquals("Deberia ser de tipo POS", TransactionOriginType.POS, withdraw.getTransactionOriginType());
    assertNotNull("Deberia tener comision", withdraw.getFee());
    assertNotNull("Deberia tener total", withdraw.getTotal());
    assertEquals("Deberia tener monto de comision = 297", BigDecimal.valueOf(297), withdraw.getFee().getValue());
    assertEquals("Deberia tener total = 50297", BigDecimal.valueOf(50297), withdraw.getTotal().getValue());
  }
}
