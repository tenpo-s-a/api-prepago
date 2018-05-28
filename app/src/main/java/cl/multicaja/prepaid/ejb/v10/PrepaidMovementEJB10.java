package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidMovementStatus;
import cl.multicaja.tecnocom.constants.CodigoMoneda;

import java.util.Map;

public interface PrepaidMovementEJB10 {

  /**
   *
   * @param header
   * @param value
   * @return
   * @throws Exception
   */
  PrepaidMovement10 addPrepaidMovement(Map<String,Object> header, PrepaidMovement10 value) throws Exception;

  /**
   *
   * @param header
   * @param id
   * @param numextcta
   * @param nummovext
   * @param clamone
   * @param status
   * @throws Exception
   */
  void updatePrepaidMovement(Map<String, Object> header, Long id, Integer numextcta, Integer nummovext, CodigoMoneda clamone, PrepaidMovementStatus status) throws Exception;
}
