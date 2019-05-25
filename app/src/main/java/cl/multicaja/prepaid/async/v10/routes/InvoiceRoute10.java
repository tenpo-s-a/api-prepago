package cl.multicaja.prepaid.async.v10.routes;

public class InvoiceRoute10 extends BaseRoute10{

  public static final String SEDA_ENDPOINT = "seda:InvoiceRoute10.pendingRequest";
  public static final String INVOICE_ENDPOINT = "InvoiceRoute10.pendingInvoice.req";

  @Override
  public void configure() throws Exception{
    int concurrentConsumers = 10;
    int sedaSize = 1000;
    from(String.format("%s?concurrentConsumers=%s&size=%s",SEDA_ENDPOINT,concurrentConsumers,sedaSize))
      .to(createJMSEndpoint(INVOICE_ENDPOINT));
  }
}
