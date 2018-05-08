package cl.multicaja.prepaid.web;

import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

  public WebApp() {
    super();
  }

  @Override
  public void contextInitialized(ServletContextEvent sce) {
    Locale.setDefault(Constants.DEFAULT_LOCALE);
    log.info("Init app: " + ConfigUtils.getInstance().getModuleProperties());
  }

  @Override
  public void contextDestroyed(ServletContextEvent sce) {

  }
}
