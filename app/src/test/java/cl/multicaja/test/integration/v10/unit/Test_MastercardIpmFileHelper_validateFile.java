package cl.multicaja.test.integration.v10.unit;

import cl.multicaja.accounting.helpers.mastercard.MastercardIpmFileHelper;
import cl.multicaja.accounting.helpers.mastercard.model.IpmFile;
import cl.multicaja.accounting.helpers.mastercard.model.IpmMessage;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileReader;

public class Test_MastercardIpmFileHelper_validateFile extends TestBaseUnit {

  @Test
  public void validateFile_fileNull() {
    try {
      MastercardIpmFileHelper.validateFile(null);
    } catch(Exception e) {
      Assert.assertEquals("Debe tener mensaje = [Ipm File Object null]", "Ipm File Object null", e.getMessage());
    }
  }

  @Test
  public void validateFile_header_null() {
    try {
      IpmFile file = new IpmFile();
      MastercardIpmFileHelper.validateFile(file);
    } catch (Exception e) {
      Assert.assertEquals("Debe tener mensaje = [Header record not found]", "Header record not found", e.getMessage());
    }
  }

  @Test
  public void validateFile_header_fileId_empty() {
    try {
      IpmFile file = new IpmFile();
      IpmMessage header = new IpmMessage();
      header.setFileId("");

      file.setHeader(header);

      MastercardIpmFileHelper.validateFile(file);
    } catch(Exception e) {
      Assert.assertEquals("Debe tener mensaje = [Header fileId not found]", "Header fileId not found", e.getMessage());
    }
  }

  @Test
  public void validateFile_header_isNotMti() {
    try {
      IpmFile file = new IpmFile();
      IpmMessage header = new IpmMessage();
      header.setFileId(getRandomString(5));
      header.setMti("1234");
      header.setFunctionCode("123");

      file.setHeader(header);

      MastercardIpmFileHelper.validateFile(file);
    } catch(Exception e) {
      Assert.assertEquals("Debe tener mensaje = [Unexpected HEADER type -> actual [1234-123], expected [1644-697]]", "Unexpected HEADER type -> actual [1234-123], expected [1644-697]", e.getMessage());
    }
  }

  @Test
  public void validateFile_trailer_null() {
    try {
      IpmFile file = new IpmFile();

      IpmMessage header = new IpmMessage();
      header.setFileId(getRandomString(5));
      header.setMti("1644");
      header.setFunctionCode("697");

      file.setHeader(header);

      MastercardIpmFileHelper.validateFile(file);
    } catch (Exception e) {
      Assert.assertEquals("Debe tener mensaje = [Trailer record not found]", "Trailer record not found", e.getMessage());
    }
  }

  @Test
  public void validateFile_trailer_fileId_empty() {
    try {
      IpmFile file = new IpmFile();

      IpmMessage header = new IpmMessage();
      header.setFileId(getRandomString(5));
      header.setMti("1644");
      header.setFunctionCode("697");

      file.setHeader(header);

      IpmMessage trailer = new IpmMessage();
      trailer.setFileId("");

      file.setTrailer(trailer);

      MastercardIpmFileHelper.validateFile(file);
    } catch (Exception e) {
      Assert.assertEquals("Debe tener mensaje = [Trailer fileId not found]", "Trailer fileId not found", e.getMessage());
    }
  }

  @Test
  public void validateFile_trailer_messageCount_0() {
    try {
      IpmFile file = new IpmFile();

      IpmMessage header = new IpmMessage();
      header.setFileId(getRandomString(5));
      header.setMti("1644");
      header.setFunctionCode("697");

      file.setHeader(header);

      IpmMessage trailer = new IpmMessage();
      trailer.setFileId(getRandomString(5));
      trailer.setMessageCount(0);

      file.setTrailer(trailer);

      MastercardIpmFileHelper.validateFile(file);
    } catch (Exception e){
      Assert.assertEquals("Debe tener mensaje = [Trailer messageCount not found]", "Trailer messageCount not found", e.getMessage());
    }

  }

