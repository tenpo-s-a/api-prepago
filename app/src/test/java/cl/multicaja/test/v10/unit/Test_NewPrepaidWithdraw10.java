package cl.multicaja.test.v10.unit;

import cl.multicaja.prepaid.model.v10.*;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @author abarazarte
 */
public class Test_NewPrepaidWithdraw10 extends TestBaseUnit {

  @Test
  public void shouldBeTypeWeb() {
    NewPrepaidWithdraw10 withdraw = new NewPrepaidWithdraw10();
    withdraw.setMerchantCode("999999999999991");

    Assert.assertEquals("Deberia ser de tipo WEB", TransactionOriginType.WEB, withdraw.getTransactionOriginType());
  }

  @Test
  public void shouldBeTypePos() {
    NewPrepaidWithdraw10 withdraw = new NewPrepaidWithdraw10();
    withdraw.setMerchantCode("111111111111111");

    assertEquals("Deberia ser de tipo POS", TransactionOriginType.POS, withdraw.getTransactionOriginType());
  }

  @Test
  public void shouldBeCdtType_WebWithdraw() {
    NewPrepaidWithdraw10 withdraw = new NewPrepaidWithdraw10();
    withdraw.setMerchantCode("999999999999991");

    assertEquals("Deberia ser de tipo WEB", TransactionOriginType.WEB, withdraw.getTransactionOriginType());
    assertEquals("Deberia ser tipo cdt sol retiro WEB", CdtTransactionType.RETIRO_WEB, withdraw.getCdtTransactionType());
  }

  @Test
  public void shouldBeCdtType_WebWithdrawConfirm() {
    NewPrepaidWithdraw10 withdraw = new NewPrepaidWithdraw10();
    withdraw.setMerchantCode("999999999999991");

    assertEquals("Deberia ser de tipo WEB", TransactionOriginType.WEB, withdraw.getTransactionOriginType());
    assertEquals("Deberia ser tipo cdt conf retiro WEB", CdtTransactionType.RETIRO_WEB_CONF, withdraw.getCdtTransactionTypeConfirm());
  }

  @Test
  public void shouldBeCdtType_PosWithdraw() {
    NewPrepaidWithdraw10 withdraw = new NewPrepaidWithdraw10();
    withdraw.setMerchantCode("111111111111111");

    assertEquals("Deberia ser de tipo POS", TransactionOriginType.POS, withdraw.getTransactionOriginType());
    assertEquals("Deberia ser tipo cdt sol retiro POS", CdtTransactionType.RETIRO_POS, withdraw.getCdtTransactionType());
  }

  @Test
  public void shouldBeCdtType_PosWithdrawConfirm() {
    NewPrepaidWithdraw10 withdraw = new NewPrepaidWithdraw10();
    withdraw.setMerchantCode("111111111111111");

    assertEquals("Deberia ser de tipo POS", TransactionOriginType.POS, withdraw.getTransactionOriginType());
    assertEquals("Deberia ser tipo cdt conf retiro POS", CdtTransactionType.RETIRO_POS_CONF, withdraw.getCdtTransactionTypeConfirm());
  }
}
