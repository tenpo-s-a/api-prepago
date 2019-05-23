package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.helpers.mastercard.model.IpmMessage;
import cl.multicaja.accounting.model.v10.AccountingTxType;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import org.junit.Assert;
import org.junit.Test;

public class Test_PrepaidAccountingEJBBean10_getTransactionType extends TestBaseUnit {

  @Test(expected = Exception.class)
  public void getTransactionType_null() throws Exception {
    getPrepaidAccountingEJBBean10().getTransactionType(null);
  }

  @Test(expected = Exception.class)
  public void getTransactionType_transactionCurrencyCode_null() throws Exception {
    IpmMessage ipmMessage = new IpmMessage();
    getPrepaidAccountingEJBBean10().getTransactionType(ipmMessage);
  }

  @Test(expected = Exception.class)
  public void getTransactionType_merchantName_null() throws Exception {
    IpmMessage ipmMessage = new IpmMessage();
    ipmMessage.setTransactionCurrencyCode(CodigoMoneda.CHILE_CLP.getValue());
    getPrepaidAccountingEJBBean10().getTransactionType(ipmMessage);
  }

  @Test(expected = Exception.class)
  public void getTransactionType_merchantName_empty() throws Exception {
    IpmMessage ipmMessage = new IpmMessage();
    ipmMessage.setTransactionCurrencyCode(CodigoMoneda.CHILE_CLP.getValue());
    ipmMessage.setMerchantName("");
    getPrepaidAccountingEJBBean10().getTransactionType(ipmMessage);
  }

  @Test
  public void getTransactionType_subscription() throws Exception {
    IpmMessage ipmMessage = new IpmMessage();
    ipmMessage.setTransactionCurrencyCode(CodigoMoneda.CHILE_CLP.getValue());
    ipmMessage.setMerchantName("NetFlix.CoM!2314123");
    AccountingTxType type = getPrepaidAccountingEJBBean10().getTransactionType(ipmMessage);
    Assert.assertEquals("Debe ser suscripcion", AccountingTxType.COMPRA_SUSCRIPCION, type);
  }

  @Test
  public void getTransactionType_clpPurchase() throws Exception {
    IpmMessage ipmMessage = new IpmMessage();
    ipmMessage.setTransactionCurrencyCode(CodigoMoneda.CHILE_CLP.getValue());
    ipmMessage.setMerchantName("Ebay");
    AccountingTxType type = getPrepaidAccountingEJBBean10().getTransactionType(ipmMessage);
    Assert.assertEquals("Debe ser compra en pesos", AccountingTxType.COMPRA_PESOS, type);
  }

  @Test
  public void getTransactionType_otherCurrencyPurchase() throws Exception {
    IpmMessage ipmMessage = new IpmMessage();
    ipmMessage.setTransactionCurrencyCode(CodigoMoneda.USA_USD.getValue());
    ipmMessage.setMerchantName("NetFlix.CoM!2314123");
    AccountingTxType type = getPrepaidAccountingEJBBean10().getTransactionType(ipmMessage);
    Assert.assertEquals("Debe ser compra en otra moneda", AccountingTxType.COMPRA_OTRA_MONEDA, type);
  }
}
