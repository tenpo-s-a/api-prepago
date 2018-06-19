package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.prepaid.model.v10.PrepaidMovement10;
import cl.multicaja.prepaid.model.v10.PrepaidMovementStatus;
import cl.multicaja.prepaid.model.v10.PrepaidMovementType;
import cl.multicaja.tecnocom.constants.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.sql.Date;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static cl.multicaja.core.model.Errors.ERROR_DE_COMUNICACION_CON_BBDD;
import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;
import static cl.multicaja.core.model.Errors.PARAMETRO_ILEGIBLE_$VALUE;

@Stateless
@LocalBean
@TransactionManagement(value=TransactionManagementType.CONTAINER)
public class PrepaidMovementEJBBean10 extends PrepaidBaseEJBBean10 implements PrepaidMovementEJB10 {

  private static Log log = LogFactory.getLog(PrepaidMovementEJBBean10.class);

  @Override
  public PrepaidMovement10 addPrepaidMovement(Map<String, Object> header, PrepaidMovement10 data) throws Exception {

    Object[] params = {
      new InParam(data.getIdMovimientoRef(),Types.NUMERIC), //_id_mov_ref NUMERIC
      new InParam(data.getIdPrepaidUser(),Types.NUMERIC), //_id_usuario NUMERIC
      data.getIdTxExterno(), //_id_tx_externo VARCHAR
      data.getTipoMovimiento().toString(), //_tipo_movimiento VARCHAR
      new InParam(data.getMonto(),Types.NUMERIC), //_monto NUMERIC
      data.getEstado().toString(), //_estado VARCHAR
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
      data.getNumaut(),//_numaut    VARCHAR
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

    System.out.println(resp);

    if ("0".equals(resp.get("_error_code"))) {
      data.setId(numberUtils.toLong(resp.get("_r_id")));
      return data;
    } else {
      log.error("addPrepaidMovement resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
  }

  @Override
  public void updatePrepaidMovement(Map<String, Object> header, Long id, Integer numextcta, Integer nummovext, Integer clamone, PrepaidMovementStatus status) throws Exception { ;

    if(id == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }

    if(status == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "status"));
    }

    Object[] params = {
      new InParam(id,Types.NUMERIC),
      numextcta == null ? new NullParam(Types.NUMERIC) : new InParam(numextcta, Types.NUMERIC),
      nummovext == null ? new NullParam(Types.NUMERIC) : new InParam(nummovext, Types.NUMERIC),
      clamone == null ? new NullParam(Types.NUMERIC) : new InParam(clamone, Types.NUMERIC),
      status.toString(),
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
    this.updatePrepaidMovement(null, id, null, null, null, status);
  }

  @Override
  public List<PrepaidMovement10> getPrepaidMovements(Long id, Long idMovimientoRef, Long idPrepaidUser, String idTxExterno, PrepaidMovementType tipoMovimiento,
                                                    PrepaidMovementStatus estado, String cuenta, CodigoMoneda clamon, IndicadorNormalCorrector indnorcor, TipoFactura tipofac) throws Exception {

    Object[] params = {
      id != null ? id : new NullParam(Types.BIGINT),
      idMovimientoRef != null ? idMovimientoRef : new NullParam(Types.BIGINT),
      idPrepaidUser != null ? idPrepaidUser : new NullParam(Types.BIGINT),
      idTxExterno != null ? idTxExterno : new NullParam(Types.VARCHAR),
      tipoMovimiento != null ? tipoMovimiento.toString() : new NullParam(Types.VARCHAR),
      estado != null ? estado.toString() : new NullParam(Types.VARCHAR),
      cuenta != null ? cuenta : new NullParam(Types.VARCHAR),
      clamon != null ? clamon.getValue() : new NullParam(Types.NUMERIC),
      indnorcor != null ? indnorcor.getValue() : new NullParam(Types.NUMERIC),
      tipofac != null ? tipofac.getCode() : new NullParam(Types.NUMERIC)
    };

    //se registra un OutParam del tipo cursor (OTHER) y se agrega un rowMapper para transformar el row al objeto necesario
    RowMapper rm = (Map<String, Object> row) -> {
      PrepaidMovement10 p = new PrepaidMovement10();
      p.setId(numberUtils.toLong(row.get("_id")));
      p.setIdMovimientoRef(numberUtils.toLong(row.get("_id_movimiento_ref")));
      p.setIdPrepaidUser(numberUtils.toLong(row.get("_id_usuario")));
      p.setIdTxExterno(String.valueOf(row.get("_id_tx_externo")));
      p.setTipoMovimiento(PrepaidMovementType.valueOfEnum(String.valueOf(row.get("_tipo_movimiento"))));
      p.setMonto(numberUtils.toBigDecimal(row.get("_monto")));
      p.setEstado(PrepaidMovementStatus.valueOfEnum(String.valueOf(row.get("_estado"))));
      //p.setFechaCreacion((Timestamp) row.get("_fecha_creacion"));
      //p.setFechaActualizacion((Timestamp) row.get("_fecha_actualizacion"));
      p.setCodent(String.valueOf(row.get("_codent")));
      p.setCentalta(String.valueOf(row.get("_centalta")));
      p.setCuenta(String.valueOf(row.get("_cuenta")));
      p.setClamon(CodigoMoneda.fromValue(numberUtils.toInteger(row.get("_clamon"))));
      p.setIndnorcor(IndicadorNormalCorrector.fromValue(numberUtils.toInteger(row.get("_indnorcor"))));
      p.setTipofac(TipoFactura.fromValue(numberUtils.toInteger(row.get("_tipofac"))));
      p.setFecfac((Date)row.get("_fecfac"));
      p.setNumreffac(String.valueOf(row.get("_numreffac")));
      p.setPan(String.valueOf(row.get("_pan")));
      p.setClamondiv(numberUtils.toInteger(row.get("_clamondiv")));
      p.setImpdiv(numberUtils.toLong(row.get("_impdiv")));
      p.setImpfac(numberUtils.toBigDecimal(row.get("_impfac")));
      p.setCmbapli(numberUtils.toInteger(row.get("_cmbapli")));
      p.setNumaut(String.valueOf(row.get("_numaut")));
      p.setIndproaje(IndicadorPropiaAjena.fromValue(String.valueOf(row.get("_indproaje"))));
      p.setCodcom(String.valueOf(row.get("_codcom")));
      p.setCodact(numberUtils.toInteger(row.get("_codact")));
      p.setImpliq(numberUtils.toLong(row.get("_impliq")));
      p.setClamonliq(numberUtils.toInteger(row.get("_clamonliq")));
      p.setCodpais(CodigoPais.fromValue(numberUtils.toInteger(row.get("_codpais"))));
      p.setNompob(String.valueOf(row.get("_nompob")));
      p.setNumextcta(numberUtils.toInteger(row.get("_numextcta")));
      p.setNummovext(numberUtils.toInteger(row.get("_nummovext")));
      p.setClamone(numberUtils.toInteger(row.get("_clamone")));
      p.setTipolin(String.valueOf(row.get("_tipolin")));
      p.setLinref(numberUtils.toInteger(row.get("_linref")));
      p.setNumbencta(numberUtils.toInteger(row.get("_numbencta")));
      p.setNumplastico(numberUtils.toLong(row.get("_numplastico")));
      return p;
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_prp_buscar_movimientos_v10", rm, params);
    return (List)resp.get("result");
  }

  @Override
  public PrepaidMovement10 getPrepaidMovementById(Long id) throws Exception {
    if(id == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }
    List<PrepaidMovement10> lst = this.getPrepaidMovements(id, null, null, null, null, null, null, null, null, null);
    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }

  @Override
  public List<PrepaidMovement10> getPrepaidMovementByIdPrepaidUser(Long idPrepaidUser) throws Exception {
    if(idPrepaidUser == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "idPrepaidUser"));
    }
    return this.getPrepaidMovements(null, null, idPrepaidUser, null, null, null, null, null, null, null);
  }

  @Override
  public List<PrepaidMovement10> getPrepaidMovementByIdPrepaidUserAndEstado(Long idPrepaidUser, PrepaidMovementStatus estado) throws Exception {
    if(idPrepaidUser == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "idPrepaidUser"));
    }
    if(estado == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "estado"));
    }
    return this.getPrepaidMovements(null, null, idPrepaidUser, null, null, estado, null, null, null, null);
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
    return this.getPrepaidMovements(null, null, idPrepaidUser, null, tipoMovimiento, null, null, null, null, null);
  }
}
