package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidMovementStateType;

import java.util.Map;

public interface PrepaidMovementEJB10 {
  PrepaidMovement10 addPrepaidMovement(Map<String,Object> header, PrepaidMovement10 value) throws Exception;
  void updatePrepaidMovement(Map<String,Object> header, Long id, Integer numExtracto, Integer numMovExtracto, Integer claveMoneda, PrepaidMovementStateType state) throws Exception;
}
