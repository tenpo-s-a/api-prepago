package cl.multicaja.test.integration.v10.async;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.prepaid.async.v10.routes.InvoiceRoute10;
import cl.multicaja.prepaid.model.v10.InvoiceData10;
import cl.multicaja.prepaid.model.v10.PrepaidMovementType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import javax.jms.Queue;

public class Test_PendingInvoice10  extends TestBaseUnitAsync {

  @Test
  public void pendingInvoiceOK() throws Exception {

    InvoiceData10 invoiceData10 = new InvoiceData10();
    invoiceData10.setRut(getUniqueRutNumber());
    invoiceData10.setMovementId(getUniqueLong());
    invoiceData10.setType(PrepaidMovementType.PURCHASE);
    invoiceData10.setClientName(getRandomString(10));

    String messageId = sendPendingInvoice(invoiceData10,0);

    //se verifica que el mensaje haya sido procesado por el proceso asincrono y lo busca en la cola de emisiones pendientes
    Queue qResp = camelFactory.createJMSQueue(InvoiceRoute10.INVOICE_ENDPOINT);

    ExchangeData<String> remoteInvoice = (ExchangeData<String>)camelFactory.createJMSMessenger().getMessage(qResp, messageId);
    Assert.assertNotNull("Deberia existir un mensaje en la cola de Invoice", remoteInvoice);
    Assert.assertNotNull("Deberia existir un mensaje en la cola de Invoice", remoteInvoice.getData());
    System.out.println(remoteInvoice.getData());
    ObjectMapper mapper = new ObjectMapper();
    InvoiceData10 invoiceDataInQueue = mapper.readValue(remoteInvoice.getData(),InvoiceData10.class);

    Assert.assertEquals("Rut debe ser igual",invoiceData10.getRut(),invoiceDataInQueue.getRut());
    Assert.assertEquals("MovementID debe ser igual",invoiceData10.getMovementId(),invoiceDataInQueue.getMovementId());
    Assert.assertEquals("Type debe ser igual",invoiceData10.getType(),invoiceDataInQueue.getType());
    Assert.assertEquals("Client Name debe ser igual",invoiceData10.getClientName(),invoiceDataInQueue.getClientName());

  }

}
