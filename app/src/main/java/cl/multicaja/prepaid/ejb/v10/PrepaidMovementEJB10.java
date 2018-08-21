package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidMovementStatus;
import cl.multicaja.prepaid.model.v10.PrepaidMovementType;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoFactura;

import java.util.List;
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
   * actualiza datos retornados por tecnocom en el movimiento
   *
   * @param header
   * @param id
   * @param numextcta
   * @param nummovext
   * @param clamone
   * @param status
   * @throws Exception
   */
  void updatePrepaidMovement(Map<String, Object> header, Long id, Integer numextcta, Integer nummovext, Integer clamone, PrepaidMovementStatus status) throws Exception;

  /**
   * actualiza solo el estado
   *
   * @param header
   * @param id
   * @param status
   * @throws Exception
   */
  void updatePrepaidMovementStatus(Map<String, Object> header, Long id, PrepaidMovementStatus status) throws Exception;

  /**
   *
   * @param id
   * @param idMovimientoRef
   * @param idPrepaidUser
   * @param idTxExterno
   * @param tipoMovimiento
   * @param estado
   * @param cuenta
   * @param clamon
   * @param indnorcor
   * @param tipofac
   * @return
   * @throws Exception
   */
  List<PrepaidMovement10> getPrepaidMovements(Long id, Long idMovimientoRef, Long idPrepaidUser, String idTxExterno, PrepaidMovementType tipoMovimiento,
                                                     PrepaidMovementStatus estado, String cuenta, CodigoMoneda clamon, IndicadorNormalCorrector indnorcor, TipoFactura tipofac) throws Exception;

  /**
   *
   * @param id
   * @return
   * @throws Exception
   */
  PrepaidMovement10 getPrepaidMovementById(Long id) throws Exception;

  /**
   *
   * @param idPrepaidUser
   * @return
   * @throws Exception
   */
  List<PrepaidMovement10> getPrepaidMovementByIdPrepaidUser(Long idPrepaidUser) throws Exception;

  /**
   *
   * @param idPrepaidUser
   * @param estado
   * @return
   * @throws Exception
   */
  List<PrepaidMovement10> getPrepaidMovementByIdPrepaidUserAndEstado(Long idPrepaidUser, PrepaidMovementStatus estado) throws Exception;

  /**
   *
   * @param idPrepaidUser
   * @param status
   * @return
   * @throws Exception
   */
  PrepaidMovement10 getLastPrepaidMovementByIdPrepaidUserAndOneStatus(Long idPrepaidUser, PrepaidMovementStatus... status) throws Exception;

  /**
   *
   * @param idPrepaidUser
   * @param tipoMovimiento
   * @return
   * @throws Exception
   */
  List<PrepaidMovement10> getPrepaidMovementByIdPrepaidUserAndTipoMovimiento(Long idPrepaidUser, PrepaidMovementType tipoMovimiento) throws Exception;

  /**
   *
   * @param idPrepaidUser
   * @param tipoMovimiento
   * @param status
   * @return
   * @throws Exception
   */
  List<PrepaidMovement10> getPrepaidMovementByIdPrepaidUserAndTipoMovimientoAndEstado(Long idPrepaidUser, PrepaidMovementType tipoMovimiento, PrepaidMovementStatus status) throws Exception;

  /**
   *
   * @param idPrepaidUser
   * @param idTxExterno
   * @param tipoMovimiento
   * @param tipofac
   * @return
   * @throws Exception
   */
  PrepaidMovement10 getPrepaidMovementForReverse(Long idPrepaidUser, String idTxExterno, PrepaidMovementType tipoMovimiento, TipoFactura tipofac) throws Exception;

  /**
   *
   * @param idPrepaidUser
   * @return
   * @throws Exception
   */
  Boolean isFirstTopup(Long idPrepaidUser) throws Exception;

}
