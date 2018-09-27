package cl.multicaja.prepaid.helpers.tecnocom;

import cl.multicaja.prepaid.helpers.tecnocom.model.ReconciliationFile;
import cl.multicaja.prepaid.helpers.tecnocom.model.ReconciliationFileDetail;
import cl.multicaja.prepaid.helpers.tecnocom.model.ReconciliationFileHeader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

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
      log.error("Header not found");
      throw new Exception("Header not found");
    }

    file.setHeader(new ReconciliationFileHeader(header));

    // validar codigo de entidad
    if(StringUtils.isBlank(file.getHeader().getCodent())) {
      log.error("CODENT not found");
      throw new Exception("CODENT not found");
    }

    // validar numero de secuencia
    if(StringUtils.isBlank(file.getHeader().getNsecfic())) {
      log.error("NSECFIC not found");
      throw new Exception("NSECFIC not found");
    }

    // validar tipo de secuencia
    if(StringUtils.isBlank(file.getHeader().getTipocinta())) {
      log.error("TIPOCINTA not found");
      throw new Exception("TIPOCINTA not found");
    }

    // validar tipo de registro
    if(StringUtils.isBlank(file.getHeader().getTiporeg())) {
      log.error("TIPOREG not found");
      throw new Exception("TIPOREG not found");
    }
    if(!"C".equals(file.getHeader().getTiporeg())) {
      log.error("TIPOREG is not [C = CABECERA]");
      throw new Exception("TIPOREG is not [C = CABECERA]");
    }

    //Validar Fecha
    if(!isValidHeaderDateTimeFormat(file.getHeader().getFecenvio(), ReconciliationFileHeader.DATE_FORMAT)) {
      log.error(String.format("Invalid DATE FORMAT %s -> [%s]", ReconciliationFileHeader.DATE_FORMAT, file.getHeader().getFecenvio()));
      throw new Exception(String.format("Invalid DATE FORMAT %s -> [%s]", ReconciliationFileHeader.DATE_FORMAT, file.getHeader().getFecenvio()));
    }
    //Validar Hora
    if(!isValidHeaderDateTimeFormat(file.getHeader().getHoraenvio(), ReconciliationFileHeader.TIME_FORMAT)) {
      log.error(String.format("Invalid TIME FORMAT %s -> [%s]", ReconciliationFileHeader.TIME_FORMAT, file.getHeader().getHoraenvio()));
      throw new Exception(String.format("Invalid TIME FORMAT %s -> [%s]", ReconciliationFileHeader.TIME_FORMAT, file.getHeader().getHoraenvio()));
    }

    // validar numero de registros
    if(file.getHeader().getNumregtot() == null) {
      log.error("NUMREGTOT not found");
      throw new Exception("NUMREGTOT not found");
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
      log.error("CODENT not found");
      throw new Exception("CODENT not found");
    }
    if(!header.getCodent().equals(foot.getCodent())) {
      log.error("CODENT does not match");
      throw new Exception("CODENT does not match");
    }

    // validar numero de secuencia
    if(StringUtils.isBlank(foot.getNsecfic())) {
      log.error("NSECFIC not found");
      throw new Exception("NSECFIC not found");
    }
    if(!header.getNsecfic().equals(foot.getNsecfic())) {
      log.error("NSECFIC does not match");
      throw new Exception("NSECFIC does not match");
    }

    // validar tipo de secuencia
    if(StringUtils.isBlank(header.getTipocinta())) {
      log.error("TIPOCINTA not found");
      throw new Exception("TIPOCINTA not found");
    }
    if(!header.getTipocinta().equals(foot.getTipocinta())) {
      log.error("TIPOCINTA does not match");
      throw new Exception("TIPOCINTA does not match");
    }

    // validar tipo de registro
    if(StringUtils.isBlank(header.getTiporeg())) {
      log.error("TIPOREG not found");
      throw new Exception("TIPOREG not found");
    }
    if(!"P".equals(foot.getTiporeg())) {
      log.error("TIPOREG is not [P = PIE]");
      throw new Exception("TIPOREG is not [P = PIE]");
    }

    //Validar Fecha
    if(!isValidHeaderDateTimeFormat(foot.getFecenvio(), ReconciliationFileHeader.DATE_FORMAT)) {
      log.error(String.format("Invalid DATE FORMAT %s -> [%s]", ReconciliationFileHeader.DATE_FORMAT, foot.getFecenvio()));
      throw new Exception(String.format("Invalid DATE FORMAT %s -> [%s]", ReconciliationFileHeader.DATE_FORMAT, foot.getFecenvio()));
    }
    if(!header.getFecenvio().equals(foot.getFecenvio())) {
      log.error("FECENVIO does not match");
      throw new Exception("FECENVIO does not match");
    }
    //Validar Hora
    if(!isValidHeaderDateTimeFormat(foot.getHoraenvio(), ReconciliationFileHeader.TIME_FORMAT)) {
      log.error(String.format("Invalid TIME FORMAT %s -> [%s]", ReconciliationFileHeader.TIME_FORMAT, foot.getHoraenvio()));
      throw new Exception(String.format("Invalid TIME FORMAT %s -> [%s]", ReconciliationFileHeader.TIME_FORMAT, foot.getHoraenvio()));
    }
    if(!header.getHoraenvio().equals(foot.getHoraenvio())) {
      log.error("HORAENVIO does not match");
      throw new Exception("HORAENVIO does not match");
    }

    // validar numero de registros
    if(foot.getNumregtot() == null) {
      log.error("NUMREGTOT not found");
      throw new Exception("NUMREGTOT not found");
    }
    if(!foot.getNumregtot().equals(records)) {
      log.error(String.format("Invalid number of records. Found -> %d. Should be -> %d", records, foot.getNumregtot()));
      throw new Exception(String.format("Invalid number of records. Found -> %d. Should be -> %d", records, foot.getNumregtot()));
    }

    return file;
  }

  public ReconciliationFile validateFile(String data) throws Exception {
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
          if(ReconciliationFileDetail.OPERATION_TYPE.equals(detail.getTiporeg())) {
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
          if(ReconciliationFileDetail.OPERATION_TYPE.equals(detail.getTiporeg())) {
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
    }

    return file;
  }

  /**
   * Insertar en la tabla prp_movimientos, las transcacciones realizadas de forma manual por SAT.
   * @param trxs
   */
  public void insertOrUpdateManualTrx(List<ReconciliationFileDetail> trxs) {
    for(int i = 0; i < trxs.size(); i++) {
      System.out.println(String.format("%d - %s", i, trxs.get(i).toString()));
    }
  }

  public void validateTransactions(List<ReconciliationFileDetail> trxs) {
    for(int i = 0; i < trxs.size(); i++) {
      System.out.println(String.format("%d - %s", i, trxs.get(i).toString()));
    }
  }

  public static void main(String[] args) throws Exception {

    TecnocomFileHelper fh = new TecnocomFileHelper();

    String data = "0987000000000170C2018-05-2414.01.0200000000025                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         \n" +
      "0987000000000170D                                                                                           000000000001098700010000000000025176081136288807      OP000000000001152PESO DE CHILE                 0NORMAL         3001CARGA TRANSFERENCIA           2018-06-0400000000000000000000000000                              -00000000000000000-00000000000030000000000000885199000000000000021                           6012INSTITUCIONES FINANCIAERAS    -00000000000030000152PESO DE CHILE                 -000000000000000002018-06-04152CHILE                                                   2018-05-23MAUTAUTORIZADOR         0000                    000001377743000000000000000000000000000                      00                                        +0000000000000000000000080012018-06-0400000000000000002N0NO ANULADO0MOVIMIENTO NO RETENIDO0PENDIENTE                                        0000000000000000000000+000000000000000001CREDITO   LREV                    00                    00000000    \n" +
      "0987000000000170D                                                                                           000000000002098700010000000000025176081136288807      OP000000000002152PESO DE CHILE                 0NORMAL         3001CARGA TRANSFERENCIA           2018-05-2900000000000000000000000000                              -00000000000000000-00000000000010000000000000789779000000000000021                           6012INSTITUCIONES FINANCIAERAS    -00000000000010000152PESO DE CHILE                 -000000000000000002018-05-31152CHILE                                                   2018-05-23MAUTAUTORIZADOR         0000                    000001377479000000000000000000000000000                      00                                        +0000000000000000000000020012018-05-3100000000000000002N0NO ANULADO0MOVIMIENTO NO RETENIDO0PENDIENTE                                        0000000000000000000000+000000000000000001CREDITO   LREV                    00                    00000000    \n" +
      "0987000000000170D                                                                                           000000000003098700010000000000025176081136288807      OP000000000003152PESO DE CHILE                 0NORMAL         3001CARGA TRANSFERENCIA           2018-06-0400000000000000000000000000                              -00000000000000000-00000000000030000000000000784199000000000000021                           6012INSTITUCIONES FINANCIAERAS    -00000000000030000152PESO DE CHILE                 -000000000000000002018-06-04152CHILE                                                   2018-05-23MAUTAUTORIZADOR         0000                    000001377742000000000000000000000000000                      00                                        +0000000000000000000000070012018-06-0400000000000000002N0NO ANULADO0MOVIMIENTO NO RETENIDO0PENDIENTE                                        0000000000000000000000+000000000000000001CREDITO   LREV                    00                    00000000    \n" +
      "0987000000000170D                                                                                           000000000004098700010000000000025176081136288807      OP000000000004152PESO DE CHILE                 0NORMAL         3001CARGA TRANSFERENCIA           2018-06-0400000000000000000000000000                              -00000000000000000-00000000000030000000000000785199000000000000021                           6012INSTITUCIONES FINANCIAERAS    -00000000000030000152PESO DE CHILE                 -000000000000000002018-06-06152CHILE                                                   2018-05-23MAUTAUTORIZADOR         0000                    000001377790000000000000000000000000000                      00                                        +0000000000000000000000130012018-06-0600000000000000002N0NO ANULADO0MOVIMIENTO NO RETENIDO0PENDIENTE                                        0000000000000000000000+000000000000000001CREDITO   LREV                    00                    00000000    \n" +
      "0987000000000170D                                                                                           000000000005098700010000000000025176081136288807      OP000000000005152PESO DE CHILE                 0NORMAL         3001CARGA TRANSFERENCIA           2018-05-2900000000000000000000000000                              -00000000000000000-00000000000010000000000000789698000000000000021                           6012INSTITUCIONES FINANCIAERAS    -00000000000010000152PESO DE CHILE                 -000000000000000002018-05-31152CHILE                                                   2018-05-23MAUTAUTORIZADOR         0000                    000001377464000000000000000000000000000                      00                                        +0000000000000000000000010012018-05-3100000000000000002N0NO ANULADO0MOVIMIENTO NO RETENIDO0PENDIENTE                                        0000000000000000000000+000000000000000001CREDITO   LREV                    00                    00000000    \n" +
      "0987000000000170D                                                                                           000000000006098700010000000000025176081136288807      OP000000000006152PESO DE CHILE                 0NORMAL         3005REEMISION DE TARJETA          2018-06-0500000000000000000000000000                              +00000000000000000+00000000000000100000000000474199000000000000021                           6012INSTITUCIONES FINANCIAERAS    +00000000000000100152PESO DE CHILE                 +000000000000000002018-06-05152CHILE                                                   2018-05-23MAUTAUTORIZADOR         0000                    000001377749000000000000000000000000000                      00                                        +0000000000000000000000120012018-06-0500000000000000002N0NO ANULADO0MOVIMIENTO NO RETENIDO0PENDIENTE                                        0000000000000000000000+000000000000000001CREDITO   LREV                    00                    00000000    \n" +
      "0987000000000170D                                                                                           000000000007098700010000000000025176081136288807      OP000000000007152PESO DE CHILE                 0NORMAL         3001CARGA TRANSFERENCIA           2018-05-2900000000000000000000000000                              -00000000000000000-00000000000030000000000000782799000000000000021                           6012INSTITUCIONES FINANCIAERAS    -00000000000030000152PESO DE CHILE                 -000000000000000002018-06-01152CHILE                                                   2018-05-23MAUTAUTORIZADOR         0000                    000001377547000000000000000000000000000                      00                                        +0000000000000000000000040012018-06-0100000000000000002N0NO ANULADO0MOVIMIENTO NO RETENIDO0PENDIENTE                                        0000000000000000000000+000000000000000001CREDITO   LREV                    00                    00000000    \n" +
      "0987000000000170D                                                                                           000000000008098700010000000000025176081136288807      OP000000000008152PESO DE CHILE                 0NORMAL         3001CARGA TRANSFERENCIA           2018-06-0500000000000000000000000000                              -00000000000000000-00000000000030000000000000774199000000000000021                           6012INSTITUCIONES FINANCIAERAS    -00000000000030000152PESO DE CHILE                 -000000000000000002018-06-05152CHILE                                                   2018-05-23MAUTAUTORIZADOR         0000                    000001377746000000000000000000000000000                      00                                        +0000000000000000000000090012018-06-0500000000000000002N0NO ANULADO0MOVIMIENTO NO RETENIDO0PENDIENTE                                        0000000000000000000000+000000000000000001CREDITO   LREV                    00                    00000000    \n" +
      "0987000000000170D                                                                                           000000000009098700010000000000025176081136288807      OP000000000009152PESO DE CHILE                 0NORMAL         3003RETIRO TRANSFERENCIA          2018-06-0500000000000000000000000000                              +00000000000000000+00000000000020000000000000974199000000000000021                           6012INSTITUCIONES FINANCIAERAS    +00000000000020000152PESO DE CHILE                 +000000000000000002018-06-05152CHILE                                                   2018-05-23MAUTAUTORIZADOR         0000                    000001377747000000000000000000000000000                      00                                        +0000000000000000000000100012018-06-0500000000000000002N0NO ANULADO0MOVIMIENTO NO RETENIDO0PENDIENTE                                        0000000000000000000000+000000000000000001CREDITO   LREV                    00                    00000000    \n" +
      "0987000000000170D                                                                                           000000000010098700010000000000025176081136288807      OP000000000010152PESO DE CHILE                 0NORMAL         3000COMISION APERTURA             2018-06-0500000000000000000000000000                              +00000000000000000+00000000000000100000000000474199000000000000021                           6012INSTITUCIONES FINANCIAERAS    +00000000000000100152PESO DE CHILE                 +000000000000000002018-06-05152CHILE                                                   2018-05-23MAUTAUTORIZADOR         0000                    000001377748000000000000000000000000000                      00                                        +0000000000000000000000110012018-06-0500000000000000002N0NO ANULADO0MOVIMIENTO NO RETENIDO0PENDIENTE                                        0000000000000000000000+000000000000000001CREDITO   LREV                    00                    00000000    \n" +
      "0987000000000170D                                                                                           000000000011098700010000000000025176081136288807      OP000000000011152PESO DE CHILE                 0NORMAL         3001CARGA TRANSFERENCIA           2018-06-0400000000000000000000000000                              -00000000000000000-00000000000030000000000000725199000000000000021                           6012INSTITUCIONES FINANCIAERAS    -00000000000030000152PESO DE CHILE                 -000000000000000002018-06-04152CHILE                                                   2018-05-23MAUTAUTORIZADOR         0000                    000001377728000000000000000000000000000                      00                                        +0000000000000000000000050012018-06-0400000000000000002N0NO ANULADO0MOVIMIENTO NO RETENIDO0PENDIENTE                                        0000000000000000000000+000000000000000001CREDITO   LREV                    00                    00000000    \n" +
      "0987000000000170D                                                                                           000000000012098700010000000000025176081136288807      OP000000000012152PESO DE CHILE                 0NORMAL         3001CARGA TRANSFERENCIA           2018-06-0400000000000000000000000000                              -00000000000000000-00000000000030000000000000704199000000000000021                           6012INSTITUCIONES FINANCIAERAS    -00000000000030000152PESO DE CHILE                 -000000000000000002018-06-04152CHILE                                                   2018-05-23MAUTAUTORIZADOR         0000                    000001377741000000000000000000000000000                      00                                        +0000000000000000000000060012018-06-0400000000000000002N0NO ANULADO0MOVIMIENTO NO RETENIDO0PENDIENTE                                        0000000000000000000000+000000000000000001CREDITO   LREV                    00                    00000000    \n" +
      "0987000000000170D                                                                                           000000000013098700010000000000025176081136288807      OP000000000013152PESO DE CHILE                 0NORMAL         3001CARGA TRANSFERENCIA           2018-05-2900000000000000000000000000                              -00000000000000000-00000000000010000000000000784790000000000000021                           6012INSTITUCIONES FINANCIAERAS    -00000000000010000152PESO DE CHILE                 -000000000000000002018-06-01152CHILE                                                   2018-05-23MAUTAUTORIZADOR         0000                    000001377533000000000000000000000000000                      00                                        +0000000000000000000000030012018-06-0100000000000000002N0NO ANULADO0MOVIMIENTO NO RETENIDO0PENDIENTE                                        0000000000000000000000+000000000000000001CREDITO   LREV                    00                    00000000    \n" +
      "0987000000000170D                                                                                           000000000016098700010000000000025176081136288807      CE00000000001600000120010000000+                 +                                                          0000000000000000000000000003005REEMISI�N DE TARJETA          02COMISION                      +00000000000000100+00000000000083200-00000000000000000+00000000000000000                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     \n" +
      "0987000000000170D                                                                                           000000000017098700010000000000035176081115089002      OP000000000017152PESO DE CHILE                 0NORMAL         3003RETIRO TRANSFERENCIA          2018-05-29                       000                              +00000000000000000+00000000000020000000000000      000000000000021COMERCIO PRUEBA CONSULTA   6012INSTITUCIONES FINANCIAERAS    +00000000000020000152PESO DE CHILE                 +000000000000000002018-05-29152CHILE                                                   2018-05-23ONLIONLINE              0000                    000001377356000000000000000000000000000                      00                                        +0000000000000000000000030012018-05-2900000000000000003N0NO ANULADO0MOVIMIENTO NO RETENIDO0PENDIENTE                                        0000000000000000000000+000000000000000001CREDITO   LREV                    00                    00000000    \n" +
      "0987000000000170D                                                                                           000000000018098700010000000000035176081115089002      OP000000000018152PESO DE CHILE                 0NORMAL         3002CARGA EFECTIVO COMERC MULTICAJ2018-05-29                       000                              -00000000000000000-00000000000300000000000000      000000000000021PRUEBAS                    6012INSTITUCIONES FINANCIAERAS    -00000000000300000152PESO DE CHILE                 -000000000000000002018-05-29152CHILE                                                   2018-05-23ONLIONLINE              0000                    000001377355000000000000000000000000000                      00                                        +0000000000000000000000020012018-05-2900000000000000003N0NO ANULADO0MOVIMIENTO NO RETENIDO0PENDIENTE                                        0000000000000000000000+000000000000000001CREDITO   LREV                    00                    00000000    \n" +
      "0987000000000170D                                                                                           000000000019098700010000000000035176081115089002      OP000000000019152PESO DE CHILE                 0NORMAL         3001CARGA TRANSFERENCIA           2018-05-29                       000                              -00000000000000000-00000000000200000000000000      000000000999999                           5812RESTAURANTES                  -00000000000200000152PESO DE CHILE                 -000000000000000002018-05-29152CHILE                                                   2018-05-23ONLIONLINE              0000                    000001377354000000000000000000000000000                      00                                        +0000000000000000000000010012018-05-2900000000000000003N0NO ANULADO0MOVIMIENTO NO RETENIDO0PENDIENTE                                        0000000000000000000000+000000000000000001CREDITO   LREV                    00                    00000000    \n" +
      "0987000000000170D                                                                                           000000000022098700010000000000125176081135830583      OP000000000022152PESO DE CHILE                 0NORMAL         3001CARGA TRANSFERENCIA           2018-06-04                       000                              -00000000000000000-00000000025000000000000000      000000000000021                           6012INSTITUCIONES FINANCIAERAS    -00000000025000000152PESO DE CHILE                 -000000000000000002018-06-04152CHILE                                                   2018-05-23ONLIONLINE              0000                    000001377727000000000000000000000000000                      00                                        +0000000000000000000000010012018-06-0400000000000000012N0NO ANULADO0MOVIMIENTO NO RETENIDO0PENDIENTE                                        0000000000000000000000+000000000000000001CREDITO   LREV                    00                    00000000    \n" +
      "0987000000000170D                                                                                           000000000093098700010000000000135176081111866841      OP000000000093152PESO DE CHILE                 0NORMAL         3001CARGA TRANSFERENCIA           2018-05-2900000000000000000000000000                              -00000000000000000-00000000049000000000000000788700000000000000013                           6012INSTITUCIONES FINANCIAERAS    -00000000049000000152PESO DE CHILE                 -000000000000000002018-06-08152CHILE                                                   2018-05-23MAUTAUTORIZADOR         0000                    000001378006000000000000000000000000000                      00                                        +0000000000000000000000010012018-06-0800000000000000013N0NO ANULADO0MOVIMIENTO NO RETENIDO0PENDIENTE                                        0000000000000000000000+000000000000000001CREDITO   LREV                    00                    00000000    \n" +
      "0987000000000170D                                                                                           000000000193098700010000000000175176081155040006      OP000000000193152PESO DE CHILE                 0NORMAL         3001CARGA TRANSFERENCIA           2018-05-2900000000000000000000000000                              -00000000000000000-00000000001000000000000000787790000000000000021                           6012INSTITUCIONES FINANCIAERAS    -00000000001000000152PESO DE CHILE                 -000000000000000002018-06-06152CHILE                                                   2018-05-23MAUTAUTORIZADOR         0000                    000001377800000000000000000000000000000                      00                                        +0000000000000000000000020012018-06-0600000000000000017N0NO ANULADO0MOVIMIENTO NO RETENIDO0PENDIENTE                                        0000000000000000000000+000000000000000001CREDITO   LREV                    00                    00000000    \n" +
      "0987000000000170D                                                                                           000000000194098700010000000000175176081155040006      OP000000000194152PESO DE CHILE                 0NORMAL         3001CARGA TRANSFERENCIA           2018-05-2900000000000000000000000000                              -00000000000000000-00000000003900000000000000787770000000000000021                           6012INSTITUCIONES FINANCIAERAS    -00000000003900000152PESO DE CHILE                 -000000000000000002018-06-06152CHILE                                                   2018-05-23MAUTAUTORIZADOR         0000                    000001377801000000000000000000000000000                      00                                        +0000000000000000000000030012018-06-0600000000000000017N0NO ANULADO0MOVIMIENTO NO RETENIDO0PENDIENTE                                        0000000000000000000000+000000000000000001CREDITO   LREV                    00                    00000000    \n" +
      "0987000000000170D                                                                                           000000000195098700010000000000175176081155040006      OP000000000195152PESO DE CHILE                 0NORMAL         3001CARGA TRANSFERENCIA           2018-05-2900000000000000000000000000                              -00000000000000000-00000000000090000000000000787700000000000000021                           6012INSTITUCIONES FINANCIAERAS    -00000000000090000152PESO DE CHILE                 -000000000000000002018-06-06152CHILE                                                   2018-05-23MAUTAUTORIZADOR         0000                    000001377802000000000000000000000000000                      00                                        +0000000000000000000000040012018-06-0600000000000000017N0NO ANULADO0MOVIMIENTO NO RETENIDO0PENDIENTE                                        0000000000000000000000+000000000000000001CREDITO   LREV                    00                    00000000    \n" +
      "0987000000000170D                                                                                           000000000196098700010000000000175176081155040006      OP000000000196152PESO DE CHILE                 0NORMAL         3001CARGA TRANSFERENCIA           2018-05-2900000000000000000000000000                              -00000000000000000-00000000000010000000000000789790000000000000021                           6012INSTITUCIONES FINANCIAERAS    -00000000000010000152PESO DE CHILE                 -000000000000000002018-06-06152CHILE                                                   2018-05-23MAUTAUTORIZADOR         0000                    000001377799000000000000000000000000000                      00                                        +0000000000000000000000010012018-06-0600000000000000017N0NO ANULADO0MOVIMIENTO NO RETENIDO0PENDIENTE                                        0000000000000000000000+000000000000000001CREDITO   LREV                    00                    00000000    \n" +
      "0987000000000170P2018-05-2414.01.0200000000025                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         \n";

    ReconciliationFile file = fh.validateFile(data);
    System.out.println(file.getDetails().size());
    fh.insertOrUpdateManualTrx(file.getDetails()
      .stream()
      .filter(detail -> detail.isFromSat())
      .collect(Collectors.toList())
    );

    System.out.println("");
    System.out.println("");

    fh.validateTransactions(file.getDetails()
      .stream()
      .filter(detail -> !detail.isFromSat())
      .collect(Collectors.toList())
    );
  }

}
