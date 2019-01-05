package cl.multicaja.prepaid.web;

import cl.multicaja.accounting.async.v10.routes.MastercardAccountingRoute10;
import cl.multicaja.camel.CamelFactory;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.Constants;
import cl.multicaja.prepaid.async.v10.routes.*;
import cl.multicaja.core.utils.EncryptUtil;
import org.apache.activemq.broker.BrokerService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.Locale;

/**
 * @autor vutreras
 */
@WebListener
public class WebApp implements ServletContextListener  {

  private static Log log = LogFactory.getLog(WebApp.class);

  private CamelFactory camelFactory = CamelFactory.getInstance();

  @Inject
  private PrepaidTopupRoute10 prepaidTopupRoute10;

  @Inject
  private TransactionReversalRoute10 transactionReversalRoute10;

  @Inject
  private CurrencyConvertionRoute10 currencyConvertionRoute10;

  @Inject
  private TecnocomReconciliationRoute10 tecnocomReconciliationRoute10;

  @Inject
  private MastercardAccountingRoute10 mastercardAccountingRoute10;

  @Inject
  private ConciliationMcRedRoute10 conciliationMcRedRoute10;

  @Inject
  private ReconciliationSchedulerRoute10 reconciliationSchedulerRoute10;

  @Inject
  private ProductChangeRoute10 productChangeRoute10;

  private BrokerService brokerService;

  public WebApp() {
    super();
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    Locale.setDefault(Constants.DEFAULT_LOCALE);
    ConfigUtils cu = new ConfigUtils("api-prepaid");
    EncryptUtil.getInstance().setPassword(cu.getProperty("encrypt.password"));
    log.info("Init app: " + cu.getModuleProperties());
    //solamente crea un mq embebido si la conexión es del tipo vm, caso contrario se conecta a un activemq externo
    if (cu.getProperty("activemq.url","").startsWith("vm:")) {
      try {
        brokerService = camelFactory.createBrokerService();
        brokerService.start();
        log.info("==== Broker activemq iniciado ====");
      } catch (Exception e) {
        log.error("Error al inicializar broker activemq", e);
      }
    } else {
      log.info("==== Coneccion a activemq externo ====");
    }
    try {
      if (!camelFactory.isCamelRunning()) {
        camelFactory.startCamelContextWithRoutes(true,
          prepaidTopupRoute10,
          transactionReversalRoute10,
          productChangeRoute10);
        log.info("==== Apache camel iniciado ====");
      }
    } catch (Exception e) {
      log.error("Error al inicializar apache camel", e);
    }
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    try {
      camelFactory.releaseCamelContext();
      log.info("==== Apache camel detenido ====");
    } catch(Exception ex) {
      log.error("Error al detener apache camel", ex);
    }
    if (brokerService != null) {
      try {
        brokerService.stop();
        log.info("==== Broker activemq detenido ====");
      } catch (Exception ex) {
        log.error("Error al detener broker activemq", ex);
      }
    }
  }
}
