package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.BadRequestException;
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
    } catch (BadRequestException ex) {
      Assert.assertEquals("cdtTransaction null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
    }
  }

  @Test
  public void shouldFail_AccountIdNull() throws Exception {

    try {
      CdtTransaction10 oCdtTx10 = new CdtTransaction10();
      oCdtTx10.setAccountId(null);
      oCdtTx10.setTransactionReference(0L);
      oCdtTx10.setExternalTransactionId("POS" + getUniqueInteger());
      oCdtTx10.setGloss("RECARGA DE PREPAGO");
      oCdtTx10.setTransactionType(CdtTransactionType.CARGA_POS);
      oCdtTx10.setAmount(new BigDecimal(20000));
      oCdtTx10.setIndSimulacion(Boolean.FALSE);
      getCdtEJBBean10().addCdtTransaction(null, oCdtTx10);
      Assert.fail("No debe caer aqui");
    } catch (BadRequestException ex ) {
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
      Assert.fail("No debe caer aqui");
    } catch (BadRequestException ex) {
      Assert.assertEquals("accountId null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
    }
  }

  @Test
  public void shouldFail_TransactionReferenceNull() throws Exception {

    try {
      CdtTransaction10 oCdtTx10 = new CdtTransaction10();
      oCdtTx10.setAccountId(accountId);
      oCdtTx10.setTransactionReference(null);
      oCdtTx10.setExternalTransactionId("POS" + getUniqueInteger());
      oCdtTx10.setGloss("RECARGA DE PREPAGO");
      oCdtTx10.setTransactionType(CdtTransactionType.CARGA_POS);
      oCdtTx10.setAmount(new BigDecimal(20000));
      oCdtTx10.setIndSimulacion(Boolean.FALSE);
      getCdtEJBBean10().addCdtTransaction(null, oCdtTx10);
      Assert.fail("No debe caer aqui");
    } catch (BadRequestException ex) {
      Assert.assertEquals("transaction reference null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
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
      Assert.fail("No debe caer aqui");
    } catch (BadRequestException ex) {
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
      Assert.fail("No debe caer aqui");
    } catch (BadRequestException ex) {
      Assert.assertEquals("externalTransactionId null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
    }
  }

  @Test
  public void shouldFail_GlossNull() throws Exception {

    try {
      CdtTransaction10 oCdtTx10 = new CdtTransaction10();
      oCdtTx10.setAccountId(accountId);
      oCdtTx10.setTransactionReference(0L);
      oCdtTx10.setExternalTransactionId("POS_" + getUniqueInteger());
      oCdtTx10.setGloss(null);
      oCdtTx10.setTransactionType(CdtTransactionType.CARGA_POS);
      oCdtTx10.setAmount(new BigDecimal(20000));
      oCdtTx10.setIndSimulacion(Boolean.FALSE);
      getCdtEJBBean10().addCdtTransaction(null, oCdtTx10);
      Assert.fail("No debe caer aqui");
    } catch (BadRequestException ex) {
      Assert.assertEquals("gloss null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
    }
  }

  @Test
  public void shouldFail_GlossEmpty() throws Exception {

    try {
      CdtTransaction10 oCdtTx10 = new CdtTransaction10();
      oCdtTx10.setAccountId(accountId);
      oCdtTx10.setTransactionReference(0L);
      oCdtTx10.setExternalTransactionId("POS_" + getUniqueInteger());
      oCdtTx10.setGloss("");
      oCdtTx10.setTransactionType(CdtTransactionType.CARGA_POS);
      oCdtTx10.setAmount(new BigDecimal(20000));
      oCdtTx10.setIndSimulacion(Boolean.FALSE);
      getCdtEJBBean10().addCdtTransaction(null, oCdtTx10);
      Assert.fail("No debe caer aqui");
    } catch (BadRequestException ex) {
      Assert.assertEquals("gloss null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
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
      Assert.fail("No debe caer aqui");
    } catch (BadRequestException ex) {
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
      Assert.fail("No debe caer aqui");
    } catch (BadRequestException ex) {
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
      Assert.fail("No debe caer aqui");
    } catch (BadRequestException ex) {
      Assert.assertEquals("indSimulacion null", PARAMETRO_FALTANTE_$VALUE.getValue(), ex.getCode());
    }
  }



  @Test
  public void searchMovimientoReferencia() throws Exception {

    CdtTransaction10 oCdtTx10 = new CdtTransaction10();
    {
      oCdtTx10.setAccountId(accountId);
      oCdtTx10.setTransactionReference(0L);
      oCdtTx10.setExternalTransactionId(getRandomNumericString(10));
      oCdtTx10.setGloss("RECARGA DE PREPAGO");
      oCdtTx10.setTransactionType(CdtTransactionType.PRIMERA_CARGA);
      oCdtTx10.setAmount(new BigDecimal(20000));
      oCdtTx10.setIndSimulacion(false);
      oCdtTx10 = getCdtEJBBean10().addCdtTransaction(null, oCdtTx10);

      Assert.assertNotNull("Debe retornar Una Tx Cdt", oCdtTx10);
      Assert.assertTrue("debe tener id", oCdtTx10.getTransactionReference() > 0);
      Assert.assertTrue("debe tener Amount", oCdtTx10.getAmount().doubleValue() > 0);
      Assert.assertNotNull("debe tener Account ID", oCdtTx10.getAccountId());
      Assert.assertNotNull("debe tener External Tx Id", oCdtTx10.getExternalTransactionId());
    }
    // Busca ID de Referencia con
    {
      CdtTransaction10 oCdtTx2 = getCdtEJBBean10().buscaMovimientoReferencia(null, oCdtTx10.getTransactionReference());
      Assert.assertNotNull("Debe retornar Una Tx Cdt", oCdtTx2);
      Assert.assertTrue("debe tener id", oCdtTx2.getTransactionReference() > 0);
      Assert.assertEquals("Deben ser Iguales", oCdtTx10.getTransactionReference(), oCdtTx2.getTransactionReference());
      Assert.assertEquals("Debes tener Tipo TxPRIMERA_CARGA",CdtTransactionType.PRIMERA_CARGA,oCdtTx2.getTransactionType());
    }
  }
  @Test
  public void searchMovimientoByIdExterno() throws Exception {
    // PRUEBA ERROR PARAMETRO
    CdtTransaction10 oCdtTx10 = new CdtTransaction10();
    try{
      oCdtTx10 = getCdtEJBBean10().buscaMovimientoByIdExterno(null, null);
      Assert.fail("No debe caer aca");
    }catch (Exception e){
        Assert.assertNotNull("Debe tener error",e);
    }

    oCdtTx10 = new CdtTransaction10();
    {
      oCdtTx10.setAccountId(accountId);
      oCdtTx10.setTransactionReference(0L);
      oCdtTx10.setExternalTransactionId(getRandomNumericString(10));
      oCdtTx10.setGloss("RECARGA DE PREPAGO");
      oCdtTx10.setTransactionType(CdtTransactionType.PRIMERA_CARGA);
      oCdtTx10.setAmount(new BigDecimal(20000));
      oCdtTx10.setIndSimulacion(false);
      oCdtTx10 = getCdtEJBBean10().addCdtTransaction(null, oCdtTx10);

      Assert.assertNotNull("Debe retornar Una Tx Cdt", oCdtTx10);
      Assert.assertTrue("debe tener id", oCdtTx10.getTransactionReference() > 0);
      Assert.assertTrue("debe tener Amount", oCdtTx10.getAmount().doubleValue() > 0);
      Assert.assertNotNull("debe tener Account ID", oCdtTx10.getAccountId());
      Assert.assertNotNull("debe tener External Tx Id", oCdtTx10.getExternalTransactionId());
    }

    CdtTransaction10 oCdtTx102 = getCdtEJBBean10().buscaMovimientoByIdExterno(null,oCdtTx10.getExternalTransactionId());
    Assert.assertNotNull("Debe retornar Una Tx Cdt", oCdtTx102);
    Assert.assertEquals("Los id deben coincidir",oCdtTx10.getExternalTransactionId(),oCdtTx102.getExternalTransactionId());
    Assert.assertEquals("Los montos deben coincidir",oCdtTx10.getAmount(),oCdtTx102.getAmount());

  }

  @Test
  public void searchMovimientoByIdExternoAndTransactionType() throws Exception {
    try{
      getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, null, null);
      Assert.fail("Not Exception Trigger");
    }catch (Exception e){
      Assert.assertNotNull("Should have Error: ",e);
    }

    CdtTransaction10 oCdtTx10 = new CdtTransaction10();
    {
      oCdtTx10.setAccountId(accountId);
      oCdtTx10.setTransactionReference(0L);
      oCdtTx10.setExternalTransactionId(getRandomNumericString(10));
      oCdtTx10.setGloss("RECARGA DE PREPAGO");
      oCdtTx10.setTransactionType(CdtTransactionType.PRIMERA_CARGA);
      oCdtTx10.setAmount(new BigDecimal(20000));
      oCdtTx10.setIndSimulacion(false);
      oCdtTx10 = getCdtEJBBean10().addCdtTransaction(null, oCdtTx10);

      Assert.assertNotNull("Debe retornar Una Tx Cdt", oCdtTx10);
      Assert.assertTrue("debe tener id", oCdtTx10.getTransactionReference() > 0);
      Assert.assertTrue("debe tener Amount", oCdtTx10.getAmount().doubleValue() > 0);
      Assert.assertNotNull("debe tener Account ID", oCdtTx10.getAccountId());
      Assert.assertNotNull("debe tener External Tx Id", oCdtTx10.getExternalTransactionId());
    }

    CdtTransaction10 oCdtTx102 = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null,oCdtTx10.getExternalTransactionId(), oCdtTx10.getTransactionType());
    Assert.assertNotNull("Debe retornar Una Tx Cdt", oCdtTx102);
    Assert.assertEquals("Los id deben coincidir",oCdtTx10.getExternalTransactionId(),oCdtTx102.getExternalTransactionId());
    Assert.assertEquals("Los montos deben coincidir",oCdtTx10.getAmount(),oCdtTx102.getAmount());

  }
}
