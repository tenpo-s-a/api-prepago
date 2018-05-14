package cl.multicaja.test.api.unit;

import cl.multicaja.prepaid.domain.CdtTransactionType;
import cl.multicaja.prepaid.domain.NewPrepaidTopup;
import cl.multicaja.prepaid.domain.TopupType;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author abarazarte
 */
public class Test_NewPrepaidTopup {

  @Test
  public void shouldBeTypeWeb() {
    NewPrepaidTopup topup = new NewPrepaidTopup();
    topup.setMerchantCode("999999999999991");

    Assert.assertEquals("Deberia ser de tipo WEB", TopupType.WEB, topup.getType());
  }

  @Test
  public void shouldBeTypePos() {
    NewPrepaidTopup topup = new NewPrepaidTopup();
    topup.setMerchantCode("111111111111111");

    assertEquals("Deberia ser de tipo POS", TopupType.POS, topup.getType());
  }

  @Test
  public void shouldBeFirstTopupByDefault() {
    NewPrepaidTopup topup = new NewPrepaidTopup();

    assertTrue("Deberia ser 1era carga por defecto", topup.isFirstTopup());
  }

  @Test
  public void shouldBeCdtType_FirstTopup() {
    NewPrepaidTopup topup = new NewPrepaidTopup();

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
    NewPrepaidTopup topup = new NewPrepaidTopup();
    topup.setMerchantCode("999999999999991");
    topup.setFirstTopup(Boolean.FALSE);

    assertEquals("Deberia ser de tipo WEB", TopupType.WEB, topup.getType());
    assertEquals("Deberia ser tipo cdt sol carga WEB", CdtTransactionType.SOL_CARGA_WEB, topup.getCdtTransactionType());
  }

  @Test
  public void shouldBeCdtType_PosTopup() {
    NewPrepaidTopup topup = new NewPrepaidTopup();
    topup.setMerchantCode("111111111111111");
    topup.setFirstTopup(Boolean.FALSE);

    assertEquals("Deberia ser de tipo POS", TopupType.POS, topup.getType());
    assertEquals("Deberia ser tipo cdt sol carga POS", CdtTransactionType.SOL_CARGA_POS, topup.getCdtTransactionType());
  }

}
