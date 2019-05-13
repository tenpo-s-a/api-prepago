package cl.multicaja.prepaid.ejb.v11;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.prepaid.async.v10.KafkaEventDelegate10;
import cl.multicaja.prepaid.ejb.v10.PrepaidMovementEJBBean10;
import cl.multicaja.prepaid.kafka.events.TransactionEvent;
import cl.multicaja.prepaid.kafka.events.model.*;
import cl.multicaja.prepaid.kafka.events.model.Timestamps;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.PrepaidMovementFeeType;
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
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;

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
      ps.setTimestamp(11, Timestamp.valueOf(LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC"))));
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
      ps.setString(26, movement.getNumaut());
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
    query.append("1 = 1");

    log.info(String.format("[getPrepaidMovements] Buscando movimiento [id: %d]", id));

    try {
      return getDbUtils().getJdbcTemplate().query(query.toString(), this.getMovementMapper());
    } catch (EmptyResultDataAccessException ex) {
      log.error(String.format("[getPrepaidMovements] Movimiento con id [%d] no existe", id));
      return null;
    }
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
}
