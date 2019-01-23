package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.model.v10.AccountingData10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.accounting.model.v10.ClearingData10;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.db.DBUtils;
import com.opencsv.CSVWriter;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
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
  }

  public ClearingData10 buildClearing() {
    ClearingData10 clearing10 = new ClearingData10();
    clearing10.setUserAccountId(getUniqueLong());
    clearing10.setFileId(getUniqueLong());
    clearing10.setStatus(AccountingStatusType.PENDING);
    return clearing10;
  }


  @Test
  public void insertClearingOK() throws Exception {

    List<AccountingData10> accounting10s = new ArrayList<>();
    for (int i = 0; i == 9; i++) {
      AccountingData10 accounting10 = buildRandomAccouting();
      accounting10s.add(accounting10);
      accounting10s = getPrepaidAccountingEJBBean10().saveAccountingData(null, accounting10s);

      ClearingData10 clearing10 = buildClearing();
      clearing10.setAccountingId(accounting10s.get(0).getId());

      clearing10 = getPrepaidClearingEJBBean10().insertClearingData(null, clearing10);
      Assert.assertNotNull("El objeto no puede ser Null", clearing10);
      Assert.assertNotEquals("El id no puede ser 0", 0, clearing10.getId().longValue());
    }


    ZonedDateTime date = ZonedDateTime.now(ZoneId.of("America/Santiago"));
    ZonedDateTime midnight = date.withHour(0).withMinute(0).withSecond(0).withNano(0);
    ZonedDateTime endDay = date.withHour(23).withMinute(59).withSecond(59).withNano(999999999);

    ZonedDateTime fromUtc = ZonedDateTime.ofInstant(midnight.toInstant(), ZoneOffset.UTC);
    ZonedDateTime toUtc = ZonedDateTime.ofInstant(endDay.toInstant(), ZoneOffset.UTC);

    LocalDateTime from = fromUtc.toLocalDateTime();
    LocalDateTime to = toUtc.toLocalDateTime();

    List<ClearingData10> movements = getPrepaidClearingEJBBean10().searchClearingDataToFile(null, from, to);
    Assert.assertEquals("Debe ser 10",10,movements.size());

    int i = 0;
    for(ClearingData10 data: movements) {
      data.setStatus(AccountingStatusType.OK);
    }

    String fileName = String.format("TRX_PREPAGO_%s.CSV", date.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
    InputStream is = createAccountingCSV(fileName, movements); // Crear archivo csv temporal
    Assert.assertNotNull("InputStream not Null",is);

  }

  public InputStream createAccountingCSV(String filename, List<ClearingData10> lstClearingMovement10s) throws IOException {
    File file = new File(filename);
    FileWriter outputFile = new FileWriter(file);
    CSVWriter writer = new CSVWriter(outputFile,',');

    String[] header = new String[]{"ID_LIQUIDACION", "ID_TRX", "ID_CUENTA_ORIGEN", "TIPO_TRX", "MOV_CONTABLE",
      "FECHA_TRX", "FECHA_CONCILIACION", "MONTO_TRX_PESOS", "MONTO_TRX_MCARD_PESOS", "MONTO_TRX_USD", "VALOR_USD",
      "DIF_TIPO_CAMBIO", "COMISION_PREPAGO_PESOS", "IVA_COMISION_PREPAGO_PESOS", "COMISION_RECAUDADOR_MC_PESOS",
      "IVA_COMISION_RECAUDADOR_MC_PESOS", "MONTO_AFECTO_A_SALDO_PESOS", "ID_CUENTA_DESTINO", "RUT", "BANCO",
      "NRO_CUENTA", "TIPO_CUENTA", "ESTADO_LIQUIDACION"};
    writer.writeNext(header);

    for (ClearingData10 mov : lstClearingMovement10s) {

      Long bankAccountId = mov.getUserBankAccount().getId();

      if(bankAccountId > 0) {
        //TODO: buscar la informacion de la cuenta bancaria en api-users
      }

      String[] data = new String[]{
        mov.getId().toString(), //ID_LIQUIDACION,
        mov.getIdTransaction().toString(), //ID_TRX
        "0", //ID_CUENTA_ORIGEN TODO: este código es dado por Multicaja red.
        mov.getType().getValue(), //TIPO_TRX
        "", //MOV_CONTABLE TODO: definir los tipos de movimientos contables.
        mov.getTransactionDateInFormat(), //FECHA_TRX
        mov.getConciliationDateInFormat(), //FECHA_CONCILIACION
        mov.getAmountBalance().getValue().toString(), //MONTO_TRX_PESOS
        mov.getAmountMastercard().getValue().toString(), //MONTO_TRX_MCARD_PESOS
        mov.getAmountUsd().getValue().toString(), //MONTO_TRX_USD
        "", //VALOR_USD TODO: de donde sacar este valor?
        mov.getExchangeRateDif().toString(), //DIF_TIPO_CAMBIO
        mov.getFee().toString(), //COMISION_PREPAGO_PESOS
        mov.getFeeIva().toString(), //IVA_COMISION_PREPAGO_PESOS
        mov.getCollectorFee().toString(), //COMISION_RECAUDADOR_MC_PESOS
        mov.getCollectorFeeIva().toString(), //IVA_COMISION_RECAUDADOR_MC_PESOS
        mov.getAmount().getValue().toString(), //MONTO_AFECTO_A_SALDO_PESOS
        "", //ID_CUENTA_DESTINO - Este campo es utilizado solo por MulticajaRed. No lo utiliza ni setea Prepago
        "", //RUT
        "", //BANCO
        "", //NRO_CUENTA
        "", //TIPO_CUENTA
        mov.getStatus().getValue() //ESTADO_LIQUIDACION
      };
      writer.writeNext(data);
    }
    writer.close();
    InputStream targetStream = new FileInputStream(file);
   return targetStream;
  }

}
