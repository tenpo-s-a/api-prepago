package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.RutUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.helpers.users.model.Rut;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import com.opencsv.CSVWriter;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.core.RowMapper;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Test_PrepaidClearingEJBBean10_ProcessClearingFileResponse extends TestBaseUnit {

  private static final String SCHEMA = ConfigUtils.getInstance().getProperty("schema.acc");



  @BeforeClass
  @AfterClass
  public static void clearData() {
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.clearing CASCADE", SCHEMA));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting CASCADE", SCHEMA));
    DBUtils.getInstance().getJdbcTemplate().execute(String.format("TRUNCATE %s.accounting_files CASCADE", SCHEMA));
  }

  public ClearingData10 buildClearing() {
    ClearingData10 clearing10 = new ClearingData10();
    clearing10.setUserAccountId(getUniqueLong());
    clearing10.setFileId(getUniqueLong());
    clearing10.setStatus(AccountingStatusType.PENDING);
    return clearing10;
  }


  @Test
  public void testProcessFileAllOK() throws Exception {

    ZonedDateTime date = ZonedDateTime.now(ZoneId.of("America/Santiago"));
    String fileId = date.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
    String fileName = String.format("TRX_PREPAGO_%s.CSV", date.format(DateTimeFormatter.ofPattern("yyyyMMdd")));

    AccountingFiles10 files10 = new AccountingFiles10();
    files10.setFileId(fileId);
    files10.setFileFormatType(AccountingFileFormatType.CSV);
    files10.setFileType(AccountingFileType.CLEARING);
    files10.setName(fileName);
    files10.setStatus(AccountingStatusType.OK);

    files10 = getPrepaidAccountingFileEJBBean10().insertAccountingFile(null, files10);

    Assert.assertNotNull("No debe ser null",files10);
    Assert.assertNotEquals("No debe ser 0",0,files10.getId().longValue());

    int numberOfOKMovements = 1;
    int numberOfRejectedMovements = 1;
    int numberOfRejectedFormatMovements = 1;
    int numberOfWrongAmountMovements = 1;
    int numberOfNotReturnedMovements = 1;
    int numberOfNotInDatabaseMovements = 1;

    int totalMovements = numberOfOKMovements + numberOfRejectedMovements + numberOfRejectedFormatMovements + numberOfWrongAmountMovements + numberOfNotReturnedMovements + numberOfNotInDatabaseMovements;

    // Crear todos los movimientos que SI estan en la BD
    List<ClearingData10> allClearingData = new ArrayList<>();
    for (int i = 0; i < totalMovements - numberOfNotInDatabaseMovements ; i++) {
      User user = registerUser();
      UserAccount userAccount = createBankAccount(user);

      PrepaidUser10 prepaidUser = buildPrepaidUser10(user);
      prepaidUser = createPrepaidUser10(prepaidUser);
      PrepaidCard10 prepaidCard = buildPrepaidCard10FromTecnocom(user, prepaidUser);
      prepaidCard = createPrepaidCard10(prepaidCard);

      PrepaidWithdraw10 prepaidWithdraw = buildPrepaidWithdraw10(user);
      prepaidWithdraw.setMerchantCode(NewPrepaidWithdraw10.WEB_MERCHANT_CODE);
      prepaidWithdraw.setBankAccountId(userAccount.getId());
      NewAmountAndCurrency10 amountAndCurrency10 = new NewAmountAndCurrency10(new BigDecimal(12000L));
      prepaidWithdraw.setFee(new NewAmountAndCurrency10(new BigDecimal(500L)));
      prepaidWithdraw.setTotal(amountAndCurrency10);

      CdtTransaction10 cdtTransaction = buildCdtTransaction10(user, prepaidWithdraw);
      cdtTransaction = createCdtTransaction10(cdtTransaction);

      PrepaidMovement10 prepaidMovement10 = buildPrepaidMovement10(prepaidUser, prepaidWithdraw, prepaidCard, cdtTransaction, PrepaidMovementType.WITHDRAW);
      prepaidMovement10.setMonto(prepaidWithdraw.getAmount().getValue());
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
      accountingData10.setAmount(amountAndCurrency10);
      accountingData10.setAmountBalance(amountAndCurrency10);
      accountingData10.setAmountUsd(amountAndCurrency10);
      accountingData10.setAmountMastercard(amountAndCurrency10);
      accountingData10 = getPrepaidAccountingEJBBean10().saveAccountingData(null, accountingData10);

      ClearingData10 clearingData10 = buildClearing();
      clearingData10.setId(getUniqueLong());
      clearingData10.setAccountingId(accountingData10.getId());
      clearingData10.setStatus(AccountingStatusType.PENDING);
      clearingData10.setUserBankAccount(userAccount);
      clearingData10.setFileId(files10.getId());
      clearingData10.setAmount(amountAndCurrency10);
      clearingData10.setAmountBalance(amountAndCurrency10);
      clearingData10.setAmountUsd(amountAndCurrency10);
      clearingData10.setAmountMastercard(amountAndCurrency10);

      clearingData10 = getPrepaidClearingEJBBean10().insertClearingData(null, clearingData10);
      Assert.assertNotNull("El objeto no puede ser Null", clearingData10);
      Assert.assertNotEquals("El id no puede ser 0", 0, clearingData10.getId().longValue());

      // Como las clearing data no cargan todos sus datos, se los seteamos para mandarlo al archivo
      clearingData10.setIdTransaction(accountingData10.getIdTransaction());
      clearingData10.setAmount(amountAndCurrency10);
      clearingData10.setAmountBalance(amountAndCurrency10);
      clearingData10.setAmountUsd(amountAndCurrency10);
      clearingData10.setAmountMastercard(amountAndCurrency10);

      clearingData10.setType(AccountingTxType.RETIRO_WEB);
      clearingData10.setAccountingMovementType(AccountingMovementType.RETIRO_WEB);
      clearingData10.setExchangeRateDif(new BigDecimal(100));
      clearingData10.setFee(new BigDecimal(10));
      clearingData10.setFeeIva(new BigDecimal(19));
      clearingData10.setCollectorFee(new BigDecimal(90));
      clearingData10.setCollectorFeeIva(new BigDecimal(9));

      clearingData10.setUserBankAccount(userAccount);

      // Se almacenan todas para prepararlas para meterlas al archivo
      allClearingData.add(clearingData10);
    }

    ZonedDateTime endDay = date.withHour(23).withMinute(59).withSecond(59).withNano( 999999999);
    ZonedDateTime toUtc = ZonedDateTime.ofInstant(endDay.toInstant(), ZoneOffset.UTC);
    LocalDateTime to = toUtc.toLocalDateTime();

    List<ClearingData10> movements = getPrepaidClearingEJBBean10().searchClearingDataToFile(null, to);
    Assert.assertEquals("Debe ser " + (totalMovements - numberOfNotInDatabaseMovements), totalMovements - numberOfNotInDatabaseMovements, movements.size());

    List<ClearingData10> okMovements = new ArrayList<>();
    List<ClearingData10> rejectedMovements = new ArrayList<>();
    List<ClearingData10> rejectedFormatMovements = new ArrayList<>();
    List<ClearingData10> wrongAmountMovements = new ArrayList<>();
    List<ClearingData10> notInFileMovements = new ArrayList<>();
    List<ClearingData10> notInBDMovements = new ArrayList<>();

    // Preparar los datos que vienen en el archivo y tambien estan en la BD
    for(int i = 0; i < allClearingData.size(); i++) {
      ClearingData10 data = allClearingData.get(i);
      if(i < numberOfOKMovements) {
        data.setStatus(AccountingStatusType.OK);
        okMovements.add(data);
      } else if (i < numberOfOKMovements + numberOfRejectedMovements) {
        data.setStatus(AccountingStatusType.REJECTED);
        rejectedMovements.add(data);
      } else if (i < numberOfOKMovements + numberOfRejectedMovements + numberOfRejectedFormatMovements) {
        data.setStatus(AccountingStatusType.REJECTED_FORMAT);
        rejectedFormatMovements.add(data);
      } else if (i < numberOfOKMovements + numberOfRejectedMovements + numberOfRejectedFormatMovements + numberOfWrongAmountMovements) {
        data.getAmount().setValue(data.getAmount().getValue().add(new BigDecimal(1L)));
        data.setStatus(AccountingStatusType.OK);
        wrongAmountMovements.add(data);
      } else {
        data.setStatus(AccountingStatusType.OK);
        notInFileMovements.add(data);
      }
    }

    // Sacar de la lista las que no tienen que venir en el archivo, pero SI estan en la BD
    for(int i = 0; i < numberOfNotReturnedMovements; i++) {
      allClearingData.remove(allClearingData.size() - 1);
    }

    // Agregar a la lista las que no estaran en la base de datos
    for(int i = 0; i < numberOfNotInDatabaseMovements; i++) {
      ClearingData10 data = new ClearingData10();
      data.setId(getUniqueLong());
      data.setFileId(files10.getId());
      data.setIdTransaction(getUniqueLong());
      data.setType(AccountingTxType.RETIRO_WEB);
      data.setAccountingMovementType(AccountingMovementType.RETIRO_WEB);
      NewAmountAndCurrency10 amount = new NewAmountAndCurrency10();
      amount.setValue(new BigDecimal(666));
      data.setAmount(amount);
      data.setAmountMastercard(amount);
      data.setAmountUsd(amount);
      data.setExchangeRateDif(new BigDecimal(100));
      data.setFee(new BigDecimal(10));
      data.setFeeIva(new BigDecimal(19));
      data.setCollectorFee(new BigDecimal(90));
      data.setCollectorFeeIva(new BigDecimal(9));
      data.setAmountBalance(amount);
      data.setStatus(AccountingStatusType.OK);
      UserAccount bankAccount = new UserAccount();
      bankAccount.setId(55L);
      bankAccount.setAccountNumber(getRandomNumericString(12));
      Rut rut = new Rut();
      rut.setValue(Integer.valueOf(getRandomNumericString(8)));
      bankAccount.setRut(rut);
      data.setUserBankAccount(bankAccount);
      allClearingData.add(data);
      notInBDMovements.add(data);
    }

    InputStream is = createAccountingCSV(fileName, fileId, allClearingData); // Crear archivo csv temporal
    Assert.assertNotNull("InputStream not Null", is);
    getPrepaidClearingEJBBean10().processClearingResponse(is, fileName);

    List<ClearingData10> processedClearingmovements = getPrepaidClearingEJBBean10().searchClearignDataByFileId(null, fileId);

    // Revisar los movimientos ok
    for(ClearingData10 originalMovement : okMovements) {
      ClearingData10 result = processedClearingmovements.stream().filter(x ->originalMovement.getId().equals(x.getId())).findAny().orElse(null);
      Assert.assertNotNull("Deberia existir un mov con el mismo id", result);
      Assert.assertEquals("El status debe ser OK", AccountingStatusType.OK, result.getStatus());
    }

    // Revisar los rechazos
    for(ClearingData10 originalMovement : rejectedMovements) {
      ClearingData10 result = processedClearingmovements.stream().filter(x ->originalMovement.getId().equals(x.getId())).findAny().orElse(null);
      Assert.assertNotNull("Deberia existir un mov con el mismo id", result);
      Assert.assertEquals("El status debe ser REJECTED", AccountingStatusType.REJECTED, result.getStatus());
    }

    // Revisar los rechazos por formato
    for(ClearingData10 originalMovement : rejectedFormatMovements) {
      ClearingData10 result = processedClearingmovements.stream().filter(x ->originalMovement.getId().equals(x.getId())).findAny().orElse(null);
      Assert.assertNotNull("Deberia existir un mov con el mismo id", result);
      Assert.assertEquals("El status debe ser REJECTED FORMAT", AccountingStatusType.REJECTED_FORMAT, result.getStatus());
    }

    // Revisar los que tienen montos distintos
    for(ClearingData10 originalMovement : wrongAmountMovements) {
      ClearingData10 result = processedClearingmovements.stream().filter(x ->originalMovement.getId().equals(x.getId())).findAny().orElse(null);
      Assert.assertNotNull("Deberia existir un mov con el mismo id", result);
      Assert.assertEquals("El status debe ser RESEARCH", AccountingStatusType.INVALID_INFORMATION, result.getStatus());
    }

    // Revisar los que no vienen en el archivo
    for(ClearingData10 originalMovement : notInFileMovements) {
      ClearingData10 result = processedClearingmovements.stream().filter(x ->originalMovement.getId().equals(x.getId())).findAny().orElse(null);
      Assert.assertNotNull("Deberia existir un mov con el mismo id", result);
      Assert.assertEquals("El status debe ser RESEARCH", AccountingStatusType.NOT_IN_FILE, result.getStatus());
    }

    // Revisar los que venian en el archivo pero no estan en nuestra BD
    for(ClearingData10 originalMovement : notInBDMovements) {
      List<ReconciliedResearch> researchMovs = getResearchMovement(originalMovement.getId());
      Assert.assertNotNull("Debe haber una respuesta", researchMovs);
      Assert.assertEquals("Debe haber un solo movimiento a investigar", 1, researchMovs.size());
    }
  }

  private List<ReconciliedResearch> getResearchMovement(Long movId) {
    RowMapper rowMapper = (rs, rowNum) -> {
      ReconciliedResearch reconciliedResearch = new ReconciliedResearch();
      reconciliedResearch.setId(numberUtils.toLong(rs.getLong("id")));
      reconciliedResearch.setIdRef(String.valueOf(rs.getString("mov_ref")));
      reconciliedResearch.setNombre_archivo(String.valueOf(rs.getString("nombre_archivo")));
      reconciliedResearch.setOrigen(String.valueOf(rs.getString("origen")));
      return reconciliedResearch;
    };
    List<ReconciliedResearch> data = getDbUtils().getJdbcTemplate().query(String.format("SELECT * FROM %s.prp_movimiento_investigar where mov_ref LIKE 'ClearingId=%s'", getSchema(), String.valueOf(movId)), rowMapper);
    return data;
  }

  public InputStream createAccountingCSV(String filename, String fileId, List<ClearingData10> lstClearingMovement10s) throws IOException {
    InputStream targetStream = new FileInputStream(getPrepaidClearingEJBBean10().createAccountingCSV(filename, fileId, lstClearingMovement10s));
    return targetStream;
  }

}
