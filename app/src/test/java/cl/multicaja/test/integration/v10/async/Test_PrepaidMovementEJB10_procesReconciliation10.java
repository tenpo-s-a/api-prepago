package cl.multicaja.test.integration.v10.async;

import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.prepaid.helpers.freshdesk.model.v10.*;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.utils.TemplateUtils;
import org.junit.*;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.*;
import java.util.ArrayList;
import java.util.List;

import static cl.multicaja.prepaid.helpers.CalculationsHelper.getParametersUtil;

public class Test_PrepaidMovementEJB10_procesReconciliation10 extends TestBaseUnitAsync {

  @Before
  @After
  public void clearData() {
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.clearing CASCADE", getSchemaAccounting()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting CASCADE", getSchemaAccounting()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento CASCADE", getSchema()));
    getDbUtils().getJdbcTemplate().execute(String.format("TRUNCATE %s.prp_movimiento_conciliado CASCADE", getSchema()));
  }

  // Se hace movimiento contrario al no estar conciliado con el switch (TOPUP)
  @Ignore
  @Test
  public void processReconciliationCase2Topup() throws Exception {

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.NOT_RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    getPrepaidMovementEJBBean10().processReconciliation(prepaidMovement10);

    //FIXME: no tiene asserts
  }

  // Se hace movimiento contrario al no estar conciliado con el switch (WITHDRAW)
  @Ignore
  @Test
  public void processReconciliationCase2Withdraw() throws Exception {

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.NOT_RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setEstado(PrepaidMovementStatus.PROCESS_OK);
    prepaidMovement10.setTipoMovimiento(PrepaidMovementType.WITHDRAW);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    getPrepaidMovementEJBBean10().processReconciliation(prepaidMovement10);

    //FIXME: no tiene asserts
  }

  // Se hace movimiento contrario al no estar conciliado con el switch (TOPUP) STATUS ERROR
  @Ignore
  @Test
  public void processReconciliationCase6Topup() throws Exception {

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.NOT_RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    getPrepaidMovementEJBBean10().processReconciliation(prepaidMovement10);

    //FIXME: no tiene asserts

  }

  // Se hace movimiento contrario al no estar conciliado con el switch (WITHDRAW) STATUS ERROR RESPONSE
  @Ignore
  @Test
  public void processReconciliationCase7Withdraw() throws Exception {

    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    prepaidTopup.setFee(new NewAmountAndCurrency10(BigDecimal.ZERO));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(BigDecimal.ZERO));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.NOT_RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setEstado(PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE);
    prepaidMovement10.setTipoMovimiento(PrepaidMovementType.WITHDRAW);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    getPrepaidMovementEJBBean10().processReconciliation(prepaidMovement10);

