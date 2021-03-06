package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.prepaid.ejb.v10.IpmEJBBean10;
import cl.multicaja.prepaid.helpers.fees.FeeService;
import cl.multicaja.prepaid.helpers.fees.model.Charge;
import cl.multicaja.prepaid.helpers.fees.model.ChargeType;
import cl.multicaja.prepaid.helpers.fees.model.Fee;
import cl.multicaja.prepaid.model.v10.IpmMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v11.Account;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

public class Test_IpmEJBBean10_findByReconciliationSimilarity extends TestBaseUnit {

  @Before
  @After
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.ipm_file_data CASCADE", getSchemaAccounting()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.clearing CASCADE", getSchemaAccounting()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting CASCADE", getSchemaAccounting()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento_comision CASCADE", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento_investigar CASCADE", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimientos_tecnocom CASCADE", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_archivos_conciliacion CASCADE", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_tarjeta CASCADE", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_cuenta CASCADE", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_usuario CASCADE", getSchema()));
    // todo: limpiar lista de cont liq y fees
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
    veryLowMovement.setCardholderBillingAmount(insertedMovement.getTransactionAmount().multiply(new BigDecimal(0.90f)));
    createIpmMovement(veryLowMovement);

    // Inserta un movimiento que tiene el 99.0% del valor original
    IpmMovement10 lowMovement = buildIpmMovement10();
    lowMovement.setPan(insertedMovement.getPan());
    lowMovement.setMerchantCode(insertedMovement.getMerchantCode());
    lowMovement.setApprovalCode(insertedMovement.getApprovalCode());
    lowMovement.setCardholderBillingAmount(insertedMovement.getTransactionAmount().multiply(new BigDecimal(0.99f)));
    createIpmMovement(lowMovement);

    // Inserta un movimiento que tiene el 99.1% del valor original, PERO ya esta conciliado, por lo que no deberia ser elegido
    IpmMovement10 lowReconciledMovement = buildIpmMovement10();
    lowReconciledMovement.setPan(insertedMovement.getPan());
    lowReconciledMovement.setMerchantCode(insertedMovement.getMerchantCode());
    lowReconciledMovement.setApprovalCode(insertedMovement.getApprovalCode());
    lowReconciledMovement.setCardholderBillingAmount(insertedMovement.getTransactionAmount().multiply(new BigDecimal(0.991f)));
    lowReconciledMovement.setReconciled(true);
    createIpmMovement(lowReconciledMovement);

    // Inserta un movimiento que tiene el 110% del valor original
    IpmMovement10 veryHighMovement = buildIpmMovement10();
    veryHighMovement.setPan(insertedMovement.getPan());
    veryHighMovement.setMerchantCode(insertedMovement.getMerchantCode());
    veryHighMovement.setApprovalCode(insertedMovement.getApprovalCode());
    veryHighMovement.setCardholderBillingAmount(insertedMovement.getTransactionAmount().multiply(new BigDecimal(1.10f)));
    createIpmMovement(veryHighMovement);

    // Inserta un movimiento que tiene el 101% del valor original
    IpmMovement10 highMovement = buildIpmMovement10();
    highMovement.setPan(insertedMovement.getPan());
    highMovement.setMerchantCode(insertedMovement.getMerchantCode());
    highMovement.setApprovalCode(insertedMovement.getApprovalCode());
    highMovement.setCardholderBillingAmount(insertedMovement.getTransactionAmount().multiply(new BigDecimal(1.01f)));
    createIpmMovement(highMovement);

    // Buscamos un valor que sea cerca al 99.2% del valor original
    IpmMovement10 ipmMovement10 = getIpmEJBBean10().findByReconciliationSimilarity(insertedMovement.getPan(), insertedMovement.getMerchantCode(), insertedMovement.getTransactionAmount().multiply(new BigDecimal(0.992)), insertedMovement.getApprovalCode());

    // Debe encontrar el lowMovement (99.0%) como mas cercano y no conciliado
    compareIpmMovements(lowMovement, ipmMovement10);
  }

  @Test
  public void findWithRealData() throws Exception {
    // Preparar archivo
    getDbUtils().getJdbcTemplate().execute(String.format("INSERT INTO %s.prp_archivos_conciliacion " +
      "(nombre_de_archivo, proceso, tipo, status, created_at, updated_at) " +
      "VALUES('test.txt', 'TECNOCOM', 'TECNOCOM_FILE', 'OK', timezone('utc', now()), timezone('utc', now()));", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("UPDATE %s.prp_archivos_conciliacion SET id = 3 WHERE nombre_de_archivo = 'test.txt'", getSchema()));

    // Insertar movimientos ipm
    String resource = "src/test/resources/mastercard/files/ipm/ipm.toMakeMatch.SQL_INSERT";
    String content = String.format(new Scanner(new File(resource)).useDelimiter("\\Z").next(), getSchemaAccounting());
    getDbUtils().getJdbcTemplate().execute(content);

    // Insertar movimientos tecnocom
    resource = "src/test/resources/tecnocom/files/PLJ61110.toMakeMatch.SQL_INSERT";
    content = new Scanner(new File(resource)).useDelimiter("\\Z").next().replaceAll("%s", getSchema());
    getDbUtils().getJdbcTemplate().execute(content);

    // Crear user
    PrepaidUser10 prepaidUser10= buildPrepaidUserv2();
    prepaidUser10 = createPrepaidUserV2(prepaidUser10);
    Account account = buildAccountFromTecnocom(prepaidUser10);
    account = createAccount(account.getUserId(),account.getAccountNumber());
    PrepaidCard10 card = buildPrepaidCardWithTecnocomData(prepaidUser10,account);
    card = createPrepaidCardV2(card);

    // Setear que todos los movimientos pertenezcan a esta tarjeta
    getDbUtils().getJdbcTemplate().execute(String.format("UPDATE %s.prp_movimientos_tecnocom SET pan = '%s', contrato = '%s'", getSchema(), card.getHashedPan(), account.getAccountNumber()));
    getDbUtils().getJdbcTemplate().execute(String.format("UPDATE %s.ipm_file_data SET pan = '%s'", getSchemaAccounting(), card.getPan()));

    // Prepara un mock de fees muy inutil
    prepareCalculateFeesMock();

    // Ejecutar proceso
    getTecnocomReconciliationEJBBean10().processTecnocomTableData(3L);

    // Debe haber 2 movimientos IPM conciliados porque hicieron match, (los otros 2 son de tipo factura desconocido asi que se ignoran)
    List<Map<String, Object>> foundList = getDbUtils().getJdbcTemplate().queryForList(String.format("SELECT * FROM %s.ipm_file_data WHERE reconciled = true", getSchemaAccounting()));
    Assert.assertEquals("Debe haber 2 movimientos ipm conciliados", 2, foundList.size());
  }

  private void prepareCalculateFeesMock() throws TimeoutException, BaseException {
    // Prepara un mock del servicio de fees
    Charge prepaidCharge = new Charge();
    prepaidCharge.setChargeType(ChargeType.COMMISSION);
    prepaidCharge.setAmount(10L);

    List<Charge> chargesList = new ArrayList<>();
    chargesList.add(prepaidCharge);

    // Prepara una fee esperada para que devuelva el servicio
    Fee returnedFee = new Fee();
    returnedFee.setTotal(10L);
    returnedFee.setCharges(chargesList);

    // Setea que calculaFees() como mock para que devuelva la fee esperada
    FeeService mockFeeService = Mockito.mock(FeeService.class);
    Mockito.doReturn(returnedFee).when(mockFeeService).calculateFees(Mockito.any(), Mockito.any(), Mockito.any());
    getTecnocomReconciliationEJBBean10().setFeeService(mockFeeService);
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
