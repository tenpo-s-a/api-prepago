package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.accounting.ejb.v10.PrepaidAccountingEJBBean10;
import cl.multicaja.accounting.ejb.v10.PrepaidClearingEJBBean10;
import cl.multicaja.accounting.model.v10.AccountingData10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.accounting.model.v10.ClearingData10;
import cl.multicaja.accounting.model.v10.UserAccount;
import cl.multicaja.cdt.ejb.v10.CdtEJBBean10;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.model.ZONEID;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.Utils;
import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.prepaid.async.v10.MailDelegate10;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDelegate10;
import cl.multicaja.prepaid.helpers.freshdesk.model.v10.*;
import cl.multicaja.prepaid.helpers.users.UserClient;
import cl.multicaja.prepaid.helpers.users.model.EmailBody;
import cl.multicaja.prepaid.helpers.users.model.User;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.utils.TemplateUtils;
import cl.multicaja.tecnocom.constants.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.opencsv.CSVWriter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Base64Utils;

import javax.ejb.*;
import javax.inject.Inject;
import javax.persistence.criteria.Order;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;
import static cl.multicaja.core.test.TestBase.numberUtils;
import static cl.multicaja.prepaid.helpers.CalculationsHelper.getParametersUtil;

@Stateless
@LocalBean
@TransactionManagement(value=TransactionManagementType.CONTAINER)
public class PrepaidMovementEJBBean10 extends PrepaidBaseEJBBean10 implements PrepaidMovementEJB10 {

  private static Log log = LogFactory.getLog(PrepaidMovementEJBBean10.class);

  @Inject
  private PrepaidTopupDelegate10 delegate;

  @Inject
  private MailDelegate10 mailDelegate;

  private UserClient userClient;

  @EJB
  private PrepaidUserEJBBean10 prepaidUserEJB10;

  @EJB
  private PrepaidCardEJBBean10 prepaidCardEJB10;

  @EJB
  private CdtEJBBean10 cdtEJB10;

  @EJB
  private PrepaidEJBBean10 prepaidEJBBean10;

  @EJB
  private PrepaidClearingEJBBean10 prepaidClearingEJB10;

  @EJB
  private PrepaidAccountingEJBBean10 prepaidAccountingEJB10;

  @EJB
  private MailPrepaidEJBBean10 mailPrepaidEJBBean10;

  private ResearchMovementInformationFiles researchMovementInformationFiles;

  @Override
  public UserClient getUserClient() {
    if(userClient == null) {
      userClient = UserClient.getInstance();
    }
    return userClient;
  }

