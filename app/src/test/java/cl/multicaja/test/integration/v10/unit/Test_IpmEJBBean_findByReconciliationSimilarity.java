package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.ejb.v10.IpmEJBBean10;
import cl.multicaja.prepaid.model.v10.IpmMovement10;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class Test_IpmEJBBean_findByReconciliationSimilarity extends TestBaseUnit {

  @Test
  public void findByReconciliationSimilarity_findOk() throws Exception {
    IpmMovement10 insertedMovement = buildIpmMovement10();
    System.out.println(String.format("reconciled: %s %b", insertedMovement.getReconciled(), insertedMovement.getReconciled()));
    insertIpmMovement(insertedMovement);

    IpmMovement10 ipmMovement10 = getIpmEJBBean10().findByReconciliationSimilarity(insertedMovement.getPan(), insertedMovement.getMerchantCode(), insertedMovement.getTransactionAmount(), insertedMovement.getApprovalCode());
    compareIpmMovements(insertedMovement, ipmMovement10);
  }

  private void compareIpmMovements(IpmMovement10 expected, IpmMovement10 found) {
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

  private void insertIpmMovement(IpmMovement10 ipmMovement10) throws Exception {
    String insertQuery = String.format(
      "INSERT INTO %s.ipm_file_data (" +
        "  file_id, " +
        "  message_type, " +
        "  function_code, " +
        "  message_reason, " +
        "  message_number, " +
        "  pan, " +
        "  transaction_amount, " +
        "  reconciliation_amount, " +
        "  cardholder_billing_amount, " +
        "  reconciliation_conversion_rate, " +
        "  cardholder_billing_conversion_rate, " +
        "  transaction_local_date, " +
        "  approval_code, " +
        "  transaction_currency_code, " +
        "  reconciliation_currency_code, " +
        "  cardholder_billing_currency_code, " +
        "  merchant_code, " +
        "  merchant_name, " +
        "  merchant_state, " +
        "  merchant_country, " +
        "  transaction_life_cycle_id, " +
        "  reconciled, " +
        "  created_at, " +
        "  updated_at " +
        ") VALUES (" +
        "  %s, " +
        "  %s, " +
        "  %s, " +
        "  %s, " +
        "  %s, " +
        "  '%s', " +
        "  %s, " +
        "  %s, " +
        "  %s, " +
        "  %s, " +
        "  %s, " +
        "  timezone('utc', now()), " +
        "  '%s', " +
        "  %s, " +
        "  %s, " +
        "  %s, " +
        "  '%s', " +
        "  '%s', " +
        "  '%s', " +
        "  '%s', " +
        "  '%s', " +
        "  %b, " +
        "  timezone('utc', now()), " +
        "  timezone('utc', now()) " +
        ")",
        getSchemaAccounting(),
        ipmMovement10.getFileId(),
        ipmMovement10.getMessageType(),
        ipmMovement10.getFunctionCode(),
        ipmMovement10.getMessageReason(),
        ipmMovement10.getMessageNumber(),
        ipmMovement10.getPan(),
        ipmMovement10.getTransactionAmount(),
        ipmMovement10.getReconciliationAmount(),
        ipmMovement10.getCardholderBillingAmount(),
        ipmMovement10.getReconciliationConversionRate(),
        ipmMovement10.getCardholderBillingConversionRate(),
        ipmMovement10.getApprovalCode(),
        ipmMovement10.getTransactionCurrencyCode(),
        ipmMovement10.getReconciliationCurrencyCode(),
        ipmMovement10.getCardholderBillingCurrencyCode(),
        ipmMovement10.getMerchantCode(),
        ipmMovement10.getMerchantName(),
        ipmMovement10.getMerchantState(),
        ipmMovement10.getMerchantCountry(),
        ipmMovement10.getTransactionLifeCycleId(),
        ipmMovement10.getReconciled()
    );

    getDbUtils().getJdbcTemplate().execute(insertQuery);
  }
}
