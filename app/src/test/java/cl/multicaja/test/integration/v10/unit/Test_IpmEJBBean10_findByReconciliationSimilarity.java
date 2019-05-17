package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.ejb.v10.IpmEJBBean10;
import cl.multicaja.prepaid.model.v10.IpmMovement10;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.List;

public class Test_IpmEJBBean10_findByReconciliationSimilarity extends TestBaseUnit {

  @Before
  @After
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.ipm_file_data CASCADE", getSchemaAccounting()));
  }

  @Test
  public void findByReconciliationSimilarity_findOk() throws Exception {
    // Inserta un movimiento original (100%)
    IpmMovement10 insertedMovement = buildIpmMovement10();
    createIpmMovement(insertedMovement);

    // Inserta un movimiento que tiene el 90% del valor original
    IpmMovement10 veryLowMovement = buildIpmMovement10();
    veryLowMovement.setPan(insertedMovement.getPan());
    veryLowMovement.setMerchantCode(insertedMovement.getMerchantCode());
    veryLowMovement.setApprovalCode(insertedMovement.getApprovalCode());
    veryLowMovement.setTransactionAmount(insertedMovement.getTransactionAmount().multiply(new BigDecimal(0.90f)));
    createIpmMovement(veryLowMovement);

    // Inserta un movimiento que tiene el 99.0% del valor original
    IpmMovement10 lowMovement = buildIpmMovement10();
    lowMovement.setPan(insertedMovement.getPan());
    lowMovement.setMerchantCode(insertedMovement.getMerchantCode());
    lowMovement.setApprovalCode(insertedMovement.getApprovalCode());
    lowMovement.setTransactionAmount(insertedMovement.getTransactionAmount().multiply(new BigDecimal(0.99f)));
    createIpmMovement(lowMovement);

    // Inserta un movimiento que tiene el 99.1% del valor original, PERO ya esta conciliado, por lo que no deberia ser elegido
    IpmMovement10 lowReconciledMovement = buildIpmMovement10();
    lowReconciledMovement.setPan(insertedMovement.getPan());
    lowReconciledMovement.setMerchantCode(insertedMovement.getMerchantCode());
    lowReconciledMovement.setApprovalCode(insertedMovement.getApprovalCode());
    lowReconciledMovement.setTransactionAmount(insertedMovement.getTransactionAmount().multiply(new BigDecimal(0.991f)));
    lowReconciledMovement.setReconciled(true);
    createIpmMovement(lowReconciledMovement);

    // Inserta un movimiento que tiene el 110% del valor original
    IpmMovement10 veryHighMovement = buildIpmMovement10();
    veryHighMovement.setPan(insertedMovement.getPan());
    veryHighMovement.setMerchantCode(insertedMovement.getMerchantCode());
    veryHighMovement.setApprovalCode(insertedMovement.getApprovalCode());
    veryHighMovement.setTransactionAmount(insertedMovement.getTransactionAmount().multiply(new BigDecimal(1.10f)));
    createIpmMovement(veryHighMovement);

    // Inserta un movimiento que tiene el 101% del valor original
    IpmMovement10 highMovement = buildIpmMovement10();
    highMovement.setPan(insertedMovement.getPan());
    highMovement.setMerchantCode(insertedMovement.getMerchantCode());
    highMovement.setApprovalCode(insertedMovement.getApprovalCode());
    highMovement.setTransactionAmount(insertedMovement.getTransactionAmount().multiply(new BigDecimal(1.01f)));
    createIpmMovement(highMovement);

    // Buscamos un valor que sea cerca al 99.2% del valor original
    IpmMovement10 ipmMovement10 = getIpmEJBBean10().findByReconciliationSimilarity(insertedMovement.getPan(), insertedMovement.getMerchantCode(), insertedMovement.getTransactionAmount().multiply(new BigDecimal(0.992)), insertedMovement.getApprovalCode());

    // Debe encontrar el lowMovement (99.0%) como mas cercano y no conciliado
    compareIpmMovements(lowMovement, ipmMovement10);
  }

  public void compareIpmMovements(IpmMovement10 expected, IpmMovement10 found) {
    Assert.assertNotNull("Debe existir", found);
    Assert.assertEquals("Debe tener mismo file_id", expected.getFileId(), found.getFileId());
    Assert.assertEquals("Debe tener mismo message_type", expected.getMessageType(), found.getMessageType());
    Assert.assertEquals("Debe tener mismo function_code", expected.getFunctionCode(), found.getFunctionCode());
    Assert.assertEquals("Debe tener mismo message_reason", expected.getMessageReason(), found.getMessageReason());
    Assert.assertEquals("Debe tener mismo message_number", expected.getMessageNumber(), found.getMessageNumber());
    Assert.assertEquals("Debe tener mismo pan", expected.getPan(), found.getPan());
    Assert.assertEquals("Debe tener mismo transaction_amount", expected.getTransactionAmount(), found.getTransactionAmount());
    Assert.assertEquals("Debe tener mismo reconciliation_amount", expected.getReconciliationAmount(), found.getReconciliationAmount());
    Assert.assertEquals("Debe tener mismo cardholder_billing_amount", expected.getCardholderBillingAmount(), found.getCardholderBillingAmount());
    Assert.assertEquals("Debe tener mismo reconciliation_conversion_rate", expected.getReconciliationConversionRate(), found.getReconciliationConversionRate());
    Assert.assertEquals("Debe tener mismo cardholder_billing_conversion_rate", expected.getCardholderBillingConversionRate(), found.getCardholderBillingConversionRate());
    Assert.assertTrue("Debe tener mismo transaction_local_date", isRecentLocalDateTime(found.getTransactionLocalDate(), 5));
    Assert.assertEquals("Debe tener mismo ", expected.getPan(), found.getPan());
    Assert.assertEquals("Debe tener mismo approval_code", expected.getApprovalCode(), found.getApprovalCode());
    Assert.assertEquals("Debe tener mismo transaction_currency_code", expected.getTransactionCurrencyCode(), found.getTransactionCurrencyCode());
    Assert.assertEquals("Debe tener mismo reconciliation_currency_code", expected.getReconciliationCurrencyCode(), found.getReconciliationCurrencyCode());
    Assert.assertEquals("Debe tener mismo cardholder_billing_currency_code", expected.getCardholderBillingCurrencyCode(), found.getCardholderBillingCurrencyCode());
    Assert.assertEquals("Debe tener mismo merchant_code", expected.getMerchantCode(), found.getMerchantCode());
    Assert.assertEquals("Debe tener mismo merchant_name", expected.getMerchantName(), found.getMerchantName());
    Assert.assertEquals("Debe tener mismo merchant_state", expected.getMerchantState(), found.getMerchantState());
    Assert.assertEquals("Debe tener mismo merchant_country", expected.getMerchantCountry(), found.getMerchantCountry());
    Assert.assertEquals("Debe tener mismo transaction_life_cycle_id", expected.getTransactionLifeCycleId(), found.getTransactionLifeCycleId());
    Assert.assertEquals("Debe tener mismo reconciled", expected.getReconciled(), found.getReconciled());
    Assert.assertTrue("Debe tener mismo created_at", isRecentLocalDateTime(found.getTimestamps().getCreatedAt(), 5));
    Assert.assertTrue("Debe tener mismo updated_at", isRecentLocalDateTime(found.getTimestamps().getUpdatedAt(), 5));
  }
}
