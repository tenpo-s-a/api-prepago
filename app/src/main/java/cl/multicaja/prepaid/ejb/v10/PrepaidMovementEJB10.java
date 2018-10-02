package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.prepaid.model.v10.ConciliationStatusType;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidMovementStatus;
import cl.multicaja.prepaid.model.v10.PrepaidMovementType;
import cl.multicaja.tecnocom.constants.CodigoMoneda;
import cl.multicaja.tecnocom.constants.IndicadorNormalCorrector;
import cl.multicaja.tecnocom.constants.TipoFactura;

import java.sql.Date;
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
  void updatePrepaidMovement(Map<String, Object> header, Long id, String pan, String centalta, String cuenta, Integer numextcta, Integer nummovext, Integer clamone, PrepaidMovementStatus status) throws Exception;

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
   * actualiza solo el estado de conciliacion con switch
   *
   * @param header
   * @param movementId
   * @param status nuevo estado de conciliacion con tecnocom
   * @throws Exception
   */
  boolean updateStatusMovementConSwitch(Map<String, Object> header, Long movementId, ConciliationStatusType status) throws Exception;

  /**
   * actualiza solo el estado de conciliacion con tecnocom
   *
   * @param header
   * @param movementId
   * @param status nuevo estado de conciliacion con tecnocom
   * @throws Exception
   */
  void updateStatusMovementConTecnocom(Map<String, Object> header, Long movementId, ConciliationStatusType status) throws Exception;

  /**
   * actualiza solo el estado de conciliacion con switch a todos los movimientos
   * que esten entre las dos fechas entregadas, y que posean el tipofac e indnorcor indicados.
   *
   * @param header
   * @param startDate fecha inicial del intervalo (inclusive)
   * @param endDate fecha final del intervalo (inclusive)
   * @param tipofac
   * @param indnorcor
   * @param status nuevo estado de conciliacion con switch
   * @throws Exception
   */
  void updatePendingPrepaidMovementsSwitchStatus(Map<String, Object> header, String startDate, String endDate, TipoFactura tipofac, IndicadorNormalCorrector indnorcor, ConciliationStatusType status) throws Exception;

  /**
   * actualiza solo el estado de conciliacion con tecnocom a todos los movimientos
   * que esten entre las dos fechas entregadas, y que posean el tipofac e indnorcor indicados.
   *
   * @param header
   * @param startDate fecha inicial del intervalo (inclusive)
   * @param endDate fecha final del intervalo (inclusive)
   * @param tipofac
   * @param indnorcor
   * @param status nuevo estado de conciliacion con tecnocom
   * @throws Exception
   */
  void updatePendingPrepaidMovementsTecnocomStatus(Map<String, Object> header, String startDate, String endDate, TipoFactura tipofac, IndicadorNormalCorrector indnorcor, ConciliationStatusType status) throws Exception;


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
                                                     PrepaidMovementStatus estado, String cuenta, CodigoMoneda clamon, IndicadorNormalCorrector indnorcor, TipoFactura tipofac, Date fecfac) throws Exception;

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

  /**
   * Busca un movimiento para ser conciliado
   * @param idPrepaidUser id del usuario
   * @param numaut numero de autorizacion de la transaccion
   * @param fecfac fecha de la factura
   * @param tipofac tipo de factura
   * @return
   * @throws Exception
   */
  PrepaidMovement10 getPrepaidMovementForTecnocomReconciliation(Long idPrepaidUser, String numaut, Date fecfac, TipoFactura tipofac) throws Exception;

}
