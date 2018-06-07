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

  private static final Integer CDT_ERROR_DEFAULT = 108000;

  static {
    map = new ConcurrentHashMap<>();
    /**
     TODO mapear todos los mensajes de error del cdt a códigos de errores distintos partiendo desde el 108000
     */
    //codigos genericos
    map.put("Transacción: error genérico: ${value}", 108000);
    map.put("Límites: error genérico: ${value}", 108001);
    //calculadora de carga
    map.put("La carga supera el monto máximo de carga web", 108201);
    map.put("La carga supera el monto máximo de carga pos", 108202);
    map.put("La carga es menor al mínimo de carga", 108203);
    map.put("La carga supera el monto máximo de cargas mensuales", 108204);
    //calculadora de retiro
    map.put("El retiro supera el monto máximo de un retiro web", 108301);
    map.put("El retiro supera el monto máximo de un retiro pos", 108302);
    map.put("El monto de retiro es menor al monto mínimo de retiros", 108303);
    map.put("El retiro supera el monto máximo de retiros mensuales", 108304);
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
