package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.json.JsonUtils;
import cl.multicaja.prepaid.async.v10.KafkaEventDelegate10;
import cl.multicaja.prepaid.kafka.events.AccountEvent;
import cl.multicaja.prepaid.kafka.events.model.Timestamps;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.model.v11.AccountProcessor;
import cl.multicaja.prepaid.model.v11.AccountStatus;
import cl.multicaja.tecnocom.constants.TipoDocumento;
import cl.multicaja.tecnocom.dto.ConsultaSaldoDTO;
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;

@Stateless
@LocalBean
@TransactionManagement(value= TransactionManagementType.CONTAINER)
public class AccountEJBBean10 extends PrepaidBaseEJBBean10 {

  private static Log log = LogFactory.getLog(AccountEJBBean10.class);

  //TODO: externalizar en parametros o variable de entorno
  public static Integer BALANCE_CACHE_EXPIRATION_MILLISECONDS = 60000;

  private static final String INSERT_ACCOUNT_SQL
    = String.format("INSERT INTO %s.prp_cuenta (id_usuario, cuenta, procesador, saldo_info, saldo_expiracion, estado, creacion, actualizacion) VALUES(?, ?, ?, ?, ?, ?, ?, ?);", getSchema());

  private static final String UPDATE_BALANCE_SQL
    = String.format("UPDATE %s.prp_cuenta SET saldo_info = ?, saldo_expiracion = ? WHERE id = ?", getSchema());

  private static final String FIND_ACCOUNT_BY_ID_SQL = String.format("SELECT * FROM %s.prp_cuenta WHERE id = ?", getSchema());
  
  private static final String FIND_ACCOUNT_BY_UUID_SQL = String.format("SELECT * FROM %s.prp_cuenta WHERE uuid = ?", getSchema());

  private static final String FIND_ACCOUNT_BY_USERID_SQL = String.format("SELECT * FROM %s.prp_cuenta WHERE id_usuario = ? ORDER BY creacion DESC LIMIT 1", getSchema());

  private static final String FIND_ACCOUNT_BY_NUMBER_AND_USER_SQL = String.format("SELECT * FROM %s.prp_cuenta WHERE id_usuario = ? AND cuenta = ?", getSchema());

  @Inject
  private KafkaEventDelegate10 kafkaEventDelegate10;

  @EJB
  private PrepaidUserEJBBean10 prepaidUserEJBBean10;


  public PrepaidUserEJBBean10 getPrepaidUserEJBBean10() {
    return prepaidUserEJBBean10;
  }

