package cl.multicaja.prepaid.ejb.v11;

import cl.multicaja.accounting.model.v10.AccountingData10;
import cl.multicaja.accounting.model.v10.AccountingStatusType;
import cl.multicaja.accounting.model.v10.ClearingData10;
import cl.multicaja.cdt.model.v10.CdtTransaction10;
import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.prepaid.async.v10.KafkaEventDelegate10;
import cl.multicaja.prepaid.ejb.v10.PrepaidMovementEJBBean10;
import cl.multicaja.prepaid.external.freshdesk.model.NewTicket;
import cl.multicaja.prepaid.external.freshdesk.model.Ticket;
import cl.multicaja.prepaid.helpers.freshdesk.model.v10.*;
import cl.multicaja.prepaid.kafka.events.TransactionEvent;
import cl.multicaja.prepaid.kafka.events.model.*;
import cl.multicaja.prepaid.kafka.events.model.Timestamps;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.model.v11.PrepaidMovementFeeType;
import cl.multicaja.prepaid.utils.TemplateUtils;
import cl.multicaja.tecnocom.constants.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.ejb.*;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static cl.multicaja.core.model.Errors.*;
import static cl.multicaja.prepaid.helpers.CalculationsHelper.getParametersUtil;

@Stateless
@LocalBean
@TransactionManagement(value= TransactionManagementType.CONTAINER)
public class PrepaidMovementEJBBean11 extends PrepaidMovementEJBBean10 {

  private static Log log = LogFactory.getLog(PrepaidMovementEJBBean11.class);

  private static final String FIND_MOVEMENT_BY_ID_SQL = String.format("SELECT * FROM %s.prp_movimiento WHERE id = ?", getSchema());
  private static final String INSERT_MOVEMENT_SQL
    = String.format("INSERT INTO %s.prp_movimiento (id_movimiento_ref, id_usuario, id_tx_externo, tipo_movimiento, monto, " +
    "estado, estado_de_negocio, estado_con_switch,estado_con_tecnocom,origen_movimiento,fecha_creacion,fecha_actualizacion," +
    "codent,centalta,cuenta,clamon,indnorcor,tipofac,fecfac,numreffac,pan,clamondiv,impdiv,impfac,cmbapli,numaut,indproaje," +
    "codcom,codact,impliq,clamonliq,codpais,nompob,numextcta,nummovext,clamone,tipolin,linref,numbencta,numplastico,nomcomred, id_tarjeta) " +
    "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);", getSchema());

  // Fee Queries
  private static final String FIND_FEE_BY_ID = String.format("SELECT * FROM %s.prp_movimiento_comision WHERE id = ?", getSchema());
  private static final String FIND_FEE_BY_MOVEMENT_ID = String.format("SELECT * FROM %s.prp_movimiento_comision WHERE id_movimiento = ?", getSchema());
  private static final String INSERT_FEE_SQL = String.format("INSERT INTO %s.prp_movimiento_comision (id_movimiento, tipo_comision, monto, iva, creacion, actualizacion) VALUES(?, ?, ?, ?, timezone('utc', now()), timezone('utc', now()))", getSchema());
  private static final String GET_NUMAUT = String.format("SELECT %s.getnumaut()",getSchema());

  @Inject
  private KafkaEventDelegate10 kafkaEventDelegate10;

  public KafkaEventDelegate10 getKafkaEventDelegate10() {
    return kafkaEventDelegate10;
  }

  public void setKafkaEventDelegate10(KafkaEventDelegate10 kafkaEventDelegate10) {
    this.kafkaEventDelegate10 = kafkaEventDelegate10;
  }