  @Test
  public void validateFile_trailer_isNotMti() {
    try {
      IpmFile file = new IpmFile();

      IpmMessage header = new IpmMessage();
      header.setFileId(getRandomString(5));
      header.setMti("1644");
      header.setFunctionCode("697");

      file.setHeader(header);

      IpmMessage trailer = new IpmMessage();
      trailer.setMti("1234");
      trailer.setFunctionCode("123");
      trailer.setFileId(getRandomString(5));
      trailer.setMessageCount(123);

      file.setTrailer(trailer);

      MastercardIpmFileHelper.validateFile(file);
    } catch( Exception e) {
      Assert.assertEquals("Debe tener mensaje = [Unexpected TRAILER type -> actual [1234-123], expected [1644-695]]", "Unexpected TRAILER type -> actual [1234-123], expected [1644-695]", e.getMessage());
    }

  }

  @Test
  public void validateFile_fileIdDoesNotMatch() {
    String headerFileId = getRandomString(5) + "1";
    String trailerFileId = getRandomString(5) + "2";

    try {
      IpmFile file = new IpmFile();

      IpmMessage header = new IpmMessage();

      header.setFileId(headerFileId);
      header.setMti("1644");
      header.setFunctionCode("697");

      file.setHeader(header);

      IpmMessage trailer = new IpmMessage();
      trailer.setMti("1644");
      trailer.setFunctionCode("695");

      trailer.setFileId(trailerFileId);
      trailer.setMessageCount(123);

      file.setTrailer(trailer);

      MastercardIpmFileHelper.validateFile(file);
    } catch( Exception e) {
      Assert.assertEquals(String.format("Debe tener mensaje = [FileId does not match. Header fileId -> [%s], trailer fileId -> [%s]]", headerFileId, trailerFileId),
        String.format("FileId does not match. Header fileId -> [%s], trailer fileId -> [%s]", headerFileId, trailerFileId), e.getMessage());
    }

  }

  @Test
  public void validateFile_messageCountDoesNotMatch() {
    String fileId = getRandomString(5) + "1";

    try {
      IpmFile file = new IpmFile();

      IpmMessage header = new IpmMessage();

      header.setFileId(fileId);
      header.setMti("1644");
      header.setFunctionCode("697");

      file.setHeader(header);

      IpmMessage trailer = new IpmMessage();
      trailer.setMti("1644");
      trailer.setFunctionCode("695");

      trailer.setFileId(fileId);
      trailer.setMessageCount(103);

      file.setTrailer(trailer);

      MastercardIpmFileHelper.validateFile(file);
    } catch( Exception e) {
      Assert.assertEquals(String.format("MessageCount does not match. file messageCount -> [%s], trailer messageCount -> [%s]", 2, 103),
        String.format("MessageCount does not match. file messageCount -> [%s], trailer messageCount -> [%s]", 2, 103), e.getMessage());
    }

  }

  @Test
  public void validateFile() {
    String fileId = getRandomString(5) + "1";

    try {
      IpmFile file = new IpmFile();

      IpmMessage header = new IpmMessage();

      header.setFileId(fileId);
      header.setMti("1644");
      header.setFunctionCode("697");

      file.setHeader(header);

      IpmMessage trailer = new IpmMessage();
      trailer.setMti("1644");
      trailer.setFunctionCode("695");

      trailer.setFileId(fileId);
      trailer.setMessageCount(2);

      file.setTrailer(trailer);

      MastercardIpmFileHelper.validateFile(file);
    } catch( Exception e) {
      Assert.fail("No debe estar aca");
    }

  }

  @Test
  public void validateFile_realCsvFile() {
    IpmFile ipmFile = new IpmFile();
    try {
      File f = new File("src/test/resources/mastercard/files/ipm/good.ipm.csv");
      FileReader fr = new FileReader(f);

      ipmFile = MastercardIpmFileHelper.readCsvIpmData(fr, ipmFile);
      fr.close();

      MastercardIpmFileHelper.validateFile(ipmFile);
      ipmFile.setFileId(ipmFile.getTrailer().getFileId());
    } catch( Exception e) {
      Assert.fail("No debe estar aca");
    }

    Assert.assertNotNull("Debe tener header", ipmFile.getHeader());
    Assert.assertNotNull("Debe tener tailer", ipmFile.getTrailer());
    Assert.assertFalse("Debe tener transacciones", ipmFile.getTransactions().isEmpty());
    Assert.assertFalse("Debe tener otros mensajes", ipmFile.getTransactions().isEmpty());
    Assert.assertEquals("Debe tener 103 mensajes", Integer.valueOf(103), ipmFile.getMessageCount());
    Assert.assertEquals("Debe tener fileId []", "0011808150000001986699401", ipmFile.getFileId());

  }

}
