package cl.multicaja.test.unit;

import cl.multicaja.prepaid.model.v10.CdtTransactionType;
import cl.multicaja.prepaid.model.v10.NewPrepaidTopup10;
import cl.multicaja.prepaid.model.v10.TransactionOriginType;
import cl.multicaja.test.integration.v10.unit.TestBaseUnit;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author abarazarte
 */
public class Test_NewPrepaidTopup10 extends TestBaseUnit {

  @Test
  public void shouldBeTypeWeb() {
    NewPrepaidTopup10 topup = new NewPrepaidTopup10();
    topup.setMerchantCode("999999999999991");

    Assert.assertEquals("Deberia ser de tipo WEB", TransactionOriginType.WEB, topup.getTransactionOriginType());
  }

  @Test
  public void shouldBeTypePos() {
    NewPrepaidTopup10 topup = new NewPrepaidTopup10();
    topup.setMerchantCode("111111111111111");

    assertEquals("Deberia ser de tipo POS", TransactionOriginType.POS, topup.getTransactionOriginType());
  }

  @Test
  public void shouldBeFirstTopupByDefault() {
    NewPrepaidTopup10 topup = new NewPrepaidTopup10();

    assertTrue("Deberia ser 1era carga por defecto", topup.isFirstTopup());
  }

  @Test
  public void shouldBeCdtType_FirstTopup() {
    NewPrepaidTopup10 topup = new NewPrepaidTopup10();

    Assert.assertEquals("Deberia ser tipo cdt 1era carga por defecto", CdtTransactionType.PRIMERA_CARGA, topup.getCdtTransactionType());

    topup.setMerchantCode("999999999999991");
    assertEquals("Deberia ser de tipo WEB", TransactionOriginType.WEB, topup.getTransactionOriginType());
    assertEquals("Deberia ser tipo cdt 1era carga", CdtTransactionType.PRIMERA_CARGA, topup.getCdtTransactionType());

    topup.setMerchantCode("111111111111111");
    assertEquals("Deberia ser de tipo POS", TransactionOriginType.POS, topup.getTransactionOriginType());
    assertEquals("Deberia ser tipo cdt 1era carga por defecto", CdtTransactionType.PRIMERA_CARGA, topup.getCdtTransactionType());
  }

  @Test
  public void shouldBeCdtType_WebTopup() {
    NewPrepaidTopup10 topup = new NewPrepaidTopup10();
    topup.setMerchantCode("999999999999991");
    topup.setFirstTopup(Boolean.FALSE);

    assertEquals("Deberia ser de tipo WEB", TransactionOriginType.WEB, topup.getTransactionOriginType());
    assertEquals("Deberia ser tipo cdt sol carga WEB", CdtTransactionType.CARGA_WEB, topup.getCdtTransactionType());
  }

  @Test
  public void shouldBeCdtType_PosTopup() {
    NewPrepaidTopup10 topup = new NewPrepaidTopup10();
    topup.setMerchantCode("111111111111111");
    topup.setFirstTopup(Boolean.FALSE);

    assertEquals("Deberia ser de tipo POS", TransactionOriginType.POS, topup.getTransactionOriginType());
    assertEquals("Deberia ser tipo cdt sol carga POS", CdtTransactionType.CARGA_POS, topup.getCdtTransactionType());
  }

}
