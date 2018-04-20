package cl.multicaja.prepago.filters;

import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class RequestFilter implements Filter {

  private String[] requireHeaders = {
    //"user-lang",
    //"user-timezone"
  };

  @Override
  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException {

    HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
    HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;

    if (requireHeaders.length > 0) {

      Enumeration<String> enumHeaders = httpServletRequest.getHeaderNames();

      Map<String, String> mapHeaders = new HashMap<>();

      while (enumHeaders.hasMoreElements()) {
        String key = enumHeaders.nextElement();
        mapHeaders.put(key, httpServletRequest.getHeader(key));
      }

      System.out.println("RequestFilter headers: " + mapHeaders);

      for (String requireHeader : requireHeaders) {
        if (StringUtils.isBlank(mapHeaders.get(requireHeader))) {
          sendError(httpServletResponse, "1", "Faltan headers: " + Arrays.asList(requireHeaders));
          return;
        }
      }
    }

    try {
      filterChain.doFilter(servletRequest, servletResponse);
    } catch(Exception ex) {
      ex.printStackTrace();
    }
  }

  private void sendError(HttpServletResponse httpServletResponse, String code, String message) throws IOException {
    Map<String, Object> resp = new HashMap<>();
    resp.put("code", code);
    resp.put("message", message);
    Gson gson = new Gson();
    httpServletResponse.setContentType(MediaType.APPLICATION_JSON);
    httpServletResponse.setCharacterEncoding("UTF-8");
    httpServletResponse.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    httpServletResponse.getWriter().write(gson.toJson(resp));
  }
}
