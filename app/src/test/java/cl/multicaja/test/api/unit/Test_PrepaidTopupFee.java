package cl.multicaja.test.api.unit;

import cl.multicaja.prepaid.domain.NewAmountAndCurrency;
import cl.multicaja.prepaid.domain.PrepaidTopup;
import cl.multicaja.prepaid.domain.TopupType;
import cl.multicaja.prepaid.ejb.v10.PrepaidEJBBean10;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author abarazarte
 */
public class Test_PrepaidTopupFee {

  private static PrepaidEJBBean10 bean;

  @BeforeClass
  public static void setup(){
    bean = new PrepaidEJBBean10();
  }

  @Test(expected = IllegalStateException.class)
  public void shouldReturnExceptionWhen_TopupNull() throws Exception {
    bean.calculateTopupFeeAndTotal(null);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldReturnExceptionWhen_TopupAmountNull() throws Exception {
    PrepaidTopup topup = new PrepaidTopup();
    bean.calculateTopupFeeAndTotal(topup);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldReturnExceptionWhen_TopupAmountValueNull() throws Exception {
    PrepaidTopup topup = new PrepaidTopup();
    NewAmountAndCurrency amount = new NewAmountAndCurrency();
    topup.setAmount(amount);

    bean.calculateTopupFeeAndTotal(topup);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldReturnExceptionWhen_TopupMerchantCodeNull() throws Exception {
    PrepaidTopup topup = new PrepaidTopup();
    NewAmountAndCurrency amount = new NewAmountAndCurrency();
    amount.setValue(new BigDecimal(100));
    topup.setAmount(amount);

    bean.calculateTopupFeeAndTotal(topup);
  }

  /*
    Calcula la comision WEB -> $0
   */
  @Test
  public void shouldCalculateWebFee()  throws Exception{
    PrepaidTopup topup = new PrepaidTopup();
    NewAmountAndCurrency amount = new NewAmountAndCurrency();
    amount.setCurrencyCode(152);
    amount.setValue(new BigDecimal(5000));
    topup.setAmount(amount);
    topup.setMerchantCode("999999999999991");

    bean.calculateTopupFeeAndTotal(topup);

    assertEquals("Deberia ser de tipo WEB", TopupType.WEB, topup.getType());
    assertNotNull("Deberia tener comision", topup.getFee());
    assertNotNull("Deberia tener total", topup.getTotal());
    assertEquals("Deberia tener monto de comision = 0", new BigDecimal(0), topup.getFee().getValue());
    assertEquals("Deberia tener total = 5000", new BigDecimal(5000), topup.getTotal().getValue());
  }

  /*
    Calcula la comision POS -> $100 + IVA
   */
  @Test
  public void shouldCalculatePosFee_100() throws Exception {
    PrepaidTopup topup = new PrepaidTopup();
    NewAmountAndCurrency amount = new NewAmountAndCurrency();
    amount.setCurrencyCode(152);
    amount.setValue(new BigDecimal(5000));
    topup.setAmount(amount);
    topup.setMerchantCode("1234567890");

    bean.calculateTopupFeeAndTotal(topup);

    assertEquals("Deberia ser de tipo POS", TopupType.POS, topup.getType());
    assertNotNull("Deberia tener comision", topup.getFee());
    assertNotNull("Deberia tener total", topup.getTotal());
    assertEquals("Deberia tener monto de comision = 119", new BigDecimal(119), topup.getFee().getValue());
    assertEquals("Deberia tener total = 4881", new BigDecimal(4881), topup.getTotal().getValue());
  }

  /*
    Calcula la comision POS -> (0,5% * TopupAmount) + IVA
   */
  @Test
  public void shouldCalculatePosFee() throws Exception{
    PrepaidTopup topup = new PrepaidTopup();
    NewAmountAndCurrency amount = new NewAmountAndCurrency();
    amount.setCurrencyCode(152);
    amount.setValue(new BigDecimal(50000));
    topup.setAmount(amount);
    topup.setMerchantCode("1234567890");

    bean.calculateTopupFeeAndTotal(topup);

    assertEquals("Deberia ser de tipo POS", TopupType.POS, topup.getType());
    assertNotNull("Deberia tener comision", topup.getFee());
    assertNotNull("Deberia tener total", topup.getTotal());
    assertEquals("Deberia tener monto de comision = 297.5", new BigDecimal(297.5), topup.getFee().getValue());
    assertEquals("Deberia tener total = 49702.5", new BigDecimal(49702.5), topup.getTotal().getValue());
  }
}
