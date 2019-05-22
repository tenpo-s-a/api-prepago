package cl.multicaja.test.integration.v10.helper;

import cl.multicaja.camel.CamelFactory;
import cl.multicaja.prepaid.async.v10.routes.*;
import cl.multicaja.prepaid.helpers.CalculationsHelper;
import cl.multicaja.test.integration.TestSuite;
import cl.multicaja.test.integration.v10.unit.TestBaseUnit;
import org.apache.activemq.broker.BrokerService;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import javax.naming.spi.NamingManager;


public class TestContextHelper extends TestBaseUnit {

  protected static CamelFactory camelFactory = CamelFactory.getInstance();
  protected static BrokerService brokerService;

  public static void initCamelContext() throws  Exception {

    SimpleNamingContextBuilder simpleNamingContextBuilder = new SimpleNamingContextBuilder();
    //Por un extraño conflicto con payara cuando no se usa, se debe sobre-escribir el InitialContext por defecto
    //sino se lanza un NullPointerException en camel producto de la existencia de payara.
    if (!NamingManager.hasInitialContextFactoryBuilder() || !TestSuite.isServerRunning()) {
      simpleNamingContextBuilder.activate();
    }
    //independiente de la configuración obliga a que el activemq no sea persistente en disco
    getConfigUtils().setProperty("activemq.broker.embedded.persistent","false");
    //crea e inicia apache camel con las rutas creadas anteriormente

    if (!camelFactory.isCamelRunning()) {

      //Inicializa servidor sftp embebido

      //crea e inicia el activemq
      brokerService = camelFactory.createBrokerService();
      brokerService.start();
      //Inicializa las rutas camel, se inicializa aun cuando no se incluya en camel, se crea dado que de
      // ella depende la instancia de tecnocomService
      PrepaidTopupRoute10 prepaidTopupRoute10 = new PrepaidTopupRoute10();
      prepaidTopupRoute10.setPrepaidUserEJBBean10(getPrepaidUserEJBBean10());
      prepaidTopupRoute10.setPrepaidCardEJBBean10(getPrepaidCardEJBBean10());
      prepaidTopupRoute10.setPrepaidEJBBean10(getPrepaidEJBBean10());
      prepaidTopupRoute10.setPrepaidMovementEJBBean10(getPrepaidMovementEJBBean10());
      prepaidTopupRoute10.setCdtEJBBean10(getCdtEJBBean10());
      prepaidTopupRoute10.setMailPrepaidEJBBean10(getMailPrepaidEJBBean10());
      prepaidTopupRoute10.setPrepaidAccountingEJBBean10(getPrepaidAccountingEJBBean10());
      prepaidTopupRoute10.setPrepaidClearingEJBBean10(getPrepaidClearingEJBBean10());
      prepaidTopupRoute10.setAccountEJBBean10(getAccountEJBBean10());
      prepaidTopupRoute10.setPrepaidCardEJBBean11(getPrepaidCardEJBBean11());
      prepaidTopupRoute10.setPrepaidMovementEJBBean11(getPrepaidMovementEJBBean11());

      TransactionReversalRoute10 transactionReversalRoute10 = new TransactionReversalRoute10();
      transactionReversalRoute10.setPrepaidUserEJBBean10(getPrepaidUserEJBBean10());
      transactionReversalRoute10.setPrepaidCardEJBBean10(getPrepaidCardEJBBean10());
      transactionReversalRoute10.setPrepaidEJBBean10(getPrepaidEJBBean10());
      transactionReversalRoute10.setPrepaidMovementEJBBean10(getPrepaidMovementEJBBean10());
      transactionReversalRoute10.setCdtEJBBean10(getCdtEJBBean10());
      transactionReversalRoute10.setMailPrepaidEJBBean10(getMailPrepaidEJBBean10());
      transactionReversalRoute10.setAccountEJBBean10(getAccountEJBBean10());
      transactionReversalRoute10.setPrepaidCardEJBBean11(getPrepaidCardEJBBean11());
      transactionReversalRoute10.setPrepaidMovementEJBBean11(getPrepaidMovementEJBBean11());

      CurrencyConvertionRoute10 currencyConvertionRoute10 = new CurrencyConvertionRoute10();
      currencyConvertionRoute10.setPrepaidCardEJBBean10(getPrepaidCardEJBBean10());

      TecnocomReconciliationRoute10 tecnocomReconciliationRoute10 = new TecnocomReconciliationRoute10();
      tecnocomReconciliationRoute10.setPrepaidMovementEJBBean10(getPrepaidMovementEJBBean10());
      tecnocomReconciliationRoute10.setPrepaidCardEJBBean10(getPrepaidCardEJBBean10());
      tecnocomReconciliationRoute10.setTecnocomReconciliationEJBBean10(getTecnocomReconciliationEJBBean10());
      tecnocomReconciliationRoute10.setAccountEJBBean10(getAccountEJBBean10());

      ProductChangeRoute10 productChangeRoute10 = new ProductChangeRoute10();
      productChangeRoute10.setMailPrepaidEJBBean10(getMailPrepaidEJBBean10());
      productChangeRoute10.setPrepaidUserEJBBean10(getPrepaidUserEJBBean10());
      productChangeRoute10.setPrepaidCardEJBBean11(getPrepaidCardEJBBean11());

      MailRoute10 mailRoute10 = new MailRoute10(); // Agregar ruta al cammel para probar los emails salientes
      mailRoute10.setPrepaidEJBBean10(getPrepaidEJBBean10());

      InvoiceRoute10 invoiceRoute10 = new InvoiceRoute10();

      KafkaEventsRoute10 kafkaEventsRoute10 = new KafkaEventsRoute10();
      kafkaEventsRoute10.setPrepaidUserEJBBean10(getPrepaidUserEJBBean10());
      kafkaEventsRoute10.setAccountEJBBean10(getAccountEJBBean10());

      camelFactory.startCamelContextWithRoutes(true,
        prepaidTopupRoute10, transactionReversalRoute10, productChangeRoute10, mailRoute10,invoiceRoute10, kafkaEventsRoute10);
    }
    simpleNamingContextBuilder.deactivate();
  }

  public static void destroyCamelContext() throws Exception {
    if (brokerService != null) {
      camelFactory.releaseCamelContext();
      brokerService.stop();
    }
  }

}
