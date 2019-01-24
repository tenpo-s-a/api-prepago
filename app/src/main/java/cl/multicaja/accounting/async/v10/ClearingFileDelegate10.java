package cl.multicaja.accounting.async.v10;

import cl.multicaja.camel.CamelFactory;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

import static cl.multicaja.accounting.async.v10.routes.ClearingFileRoute10.DIRECT_CLEARING_UPLOAD_ENDPOINT;

/**
 * Clase delegate que permite iniciar los procesos asincronos
 *
 * @author abarazarte
 **/
public class ClearingFileDelegate10 {

  private static Log log = LogFactory.getLog(ClearingFileDelegate10.class);

  private CamelFactory camelFactory = CamelFactory.getInstance();

  private ProducerTemplate producerTemplate;

  public ClearingFileDelegate10() {
    super();
  }

  /**
   *
   * @return
   */
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
  public void uploadFile(String fileName) {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
    } else {
      Map<String, Object> headers = new HashMap<>();
      headers.put(Exchange.FILE_NAME, fileName);

      this.getProducerTemplate().sendBodyAndHeaders(DIRECT_CLEARING_UPLOAD_ENDPOINT, "", headers);
    }
  }
}
