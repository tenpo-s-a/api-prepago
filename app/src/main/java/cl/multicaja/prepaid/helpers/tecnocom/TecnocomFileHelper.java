package cl.multicaja.prepaid.helpers.tecnocom;

import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.prepaid.helpers.tecnocom.model.*;
import cl.multicaja.prepaid.model.v10.BusinessStatusType;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidMovementStatus;
import cl.multicaja.prepaid.model.v10.TecnocomOperationType;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.constants.CodigoPais;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.IndicadorPropiaAjena;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.*;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Scanner;

/**
 * @author abarazarte
 **/
public class TecnocomFileHelper {

  private static Log log = LogFactory.getLog(TecnocomFileHelper.class);
  private static TecnocomFileHelper instance;

  public static TecnocomFileHelper getInstance() {
    if (instance == null) {
      synchronized(TecnocomFileHelper.class) {
        if (instance == null) {
          instance = new TecnocomFileHelper();
        }
      }
    }
    return instance;
  }

  public Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }

  private Boolean isValidHeaderDateTimeFormat(String toValidate, String format){
    if(StringUtils.isBlank(toValidate)){
      return Boolean.FALSE;
    }

    SimpleDateFormat sdf = new SimpleDateFormat(format);
    sdf.setLenient(false);
    try {
      sdf.parse(toValidate);
      return Boolean.TRUE;
    } catch (ParseException e) {
      log.error(String.format("Error parsing -> %s", toValidate), e);
      return Boolean.FALSE;
    }
  }

  private ReconciliationFile validateHeader(String header, ReconciliationFile file) throws Exception {
    if(StringUtils.isAllBlank(header)) {
      String msg = "Header not found";
      log.error(msg);
      throw new Exception(msg);
    }

    file.setHeader(new ReconciliationFileHeader(header));

    // validar codigo de entidad
    if(StringUtils.isBlank(file.getHeader().getCodent())) {
      String msg = "CODENT not found";
      log.error(msg);
      throw new Exception(msg);
    }

    // validar numero de secuencia
    if(StringUtils.isBlank(file.getHeader().getNsecfic())) {
      String msg = "NSECFIC not found";
      log.error(msg);
      throw new Exception();
    }

    // validar tipo de secuencia
    if(StringUtils.isBlank(file.getHeader().getTipocinta())) {
      String msg = "TIPOCINTA not found";
      log.error(msg);
      throw new Exception(msg);
    }

    // validar tipo de registro
    if(StringUtils.isBlank(file.getHeader().getTiporeg())) {
      String msg = "TIPOREG not found";
      log.error(msg);
      throw new Exception(msg);
    }
    if(!"C".equals(file.getHeader().getTiporeg())) {
      String msg = "TIPOREG is not [C = CABECERA]";
      log.error(msg);
      throw new Exception(msg);
    }

    //Validar Fecha
    if(!isValidHeaderDateTimeFormat(file.getHeader().getFecenvio(), ReconciliationFileHeader.DATE_FORMAT)) {
      String msg = String.format("Invalid DATE FORMAT %s -> [%s]", ReconciliationFileHeader.DATE_FORMAT, file.getHeader().getFecenvio());
      log.error(msg);
      throw new Exception(msg);
    }
    //Validar Hora
    if(!isValidHeaderDateTimeFormat(file.getHeader().getHoraenvio(), ReconciliationFileHeader.TIME_FORMAT)) {
      String msg = String.format("Invalid TIME FORMAT %s -> [%s]", ReconciliationFileHeader.TIME_FORMAT, file.getHeader().getHoraenvio());
      log.error(msg);
      throw new Exception(msg);
    }

    // validar numero de registros
    if(file.getHeader().getNumregtot() == null) {
      String msg = "NUMREGTOT not found";
      log.error(msg);
      throw new Exception(msg);
    }
    return file;
  }

  private ReconciliationFile validateFooter(String footer, Integer records, ReconciliationFile file) throws Exception {
    if(StringUtils.isAllBlank(footer)) {
      log.error("Footer not found");
      throw new Exception("Footer not found");
    }
    ReconciliationFileHeader foot = new ReconciliationFileHeader(footer);
    file.setFooter(foot);

    ReconciliationFileHeader header = file.getHeader();

    // validar codigo de entidad
    if(StringUtils.isBlank(foot.getCodent())) {
      String msg = "CODENT not found";
      log.error(msg);
      throw new Exception(msg);
    }
    if(!header.getCodent().equals(foot.getCodent())) {
      String msg = "CODENT does not match";
      log.error(msg);
      throw new Exception(msg);
    }

    // validar numero de secuencia
    if(StringUtils.isBlank(foot.getNsecfic())) {
      String msg = "NSECFIC not found";
      log.error(msg);
      throw new Exception(msg);
    }
    if(!header.getNsecfic().equals(foot.getNsecfic())) {
      log.error("NSECFIC does not match");
      throw new Exception("NSECFIC does not match");
    }

    // validar tipo de secuencia
    if(StringUtils.isBlank(header.getTipocinta())) {
      String msg = "TIPOCINTA not found";
      log.error(msg);
      throw new Exception(msg);
    }
    if(!header.getTipocinta().equals(foot.getTipocinta())) {
      String msg = "TIPOCINTA does not match";
      log.error(msg);
      throw new Exception(msg);
    }

    // validar tipo de registro
    if(StringUtils.isBlank(header.getTiporeg())) {
      String msg = "TIPOREG not found";
      log.error(msg);
      throw new Exception(msg);
    }
    if(!"P".equals(foot.getTiporeg())) {
      String msg = "TIPOREG is not [P = PIE]";
      log.error(msg);
      throw new Exception(msg);
    }

    //Validar Fecha
    if(!isValidHeaderDateTimeFormat(foot.getFecenvio(), ReconciliationFileHeader.DATE_FORMAT)) {
      String msg = String.format("Invalid DATE FORMAT %s -> [%s]", ReconciliationFileHeader.DATE_FORMAT, foot.getFecenvio());
      log.error(msg);
      throw new Exception(msg);
    }
    if(!header.getFecenvio().equals(foot.getFecenvio())) {
      String msg = "FECENVIO does not match";
      log.error(msg);
      throw new Exception(msg);
    }
    //Validar Hora
    if(!isValidHeaderDateTimeFormat(foot.getHoraenvio(), ReconciliationFileHeader.TIME_FORMAT)) {
      String msg = String.format("Invalid TIME FORMAT %s -> [%s]", ReconciliationFileHeader.TIME_FORMAT, foot.getHoraenvio());
      log.error(msg);
      throw new Exception(msg);
    }
    if(!header.getHoraenvio().equals(foot.getHoraenvio())) {
      String msg = "HORAENVIO does not match";
      log.error(msg);
      throw new Exception(msg);
    }

    // validar numero de registros
    if(foot.getNumregtot() == null) {
      String msg = "NUMREGTOT not found";
      log.error(msg);
      throw new Exception(msg);
    }

    if(!foot.getNumregtot().equals(header.getNumregtot())) {
      String msg = String.format("Invalid number of records. %d != %d", header.getNumregtot(), foot.getNumregtot());
      log.error(msg);
      throw new Exception(msg);
    }

    if(!foot.getNumregtot().equals(records)) {
      String msg = String.format("Invalid number of records. Found -> %d. Should be -> %d", records, foot.getNumregtot());
      log.error(msg);
      throw new Exception(msg);
    }

    return file;
  }

  public ReconciliationFile validateFile(InputStream data) throws Exception {
    Integer records = 0;

    ReconciliationFile file = new ReconciliationFile();

    Scanner scanner = new Scanner(data);
    try {
      final String headerLine = scanner.hasNextLine() ? scanner.nextLine() : null;
      String footerLine = "";

      file = this.validateHeader(headerLine, file);
      records++;

      String pattern = String.format("%s%s%s", file.getHeader().getCodent(), file.getHeader().getNsecfic(), file.getHeader().getTipocinta());

      while (scanner.hasNextLine()) {
        final String line = scanner.nextLine();
        if (line.startsWith(String.format("%sP", pattern))){
          footerLine = line;
        } else {
          ReconciliationFileDetail detail = new ReconciliationFileDetail(line);
          if(TecnocomOperationType.OP.equals(detail.getOperationType())) {
            file.getDetails().add(new ReconciliationFileDetail(line));
          }
          else if (TecnocomOperationType.AU.equals(detail.getOperationType())){
            file.getDetails().add(new ReconciliationFileDetail(line));
          }
          if(!line.startsWith(String.format("%sD", pattern))) {
            if(!file.isSuspicious()) {
              file.setSuspicious(Boolean.TRUE);
            }
          }
        }
        records++;
      }

      file = this.validateFooter(footerLine, records, file);
    } catch (Exception ex) {
      file.setSuspicious(Boolean.TRUE);
      file.setDetails(Collections.EMPTY_LIST);
    }

    return file;
  }

  public PrepaidMovement10 buildMovement(Long userId, String pan, ReconciliationFileDetail batchTrx) {

    PrepaidMovement10 prepaidMovement = new PrepaidMovement10();

    prepaidMovement.setIdMovimientoRef(null);
    prepaidMovement.setIdPrepaidUser(userId);
    prepaidMovement.setIdTxExterno(null);
    prepaidMovement.setTipoMovimiento(batchTrx.getMovementType());
    prepaidMovement.setMonto(batchTrx.getImpfac());
    prepaidMovement.setEstado(PrepaidMovementStatus.PENDING);
    prepaidMovement.setEstadoNegocio(BusinessStatusType.IN_PROCESS);
    prepaidMovement.setCodent(batchTrx.getCodent());
    prepaidMovement.setCentalta(batchTrx.getCentalta());
    prepaidMovement.setCuenta(batchTrx.getCuenta());
    prepaidMovement.setClamon(CodigoMoneda.fromValue(NumberUtils.getInstance().toInteger(batchTrx.getClamon())));
    prepaidMovement.setIndnorcor(IndicadorNormalCorrector.fromValue(batchTrx.getTipoFac().getCorrector()));
    prepaidMovement.setTipofac(batchTrx.getTipoFac());
    prepaidMovement.setFecfac(Date.valueOf(batchTrx.getFecfac()));
    prepaidMovement.setNumreffac(""); //se debe actualizar despues, es el id de PrepaidMovement10
    prepaidMovement.setPan(pan);
    prepaidMovement.setClamondiv(0);
    prepaidMovement.setImpdiv(0L);
    prepaidMovement.setImpfac(batchTrx.getImpfac());
    prepaidMovement.setCmbapli(0);
    prepaidMovement.setNumaut(batchTrx.getNumaut());
    prepaidMovement.setIndproaje(IndicadorPropiaAjena.AJENA);
    prepaidMovement.setCodcom(batchTrx.getCodcom());
    prepaidMovement.setCodact(NumberUtils.getInstance().toInteger(batchTrx.getCodact()));
    prepaidMovement.setImpliq(0L);
    prepaidMovement.setClamonliq(0);
    prepaidMovement.setCodpais(CodigoPais.fromValue(NumberUtils.getInstance().toInteger(batchTrx.getCodpais())));
    prepaidMovement.setNompob("");
    prepaidMovement.setNumextcta(NumberUtils.getInstance().toInteger(batchTrx.getNumextcta()));
    prepaidMovement.setNummovext(NumberUtils.getInstance().toInteger(batchTrx.getNummovext()));
    prepaidMovement.setClamone(NumberUtils.getInstance().toInteger(batchTrx.getClamon()));
    prepaidMovement.setTipolin(batchTrx.getTipolin());
    prepaidMovement.setLinref(NumberUtils.getInstance().toInteger(batchTrx.getLinref()));
    prepaidMovement.setNumbencta(1);
    prepaidMovement.setNumplastico(0L);

    return prepaidMovement;
  }


}
