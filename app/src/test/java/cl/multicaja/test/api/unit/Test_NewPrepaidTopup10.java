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

    Assert.assertEquals("Deberia ser de tipo WEB", topup.getType(), TopupType.WEB);
  }

  @Test
  public void shouldBeTypePos() {
    NewPrepaidTopup10 topup = new NewPrepaidTopup10();
    topup.setMerchantCode("111111111111111");

    assertEquals("Deberia ser de tipo POS", topup.getType(), TopupType.POS);
  }

  @Test
  public void shouldBeFirstTopupByDefault() {
    NewPrepaidTopup10 topup = new NewPrepaidTopup10();

    assertTrue("Deberia ser 1era carga por defecto", topup.isFirstTopup());
  }

  @Test
  public void shouldBeCdtType_FirstTopup() {
    NewPrepaidTopup10 topup = new NewPrepaidTopup10();

    Assert.assertEquals("Deberia ser tipo cdt 1era carga por defecto", topup.getCdtTransactionType(), CdtTransactionType.SOL_1_CARGA);

    topup.setMerchantCode("999999999999991");
    assertEquals("Deberia ser de tipo WEB", topup.getType(), TopupType.WEB);
    assertEquals("Deberia ser tipo cdt 1era carga", topup.getCdtTransactionType(), CdtTransactionType.SOL_1_CARGA);

    topup.setMerchantCode("111111111111111");
    assertEquals("Deberia ser de tipo POS", topup.getType(), TopupType.POS);
    assertEquals("Deberia ser tipo cdt 1era carga por defecto", topup.getCdtTransactionType(), CdtTransactionType.SOL_1_CARGA);
  }

  @Test
  public void shouldBeCdtType_WebTopup() {
    NewPrepaidTopup10 topup = new NewPrepaidTopup10();
    topup.setMerchantCode("999999999999991");
    topup.setFirstTopup(Boolean.FALSE);

    assertEquals("Deberia ser de tipo WEB", topup.getType(), TopupType.WEB);
    assertEquals("Deberia ser tipo cdt sol carga WEB", topup.getCdtTransactionType(), CdtTransactionType.SOL_CARGA_WEB);
  }

  @Test
  public void shouldBeCdtType_PosTopup() {
    NewPrepaidTopup10 topup = new NewPrepaidTopup10();
    topup.setMerchantCode("111111111111111");
    topup.setFirstTopup(Boolean.FALSE);

    assertEquals("Deberia ser de tipo POS", topup.getType(), TopupType.POS);
    assertEquals("Deberia ser tipo cdt sol carga POS", topup.getCdtTransactionType(), CdtTransactionType.SOL_CARGA_POS);
  }

}
