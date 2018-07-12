package cl.multicaja.test.v10.api;

import cl.multicaja.camel.CamelFactory;
import cl.multicaja.core.test.TestApiBase;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.http.HttpResponse;
import cl.multicaja.prepaid.async.v10.routes.PrepaidTopupRoute10;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.test.TestSuite;
import cl.multicaja.test.v10.unit.TestBaseUnit;
import cl.multicaja.users.async.v10.routes.UsersEmailRoute10;
import cl.multicaja.users.model.v10.User;
import org.apache.activemq.broker.BrokerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import javax.naming.spi.NamingManager;
import java.math.BigDecimal;

/**
 * @autor vutreras
 */
public class TestBaseUnitApi extends TestBaseUnit {

  private static Log log = LogFactory.getLog(TestBaseUnitApi.class);

  // Descomentar para probar los tests del API al payara
  /*
  static {
    System.setProperty("env", "development");
    PORT_HTTP = 8080;
    CONTEXT_PATH = "api-prepaid-1.0";
    System.setProperty("db.use.basicdatasource", "true");
  }
  */
  public static CamelFactory camelFactory = CamelFactory.getInstance();

  private static BrokerService brokerService;

  @BeforeClass
  public static void beforeClass() throws Exception {

    if (ConfigUtils.isEnvTest()) {
      System.setProperty("db.use.basicdatasource", "true");
      if (!TestSuite.isServerRunning()) {
        TestSuite.startServer();
      }
    }
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

      //crea e inicia el activemq
      brokerService = camelFactory.createBrokerService();
      brokerService.start();

      //Inicializa las rutas camel, se inicializa aun cuando no se incluya en camel, se crea dado que de
      // ella depende la instancia de tecnocomService
      PrepaidTopupRoute10 prepaidTopupRoute10 = new PrepaidTopupRoute10();
      prepaidTopupRoute10.setPrepaidUserEJBBean10(getPrepaidUserEJBBean10());
      prepaidTopupRoute10.setPrepaidCardEJBBean10(getPrepaidCardEJBBean10());
      prepaidTopupRoute10.setPrepaidEJBBean10(getPrepaidEJBBean10());
      prepaidTopupRoute10.setUsersEJBBean10(getUsersEJBBean10());
      prepaidTopupRoute10.setPrepaidMovementEJBBean10(getPrepaidMovementEJBBean10());
      prepaidTopupRoute10.setCdtEJBBean10(getCdtEJBBean10());
      prepaidTopupRoute10.setMailEJBBean10(getMailEJBBean10());

      /**
       * Agrega rutas de envio de emails de users pero al camel context de prepago necesario para los test
       */

      UsersEmailRoute10 usersEmailRoute10 = new UsersEmailRoute10();
      usersEmailRoute10.setUsersEJBBean10(getUsersEJBBean10());
      usersEmailRoute10.setMailEJBBean10(getMailEJBBean10());

      camelFactory.startCamelContextWithRoutes(true, prepaidTopupRoute10, usersEmailRoute10);
    }

    simpleNamingContextBuilder.deactivate();
  }

  @AfterClass
  public static void afterClass() throws Exception {
    if (ConfigUtils.isEnvTest()) {
      if (!TestSuite.isRunningInTestSuite() && TestSuite.isServerRunning()) {
        TestSuite.stopServer();
      } else {
        log.warn("No es necesario detener el servidor dado que se encuentra en suite");
      }
    }
    if (brokerService != null) {
      camelFactory.releaseCamelContext();
      brokerService.stop();
    }
  }

  protected void topupUserBalance(User user, BigDecimal amount) {
    NewPrepaidTopup10 prepaidTopup = buildNewPrepaidTopup10(user);
    prepaidTopup.getAmount().setValue(amount);
    prepaidTopup.setMerchantCode(NewPrepaidBaseTransaction10.WEB_MERCHANT_CODE);

    HttpResponse respHttp = apiPOST("/1.0/prepaid/topup", toJson(prepaidTopup));
    System.out.println("respHttp: " + respHttp);
    Assert.assertEquals("Debe cargar ok", 201, respHttp.getStatus());
  }
}
