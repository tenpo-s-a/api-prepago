package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.prepaid.async.v10.KafkaEventDelegate10;
import cl.multicaja.prepaid.kafka.events.AccountEvent;
import cl.multicaja.prepaid.kafka.events.model.Timestamps;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.model.v11.AccountProcessor;
import cl.multicaja.prepaid.model.v11.AccountStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.ejb.*;
import javax.inject.Inject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static cl.multicaja.core.model.Errors.CUENTA_NO_EXISTE;
import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

@Stateless
@LocalBean
@TransactionManagement(value= TransactionManagementType.CONTAINER)
public class AccountEJBBean10 extends PrepaidBaseEJBBean10 {

  private static Log log = LogFactory.getLog(AccountEJBBean10.class);

  private static final String INSERT_ACCOUNT_SQL
    = String.format("INSERT INTO %s.prp_cuenta (id_usuario, cuenta, procesador, saldo_info, saldo_expiracion, estado, creacion, actualizacion) VALUES(?, ?, ?, ?, ?, ?, ?, ?);", getSchema());

  private static final String FIND_ACCOUNT_BY_ID_SQL = String.format("SELECT * FROM %s.prp_cuenta WHERE id = ?", getSchema());

  private static final String FIND_ACCOUNT_BY_USERID_SQL = String.format("SELECT * FROM %s.prp_cuenta WHERE id_usuario = ? ORDER BY creacion DESC LIMIT 1", getSchema());

  private static final String FIND_ACCOUNT_BY_NUMBER_AND_USER_SQL = String.format("SELECT * FROM %s.prp_cuenta WHERE id_usuario = ? AND cuenta = ?", getSchema());

  @Inject
  private KafkaEventDelegate10 kafkaEventDelegate10;


  public KafkaEventDelegate10 getKafkaEventDelegate10() {
    return kafkaEventDelegate10;
  }

  public void setKafkaEventDelegate10(KafkaEventDelegate10 kafkaEventDelegate10) {
    this.kafkaEventDelegate10 = kafkaEventDelegate10;
  }

  public Account findById(Long id) throws Exception {
    if(id == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }

    log.info(String.format("[findById] Buscando cuenta/contrato por id [%d]", id));
    try {
      return getDbUtils().getJdbcTemplate()
        .queryForObject(FIND_ACCOUNT_BY_ID_SQL, this.getAccountMapper(), id);
    } catch (EmptyResultDataAccessException ex) {
      log.error(String.format("[findById]  Cuenta/contrato con id [%d] no existe", id));
      throw new ValidationException(CUENTA_NO_EXISTE);
    }
  }

