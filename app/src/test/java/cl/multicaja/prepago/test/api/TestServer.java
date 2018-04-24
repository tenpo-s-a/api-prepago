package cl.multicaja.prepago.test.api;

import cl.multicaja.prepago.test.TestApiBase;
import cl.multicaja.prepago.utils.NumberUtils;
import org.apache.commons.lang3.StringUtils;
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

  private GlassFish glassfish;

  public TestServer() {
    super();
  }

  /**
   *
   * @throws Exception
   */
  public void start() throws Exception {

    int port = NumberUtils.getInstance().random(3200, 7200);
    String env = System.getProperty("env");

    System.out.println("Suite env: " + System.getProperty("env"));
    System.out.println("Suite port: http: " + port + ", https: " + (port + 1));

    if (StringUtils.isBlank(env)) {
      throw new IllegalArgumentException("Falta la definicion del parametro -Denv con los valores: test, development, production, jenkins");
    }

    File dirWar = new File("./target");

    File[] files = dirWar.listFiles(new FileFilter() {
      @Override
      public boolean accept(File file) {
        return file.getName().endsWith(".war");
      }
    });

    System.out.println("Files war: " + Arrays.asList(files));

    if (files == null || files.length == 0) {
      throw new RuntimeException("No existe el war para desplegar en el servidor de test");
    }

    File fileWar = files[0];

    System.out.println("Se desplegara el war: " + fileWar.getAbsolutePath());

    TestApiBase.PORT_HTTP = port;
    TestApiBase.PORT_HTTPS = port + 1;
    TestApiBase.CONTEXT_PATH = fileWar.getName().replace(".war", "");

    BootstrapProperties bootstrap = new BootstrapProperties();
    GlassFishRuntime runtime = GlassFishRuntime.bootstrap(bootstrap);
    GlassFishProperties glassfishProperties = new GlassFishProperties();
    glassfishProperties.setPort("http-listener", port);
    glassfishProperties.setPort("https-listener", port + 1);
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
