package cl.multicaja.cdt.helpers;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @autor vutreras
 */
public final class CdtHelper {

  private static final Map<String, Integer> map;

  private static final Integer CDT_ERROR_DEFAULT = 107000;

  static {
    map = new ConcurrentHashMap<>();
    /**
     TODO mapear todos los mensajes de error del cdt a códigos de errores distintos partiendo desde el 107001,
     TODO ademas se deben maperar en el archivo de errores del api-core
     */
    //Ejemplo: map.put("Error en calculo de cdt", 107001);
  }

  /**
   * retorna el codigo de error asignado desde prepago a los errores de cdt
   * @param errorCdt
   * @return
   */
  public static Integer getErrorCode(String errorCdt) {
    Integer code = null;
    if (StringUtils.isNotBlank(errorCdt)) {
      Set<String> keys = map.keySet();
      for (String k : keys) {
        if (k.equalsIgnoreCase(errorCdt) || k.contains(errorCdt) || errorCdt.contains(k)) {
          code = map.get(errorCdt);
          break;
        }
      }
    }
    if (code == null) {
      code = CDT_ERROR_DEFAULT;
    }
    return code;
  }

}
