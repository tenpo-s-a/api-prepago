package cl.multicaja.prepaid.async.v10;

import cl.multicaja.camel.CamelFactory;
import cl.multicaja.camel.ExchangeData;
import cl.multicaja.camel.ProcessorMetadata;
import cl.multicaja.prepaid.async.v10.routes.InvoiceRoute10;
import cl.multicaja.prepaid.model.v10.InvoiceData10;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class PrepaidInvoiceDelegate10 {

  private static Log log = LogFactory.getLog(PrepaidInvoiceDelegate10.class);

  private CamelFactory camelFactory = CamelFactory.getInstance();
  private ProducerTemplate producerTemplate;

  public PrepaidInvoiceDelegate10() {
    super();
  }


  /**
   *
   * @return
   */
  private ProducerTemplate getProducerTemplate() {
    if (this.producerTemplate == null) {
      this.producerTemplate = this.camelFactory.createProducerTemplate();
    }
    return producerTemplate;
  }

  /**
   * Envia una solicitud de generacion de Boleta a la cola
   * @param invoiceData10
   * @return
   * @throws JsonProcessingException
   */
  public String sendInvoice(InvoiceData10 invoiceData10) throws JsonProcessingException {

    if (!camelFactory.isCamelRunning()) {
      log.error("====== No fue posible enviar mensaje al proceso asincrono, camel no se encuentra en ejecución =======");
      return null;
    }

    String messageId = String.format("%s#%s#%d#", invoiceData10.getType(),invoiceData10.getMovementId(),invoiceData10.getRut());

    Map<String, Object> headers = new HashMap<>();
    headers.put("JMSCorrelationID", messageId);
    ObjectMapper Obj = new ObjectMapper();

    ExchangeData<String> req = new ExchangeData<>( Obj.writeValueAsString(invoiceData10));
    req.getProcessorMetadata().add(new ProcessorMetadata(0, InvoiceRoute10.SEDA_ENDPOINT));
    this.getProducerTemplate().sendBodyAndHeaders(InvoiceRoute10.SEDA_ENDPOINT, req, headers);

    return messageId;

  }

  //Todo: Completar los datos
  public InvoiceData10 buildInvoiceData(PrepaidMovement10 prepaidMovement10, PrepaidUser10 prepaidUser10) {
    InvoiceData10 invoiceData10 = new InvoiceData10();
    invoiceData10.setType(prepaidMovement10.getTipoMovimiento());
    invoiceData10.setMovementId(prepaidMovement10.getId());
    invoiceData10.setRut(prepaidUser10.getRut());
    invoiceData10.setDv("");
    invoiceData10.setAmount(prepaidMovement10.getMonto());
    invoiceData10.setAmountPaid(BigDecimal.TEN);
    invoiceData10.setClientName("asdasd asdasd");
    return invoiceData10;
  }
}
