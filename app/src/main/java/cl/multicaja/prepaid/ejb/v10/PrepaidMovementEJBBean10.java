package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.Constants;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDelegate10;
import cl.multicaja.prepaid.helpers.freshdesk.model.v10.*;
import cl.multicaja.prepaid.helpers.users.UserClient;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.utils.TemplateUtils;
import cl.multicaja.tecnocom.constants.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import javax.inject.Inject;
import java.sql.Date;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;
import static cl.multicaja.prepaid.helpers.CalculationsHelper.getParametersUtil;

@Stateless
@LocalBean
@TransactionManagement(value=TransactionManagementType.CONTAINER)
public class PrepaidMovementEJBBean10 extends PrepaidBaseEJBBean10 implements PrepaidMovementEJB10 {

  private static Log log = LogFactory.getLog(PrepaidMovementEJBBean10.class);
  @Inject
  private PrepaidTopupDelegate10 delegate;

  private UserClient userClient;

  @EJB
  private PrepaidUserEJBBean10 prepaidUserEJB10;

  @EJB
  private PrepaidCardEJBBean10 prepaidCardEJB10;

  @EJB
  private CdtEJBBean10 cdtEJB10;

  @EJB
  private PrepaidEJBBean10 prepaidEJBBean10;

  @Override
  public UserClient getUserClient() {
    if(userClient == null) {
      userClient = UserClient.getInstance();
    }
    return userClient;
  }

  public void setUserClient(UserClient userClient) {
    this.userClient = userClient;
  }

  public PrepaidEJBBean10 getPrepaidEJBBean10() {
    return prepaidEJBBean10;
  }

  public void setPrepaidEJBBean10(PrepaidEJBBean10 prepaidEJBBean10) {
    this.prepaidEJBBean10 = prepaidEJBBean10;
  }

  public PrepaidTopupDelegate10 getDelegate() {
    return delegate;
  }

  public void setDelegate(PrepaidTopupDelegate10 delegate) {
    this.delegate = delegate;
  }

  public PrepaidUserEJBBean10 getPrepaidUserEJB10() {
    return prepaidUserEJB10;
  }

  public PrepaidCardEJBBean10 getPrepaidCardEJB10() {
    return prepaidCardEJB10;
  }

  public CdtEJBBean10 getCdtEJB10() {
    return cdtEJB10;
  }

  public void setPrepaidUserEJB10(PrepaidUserEJBBean10 prepaidUserEJB10) {
    this.prepaidUserEJB10 = prepaidUserEJB10;
  }

  public void setPrepaidCardEJB10(PrepaidCardEJBBean10 prepaidCardEJB10) {
    this.prepaidCardEJB10 = prepaidCardEJB10;
  }

  public void setCdtEJB10(CdtEJBBean10 cdtEJB10) {
    this.cdtEJB10 = cdtEJB10;
  }