  @Override
  public PrepaidMovement10 addPrepaidMovement(Map<String, Object> header, PrepaidMovement10 movement) throws Exception {
    if(movement == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "movement"));
    }

    log.info(String.format("[addPrepaidMovement] Guardando Movimiento con [%s]", movement.toString()));

    KeyHolder keyHolder = new GeneratedKeyHolder();

    getDbUtils().getJdbcTemplate().update(connection -> {
      PreparedStatement ps = connection
        .prepareStatement(INSERT_MOVEMENT_SQL, new String[] {"id"});
      ps.setLong(1, movement.getIdMovimientoRef());
      ps.setLong(2, movement.getIdPrepaidUser());
      ps.setString(3, movement.getIdTxExterno());
      ps.setString(4, movement.getTipoMovimiento().toString());
      ps.setBigDecimal(5, movement.getMonto());
      ps.setObject(6, movement.getEstado().toString());
      ps.setObject(7, movement.getEstadoNegocio().toString());
      ps.setObject(8, movement.getConSwitch().toString());
      ps.setObject(9, movement.getConTecnocom().toString());
      ps.setObject(10, movement.getOriginType().toString());
      ps.setTimestamp(11, movement.getFechaCreacion() != null ? movement.getFechaCreacion():  Timestamp.valueOf(LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"))));
      ps.setTimestamp(12, Timestamp.valueOf(LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"))));
      ps.setString(13, movement.getCodent());
      ps.setString(14, movement.getCentalta());
      ps.setString(15, movement.getCuenta());
      ps.setObject(16, movement.getClamon().getValue());
      ps.setObject(17, movement.getIndnorcor().getValue());
      ps.setObject(18, movement.getTipofac().getCode());
      ps.setDate(19, java.sql.Date.valueOf(movement.getFecfac().toInstant().atZone(ZoneId.of("UTC")).toLocalDate()));
      ps.setString(20, movement.getNumreffac());
      ps.setString(21, movement.getPan());
      ps.setLong(22, movement.getClamondiv());
      ps.setBigDecimal(23, movement.getImpdiv());
      ps.setBigDecimal(24, movement.getImpfac());
      ps.setLong(25, movement.getCmbapli());
      ps.setString(26,  (movement.getNumaut() == null || movement.getNumaut().isEmpty()) ? getNumAut() : movement.getNumaut());
      ps.setObject(27, movement.getIndproaje().toString());
      ps.setString(28, movement.getCodcom());
      ps.setLong(29, movement.getCodact());
      ps.setBigDecimal(30, movement.getImpliq());
      ps.setLong(31, movement.getClamonliq());
      ps.setObject(32, movement.getCodpais().getValue());
      ps.setString(33, movement.getNompob());
      ps.setLong(34, movement.getNumextcta());
      ps.setLong(35, movement.getNummovext());
      ps.setLong(36, movement.getClamone());
      ps.setString(37, movement.getTipolin());
      ps.setLong(38, movement.getLinref());
      ps.setLong(39, movement.getNumbencta());
      ps.setLong(40, movement.getNumplastico());
      ps.setString(41, movement.getNomcomred());
      ps.setLong(42, movement.getCardId());

      return ps;
    }, keyHolder);

    return  this.getPrepaidMovementById((long) keyHolder.getKey());
  }

  @Override
  public PrepaidMovement10 getPrepaidMovementById(Long id) throws Exception {
    if(id == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }

    log.info(String.format("[getPrepaidMovementById] Buscando movimiento por id [%d]", id));
    try {
      return getDbUtils().getJdbcTemplate()
        .queryForObject(FIND_MOVEMENT_BY_ID_SQL, this.getMovementMapper(), id);
    } catch (EmptyResultDataAccessException ex) {
      log.error(String.format("[getPrepaidMovementById]  Movimiento con id [%d] no existe", id));
      return null;
    }
  }
  @Override
  public List<PrepaidMovement10> getPrepaidMovements(Long id, Long idMovimientoRef, Long idPrepaidUser, String idTxExterno, PrepaidMovementType tipoMovimiento,
                                                     PrepaidMovementStatus estado, String cuenta, CodigoMoneda clamon, IndicadorNormalCorrector indnorcor, TipoFactura tipofac, Date fecfac, String numaut,
                                                     ReconciliationStatusType estadoConSwitch, ReconciliationStatusType estadoConTecnocom, MovementOriginType origen, String pan, String codcom) throws Exception {
    return getPrepaidMovements( id, idMovimientoRef, idPrepaidUser, idTxExterno, tipoMovimiento,
      estado, cuenta, clamon, indnorcor, tipofac, fecfac, numaut, estadoConSwitch, estadoConTecnocom, origen, pan, codcom,null);
  }

  public List<PrepaidMovement10> getPrepaidMovements(Long id, Long idMovimientoRef, Long idPrepaidUser, String idTxExterno, PrepaidMovementType tipoMovimiento,
                                                     PrepaidMovementStatus estado, String cuenta, CodigoMoneda clamon, IndicadorNormalCorrector indnorcor, TipoFactura tipofac, Date fecfac, String numaut,
                                                     ReconciliationStatusType estadoConSwitch, ReconciliationStatusType estadoConTecnocom, MovementOriginType origen, String pan, String codcom,Long cardId) throws Exception {
    StringBuilder query = new StringBuilder();
    query.append(String.format("SELECT * FROM %s.prp_movimiento WHERE ", getSchema()));
    query.append(id != null ? String.format("id = %d AND ", id) : "");
    query.append(idMovimientoRef != null ? String.format("id_movimiento_ref = %d AND ", idMovimientoRef) : "");
    query.append(idPrepaidUser != null ? String.format("id_usuario = %d AND ", idPrepaidUser) : "");
    query.append(idTxExterno != null ? String.format("id_tx_externo = '%s' AND ", idTxExterno) : "");
    query.append(tipoMovimiento != null ? String.format("tipo_movimiento = '%s' AND ", tipoMovimiento.toString()) : "");
    query.append(estado != null ? String.format("estado = '%s' AND ", estado.toString()) : "");
    query.append(cuenta != null ? String.format("cuenta = '%s' AND ", cuenta) : "");
    query.append(clamon != null ? String.format("clamon = %d AND ", clamon.getValue()) : "");
    query.append(indnorcor != null ? String.format("indnorcor = %d AND ", indnorcor.getValue()) : "");
    query.append(tipofac != null ? String.format("tipofac = %d AND ", tipofac.getCode()) : "");
    if (fecfac != null) {
      SimpleDateFormat sdf = new SimpleDateFormat("YYYYMMdd");
      String fecfacString = sdf.format(fecfac);
      query.append(String.format("fecfac = to_date('%s', 'YYYYMMDD') AND ", fecfacString));
    }
    query.append(numaut != null ? String.format("numaut = '%s' AND ", numaut) : "");
    query.append(estadoConSwitch != null ? String.format("estado_con_switch = '%s' AND ", estadoConSwitch.getValue()) : "");
    query.append(estadoConTecnocom != null ? String.format("estado_con_tecnocom = '%s' AND ", estadoConTecnocom.getValue()) : "");
    query.append(origen != null ? String.format("origen_movimiento = '%s' AND ", origen.getValue()) : "");
    query.append(pan != null ? String.format("pan = '%s' AND ", pan) : "");
    query.append(codcom != null ? String.format("codcom = '%s' AND ", codcom) : "");
    query.append(cardId != null ? String.format("id_tarjeta = %d AND ",cardId) : "");
    query.append("1 = 1");
    log.info(String.format("[getPrepaidMovements] Buscando movimiento [id: %d]", id));
    log.info("[getPrepaidMovements Query]  "+query.toString());
    try {
      return getDbUtils().getJdbcTemplate().query(query.toString(), this.getMovementMapper());
    } catch (EmptyResultDataAccessException ex) {
      log.error(String.format("[getPrepaidMovements] Movimiento con id [%d] no existe", id));
      return null;
    }
  }

  public List<PrepaidMovement10> getPrepaidMovementByCardIdAndEstado(Long cardId, PrepaidMovementStatus estado) throws Exception {
    if (cardId == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "cardId"));
    }
    if (estado == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "estado"));
    }
    return this.getPrepaidMovements(null, null, null, null, null, estado, null,
      null, null, null, null, null, null, null, null, null, null,cardId);
  }

  public List<PrepaidMovement10> getPrepaidMovementByCardIdAndTipoMovimiento(Long cardId, PrepaidMovementType tipoMovimiento) throws Exception {
    if (cardId == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "idPrepaidUser"));
    }
    if (tipoMovimiento == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "tipoMovimiento"));
    }
    return this.getPrepaidMovements(null, null, null, null, tipoMovimiento, null, null,
      null, null, null, null, null, null, null, null, null, null,cardId);
  }

  public PrepaidMovement10 getPrepaidMovementForAut(Long idPrepaidUser, TipoFactura tipoFactura, String numaut, String codcom) throws Exception {
    if (idPrepaidUser == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "idPrepaidUser"));
    }
    if (numaut == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "numaut"));
    }
    List<PrepaidMovement10> lst = this.getPrepaidMovements(null, null, idPrepaidUser, null, null, null, null, null, null, tipoFactura, null, numaut, null, null, null, null, codcom);
    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }
  @Override
  public void processReconciliationRules() throws Exception {
    List<PrepaidMovement10> lstPrepaidMovement10s = this.getMovementsForConciliate(null);
    if (lstPrepaidMovement10s == null) {
      return;
    }

    log.info(String.format("lstPrepaidMovement10s: %d", lstPrepaidMovement10s.size()));
    for (PrepaidMovement10 mov : lstPrepaidMovement10s) {
      try {
        log.info("[processReconciliation] IN");
        this.processReconciliation(mov);
        log.info("[processReconciliation] OUT");
      } catch (Exception e) {
        log.error(e.getMessage());
        e.printStackTrace();
        continue;
      }
    }
  }
  @Override
  public ReconciliedMovement10 getReconciliedMovementByIdMovRef(Long idMovRef) throws BaseException, SQLException {
    log.info("[getReonciliedMovementByIdMovRef In Id] : " + idMovRef);
    if (idMovRef == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "idMov"));
    }
    String query = "SELECT\n" +
      "      id,\n" +
      "      id_mov_ref,\n" +
      "      fecha_registro,\n" +
      "      accion,\n" +
      "      estado\n" +
      "    FROM\n" +
      "      prepago.prp_movimiento_conciliado\n" +
      "    WHERE\n" +
      "      id_mov_ref = ?";
    try {
      return getDbUtils().getJdbcTemplate().queryForObject(query, this.getReconciliedMovementMapper(), idMovRef);
    } catch (EmptyResultDataAccessException ex) {
      log.error(String.format("[getReconciliedMovementByIdMovRef]  Movimiento con idRef [%d] no existe", idMovRef));
      return null;
    }
  }

  public PrepaidMovement10 getPrepaidMovementByIdTxExterno(String idTxExterno, PrepaidMovementType prepaidMovementType, IndicadorNormalCorrector indicadorNormalCorrector) throws Exception {
    if (idTxExterno == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "idTxExterno"));
    }

    List<PrepaidMovement10> lst = this.getPrepaidMovements(null, null,  null,  idTxExterno,  prepaidMovementType, null,  null,  null,  indicadorNormalCorrector,  null,  null,  null, null,  null,  null,  null,  null);
    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }

  private RowMapper<ReconciliedMovement10> getReconciliedMovementMapper() {
    return (ResultSet rs, int rowNum) -> {
      ReconciliedMovement10 rm = new ReconciliedMovement10();
      rm.setId(rs.getLong("id"));
      rm.setIdMovRef(rs.getLong("id_mov_ref"));
      rm.setFechaRegistro(rs.getTimestamp("fecha_registro"));
      rm.setActionType(ReconciliationActionType.valueOfEnum(rs.getString("accion")));
      rm.setReconciliationStatusType(ReconciliationStatusType.valueOfEnum(rs.getString("estado")));
      return rm;
    };
  }

  private RowMapper<PrepaidMovement10> getMovementMapper() {
    return (ResultSet rs, int rowNum) -> {
      PrepaidMovement10 movement = new PrepaidMovement10();
      movement.setId(rs.getLong("id"));
      movement.setIdMovimientoRef(rs.getLong("id_movimiento_ref"));
      movement.setIdPrepaidUser(rs.getLong("id_usuario"));
      movement.setIdTxExterno(rs.getString("id_tx_externo"));
      movement.setTipoMovimiento(PrepaidMovementType.valueOfEnum(rs.getString("tipo_movimiento")));
      movement.setMonto(rs.getBigDecimal("monto"));
      movement.setEstado(PrepaidMovementStatus.valueOfEnum(rs.getString("estado")));
      movement.setEstadoNegocio(BusinessStatusType.valueOfEnum(rs.getString("estado_de_negocio")));
      movement.setConSwitch(ReconciliationStatusType.valueOfEnum(rs.getString("estado_con_switch")));
      movement.setConTecnocom(ReconciliationStatusType.valueOfEnum(rs.getString("estado_con_tecnocom")));
      movement.setOriginType(MovementOriginType.valueOf(rs.getString("origen_movimiento")));
      movement.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
      movement.setFechaActualizacion(rs.getTimestamp("fecha_actualizacion"));
      movement.setCodent(rs.getString("codent"));
      movement.setCentalta(rs.getString("centalta"));
      movement.setCuenta(rs.getString("cuenta"));
      movement.setClamon(CodigoMoneda.fromValue(rs.getInt("clamon")));
      movement.setIndnorcor(IndicadorNormalCorrector.fromValue(rs.getInt("indnorcor")));
      movement.setTipofac(TipoFactura.valueOfEnumByCodeAndCorrector(rs.getInt("tipofac"),rs.getInt("indnorcor")));
      movement.setFecfac(rs.getDate("fecfac"));
      movement.setNumreffac(rs.getString("numreffac"));
      movement.setPan(rs.getString("pan"));
      movement.setClamondiv(rs.getInt("clamondiv"));
      movement.setImpdiv(rs.getBigDecimal("impdiv"));
      movement.setImpfac(rs.getBigDecimal("impfac"));
      movement.setCmbapli(rs.getInt("cmbapli"));
      movement.setNumaut(rs.getString("numaut"));
      movement.setIndproaje(IndicadorPropiaAjena.fromValue(rs.getString("indproaje")));
      movement.setCodcom(rs.getString("codcom"));
      movement.setCodact(rs.getInt("codact"));
      movement.setImpliq(rs.getBigDecimal("impliq"));
      movement.setClamonliq(rs.getInt("clamonliq"));
      movement.setCodpais(CodigoPais.fromValue(rs.getInt("codpais")));
      movement.setNompob(rs.getString("nompob"));
      movement.setNumextcta(rs.getInt("numextcta"));
      movement.setNummovext(rs.getInt("nummovext"));
      movement.setClamone(rs.getInt("clamone"));
      movement.setTipolin(rs.getString("tipolin"));
      movement.setLinref(rs.getInt("linref"));
      movement.setNumbencta(rs.getInt("numbencta"));
      movement.setNumplastico(rs.getLong("numplastico"));
      movement.setNomcomred(rs.getString("nomcomred"));
      movement.setCardId(rs.getLong("id_tarjeta"));

      return movement;
    };
  }
  private RowMapper<PrepaidMovement10> getMovementConciliationMapper() {
    return (ResultSet rs, int rowNum) -> {
      PrepaidMovement10 movement = new PrepaidMovement10();
      movement.setId(rs.getLong("id"));
      movement.setTipoMovimiento(PrepaidMovementType.valueOfEnum(rs.getString("tipo_movimiento")));
      movement.setEstado(PrepaidMovementStatus.valueOfEnum(rs.getString("estado")));
      movement.setEstadoNegocio(BusinessStatusType.valueOfEnum(rs.getString("estado_de_negocio")));
      movement.setConSwitch(ReconciliationStatusType.valueOfEnum(rs.getString("estado_con_switch")));
      movement.setConTecnocom(ReconciliationStatusType.valueOfEnum(rs.getString("estado_con_tecnocom")));
      movement.setFechaCreacion(rs.getTimestamp("fecha_creacion"));
      movement.setIndnorcor(IndicadorNormalCorrector.fromValue(rs.getInt("indnorcor")));
      movement.setTipofac(TipoFactura.valueOfEnumByCodeAndCorrector(rs.getInt("tipofac"),rs.getInt("indnorcor")));
      movement.setCardId(rs.getLong("id_tarjeta"));

      return movement;
    };
  }
  public void publishTransactionAuthorizedEvent(String externalUserId, String accountUuid, String cardUuid, PrepaidMovement10 movement, List<PrepaidMovementFee10> feeList, TransactionType type) throws Exception {
    this.publishTransactionEvent(externalUserId, accountUuid, cardUuid, movement, feeList, type, TransactionStatus.AUTHORIZED);
  }

  public void publishTransactionRejectedEvent(String externalUserId, String accountUuid, String cardUuid, PrepaidMovement10 movement, List<PrepaidMovementFee10> feeList, TransactionType type) throws Exception {
    this.publishTransactionEvent(externalUserId, accountUuid, cardUuid, movement, feeList, type, TransactionStatus.REJECTED);
  }

  public void publishTransactionReversedEvent(String externalUserId, String accountUuid, String cardUuid, PrepaidMovement10 movement, List<PrepaidMovementFee10> feeList, TransactionType type) throws Exception {
    this.publishTransactionEvent(externalUserId, accountUuid, cardUuid, movement, feeList, type, TransactionStatus.REVERSED);
  }

  public void publishTransactionPaidEvent(String externalUserId, String accountUuid, String cardUuid, PrepaidMovement10 movement, List<PrepaidMovementFee10> feeList, TransactionType type) throws Exception {
    this.publishTransactionEvent(externalUserId, accountUuid, cardUuid, movement, feeList, type, TransactionStatus.PAID);
  }

  /**
   *  Publica evento de transacion
   *
   * @throws Exception
   */
  private void publishTransactionEvent(String externalUserId, String accountUuid, String cardUuid, PrepaidMovement10 movement, List<PrepaidMovementFee10> feeList, TransactionType type, TransactionStatus status) throws Exception {
    if(StringUtils.isAllBlank(externalUserId)){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "externalUserId"));
    }
    if(StringUtils.isAllBlank(accountUuid)){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountUuid"));
    }
    if(StringUtils.isAllBlank(cardUuid)){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "cardUuid"));
    }
    if(movement == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "movement"));
    }
    if(type == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "type"));
    }
    if(status == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "status"));
    }

    log.info(String.format("[publishTransactionEvent] Publicando evento de transaccion -> [status: %s, transactionId: %s, user: %s, account: %s, card: %s]",
      status, movement.getIdTxExterno(), externalUserId, accountUuid, cardUuid));

    Transaction transaction = new Transaction();
    //transaction.setId(prepaidTopup10.getId().toString());
    transaction.setRemoteTransactionId(movement.getIdTxExterno());
    transaction.setAuthCode(movement.getNumaut());
    transaction.setCountryCode(movement.getCodpais().getValue());

    Merchant merchant = new Merchant();
    merchant.setCategory(movement.getCodact());
    merchant.setCode(movement.getCodcom());
    merchant.setName(movement.getNomcomred());

    transaction.setMerchant(merchant);

    NewAmountAndCurrency10 newAmountAndCurrency10 = new NewAmountAndCurrency10();
    newAmountAndCurrency10.setValue(movement.getMonto());
    newAmountAndCurrency10.setCurrencyCode(movement.getClamon());

    transaction.setPrimaryAmount(newAmountAndCurrency10);
    if(TransactionType.CASH_IN_MULTICAJA.equals(type) || TransactionType.CASH_OUT_MULTICAJA.equals(type)){
      transaction.setSecondaryAmount(newAmountAndCurrency10);
    }
    transaction.setType(type.toString());
    transaction.setStatus(status.toString());

    List<Fee> fees;
    if(feeList == null || feeList.isEmpty()) {
      fees = Collections.emptyList();
    } else {
      fees = new ArrayList<>();
      for(PrepaidMovementFee10 fee : feeList) {
        Fee f = new Fee();
        f.setAmount(new NewAmountAndCurrency10(fee.getAmount()));
        f.setType(fee.getFeeType().toString());
        fees.add(f);
      }
    }

    transaction.setFees(fees);

    cl.multicaja.prepaid.kafka.events.model.Timestamps timestamps = new Timestamps();
    timestamps.setCreatedAt(movement.getFechaCreacion().toLocalDateTime());
    timestamps.setUpdatedAt(movement.getFechaActualizacion().toLocalDateTime());

    transaction.setTimestamps(timestamps);

    TransactionEvent transactionEvent = new TransactionEvent();
    transactionEvent.setTransaction(transaction);

    transactionEvent.setAccountId(accountUuid);
    transactionEvent.setUserId(externalUserId);
    transactionEvent.setCardId(cardUuid);

    getKafkaEventDelegate10().publishTransactionEvent(transactionEvent);
  }

  public PrepaidMovementFee10 getPrepaidMovementFeeById(Long feeId) throws Exception {
    if(feeId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "feeId"));
    }

    log.info(String.format("[getPrepaidMovementFeeById] Buscando fee [feeId: %d]", feeId));

    try {
      return getDbUtils().getJdbcTemplate()
        .queryForObject(FIND_FEE_BY_ID, getMovementFeeMapper(), feeId);
    } catch (EmptyResultDataAccessException ex) {
      log.error(String.format("[getPrepaidMovementFeeById] Fee [feeId: %d] no existe", feeId));
      return null;
    } catch (Exception e) {
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
  }



  public List<PrepaidMovementFee10> getPrepaidMovementFeesByMovementId(Long movementId) throws BaseException {
    if(movementId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "movementId"));
    }

    log.info(String.format("[getPrepaidMovementFeesByMovementId] Buscando fee [movementId: %d]", movementId));

    try {
      return getDbUtils().getJdbcTemplate().query(FIND_FEE_BY_MOVEMENT_ID, getMovementFeeMapper(), movementId);
    } catch (Exception e) {
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
  }

  public void addPrepaidMovementFeeList(List<PrepaidMovementFee10> feeList) throws Exception {
    for(PrepaidMovementFee10 fee : feeList) {
      addPrepaidMovementFee(fee);
    }
  }

  public PrepaidMovementFee10 addPrepaidMovementFee(PrepaidMovementFee10 prepaidMovementFee) throws Exception {
    if(prepaidMovementFee == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "prepaidMovementFee"));
    }
    if(prepaidMovementFee.getFeeType() == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "prepaidMovementFee.feeType"));
    }
    if(prepaidMovementFee.getAmount() == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "prepaidMovementFee.amount"));
    }

    log.info(String.format("[addPrepaidMovementFee] Guardando Comision de movimiento con [%s]", prepaidMovementFee.toString()));

    KeyHolder keyHolder = new GeneratedKeyHolder();

    getDbUtils().getJdbcTemplate().update(connection -> {
      PreparedStatement ps = connection.prepareStatement(INSERT_FEE_SQL, new String[] {"id"});
      ps.setLong(1, prepaidMovementFee.getMovementId());
      ps.setString(2, prepaidMovementFee.getFeeType().toString());
      ps.setBigDecimal(3, prepaidMovementFee.getAmount());
      ps.setBigDecimal(4, new BigDecimal(0L)); //Todo: Columna debe eliminarse
      return ps;
    }, keyHolder);

    return  this.getPrepaidMovementFeeById((long) keyHolder.getKey());
  }

  private RowMapper<PrepaidMovementFee10> getMovementFeeMapper() {
    return (ResultSet rs, int rowNum) -> {
      PrepaidMovementFee10 prepaidMovementFee = new PrepaidMovementFee10();
      prepaidMovementFee.setId(rs.getLong("id"));
      prepaidMovementFee.setMovementId(rs.getLong("id_movimiento"));
      prepaidMovementFee.setFeeType(PrepaidMovementFeeType.valueOfEnum(rs.getString("tipo_comision")));
      prepaidMovementFee.setAmount(rs.getBigDecimal("monto"));
      prepaidMovementFee.setIva(rs.getBigDecimal("iva"));
      cl.multicaja.prepaid.model.v10.Timestamps timestamps = new cl.multicaja.prepaid.model.v10.Timestamps();
      timestamps.setCreatedAt(rs.getTimestamp("creacion").toLocalDateTime());
      timestamps.setUpdatedAt(rs.getTimestamp("actualizacion").toLocalDateTime());
      prepaidMovementFee.setTimestamps(timestamps);
      return prepaidMovementFee;
    };
  }

  public void expireNotReconciledAuthorizations() throws Exception {
    StringBuilder queryExpire = new StringBuilder();
    queryExpire.append("UPDATE %s.prp_movimiento mov SET estado_con_tecnocom = 'NOT_RECONCILED', estado = 'EXPIRED' " );
    queryExpire.append("WHERE (mov.tipo_movimiento = 'SUSCRIPTION' OR mov.tipo_movimiento = 'PURCHASE') ");
    queryExpire.append("AND mov.estado_con_tecnocom = 'PENDING' ");
    queryExpire.append("AND mov.estado = '%s' ");
    queryExpire.append("AND (SELECT COUNT(f.id) ");
    queryExpire.append("FROM %s.prp_archivos_conciliacion f ");
    queryExpire.append("WHERE f.created_at >= mov.fecha_creacion AND f.tipo = 'TECNOCOM_FILE' AND f.status = 'OK' ) >= %d");
    String expiredQuery = queryExpire.toString();

    //Expira los movimientos con estado Notified que ya cumplieron 2 archivos
    List<PrepaidMovement10> notifiedMovements = this.searchMovementsForExpire(PrepaidMovementStatus.NOTIFIED.toString(), 2);

    String expiredNotifiedQuery = String.format(expiredQuery,getSchema(), PrepaidMovementStatus.NOTIFIED.toString(), getSchema(), 2);
    log.info("Expirando autorizaciones notificadas: " + expiredNotifiedQuery);
    getDbUtils().getJdbcTemplate().execute(expiredNotifiedQuery);

    //Levanta eventos por cada movimiento expirado para notificadas
    this.lauchEventReverse(notifiedMovements);

    //Expira los movimientos con estado Authorized que ya cumplieron 7 archivos
    List<PrepaidMovement10> authorizedMovements = this.searchMovementsForExpire(PrepaidMovementStatus.AUTHORIZED.toString(), 7);

    String expiredAuthorizedQuery = String.format(expiredQuery,getSchema(), PrepaidMovementStatus.AUTHORIZED.toString(), getSchema(), 7);
    log.info("Expirando autorizaciones autorizadas: " + expiredAuthorizedQuery);
    getDbUtils().getJdbcTemplate().execute(expiredAuthorizedQuery);

    //Levanta eventos por cada movimiento expirado para autorizadas
    this.lauchEventReverse(authorizedMovements);

    //Se cierran los estados para accounting y clearing
    this.closingStatusForAccountingAndClearing(authorizedMovements);
  }

  public void lauchEventReverse(List<PrepaidMovement10> movement10s) throws Exception {
    for (PrepaidMovement10 movement: movement10s) {
      TransactionType transactionType = null;
      PrepaidCard10 prepaidCard10 = getPrepaidCardEJB11().getPrepaidCardById(null, movement.getCardId());
      Account account10 = getAccountEJBBean10().findById(prepaidCard10.getAccountId());
      PrepaidUser10 prepaidUser10 = getPrepaidUserEJB10().findById(null, account10.getUserId());
      List<PrepaidMovementFee10> feeList = new ArrayList<PrepaidMovementFee10>();

      if (movement.getTipofac() == TipoFactura.COMPRA_INTERNACIONAL) {
        transactionType = TransactionType.PURCHASE;
      }
      if (movement.getTipofac() == TipoFactura.SUSCRIPCION_INTERNACIONAL) {
        transactionType = TransactionType.SUSCRIPTION;
      }

      publishTransactionReversedEvent(
        prepaidUser10.getUuid(),
        account10.getUuid(),
        prepaidCard10.getUuid(),
        movement,
        feeList,
        transactionType
      );
    }
  }
  @Override
  public List<PrepaidMovement10> getMovementsForConciliate(Map<String, Object> headers) throws Exception {
    String query = String.format("SELECT\n" +
      "      id,\n" +
      "      estado,\n" +
      "      estado_de_negocio,\n" +
      "      estado_con_switch,\n" +
      "      estado_con_tecnocom, \n" +
      "      tipo_movimiento,\n" +
      "      indnorcor,\n" +
      "      fecha_creacion,\n" +
      "      tipofac, \n" +
      "      id_tarjeta\n" +
      "    FROM\n" +
      "     %s.prp_movimiento\n" +
      "    WHERE\n" +
      "      id NOT IN (select id_mov_ref FROM %s.prp_movimiento_conciliado) AND \n"+
      "      estado_con_switch != 'PENDING' AND\n" +
      "      estado_con_tecnocom != 'PENDING' AND\n" +
      "      tipofac != 3003 AND\n" +
      "      tipo_movimiento != 'PURCHASE' AND\n" +
      "      tipo_movimiento != 'SUSCRIPTION'",getSchema(),getSchema());

    return getDbUtils().getJdbcTemplate().query(query, this.getMovementConciliationMapper());
  }

  public List<PrepaidMovement10> searchMovementsForExpire(String movement, int numFiles){
    StringBuilder queryExpire = new StringBuilder();
    queryExpire.append("select * from  %s.prp_movimiento mov " );
    queryExpire.append("WHERE (mov.tipo_movimiento = 'SUSCRIPTION' OR mov.tipo_movimiento = 'PURCHASE') ");
    queryExpire.append("AND mov.estado_con_tecnocom = 'PENDING' ");
    queryExpire.append("AND mov.estado = '%s' ");
    queryExpire.append("AND (SELECT COUNT(f.id) ");
    queryExpire.append("FROM %s.prp_archivos_conciliacion f ");
    queryExpire.append("WHERE f.created_at >= mov.fecha_creacion AND f.tipo = 'TECNOCOM_FILE' AND f.status = 'OK' ) >= %d");
    String expiredQuerySelected = queryExpire.toString();

    //PrepaidMovementStatus.NOTIFIED.toString()
    String expiredQuerySelect = String.format(expiredQuerySelected,getSchema(),movement,getSchema(),numFiles);
    log.info("Buscando autorizaciones " + movement +": " + expiredQuerySelect);

    return getDbUtils().getJdbcTemplate().query(expiredQuerySelect, this.getMovementMapper());
  }

  public void closingStatusForAccountingAndClearing(List<PrepaidMovement10> movement10s) throws Exception {
    for (PrepaidMovement10 movement: movement10s) {
      PrepaidCard10 prepaidCard10 = getPrepaidCardEJB11().getPrepaidCardById(null, movement.getCardId());
      AccountingData10 acc = getPrepaidAccountingEJB10().searchAccountingByIdTrx(null,movement.getId());
      ClearingData10 clearingData10 = getPrepaidClearingEJB10().searchClearingDataByAccountingId(null, acc.getId());

      // Al expirar los movimientos en acc y liq se deben cerrar estos (not_ok y not_send respectivamente)
      getPrepaidAccountingEJB10().updateAccountingStatus(null, acc.getId(), AccountingStatusType.NOT_OK);
      getPrepaidClearingEJB10().updateClearingData(null, clearingData10.getId(), AccountingStatusType.NOT_SEND);

    }
  }
  public PrepaidMovement10 getPrepaidMovementForTecnocomReconciliationV2(Long cardId, String numaut, Date fecfac, TipoFactura tipofac) throws Exception {

    if (cardId == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "cardId"));
    }

    if (numaut == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "numaut"));
    }

    if (fecfac == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "fecfac"));
    }

    if (tipofac == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "tipofac"));
    }

    List<PrepaidMovement10> lst = this.getPrepaidMovements(null, null, null, null, null,
      null, null, null, IndicadorNormalCorrector.fromValue(tipofac.getCorrector()), tipofac, fecfac, numaut, null, null, null, null, null,cardId);

    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }

  @Override
  public PrepaidMovement10 getPrepaidMovementForReverse(Long cardId, String idTxExterno, PrepaidMovementType tipoMovimiento, TipoFactura tipofac) throws Exception {
    if (cardId == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "cardId"));
    }
    if (idTxExterno == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "idTxExterno"));
    }
    if (tipoMovimiento == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "tipoMovimiento"));
    }
    if (tipofac == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "tipofac"));
    }

    List<PrepaidMovement10> lst = this.getPrepaidMovements(null, null, null, idTxExterno, tipoMovimiento,
      null, null, null, IndicadorNormalCorrector.fromValue(tipofac.getCorrector()), tipofac, null, null, null, null, null, null, null,cardId);
    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }


  @Override
  public PrepaidMovement10 getPrepaidMovementForTecnocomReconciliation(Long idPrepaidUser, String numaut, Date fecfac, TipoFactura tipofac) throws Exception {

    if (idPrepaidUser == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "idPrepaidUser"));
    }

    if (numaut == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "numaut"));
    }

    if (fecfac == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "fecfac"));
    }

    if (tipofac == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "tipofac"));
    }

    List<PrepaidMovement10> lst = this.getPrepaidMovements(null, null, idPrepaidUser, null, null,
      null, null, null, null, tipofac, fecfac, numaut, null, null, null, null, null);

    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }



  public void processReconciliation(PrepaidMovement10 mov) throws Exception {
    String messageID = "";
    ResearchMovementInformationFiles researchMovementInformationFiles;
    log.info("Mov to Reconciliation: " + mov);

    /**
     * ID 1 - Movimiento (Carga, Retiro o Reversa)
     *  - Existe en la tabla de movimientos
     *  - Status -> PROCESS_OK
     *  - Existe en archivo Tecnocom
     *  - Existe en archivo Switch
     *
     * Se guarda en tabla de movimientos conciliados con status RECONCILIED
     */
    if (ReconciliationStatusType.RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.RECONCILED.equals(mov.getConSwitch()) &&
      PrepaidMovementStatus.PROCESS_OK.equals(mov.getEstado())) {

      log.debug("XLS ID 1");
      createMovementConciliate(null, mov.getId(), ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);
      updatePrepaidBusinessStatus(null, mov.getId(), BusinessStatusType.OK);

      // Si el moviento es una Carga o Retiro POS, se actualiza informacion en accounting y clearing
      if (TipoFactura.CARGA_TRANSFERENCIA.equals(mov.getTipofac()) ||
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
    else if (ReconciliationStatusType.RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConSwitch()) &&
      PrepaidMovementStatus.PROCESS_OK.equals(mov.getEstado())) {
      log.info("XLS ID 2");
      log.info("Get Prepaid by ID: " + mov.getId());
      PrepaidMovement10 movFull = getPrepaidMovementById(mov.getId());
      log.info(movFull);

      //Se busca usuario prepago para obtener user
      PrepaidCard10 prepaidCard10 = getPrepaidCardEJB11().getPrepaidCardById(null,movFull.getCardId());
      if(prepaidCard10 == null ){
        log.info("PrepaidCard10 NULL ERROR");
      }

      Account account = getAccountEJBBean10().findById(prepaidCard10.getAccountId());
      if (account == null) {
        log.info("account null");
      }

      PrepaidUser10 prepaidUser10 = getPrepaidUserEJB10().findById(null,account.getUserId());
      if(prepaidUser10 == null){
        log.info("prepaidUser10 null");
      }


      updatePrepaidBusinessStatus(null, movFull.getId(), BusinessStatusType.OK);

      /**
       * Carga
       */
      if (PrepaidMovementType.TOPUP.equals(mov.getTipoMovimiento())) {
        /**
         * Si es una carga - Se guarda en tabla de movimientos conciliados con status COUNTER_MOVEMENT y se realiza la reversa del movimiento
         */
        if (IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor())) {

          // Se agrega a movimiento conciliado para que no vuelva a ser enviado.
          createMovementConciliate(null, mov.getId(), ReconciliationActionType.REVERSA_CARGA, ReconciliationStatusType.COUNTER_MOVEMENT);

          // se actualiza informacion en accounting y clearing
          this.updateAccountingStatusReconciliationDateAndClearingStatus(mov.getId(), AccountingStatusType.NOT_OK, AccountingStatusType.NOT_SEND);

          // Se crea movimiento de reversa
          NewPrepaidTopup10 newPrepaidTopup10 = new NewPrepaidTopup10();
          newPrepaidTopup10.setAmount(new NewAmountAndCurrency10(movFull.getMonto()));
          newPrepaidTopup10.setMerchantCategory(movFull.getCodact());
          newPrepaidTopup10.setMerchantCode(movFull.getCodcom());
          newPrepaidTopup10.setMerchantName("Conciliacion");
          newPrepaidTopup10.setTransactionId(movFull.getIdTxExterno());
          // Se envia movimiento a reversar
          getPrepaidEJBBean10().reverseTopupUserBalance(null, prepaidUser10.getUuid(), newPrepaidTopup10, false);
        }
        /**
         * Si es una reversa de carga - Se crea el movimiento contrario
         */
        else {
          // Se agrega a movimiento conciliado para que no vuelva a ser enviado.
          createMovementConciliate(null, mov.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.COUNTER_MOVEMENT);

          NewPrepaidTopup10 newPrepaidTopup10 = new NewPrepaidTopup10();
          newPrepaidTopup10.setAmount(new NewAmountAndCurrency10(movFull.getMonto()));
          newPrepaidTopup10.setMerchantCategory(movFull.getCodact());
          newPrepaidTopup10.setMerchantCode(movFull.getCodcom());
          newPrepaidTopup10.setMerchantName("Conciliacion");
          newPrepaidTopup10.setTransactionId(String.format("MC_%s", movFull.getIdTxExterno()));
          newPrepaidTopup10.setMovementType(PrepaidMovementType.TOPUP);
          newPrepaidTopup10.setFirstTopup(false);

          // Se envia movimiento contrario
          getPrepaidEJBBean10().topupUserBalance(null, prepaidUser10.getUuid(), newPrepaidTopup10, false);
        }
      }
      /**
       * Retiro
       */
      else if (PrepaidMovementType.WITHDRAW.equals(mov.getTipoMovimiento())) {
        /**
         * Si es un retiro - Se guarda en tabla de movimientos conciliados con status COUNTER_MOVEMENT y se realiza la reversa del movimiento
         */
        if (IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor())) {

          // Se agrega a la tabla para que no vuelva a ser enviado
          createMovementConciliate(null, mov.getId(), ReconciliationActionType.REVERSA_RETIRO, ReconciliationStatusType.COUNTER_MOVEMENT);

          // se actualiza informacion en accounting y clearing
          this.updateAccountingStatusReconciliationDateAndClearingStatus(mov.getId(), AccountingStatusType.NOT_OK, AccountingStatusType.NOT_SEND);

          //Se envia a reversar el retiro
          NewPrepaidWithdraw10 newPrepaidWithdraw10 = new NewPrepaidWithdraw10();
          newPrepaidWithdraw10.setTransactionId(movFull.getIdTxExterno());
          newPrepaidWithdraw10.setAmount(new NewAmountAndCurrency10(movFull.getMonto()));
          newPrepaidWithdraw10.setMerchantCode(movFull.getCodcom());
          newPrepaidWithdraw10.setMerchantCategory(movFull.getCodact());
          newPrepaidWithdraw10.setMerchantName("Conciliacion");
          newPrepaidWithdraw10.setPassword("CONCI");
          log.info(newPrepaidWithdraw10);

          getPrepaidEJBBean10().reverseWithdrawUserBalance(null,prepaidUser10.getUuid(), newPrepaidWithdraw10, false);
        }
        /**
         * Si es una reversa de retiro - Se crea el movimiento contrario
         */
        else {
          // Se agrega a movimiento conciliado para que no vuelva a ser enviado.
          createMovementConciliate(null, mov.getId(), ReconciliationActionType.RETIRO, ReconciliationStatusType.COUNTER_MOVEMENT);

          NewPrepaidWithdraw10 newPrepaidWithdraw = new NewPrepaidWithdraw10();
          newPrepaidWithdraw.setAmount(new NewAmountAndCurrency10(movFull.getMonto()));
          newPrepaidWithdraw.setMerchantCategory(movFull.getCodact());
          newPrepaidWithdraw.setMerchantCode(movFull.getCodcom());
          newPrepaidWithdraw.setMerchantName("Conciliacion");
          newPrepaidWithdraw.setTransactionId(String.format("MC_%s", movFull.getIdTxExterno()));
          newPrepaidWithdraw.setMovementType(PrepaidMovementType.TOPUP);

          // Se envia movimiento contrario
          getPrepaidEJBBean10().withdrawUserBalance(null, prepaidUser10.getUuid(), newPrepaidWithdraw,false);

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
    else if (ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.RECONCILED.equals(mov.getConSwitch()) &&
      PrepaidMovementStatus.PROCESS_OK.equals(mov.getEstado())) {
      log.debug("XLS ID 3");
      PrepaidMovement10 movFull = getPrepaidMovementById(mov.getId());

      createReconciliationResearchMovement(movFull, ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_PROCESOR, false, true);
      createMovementConciliate(null, mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);

      if (IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor())) {
        // se actualiza informacion en accounting y clearing
        this.updateAccountingStatusReconciliationDateAndClearingStatus(mov.getId(), AccountingStatusType.RESEARCH, AccountingStatusType.RESEARCH);
      }
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
    else if (ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConSwitch()) &&
      PrepaidMovementStatus.PROCESS_OK.equals(mov.getEstado())) {
      log.debug("XLS ID 4");
      PrepaidMovement10 movFull = getPrepaidMovementById(mov.getId());

      createReconciliationResearchMovement(movFull, ResearchMovementResponsibleStatusType.RECONCILIATION_PREPAID, ResearchMovementDescriptionType.NOT_RECONCILIATION_TO_SWITCH_AND_PROCESOR, true, true);
      createMovementConciliate(null, mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
      if (IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor())) {
        // se actualiza informacion en accounting y clearing
        this.updateAccountingStatusReconciliationDateAndClearingStatus(mov.getId(), AccountingStatusType.RESEARCH, AccountingStatusType.RESEARCH);
      }
    }
    /**
     * ID 5 - Movimiento (Carga o Reversa de carga)
     *  - Existe en la tabla de movimientos
     *  - Status -> ERROR_TECNOCOM_REINTENTABLE, ERROR_TIMEOUT_CONEXION, ERROR_TIMEOUT_RESPONSE
     *  - Existe en archivo Tecnocom
     *  - Existe en archivo Switch
     *
     * Se guarda en tabla de movimientos conciliados con status RECONCILED, se actualiza el status del movimiento a PROCESS_OK, se actualzia en status de negocio a CONFIRMED
     */
    else if (ReconciliationStatusType.RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.RECONCILED.equals(mov.getConSwitch()) &&
      isRetryErrorStatus(mov.getEstado()) &&
      (PrepaidMovementType.TOPUP.equals(mov.getTipoMovimiento()) ||
        PrepaidMovementType.WITHDRAW.equals(mov.getTipoMovimiento()) && IndicadorNormalCorrector.CORRECTORA.equals(mov.getIndnorcor()))
    ) {
      log.debug("XLS ID 5");

      createMovementConciliate(null, mov.getId(), ReconciliationActionType.NONE, ReconciliationStatusType.RECONCILED);
      updatePrepaidMovementStatus(null, mov.getId(), PrepaidMovementStatus.PROCESS_OK);
      updatePrepaidBusinessStatus(null, mov.getId(), BusinessStatusType.OK);

      if (IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor())) {
        // se actualiza informacion en accounting y clearing
        this.updateAccountingStatusReconciliationDateAndClearingStatus(mov.getId(), AccountingStatusType.OK, AccountingStatusType.PENDING);
      } else {
        // Si el movimiento es una reversa, debe actualizar el status de negocio del movimiento original a REVERSED
        PrepaidMovement10 movFull = this.getPrepaidMovementById(mov.getId());
        PrepaidMovement10 originalMovement = getPrepaidMovementForReverse(movFull.getCardId(), movFull.getIdTxExterno(), movFull.getTipoMovimiento(), TipoFactura.valueOfEnumByCodeAndCorrector(movFull.getTipofac().getCode(), IndicadorNormalCorrector.NORMAL.getValue()));
        updatePrepaidBusinessStatus(null, originalMovement.getId(), BusinessStatusType.REVERSED);
      }
    }
    /**
     * ID 6 - Movimiento (Carga o Reversa)
     *  - Existe en la tabla de movimientos
     *  - Status -> ERROR_TECNOCOM_REINTENTABLE, ERROR_TIMEOUT_CONEXION, ERROR_TIMEOUT_RESPONSE
     *  - Existe en archivo Tecnocom
     *  - No existe en archivo Switch
     */
    else if (ReconciliationStatusType.RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConSwitch()) &&
      isRetryErrorStatus(mov.getEstado()) &&
      (PrepaidMovementType.TOPUP.equals(mov.getTipoMovimiento()) ||
        (PrepaidMovementType.WITHDRAW.equals(mov.getTipoMovimiento()) && IndicadorNormalCorrector.CORRECTORA.equals(mov.getIndnorcor())))
    ) {
      log.debug("XLS ID 6");
      // Se obtiene el movimiento completo.--
      PrepaidMovement10 movFull = getPrepaidMovementById(mov.getId());


      PrepaidCard10 prepaidCard10 = getPrepaidCardEJB11().getPrepaidCardById(null,movFull.getCardId());
      if(prepaidCard10 == null ){
        log.info("PrepaidCard10 NULL ERROR");
      }

      Account account = getAccountEJBBean10().findById(prepaidCard10.getAccountId());
      if (account == null) {
        log.info("account null");
      }

      PrepaidUser10 prepaidUser10 = getPrepaidUserEJB10().findById(null,account.getUserId());
      if(prepaidUser10 == null){
        log.info("prepaidUser10 null");
      }

      updatePrepaidMovementStatus(null, mov.getId(), PrepaidMovementStatus.PROCESS_OK);
      updatePrepaidBusinessStatus(null, mov.getId(), BusinessStatusType.OK);

      /**
       * Si es una carga -  Se guarda en tabla de movimientos conciliados con status COUNTER_MOVEMENT y se realiza la reversa del movimiento
       */
      if (IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor())) {

        // Se agrega a la tabla de movimientos conciliados para que no vuelkva a ser enviado
        createMovementConciliate(null, mov.getId(), ReconciliationActionType.REVERSA_CARGA, ReconciliationStatusType.COUNTER_MOVEMENT);

        // se actualiza informacion en accounting y clearing
        this.updateAccountingStatusReconciliationDateAndClearingStatus(mov.getId(), AccountingStatusType.NOT_OK, AccountingStatusType.NOT_SEND);

        // Se crea movimiento de reversa
        NewPrepaidTopup10 newPrepaidTopup10 = new NewPrepaidTopup10();
        newPrepaidTopup10.setAmount(new NewAmountAndCurrency10(movFull.getMonto()));
        newPrepaidTopup10.setMerchantCode(movFull.getCodcom());
        newPrepaidTopup10.setMerchantCategory(movFull.getCodact());
        newPrepaidTopup10.setMerchantName("Conciliacion");
        newPrepaidTopup10.setTransactionId(movFull.getIdTxExterno());
        // Se envia movimiento a reversar
        getPrepaidEJBBean10().reverseTopupUserBalance(null, prepaidUser10.getUuid(), newPrepaidTopup10, false);
      }
      /**
       * Si es una reversa de carga - Se guarda en tabla de movimientos conciliados con status COUNTER_MOVEMENT y se hace el movimiento contrario
       */
      else {

        if (PrepaidMovementType.TOPUP.equals(mov.getTipoMovimiento())) {
          createMovementConciliate(null, mov.getId(), ReconciliationActionType.CARGA, ReconciliationStatusType.COUNTER_MOVEMENT);

          NewPrepaidTopup10 newPrepaidTopup10 = new NewPrepaidTopup10();
          newPrepaidTopup10.setAmount(new NewAmountAndCurrency10(movFull.getMonto()));
          newPrepaidTopup10.setMerchantCategory(movFull.getCodact());
          newPrepaidTopup10.setMerchantCode(movFull.getCodcom());
          newPrepaidTopup10.setMerchantName("Conciliacion");
          newPrepaidTopup10.setTransactionId(String.format("MC_%s", movFull.getIdTxExterno()));
          newPrepaidTopup10.setMovementType(PrepaidMovementType.TOPUP);
          newPrepaidTopup10.setFirstTopup(false);

          // Se envia movimiento contrario
          getPrepaidEJBBean10().topupUserBalance(null, prepaidUser10.getUuid(), newPrepaidTopup10, false);

        } else {
          // Se agrega a movimiento conciliado para que no vuelva a ser enviado.
          createMovementConciliate(null, mov.getId(), ReconciliationActionType.RETIRO, ReconciliationStatusType.COUNTER_MOVEMENT);

          NewPrepaidWithdraw10 newPrepaidWithdraw = new NewPrepaidWithdraw10();
          newPrepaidWithdraw.setAmount(new NewAmountAndCurrency10(movFull.getMonto()));
          newPrepaidWithdraw.setMerchantCategory(movFull.getCodact());
          newPrepaidWithdraw.setMerchantCode(movFull.getCodcom());
          newPrepaidWithdraw.setMerchantName("Conciliacion");
          newPrepaidWithdraw.setTransactionId(String.format("MC_%s", movFull.getIdTxExterno()));
          newPrepaidWithdraw.setMovementType(PrepaidMovementType.TOPUP);

          // Se envia movimiento contrario
          getPrepaidEJBBean10().withdrawUserBalance(null,prepaidUser10.getUuid(), newPrepaidWithdraw,false);

        }
      }
    }
    /**
     * ID 7 - Movimiento (Retiro)
     *  - Existe en la tabla de movimientos
     *  - Status -> ERROR_TIMEOUT_RESPONSE
     *  - Existe en archivo Tecnocom
     *  - No existe en archivo Switch
     */
    else if (ReconciliationStatusType.RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConSwitch()) &&
      PrepaidMovementType.WITHDRAW.equals(mov.getTipoMovimiento()) &&
      IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor()) &&
      isRetryErrorStatus(mov.getEstado())
    ) {
      log.debug("XLS ID 7");
      PrepaidMovement10 movFull = getPrepaidMovementById(mov.getId());


      PrepaidCard10 prepaidCard10 = getPrepaidCardEJB11().getPrepaidCardById(null,movFull.getCardId());
      if(prepaidCard10 == null ){
        log.info("PrepaidCard10 NULL ERROR");
      }

      Account account = getAccountEJBBean10().findById(prepaidCard10.getAccountId());
      if (account == null) {
        log.info("account null");
      }

      PrepaidUser10 prepaidUser10 = getPrepaidUserEJB10().findById(null,account.getUserId());
      if(prepaidUser10 == null){
        log.info("prepaidUser10 null");
      }

      // Confirmar el movimiento original
      updatePrepaidMovementStatus(null, mov.getId(), PrepaidMovementStatus.PROCESS_OK);
      updatePrepaidBusinessStatus(null, mov.getId(), BusinessStatusType.OK);

      // Se agrega a la tabla de movimientos conciliados para que no vuelkva a ser enviado
      createMovementConciliate(null, mov.getId(), ReconciliationActionType.REVERSA_RETIRO, ReconciliationStatusType.COUNTER_MOVEMENT);

      // se actualiza informacion en accounting y clearing
      this.updateAccountingStatusReconciliationDateAndClearingStatus(mov.getId(), AccountingStatusType.NOT_OK, AccountingStatusType.NOT_SEND);

      // Se crea movimiento de reversa
      NewPrepaidWithdraw10 newPrepaidWithdraw10 = new NewPrepaidWithdraw10();
      newPrepaidWithdraw10.setTransactionId(movFull.getIdTxExterno());
      newPrepaidWithdraw10.setAmount(new NewAmountAndCurrency10(movFull.getMonto()));
      newPrepaidWithdraw10.setMerchantCode(movFull.getCodcom());
      newPrepaidWithdraw10.setMerchantCategory(movFull.getCodact());
      newPrepaidWithdraw10.setMerchantName("Conciliacion");
      newPrepaidWithdraw10.setPassword("CONCI");
      log.info(newPrepaidWithdraw10);

      getPrepaidEJBBean10().reverseWithdrawUserBalance(null,prepaidUser10.getUuid(), newPrepaidWithdraw10, false);
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
    else if (ReconciliationStatusType.RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.RECONCILED.equals(mov.getConSwitch()) &&
      PrepaidMovementType.WITHDRAW.equals(mov.getTipoMovimiento()) &&
      IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor()) &&
      isRetryErrorStatus(mov.getEstado())
    ) {
      log.debug("XLS ID 8");
      PrepaidMovement10 movFull = getPrepaidMovementById(mov.getId());

      // Confirmar el movimiento original
      updatePrepaidMovementStatus(null, mov.getId(), PrepaidMovementStatus.PROCESS_OK);
      updatePrepaidBusinessStatus(null, mov.getId(), BusinessStatusType.OK);

      createReconciliationResearchMovement(movFull, ResearchMovementResponsibleStatusType.OTI_PREPAID, ResearchMovementDescriptionType.ERROR_STATUS_IN_DB, false, false);
      createMovementConciliate(null, mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);

      if (IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor())) {
        // se actualiza informacion en accounting y clearing
        this.updateAccountingStatusReconciliationDateAndClearingStatus(mov.getId(), AccountingStatusType.RESEARCH, AccountingStatusType.RESEARCH);
      }
    }
    /**
     * ID 9 - Movimiento (Carga o Reversa)
     *  - Existe en la tabla de movimientos
     *  - Status -> ERROR_TECNOCOM_REINTENTABLE, ERROR_TIMEOUT_CONEXION, ERROR_TIMEOUT_RESPONSE
     *  - NO existe en archivo Tecnocom
     *  - Existe en archivo Switch
     */
    else if (ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.RECONCILED.equals(mov.getConSwitch()) &&
      isRetryErrorStatus(mov.getEstado()) &&
      (PrepaidMovementType.TOPUP.equals(mov.getTipoMovimiento()) ||
        (PrepaidMovementType.WITHDRAW.equals(mov.getTipoMovimiento()) && IndicadorNormalCorrector.CORRECTORA.equals(mov.getIndnorcor())))
    ) {
      log.debug("XLS ID 9");
      PrepaidMovement10 movFull = getPrepaidMovementById(mov.getId());

      //Se busca usuario prepago para obtener user
      PrepaidCard10 prepaidCard10 = getPrepaidCardEJB11().getPrepaidCardById(null,movFull.getCardId());
      if(prepaidCard10 == null ){
        log.info("PrepaidCard10 NULL ERROR");
      }

      Account account = getAccountEJBBean10().findById(prepaidCard10.getAccountId());
      if (account == null) {
        log.info("account null");
      }

      PrepaidUser10 prepaidUser10 = getPrepaidUserEJB10().findById(null,account.getUserId());
      if(prepaidUser10 == null){
        log.info("prepaidUser10 null");
      }

      if (prepaidUser10 == null) {
        log.info("prepaidTopup10 null");
      }

      if (IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor())) {
        PrepaidTopup10 prepaidTopup = new PrepaidTopup10();
        prepaidTopup.setMerchantName(movFull.getNomcomred());
        prepaidTopup.setMerchantName("nomcomred");
        prepaidTopup.setMerchantCode(movFull.getCodcom());
        CdtTransaction10 cdtTransaction = getCdtEJB10().buscaMovimientoByIdExternoAndTransactionType(null, movFull.getIdTxExterno(), prepaidTopup.getCdtTransactionType());

        // Reenviar el movimiento a tecnocom
        getPrepaidEJBBean10().getDelegate().sendTopUp(prepaidTopup, prepaidUser10, cdtTransaction, movFull);
      } else {
        if (PrepaidMovementType.TOPUP.equals(mov.getTipoMovimiento())) {
          PrepaidTopup10 prepaidTopup10 = new PrepaidTopup10();
          prepaidTopup10.setMerchantCode(movFull.getCodcom());
          prepaidTopup10.setTransactionId(movFull.getIdTxExterno());
          prepaidTopup10.setAmount(new NewAmountAndCurrency10(movFull.getMonto(), movFull.getClamon()));

          PrepaidCard10 card = getPrepaidCardEJB11().getLastPrepaidCardByUserIdAndOneOfStatus(null, prepaidUser10.getId(), PrepaidCardStatus.ACTIVE, PrepaidCardStatus.LOCKED);
          getPrepaidEJBBean10().getDelegate().sendPendingTopupReverse(prepaidTopup10, card, prepaidUser10, movFull);
        } else {
          PrepaidWithdraw10 prepaidWithdraw10 = new PrepaidWithdraw10();
          prepaidWithdraw10.setMerchantCode(movFull.getCodcom());
          prepaidWithdraw10.setTransactionId(movFull.getIdTxExterno());
          prepaidWithdraw10.setAmount(new NewAmountAndCurrency10(movFull.getMonto(), movFull.getClamon()));

          getPrepaidEJBBean10().getDelegate().sendPendingWithdrawReversal(prepaidWithdraw10, prepaidUser10, movFull);
        }
      }
    }
    /**
     * ID 10 - Movimiento (Carga o Reversa)
     *  - Existe en la tabla de movimientos
     *  - Status -> ERROR_TECNOCOM_REINTENTABLE, ERROR_TIMEOUT_CONEXION, ERROR_TIMEOUT_RESPONSE
     *  - NO existe en archivo Tecnocom
     *  - NO existe en archivo Switch
     */
    else if (ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConSwitch()) &&
      isRetryErrorStatus(mov.getEstado()) &&
      (PrepaidMovementType.TOPUP.equals(mov.getTipoMovimiento()) ||
        (PrepaidMovementType.WITHDRAW.equals(mov.getTipoMovimiento()) && IndicadorNormalCorrector.CORRECTORA.equals(mov.getIndnorcor())))
    ) {
      log.debug("XLS ID 10");
      PrepaidMovement10 movFull = getPrepaidMovementById(mov.getId());

      // Confirmar el movimiento original
      updatePrepaidMovementStatus(null, mov.getId(), PrepaidMovementStatus.PROCESS_OK);

      createMovementConciliate(null, mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
      createReconciliationResearchMovement(movFull, ResearchMovementResponsibleStatusType.RECONCILIATION_MULTICAJA, ResearchMovementDescriptionType.DESCRIPTION_UNDEFINED, true, true);

      if (IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor())) {
        this.updateAccountingStatusReconciliationDateAndClearingStatus(mov.getId(), AccountingStatusType.RESEARCH, AccountingStatusType.RESEARCH);
      }
    }
    /**
     * ID 11 - Movimiento (Retiro)
     *  - Existe en la tabla de movimientos
     *  - Status -> ERROR_TECNOCOM_REINTENTABLE, ERROR_TIMEOUT_CONEXION, ERROR_TIMEOUT_RESPONSE
     *  - NO existe en archivo Tecnocom
     *  - Existe en archivo Switch
     *
     *  Se guarda en tabla de movimientos conciliados con status NEED_VERIFICATION y se agrega en la tabla de movimientos a investigar
     */
    else if (ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.RECONCILED.equals(mov.getConSwitch()) &&
      PrepaidMovementType.WITHDRAW.equals(mov.getTipoMovimiento()) &&
      IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor()) &&
      isRetryErrorStatus(mov.getEstado())
    ) {
      log.debug("XLS ID 11");
      PrepaidMovement10 movFull = getPrepaidMovementById(mov.getId());

      createReconciliationResearchMovement(movFull, ResearchMovementResponsibleStatusType.RECONCILIATION_MULTICAJA, ResearchMovementDescriptionType.MOVEMENT_REJECTED_IN_AUTHORIZATION, false, true);
      createMovementConciliate(null, mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);

      if (IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor())) {
        // se actualiza informacion en accounting y clearing
        this.updateAccountingStatusReconciliationDateAndClearingStatus(mov.getId(), AccountingStatusType.RESEARCH, AccountingStatusType.RESEARCH);
      }
    }
    /**
     * ID 12 - Movimiento (Retiro)
     *  - Existe en la tabla de movimientos
     *  - Status -> ERROR_TECNOCOM_REINTENTABLE, ERROR_TIMEOUT_CONEXION, ERROR_TIMEOUT_RESPONSE
     *  - NO existe en archivo Tecnocom
     *  - NO existe en archivo Switch
     *
     *  Se guarda en tabla de movimientos conciliados con status NEED_VERIFICATION y se agrega en la tabla de movimientos a investigar
     */
    else if (ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConSwitch()) &&
      PrepaidMovementType.WITHDRAW.equals(mov.getTipoMovimiento()) &&
      IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor()) &&
      isRetryErrorStatus(mov.getEstado())
    ) {
      log.debug("XLS ID 12");

      // Confirmar el movimiento original
      updatePrepaidMovementStatus(null, mov.getId(), PrepaidMovementStatus.NOT_EXECUTED);

      createMovementConciliate(null, mov.getId(), ReconciliationActionType.NONE, ReconciliationStatusType.NOT_RECONCILED);

      if (IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor())) {
        // se actualiza informacion en accounting y clearing
        this.updateAccountingStatusReconciliationDateAndClearingStatus(mov.getId(), AccountingStatusType.NOT_OK, AccountingStatusType.NOT_SEND);
      }
    }
    /**
     * ID 13 - Movimiento (Carga)
     *  - Existe en la tabla de movimientos
     *  - Status -> ERROR_TECNOCOM_REINTENTABLE, ERROR_TIMEOUT_CONEXION, ERROR_TIMEOUT_RESPONSE
     *  - NO existe en archivo Tecnocom
     *  - NO existe en archivo Switch
     */
    else if (ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.RECONCILED.equals(mov.getConSwitch()) &&
      PrepaidMovementStatus.REJECTED.equals(mov.getEstado()) &&
      PrepaidMovementType.TOPUP.equals(mov.getTipoMovimiento()) &&
      IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor())
    ) {
      log.debug("XLS ID 13");
      PrepaidMovement10 movFull = getPrepaidMovementById(mov.getId());

      // Refund
      //Se busca usuario prepago para obtener user
      PrepaidCard10 prepaidCard10 = getPrepaidCardEJB11().getPrepaidCardById(null,movFull.getCardId());
      if(prepaidCard10 == null ){
        log.info("PrepaidCard10 NULL ERROR");
      }

      Account account = getAccountEJBBean10().findById(prepaidCard10.getAccountId());
      if (account == null) {
        log.info("account null");
      }

      PrepaidUser10 prepaidUser10 = getPrepaidUserEJB10().findById(null,account.getUserId());
      if(prepaidUser10 == null){
        log.info("prepaidUser10 null");
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
      template = TemplateUtils.freshDeskTemplateDevolucion(template, String.format("%s %s", prepaidUser10.getName(), prepaidUser10.getDocumentNumber()), String.format("%s", prepaidUser10.getDocumentNumber()), prepaidUser10.getId(), movFull.getNumaut(), movFull.getMonto().longValue(), "",0L);

      NewTicket newTicket = new NewTicket();
      newTicket.setDescription(template);
      newTicket.setGroupId(GroupId.OPERACIONES);
      newTicket.setType(TicketType.DEVOLUCION.getValue());
      newTicket.setStatus(Long.valueOf(StatusType.OPEN.getValue()));
      newTicket.setPriority(Long.valueOf(PriorityType.URGENT.getValue()));
      newTicket.setSubject("Devolucion de carga");
      newTicket.setProductId(43000001595L);
      newTicket.addCustomField("cf_id_movimiento", movFull.getId().toString());

      newTicket.setUniqueExternalId(prepaidUser10.getUuid());
      Ticket ticket = FreshdeskServiceHelper.getInstance().getFreshdeskService().createTicket(newTicket);
      if (ticket != null && ticket.getId() != null) {
        log.info("[processReconciliation][Ticket_Success][ticketId]:"+ticket.getId());
      }else{
        log.info("[processReconciliation][Ticket_Fail][ticketData]:"+newTicket.toString());
      }

    }
    /**
     * ID 14 - Movimiento (Carga)
     *  - Existe en la tabla de movimientos
     *  - Status -> REJECTED
     *  - NO existe en archivo Tecnocom
     *  - NO existe en archivo Switch
     */
    else if (ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConSwitch()) &&
      PrepaidMovementStatus.REJECTED.equals(mov.getEstado()) &&
      PrepaidMovementType.TOPUP.equals(mov.getTipoMovimiento()) &&
      IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor())
    ) {
      log.debug("XLS ID 14");
      PrepaidMovement10 movFull = getPrepaidMovementById(mov.getId());

      createMovementConciliate(null, mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
      createReconciliationResearchMovement(movFull, ResearchMovementResponsibleStatusType.RECONCILIATION_MULTICAJA, ResearchMovementDescriptionType.DESCRIPTION_UNDEFINED, true, true);

      if (IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor())) {
        this.updateAccountingStatusReconciliationDateAndClearingStatus(mov.getId(), AccountingStatusType.RESEARCH, AccountingStatusType.RESEARCH);
      }
    }
    /**
     * ID 15 - Movimiento (Retiro)
     *  - Existe en la tabla de movimientos
     *  - Status -> REJECTED
     *  - NO existe en archivo Tecnocom
     *  - Existe en archivo Switch
     */
    else if (ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.RECONCILED.equals(mov.getConSwitch()) &&
      PrepaidMovementStatus.REJECTED.equals(mov.getEstado()) &&
      PrepaidMovementType.WITHDRAW.equals(mov.getTipoMovimiento()) &&
      IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor())
    ) {
      log.debug("XLS ID 15");
      PrepaidMovement10 movFull = getPrepaidMovementById(mov.getId());

      createMovementConciliate(null, mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
      createReconciliationResearchMovement(movFull, ResearchMovementResponsibleStatusType.RECONCILIATION_MULTICAJA, ResearchMovementDescriptionType.MOVEMENT_REJECTED_IN_AUTHORIZATION, false, true);

      if (IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor())) {
        this.updateAccountingStatusReconciliationDateAndClearingStatus(mov.getId(), AccountingStatusType.RESEARCH, AccountingStatusType.RESEARCH);
      }
    }
    /**
     * ID 16 - Movimiento (Retiro)
     *  - Existe en la tabla de movimientos
     *  - Status -> REJECTED
     *  - NO existe en archivo Tecnocom
     *  - NO existe en archivo Switch
     */
    else if (ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConTecnocom()) &&
      ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConSwitch()) &&
      PrepaidMovementStatus.REJECTED.equals(mov.getEstado()) &&
      PrepaidMovementType.WITHDRAW.equals(mov.getTipoMovimiento()) &&
      IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor())
    ) {
      log.debug("XLS ID 16");

      createMovementConciliate(null, mov.getId(), ReconciliationActionType.NONE, ReconciliationStatusType.NOT_RECONCILED);
      updatePrepaidBusinessStatus(null, mov.getId(), BusinessStatusType.REJECTED);

      // FIXME: esto va? no se hace mencion a los estado clearing/accounting. Revisar con Negocio
      if (IndicadorNormalCorrector.NORMAL.equals(mov.getIndnorcor())) {
        this.updateAccountingStatusReconciliationDateAndClearingStatus(mov.getId(), AccountingStatusType.NOT_OK, AccountingStatusType.NOT_SEND);
      }
    }
    /**
     * ID 20 a 26 - Movimiento (Carga, Retiro o Reversa)
     *  - Existe en la tabla de movimientos
     *  - Status -> PENDING o IN_PROCESS
     *  - SI/NO existe en archivo Tecnocom
     *  - SI/NO existe en archivo Switch
     *
     *  Se guarda en tabla de movimientos conciliados con status NEED_VERIFICATION y se agrega en la tabla de movimientos a investigar
     */
    //Movimientos que esten en estado pendiente o en proceso y vengan en alguno de los archivos Caso 19 al 24
    else if (PrepaidMovementStatus.PENDING.equals(mov.getEstado()) || PrepaidMovementStatus.IN_PROCESS.equals(mov.getEstado())) {
      log.debug("Movimiento Pendiente o En proceso");
      PrepaidMovement10 movFull = getPrepaidMovementById(mov.getId());

      createMovementConciliate(null, mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NEED_VERIFICATION);
      createReconciliationResearchMovement(movFull,
        ResearchMovementResponsibleStatusType.OTI_PREPAID,
        ResearchMovementDescriptionType.ERROR_STATUS_IN_DB,
        ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConSwitch()),
        ReconciliationStatusType.NOT_RECONCILED.equals(mov.getConTecnocom()));


      // Si el moviento es una Carga o Retiro POS, se actualiza informacion en accounting y clearing
      if (TipoFactura.CARGA_TRANSFERENCIA.equals(mov.getTipofac()) ||
        TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA.equals(mov.getTipofac()) ||
        TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA.equals(mov.getTipofac())) {
        updateAccountingStatusReconciliationDateAndClearingStatus(mov.getId(), AccountingStatusType.RESEARCH, AccountingStatusType.RESEARCH);
      }
    }
    /**
     * ID ? - Movimiento (Carga, Retiro o Reversa)
     *  No cae en ninguno de los casos anteriores
     *  Se guarda en tabla de movimientos conciliados con status NO_CASE y se agrega en la tabla de movimientos a investigar
     */
    else {
      log.error("No cae en ningun caso: " + mov);

      List<ResearchMovementInformationFiles> researchMovementInformationFilesList = new ArrayList<>();
      researchMovementInformationFiles = new ResearchMovementInformationFiles();
      researchMovementInformationFilesList.add(researchMovementInformationFiles);
      createResearchMovement(
        null,
        toJson(researchMovementInformationFilesList),
        ReconciliationOriginType.MOTOR.name(),
        mov.getFechaCreacion().toLocalDateTime(),
        ResearchMovementResponsibleStatusType.STATUS_UNDEFINED.getValue(),
        ResearchMovementDescriptionType.DESCRIPTION_UNDEFINED.getValue(),
        mov.getId(),
        mov.getTipoMovimiento().toString(),
        ResearchMovementSentStatusType.SENT_RESEARCH_PENDING.getValue()
      );

      createMovementConciliate(null, mov.getId(), ReconciliationActionType.INVESTIGACION, ReconciliationStatusType.NO_CASE);

      // Si el moviento es una Carga o Retiro POS, se actualiza informacion en accounting y clearing
      if (TipoFactura.CARGA_TRANSFERENCIA.equals(mov.getTipofac()) ||
        TipoFactura.CARGA_EFECTIVO_COMERCIO_MULTICAJA.equals(mov.getTipofac()) ||
        TipoFactura.RETIRO_EFECTIVO_COMERCIO_MULTICJA.equals(mov.getTipofac())) {
        updateAccountingStatusReconciliationDateAndClearingStatus(mov.getId(), AccountingStatusType.RESEARCH, AccountingStatusType.RESEARCH);
      }
    }
  }

  public List<PrepaidMovement10> getPrepaidMovementByCardId(Long cardId) throws Exception {
    if (cardId == null) {
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "cardId"));
    }
    log.info(String.format("[getPrepaidMovementByCardId] cardId: %s",cardId));

    return this.getPrepaidMovements(null, null, null, null, null,
      null, null, null, null, null, null, null,
      null, null, null, null, null, cardId);
  }

  private String getNumAut() {
    String numAut = getDbUtils().getJdbcTemplate().queryForObject(GET_NUMAUT,String.class);
    return numAut;
  }

  public PrepaidMovement10 getLastPrepaidMovementByIdCardIdAndOneStatus(Long cardId, PrepaidMovementStatus... status) throws Exception {
    List<PrepaidMovement10> movements = this.getPrepaidMovementByCardId(cardId);

    log.info(String.format("[getPrepaidMovementByCardId] cardId: %s %s",cardId, Arrays.toString(status)));

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

}
