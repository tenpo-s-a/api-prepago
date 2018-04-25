package cl.multicaja.prepago.test.api;

import cl.multicaja.prepago.test.TestApiBase;
import cl.multicaja.prepago.utils.ConfigUtils;
import cl.multicaja.prepago.utils.NumberUtils;
import org.apache.commons.lang3.StringUtils;
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

  public TestServer() {
    super();
  }

  /**
   *
   * @throws Exception
   */
  public void start() throws Exception {

    //por defecto obliga a que lo que se ejecute mediante test use un BasicDatasource y no uno por jndi
    System.setProperty("db.use.basicdatasource", "true");

    //se genera un port aleatorio
    int port = NumberUtils.getInstance().random(3200, 7200);
    String env = System.getProperty("env");

    if (StringUtils.isBlank(env)) {
      throw new IllegalArgumentException("Falta la definicion del parametro env con los valores: test, development, production, jenkins");
    }

    //se establecen los datos de conexion http al TestApiBase
    TestApiBase.PORT_HTTP = port;
    TestApiBase.PORT_HTTPS = port + 1;
    TestApiBase.CONTEXT_PATH = ConfigUtils.getInstance().getModuleProperty("context.path");

    //en caso que sea development* o production* lo puertos por defecto son 8080 y 8181 dado que son los puertos
    //del servidor externo
    if (env.startsWith("development") || env.startsWith("production")) {
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
    if (env.startsWith("development") || env.startsWith("production")) {
      return;
    }

    File dirWar = new File("./target");

    File[] files = dirWar.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        return file.getName().endsWith(".war");
      }
    });

    log.info("war: " + Arrays.asList(files));

    if (files == null || files.length == 0) {
      throw new RuntimeException("No existe el war para desplegar en el servidor de test");
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
  }

  /**
   *
   */
  public void stop() {
    if (glassfish != null) {
      try {
        glassfish.stop();
      } catch (Exception e) {
      }
    }
  }
}