  protected String toJson(Object obj) throws JsonProcessingException {
    return new ObjectMapper().writeValueAsString(obj);
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

  public PrepaidClearingEJBBean10 getPrepaidClearingEJB10() {
    return prepaidClearingEJB10;
  }

  public MailPrepaidEJBBean10 getMailPrepaidEJBBean10() {
    return mailPrepaidEJBBean10;
  }

  public void setMailPrepaidEJBBean10(MailPrepaidEJBBean10 mailPrepaidEJBBean10) {
    this.mailPrepaidEJBBean10 = mailPrepaidEJBBean10;
  }

  public void setPrepaidClearingEJB10(PrepaidClearingEJBBean10 prepaidClearingEJB10) {
    this.prepaidClearingEJB10 = prepaidClearingEJB10;
  }

  public PrepaidAccountingEJBBean10 getPrepaidAccountingEJB10() {
    return prepaidAccountingEJB10;
  }

  public void setPrepaidAccountingEJB10(PrepaidAccountingEJBBean10 prepaidAccountingEJB10) {
    this.prepaidAccountingEJB10 = prepaidAccountingEJB10;
  }

  public MailDelegate10 getMailDelegate() {
    return mailDelegate;
  }

  public void setMailDelegate(MailDelegate10 mailDelegate) {
    this.mailDelegate = mailDelegate;
  }

  public void addPrepaidMovement(Map<String, Object> header, List<PrepaidMovement10> data) throws Exception {
    for(PrepaidMovement10 movement : data){
      addPrepaidMovement(header,movement);
    }
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
      new InParam(data.getFechaCreacion(),Types.TIMESTAMP),// Nuevo dato entrada Fecha para movimiento compra
      new OutParam("_r_id", Types.NUMERIC),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_prp_crea_movimiento_v10", params);

    if ("0".equals(resp.get("_error_code"))) {
      data.setId(getNumberUtils().toLong(resp.get("_r_id")));
      return this.getPrepaidMovementById(data.getId());
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

  public void expireNotReconciledMovements(ReconciliationFileType fileType) throws Exception {
    if(fileType == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "fileType"));
    }

    PrepaidMovementType movementType = PrepaidMovementType.TOPUP;
    String statusColumnName = "estado_con_switch";
    IndicadorNormalCorrector indnorcor = IndicadorNormalCorrector.NORMAL;

    switch(fileType) {
      case TECNOCOM_FILE:
        movementType = null;
        indnorcor = null;
        statusColumnName = "estado_con_tecnocom";
        break;
      case SWITCH_TOPUP:
        movementType = PrepaidMovementType.TOPUP;
        indnorcor = IndicadorNormalCorrector.NORMAL;
        statusColumnName = "estado_con_switch";
        break;
      case SWITCH_REVERSED_TOPUP:
        movementType = PrepaidMovementType.TOPUP;
        indnorcor = IndicadorNormalCorrector.CORRECTORA;
        statusColumnName = "estado_con_switch";
        break;
      case SWITCH_WITHDRAW:
        movementType = PrepaidMovementType.WITHDRAW;
        indnorcor = IndicadorNormalCorrector.NORMAL;
        statusColumnName = "estado_con_switch";
        break;
      case SWITCH_REVERSED_WITHDRAW:
        movementType = PrepaidMovementType.WITHDRAW;
        indnorcor = IndicadorNormalCorrector.CORRECTORA;
        statusColumnName = "estado_con_switch";
        break;
      case SWITCH_REJECTED_TOPUP:
        return;
      case SWITCH_REJECTED_WITHDRAW:
        return;
    }

    // Llamar a expirar movimientos con los parametros definidos
    Object[] params = {
      new InParam(statusColumnName, Types.VARCHAR),
      new InParam(fileType.toString(), Types.VARCHAR),
      movementType != null ? new InParam(movementType.toString(), Types.VARCHAR) : new NullParam(Types.VARCHAR),
      indnorcor != null ? new InParam(indnorcor.getValue(), Types.NUMERIC) : new NullParam(Types.NUMERIC),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp =  getDbUtils().execute(getSchema() + ".mc_expire_old_reconciliation_movements_v10", params);

    if (!"0".equals(resp.get("_error_code"))) {
      log.error("expireNotReconciledMovements resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
  }

  public PrepaidMovement10 getPrepaidMovementByNumAutAndPan(String pan, String numaut,MovementOriginType movementOriginType) throws Exception {
    List<PrepaidMovement10> lst = getPrepaidMovements(null, null, null,null,
      null, null, null, null, null, null,
      null, numaut,null, null, movementOriginType,pan);
    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }

  @Override
  public List<PrepaidMovement10> getPrepaidMovements(Long id, Long idMovimientoRef, Long idPrepaidUser, String idTxExterno, PrepaidMovementType tipoMovimiento,
                                                    PrepaidMovementStatus estado, String cuenta, CodigoMoneda clamon, IndicadorNormalCorrector indnorcor, TipoFactura tipofac, Date fecfac, String numaut) throws Exception {
    return this.getPrepaidMovements(id, idMovimientoRef, idPrepaidUser, idTxExterno, tipoMovimiento, estado, cuenta,
      clamon, indnorcor, tipofac, fecfac, numaut, null, null, null,null);
  }

  @Override
  public List<PrepaidMovement10> getPrepaidMovements(Long id, Long idMovimientoRef, Long idPrepaidUser, String idTxExterno, PrepaidMovementType tipoMovimiento,
                                                     PrepaidMovementStatus estado, String cuenta, CodigoMoneda clamon, IndicadorNormalCorrector indnorcor, TipoFactura tipofac, Date fecfac, String numaut,
                                                     ReconciliationStatusType estadoConSwitch, ReconciliationStatusType estadoConTecnocom, MovementOriginType origen, String pan) throws Exception {

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
      pan != null ? pan : new NullParam(Types.VARCHAR)
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
      p.setImpdiv(getNumberUtils().toBigDecimal(row.get("_impdiv")));
      p.setImpfac(getNumberUtils().toBigDecimal(row.get("_impfac")));
      p.setCmbapli(getNumberUtils().toInteger(row.get("_cmbapli")));
      p.setNumaut(String.valueOf(row.get("_numaut")));
      p.setIndproaje(IndicadorPropiaAjena.fromValue(String.valueOf(row.get("_indproaje"))));
      p.setCodcom(String.valueOf(row.get("_codcom")));
      p.setCodact(getNumberUtils().toInteger(row.get("_codact")));
      p.setImpliq(getNumberUtils().toBigDecimal(row.get("_impliq")));
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
  public PrepaidMovement10 getPrepaidMovementForAut(Long idPrepaidUse,TipoFactura tipoFactura, String numaut)throws Exception{
    if(idPrepaidUse == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "idPrepaidUse"));
    }
    if(numaut == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "numaut"));
    }
    List<PrepaidMovement10> lst = this.getPrepaidMovements(null, null, idPrepaidUse, null, null, null, null, null, null,tipoFactura , null, numaut);
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
    log.info("Mov to Reconciliation: "+mov);

    /**
     * ID 1 - Movimiento (Carga, Retiro o Reversa)
     *  - Existe en la tabla de movimientos
     *  - Status -> PROCESS_OK
     *  - Existe en archivo Tecnocom
     *  - Existe en archivo Switch
     *
     * Se guarda en tabla de movimientos conciliados con status RECONCILIED
     */
    if(ReconciliationStatusType.RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.RECONCILED.equals(mov.getConSwitch())&& PrepaidMovementStatus.PROCESS_OK.equals(mov.getEstado())) {
      log.debug("XLS ID 1");
      createMovementConciliate(null,mov.getId(), ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);

      // Si el moviento es una Carga o Retiro POS, se actualiza informacion en accounting y clearing
      if(TipoFactura.CARGA_TRANSFERENCIA.equals(mov.getTipofac()) ||
        TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA.equals(mov.getTipofac()) ||
        TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA.equals(mov.getTipofac())) {

        // se actualiza informacion en accounting y clearing
        this.updateAccountingStatusReconciliationDateAndClearingStatus(mov.getId(), AccountingStatusType.OK, AccountingStatusType.PENDING);
      }
    }
    /**
     * ID 2 - Movimiento (Carga, Retiro o Reversa)
     *  - Existe en la tabla de movimientos
     *  - Status -> PROCESS_OK
     *  - Existe en archivo Tecnocom
     *  - NO existe en archivo Switch
     */
    else if(ReconciliationStatusType.RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConSwitch())&& PrepaidMovementStatus.PROCESS_OK.equals(mov.getEstado())) {
      log.info("XLS ID 2");
      log.info("Get Prepaid by ID: "+mov.getId());
      PrepaidMovement10 movFull = getPrepaidMovementById(mov.getId());
      log.info(movFull);
      /**
       * Carga
       */
      if(PrepaidMovementType.TOPUP.equals(mov.getTipoMovimiento())){
        /**
         * Si es una carga - Se guarda en tabla de movimientos conciliados con status COUNTER_MOVEMENT y se realiza la reversa del movimiento
         */
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

          // se actualiza informacion en accounting y clearing
          this.updateAccountingStatusReconciliationDateAndClearingStatus(mov.getId(), AccountingStatusType.NOT_OK, AccountingStatusType.NOT_SEND);
        }
        /**
         * Si es una reversa de carga - Se guarda en tabla de movimientos conciliados con status NEED_VERIFICATION y se agrega en la tabla de movimientos a investigar
         */
        else {
          //TODO: Esta OK este Research?
          /*createMovementResearch(
            null,String.format("idMov=%s",mov.getId()), ReconciliationOriginType.MOTOR,
            ResearchMovementFileStatusType.NOT_FILE_NAME.getValue(),movFull.getFechaCreacion(),
            ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID,
            ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH,movFull.getId());*/

          List<ResearchMovementInformationFiles> researchMovementInformationFilesList = new ArrayList<>();
          researchMovementInformationFiles = new ResearchMovementInformationFiles();
          //researchMovementInformationFiles.setIdArchivo();
          //researchMovementInformationFiles.setIdEnArchivo();
          //researchMovementInformationFiles.setNombreArchivo();
          //researchMovementInformationFiles.setTipoArchivo();
          researchMovementInformationFilesList.add(researchMovementInformationFiles);

          createResearchMovement(
            null,
            toJson(researchMovementInformationFilesList),
            ReconciliationOriginType.MOTOR.name(),
            movFull.getFechaCreacion(),
            ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID.getValue(),
            ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH.getValue(),
            mov.getId(),PrepaidMovementType.TOPUP.name(),
            ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue()
          );

          createMovementConciliate(null,mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
        }
      }
      /**
       * Retiro
       */
      else {
        /**
         * Si es un retiro - Se guarda en tabla de movimientos conciliados con status COUNTER_MOVEMENT y se realiza la reversa del movimiento
         */
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

          // se actualiza informacion en accounting y clearing
          this.updateAccountingStatusReconciliationDateAndClearingStatus(mov.getId(), AccountingStatusType.NOT_OK, AccountingStatusType.NOT_SEND);
        }
        /**
         * Si es una reversa de retiro - Se guarda en tabla de movimientos conciliados con status NEED_VERIFICATION y se agrega en la tabla de movimientos a investigar
         */
        else {
          //TODO: Esta OK este Research?
          /*createMovementResearch(
            null,String.format("idMov=%s",mov.getId()), ReconciliationOriginType.MOTOR,
            ResearchMovementFileStatusType.NOT_FILE_NAME.getValue(),mov.getFechaCreacion(),
            ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID,
            ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH,mov.getId());*/

          List<ResearchMovementInformationFiles> researchMovementInformationFilesList = new ArrayList<>();
          researchMovementInformationFiles = new ResearchMovementInformationFiles();
          //researchMovementInformationFiles.setIdArchivo();
          //researchMovementInformationFiles.setIdEnArchivo();
          //researchMovementInformationFiles.setNombreArchivo();
          //researchMovementInformationFiles.setTipoArchivo();
          researchMovementInformationFilesList.add(researchMovementInformationFiles);
          createResearchMovement(
            null,
            toJson(researchMovementInformationFilesList),
            ReconciliationOriginType.MOTOR.name(),
            mov.getFechaCreacion(),
            ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID.getValue(),
            ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH.getValue(),
            mov.getId(),
            PrepaidMovementType.WITHDRAW.name(),
            ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue()
          );

          createMovementConciliate(null,mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
        }
      }
    }
    /**
     * ID 3 - Movimiento (Carga, Retiro o Reversa)
     *  - Existe en la tabla de movimientos
     *  - Status -> PROCESS_OK
     *  - NO existe en archivo Tecnocom
     *  - Existe en archivo Switch
     *
     * Se guarda en tabla de movimientos conciliados con status NEED_VERIFICATION y se agrega en la tabla de movimientos a investigar
     */
    else if(ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.RECONCILED.equals(mov.getConSwitch())&& PrepaidMovementStatus.PROCESS_OK.equals(mov.getEstado())){
      log.debug("XLS ID 3");

      /*createMovementResearch(null,String.format("idMov=%s",mov.getId()), ReconciliationOriginType.MOTOR,
        ResearchMovementFileStatusType.NOT_FILE_NAME.getValue(),mov.getFechaCreacion(),
        ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID,
        ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_PROCESOR,mov.getId());*/

      List<ResearchMovementInformationFiles> researchMovementInformationFilesList = new ArrayList<>();
      researchMovementInformationFiles = new ResearchMovementInformationFiles();
      //researchMovementInformationFiles.setIdArchivo();
      //researchMovementInformationFiles.setIdEnArchivo();
      //researchMovementInformationFiles.setNombreArchivo();
      //researchMovementInformationFiles.setTipoArchivo();
      researchMovementInformationFilesList.add(researchMovementInformationFiles);
      createResearchMovement(
        null,
        toJson(researchMovementInformationFilesList),
        ReconciliationOriginType.MOTOR.name(),
        mov.getFechaCreacion(),
        ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID.getValue(),
        ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_PROCESOR.getValue(),
        mov.getId(),PrepaidMovementType.TOPUP.name(),
        ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue()
      );

      createMovementConciliate(null,mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
      //TODO: actualizar fecha de conciliacion y status -> RESEARCH en accounting
      //TODO: actualizar status -> RESEARCH en clearing
    }
    /**
     * ID 4 - Movimiento (Carga, Retiro o Reversa)
     *  - Existe en la tabla de movimientos
     *  - Status -> PROCESS_OK
     *  - NO existe en archivo Tecnocom
     *  - NO existe en archivo Switch
     *
     * Se guarda en tabla de movimientos conciliados con status NEED_VERIFICATION y se agrega en la tabla de movimientos a investigar
     */
    else if(ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConSwitch())&& PrepaidMovementStatus.PROCESS_OK.equals(mov.getEstado())){
      log.debug("XLS ID 4");

      //TODO: Esta OK este Research
      /*createMovementResearch(null,String.format("idMov=%s",mov.getId()), ReconciliationOriginType.MOTOR,
        ResearchMovementFileStatusType.NOT_FILE_NAME.getValue(),mov.getFechaCreacion(),
        ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID,
        ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR,mov.getId());*/

      List<ResearchMovementInformationFiles> researchMovementInformationFilesList = new ArrayList<>();
      researchMovementInformationFiles = new ResearchMovementInformationFiles();
      //researchMovementInformationFiles.setIdArchivo();
      //researchMovementInformationFiles.setIdEnArchivo();
      //researchMovementInformationFiles.setNombreArchivo();
      //researchMovementInformationFiles.setTipoArchivo();
      researchMovementInformationFilesList.add(researchMovementInformationFiles);
      createResearchMovement(
        null,
        toJson(researchMovementInformationFilesList),
        ReconciliationOriginType.MOTOR.name(),
        mov.getFechaCreacion(),
        ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID.getValue(),
        ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR.getValue(),
        mov.getId(),
        PrepaidMovementType.TOPUP.name(),
        ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue()
      );

      createMovementConciliate(null,mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
      //TODO: actualizar fecha de conciliacion y status -> RESEARCH en accounting
      //TODO: actualizar status -> RESEARCH en clearing
    }
    /**
     * ID 5 - Movimiento (Carga o Reversa)
     *  - Existe en la tabla de movimientos
     *  - Status -> ERROR_TECNOCOM_REINTENTABLE, ERROR_TIMEOUT_CONEXION, ERROR_TIMEOUT_RESPONSE
     *  - Existe en archivo Tecnocom
     *  - Existe en archivo Switch
     *
     * Se guarda en tabla de movimientos conciliados con status RECONCILED, se actualiza el status del movimiento a PROCESS_OK, se actualzia en status de negocio a CONFIRMED
     */
    else if(ReconciliationStatusType.RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.RECONCILED.equals(mov.getConSwitch())&&
      ( PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE.equals(mov.getEstado()) ||
        PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION.equals(mov.getEstado()) ||
        PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE.equals(mov.getEstado())
      ) && mov.getTipoMovimiento().equals(PrepaidMovementType.TOPUP)
    ){
      log.debug("XLS ID 5");

      //TODO: Esta OK este Research?
      /*createMovementResearch(null,String.format("idMov=%s",mov.getId()), ReconciliationOriginType.MOTOR,
        ResearchMovementFileStatusType.NOT_FILE_NAME.getValue(),mov.getFechaCreacion(),
        ResearchMovementResponsibleStatusType.OTI_PREPAID,ResearchMovementDescriptionType.ERROR_STATUS_IN_DB,mov.getId());*/

      List<ResearchMovementInformationFiles> researchMovementInformationFilesList = new ArrayList<>();
      researchMovementInformationFiles = new ResearchMovementInformationFiles();
      //researchMovementInformationFiles.setIdArchivo();
      //researchMovementInformationFiles.setIdEnArchivo();
      //researchMovementInformationFiles.setNombreArchivo();
      //researchMovementInformationFiles.setTipoArchivo();
      researchMovementInformationFilesList.add(researchMovementInformationFiles);
      createResearchMovement(
        null,
        toJson(researchMovementInformationFilesList),
        ReconciliationOriginType.MOTOR.name(),
        mov.getFechaCreacion(),
        ResearchMovementResponsibleStatusType.OTI_PREPAID.getValue(),
        ResearchMovementDescriptionType.ERROR_STATUS_IN_DB.getValue(),
        mov.getId(),
        PrepaidMovementType.TOPUP.name(),
        ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue()
      );

      createMovementConciliate(null,mov.getId(), ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);
      updatePrepaidMovementStatus(null,mov.getId(),PrepaidMovementStatus.PROCESS_OK);
      updatePrepaidBusinessStatus(null, mov.getId(), BusinessStatusType.CONFIRMED);
      if(IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor())) {

        // se actualiza informacion en accounting y clearing
        this.updateAccountingStatusReconciliationDateAndClearingStatus(mov.getId(), AccountingStatusType.OK, AccountingStatusType.PENDING);

      } else {
        //TODO: si el movimiento es una reversa, no deberia actualizar el status de negocio del movimiento original a REVERSED?
      }
    }
    /**
     * ID 6 - Movimiento (Carga o Reversa)
     *  - Existe en la tabla de movimientos
     *  - Status -> ERROR_TECNOCOM_REINTENTABLE, ERROR_TIMEOUT_CONEXION, ERROR_TIMEOUT_RESPONSE
     *  - Existe en archivo Tecnocom
     *  - No existe en archivo Switch
     */
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

      /**
       * Si es una carga -  Se guarda en tabla de movimientos conciliados con status COUNTER_MOVEMENT y se realiza la reversa del movimiento
       */
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

        // se actualiza informacion en accounting y clearing
        this.updateAccountingStatusReconciliationDateAndClearingStatus(mov.getId(), AccountingStatusType.NOT_OK, AccountingStatusType.NOT_SEND);
      }
      /**
       * Si es una reversa de carga - Se guarda en tabla de movimientos conciliados con status NEED_VERIFICATION y se agrega en la tabla de movimientos a investigar
       */
      else {

        //TODO: Esta OK este Research?
        /*createMovementResearch(null, String.format("idMov=%s", mov.getId()), ReconciliationOriginType.MOTOR,
          ResearchMovementFileStatusType.NOT_FILE_NAME.getValue(),mov.getFechaCreacion(),
          ResearchMovementResponsibleStatusType.RECONCILIATION_MULTICAJA,ResearchMovementDescriptionType.DESCRIPTION_UNDEFINED,mov.getId());*/

        List<ResearchMovementInformationFiles> researchMovementInformationFilesList = new ArrayList<>();
        researchMovementInformationFiles = new ResearchMovementInformationFiles();
        //researchMovementInformationFiles.setIdArchivo();
        //researchMovementInformationFiles.setIdEnArchivo();
        //researchMovementInformationFiles.setNombreArchivo();
        //researchMovementInformationFiles.setTipoArchivo();
        researchMovementInformationFilesList.add(researchMovementInformationFiles);
        createResearchMovement(
          null,
          toJson(researchMovementInformationFilesList),
          ReconciliationOriginType.MOTOR.name(),
          mov.getFechaCreacion(),
          ResearchMovementResponsibleStatusType.RECONCILIATION_MULTICAJA.getValue(),
          ResearchMovementDescriptionType.DESCRIPTION_UNDEFINED.getValue(),
          mov.getId(),
          PrepaidMovementType.TOPUP.name(),
          ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue()
        );

        createMovementConciliate(null,mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
      }
    }
    /**
     * ID 7 - Movimiento (Retiro)
     *  - Existe en la tabla de movimientos
     *  - Status -> ERROR_TIMEOUT_RESPONSE
     *  - Existe en archivo Tecnocom
     *  - No existe en archivo Switch
     */
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