  @Override
  public PrepaidMovement10 addPrepaidMovement(Map<String, Object> header, PrepaidMovement10 data) throws Exception {

    Object[] params = {
      new InParam(data.getIdMovimientoRef(),Types.NUMERIC), //_id_mov_ref NUMERIC
      new InParam(data.getIdPrepaidUser(),Types.NUMERIC), //_id_usuario NUMERIC
      data.getIdTxExterno(), //_id_tx_externo VARCHAR
      data.getTipoMovimiento().toString(), //_tipo_movimiento VARCHAR
      new InParam(data.getMonto(),Types.NUMERIC), //_monto NUMERIC
      data.getEstado().toString(), //_estado VARCHAR
      data.getEstadoNegocio().getValue(), // _estado_de_negocio VARCHAR
      data.getConSwitch().getValue(), //_estado_con_switch VARCHAR
      data.getConTecnocom().getValue(), //_estado_con_tecnocom VARCHAR
      data.getOriginType().getValue(), //_origen_movimiento VARCHAR
      data.getCodent(),//_codent VARCHAR
      data.getCentalta(),//_centalta VARCHAR
      data.getCuenta(),//_cuenta VARCHAR
      new InParam(data.getClamon().getValue(), Types.NUMERIC),//_clamon NUMERIC
      new InParam(data.getIndnorcor().getValue(),Types.NUMERIC),//_indnorcor NUMERIC
      new InParam(data.getTipofac().getCode(),Types.NUMERIC),//_tipofac NUMERIC
      new Date(data.getFecfac().getTime()),//_fecfac DATE
      data.getNumreffac(),//_numreffac VARCHAR
      data.getPan(),// _pan VARCHAR
      new InParam(data.getClamondiv(), Types.NUMERIC),//_clamondiv NUMERIC
      new InParam(data.getImpdiv(), Types.NUMERIC),//_impdiv NUMERIC
      new InParam(data.getImpfac(), Types.NUMERIC),//_impfac NUMERIC
      new InParam(data.getCmbapli(), Types.NUMERIC),//_cmbapli NUMERIC
      !StringUtils.isBlank(data.getNumaut()) ? data.getNumaut() : "",//_numaut    VARCHAR
      data.getIndproaje().getValue(),//_indproaje VARCHAR
      data.getCodcom(),//_codcom VARCHAR
      data.getCodact(),//_codact VARCHAR
      new InParam(data.getImpliq(), Types.NUMERIC),//_impliq NUMERIC
      new InParam(data.getClamonliq(),Types.NUMERIC), //_clamonliq NUMERIC
      new InParam(data.getCodpais().getValue(), Types.NUMERIC), //_codpais NUMERIC
      data.getNompob(),//_nompob VARCHAR
      new InParam(data.getNumextcta(),Types.NUMERIC),//_numextcta NUMERIC
      new InParam(data.getNummovext(),Types.NUMERIC),//_nummovext NUMERIC
      new InParam(data.getClamone(),Types.NUMERIC),// _clamone NUMERIC
      data.getTipolin(),//_tipolin VARCHAR
      new InParam(data.getLinref(), Types.NUMERIC),//_linref NUMERIC
      new InParam(data.getNumbencta(),Types.NUMERIC),//_numbencta NUMERIC
      new InParam(data.getNumplastico(),Types.NUMERIC),//_numplastico NUMERIC
      new OutParam("_r_id", Types.NUMERIC),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_prp_crea_movimiento_v10", params);

    if ("0".equals(resp.get("_error_code"))) {
      data.setId(getNumberUtils().toLong(resp.get("_r_id")));
      return data;
    } else {
      log.error("addPrepaidMovement resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
  }

  @Override
  public void updatePrepaidMovement(Map<String, Object> header, Long id, String pan, String centalta, String cuenta, Integer numextcta, Integer nummovext, Integer clamone, BusinessStatusType businessStatus, PrepaidMovementStatus status) throws Exception {

    if(id == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }

    Object[] params = {
      new InParam(id,Types.NUMERIC),
      pan == null ? new NullParam(Types.VARCHAR) : new InParam(Utils.replacePan(pan), Types.VARCHAR),
      centalta == null ? new NullParam(Types.VARCHAR) : new InParam(centalta, Types.VARCHAR),
      cuenta == null ? new NullParam(Types.VARCHAR) : new InParam(cuenta, Types.VARCHAR),
      numextcta == null ? new NullParam(Types.NUMERIC) : new InParam(numextcta, Types.NUMERIC),
      nummovext == null ? new NullParam(Types.NUMERIC) : new InParam(nummovext, Types.NUMERIC),
      clamone == null ? new NullParam(Types.NUMERIC) : new InParam(clamone, Types.NUMERIC),
      businessStatus == null ? new NullParam(Types.VARCHAR) : new InParam(businessStatus, Types.VARCHAR),
      status == null ? new NullParam(Types.VARCHAR) : new InParam(status, Types.VARCHAR),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp =  getDbUtils().execute(getSchema() + ".mc_prp_actualiza_movimiento_v10",params);

    if (!"0".equals(resp.get("_error_code"))) {
      log.error("updatePrepaidMovement resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
  }

  @Override
  public void updatePrepaidMovementStatus(Map<String, Object> header, Long id, PrepaidMovementStatus status) throws Exception {
    this.updatePrepaidMovement(null, id, null, null, null, null, null, null, null, status);
  }
  @Override
  public void updatePrepaidBusinessStatus(Map<String, Object> header, Long id, BusinessStatusType businessStatusType) throws Exception {
    this.updatePrepaidMovement(null, id, null, null, null, null, null, null, businessStatusType, null);
  }
  @Override
  public void updatePendingPrepaidMovementsSwitchStatus(Map<String, Object> header, String startDate, String endDate, PrepaidMovementType tipoMovimiento, IndicadorNormalCorrector indnorcor, ReconciliationStatusType status) throws Exception {
    if(startDate == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "startDate"));
    }

    if(endDate == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "endDate"));
    }

    if(tipoMovimiento == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "tipoMovimiento"));
    }

    if(indnorcor == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "indnorcor"));
    }

    if(status == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "status"));
    }

    String allowedFormat = "\\d{8,17}";
    if (!startDate.matches(allowedFormat)) {
      throw new BadRequestException(PARAMETRO_NO_CUMPLE_FORMATO_$VALUE).setData(new KeyValue("value", "startDate"));
    }

    if (!endDate.matches(allowedFormat)) {
      throw new BadRequestException(PARAMETRO_NO_CUMPLE_FORMATO_$VALUE).setData(new KeyValue("value", "endDate"));
    }

    Object[] params = {
      startDate,
      endDate,
      new InParam(tipoMovimiento.toString(), Types.VARCHAR),
      new InParam(indnorcor.getValue(), Types.NUMERIC),
      status.getValue(),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp =  getDbUtils().execute(getSchema() + ".mc_prp_actualiza_no_conciliados_switch_v10", params);

    if (!"0".equals(resp.get("_error_code"))) {
      log.error("updatePendingPrepaidMovementsSwitchStatus resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
  }

  @Override
  public void updatePendingPrepaidMovementsTecnocomStatus(Map<String, Object> header, String startDate, String endDate, TipoFactura tipofac, IndicadorNormalCorrector indnorcor, ReconciliationStatusType status) throws Exception {
    if(startDate == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "startDate"));
    }

    if(endDate == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "endDate"));
    }

    if(tipofac == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "tipofac"));
    }

    if(indnorcor == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "indnorcor"));
    }

    if(status == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "status"));
    }

    String allowedFormat = "\\d{8}";
    if (!startDate.matches(allowedFormat)) {
      throw new BadRequestException(PARAMETRO_NO_CUMPLE_FORMATO_$VALUE).setData(new KeyValue("value", "startDate"));
    }

    if (!endDate.matches(allowedFormat)) {
      throw new BadRequestException(PARAMETRO_NO_CUMPLE_FORMATO_$VALUE).setData(new KeyValue("value", "endDate"));
    }


    Object[] params = {
      startDate,
      endDate,
      new InParam(tipofac.getCode(), Types.NUMERIC),
      new InParam(indnorcor.getValue(), Types.NUMERIC),
      status.getValue(),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp =  getDbUtils().execute(getSchema() + ".mc_prp_actualiza_no_conciliados_tecnocom_v10", params);

    if (!"0".equals(resp.get("_error_code"))) {
      log.error("updatePendingPrepaidMovementsTecnocomStatus resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
  }

  @Override
  public List<PrepaidMovement10> getPrepaidMovements(Long id, Long idMovimientoRef, Long idPrepaidUser, String idTxExterno, PrepaidMovementType tipoMovimiento,
                                                    PrepaidMovementStatus estado, String cuenta, CodigoMoneda clamon, IndicadorNormalCorrector indnorcor, TipoFactura tipofac, Date fecfac, String numaut) throws Exception {

    return this.getPrepaidMovements(id, idMovimientoRef, idPrepaidUser, idTxExterno, tipoMovimiento, estado, cuenta,
      clamon, indnorcor, tipofac, fecfac, numaut, null, null, null);
  }


  @Override
  public List<PrepaidMovement10> getPrepaidMovements(Long id, Long idMovimientoRef, Long idPrepaidUser, String idTxExterno, PrepaidMovementType tipoMovimiento,
                                                     PrepaidMovementStatus estado, String cuenta, CodigoMoneda clamon, IndicadorNormalCorrector indnorcor, TipoFactura tipofac, Date fecfac, String numaut,
                                                     ReconciliationStatusType estadoConSwitch, ReconciliationStatusType estadoConTecnocom, MovementOriginType origen) throws Exception {

    Object[] params = {
      id != null ? id : new NullParam(Types.BIGINT),
      idMovimientoRef != null ? idMovimientoRef : new NullParam(Types.BIGINT),
      idPrepaidUser != null ? idPrepaidUser : new NullParam(Types.BIGINT),
      idTxExterno != null ? idTxExterno : new NullParam(Types.VARCHAR),
      tipoMovimiento != null ? tipoMovimiento.toString() : new NullParam(Types.VARCHAR),
      estado != null ? estado.toString() : new NullParam(Types.VARCHAR),
      estadoConSwitch != null ? estadoConSwitch.toString() : new NullParam(Types.VARCHAR), // estado_con_switch
      estadoConTecnocom != null ? estadoConTecnocom.toString() : new NullParam(Types.VARCHAR), // estado_con_tecnocom
      origen != null ? origen.toString() : new NullParam(Types.VARCHAR), // origen_movimiento
      cuenta != null ? cuenta : new NullParam(Types.VARCHAR),
      clamon != null ? clamon.getValue() : new NullParam(Types.NUMERIC),
      indnorcor != null ? indnorcor.getValue() : new NullParam(Types.NUMERIC),
      tipofac != null ? tipofac.getCode() : new NullParam(Types.NUMERIC),
      fecfac != null ? fecfac : new NullParam(Types.DATE),
      numaut != null ? numaut : new NullParam(Types.VARCHAR),

    };

    //se registra un OutParam del tipo cursor (OTHER) y se agrega un rowMapper para transformar el row al objeto necesario
    RowMapper rm = (Map<String, Object> row) -> {
      try{
      PrepaidMovement10 p = new PrepaidMovement10();
      p.setId(getNumberUtils().toLong(row.get("_id")));
      p.setIdMovimientoRef(getNumberUtils().toLong(row.get("_id_movimiento_ref")));
      p.setIdPrepaidUser(getNumberUtils().toLong(row.get("_id_usuario")));
      p.setIdTxExterno(String.valueOf(row.get("_id_tx_externo")));
      p.setTipoMovimiento(PrepaidMovementType.valueOfEnum(String.valueOf(row.get("_tipo_movimiento"))));
      p.setMonto(getNumberUtils().toBigDecimal(row.get("_monto")));
      p.setEstado(PrepaidMovementStatus.valueOfEnum(String.valueOf(row.get("_estado"))));
      p.setEstadoNegocio(BusinessStatusType.fromValue(String.valueOf(row.get("_estado_de_negocio"))));
      p.setConSwitch(ReconciliationStatusType.fromValue(String.valueOf(row.get("_estado_con_switch"))));
      p.setConTecnocom(ReconciliationStatusType.fromValue(String.valueOf(row.get("_estado_con_tecnocom"))));
      p.setOriginType(MovementOriginType.fromValue(String.valueOf(row.get("_origen_movimiento"))));
      p.setFechaCreacion((Timestamp) row.get("_fecha_creacion"));
      p.setFechaActualizacion((Timestamp) row.get("_fecha_actualizacion"));
      p.setCodent(String.valueOf(row.get("_codent")));
      p.setCentalta(String.valueOf(row.get("_centalta")));
      p.setCuenta(String.valueOf(row.get("_cuenta")));
      p.setClamon(CodigoMoneda.fromValue(getNumberUtils().toInteger(row.get("_clamon"))));
      p.setIndnorcor(IndicadorNormalCorrector.fromValue(getNumberUtils().toInteger(row.get("_indnorcor"))));
      p.setTipofac(TipoFactura.valueOfEnumByCodeAndCorrector(getNumberUtils().toInteger(row.get("_tipofac")), p.getIndnorcor().getValue()));
      p.setFecfac((Date)row.get("_fecfac"));
      p.setNumreffac(String.valueOf(row.get("_numreffac")));
      p.setPan(String.valueOf(row.get("_pan")));
      p.setClamondiv(getNumberUtils().toInteger(row.get("_clamondiv")));
      p.setImpdiv(getNumberUtils().toLong(row.get("_impdiv")));
      p.setImpfac(getNumberUtils().toBigDecimal(row.get("_impfac")));
      p.setCmbapli(getNumberUtils().toInteger(row.get("_cmbapli")));
      p.setNumaut(String.valueOf(row.get("_numaut")));
      p.setIndproaje(IndicadorPropiaAjena.fromValue(String.valueOf(row.get("_indproaje"))));
      p.setCodcom(String.valueOf(row.get("_codcom")));
      p.setCodact(getNumberUtils().toInteger(row.get("_codact")));
      p.setImpliq(getNumberUtils().toLong(row.get("_impliq")));
      p.setClamonliq(getNumberUtils().toInteger(row.get("_clamonliq")));
      p.setCodpais(CodigoPais.fromValue(getNumberUtils().toInteger(row.get("_codpais"))));
      p.setNompob(String.valueOf(row.get("_nompob")));
      p.setNumextcta(getNumberUtils().toInteger(row.get("_numextcta")));
      p.setNummovext(getNumberUtils().toInteger(row.get("_nummovext")));
      p.setClamone(getNumberUtils().toInteger(row.get("_clamone")));
      p.setTipolin(String.valueOf(row.get("_tipolin")));
      p.setLinref(getNumberUtils().toInteger(row.get("_linref")));
      p.setNumbencta(getNumberUtils().toInteger(row.get("_numbencta")));
      p.setNumplastico(getNumberUtils().toLong(row.get("_numplastico")));

      return p;
      }catch (Exception e){
        e.printStackTrace();
        log.info("RowMapper Error: "+e);
        return null;
      }
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_prp_buscar_movimientos_v10", rm, params);
    log.info("Respuesta Busca Movimiento: "+resp);
    return (List)resp.get("result");
  }


  @Override
  public PrepaidMovement10 getPrepaidMovementById(Long id) throws Exception {
    log.info("[getPrepaidMovementById In Id] : "+id);
    if(id == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }
    log.info(String.format("ID IN : %s",id));
    List<PrepaidMovement10> lst = this.getPrepaidMovements(id, null, null, null, null, null, null, null, null, null, null, null);
    log.info("getPrepaidMovementById: "+lst);
    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }

  @Override
  public List<PrepaidMovement10> getPrepaidMovementByIdPrepaidUser(Long idPrepaidUser) throws Exception {
    if(idPrepaidUser == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "idPrepaidUser"));
    }
    return this.getPrepaidMovements(null, null, idPrepaidUser, null, null, null, null, null, null, null, null, null);
  }

  @Override
  public PrepaidMovement10 getPrepaidMovementByIdPrepaidUserAndIdMovement(Long idPrepaidUser, Long IdMovement) throws Exception {
    if(idPrepaidUser == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "idPrepaidUser"));
    }
    if(IdMovement == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "IdMovement"));
    }
    List<PrepaidMovement10> lst = this.getPrepaidMovements(IdMovement, null, idPrepaidUser, null, null, null, null, null, null, null, null, null);
    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }

  @Override
  public List<PrepaidMovement10> getPrepaidMovementByIdPrepaidUserAndEstado(Long idPrepaidUser, PrepaidMovementStatus estado) throws Exception {
    if(idPrepaidUser == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "idPrepaidUser"));
    }
    if(estado == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "estado"));
    }
    return this.getPrepaidMovements(null, null, idPrepaidUser, null, null, estado, null, null, null, null, null, null);
  }

  @Override
  public PrepaidMovement10 getLastPrepaidMovementByIdPrepaidUserAndOneStatus(Long idPrepaidUser, PrepaidMovementStatus... status) throws Exception {
    List<PrepaidMovement10> movements = this.getPrepaidMovementByIdPrepaidUser(idPrepaidUser);

    PrepaidMovement10 movement = movements != null && !movements.isEmpty() ? movements.get(0) : null;

    if (movement == null) {
      return null;
    }

    for (PrepaidMovementStatus st : status) {
      if (st.equals(movement.getEstado())) {
        return movement;
      }
    }
    return null;
  }

  @Override
  public List<PrepaidMovement10> getPrepaidMovementByIdPrepaidUserAndTipoMovimiento(Long idPrepaidUser, PrepaidMovementType tipoMovimiento) throws Exception {
    if(idPrepaidUser == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "idPrepaidUser"));
    }
    if(tipoMovimiento == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "tipoMovimiento"));
    }
    return this.getPrepaidMovements(null, null, idPrepaidUser, null, tipoMovimiento, null, null, null, null, null, null, null);
  }

  @Override
  public List<PrepaidMovement10> getPrepaidMovementByIdPrepaidUserAndTipoMovimientoAndEstado(Long idPrepaidUser, PrepaidMovementType tipoMovimiento, PrepaidMovementStatus status) throws Exception {
    if(idPrepaidUser == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "idPrepaidUser"));
    }
    if(tipoMovimiento == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "tipoMovimiento"));
    }
    if(status == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "estado"));
    }

    return this.getPrepaidMovements(null, null, idPrepaidUser, null, tipoMovimiento, status, null, null, null, null, null, null);
  }

  public PrepaidMovement10 getPrepaidMovementForReverse(Long idPrepaidUser, String idTxExterno, PrepaidMovementType tipoMovimiento, TipoFactura tipofac) throws Exception {
    if(idPrepaidUser == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "idPrepaidUser"));
    }
    if(idTxExterno == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "idTxExterno"));
    }
    if(tipoMovimiento == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "tipoMovimiento"));
    }
    if(tipofac == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "tipofac"));
    }

    List<PrepaidMovement10> lst = this.getPrepaidMovements(null, null, idPrepaidUser, idTxExterno, tipoMovimiento, null, null, null, IndicadorNormalCorrector.fromValue(tipofac.getCorrector()), tipofac, null, null);
    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }

  public PrepaidMovement10 getPrepaidMovementReverse(Long idMovimientoRef)throws Exception{
    if(idMovimientoRef == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "idMovimientoRef"));
    }
    List<PrepaidMovement10> lst = this.getPrepaidMovements(null, idMovimientoRef, null, null, PrepaidMovementType.WITHDRAW, null, null, null, IndicadorNormalCorrector.CORRECTORA, null, null, null);
    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }

  public PrepaidMovement10 getPrepaidMovementByIdTxExterno(String idTxExterno,PrepaidMovementType prepaidMovementType,IndicadorNormalCorrector indicadorNormalCorrector) throws Exception {
    if(idTxExterno == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "idTxExterno"));
    }
    List<PrepaidMovement10> lst = this.getPrepaidMovements(null, null, null, idTxExterno, prepaidMovementType, null, null, null, indicadorNormalCorrector, null,null, null);
    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }

  @Override
  public Boolean isFirstTopup(Long idPrepaidUser) throws Exception {
    if(idPrepaidUser == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "idPrepaidUser"));
    }

    List<PrepaidMovement10> movements = this.getPrepaidMovementByIdPrepaidUserAndTipoMovimientoAndEstado(idPrepaidUser,
      PrepaidMovementType.TOPUP,
      PrepaidMovementStatus.PROCESS_OK);

    return  !(movements != null && !movements.isEmpty());
  }

  @Override
  public boolean updateStatusMovementConSwitch(Map<String, Object> header, Long movementId, ReconciliationStatusType status) throws Exception {
    if (movementId == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "movementId"));
    }
    if (status == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "status"));
    }

    Object[] params = {
      movementId,
      status.getValue(),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp =  getDbUtils().execute(getSchema() + ".mc_prp_actualiza_movimiento_estado_switch_v10",params);

    if (!"0".equals(resp.get("_error_code"))) {
      log.error("updateStatusMovementConSwitch resp: " + resp);
      return false;
    }
    return true;
  }

  @Override
  public void updateStatusMovementConTecnocom(Map<String, Object> header, Long movementId, ReconciliationStatusType status) throws Exception {
    if (movementId == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "movementId"));
    }
    if (status == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "status"));
    }

    Object[] params = {
      movementId,
      status.getValue(),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp =  getDbUtils().execute(getSchema() + ".mc_prp_actualiza_movimiento_estado_tecnocom_v10",params);

    if (!"0".equals(resp.get("_error_code"))) {
      log.error("updateStatusMovementConTecnocom resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
  }

  @Override
  public PrepaidMovement10 getPrepaidMovementForTecnocomReconciliation(Long idPrepaidUser, String numaut, Date fecfac, TipoFactura tipofac) throws Exception {
    if(idPrepaidUser == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "idPrepaidUser"));
    }
    if(numaut == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "numaut"));
    }
    if(fecfac == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "fecfac"));
    }
    if(tipofac == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "tipofac"));
    }


    List<PrepaidMovement10> lst = this.getPrepaidMovements(null, null, idPrepaidUser, null, null, null, null, null, IndicadorNormalCorrector.fromValue(tipofac.getCorrector()), tipofac, fecfac, numaut);

    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }

  public CdtTransaction10 processRefundMovement(Long userPrepagoId, Long movementId) throws Exception{

    CdtTransaction10 cdtTransaction = null;

    PrepaidUserEJBBean10 prepaidUserEJBBean10 = new PrepaidUserEJBBean10();
    PrepaidUser10 prepaidUserTest = prepaidUserEJBBean10.getPrepaidUserById(null,userPrepagoId);
    if (prepaidUserTest == null){
      return cdtTransaction;
    }

    PrepaidMovementEJBBean10 prepaidMovementEJBBean10 = new PrepaidMovementEJBBean10();
    PrepaidMovement10 prepaidMovementTest = prepaidMovementEJBBean10.getPrepaidMovementById(movementId.longValue());
    if (prepaidMovementTest == null){
      return cdtTransaction;
    }

    PrepaidMovement10 prepaidMovement10sTest = prepaidMovementEJBBean10.
      getPrepaidMovementByIdPrepaidUserAndIdMovement(userPrepagoId,movementId);
    if(prepaidMovement10sTest == null) {
      return cdtTransaction;
    }

    PrepaidMovement10 prepaidMovement = prepaidMovementEJBBean10.getPrepaidMovementByIdPrepaidUserAndIdMovement(userPrepagoId,movementId);

    Long _movementId = prepaidMovement.getId();

    prepaidMovementEJBBean10.updatePrepaidBusinessStatus(null, _movementId, BusinessStatusType.REFUND_OK);

    List<CdtTransaction10> transaction10s = getCdtEJB10().buscaListaMovimientoByIdExterno(null,prepaidMovement.getIdTxExterno());

    if(transaction10s.size() > 0){

      for (ListIterator<CdtTransaction10> iter = transaction10s.listIterator(); iter.hasNext();) {
        cdtTransaction = iter.next();

        if(cdtTransaction.getCdtTransactionTypeConfirm() != null){

          cdtTransaction.setTransactionType(cdtTransaction.getCdtTransactionTypeConfirm());
          cdtTransaction.setIndSimulacion(Boolean.FALSE);
          cdtTransaction.setTransactionReference(cdtTransaction.getId());
          cdtTransaction = getCdtEJB10().addCdtTransaction(null, cdtTransaction);

        }

      }

    }

    return cdtTransaction;
  }

  @Override
  public void createMovementConciliate(Map<String, Object> headers, Long idMovRef, ReconciliationActionType actionType, ReconciliationStatusType statusType) throws Exception {
    if(idMovRef == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "idMovRef"));
    }
    if(actionType == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "actionType"));
    }
    if(statusType == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "statusType"));
    }
    Object[] params = {
      idMovRef,
      actionType.name(),
      statusType.name(),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp = getDbUtils().execute(String.format("%s.mc_prp_crea_movimiento_conciliado_v10",getSchema()),params);
    if (!"0".equals(resp.get("_error_code"))) {
      log.error("mc_prp_crea_movimiento_conciliado_v10 resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
  }

  @Override
  public void createMovementResearch(Map<String, Object> headers, String movRef, ReconciliationOriginType originType, String fileName) throws Exception {
    if(movRef == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "movRef"));
    }
    if(originType == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "originType"));
    }
    if(fileName == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "fileName"));
    }
    Object[] params = {
      movRef,
      originType.name(),
      fileName,
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };
    Map<String,Object> resp = getDbUtils().execute(String.format("%s.mc_prp_crea_movimiento_investigar_v10",getSchema()),params);
    if (!"0".equals(resp.get("_error_code"))) {
      log.error("mc_prp_crea_movimiento_investigar_v10 resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
  }

  @Override
  public List<PrepaidMovement10> getMovementsForConciliate(Map<String, Object> headers) throws Exception {

    RowMapper rm = (Map<String, Object> row) -> {
      PrepaidMovement10 movement10 = new PrepaidMovement10();
      movement10.setId(getNumberUtils().toLong(row.get("_id")));
      movement10.setEstado(PrepaidMovementStatus.valueOfEnum(String.valueOf(row.get("_estado"))));
      movement10.setEstadoNegocio(BusinessStatusType.fromValue(String.valueOf(row.get("_estado_de_negocio"))));
      movement10.setConSwitch(ReconciliationStatusType.fromValue(String.valueOf(row.get("_estado_con_switch"))));
      movement10.setConTecnocom(ReconciliationStatusType.fromValue(String.valueOf(row.get("_estado_con_tecnocom"))));
      movement10.setTipoMovimiento(PrepaidMovementType.valueOf(String.valueOf(row.get("_tipo_movimiento"))));
      movement10.setIndnorcor(IndicadorNormalCorrector.fromValue(getNumberUtils().toInt(row.get("_indnorcor"))));
      return movement10;
    };
    Map<String, Object> resp = getDbUtils().execute(String.format("%s.mc_prp_busca_movimientos_conciliar_v10",getSchema()), rm);
    return (List)resp.get("result");
  }

  public void processReconciliation(PrepaidMovement10 mov) throws Exception {
    String messageID = "";
    // Excel fila 1
    log.info("Mov to Reconciliation: "+mov);
    if(ReconciliationStatusType.RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.RECONCILED.equals(mov.getConSwitch())&& PrepaidMovementStatus.PROCESS_OK.equals(mov.getEstado())) {
      log.debug("XLS ID 1");
      createMovementConciliate(null,mov.getId(), ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);
    }
    // Excel fila 2
    else if(ReconciliationStatusType.RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConSwitch())&& PrepaidMovementStatus.PROCESS_OK.equals(mov.getEstado())) {
      log.info("XLS ID 2");
      log.info("Get Prepaid by ID: "+mov.getId());
      PrepaidMovement10 movFull = getPrepaidMovementById(mov.getId());
      log.info(movFull);
      if(PrepaidMovementType.TOPUP.equals(mov.getTipoMovimiento())){
        if(IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor())) {

          //Se busca usuario prepago para obtener user
          PrepaidUser10 prepaidUser10 = getPrepaidUserEJB10().getPrepaidUserById(null,movFull.getIdPrepaidUser());
          if(prepaidUser10 == null){
            log.info("prepaidTopup10 null");
          }
          //Se busca user para obterner rut
          User user = userClient.getUserById(null,prepaidUser10.getUserIdMc());
          if(user == null){
            log.info("user null");
          }
          PrepaidMovement10 movToReverse = getPrepaidMovementForReverse(prepaidUser10.getId(),movFull.getIdTxExterno(),PrepaidMovementType.TOPUP,movFull.getTipofac());
          // Se crea movimiento de reversa
          NewPrepaidTopup10 newPrepaidTopup10 = new NewPrepaidTopup10();
          newPrepaidTopup10.setAmount(new NewAmountAndCurrency10(movFull.getMonto()));
          newPrepaidTopup10.setMerchantCategory(movFull.getCodact());
          newPrepaidTopup10.setMerchantCode(movFull.getCodcom());
          newPrepaidTopup10.setMerchantName("Conciliacion");
          newPrepaidTopup10.setRut(user.getRut().getValue());
          newPrepaidTopup10.setTransactionId(movFull.getIdTxExterno());
          log.info(newPrepaidTopup10);
          log.info(movToReverse);
          log.info(movFull);
          // Se envia movimiento a reversar
          getPrepaidEJBBean10().reverseTopupUserBalance(null,newPrepaidTopup10,false);
          // Se agrega a movimiento conciliado para que no vuelva a ser enviado.
          createMovementConciliate(null,mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.COUNTER_MOVEMENT);
        }
        else {
          createMovementResearch(null,String.format("idMov=%s",mov.getId()), ReconciliationOriginType.MOTOR,"");
          createMovementConciliate(null,mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
        }
      }
      else{
        if(IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor())){

          //Se busca usuario prepago para obtener user
          PrepaidUser10 prepaidUser10 = getPrepaidUserEJB10().getPrepaidUserById(null,movFull.getIdPrepaidUser());
          if(prepaidUser10 == null){
            log.info("prepaidTopup10 null");
            throw new ValidationException();
          }
          //Se busca user para obterner rut
          User user = userClient.getUserById(null,prepaidUser10.getUserIdMc());
          if(user == null){
            log.info("user null");
          }
          //PrepaidMovement10 movToReverse = getPrepaidMovementForReverse(prepaidUser10.getId(),movFull.getIdTxExterno(),PrepaidMovementType.TOPUP,movFull.getTipofac());

          //Se envia a reversar el retiro
          NewPrepaidWithdraw10 newPrepaidWithdraw10 = new NewPrepaidWithdraw10();
          newPrepaidWithdraw10.setTransactionId(movFull.getIdTxExterno());
          newPrepaidWithdraw10.setAmount(new NewAmountAndCurrency10(movFull.getMonto()));

          newPrepaidWithdraw10.setMerchantCode(movFull.getCodcom());
          newPrepaidWithdraw10.setMerchantCategory(movFull.getCodact());
          newPrepaidWithdraw10.setMerchantName("Conciliacion");
          newPrepaidWithdraw10.setRut(user.getRut().getValue());
          newPrepaidWithdraw10.setPassword("CONCI");
          log.info(newPrepaidWithdraw10);
          //log.info(movToReverse);
          getPrepaidEJBBean10().reverseWithdrawUserBalance(null,newPrepaidWithdraw10,false);
          // Se agrega a la tabla para que no vuelva a ser enviado
          createMovementConciliate(null,mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.COUNTER_MOVEMENT);

        }else {
          createMovementResearch(null,String.format("idMov=%s",mov.getId()), ReconciliationOriginType.MOTOR,"");
          createMovementConciliate(null,mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
        }
      }
    }
    // Excel fila 3
    else if(ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.RECONCILED.equals(mov.getConSwitch())&& PrepaidMovementStatus.PROCESS_OK.equals(mov.getEstado())){
      log.debug("XLS ID 3");
      createMovementResearch(null,String.format("idMov=%s",mov.getId()), ReconciliationOriginType.MOTOR,"");
      createMovementConciliate(null,mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    }
    // Excel fila 4
    else if(ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConSwitch())&& PrepaidMovementStatus.PROCESS_OK.equals(mov.getEstado())){
      log.debug("XLS ID 4");
      createMovementResearch(null,String.format("idMov=%s",mov.getId()), ReconciliationOriginType.MOTOR,"");
      createMovementConciliate(null,mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    }
    // Excel fila 5
    else if(ReconciliationStatusType.RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.RECONCILED.equals(mov.getConSwitch())&&
      ( PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE.equals(mov.getEstado()) ||
        PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION.equals(mov.getEstado()) ||
        PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE.equals(mov.getEstado())
      ) && mov.getTipoMovimiento().equals(PrepaidMovementType.TOPUP)
    ){
      log.debug("XLS ID 5");
      createMovementResearch(null,String.format("idMov=%s",mov.getId()), ReconciliationOriginType.MOTOR,"");
      createMovementConciliate(null,mov.getId(), ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);
      updatePrepaidMovementStatus(null,mov.getId(),PrepaidMovementStatus.PROCESS_OK);
    }
    // Excel fila 6
    else if( ReconciliationStatusType.RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConSwitch())&&
      ( PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE.equals(mov.getEstado()) ||
        PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION.equals(mov.getEstado()) ||
        PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE.equals(mov.getEstado())
      ) &&
        PrepaidMovementType.TOPUP.equals(mov.getTipoMovimiento())) {
      log.debug("XLS ID 6");
      // Se obtiene el movimiento completo.--
      PrepaidMovement10 movFull = getPrepaidMovementById(mov.getId());

      if (IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor())) {

        //Se busca usuario prepago para obtener user
        PrepaidUser10 prepaidUser10 = getPrepaidUserEJB10().getPrepaidUserById(null,movFull.getIdPrepaidUser());
        if(prepaidUser10 == null){
          log.info("prepaidTopup10 null");
        }
        //Se busca user para obterner rut
        User user = userClient.getUserById(null,prepaidUser10.getUserIdMc());
        if(user == null){
          log.info("user null");
        }
        // Se crea movimiento de reversa
        NewPrepaidTopup10 newPrepaidTopup10 = new NewPrepaidTopup10();
        newPrepaidTopup10.setAmount(new NewAmountAndCurrency10(movFull.getMonto()));
        newPrepaidTopup10.setMerchantCode(movFull.getCodcom());
        newPrepaidTopup10.setMerchantCategory(movFull.getCodact());
        newPrepaidTopup10.setMerchantName("Conciliacion");
        newPrepaidTopup10.setRut(user.getRut().getValue());
        newPrepaidTopup10.setTransactionId(movFull.getIdTxExterno());
        // Se envia movimiento a reversar
        getPrepaidEJBBean10().reverseTopupUserBalance(null,newPrepaidTopup10,false);
        // Se agrega a la tabla de movimientos conciliados para que no vuelkva a ser enviado
        createMovementConciliate(null,mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.COUNTER_MOVEMENT);

      }
      else {
        createMovementResearch(null, String.format("idMov=%s", mov.getId()), ReconciliationOriginType.MOTOR, "");
        createMovementConciliate(null,mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
      }

    }
    // Excel fila 7
    else if(ReconciliationStatusType.RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConSwitch())&&
      PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE.equals(mov.getEstado()) &&
      PrepaidMovementType.WITHDRAW.equals(mov.getTipoMovimiento() )&&
      IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor())
    ){
      log.debug("XLS ID 7");
      PrepaidMovement10 movFull = getPrepaidMovementById(mov.getId());
      if(movFull != null ) {
        if(PrepaidMovementStatus.PROCESS_OK.equals(movFull.getEstado())) {
          createMovementConciliate(null, mov.getId(), ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);
        } else {
          //TODO: que se hace en los otros casos?
        }
      }
      else { // SE REVERSA EL MOVIMIENTO DE RETIRO

        //Se busca usuario prepago para obtener user
        PrepaidUser10 prepaidUser10 = getPrepaidUserEJB10().getPrepaidUserById(null,movFull.getIdPrepaidUser());
        if(prepaidUser10 == null){
          log.info("prepaidTopup10 null");
        }
        //Se busca user para obterner rut
        User user = userClient.getUserById(null,prepaidUser10.getUserIdMc());
        if(user == null){
          log.info("user null");
        }
        // Se crea movimiento de reversa
        NewPrepaidTopup10 newPrepaidTopup10 = new NewPrepaidTopup10();
        newPrepaidTopup10.setAmount(new NewAmountAndCurrency10(movFull.getMonto()));
        newPrepaidTopup10.setMerchantCode(movFull.getCodcom());
        newPrepaidTopup10.setMerchantCategory(movFull.getCodact());
        newPrepaidTopup10.setMerchantName("Conciliacion");
        newPrepaidTopup10.setRut(user.getRut().getValue());
        newPrepaidTopup10.setTransactionId(movFull.getNumaut());
        // Se envia movimiento a reversar
        getPrepaidEJBBean10().reverseTopupUserBalance(null,newPrepaidTopup10,false);
        // Se agrega a la tabla de movimientos conciliados para que no vuelkva a ser enviado
        createMovementConciliate(null,mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.COUNTER_MOVEMENT);

      }
    }
    // Excel fila 8
    else if(ReconciliationStatusType.RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.RECONCILED.equals(mov.getConSwitch())&&  ( PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE.equals(mov.getEstado()) ||
      PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION.equals(mov.getEstado()) ||
      PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE.equals(mov.getEstado())
    ) && PrepaidMovementType.WITHDRAW.equals(mov.getTipoMovimiento())){
      log.debug("XLS ID 8");
      createMovementResearch(null,String.format("idMov=%s",mov.getId()), ReconciliationOriginType.MOTOR,"");
      createMovementConciliate(null,mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    }
    // Excel fila 9
    else if(ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.RECONCILED.equals(mov.getConSwitch())&&  ( PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE.equals(mov.getEstado()) ||
      PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION.equals(mov.getEstado()) ||
      PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE.equals(mov.getEstado())
    ) && PrepaidMovementType.TOPUP.equals(mov.getTipoMovimiento())) {
      log.debug("XLS ID 9");
      PrepaidMovement10 movFull = getPrepaidMovementById(mov.getId());

      if (IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor())) {
        //Se busca usuario prepago para obtener user
        PrepaidUser10 prepaidUser10 = getPrepaidUserEJB10().getPrepaidUserById(null,movFull.getIdPrepaidUser());
        if(prepaidUser10 == null){
          log.info("prepaidTopup10 null");
        }
        //Se busca user para obterner rut
        User user = userClient.getUserById(null,prepaidUser10.getUserIdMc());
        if(user == null){
          log.info("user null");
        }
        // Se crea movimiento de reversa
        NewPrepaidTopup10 newPrepaidTopup10 = new NewPrepaidTopup10();
        newPrepaidTopup10.setAmount(new NewAmountAndCurrency10(movFull.getMonto()));
        newPrepaidTopup10.setMerchantCategory(movFull.getCodact());
        newPrepaidTopup10.setMerchantCode(movFull.getCodcom());
        newPrepaidTopup10.setMerchantName("Conciliacion");
        newPrepaidTopup10.setRut(user.getRut().getValue());
        String newTxId = String.valueOf(getNumberUtils().random(8000000L,9999999L));
        newPrepaidTopup10.setTransactionId(newTxId);
        // Se envia movimiento a reversar
        getPrepaidEJBBean10().topupUserBalance(null,newPrepaidTopup10,false);
        // Se agrega a movimiento conciliado para que no vuelva a ser enviado.
        createMovementConciliate(null,mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.COUNTER_MOVEMENT);
        createMovementResearch(null, String.format("idMov=%s", mov.getId()), ReconciliationOriginType.MOTOR, "");
      } else {
        //TODO: que se debe hacer en los otros casos?
      }
    }
    else if(ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.RECONCILED.equals(mov.getConSwitch()) && (
      PrepaidMovementStatus.REJECTED.equals(mov.getEstado())
    ) && PrepaidMovementType.TOPUP.equals(mov.getTipoMovimiento())) {
      log.debug("XLS ID 9");
      System.out.println("XLS ID 9");
      PrepaidMovement10 movFull = getPrepaidMovementById(mov.getId());

      if(IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor())) {
        //Se busca usuario prepago para obtener user
        PrepaidUser10 prepaidUser10 = getPrepaidUserEJB10().getPrepaidUserById(null, movFull.getIdPrepaidUser());
        if(prepaidUser10 == null) {
          log.info("prepaidTopup10 null");
        }
        //Se busca user para obterner rut
        User user = userClient.getUserById(null, prepaidUser10.getUserIdMc());
        if(user == null) {
          log.info("user null");
          return;
        }

        //TODO: esta correcto crear una reversa? El movimiento fue rechazado por tecnocom.
        // Y porque se envia a research? Que no esté en tecnocom es normal si fue rechazado.
        /*
        // Se crea movimiento de reversa
        NewPrepaidTopup10 newPrepaidTopup10 = new NewPrepaidTopup10();
        newPrepaidTopup10.setAmount(new NewAmountAndCurrency10(movFull.getMonto()));
        newPrepaidTopup10.setMerchantCategory(movFull.getCodact());
        newPrepaidTopup10.setMerchantCode(movFull.getCodcom());
        newPrepaidTopup10.setMerchantName("Conciliacion" );
        newPrepaidTopup10.setRut(user.getRut().getValue());
        String newTxId = String.valueOf(getNumberUtils().random(8000000L,9999999L));
        newPrepaidTopup10.setTransactionId(newTxId);
        // Se envia movimiento a reversar
        getPrepaidEJBBean10().topupUserBalance(null,newPrepaidTopup10,false);

        createMovementConciliate(null,mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.COUNTER_MOVEMENT);
        createMovementResearch(null, String.format("idMov=%s", mov.getId()), ReconciliationOriginType.MOTOR, "");
        */

        // Enviar movimiento a REFUND
        createMovementConciliate(null, movFull.getId(), ReconciliationActionType.REFUND, ReconciliationStatusType.TO_REFUND);
        updatePrepaidBusinessStatus(null, movFull.getId(), BusinessStatusType.TO_REFUND);

        // Confirmar el topup en el CDT
        CdtTransaction10 cdtTransaction = getCdtEJB10().buscaMovimientoByIdExterno(null, movFull.getIdTxExterno());

        CdtTransactionType reverseTransactionType = cdtTransaction.getCdtTransactionTypeReverse();
        cdtTransaction.setTransactionType(cdtTransaction.getCdtTransactionTypeConfirm());
        cdtTransaction.setIndSimulacion(Boolean.FALSE);
        cdtTransaction.setTransactionReference(cdtTransaction.getId());
        cdtTransaction = getCdtEJB10().addCdtTransaction(null, cdtTransaction);

        // Iniciar reversa en CDT
        cdtTransaction.setTransactionType(reverseTransactionType);
        cdtTransaction.setTransactionReference(0L);
        cdtTransaction = getCdtEJB10().addCdtTransaction(null, cdtTransaction);

        // Enviar ticket a freshdesk
        String template = getParametersUtil().getString("api-prepaid", "template_ticket_devolucion", "v1.0");
        template = TemplateUtils.freshDeskTemplateDevolucion(template, String.format("%s %s", user.getName(), user.getLastname_1()), String.format("%s-%s", user.getRut().getValue(), user.getRut().getDv()), user.getId(), movFull.getNumaut(), movFull.getMonto().longValue(), user.getEmail().getValue(), user.getCellphone().getValue());

        NewTicket newTicket = new NewTicket();
        newTicket.setDescription(template);
        newTicket.setGroupId(GroupId.OPERACIONES);
        newTicket.setUniqueExternalId(String.valueOf(user.getRut().getValue()));
        newTicket.setType(TicketType.DEVOLUCION);
        newTicket.setStatus(StatusType.OPEN);
        newTicket.setPriority(PriorityType.URGENT);
        newTicket.setSubject("Devolucion de carga");
        newTicket.setProductId(43000001595L);
        newTicket.addCustomField("cf_id_movimiento", movFull.getId().toString());

        Ticket ticket = getUserClient().createFreshdeskTicket(null, user.getId(), newTicket);
        if (ticket.getId() != null) {
          log.info("Ticket Creado Exitosamente");
        }
      } else {
        //TODO: que se hace en los otros casos?
      }
    }
    //Movimientos que esten en estado pendiente o en proceso y vengan en alguno de los archivos Caso 19 al 24
    else if (PrepaidMovementStatus.PENDING.equals(mov.getEstado())||PrepaidMovementStatus.IN_PROCESS.equals(mov.getEstado())){
      log.debug("Movimiento Pendiente o En proceso");
      createMovementResearch(null,String.format("idMov=%s",mov.getId()), ReconciliationOriginType.MOTOR,"");
      createMovementConciliate(null,mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
    } else {
        log.error("No cae en ningun caso: "+mov);
        createMovementResearch(null,String.format("idMov=%s",mov.getId()), ReconciliationOriginType.MOTOR,"");
        createMovementConciliate(null,mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NO_CASE);
    }

  }




}
