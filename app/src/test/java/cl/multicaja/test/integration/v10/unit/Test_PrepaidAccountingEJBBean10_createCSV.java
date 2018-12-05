package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.model.v10.Accounting10;
import cl.multicaja.accounting.model.v10.AccountingTxType;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.prepaid.helpers.mastercard.model.AccountingFile;
import cl.multicaja.prepaid.model.v10.NewAmountAndCurrency10;
import cl.multicaja.prepaid.model.v10.ReconciliationMcRed10;
import com.opencsv.CSVReader;
import org.apache.commons.net.ntp.TimeStamp;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static cl.multicaja.core.model.Errors.ERROR_PROCESSING_FILE;

public class Test_PrepaidAccountingEJBBean10_createCSV extends TestBaseUnit {

  @Test
  public void createCSV() throws Exception {
    String fileName = "src/test/resources/mastercard/files/csv_prueba.csv";
    ArrayList<Accounting10> movements = new ArrayList<>();

    // Crear los movimientos a insertar
    Accounting10 mov1 = createAccountingMovement(1L, AccountingTxType.CARGA_WEB, 10000L, 15L, 100L, 200L, 38L);
    movements.add(mov1);

    Accounting10 mov2 = createAccountingMovement(2L, AccountingTxType.CARGA_POS, 20000L, 30L, 200L, 400L, 76L);
    movements.add(mov2);

    Accounting10 mov3 = createAccountingMovement(3L, AccountingTxType.RETIRO_WEB, 8000L, 11L, 70L, 40L, 8L);
    movements.add(mov3);

    getPrepaidAccountingEJBBean10().createAccountingCSV(fileName, movements);

    // Cargar los movimientos existentes en el archivo
    List<Accounting10> movementsOut = getCsvData(fileName);

    Assert.assertNotNull("Debe haber movimientos", movementsOut);
    Assert.assertEquals("Debe tener 3 movs", 3, movementsOut.size());

    // Comprobar que sean los mismos movimientos
    for (int i = 0; i < movements.size(); i++) {
      Accounting10 originalMov = movements.get(i);
      Accounting10 outputMov = movementsOut.get(i);

      Assert.assertEquals("Deben tener mismo id", originalMov.getId(), outputMov.getId());
      Assert.assertEquals("Deben tener mismo type", originalMov.getType().getValue(), outputMov.getType().getValue());
      Assert.assertEquals("Deben tener mismo monto", originalMov.getAmount(), outputMov.getAmount());
      Assert.assertEquals("Deben tener mismo montoUsd", originalMov.getAmountUsd(), outputMov.getAmountUsd());
      Assert.assertEquals("Deben tener mismo dif cambio", originalMov.getExchangeRateDif(), outputMov.getExchangeRateDif());
      Assert.assertEquals("Deben tener mismo fee", originalMov.getFee(), outputMov.getFee());
      Assert.assertEquals("Deben tener mismo iva", originalMov.getFeeIva(), outputMov.getFeeIva());
    }

    // Borrar el archivo
    new File(fileName).delete();
  }

  Accounting10 createAccountingMovement(Long id, AccountingTxType type, Long ammount, Long ammountUsd, Long exchangeDif, Long fee, Long iva) {
    Accounting10 mov = new Accounting10();

    mov.setId(id);
    mov.setType(type);
    mov.setTransactionDate(Timestamp.valueOf("2018-11-30 10:23:11"));
    NewAmountAndCurrency10 newAmmount = new NewAmountAndCurrency10();
    newAmmount.setValue(new BigDecimal(ammount));
    mov.setAmount(newAmmount);
    NewAmountAndCurrency10 newAmmountUsd = new NewAmountAndCurrency10();
    newAmmountUsd.setValue(new BigDecimal(ammountUsd));
    mov.setAmountUsd(newAmmountUsd);
    mov.setExchangeRateDif(new BigDecimal(exchangeDif));
    mov.setFee(new BigDecimal(fee));
    mov.setFeeIva(new BigDecimal(iva));

    return mov;
  }

  public List<Accounting10> getCsvData(String fileName) throws Exception {
    FileInputStream is = new FileInputStream(fileName);
    List<Accounting10> lstAccounting10;
    try {
      Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
      CSVReader csvReader = new CSVReader(reader,',');
      csvReader.readNext();
      String[] record;
      lstAccounting10 = new ArrayList<>();

      while ((record = csvReader.readNext()) != null) {
        Accounting10 accountingMov = createAccountingMovement(Long.valueOf(record[0]),
                                                              AccountingTxType.fromValue(record[2]),
                                                              Long.valueOf(record[3]),
                                                              Long.valueOf(record[4]),
                                                              Long.valueOf(record[5]),
                                                              Long.valueOf(record[6]),
                                                              Long.valueOf(record[7]));
        lstAccounting10.add(accountingMov);
      }
    } catch (Exception e) {
      e.printStackTrace();
      System.out.println("Exception: "+e);
      throw new ValidationException(ERROR_PROCESSING_FILE.getValue(), e.getMessage());
    }
    return lstAccounting10;
  }
}
