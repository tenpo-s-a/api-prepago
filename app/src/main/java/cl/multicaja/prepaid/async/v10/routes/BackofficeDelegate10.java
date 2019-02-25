package cl.multicaja.prepaid.async.v10.routes;

import cl.multicaja.camel.CamelFactory;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.prepaid.async.v10.routes.BackofficeReportRoute10.DIRECT_E06_REPORT_UPLOAD_ENDPOINT;

public class BackofficeDelegate10 {

  private static Log log = LogFactory.getLog(BackofficeDelegate10.class);
  private CamelFactory camelFactory = CamelFactory.getInstance();
  private ProducerTemplate producerTemplate;

  public BackofficeDelegate10() {
    super();
  }

  public ProducerTemplate getProducerTemplate() {
    if (this.producerTemplate == null) {
      this.producerTemplate = this.camelFactory.createProducerTemplate();
    }
    return producerTemplate;
  }

  /**
   * Envia envia un archivo CSV a la casilla sftp
   *
   * @param fileName
   * @return
   */
  public void uploadE06ReportFile(String fileName) {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
    } else {
      Map<String, Object> headers = new HashMap<>();
      headers.put(Exchange.FILE_NAME, fileName);

      this.getProducerTemplate().sendBodyAndHeaders(DIRECT_E06_REPORT_UPLOAD_ENDPOINT, "", headers);
    }
  }

}
