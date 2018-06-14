package cl.multicaja.test.v10.unit;

import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.model.v10.CdtTransactionType;
import org.junit.Assert;
import org.junit.Test;


import java.math.BigDecimal;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

public class Test_CdtEJBBean10 extends TestBaseUnit {

  private String accountId = String.format("PREPAGO_%s", getUniqueRutNumber());

  @Test
  public void addCdtTx() throws Exception {

    CdtTransaction10 oCdtTx10 = new CdtTransaction10();
    oCdtTx10.setAccountId(accountId);
    oCdtTx10.setTransactionReference(0L);
    oCdtTx10.setExternalTransactionId("POS"+getUniqueInteger());
    oCdtTx10.setGloss("RECARGA DE PREPAGO");
    oCdtTx10.setTransactionType(CdtTransactionType.CARGA_POS);
    oCdtTx10.setAmount(new BigDecimal(20000));
    oCdtTx10.setIndSimulacion(false);
    oCdtTx10 = getCdtEJBBean10().addCdtTransaction(null,oCdtTx10);

    Assert.assertNotNull("Debe retornar Una Tx Cdt", oCdtTx10);
    Assert.assertTrue("debe tener id", oCdtTx10.getTransactionReference() > 0);
    Assert.assertTrue("debe tener Amount", oCdtTx10.getAmount().doubleValue() > 0);
    Assert.assertNotNull("debe tener Account ID", oCdtTx10.getAccountId());
    Assert.assertNotNull("debe tener External Tx Id", oCdtTx10.getExternalTransactionId());
  }

  @Test
  public void shouldFail_CdtTransactionNull() throws Exception {
    try {
      getCdtEJBBean10().addCdtTransaction(null,null);
    } catch (ValidationException ex ) {
      Assert.assertEquals("cdtTransaction null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
    }
  }

  @Test
  public void shouldFail_AccountIdNull() throws Exception {

    try {
      CdtTransaction10 oCdtTx10 = new CdtTransaction10();
      oCdtTx10.setAccountId(null);
      oCdtTx10.setTransactionReference(0L);
      oCdtTx10.setExternalTransactionId("POS"+getUniqueInteger());
      oCdtTx10.setGloss("RECARGA DE PREPAGO");
      oCdtTx10.setTransactionType(CdtTransactionType.CARGA_POS);
      oCdtTx10.setAmount(new BigDecimal(20000));
      oCdtTx10.setIndSimulacion(Boolean.FALSE);
      getCdtEJBBean10().addCdtTransaction(null,oCdtTx10);
    } catch (ValidationException ex ) {
      Assert.assertEquals("accountId null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
    }
  }

  @Test
  public void shouldFail_AccountIdEmpty() throws Exception {

    try {
      CdtTransaction10 oCdtTx10 = new CdtTransaction10();
      oCdtTx10.setAccountId("");
      oCdtTx10.setTransactionReference(0L);
      oCdtTx10.setExternalTransactionId("POS" + getUniqueInteger());
      oCdtTx10.setGloss("RECARGA DE PREPAGO");
      oCdtTx10.setTransactionType(CdtTransactionType.CARGA_POS);
      oCdtTx10.setAmount(new BigDecimal(20000));
      oCdtTx10.setIndSimulacion(Boolean.FALSE);
      getCdtEJBBean10().addCdtTransaction(null, oCdtTx10);
    } catch (ValidationException ex) {
      Assert.assertEquals("accountId null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
    }
  }

  @Test
  public void shouldFail_TransactionTypeNull() throws Exception {

    try {
      CdtTransaction10 oCdtTx10 = new CdtTransaction10();
      oCdtTx10.setAccountId(accountId);
      oCdtTx10.setTransactionReference(0L);
      oCdtTx10.setExternalTransactionId("POS_" + getUniqueInteger());
      oCdtTx10.setGloss("RECARGA DE PREPAGO");
      oCdtTx10.setTransactionType(null);
      oCdtTx10.setAmount(new BigDecimal(20000));
      oCdtTx10.setIndSimulacion(Boolean.FALSE);
      getCdtEJBBean10().addCdtTransaction(null, oCdtTx10);
    } catch (ValidationException ex) {
      Assert.assertEquals("transactionType null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
    }
  }

  @Test
  public void shouldFail_ExternalTransactionIdNull() throws Exception {

    try {
      CdtTransaction10 oCdtTx10 = new CdtTransaction10();
      oCdtTx10.setAccountId(accountId);
      oCdtTx10.setTransactionReference(0L);
      oCdtTx10.setExternalTransactionId(null);
      oCdtTx10.setGloss("RECARGA DE PREPAGO");
      oCdtTx10.setTransactionType(CdtTransactionType.CARGA_POS);
      oCdtTx10.setAmount(new BigDecimal(20000));
      oCdtTx10.setIndSimulacion(Boolean.FALSE);
      getCdtEJBBean10().addCdtTransaction(null, oCdtTx10);
    } catch (ValidationException ex) {
      Assert.assertEquals("externalTransactionId null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
    }
  }

  @Test
  public void shouldFail_AmountNull() throws Exception {

    try {
      CdtTransaction10 oCdtTx10 = new CdtTransaction10();
      oCdtTx10.setAccountId(accountId);
      oCdtTx10.setTransactionReference(0L);
      oCdtTx10.setExternalTransactionId("POS_" + getUniqueInteger());
      oCdtTx10.setGloss("RECARGA DE PREPAGO");
      oCdtTx10.setTransactionType(CdtTransactionType.CARGA_POS);
      oCdtTx10.setAmount(null);
      oCdtTx10.setIndSimulacion(Boolean.FALSE);
      getCdtEJBBean10().addCdtTransaction(null, oCdtTx10);
    } catch (ValidationException ex) {
      Assert.assertEquals("amount null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
    }
  }

  @Test
  public void shouldFail_Amount0() throws Exception {

    try {
      CdtTransaction10 oCdtTx10 = new CdtTransaction10();
      oCdtTx10.setAccountId(accountId);
      oCdtTx10.setTransactionReference(0L);
      oCdtTx10.setExternalTransactionId("POS_" + getUniqueInteger());
      oCdtTx10.setGloss("RECARGA DE PREPAGO");
      oCdtTx10.setTransactionType(CdtTransactionType.CARGA_POS);
      oCdtTx10.setAmount(BigDecimal.valueOf(0));
      oCdtTx10.setIndSimulacion(Boolean.FALSE);
      getCdtEJBBean10().addCdtTransaction(null, oCdtTx10);
    } catch (ValidationException ex) {
      Assert.assertEquals("amount 0", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
    }
  }

  @Test
  public void shouldFail_IndSimulacionNull() throws Exception {

    try {
      CdtTransaction10 oCdtTx10 = new CdtTransaction10();
      oCdtTx10.setAccountId(accountId);
      oCdtTx10.setTransactionReference(0L);
      oCdtTx10.setExternalTransactionId("POS_" + getUniqueInteger());
      oCdtTx10.setGloss("RECARGA DE PREPAGO");
      oCdtTx10.setTransactionType(CdtTransactionType.CARGA_POS);
      oCdtTx10.setAmount(BigDecimal.valueOf(0));
      oCdtTx10.setIndSimulacion(null);
      getCdtEJBBean10().addCdtTransaction(null, oCdtTx10);
    } catch (ValidationException ex) {
      Assert.assertEquals("indSimulacion null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
    }
  }
}
