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
    map.put("Transacción: error genérico: ${value}", 108000);
    map.put("Saldo: error genérico: ${value}", 108050);
    map.put("Saldo insuficiente", 108051);
    map.put("Límites: error genérico: ${value}", 108100);
    map.put("Excede límite de saldo máximo", 108101);
    map.put("Excede límite de cargas mensuales", 108102);
    map.put("Excede límite de retiros mensuales", 108103);
    map.put("Excede límite de carga máxima POS", 108104);
    map.put("Infringe límite de cargas mínima POS", 108105);
    map.put("Infringe límite de retiro mínimo POS", 108106);
    map.put("Excede límite de retiro máximo POS", 108107);
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
