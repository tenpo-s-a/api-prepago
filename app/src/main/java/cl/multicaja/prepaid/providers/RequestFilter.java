package cl.multicaja.prepaid.providers;

import org.apache.commons.lang3.StringUtils;
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

  //lista de headers requeridos por el api
  private String[] requireHeaders = {
    //Constants.HEADER_USER_LOCALE,
    //Constants.HEADER_USER_TIMEZONE
  };

  @Override
  public void filter(ContainerRequestContext ctx) throws IOException {
    if (requireHeaders != null && requireHeaders.length > 0) {
      MultivaluedMap<String, String> mapHeaders = ctx.getHeaders();
      log.info("RequestFilter headers: " + mapHeaders);
      for (String h : requireHeaders) {
        if (StringUtils.isBlank(mapHeaders.getFirst(h))) {
          throw new IOException("Falta http header: " + h);
        }
      }
    }
  }
}
