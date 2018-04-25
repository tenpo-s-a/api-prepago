package cl.multicaja.prepago.web;

import cl.multicaja.prepago.utils.ConfigUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * @autor vutreras
 */
@WebListener
public class WebApp implements ServletContextListener  {

  private static Log log = LogFactory.getLog(WebApp.class);

  public WebApp() {
    super();
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    log.info("Init app: " + ConfigUtils.getInstance().getModuleProperties());
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {

  }
}
