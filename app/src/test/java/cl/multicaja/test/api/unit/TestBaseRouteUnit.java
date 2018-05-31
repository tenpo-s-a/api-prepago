package cl.multicaja.test.api.unit;

import cl.multicaja.camel.CamelFactory;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.prepaid.async.v10.PrepaidTopupRoute10;
import cl.multicaja.prepaid.helpers.TecnocomServiceHelper;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.tecnocom.TecnocomService;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import cl.multicaja.tecnocom.dto.AltaClienteDTO;
import cl.multicaja.tecnocom.dto.DatosTarjetaDTO;
import cl.multicaja.users.model.v10.User;
import org.apache.activemq.broker.BrokerService;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.mock.jndi.SimpleNamingContextBuilder;

import javax.naming.spi.NamingManager;

/**
 * @autor vutreras
 */
public class TestBaseRouteUnit extends TestBaseUnit {

  protected static CamelFactory camelFactory = CamelFactory.getInstance();

  private static BrokerService brokerService;

  @BeforeClass
  public static void beforeClass() throws Exception {

    SimpleNamingContextBuilder simpleNamingContextBuilder = new SimpleNamingContextBuilder();

    if (!NamingManager.hasInitialContextFactoryBuilder()) {
      simpleNamingContextBuilder.activate();
    }

    //independiente de la configuración obliga a que el activemq no sea persistente en disco
    ConfigUtils.getInstance().setProperty("activemq.broker.embedded.persistent","false");

    //crea e inicia apache camel con las rutas creadas anteriormente
    if (!camelFactory.isCamelRunning()) {

      //crea e inicia el activemq
      brokerService = camelFactory.createBrokerService();
      brokerService.start();

      //Inicializa las rutas camel, se inicializa aun cuando no se incluya en camel, se crea dado que de
      // ella depende la instancia de tecnocomService
      PrepaidTopupRoute10 prepaidTopupRoute10 = new PrepaidTopupRoute10();
      prepaidTopupRoute10.setPrepaidEJBBean10(getPrepaidEJBBean10());
      prepaidTopupRoute10.setUsersEJBBean10(getUsersEJBBean10());
      prepaidTopupRoute10.setPrepaidMovementEJBBean10(getPrepaidMovementEJBBean10());
      prepaidTopupRoute10.setCdtEJBBean10(getCdtEJBBean10());

      camelFactory.startCamelContextWithRoutes(true, prepaidTopupRoute10);
    }

    simpleNamingContextBuilder.deactivate();
  }

  @AfterClass
  public static void afterClass() throws Exception {
    if (brokerService != null) {
      camelFactory.releaseCamelContext();
      brokerService.stop();
    }
  }

  @After
  public void after() {
    //Todos los test que involucren procesos asincronos esperaran 1 segundo despues que terminen para
    //asegurarse que los mensajes de su test fueron procesado
    try {
      Thread.sleep(1000);
    } catch (Exception e) {
    }
    System.out.println("----------------------------------------------------------------");
    System.out.println("After Test - class: " + this.getClass().getSimpleName() + ", method: " + testName.getMethodName());
    System.out.println("----------------------------------------------------------------");
  }

  /**
   *
   * @param prepaidTopup
   * @param user
   * @return
   */
  protected String sendTopup(PrepaidTopup10 prepaidTopup, User user) throws Exception {
    String messageId = getPrepaidTopupDelegate10().sendTopUp(prepaidTopup, user, null, null);
    return messageId;
  }

  /**
   *
   * @param prepaidTopup
   * @param user
   * @return
   */
  protected String sendTopup(PrepaidTopup10 prepaidTopup, User user, PrepaidMovement10 prepaidMovement) throws Exception {
    String messageId = getPrepaidTopupDelegate10().sendTopUp(prepaidTopup, user, null, prepaidMovement);
    return messageId;
  }

  /**
   *
   * @param prepaidTopup
   * @param user
   * @param cdtTransaction
   * @param prepaidMovement
   * @return
   * @throws Exception
   */
  protected String sendTopup(PrepaidTopup10 prepaidTopup, User user, CdtTransaction10 cdtTransaction, PrepaidMovement10 prepaidMovement) throws Exception {
    String messageId = getPrepaidTopupDelegate10().sendTopUp(prepaidTopup, user, cdtTransaction, prepaidMovement);
    return messageId;
  }

  /**
   *
   * @param prepaidTopup
   * @param user
   * @return
   * @throws Exception
   */
  protected String sendTopUpReverseConfirmation(PrepaidTopup10 prepaidTopup, User user) throws Exception {
    String messageId = getPrepaidTopupDelegate10().sendTopUpReverseConfirmation(prepaidTopup, user, null, null);
    return messageId;
  }

  /**
   *
   * @param prepaidTopup
   * @param user
   * @param cdtTransaction
   * @return
   * @throws Exception
   */
  protected String sendTopUpReverseConfirmation(PrepaidTopup10 prepaidTopup, User user, CdtTransaction10 cdtTransaction) throws Exception {
    String messageId = getPrepaidTopupDelegate10().sendTopUpReverseConfirmation(prepaidTopup, user, cdtTransaction, null);
    return messageId;
  }

  /**
   *
   * @param prepaidTopup
   * @param user
   * @param cdtTransaction
   * @param prepaidMovement
   * @return
   * @throws Exception
   */
  protected String sendTopUpReverseConfirmation(PrepaidTopup10 prepaidTopup, User user, CdtTransaction10 cdtTransaction, PrepaidMovement10 prepaidMovement) throws Exception {
    String messageId = getPrepaidTopupDelegate10().sendTopUpReverseConfirmation(prepaidTopup, user, cdtTransaction, prepaidMovement);
    return messageId;
  }
}
