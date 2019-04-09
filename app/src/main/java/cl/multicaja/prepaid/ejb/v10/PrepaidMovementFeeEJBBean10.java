package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.prepaid.model.v10.PrepaidMovementFee10;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

@Stateless
@LocalBean
@TransactionManagement(value= TransactionManagementType.CONTAINER)
public class PrepaidMovementFeeEJBBean10 extends PrepaidBaseEJBBean10 {

  private static final String FIND_FEE_BY_ID = String.format("SELECT * FROM %s.prp_movimiento_comision WHERE id = ?", getSchema());

  private static final String FIND_FEE_BY_MOVEMENT_ID = String.format("SELECT * FROM %s.prp_movimiento_comision WHERE id_movimiento = ?", getSchema());

  public PrepaidMovementFee10 findFeeById(Long feeId) {
    return new PrepaidMovementFee10();
  }

  public PrepaidMovementFee10 findFeeByMovementId(Long movementId) {
    return new PrepaidMovementFee10();
  }


}
