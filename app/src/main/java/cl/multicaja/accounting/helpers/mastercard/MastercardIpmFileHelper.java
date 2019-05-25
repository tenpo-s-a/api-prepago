package cl.multicaja.accounting.helpers.mastercard;

import cl.multicaja.accounting.helpers.mastercard.model.IpmFile;
import cl.multicaja.accounting.helpers.mastercard.model.IpmMessage;
import cl.multicaja.core.utils.encryption.PgpHelper;
import com.opencsv.CSVReader;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;

public class MastercardIpmFileHelper {

  private static Log log = LogFactory.getLog(MastercardIpmFileHelper.class);

  public static IpmFile readCsvIpmData(FileReader file, IpmFile ipmFile) throws Exception {
    if(file == null) {
      throw new Exception("Csv File is null");
    }
    if(ipmFile == null) {
      throw new Exception("IpmFile object is null");
    }

    CSVReader reader = null;

    try {
      reader = new CSVReader(file);
      String[] line;

      // skip header line
      reader.readNext();

      IpmMessage message;
      while ((line = reader.readNext()) != null) {

        message = new IpmMessage(line);

        if (message.isHeader()) {
          log.info("Header record found");
          ipmFile.setHeader(message);
        } else if (message.isTrailer()) {
          log.info("Trailer record found");
          ipmFile.setTrailer(message);
        } else if (message.isTransaction()) {
          ipmFile.addTransaction(message);
          ipmFile.addCurrencyExponent(message.getCurrencyExponents());
        } else {
          ipmFile.addOtherMessage(message);
        }
      }
      return ipmFile;
    } catch (Exception e) {
      log.error(String.format("Error processing IPM file -> [%s]", ipmFile.getFileName()), e);
      reader.close();
      throw e;
    }
  }

  public static void validateFile(IpmFile file) throws Exception {
    if(file == null) {
      throw new Exception("Ipm File Object null");
    }

    log.info(String.format("Validating IPM file -> [%s]", file.getFileName()));
    try {
      validateHeader(file.getHeader());
      validateTrailer(file.getTrailer());

      String headerFileId = file.getHeader().getFileId();
      String trailerFileId = file.getTrailer().getFileId();

      if(!headerFileId.equals(trailerFileId)) {
        String msg = String.format("FileId does not match. Header fileId -> [%s], trailer fileId -> [%s]", headerFileId, trailerFileId);
        log.error(msg);
        throw new Exception(msg);
      }

      Integer fileMessageCount = file.getMessageCount();
      Integer trailerMessageCount = file.getTrailer().getMessageCount();

      if(!fileMessageCount.equals(trailerMessageCount)) {
        String msg = String.format("MessageCount does not match. file messageCount -> [%s], trailer messageCount -> [%s]", fileMessageCount, trailerMessageCount);
        log.error(msg);
        throw new Exception(msg);
      }

      log.info(String.format("IPM file -> [%s] seems valid", file.getFileName()));
    } catch (Exception ex) {
      log.error(String.format("IPM file -> [%s] seems suspicious", file.getFileName()), ex);
      throw ex;
    }
  }

  private static void validateHeader(IpmMessage header) throws Exception {
    if(header == null) {
      throw new Exception("Header record not found");
    }

    if(StringUtils.isAllBlank(header.getFileId())) {
      throw new Exception("Header fileId not found");
    }

    if(!header.isHeader()) {
      throw new Exception(String.format("Unexpected HEADER type -> actual [%s], expected [%s]", String.format("%s-%s", header.getMti(), header.getFunctionCode()), IpmMessage.HEADER_MESSAGE_TYPE));
    }
  }

  private static void validateTrailer(IpmMessage trailer) throws Exception {
    if(trailer == null) {
      throw new Exception("Trailer record not found");
    }

    if(StringUtils.isAllBlank(trailer.getFileId())) {
      throw new Exception("Trailer fileId not found");
    }

    if(trailer.getMessageCount().equals(0)) {
      throw new Exception("Trailer messageCount not found");
    }

    if(!trailer.isTrailer()) {
      throw new Exception(String.format("Unexpected TRAILER type -> actual [%s], expected [%s]", String.format("%s-%s", trailer.getMti(), trailer.getFunctionCode()), IpmMessage.TRAILER_MESSAGE_TYPE));
    }
  }

}
