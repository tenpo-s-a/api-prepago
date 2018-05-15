package cl.multicaja.test.api.unit;

import cl.multicaja.prepaid.domain.v10.CdtTransactionType;
import cl.multicaja.prepaid.domain.v10.NewPrepaidTopup10;
import cl.multicaja.prepaid.domain.v10.TopupType;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author abarazarte
 */
public class Test_NewPrepaidTopup10 {

  @Test
  public void shouldBeTypeWeb() {
    NewPrepaidTopup10 topup = new NewPrepaidTopup10();
    topup.setMerchantCode("999999999999991");

    Assert.assertEquals("Deberia ser de tipo WEB", TopupType.WEB, topup.getType());
  }

  @Test
  public void shouldBeTypePos() {
    NewPrepaidTopup10 topup = new NewPrepaidTopup10();
    topup.setMerchantCode("111111111111111");

    assertEquals("Deberia ser de tipo POS", TopupType.POS, topup.getType());
  }

  @Test
  public void shouldBeFirstTopupByDefault() {
    NewPrepaidTopup10 topup = new NewPrepaidTopup10();

    assertTrue("Deberia ser 1era carga por defecto", topup.isFirstTopup());
  }

  @Test
  public void shouldBeCdtType_FirstTopup() {
    NewPrepaidTopup10 topup = new NewPrepaidTopup10();

    Assert.assertEquals("Deberia ser tipo cdt 1era carga por defecto", CdtTransactionType.SOL_1_CARGA, topup.getCdtTransactionType());

    topup.setMerchantCode("999999999999991");
    assertEquals("Deberia ser de tipo WEB", TopupType.WEB, topup.getType());
    assertEquals("Deberia ser tipo cdt 1era carga", CdtTransactionType.SOL_1_CARGA, topup.getCdtTransactionType());

    topup.setMerchantCode("111111111111111");
    assertEquals("Deberia ser de tipo POS", TopupType.POS, topup.getType());
    assertEquals("Deberia ser tipo cdt 1era carga por defecto", CdtTransactionType.SOL_1_CARGA, topup.getCdtTransactionType());
  }

  @Test
  public void shouldBeCdtType_WebTopup() {
    NewPrepaidTopup10 topup = new NewPrepaidTopup10();
    topup.setMerchantCode("999999999999991");
    topup.setFirstTopup(Boolean.FALSE);

    assertEquals("Deberia ser de tipo WEB", TopupType.WEB, topup.getType());
    assertEquals("Deberia ser tipo cdt sol carga WEB", CdtTransactionType.SOL_CARGA_WEB, topup.getCdtTransactionType());
  }

  @Test
  public void shouldBeCdtType_PosTopup() {
    NewPrepaidTopup10 topup = new NewPrepaidTopup10();
    topup.setMerchantCode("111111111111111");
    topup.setFirstTopup(Boolean.FALSE);

    assertEquals("Deberia ser de tipo POS", TopupType.POS, topup.getType());
    assertEquals("Deberia ser tipo cdt sol carga POS", CdtTransactionType.SOL_CARGA_POS, topup.getCdtTransactionType());
  }

}
