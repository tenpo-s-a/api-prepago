package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.prepaid.helpers.tecnocom.TecnocomFileHelper;
import cl.multicaja.prepaid.helpers.tecnocom.model.TecnocomReconciliationFile;
import cl.multicaja.prepaid.helpers.tecnocom.model.TecnocomReconciliationFileDetail;
import org.junit.Assert;
import org.junit.Test;

import java.io.InputStream;

/**
 * @author abarazarte
 **/
public class Test_TecnocomFileHelper extends TestBaseUnit {

  @Test
  public void shouldProcessFile() throws Exception {
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("tecnocom/files/PLJ61110.FINT0003");
    TecnocomReconciliationFile file = TecnocomFileHelper.getInstance().validateFile(inputStream);
    inputStream.close();

    Assert.assertNotNull("Deberia procesar el archivo", file);
    Assert.assertNotNull("Deberia tener header", file.getHeader());
    Assert.assertNotNull("Deberia tener footer", file.getFooter());
    Assert.assertNotNull("Deberia detalles", file.getDetails());
    Assert.assertFalse("Deberia detalles", file.getDetails().isEmpty());
    Assert.assertFalse("No debe ser sospechoso", file.isSuspicious());
    Assert.assertEquals("Deberia tener 16 registros", 16, file.getDetails().size());
  }

  //Archivo con autorizaciones.
  @Test
  public void shouldProcessFileAut() throws Exception {
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("tecnocom/files/PLJ61110.FINT0004");
    TecnocomReconciliationFile file = TecnocomFileHelper.getInstance().validateFile(inputStream);
    inputStream.close();

    Assert.assertNotNull("Deberia procesar el archivo", file);
    Assert.assertNotNull("Deberia tener header", file.getHeader());
    Assert.assertNotNull("Deberia tener footer", file.getFooter());
    Assert.assertNotNull("Deberia detalles", file.getDetails());
    Assert.assertFalse("Deberia detalles", file.getDetails().isEmpty());
    Assert.assertFalse("No debe ser sospechoso", file.isSuspicious());
    Assert.assertEquals("Deberia tener 18 registros", 18, file.getDetails().size());
    for(TecnocomReconciliationFileDetail detail :file.getDetails()){
      System.out.println(detail);
    }
  }

  @Test
  public void shouldBeSuspicious_noHeader() throws Exception {
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("tecnocom/files/PLJ61110.FINT0003.NO_HEADER");
    TecnocomReconciliationFile file = TecnocomFileHelper.getInstance().validateFile(inputStream);
    inputStream.close();
    Assert.assertNotNull("Deberia procesar el archivo", file);
    Assert.assertTrue("Debe ser sospechoso", file.isSuspicious());
    Assert.assertNull("Deberia tener header", file.getHeader());
    Assert.assertNull("Deberia tener footer", file.getFooter());
    Assert.assertTrue("Deberia detalles", file.getDetails().isEmpty());
  }

  @Test
  public void shouldBeSuspicious_noFooter() throws Exception {
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("tecnocom/files/PLJ61110.FINT0003.NO_FOOTER");
    TecnocomReconciliationFile file = TecnocomFileHelper.getInstance().validateFile(inputStream);
    inputStream.close();

    Assert.assertNotNull("Deberia procesar el archivo", file);
    Assert.assertTrue("Debe ser sospechoso", file.isSuspicious());
    Assert.assertNotNull("Deberia tener header", file.getHeader());
    Assert.assertNull("No deberia tener footer", file.getFooter());
    Assert.assertNotNull("Deberia detalles", file.getDetails());
    Assert.assertTrue("No deberia detalles", file.getDetails().isEmpty());
  }

  @Test
  public void shouldBeSuspicious_numberOfRecordsDoesNotMatch() throws Exception {
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("tecnocom/files/PLJ61110.FINT0003.RECORDS_1");
    TecnocomReconciliationFile file = TecnocomFileHelper.getInstance().validateFile(inputStream);
    inputStream.close();

    Assert.assertNotNull("Deberia procesar el archivo", file);
    Assert.assertTrue("Debe ser sospechoso", file.isSuspicious());
    Assert.assertNotNull("Deberia tener header", file.getHeader());
    Assert.assertNotNull("No deberia tener footer", file.getFooter());
    Assert.assertNotNull("Deberia detalles", file.getDetails());
    Assert.assertTrue("No deberia detalles", file.getDetails().isEmpty());
  }

  @Test
  public void shouldBeSuspicious_numberOfRecords() throws Exception {
    InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("tecnocom/files/PLJ61110.FINT0003.RECORDS_2");
    TecnocomReconciliationFile file = TecnocomFileHelper.getInstance().validateFile(inputStream);
    inputStream.close();

    Assert.assertNotNull("Deberia procesar el archivo", file);
    Assert.assertTrue("Debe ser sospechoso", file.isSuspicious());
    Assert.assertNotNull("Deberia tener header", file.getHeader());
    Assert.assertNotNull("Deberia tener footer", file.getFooter());
    Assert.assertNotNull("Deberia detalles", file.getDetails());
    Assert.assertTrue("No deberia detalles", file.getDetails().isEmpty());
  }
}
