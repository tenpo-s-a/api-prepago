package cl.multicaja.prepaid.resources;

import cl.multicaja.core.providers.BaseExceptionMapper;
import cl.multicaja.core.providers.JacksonObjectMapperProvider;
import cl.multicaja.prepaid.providers.RequestFilter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

import javax.ws.rs.ApplicationPath;

/**
 * @autor vutreras
 */
@ApplicationPath("/")
public class App extends ResourceConfig {

  public App() {
    register(JacksonFeature.class);
    register(RequestFilter.class);
    register(BaseExceptionMapper.class);
    register(JacksonObjectMapperProvider.class);
    packages(this.getClass().getPackage().getName());
  }
}
