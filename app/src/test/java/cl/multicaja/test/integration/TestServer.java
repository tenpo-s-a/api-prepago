package cl.multicaja.test.integration;

import cl.multicaja.core.test.TestApiBase;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.embeddable.BootstrapProperties;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;

/**
 * @autor vutreras
 */
public class TestServer {

  private static Log log = LogFactory.getLog(TestServer.class);

  private GlassFish glassfish;

  private boolean serverRunning;

  public TestServer() {
    super();
  }

  /**
   * inicia el servidor embebido
   *
   * @throws Exception
   */
  public void start() throws Exception {

    //por defecto obliga a que lo que se ejecute mediante test use un BasicDatasource y no uno por jndi
    System.setProperty("db.use.basicdatasource", "true");

    //se genera un port aleatorio
    int port = NumberUtils.getInstance().random(3200, 7200);
    String env = ConfigUtils.getEnv();

    //se establecen los datos de conexion http al TestApiBase
    TestApiBase.PORT_HTTP = port;
    TestApiBase.PORT_HTTPS = port + 1;
    TestApiBase.CONTEXT_PATH = ConfigUtils.getInstance().getModuleProperty("context.path");

    //en caso que sea development* o production* lo puertos por defecto son 8080 y 8181 dado que son los puertos
    //del servidor externo
    if (ConfigUtils.isEnvDevelopment() || ConfigUtils.isEnvProduction()) {
      TestApiBase.PORT_HTTP = 8080;
      TestApiBase.PORT_HTTPS = 8181;
    }

    TestApiBase testApiBase = new TestApiBase();

    log.info("============================================");
    log.info("Suite env: " + env);
    log.info("Suite context path: " + TestApiBase.CONTEXT_PATH);
    log.info("Suite host: " + testApiBase.getApiHost().get("host"));
    log.info("Suite port: " + testApiBase.getApiHost().get("port"));
    log.info("============================================");

    //Si es development o production quiere decir que se intenta conectar a un payara externo, en este caso no es necesario
    //ejecutar un payara embebido
    if (ConfigUtils.isEnvDevelopment() || ConfigUtils.isEnvProduction()) {
      return;
    }

    File dirWar = new File("./target");

    File[] files = dirWar.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        return file.getName().endsWith(".war");
      }
    });

    log.info("war: " + (files != null ? Arrays.asList(files) : null));

    if (files == null || files.length == 0) {
      throw new RuntimeException("No existe el war para desplegar en el servidor de test, ejecuta un ./package.sh o mvn package");
    }

    File fileWar = files[0];

    System.out.println("Se desplegara el war: " + fileWar.getAbsolutePath());

    BootstrapProperties bootstrap = new BootstrapProperties();
    GlassFishRuntime runtime = GlassFishRuntime.bootstrap(bootstrap);
    GlassFishProperties glassfishProperties = new GlassFishProperties();
    glassfishProperties.setPort("http-listener", TestApiBase.PORT_HTTP);
    glassfishProperties.setPort("https-listener", TestApiBase.PORT_HTTPS);
    glassfish = runtime.newGlassFish(glassfishProperties);
    glassfish.start();
    glassfish.getDeployer().deploy(fileWar);
    serverRunning = true;
  }

  /**
   * detiene el servidor embebido
   */
  public void stop() {
    if (glassfish != null) {
      try {
        glassfish.stop();
      } catch (Exception e) {
      }
    }
    serverRunning = false;
  }

  /**
   * retorna true si el servidor embebido se encuentra en ejecucion
   *
   * @return
   */
  public boolean isServerRunning() {
    return serverRunning;
  }
}
