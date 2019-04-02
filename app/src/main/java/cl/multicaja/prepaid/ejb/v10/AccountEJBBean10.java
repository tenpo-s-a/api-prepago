package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.BadRequestException;
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
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import javax.ejb.*;
import javax.inject.Inject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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

    RowMapper<Account> rm = (ResultSet rs, int rowNum) -> {
      Account a = new Account();
      a.setId(rs.getLong("id"));
      a.setUuid(rs.getString("uuid"));
      a.setUserId(rs.getLong("id_usuario"));
      a.setAccount(rs.getString("cuenta"));
      a.setStatus(rs.getString("estado"));
      a.setBalanceInfo(rs.getString("saldo_info"));
      a.setExpireBalance(rs.getLong("saldo_expiracion"));
      a.setProcessor(rs.getString("procesador"));
      a.setCreatedAt(rs.getObject("creacion", LocalDateTime.class));
      a.setUpdatedAt(rs.getObject("actualizacion", LocalDateTime.class));
      return a;
    };

    return getDbUtils().getJdbcTemplate()
      .queryForObject(FIND_ACCOUNT_BY_ID_SQL, rm, id);
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
      a.setAccount(rs.getString("cuenta"));
      a.setStatus(rs.getString("estado"));
      a.setBalanceInfo(rs.getString("saldo_info"));
      a.setExpireBalance(rs.getLong("saldo_expiracion"));
      a.setProcessor(rs.getString("procesador"));
      a.setCreatedAt(rs.getObject("creacion", LocalDateTime.class));
      a.setUpdatedAt(rs.getObject("actualizacion", LocalDateTime.class));
      return a;
    };

    return getDbUtils().getJdbcTemplate().queryForObject(FIND_ACCOUNT_BY_USERID_SQL, rm, userId);
  }

  public Account insertAccount(Long userId, String accountNumber) throws Exception {

    if(StringUtils.isAllBlank(accountNumber)){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountNumber"));
    }
    if(userId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "userId"));
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
      ps.setObject(7, LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC")));
      ps.setObject(8, LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC")));

      return ps;
    }, keyHolder);

    return  this.findById((long) keyHolder.getKey());
  }

  public void publishAccountCreatedEvent(Long externalUserId, Account acc) throws Exception {
    if(externalUserId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "externalUserId"));
    }

    if(acc == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "account"));
    }

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
}
