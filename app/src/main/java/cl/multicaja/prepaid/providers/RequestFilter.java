package cl.multicaja.prepaid.providers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
@PreMatching
public class RequestFilter implements ContainerRequestFilter {

  private static Log log = LogFactory.getLog(RequestFilter.class);

  private String[] requireHeaders = {
    "user-lang",
    "user-timezone"
  };

  @Override
  public void filter(ContainerRequestContext ctx) throws IOException {

    MultivaluedMap<String, String> mapHeaders = ctx.getHeaders();

    log.info("RequestFilter headers: " + mapHeaders);

    if (requireHeaders.length > 0) {

    }
  }
}