    //FIXME: no tiene asserts
  }

  @Ignore
  @Test
  public void processReconciliation_SendTicketDevolucion() throws Exception {
    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    createPrepaidUser10(prepaidUser);

    String template = getParametersUtil().getString("api-prepaid", "template_ticket_devolucion", "v1.0");
    template = TemplateUtils.freshDeskTemplateDevolucion(template, String.format("%s %s", user.getName(), user.getLastname_1()), String.format("%s-%s", user.getRut().getValue(), user.getRut().getDv()), user.getId(), "8888", 200000L, user.getEmail().getValue(), user.getCellphone().getValue());

    NewTicket newTicket = new NewTicket();
    newTicket.setDescription(template);
    newTicket.setGroupId(GroupId.OPERACIONES);
    newTicket.setUniqueExternalId("14621456");
    newTicket.setType(TicketType.DEVOLUCION);
    newTicket.setStatus(StatusType.OPEN);
    newTicket.setPriority(PriorityType.URGENT);
    newTicket.setSubject("Devolucion de carga");
    newTicket.setProductId(43000001595L);
    newTicket.addCustomField("cf_id_movimiento", "123444567");

    Ticket ticket = getUserClient().createFreshdeskTicket(null, user.getId(), newTicket);
    Assert.assertNotNull("Deberia crear un ticket de devolucion", ticket);
    Assert.assertNotNull("Ticket debe tener id", ticket.getId());
  }

  @Test
  public void processReconciliation_CaseRefund() throws Exception {
    User user = registerUser();
    PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
    prepaidUser = createPrepaidUser10(prepaidUser);
    PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
    prepaidCard = createPrepaidCard10(prepaidCard);
    PrepaidTopup10 prepaidTopup = buildPrepaidTopup10(user);
    prepaidTopup.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
    prepaidTopup.setTotal(new NewAmountAndCurrency10(new BigDecimal(10000L)));

    CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidTopup);
    cdtTransaction = createCdtTransaction10(cdtTransaction);

    PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidTopup, prepaidCard, cdtTransaction);
    prepaidMovement10.setConSwitch(ReconciliationStatusType.RECONCILED);
    prepaidMovement10.setConTecnocom(ReconciliationStatusType.NOT_RECONCILED);
    prepaidMovement10.setEstado(PrepaidMovementStatus.REJECTED);
    prepaidMovement10.setTipoMovimiento(PrepaidMovementType.TOPUP);
    prepaidMovement10 = createPrepaidMovement10(prepaidMovement10);

    // crea los movimientos de accounting y clearing correspondientes
    addAccountingAndClearing(prepaidMovement10);

    getPrepaidMovementEJBBean10().processReconciliation(prepaidMovement10);

    List cdtTransaction10s = getCdtEJBBean10().buscaListaMovimientoByIdExterno(null, prepaidMovement10.getIdTxExterno());
    System.out.println(cdtTransaction10s);
    Assert.assertNotNull("Debe existir una lista", cdtTransaction10s);
    Assert.assertEquals("Debe tener 3 transacciones con su id", 3, cdtTransaction10s.size());

    int foundCargas = 0;
    int foundCargasConf = 0;
    int foundReversasCarga = 0;

    for(Object t : cdtTransaction10s) {
      CdtTransaction10 transaction = (CdtTransaction10) t;
      if(CdtTransactionType.PRIMERA_CARGA.equals(transaction.getTransactionType())) {
        foundCargas++;
      } else if(CdtTransactionType.PRIMERA_CARGA_CONF.equals(transaction.getTransactionType())) {
        foundCargasConf++;
      } else if(CdtTransactionType.REVERSA_PRIMERA_CARGA.equals(transaction.getTransactionType())) {
        foundReversasCarga++;
      } else {
        Assert.fail("No debe existir otro tipo de transaccion asociado");
      }
    }

    Assert.assertEquals("Debe existir una carga", 1, foundCargas);
    Assert.assertEquals("Debe existir una carga conf", 1, foundCargasConf);
    Assert.assertEquals("Debe existir una reversa carga", 1, foundReversasCarga);

    // Debe existir el movimiento conciliado como refund
    RowMapper rowMapper = (rs, rowNum) -> {
      ReconciliedMovement10 reconciliedMovement10 = new ReconciliedMovement10();
      reconciliedMovement10.setId(numberUtils.toLong(rs.getLong("id")));
      reconciliedMovement10.setIdMovRef(numberUtils.toLong(rs.getLong("id_mov_ref")));
      reconciliedMovement10.setReconciliationStatusType(ReconciliationStatusType.fromValue(String.valueOf(rs.getString("estado"))));
      reconciliedMovement10.setActionType(ReconciliationActionType.valueOf(String.valueOf(rs.getString("accion"))));
      return reconciliedMovement10;
    };
    List<ReconciliedMovement10> data = getDbUtils().getJdbcTemplate().query(String.format("SELECT * FROM %s.prp_movimiento_conciliado where id_mov_ref = %s", getSchema(), prepaidMovement10.getId()), rowMapper);
    ReconciliedMovement10 reconciliedMovement10 = data.get(0);

    Assert.assertNotNull("Debe existir en conciliados", reconciliedMovement10);
    Assert.assertEquals("Debe estr en estado refund", ReconciliationStatusType.TO_REFUND, reconciliedMovement10.getReconciliationStatusType());
    Assert.assertEquals("Debe tener accion refund", ReconciliationActionType.REFUND, reconciliedMovement10.getActionType());

    // Debe tener estado de negocio refund
    PrepaidMovement10 storedMovement = getPrepaidMovementEJBBean10().getPrepaidMovementById(prepaidMovement10.getId());
    Assert.assertEquals("El estado de negocio debe ser TO_REFUND", BusinessStatusType.TO_REFUND, storedMovement.getEstadoNegocio());

    Long movId = prepaidMovement10.getId();

    // verifica movimiento accounting y clearing
    List<AccountingData10> accounting10s = getPrepaidAccountingEJBBean10().searchAccountingData(null, LocalDateTime.now());
    Assert.assertNotNull("No debe ser null", accounting10s);
    Assert.assertEquals("Debe haber 1 movimientos de account", 1, accounting10s.size());

    AccountingData10 accounting10 = accounting10s.stream().filter(acc -> acc.getIdTransaction().equals(movId)).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener una carga", accounting10);
    Assert.assertEquals("Debe tener tipo POS", AccountingTxType.CARGA_POS, accounting10.getType());
    Assert.assertEquals("Debe tener acc movement type POS", AccountingMovementType.CARGA_POS, accounting10.getAccountingMovementType());
    Assert.assertEquals("Debe tener el mismo imp fac", prepaidMovement10.getImpfac().stripTrailingZeros(), accounting10.getAmount().getValue().stripTrailingZeros());
    Assert.assertEquals("Debe tener accountingStatus NOT_OK", AccountingStatusType.NOT_OK, accounting10.getAccountingStatus());
    Assert.assertEquals("Debe tener status NOT_SEND", AccountingStatusType.NOT_SEND, accounting10.getStatus());
    Assert.assertEquals("Debe tener el mismo id", movId, accounting10.getIdTransaction());
    Assert.assertTrue("Tiene fecha de conciliacion menor a now()", this.checkReconciliationDate(accounting10.getConciliationDate()));

    List<ClearingData10> clearing10s = getPrepaidClearingEJBBean10().searchClearingData(null, null, AccountingStatusType.NOT_SEND, null);
    Assert.assertNotNull("No debe ser null", clearing10s);
    Assert.assertEquals("Debe haber 1 movimiento de clearing", 1, clearing10s.size());

    ClearingData10 clearing10 = clearing10s.stream().filter(acc -> acc.getAccountingId().equals(accounting10.getId())).findFirst().orElse(null);
    Assert.assertNotNull("deberia tener un retiro", clearing10);
    Assert.assertEquals("Debe tener el id de accounting", accounting10.getId(), clearing10.getAccountingId());
    Assert.assertEquals("Debe tener el id de la cuenta", Long.valueOf(0), clearing10.getUserBankAccount().getId());
    Assert.assertEquals("Debe estar en estado NOT_SEND", AccountingStatusType.NOT_SEND, clearing10.getStatus());
  }

  private void addAccountingAndClearing(PrepaidMovement10 prepaidMovement, AccountingStatusType clearingStatus) throws Exception {
    PrepaidAccountingMovement pam = new PrepaidAccountingMovement();
    pam.setPrepaidMovement10(prepaidMovement);
    pam.setReconciliationDate(Timestamp.from(ZonedDateTime.now(ZoneOffset.UTC).plusYears(1000).toInstant()));

    AccountingData10 accounting = getPrepaidAccountingEJBBean10().buildAccounting10(pam, AccountingStatusType.PENDING, AccountingStatusType.PENDING);

    accounting = getPrepaidAccountingEJBBean10().saveAccountingData(null, accounting);

    // Insertar en clearing
    ClearingData10 clearing10 = new ClearingData10();
    clearing10.setStatus(clearingStatus);
    clearing10.setUserBankAccount(null);
    clearing10.setAccountingId(accounting.getId());
    getPrepaidClearingEJBBean10().insertClearingData(null, clearing10);
  }

  private void addAccountingAndClearing(PrepaidMovement10 prepaidMovement) throws Exception {
    addAccountingAndClearing(prepaidMovement, AccountingStatusType.INITIAL);
  }

  private Boolean checkReconciliationDate(Timestamp ts) {

    Instant instant = Instant.now();
    ZoneId z = ZoneId.of( "UTC" );
    ZonedDateTime nowUtc = instant.atZone(z);

    LocalDateTime localDateTime = ts.toLocalDateTime();
    ZonedDateTime reconciliationDateUtc = localDateTime.atZone(ZoneOffset.UTC);

    return reconciliationDateUtc.isBefore(nowUtc);
  }
}