          // se actualiza informacion en accounting y clearing
          this.updateAccountingStatusReconciliationDateAndClearingStatus(mov.getId(), AccountingStatusType.OK, AccountingStatusType.PENDING);

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

        // se actualiza informacion en accounting y clearing
        this.updateAccountingStatusReconciliationDateAndClearingStatus(mov.getId(), AccountingStatusType.NOT_OK, AccountingStatusType.NOT_SEND);
      }
    }
    /**
     * ID 8 - Movimiento (Retiro)
     *  - Existe en la tabla de movimientos
     *  - Status -> ERROR_TECNOCOM_REINTENTABLE, ERROR_TIMEOUT_CONEXION, ERROR_TIMEOUT_RESPONSE
     *  - Existe en archivo Tecnocom
     *  - Existe en archivo Switch
     *
     *  Se guarda en tabla de movimientos conciliados con status NEED_VERIFICATION y se agrega en la tabla de movimientos a investigar
     */
    else if(ReconciliationStatusType.RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.RECONCILED.equals(mov.getConSwitch())&&  ( PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE.equals(mov.getEstado()) ||
      PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION.equals(mov.getEstado()) ||
      PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE.equals(mov.getEstado())
    ) && PrepaidMovementType.WITHDRAW.equals(mov.getTipoMovimiento())){
      log.debug("XLS ID 8");

      //TODO: Esta OK este Research?
      /*createMovementResearch(null,String.format("idMov=%s",mov.getId()), ReconciliationOriginType.MOTOR,
        ResearchMovementFileStatusType.NOT_FILE_NAME.getValue(),mov.getFechaCreacion(),
        ResearchMovementResponsibleStatusType.OTI_PREPAID,ResearchMovementDescriptionType.ERROR_STATUS_IN_DB,mov.getId());*/

      List<ResearchMovementInformationFiles> researchMovementInformationFilesList = new ArrayList<>();
      researchMovementInformationFiles = new ResearchMovementInformationFiles();
      //researchMovementInformationFiles.setIdArchivo();
      //researchMovementInformationFiles.setIdEnArchivo();
      //researchMovementInformationFiles.setNombreArchivo();
      //researchMovementInformationFiles.setTipoArchivo();
      researchMovementInformationFilesList.add(researchMovementInformationFiles);
      createResearchMovement(
        null,
        toJson(researchMovementInformationFilesList),
        ReconciliationOriginType.MOTOR.name(),
        mov.getFechaCreacion(),
        ResearchMovementResponsibleStatusType.OTI_PREPAID.getValue(),
        ResearchMovementDescriptionType.ERROR_STATUS_IN_DB.getValue(),
        mov.getId(),
        PrepaidMovementType.WITHDRAW.name(),
        ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue()
      );

      createMovementConciliate(null,mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
      //TODO: actualizar fecha de conciliacion y status -> RESEARCH en accounting
      //TODO: actualizar status -> RESEARCH en clearing
    }
    /**
     * ID 9 - Movimiento (Carga o Reversa)
     *  - Existe en la tabla de movimientos
     *  - Status -> ERROR_TECNOCOM_REINTENTABLE, ERROR_TIMEOUT_CONEXION, ERROR_TIMEOUT_RESPONSE
     *  - NO existe en archivo Tecnocom
     *  - Existe en archivo Switch
     */
    else if(ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.RECONCILED.equals(mov.getConSwitch())&&  ( PrepaidMovementStatus.ERROR_TECNOCOM_REINTENTABLE.equals(mov.getEstado()) ||
      PrepaidMovementStatus.ERROR_TIMEOUT_CONEXION.equals(mov.getEstado()) ||
      PrepaidMovementStatus.ERROR_TIMEOUT_RESPONSE.equals(mov.getEstado())
    ) && PrepaidMovementType.TOPUP.equals(mov.getTipoMovimiento())) {
      log.debug("XLS ID 9");
      PrepaidMovement10 movFull = getPrepaidMovementById(mov.getId());
      /**
       * Si es una carga - Se guarda en tabla de movimientos conciliados con status COUNTER_MOVEMENT y se realiza la reversa del movimiento
       */
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
        // Referencia a movimiento original
        newPrepaidTopup10.setTransactionId(String.format("MC_%s",movFull.getIdTxExterno()));
        // Se envia movimiento a reversar
        getPrepaidEJBBean10().topupUserBalance(null,newPrepaidTopup10,false);
        // Se agrega a movimiento conciliado para que no vuelva a ser enviado.
        createMovementConciliate(null,mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.COUNTER_MOVEMENT);

        //TODO: Esta OK este Research?
        /*createMovementResearch(null, String.format("idMov=%s", mov.getId()), ReconciliationOriginType.MOTOR,
          ResearchMovementFileStatusType.NOT_FILE_NAME.getValue(),mov.getFechaCreacion(),
          ResearchMovementResponsibleStatusType.RECONCILIATION_MULTICAJA,ResearchMovementDescriptionType.MOVEMENT_REJECTED_IN_AUTHORIZATION,mov.getId());*/

        List<ResearchMovementInformationFiles> researchMovementInformationFilesList = new ArrayList<>();
        researchMovementInformationFiles = new ResearchMovementInformationFiles();
        //researchMovementInformationFiles.setIdArchivo();
        //researchMovementInformationFiles.setIdEnArchivo();
        //researchMovementInformationFiles.setNombreArchivo();
        //researchMovementInformationFiles.setTipoArchivo();
        researchMovementInformationFilesList.add(researchMovementInformationFiles);
        createResearchMovement(
          null,
          toJson(researchMovementInformationFilesList),
          ReconciliationOriginType.MOTOR.name(),
          mov.getFechaCreacion(),
          ResearchMovementResponsibleStatusType.RECONCILIATION_MULTICAJA.getValue(),
          ResearchMovementDescriptionType.MOVEMENT_REJECTED_IN_AUTHORIZATION.getValue(),
          mov.getId(),
          PrepaidMovementType.TOPUP.name(),
          ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue()
        );

        // se actualiza informacion en accounting y clearing
        this.updateAccountingStatusReconciliationDateAndClearingStatus(mov.getId(), AccountingStatusType.NOT_OK, AccountingStatusType.NOT_SEND);
      }
      /**
       * Si es una reversa -
       */
      else {
        //TODO: que se debe hacer en los otros casos?
      }
    }
    /**
     * ID ? - Movimiento (Carga o Reversa)
     *  - Existe en la tabla de movimientos
     *  - Status -> REJECTED
     *  - NO existe en archivo Tecnocom
     *  - Existe en archivo Switch
     */
    else if(ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.RECONCILED.equals(mov.getConSwitch()) && (
      PrepaidMovementStatus.REJECTED.equals(mov.getEstado())
    ) && PrepaidMovementType.TOPUP.equals(mov.getTipoMovimiento())) {
      log.debug("XLS ID 9");
      System.out.println("XLS ID 9");
      PrepaidMovement10 movFull = getPrepaidMovementById(mov.getId());

      /**
       * Si es una carga - Se inicia proceso de devolucion
       */
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

        // Enviar movimiento a REFUND
        createMovementConciliate(null, movFull.getId(), ReconciliationActionType.REFUND, ReconciliationStatusType.TO_REFUND);
        updatePrepaidBusinessStatus(null, movFull.getId(), BusinessStatusType.TO_REFUND);

        // se actualiza informacion en accounting y clearing
        this.updateAccountingStatusReconciliationDateAndClearingStatus(mov.getId(), AccountingStatusType.NOT_OK, AccountingStatusType.NOT_SEND);

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
      }
      /**
       * Si es una reversa de carga -
       */
      else {
        //TODO: que se hace en los otros casos?
        //TODO: Insertar nuevamente en tecnocom, no tiene movimiento a investigar, mañana
      }
    }
    /**
     * ID 19 a 24 - Movimiento (Carga, Retiro o Reversa)
     *  - Existe en la tabla de movimientos
     *  - Status -> PENDING o IN_PROCESS
     *  - NO existe en archivo Tecnocom
     *  - Existe en archivo Switch
     *
     *  Se guarda en tabla de movimientos conciliados con status NEED_VERIFICATION y se agrega en la tabla de movimientos a investigar
     */
    //Movimientos que esten en estado pendiente o en proceso y vengan en alguno de los archivos Caso 19 al 24
    else if (PrepaidMovementStatus.PENDING.equals(mov.getEstado())||PrepaidMovementStatus.IN_PROCESS.equals(mov.getEstado())){
      log.debug("Movimiento Pendiente o En proceso");

      //TODO: Esta OK este Research?
      /*createMovementResearch(null,String.format("idMov=%s",mov.getId()), ReconciliationOriginType.MOTOR,
        ResearchMovementFileStatusType.NOT_FILE_NAME.getValue(),mov.getFechaCreacion(),
        ResearchMovementResponsibleStatusType.OTI_PREPAID,ResearchMovementDescriptionType.ERROR_STATUS_IN_DB,mov.getId());*/

      List<ResearchMovementInformationFiles> researchMovementInformationFilesList = new ArrayList<>();
      researchMovementInformationFiles = new ResearchMovementInformationFiles();
      //researchMovementInformationFiles.setIdArchivo();
      //researchMovementInformationFiles.setIdEnArchivo();
      //researchMovementInformationFiles.setNombreArchivo();
      //researchMovementInformationFiles.setTipoArchivo();
      researchMovementInformationFilesList.add(researchMovementInformationFiles);
      createResearchMovement(
        null,
        toJson(researchMovementInformationFilesList),
        ReconciliationOriginType.MOTOR.name(),
        mov.getFechaCreacion(),
        ResearchMovementResponsibleStatusType.OTI_PREPAID.getValue(),
        ResearchMovementDescriptionType.ERROR_STATUS_IN_DB.getValue(),
        mov.getId(),
        PrepaidMovementType.TOPUP.name(),
        ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue()
      );


      createMovementConciliate(null,mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
      // Si el moviento es una Carga o Retiro POS, se actualiza informacion en accounting y clearing
      if(TipoFactura.CARGA_TRANSFERENCIA.equals(mov.getTipofac()) ||
        TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA.equals(mov.getTipofac()) ||
        TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA.equals(mov.getTipofac())) {
        //TODO: actualizar fecha de conciliacion y status -> RESEARCH en accounting
        //TODO: actualizar status -> RESEARCH en clearing
      }
    }
    /**
     * ID ? - Movimiento (Carga, Retiro o Reversa)
     *  No cae en ninguno de los casos anteriores
     *  Se guarda en tabla de movimientos conciliados con status NO_CASE y se agrega en la tabla de movimientos a investigar
     */
    else {
        log.error("No cae en ningun caso: "+mov);

        //TODO: Esta OK este Research?
        /*createMovementResearch(null,String.format("idMov=%s",mov.getId()), ReconciliationOriginType.MOTOR,
          ResearchMovementFileStatusType.NOT_FILE_NAME.getValue(),mov.getFechaCreacion(),
          ResearchMovementResponsibleStatusType.STATUS_UNDEFINED,ResearchMovementDescriptionType.DESCRIPTION_UNDEFINED,mov.getId());*/

      List<ResearchMovementInformationFiles> researchMovementInformationFilesList = new ArrayList<>();
      researchMovementInformationFiles = new ResearchMovementInformationFiles();
      //researchMovementInformationFiles.setIdArchivo();
      //researchMovementInformationFiles.setIdEnArchivo();
      //researchMovementInformationFiles.setNombreArchivo();
      //researchMovementInformationFiles.setTipoArchivo();
      researchMovementInformationFilesList.add(researchMovementInformationFiles);
      createResearchMovement(
        null,
        toJson(researchMovementInformationFilesList),
        ReconciliationOriginType.MOTOR.name(),
        mov.getFechaCreacion(),
        ResearchMovementResponsibleStatusType.STATUS_UNDEFINED.getValue(),
        ResearchMovementDescriptionType.DESCRIPTION_UNDEFINED.getValue(),
        mov.getId(),
        PrepaidMovementType.TOPUP.name(),
        ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue()
      );

        createMovementConciliate(null,mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NO_CASE);
      // Si el moviento es una Carga o Retiro POS, se actualiza informacion en accounting y clearing
      if(TipoFactura.CARGA_TRANSFERENCIA.equals(mov.getTipofac()) ||
        TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA.equals(mov.getTipofac()) ||
        TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA.equals(mov.getTipofac())) {
        //TODO: actualizar fecha de conciliacion y status -> RESEARCH en accounting
        //TODO: actualizar status -> RESEARCH en clearing
      }
    }
  }

  public void updateAccountingStatusReconciliationDateAndClearingStatus(Long idTrx, AccountingStatusType accountingStatus, AccountingStatusType clearingStatus) throws Exception {

    // Obtengo el movimiento correspondiente en Accounting
    AccountingData10 accounting = getPrepaidAccountingEJB10().searchAccountingByIdTrx(null, idTrx);

    Instant instant = Instant.now();
    ZoneId z = ZoneId.of( "UTC" );
    ZonedDateTime nowUtc = instant.atZone(z);

    LocalDateTime localDateTime = accounting.getConciliationDate().toLocalDateTime();
    ZonedDateTime reconciliationDateUtc = localDateTime.atZone(ZoneOffset.UTC);

    String date = null;
    if(nowUtc.isBefore(reconciliationDateUtc)) {
      date = nowUtc.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    } else {
      date = reconciliationDateUtc.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    getPrepaidAccountingEJB10().updateAccountingStatusAndConciliationDate(null, accounting.getId(), accountingStatus,  date);

    // Si el movimiento en accounting todavia esta PENDING y se actualiza el accountingStatus a NOT_OK, el movimiento se deja en NOT_SEND para no ser enviado en el archivo de contabilidad
    if(AccountingStatusType.PENDING.equals(accounting.getStatus()) && AccountingStatusType.NOT_OK.equals(accountingStatus)) {
      getPrepaidAccountingEJB10().updateAccountingData(null, accounting.getId(), null, AccountingStatusType.NOT_SEND);
    }

    // Obtengo el movimiento correspondiente en Clearing
    ClearingData10 clearing = getPrepaidClearingEJB10().searchClearingDataByAccountingId(null, accounting.getId());

    // Solo actualiza el status en clearing si es INITIAL
    if(AccountingStatusType.INITIAL.equals(clearing.getStatus())){
      getPrepaidClearingEJB10().updateClearingData(null, clearing.getId(), clearingStatus);
    }
  }

  public void clearingResolution() throws Exception {
    List<ClearingData10> clearingData10s = getPrepaidClearingEJB10().getWebWithdrawForReconciliation(null);

    for(ClearingData10 clearingData10 : clearingData10s) {
      try {
        this.processClearingResolution(clearingData10);
      } catch (Exception e) {
        log.error("Error al procesar la resolucion del movimiento [" + (clearingData10 != null ? clearingData10.getIdTransaction() : null) + "]");
      }
    }
  }

  public void processClearingResolution(ClearingData10 clearingData10) throws Exception {
    if(clearingData10 == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "clearingData10"));
    }

    PrepaidMovement10 prepaidMovement10 = this.getPrepaidMovementById(clearingData10.getIdTransaction());

    // Solo se procesan los retiros web
    if(!PrepaidMovementType.WITHDRAW.equals(prepaidMovement10.getTipoMovimiento()) ||
       !NewPrepaidWithdraw10.WEB_MERCHANT_CODE.equals(prepaidMovement10.getCodcom())) {
      throw new ValidationException(PARAMETRO_NO_PERMITIDO_$VALUE).setData(new KeyValue("value", "Movimiento no es retiro web"));
    }

    // Regla: los movimientos que no vinieron en el archivo, se concilian y se mandan a investigar
    if(AccountingStatusType.NOT_IN_FILE.equals(clearingData10.getStatus())) {

      //TODO: Esta OK este Research?
      /*
        String idToResearch = String.format("idMov=%d", prepaidMovement10.getId());
        createMovementResearch(
        null, idToResearch, ReconciliationOriginType.CLEARING_RESOLUTION,
        ResearchMovementFileStatusType.NOT_FILE_NAME.getValue(),
        prepaidMovement10.getFechaCreacion(),ResearchMovementResponsibleStatusType.RECONCILIATION_MULTICAJA,
        ResearchMovementDescriptionType.MOVEMENT_NOT_FOUND_IN_FILE,prepaidMovement10.getId());*/

      List<ResearchMovementInformationFiles> researchMovementInformationFilesList = new ArrayList<>();
      researchMovementInformationFiles = new ResearchMovementInformationFiles();
      //researchMovementInformationFiles.setIdArchivo();
      //researchMovementInformationFiles.setIdEnArchivo();
      //researchMovementInformationFiles.setNombreArchivo(ResearchMovementFileStatusType.NOT_FILE_NAME.getValue());
      //researchMovementInformationFiles.setTipoArchivo();
      researchMovementInformationFilesList.add(researchMovementInformationFiles);
      createResearchMovement(
        null,
        toJson(researchMovementInformationFilesList),
        ReconciliationOriginType.CLEARING_RESOLUTION.name(),
        prepaidMovement10.getFechaCreacion(),
        ResearchMovementResponsibleStatusType.RECONCILIATION_MULTICAJA.getValue(),
        ResearchMovementDescriptionType.MOVEMENT_NOT_FOUND_IN_FILE.getValue(),
        prepaidMovement10.getId(),
        PrepaidMovementType.PURCHASE.name(),
        ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue()
      );

      // Se agrega a movimiento conciliado para que no vuelva a ser enviado.
      createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
      return;
    }

    // Regla: los movimientos que vengan con datos incorrectos, se concilian y se mandan a investigar
    if(AccountingStatusType.INVALID_INFORMATION.equals(clearingData10.getStatus())) {

      //TODO: Esta OK este Research?
      /*
        String idToResearch = String.format("idMov=%d", prepaidMovement10.getId());
        createMovementResearch(
        null, idToResearch, ReconciliationOriginType.CLEARING_RESOLUTION,
        ResearchMovementFileStatusType.NOT_FILE_NAME.getValue(),
        prepaidMovement10.getFechaCreacion(), ResearchMovementResponsibleStatusType.RECONCILIATION_MULTICAJA,
        ResearchMovementDescriptionType.ERROR_INFO,prepaidMovement10.getId());*/

      List<ResearchMovementInformationFiles> researchMovementInformationFilesList = new ArrayList<>();
      researchMovementInformationFiles = new ResearchMovementInformationFiles();
      //researchMovementInformationFiles.setIdArchivo();
      //researchMovementInformationFiles.setIdEnArchivo();
      //researchMovementInformationFiles.setNombreArchivo();
      //researchMovementInformationFiles.setTipoArchivo();
      researchMovementInformationFilesList.add(researchMovementInformationFiles);
      createResearchMovement(
        null,
        toJson(researchMovementInformationFilesList),
        ReconciliationOriginType.CLEARING_RESOLUTION.name(),
        prepaidMovement10.getFechaCreacion(),
        ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID.getValue(),
        ResearchMovementDescriptionType.ERROR_INFO.getValue(),
        prepaidMovement10.getId(),
        PrepaidMovementType.TOPUP.name(),
        ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue()
      );

      // Se agrega a movimiento conciliado para que no vuelva a ser enviado.
      createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
      return;
    }

    // Regla: los movimientos que no esten confirmados en nuestra BD -> Investigar
    if(!PrepaidMovementStatus.PROCESS_OK.equals(prepaidMovement10.getEstado())) {


      //TODO: Esta OK este Research?
      /*
        String idToResearch = String.format("idMov=%d", prepaidMovement10.getId());
        createMovementResearch(
        null, idToResearch, ReconciliationOriginType.CLEARING_RESOLUTION,
        ResearchMovementFileStatusType.NOT_FILE_NAME.getValue(),
        prepaidMovement10.getFechaCreacion(), ResearchMovementResponsibleStatusType.OTI_PREPAID,
        ResearchMovementDescriptionType.ERROR_STATUS_IN_DB,prepaidMovement10.getId());*/

      List<ResearchMovementInformationFiles> researchMovementInformationFilesList = new ArrayList<>();
      researchMovementInformationFiles = new ResearchMovementInformationFiles();
      //researchMovementInformationFiles.setIdArchivo();
      //researchMovementInformationFiles.setIdEnArchivo();
      //researchMovementInformationFiles.setNombreArchivo();
      //researchMovementInformationFiles.setTipoArchivo();
      researchMovementInformationFilesList.add(researchMovementInformationFiles);
      createResearchMovement(
        null,
        toJson(researchMovementInformationFilesList),
        ReconciliationOriginType.MOTOR.name(),
        prepaidMovement10.getFechaCreacion(),
        ResearchMovementResponsibleStatusType.OTI_PREPAID.getValue(),
        ResearchMovementDescriptionType.ERROR_STATUS_IN_DB.getValue(),
        prepaidMovement10.getId(),
        PrepaidMovementType.TOPUP.name(),
        ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue()
      );

      // Se agrega a movimiento conciliado para que no vuelva a ser enviado.
      createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
      return;
    }

    // Aplicar excel de decisiones
    switch(prepaidMovement10.getConTecnocom()) {
      case RECONCILED: // Tecnocom conciliado ok
        switch(clearingData10.getStatus()) {
          case OK: // Linea 1: OK tecnocom, Banco OK
            {
              // Confirmar movimiento en cdt
              CdtTransaction10 cdtTransaction = getCdtEJB10().buscaMovimientoByIdExternoAndTransactionType(null, prepaidMovement10.getIdTxExterno(), CdtTransactionType.RETIRO_WEB);
              cdtTransaction.setTransactionType(cdtTransaction.getCdtTransactionTypeConfirm());
              cdtTransaction.setIndSimulacion(Boolean.FALSE);
              cdtTransaction.setTransactionReference(cdtTransaction.getId());
              getCdtEJB10().addCdtTransaction(null, cdtTransaction);

              // Actualizar el estado de negocio
              updatePrepaidBusinessStatus(null, prepaidMovement10.getId(), BusinessStatusType.CONFIRMED);
              prepaidMovement10.setEstadoNegocio(BusinessStatusType.CONFIRMED);

              // Actualiza estado accounting
              log.info("Actualizando el accounting id: " + clearingData10.getAccountingId());

              this.updateAccountingStatusReconciliationDateAndClearingStatus(prepaidMovement10.getId(), AccountingStatusType.OK, clearingData10.getStatus());

              // Se agrega a movimiento conciliado para que no vuelva a ser enviado.
              createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);

              // Enviar correo al usuario
              PrepaidUser10 prepaidUser = getPrepaidUserEJB10().getPrepaidUserById(null, prepaidMovement10.getIdPrepaidUser());
              User mcUser = getUserClient().getUserById(null, prepaidUser.getUserIdMc());
              UserAccount userAccount = getUserClient().getUserBankAccountById(null, mcUser.getId(), clearingData10.getUserBankAccount().getId());
              getMailDelegate().sendWithdrawSuccessMail(mcUser, prepaidMovement10, userAccount);
            }
            break;
          case REJECTED: // Linea 2: OK tecnocom, Banco RECHAZADO -> Reversar
          case REJECTED_FORMAT: // Linea 3: OK tecnocom, Banco RECHAZADO_FORMATO -> Reversar
            {
              //Se busca usuario prepago para obtener user
              PrepaidUser10 prepaidUser10 = getPrepaidUserEJB10().getPrepaidUserById(null, prepaidMovement10.getIdPrepaidUser());
              User user = userClient.getUserById(null, prepaidUser10.getUserIdMc());

              // Se crea movimiento de reversa
              NewPrepaidWithdraw10 newPrepaidWithdraw10 = new NewPrepaidWithdraw10();
              newPrepaidWithdraw10.setAmount(new NewAmountAndCurrency10(prepaidMovement10.getMonto()));
              newPrepaidWithdraw10.setMerchantCategory(prepaidMovement10.getCodact());
              newPrepaidWithdraw10.setMerchantCode(prepaidMovement10.getCodcom());
              newPrepaidWithdraw10.setMerchantName("Resolucion");
              newPrepaidWithdraw10.setRut(user.getRut().getValue());
              newPrepaidWithdraw10.setTransactionId(prepaidMovement10.getIdTxExterno());
              newPrepaidWithdraw10.setMovementType(PrepaidMovementType.WITHDRAW);
              // Se envia movimiento a reversar
              getPrepaidEJBBean10().reverseWithdrawUserBalance(null, newPrepaidWithdraw10,false);

              // Se agrega a movimiento conciliado para que no vuelva a ser enviado.
              createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.REVERSA_RETIRO, ReconciliationStatusType.COUNTER_MOVEMENT);

              this.updateAccountingStatusReconciliationDateAndClearingStatus(prepaidMovement10.getId(), AccountingStatusType.NOT_OK, clearingData10.getStatus());

              //Enviar correo al usuario
              PrepaidUser10 prepaidUser = prepaidUserEJB10.getPrepaidUserById(null, prepaidMovement10.getIdPrepaidUser());
              User mcUser = getUserClient().getUserById(null, prepaidUser.getUserIdMc());
              getMailDelegate().sendWithdrawFailedMail(mcUser, prepaidMovement10);
            }
            break;
          default: // Nunca deberia llegar aqui
            {

              //TODO: Esta OK este Research?

              /*
                String idToResearch = String.format("idMov=%d", prepaidMovement10.getId());
                createMovementResearch(
                null, idToResearch, ReconciliationOriginType.CLEARING_RESOLUTION,
                ResearchMovementFileStatusType.NOT_FILE_NAME.getValue(),
                prepaidMovement10.getFechaCreacion(), ResearchMovementResponsibleStatusType.RECONCILIATION_MULTICAJA,
                ResearchMovementDescriptionType.MOVEMENT_NOT_FOUND_IN_FILE,prepaidMovement10.getId());*/

              List<ResearchMovementInformationFiles> researchMovementInformationFilesList = new ArrayList<>();
              researchMovementInformationFiles = new ResearchMovementInformationFiles();
              //researchMovementInformationFiles.setIdArchivo();
              //researchMovementInformationFiles.setIdEnArchivo();
              //researchMovementInformationFiles.setNombreArchivo();
              //researchMovementInformationFiles.setTipoArchivo();
              researchMovementInformationFilesList.add(researchMovementInformationFiles);
              createResearchMovement(
                null,
                toJson(researchMovementInformationFilesList),
                ReconciliationOriginType.CLEARING_RESOLUTION.name(),
                prepaidMovement10.getFechaCreacion(),
                ResearchMovementResponsibleStatusType.RECONCILIATION_MULTICAJA.getValue(),
                ResearchMovementDescriptionType.MOVEMENT_NOT_FOUND_IN_FILE.getValue(),
                prepaidMovement10.getId(),
                PrepaidMovementType.TOPUP.name(),
                ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue()
              );

              // Se agrega a movimiento conciliado para que no vuelva a ser enviado.
              createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
            }
            break;
        }
        break;
      case NOT_RECONCILED: // Tecnocom NO conciliado -> todos los casos mandan a INVESTIGAR
        {

          //TODO: Esta OK este Research?
          //String idToResearch = String.format("idMov=%d", prepaidMovement10.getId());
          /*createMovementResearch(
            null, idToResearch, ReconciliationOriginType.CLEARING_RESOLUTION,
            ResearchMovementFileStatusType.NOT_FILE_NAME.getValue(),
            prepaidMovement10.getFechaCreacion(), ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID,
            ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_BANC_AND_PROCESOR,prepaidMovement10.getId());*/

          List<ResearchMovementInformationFiles> researchMovementInformationFilesList = new ArrayList<>();
          researchMovementInformationFiles = new ResearchMovementInformationFiles();
          //researchMovementInformationFiles.setIdArchivo();
          //researchMovementInformationFiles.setIdEnArchivo();
          //researchMovementInformationFiles.setNombreArchivo();
          //researchMovementInformationFiles.setTipoArchivo();
          researchMovementInformationFilesList.add(researchMovementInformationFiles);
          createResearchMovement(
            null,
            toJson(researchMovementInformationFilesList),
            ReconciliationOriginType.CLEARING_RESOLUTION.name(),
            prepaidMovement10.getFechaCreacion(),
            ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID.getValue(),
            ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_BANC_AND_PROCESOR.getValue(),
            prepaidMovement10.getId(),
            PrepaidMovementType.TOPUP.name(),
            ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue()
          );

          // Se agrega a movimiento conciliado para que no vuelva a ser enviado.
          createMovementConciliate(null, prepaidMovement10.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
        }
        break;

        //TODO: No conciliado en tecnocom, pero si en el banco, se envia a investigar, el responsable es concilicaciones prepago.
        //TODO: Viene en archivo tecnocom, pero no esta en la base de datos, se envía a investigar.

      default:
        //TODO: Viene en el archivo del banco, y no podemos conciliarlo, se envía a investigar.
        break;
    }
  }

  @Override
  public ReconciliedMovement10 getReconciliedMovementByIdMovRef(Long idMovRef) throws BaseException, SQLException {
    log.info("[getReonciliedMovementByIdMovRef In Id] : " + idMovRef);
    if(idMovRef == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "idMov"));
    }

    Object[] params = {
      new InParam(idMovRef, Types.BIGINT)
    };

    log.info(String.format("ID IN : %s", idMovRef));
    RowMapper rm = getReconciliedMovementRowMapper();
    Map<String, Object> resp = getDbUtils().execute(String.format("%s.mc_prp_busca_movimientos_conciliados_v10", getSchema()), rm, params);
    List list = (List)resp.get("result");
    log.info("getReconciliedMovementByIdMovRef: " + list);
    return list != null && !list.isEmpty() ? (ReconciliedMovement10) list.get(0) : null;
  }

  private RowMapper getReconciliedMovementRowMapper() {
    return (Map<String, Object> row) -> {
      ReconciliedMovement10 reconciliedMovement10 = new ReconciliedMovement10();
      reconciliedMovement10.setId(getNumberUtils().toLong(row.get("_id")));
      reconciliedMovement10.setIdMovRef(getNumberUtils().toLong(row.get("_id_mov_ref")));
      reconciliedMovement10.setActionType(ReconciliationActionType.valueOfEnum(String.valueOf(row.get("_accion"))));
      reconciliedMovement10.setReconciliationStatusType(ReconciliationStatusType.fromValue(String.valueOf(row.get("_estado"))));
      reconciliedMovement10.setFechaRegistro((Timestamp)row.get("_fecha_registro"));
      return reconciliedMovement10;
    };
  }

  @Override
  public Map<String,Object> createResearchMovement(
    Map<String, Object> headers, String filesInfo, String originType, Timestamp dateOfTransaction,
    String responsible, String description, Long movRef, String movementType, String sentStatus) throws Exception {


    String SP_INSERT_RESEARCH_MOVEMENT_NAME = getSchema() + ".mc_prp_crea_movimiento_investigar_v12";

    if(filesInfo == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "filesInfo"));
    }
    if(originType == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "originType"));
    }
    if(dateOfTransaction == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "dateOfTransaction"));
    }
    if(responsible == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "responsible"));
    }
    if(description == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "description"));
    }
    if(movRef == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "movRef"));
    }
    if(movementType == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "movementType"));
    }
    if(sentStatus == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "sentStatus"));
    }

    Object[] params = {
      new InParam(filesInfo,Types.VARCHAR),
      new InParam(originType,Types.VARCHAR),
      new InParam(dateOfTransaction,Types.TIMESTAMP),
      new InParam(responsible,Types.VARCHAR),
      new InParam(description,Types.VARCHAR),
      new InParam(movRef,Types.NUMERIC),
      new InParam(movementType,Types.VARCHAR),
      new InParam(sentStatus,Types.VARCHAR),
      new OutParam("_r_id", Types.BIGINT),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp = getDbUtils().execute(SP_INSERT_RESEARCH_MOVEMENT_NAME,params);

    if (!"0".equals(resp.get("_error_code"))) {
      log.error(SP_INSERT_RESEARCH_MOVEMENT_NAME+" resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }

    return resp;
  }

  @Override
  public Map<String, Object> updateResearchMovement(Long id, String sentStatus) throws Exception {

    String SP_UPDATE_RESEARCH_MOVEMENT_NAME = getSchema() + ".mc_prp_actualiza_movimiento_investigar_v10";


    if(id == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }

    if(sentStatus == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "sentStatus"));
    }


    Object[] params = {
      id != null ? new InParam(id, Types.BIGINT) : new NullParam(Types.BIGINT),
      sentStatus != null ? new InParam(sentStatus, Types.VARCHAR) : new NullParam(Types.VARCHAR),
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String,Object> resp = getDbUtils().execute(SP_UPDATE_RESEARCH_MOVEMENT_NAME,params);

    if (!"0".equals(resp.get("_error_code"))) {
      log.error(SP_UPDATE_RESEARCH_MOVEMENT_NAME+" resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }

    return resp;
  }

  @Override
  public List<ResearchMovement10> getResearchMovement(
    Long id, Timestamp beginDateTime, Timestamp endDateTime, String sentStatus, BigDecimal movRef) throws SQLException {

    String SP_SEARCH_RESEARCH_MOVEMENT_NAME = getSchema() + ".mc_prp_busca_movimientos_a_investigar_v13";

    List<ResearchMovement10> researchMovements = new ArrayList<>();

    Object[] params = {
      id != null ? new InParam(id, Types.BIGINT) : new NullParam(Types.BIGINT),
      beginDateTime != null ? new InParam(beginDateTime, Types.TIMESTAMP) : new NullParam(Types.TIMESTAMP),
      endDateTime != null ? new InParam(endDateTime, Types.TIMESTAMP) : new NullParam(Types.TIMESTAMP),
      sentStatus != null ? new InParam(sentStatus, Types.VARCHAR) : new NullParam(Types.VARCHAR),
      movRef != null ? new InParam(movRef, Types.DECIMAL) : new NullParam(Types.DECIMAL)
    };

    RowMapper rm = getResearchMovementRowMapper();
    Map<String,Object> resp = getDbUtils().execute(SP_SEARCH_RESEARCH_MOVEMENT_NAME,rm,params);

    List<Map<String, Object>> results = (List)resp.get("result");

    if(results != null){
      if(results.size()>0){
        for(int i=0; i<Long.valueOf(results.size());i++) {
          researchMovements.add((ResearchMovement10) results.get(i));
        }
      }
    }

    log.info("getResearchMovement: " + researchMovements);
    return researchMovements;
  }

  @Override
  public ResearchMovement10 getResearchMovementById(Long id) throws SQLException {
    log.info("[getResearchMovementById In Id] : " + id);
    List<ResearchMovement10> researchMovements = getResearchMovement(
      id,null,null,null,null);
    return researchMovements != null && !researchMovements.isEmpty() ? researchMovements.get(0) : null;
  }

  @Override
  public List<ResearchMovement10> getResearchMovementByDateTimeRange(
    Timestamp startDateTime, Timestamp endDateTime) throws SQLException {
    log.info("[getResearchMovementByDateTimeRange In startDateTime and endDataTime] : " + startDateTime+" "+endDateTime);
    return getResearchMovement(null,startDateTime,endDateTime,null,null);
  }

  @Override
  public List<ResearchMovement10> getResearchMovementByMovRef(BigDecimal movRef) throws SQLException {
    log.info("[getResearchMovementByMovRef In movRef] : " + movRef);
    List<ResearchMovement10> researchMovements = getResearchMovement(
      null,null,null,null,movRef);
    //return researchMovements != null && !researchMovements.isEmpty() ? researchMovements.get(0) : null;
    return researchMovements;
  }

  @Override
  public List<ResearchMovement10> getResearchMovementBySentStatus(String sentStatus) throws SQLException {
    log.info("[getResearchMovementBySentStatus In sentStatus] : " + sentStatus);
    return getResearchMovement(null,null,null,sentStatus,null);
  }

  private RowMapper getResearchMovementRowMapper(){

    return (Map<String,Object> row) -> {
      ResearchMovement10 researchMovement = new ResearchMovement10();

      researchMovement.setId(NumberUtils.getInstance().toLong(row.get("_id")));
      researchMovement.setFilesInfo(String.valueOf(row.get("_informacion_archivos")));
      researchMovement.setOriginType(ReconciliationOriginType.valueOf(String.valueOf(row.get("_origen"))));
      researchMovement.setCreatedAt((Timestamp) row.get("_fecha_registro"));
      researchMovement.setDateOfTransaction((Timestamp) row.get("_fecha_de_transaccion"));
      researchMovement.setResponsible(ResearchMovementResponsibleStatusType.fromValue(String.valueOf(row.get("_responsable"))));
      researchMovement.setDescription(ResearchMovementDescriptionType.fromValue(String.valueOf(row.get("_descripcion"))));
      researchMovement.setMovRef(BigDecimal.valueOf(NumberUtils.getInstance().toLong(row.get("_mov_ref"))));
      researchMovement.setMovementType(PrepaidMovementType.valueOfEnum(String.valueOf(row.get("_tipo_movimiento"))));
      researchMovement.setSentStatus(ResearchMovementSentStatusType.fromValue(String.valueOf(row.get("_sent_status"))));

      return researchMovement;
    };
  }

  public void sendResearchEmail() throws Exception {
    ZonedDateTime nowChileDateTime = ZonedDateTime.now(ZoneId.of(ZONEID.AMERICA_SANTIAGO.getValue()));
    ZonedDateTime yesterdayChileDateTime = nowChileDateTime.minusDays(1);
    LocalDate yesterdayDate = yesterdayChileDateTime.toLocalDate();
    LocalDateTime yesterdayBegining = yesterdayDate.atTime(0, 0, 0);
    ZonedDateTime yesterdayBeginingZonedChile = yesterdayBegining.atZone(ZoneId.of(ZONEID.AMERICA_SANTIAGO.getValue()));
    ZonedDateTime startZonedUtc = yesterdayBeginingZonedChile.withZoneSameInstant(ZoneId.of("UTC"));
    ZonedDateTime endZoneUtc = nowChileDateTime.withZoneSameInstant(ZoneId.of("UTC"));

    List<ResearchMovement10> yesterdayResearchMovements = getResearchMovement(
      null,
      Timestamp.valueOf(startZonedUtc.toLocalDateTime()),
      Timestamp.valueOf(endZoneUtc.toLocalDateTime()),
      ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue(),
      null
      );

    LocalDateTime todayLocal = LocalDateTime.now();
    String todayString = todayLocal.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
    String fileName = String.format("research_%s.csv", todayString);

    File file = new File(fileName);

    try {

      FileWriter outputfile = new FileWriter(file);
      CSVWriter writer = new CSVWriter(outputfile,',');
      String[] header;

      String[] data;
      Boolean isSetHeader;

      isSetHeader = Boolean.TRUE;
      for(ResearchMovement10 mov : yesterdayResearchMovements) {

        if(isSetHeader){
          header = mov.toMailUse(Boolean.TRUE);
          writer.writeNext(header);
        }
        data = mov.toMailUse(Boolean.FALSE);
        writer.writeNext(data);
        isSetHeader = Boolean.FALSE;
      }
      writer.close();
    } catch (Exception e) {
      log.error("Exception : " + e);
      e.printStackTrace();
    }

    sendResearchFile(fileName, "research_test@gmail.com",yesterdayResearchMovements);

    file.delete();
  }

  private void sendResearchFile(String fileName, String emailAddress,List<ResearchMovement10> researchMovements) throws Exception {

    String file = fileName;
    FileInputStream attachmentFile = new FileInputStream(file);
    String fileToSend = Base64Utils.encodeToString(IOUtils.toByteArray(attachmentFile));
    attachmentFile.close();

    // Enviamos el archivo al mail de reportes diarios
    EmailBody emailBodyToSend = new EmailBody();
    emailBodyToSend.addAttached(fileToSend, MimeType.CSV.getValue(), fileName);
    emailBodyToSend.setTemplateData(null);
    emailBodyToSend.setTemplate(MailTemplates.TEMPLATE_MAIL_RESEARCH_REPORT);
    emailBodyToSend.setAddress(emailAddress);
    mailPrepaidEJBBean10.sendMailAsync(null, emailBodyToSend);

    Files.delete(Paths.get(file));

    //change status of research_movements to sent_ok
    for(ResearchMovement10 researchMovement : researchMovements){
      updateResearchMovement(researchMovement.getId(),ResearchMovementSentStatusType.SENT_RESEARCH_OK.getValue());
    }

  }
}
