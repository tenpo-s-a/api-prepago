package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import org.junit.*;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.util.List;
//TODO: Corregir tests ignorados cuando se trabaje el retiro web
@Ignore
public class Test_PrepaidMovementEJBBean10_processClearingResolution extends TestBaseUnit {

  @Before
  @After
  public void after() {
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento_investigar CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento_conciliado CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento CASCADE", getSchema()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.clearing CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting CASCADE", getSchemaAccounting()));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting_files CASCADE", getSchemaAccounting()));
  }

  @Ignore
  @Test
  public void processClearingResolution_allOK() throws Exception {
    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    UserAccount userAccount = randomBankAccount();

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdrawV2();
    prepaidWithdraw.setMerchantCode(NewPrepaidWithdraw10.WEB_MERCHANT_CODE);
    //prepaidWithdraw.setBankAccountId(userAccount.getId());
    prepaidWithdraw.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
    prepaidWithdraw.setTotal(new NewAmountAndCurrency10(new BigDecimal(10000L)));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidWithdraw);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidWithdraw, prepaidCard10, cdtTransaction, PrepaidMovementType.WITHDRAW);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement10.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    AccountingData10 accountingData10 = buildRandomAccouting();
    accountingData10.setAccountingMovementType(AccountingMovementType.RETIRO_WEB);
    accountingData10.setIdTransaction(prepaidMovement10.getId());
    accountingData10.setType(AccountingTxType.RETIRO_WEB);
    accountingData10.setStatus(AccountingStatusType.PENDING);
    getPrepaidAccountingEJBBean10().saveAccountingData(null, accountingData10);

    ClearingData10 clearingData10 = new ClearingData10();
    clearingData10.setAccountingId(accountingData10.getId());
    clearingData10.setStatus(AccountingStatusType.OK);
    clearingData10.setUserBankAccount(userAccount);
    clearingData10.setIdTransaction(prepaidMovement10.getId());
    getPrepaidClearingEJBBean10().insertClearingData(null, clearingData10);

    //OK
    getPrepaidMovementEJBBean10().processClearingResolution(clearingData10);

    // Revisar que haya confirmado en el cdt
    CdtTransaction10 foundCdtTransation = getCdtEJBBean10().buscaMovimientoByIdExternoAndTransactionType(null, prepaidMovement10.getIdTxExterno(), CdtTransactionType.RETIRO_WEB_CONF);
    Assert.assertNotNull("Debe existir la confirmacion en el cdt", foundCdtTransation);
    Assert.assertEquals("Deben ser por montos iguales", cdtTransaction.getAmount(), foundCdtTransation.getAmount());

    // Revisar que se confirme el estado del negocio
    PrepaidMovement10 foundMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
    Assert.assertEquals("Debe tener estado de nogocio confirmed", BusinessStatusType.CONFIRMED, foundMovement.getEstadoNegocio());

    // Revisar que el estado de accounting haya cambiado a OK
    AccountingData10 foundAccounting = getAccountingData(accountingData10.getId());
    Assert.assertEquals("Debe tener estado PENDING", AccountingStatusType.PENDING, foundAccounting.getStatus());
    Assert.assertEquals("Debe tener estado OK", AccountingStatusType.OK, foundAccounting.getAccountingStatus());

    // El movimiento debe quedar conciliado para que no vuelva a ser procesado
    ReconciliedMovement10 reconciliedMovement10 = getReconciliedMovement(prepaidMovement10.getId());
    Assert.assertEquals("Debe tener estado reconciled", ReconciliationStatusType.RECONCILED, reconciliedMovement10.getReconciliationStatusType());
    Assert.assertEquals("Debe tener accion none", ReconciliationActionType.NONE, reconciliedMovement10.getActionType());
  }

  @Test(expected = BadRequestException.class)
  public void processClearingResolution_badRequest_clearingNull() throws Exception {
    getPrepaidMovementEJBBean10().processClearingResolution(null);
  }

  @Ignore
  @Test(expected = ValidationException.class)
  public void processClearingResolution_ValidationException_notWithdraw() throws Exception {
    UserAccount userAccount = randomBankAccount();

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);


    PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdrawV2();
    prepaidWithdraw.setMerchantCode(NewPrepaidWithdraw10.WEB_MERCHANT_CODE);
    prepaidWithdraw.setBankId(userAccount.getBankId());
    prepaidWithdraw.setAccountNumber(userAccount.getAccountNumber());
    prepaidWithdraw.setAccountType(userAccount.getAccountType());
    prepaidWithdraw.setAccountRut(userAccount.getRut());
    prepaidWithdraw.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
    prepaidWithdraw.setTotal(new NewAmountAndCurrency10(new BigDecimal(10000L)));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidWithdraw);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidWithdraw, prepaidCard10, cdtTransaction, PrepaidMovementType.TOPUP);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement10.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    ClearingData10 clearingData10 = new ClearingData10();
    clearingData10.setIdTransaction(prepaidMovement10.getId());

    getPrepaidMovementEJBBean10().processClearingResolution(clearingData10);
  }

  @Ignore
  @Test(expected = ValidationException.class)
  public void processClearingResolution_ValidationException_notWeb() throws Exception {
    UserAccount userAccount = randomBankAccount();

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdrawV2();
    prepaidWithdraw.setMerchantCode(getRandomNumericString(5));
    prepaidWithdraw.setBankId(userAccount.getBankId());
    prepaidWithdraw.setAccountNumber(userAccount.getAccountNumber());
    prepaidWithdraw.setAccountType(userAccount.getAccountType());
    prepaidWithdraw.setAccountRut(userAccount.getRut());
    prepaidWithdraw.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
    prepaidWithdraw.setTotal(new NewAmountAndCurrency10(new BigDecimal(10000L)));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidWithdraw);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidWithdraw, prepaidCard10, cdtTransaction, PrepaidMovementType.WITHDRAW);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement10.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    ClearingData10 clearingData10 = new ClearingData10();
    clearingData10.setIdTransaction(prepaidMovement10.getId());

    getPrepaidMovementEJBBean10().processClearingResolution(clearingData10);
  }

  @Ignore
  @Test
  public void processClearingResolution_movement_notProcessOK() throws Exception {

    UserAccount userAccount = randomBankAccount();

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdrawV2();
    prepaidWithdraw.setMerchantCode(NewPrepaidWithdraw10.WEB_MERCHANT_CODE);
    prepaidWithdraw.setBankId(userAccount.getBankId());
    prepaidWithdraw.setAccountNumber(userAccount.getAccountNumber());
    prepaidWithdraw.setAccountType(userAccount.getAccountType());
    prepaidWithdraw.setAccountRut(userAccount.getRut());
    prepaidWithdraw.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
    prepaidWithdraw.setTotal(new NewAmountAndCurrency10(new BigDecimal(10000L)));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidWithdraw);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidWithdraw, prepaidCard10, cdtTransaction, PrepaidMovementType.WITHDRAW);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setEstado(PrepaidMovementStatus.REJECTED);
    prepaidMovement10.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    AccountingFiles10 clearingFile = new AccountingFiles10();
    clearingFile.setName("accountingFile1");
    clearingFile.setFileId("some_id2");
    clearingFile.setFileType(AccountingFileType.CLEARING);
    clearingFile.setStatus(AccountingStatusType.OK);
    clearingFile.setFileFormatType(AccountingFileFormatType.CSV);
    clearingFile = getPrepaidAccountingFileEJBBean10().insertAccountingFile(null, clearingFile);

    ClearingData10 clearingData10 = new ClearingData10();
    clearingData10.setIdTransaction(prepaidMovement10.getId());
    clearingData10.setFileId(clearingFile.getId());

    // Testeamos, deberia rechazar por movimiento no process_ok
    getPrepaidMovementEJBBean10().processClearingResolution(clearingData10);

    // Debe existir el movimiento conciliado
    ReconciliedMovement10 reconciliedMovement10 = getReconciliedMovement(prepaidMovement10.getId());

    Assert.assertNotNull("Debe existir en conciliados", reconciliedMovement10);
    Assert.assertEquals("Debe estr en estado refund", ReconciliationStatusType.NEED_VERIFICATION, reconciliedMovement10.getReconciliationStatusType());
    Assert.assertEquals("Debe tener accion refund", ReconciliationActionType.INVESTIGACION, reconciliedMovement10.getActionType());

    List<ResearchMovement10> researchMovs = getResearchMovement(prepaidMovement10.getId());
    Assert.assertNotNull("Debe haber una respuesta", researchMovs);
    Assert.assertEquals("Debe haber un solo movimiento a investigar", 1, researchMovs.size());
  }

  @Ignore
  @Test
  public void processClearingResolution_TecnocomStatus_NotReconciled() throws Exception {
    UserAccount userAccount = randomBankAccount();

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdrawV2();
    prepaidWithdraw.setMerchantCode(NewPrepaidWithdraw10.WEB_MERCHANT_CODE);
    prepaidWithdraw.setBankId(userAccount.getBankId());
    prepaidWithdraw.setAccountNumber(userAccount.getAccountNumber());
    prepaidWithdraw.setAccountType(userAccount.getAccountType());
    prepaidWithdraw.setAccountRut(userAccount.getRut());
    prepaidWithdraw.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
    prepaidWithdraw.setTotal(new NewAmountAndCurrency10(new BigDecimal(10000L)));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidWithdraw);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidWithdraw, prepaidCard10, cdtTransaction, PrepaidMovementType.WITHDRAW);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.NOT_RECONCILED); // No conciliado con tecnocom
    prepaidMovement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement10.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    AccountingData10 accountingData10 = buildRandomAccouting();
    accountingData10.setAccountingMovementType(AccountingMovementType.RETIRO_WEB);
    accountingData10.setIdTransaction(prepaidMovement10.getId());
    accountingData10.setType(AccountingTxType.RETIRO_WEB);
    accountingData10.setStatus(AccountingStatusType.PENDING);
    getPrepaidAccountingEJBBean10().saveAccountingData(null, accountingData10);

    ClearingData10 clearingData10 = new ClearingData10();
    clearingData10.setAccountingId(accountingData10.getId());
    clearingData10.setStatus(AccountingStatusType.OK);
    clearingData10.setUserBankAccount(userAccount);
    clearingData10.setIdTransaction(prepaidMovement10.getId());

    // No conciliado con tecnocom
    getPrepaidMovementEJBBean10().processClearingResolution(clearingData10);

    // Debe existir el movimiento conciliado
    ReconciliedMovement10 reconciliedMovement10 = getReconciliedMovement(prepaidMovement10.getId());

    Assert.assertNotNull("Debe existir en conciliados", reconciliedMovement10);
    Assert.assertEquals("Debe estar en estado refund", ReconciliationStatusType.NEED_VERIFICATION, reconciliedMovement10.getReconciliationStatusType());
    Assert.assertEquals("Debe tener accion refund", ReconciliationActionType.INVESTIGACION, reconciliedMovement10.getActionType());

    List<ResearchMovement10> researchMovs = getResearchMovement(prepaidMovement10.getId());
    Assert.assertNotNull("Debe haber una respuesta", researchMovs);
    Assert.assertEquals("Debe haber un solo movimiento a investigar", 1, researchMovs.size());
    ResearchMovement10 researchMovement = researchMovs.get(0);
    Assert.assertEquals("Debe venir de la resolucion", ReconciliationOriginType.CLEARING_RESOLUTION, researchMovement.getOriginType());
  }

  @Ignore
  @Test
  public void processClearingResolution_BankStatus_NotInFile() throws Exception {

    UserAccount userAccount = randomBankAccount();

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdrawV2();
    prepaidWithdraw.setMerchantCode(NewPrepaidWithdraw10.WEB_MERCHANT_CODE);
    prepaidWithdraw.setBankId(userAccount.getBankId());
    prepaidWithdraw.setAccountNumber(userAccount.getAccountNumber());
    prepaidWithdraw.setAccountType(userAccount.getAccountType());
    prepaidWithdraw.setAccountRut(userAccount.getRut());
    prepaidWithdraw.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
    prepaidWithdraw.setTotal(new NewAmountAndCurrency10(new BigDecimal(10000L)));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidWithdraw);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidWithdraw, prepaidCard10, cdtTransaction, PrepaidMovementType.WITHDRAW);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement10.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    AccountingData10 accountingData10 = buildRandomAccouting();
    accountingData10.setAccountingMovementType(AccountingMovementType.RETIRO_WEB);
    accountingData10.setIdTransaction(prepaidMovement10.getId());
    accountingData10.setType(AccountingTxType.RETIRO_WEB);
    accountingData10.setStatus(AccountingStatusType.PENDING);
    getPrepaidAccountingEJBBean10().saveAccountingData(null, accountingData10);

    ClearingData10 clearingData10 = new ClearingData10();
    clearingData10.setId(getUniqueLong());
    clearingData10.setAccountingId(accountingData10.getId());
    clearingData10.setStatus(AccountingStatusType.NOT_IN_FILE); // No vino en el archivo del banco
    clearingData10.setUserBankAccount(userAccount);
    clearingData10.setIdTransaction(prepaidMovement10.getId());

    // No vino en el archivo del banco
    getPrepaidMovementEJBBean10().processClearingResolution(clearingData10);

    // Debe existir el movimiento conciliado
    ReconciliedMovement10 reconciliedMovement10 = getReconciliedMovement(prepaidMovement10.getId());

    Assert.assertNotNull("Debe existir en conciliados", reconciliedMovement10);
    Assert.assertEquals("Debe estar en estado refund", ReconciliationStatusType.NEED_VERIFICATION, reconciliedMovement10.getReconciliationStatusType());
    Assert.assertEquals("Debe tener accion refund", ReconciliationActionType.INVESTIGACION, reconciliedMovement10.getActionType());

    List<ResearchMovement10> researchMovs = getResearchMovement(prepaidMovement10.getId());
    Assert.assertNotNull("Debe haber una respuesta", researchMovs);
    Assert.assertEquals("Debe haber un solo movimiento a investigar", 1, researchMovs.size());
    ResearchMovement10 researchMovement = researchMovs.get(0);
    Assert.assertEquals("Debe venir de la resolucion", ReconciliationOriginType.CLEARING_RESOLUTION, researchMovement.getOriginType());
  }

  @Ignore
  @Test
  public void processClearingResolution_BankStatus_InvalidInformation() throws Exception {
    UserAccount userAccount = randomBankAccount();

    PrepaidUser10 prepaidUser = buildPrepaidUserv2();
    prepaidUser = createPrepaidUserV2(prepaidUser);

    Account account = buildAccountFromTecnocom(prepaidUser);
    account = createAccount(account.getUserId(),account.getAccountNumber());

    PrepaidCard10 prepaidCard10 = buildPrepaidCardWithTecnocomData(prepaidUser, account);
    prepaidCard10 = createPrepaidCardV2(prepaidCard10);

    PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdrawV2();
    prepaidWithdraw.setMerchantCode(NewPrepaidWithdraw10.WEB_MERCHANT_CODE);
    prepaidWithdraw.setBankId(userAccount.getBankId());
    prepaidWithdraw.setAccountNumber(userAccount.getAccountNumber());
    prepaidWithdraw.setAccountType(userAccount.getAccountType());
    prepaidWithdraw.setAccountRut(userAccount.getRut());
    prepaidWithdraw.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
    prepaidWithdraw.setTotal(new NewAmountAndCurrency10(new BigDecimal(10000L)));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(prepaidUser, prepaidWithdraw);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidWithdraw, prepaidCard10, cdtTransaction, PrepaidMovementType.WITHDRAW);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement10.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    AccountingData10 accountingData10 = buildRandomAccouting();
    accountingData10.setAccountingMovementType(AccountingMovementType.RETIRO_WEB);
    accountingData10.setIdTransaction(prepaidMovement10.getId());
    accountingData10.setType(AccountingTxType.RETIRO_WEB);
    accountingData10.setStatus(AccountingStatusType.PENDING);
    getPrepaidAccountingEJBBean10().saveAccountingData(null, accountingData10);

    AccountingFiles10 clearingFile = new AccountingFiles10();
    clearingFile.setName("accountingFile1");
    clearingFile.setFileId("some_id");
    clearingFile.setFileType(AccountingFileType.CLEARING);
    clearingFile.setStatus(AccountingStatusType.OK);
    clearingFile.setFileFormatType(AccountingFileFormatType.CSV);
    clearingFile = getPrepaidAccountingFileEJBBean10().insertAccountingFile(null, clearingFile);

    ClearingData10 clearingData10 = new ClearingData10();
    clearingData10.setId(getUniqueLong());
    clearingData10.setFileId(clearingFile.getId());
    clearingData10.setAccountingId(accountingData10.getId());
    clearingData10.setStatus(AccountingStatusType.INVALID_INFORMATION); // Los montos no concuerdan
    clearingData10.setUserBankAccount(userAccount);
    clearingData10.setIdTransaction(prepaidMovement10.getId());

    // No vino en el archivo del banco
    getPrepaidMovementEJBBean10().processClearingResolution(clearingData10);

    // Debe existir el movimiento conciliado
    ReconciliedMovement10 reconciliedMovement10 = getReconciliedMovement(prepaidMovement10.getId());

    Assert.assertNotNull("Debe existir en conciliados", reconciliedMovement10);
    Assert.assertEquals("Debe estar en estado necesita verificacion", ReconciliationStatusType.NEED_VERIFICATION, reconciliedMovement10.getReconciliationStatusType());
    Assert.assertEquals("Debe tener accion research", ReconciliationActionType.INVESTIGACION, reconciliedMovement10.getActionType());

    List<ResearchMovement10> researchMovs = getResearchMovement(prepaidMovement10.getId());
    Assert.assertNotNull("Debe haber una respuesta", researchMovs);
    Assert.assertEquals("Debe haber un solo movimiento a investigar", 1, researchMovs.size());
    ResearchMovement10 researchMovemen = researchMovs.get(0);
    Assert.assertEquals("Debe venir de la resolucion", ReconciliationOriginType.CLEARING_RESOLUTION, researchMovemen.getOriginType());
  }

  private List<ResearchMovement10> getResearchMovement(Long movId) throws Exception{
    return getPrepaidMovementEJBBean10().getResearchMovementByMovRef(numberUtils.toBigDecimal(movId));
  }

  private ReconciliedMovement10 getReconciliedMovement(Long idMov) {

    RowMapper rowMapper = (rs, rowNum) -> {
      ReconciliedMovement10 reconciliedMovement10 = new ReconciliedMovement10();
      reconciliedMovement10.setId(numberUtils.toLong(rs.getLong("id")));
      reconciliedMovement10.setIdMovRef(numberUtils.toLong(rs.getLong("id_mov_ref")));
      reconciliedMovement10.setReconciliationStatusType(ReconciliationStatusType.fromValue(String.valueOf(rs.getString("estado"))));
      reconciliedMovement10.setActionType(ReconciliationActionType.valueOf(String.valueOf(rs.getString("accion"))));
      return reconciliedMovement10;
    };
    List<ReconciliedMovement10> data = getDbUtils().getJdbcTemplate().query(String.format("SELECT * FROM %s.prp_movimiento_conciliado where id_mov_ref = %d", getSchema(), idMov), rowMapper);
    ReconciliedMovement10 reconciliedMovement10 = data.get(0);
    return reconciliedMovement10;
  }

  private AccountingData10 getAccountingData(Long idMov) {
    RowMapper rowMapper = (rs, rowNum) -> {
      AccountingData10 accountingData10 = new AccountingData10();
      accountingData10.setId(numberUtils.toLong(rs.getLong("id")));
      accountingData10.setStatus(AccountingStatusType.fromValue(String.valueOf(rs.getString("status"))));
      accountingData10.setAccountingStatus(AccountingStatusType.fromValue(String.valueOf(rs.getString("accounting_status"))));
      return accountingData10;
    };
    List<AccountingData10> data = getDbUtils().getJdbcTemplate().query(String.format("SELECT id, status, accounting_status FROM %s.accounting where id = %d", getSchemaAccounting(), idMov), rowMapper);
    AccountingData10 accountingData10 = data.get(0);
    return accountingData10;
  }
}