  public Account findByUserId(Long userId) throws Exception {
    if(userId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "userId"));
    }

    RowMapper<Account> rm = (ResultSet rs, int rowNum) -> {
      Account a = new Account();
      a.setId(rs.getLong("id"));
      a.setUuid(rs.getString("uuid"));
      a.setUserId(rs.getLong("id_usuario"));
      a.setAccountNumber(rs.getString("cuenta"));
      a.setStatus(rs.getString("estado"));
      a.setBalanceInfo(rs.getString("saldo_info"));
      a.setExpireBalance(rs.getLong("saldo_expiracion"));
      a.setProcessor(rs.getString("procesador"));
      a.setCreatedAt(rs.getObject("creacion", LocalDateTime.class));
      a.setUpdatedAt(rs.getObject("actualizacion", LocalDateTime.class));
      return a;
    };
    try{
      return getDbUtils().getJdbcTemplate().queryForObject(FIND_ACCOUNT_BY_USERID_SQL, rm, userId);
    }catch (Exception e){
      return null;
    }

  }

  public Account findByUserIdAndAccountNumber(Long userId, String accountNumber) throws Exception {
    if(userId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "userId"));
    }

    if(StringUtils.isAllBlank(accountNumber)){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountNumber"));
    }

    log.info(String.format("[findByUserIdAndAccountNumber] Buscando cuenta/contrato por -> userId [%d], accountNumber [%s]", userId, accountNumber));
    try {
      return getDbUtils().getJdbcTemplate()
        .queryForObject(FIND_ACCOUNT_BY_NUMBER_AND_USER_SQL, this.getAccountMapper(), userId, accountNumber);
    } catch (EmptyResultDataAccessException ex) {
      log.error(String.format("[findByUserIdAndAccountNumber] Cuenta/contrato con userId [%d] y accountNumber [%s] no existe", userId, accountNumber));
      return null;
    }
  }

  public Account insertAccount(Long userId, String accountNumber) throws Exception {
    if(userId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "userId"));
    }
    if(StringUtils.isAllBlank(accountNumber)){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountNumber"));
    }

    log.info(String.format("[insertAccount] Guardando cuenta/contrato con -> userId [%d], accountNumber [%s]", userId, accountNumber));

    // Se valida si existe la cuenta anteriormente
    Account acc = this.findByUserIdAndAccountNumber(userId, accountNumber);
    if(acc != null) {
      log.info(String.format("[insertAccount] Cuenta/contrato con -> userId [%d], accountNumber [%s] ya existe [id: %d]", userId, accountNumber, acc.getId()));
      return acc;
    }

    KeyHolder keyHolder = new GeneratedKeyHolder();

    getDbUtils().getJdbcTemplate().update(connection -> {
      PreparedStatement ps = connection
        .prepareStatement(INSERT_ACCOUNT_SQL, new String[] {"id"});
      ps.setLong(1, userId);
      ps.setString(2, accountNumber);
      ps.setString(3, AccountProcessor.TECNOCOM_CL.toString());
      ps.setString(4, "");
      ps.setLong(5, 0L);
      ps.setString(6, AccountStatus.ACTIVE.toString());
      ps.setTimestamp(7, Timestamp.from(Instant.now()));
      ps.setTimestamp(8, Timestamp.from(Instant.now()));

      return ps;
    }, keyHolder);
    try{
    return  this.findById((long) keyHolder.getKey());
    }catch (Exception e){
      return null;
    }
  }

  public void publishAccountCreatedEvent(Long externalUserId, Account acc) throws Exception {
    if(externalUserId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "externalUserId"));
    }

    if(acc == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "account"));
    }

    log.info(String.format("[publishAccountCreatedEvent] Publicando evento ACCOUNT_CREATED -> account [userId: %d, id: %d, uuid: %s]", externalUserId, acc.getId(), acc.getUuid()));

    cl.multicaja.prepaid.kafka.events.model.Account account = new cl.multicaja.prepaid.kafka.events.model.Account();
    account.setId(acc.getUuid());
    account.setStatus(acc.getStatus());

    Timestamps timestamps = new Timestamps();
    timestamps.setCreatedAt(acc.getCreatedAt());
    timestamps.setUpdatedAt(acc.getUpdatedAt());
    account.setTimestamps(timestamps);

    AccountEvent accountEvent = new AccountEvent();
    accountEvent.setUserId(externalUserId.toString());
    accountEvent.setAccount(account);

    getKafkaEventDelegate10().publishAccountCreatedEvent(accountEvent);
  }

  private RowMapper<Account> getAccountMapper() {
    return (ResultSet rs, int rowNum) -> {
      Account a = new Account();
      a.setId(rs.getLong("id"));
      a.setUuid(rs.getString("uuid"));
      a.setUserId(rs.getLong("id_usuario"));
      a.setAccountNumber(rs.getString("cuenta"));
      a.setStatus(rs.getString("estado"));
      a.setBalanceInfo(rs.getString("saldo_info"));
      a.setExpireBalance(rs.getLong("saldo_expiracion"));
      a.setProcessor(rs.getString("procesador"));
      a.setCreatedAt(rs.getTimestamp("creacion").toLocalDateTime());
      a.setUpdatedAt(rs.getTimestamp("actualizacion").toLocalDateTime());
      return a;
    };
  }
}
