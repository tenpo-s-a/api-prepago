package cl.multicaja.test.integration.v10.helper;

import cl.multicaja.camel.CamelFactory;
import cl.multicaja.prepaid.async.v10.routes.CurrencyConvertionRoute10;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.async.v10.routes.TecnocomReconciliationRoute10;
import cl.multicaja.prepaid.async.v10.routes.TransactionReversalRoute10;
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

      TransactionReversalRoute10 transactionReversalRoute10 = new TransactionReversalRoute10();
      transactionReversalRoute10.setPrepaidUserEJBBean10(getPrepaidUserEJBBean10());
      transactionReversalRoute10.setPrepaidCardEJBBean10(getPrepaidCardEJBBean10());
      transactionReversalRoute10.setPrepaidEJBBean10(getPrepaidEJBBean10());
      transactionReversalRoute10.setPrepaidMovementEJBBean10(getPrepaidMovementEJBBean10());
      transactionReversalRoute10.setCdtEJBBean10(getCdtEJBBean10());
      transactionReversalRoute10.setMailPrepaidEJBBean10(getMailPrepaidEJBBean10());

      CurrencyConvertionRoute10 currencyConvertionRoute10 = new CurrencyConvertionRoute10();
      currencyConvertionRoute10.setPrepaidCardEJBBean10(getPrepaidCardEJBBean10());

      TecnocomReconciliationRoute10 tecnocomReconciliationRoute10 = new TecnocomReconciliationRoute10();
      tecnocomReconciliationRoute10.setPrepaidMovementEJBBean10(getPrepaidMovementEJBBean10());
      tecnocomReconciliationRoute10.setPrepaidCardEJBBean10(getPrepaidCardEJBBean10());

      /**
       * Agrega rutas de envio de emails de users pero al camel context de prepago necesario para los test
       */
      //TODO: Revisar esto, creo que no se podra realizar !
     /* UsersEmailRoute10 usersEmailRoute10 = new UsersEmailRoute10();
      usersEmailRoute10.setUsersEJBBean10(getUsersEJBBean10());
      usersEmailRoute10.setMailEJBBean10(getMailEJBBean10());
      */
      camelFactory.startCamelContextWithRoutes(true, prepaidTopupRoute10, currencyConvertionRoute10,transactionReversalRoute10, tecnocomReconciliationRoute10);
    }
    simpleNamingContextBuilder.deactivate();
  }

  public static void destroyCamelContext() throws Exception {
    if (brokerService != null) {
      camelFactory.releaseCamelContext();
      brokerService.stop();
     // TestSftpServer.getInstance().end();
    }
  }

}