  public void setPrepaidUserEJBBean10(PrepaidUserEJBBean10 prepaidUserEJBBean10) {
    this.prepaidUserEJBBean10 = prepaidUserEJBBean10;
  }

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
    log.info(String.format("[findByUserId] Buscando cuenta/contrato por -> userId [%d]", userId));
    try{
      return getDbUtils().getJdbcTemplate().queryForObject(FIND_ACCOUNT_BY_USERID_SQL, this.getAccountMapper(), userId);
    }catch (Exception e){
      log.error(String.format("[findByUserId] Buscando cuenta/contrato por -> userId [%d] no existe", userId));
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
      //log.error(String.format("[findByUserIdAndAccountNumber] Cuenta/contrato con userId [%d] y accountNumber [%s] no existe", userId, accountNumber));
      return null;
    }
  }

  public Account findByUuid(String uuid) throws Exception {
    if(StringUtils.isAllBlank(uuid)){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "uuid"));
    }

    log.info(String.format("[findByUuid] Buscando cuenta/contrato por -> uuid [%s]", uuid));
    try {
      return getDbUtils().getJdbcTemplate()
        .queryForObject(FIND_ACCOUNT_BY_UUID_SQL, this.getAccountMapper(), uuid);
    } catch (EmptyResultDataAccessException ex) {
      //log.error(String.format("[findByUserIdAndAccountNumber] Cuenta/contrato con userId [%d] y accountNumber [%s] no existe", userId, accountNumber));
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

  /**
   * Retorna el saldo de la cuenta
   *
   * @param headers
   * @param accountId id de la cuenta
   * @return
   */
  public PrepaidBalance10 getBalance(Map<String, Object> headers, Long accountId) throws Exception {

    if(accountId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountId"));
    }

    Account account = this.findById(accountId);

    if(account == null){
      throw new NotFoundException(CUENTA_NO_EXISTE);
    }

    PrepaidUser10 prepaidUser = getPrepaidUserEJBBean10().findById(null, account.getUserId());

    if(prepaidUser == null){
      throw new NotFoundException(CLIENTE_NO_TIENE_PREPAGO);
    }

    return this.getBalance(headers, prepaidUser, account);
  }

  public PrepaidBalance10 getBalance(Map<String, Object> headers, PrepaidUser10 prepaidUser, Account account) throws Exception {

    if(prepaidUser == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "prepaidUser"));
    }

    if(StringUtils.isAllBlank(prepaidUser.getDocumentNumber())){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "prepaidUser.documentNumber"));
    }

    if(account == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "account"));
    }

    if(account.getId() == null || account.getId().equals(0L)){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "account.id"));
    }

    if(StringUtils.isAllBlank(account.getAccountNumber())){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "account.accountNumber"));
    }

    //permite refrescar el saldo del usuario de forma obligada, usado principalmente en test o podria usarse desde la web
    Boolean forceRefreshBalance = headers != null ? getNumberUtils().toBoolean(headers.get("forceRefreshBalance"), Boolean.FALSE) : Boolean.FALSE;

    Long balanceExpiration = account.getExpireBalance();

    PrepaidBalanceInfo10 pBalance = null;

    if(!StringUtils.isAllBlank(account.getBalanceInfo())) {
      try {
        pBalance = JsonUtils.getJsonParser().fromJson(account.getBalanceInfo(), PrepaidBalanceInfo10.class);
      } catch(Exception ex) {
        log.error("[getBalance] Error al convertir el saldo de la cuenta [id: %d]", ex);
      }
    }

    Boolean updated = Boolean.FALSE;

    //solamente si el usuario no tiene saldo registrado o se encuentra expirado, se busca en tecnocom
    if (pBalance == null || balanceExpiration <= 0 || Instant.now().toEpochMilli() >= balanceExpiration || forceRefreshBalance) {

      ConsultaSaldoDTO consultaSaldoDTO = getTecnocomService().consultaSaldo(account.getAccountNumber(), prepaidUser.getDocumentNumber(), TipoDocumento.RUT);

      if (consultaSaldoDTO != null && consultaSaldoDTO.isRetornoExitoso()) {
        log.error("[getBalance] Respuesta del WS ConsultaSaldo [isRetornoExitoso: TRUE]");
        pBalance = new PrepaidBalanceInfo10(consultaSaldoDTO);
        try {
          this.updateBalance(account.getId(), pBalance);
          updated = Boolean.TRUE;
        } catch(Exception ex) {
          log.error(String.format("[getBalance] Error al actualizar el saldo de la cuenta [id: %d]", account.getId()), ex);
        }
      } else {
        log.error("[getBalance] Respuesta del WS ConsultaSaldo [isRetornoExitoso: FALSE]");
        String codErrorTecnocom = consultaSaldoDTO != null ? consultaSaldoDTO.getRetorno() : null;
        throw new ValidationException(SALDO_NO_DISPONIBLE_$VALUE).setData(new KeyValue("value", codErrorTecnocom));
      }
    }

    //por defecto debe ser 0
    BigDecimal balanceValue = BigDecimal.valueOf(0L);

    if (pBalance != null) {
      //El que le mostraremos al cliente será el saldo dispuesto principal menos el saldo autorizado principal
      balanceValue = BigDecimal.valueOf(pBalance.getSaldisconp().longValue() - pBalance.getSalautconp().longValue());
    }

    if(balanceValue.compareTo(BigDecimal.ZERO) < 0) {
      balanceValue = balanceValue.multiply(BigDecimal.valueOf(-1));
    }

    NewAmountAndCurrency10 balance = new NewAmountAndCurrency10(balanceValue);
    NewAmountAndCurrency10 pcaMain = getCalculationsHelper().calculatePcaMain(balance);
    NewAmountAndCurrency10 pcaSecondary = getCalculationsHelper().calculatePcaSecondary(balance, pcaMain);

    //TODO: debe ser el valor de venta o el valor del día?.
    return new PrepaidBalance10(balance, pcaMain, pcaSecondary, getCalculationsHelper().getUsdValue().intValue(), updated);
  }

  /**
   * Actualiza el saldo de la cuenta
   *
   * @param accountId id de la cuenta
   * @param balance informacion del saldo
   * @throws Exception
   */
  public void updateBalance(Long accountId, PrepaidBalanceInfo10 balance) throws Exception {

    if(accountId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountId"));
    }
    if(balance == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "balance"));
    }

    log.error(String.format("[updateBalance] Actualizando saldo de la cuenta [id: %d]", accountId));

    //expira en X minutos
    Long balanceExpiration = Instant.now()
      .plusMillis(BALANCE_CACHE_EXPIRATION_MILLISECONDS)
      .toEpochMilli();

    int rows = getDbUtils().getJdbcTemplate().update(connection -> {
      PreparedStatement ps = connection
        .prepareStatement(UPDATE_BALANCE_SQL);
      ps.setString(1, JsonUtils.getJsonParser().toJson(balance));
      ps.setLong(2, balanceExpiration);
      ps.setLong(3, accountId);

      return ps;
    });

    if(rows == 0) {
      log.error(String.format("[updateBalance] Error al actualizar el saldo de la cuenta [id: %d]", accountId));
      throw new Exception("No se pudo actualizar el saldo");
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
