package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.model.v10.*;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.helpers.users.model.Rut;
import cl.multicaja.prepaid.model.v10.NewAmountAndCurrency10;
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

    int numberOfOKMovements = 3;
    int numberOfRejectedMovements = 3;
    int numberOfRejectedFormatMovements = 3;
    int numberOfWrongAmountMovements = 3;
    int numberOfNotReturnedMovements = 3;
    int numberOfNotInDatabaseMovements = 3;

    int totalMovements = numberOfOKMovements + numberOfRejectedMovements + numberOfRejectedFormatMovements + numberOfWrongAmountMovements + numberOfNotReturnedMovements + numberOfNotInDatabaseMovements;

    // Crear todos los movimientos que SI estan en la BD
    List<AccountingData10> accounting10s = new ArrayList<>();
    for (int i = 0; i < totalMovements - numberOfNotInDatabaseMovements ; i++) {

      AccountingData10 accounting10 = buildRandomAccouting(AccountingTxType.RETIRO_WEB);
      accounting10s.add(accounting10);
      getPrepaidAccountingEJBBean10().saveAccountingData(null, accounting10);

      ClearingData10 clearing10 = buildClearing();
      clearing10.setAccountingId(accounting10s.get(0).getId());
      clearing10.setFileId(files10.getId());

      clearing10 = getPrepaidClearingEJBBean10().insertClearingData(null, clearing10);
      Assert.assertNotNull("El objeto no puede ser Null", clearing10);
      Assert.assertNotEquals("El id no puede ser 0", 0, clearing10.getId().longValue());
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
    for(int i = 0; i < movements.size(); i++) {
      ClearingData10 data = movements.get(i);
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
      movements.remove(movements.size() - 1);
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
      data.setUserBankAccount(bankAccount);
      movements.add(data);
      notInBDMovements.add(data);
    }

    InputStream is = createAccountingCSV(fileName, fileId, movements); // Crear archivo csv temporal
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
      Assert.assertEquals("El status debe ser RESEARCH", AccountingStatusType.RESEARCH, result.getStatus());
      List<ReconciliedResearch> researchMovs = getResearchMovement(result.getId());
      Assert.assertNotNull("Debe haber una respuesta", researchMovs);
      Assert.assertEquals("Debe haber un solo movimiento a investigar", 1, researchMovs.size());
    }

    // Revisar los que no vienen en el archivo
    for(ClearingData10 originalMovement : notInFileMovements) {
      ClearingData10 result = processedClearingmovements.stream().filter(x ->originalMovement.getId().equals(x.getId())).findAny().orElse(null);
      Assert.assertNotNull("Deberia existir un mov con el mismo id", result);
      Assert.assertEquals("El status debe ser RESEARCH", AccountingStatusType.RESEARCH, result.getStatus());
      List<ReconciliedResearch> researchMovs = getResearchMovement(result.getId());
      Assert.assertNotNull("Debe haber una respuesta", researchMovs);
      Assert.assertEquals("Debe haber un solo movimiento a investigar", 1, researchMovs.size());
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
