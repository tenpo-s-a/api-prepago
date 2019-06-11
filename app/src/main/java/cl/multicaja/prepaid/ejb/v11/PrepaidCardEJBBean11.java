package cl.multicaja.prepaid.ejb.v11;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.prepaid.async.v10.KafkaEventDelegate10;
import cl.multicaja.prepaid.async.v10.routes.KafkaEventsRoute10;
import cl.multicaja.prepaid.ejb.v10.*;
import cl.multicaja.prepaid.kafka.events.CardEvent;
import cl.multicaja.prepaid.kafka.events.model.Card;
import cl.multicaja.prepaid.model.v10.*;
import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.utils.AESEncryptCardUtilImpl;
import cl.multicaja.prepaid.utils.AzureEncryptCardUtilImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.ejb.*;
import javax.inject.Inject;
import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;

@Stateless
@LocalBean
@TransactionManagement(value= TransactionManagementType.CONTAINER)
public class PrepaidCardEJBBean11 extends PrepaidBaseEJBBean10 implements PrepaidCardEJB11 {

  private static Log log = LogFactory.getLog(PrepaidCardEJBBean11.class);

  @EJB
  private PrepaidUserEJBBean10 prepaidUserEJBBean10;

  @EJB
  private AccountEJBBean10 accountEJBBean10;

  private AESEncryptCardUtilImpl aesEncryptCardUtil;

  private AzureEncryptCardUtilImpl azureEncryptCardUtil;

  @Inject
  private KafkaEventDelegate10 kafkaEventDelegate10;

  private AESEncryptCardUtilImpl aesEncryptCardUtil;

  private AzureEncryptCardUtilImpl azureEncryptCardUtil;

  private static final String FIND_CARD = "SELECT * FROM %s.prp_tarjeta WHERE %s ORDER BY fecha_creacion DESC";
  private static final String FIND_LAST_CARD = "SELECT * FROM %s.prp_tarjeta WHERE %s ORDER BY fecha_creacion DESC LIMIT 1";
  private static final String FIND_CARD_BY_ID_SQL = String.format("SELECT * FROM %s.prp_tarjeta WHERE id = ?", getSchema());
  private static final String UPDATE_CARD_BY_ID_SQL = "UPDATE %s.prp_tarjeta SET %s WHERE id = ? %s";
  private static final String FIND_CARD_BY_USERID_STATUS = "SELECT \n" +
    "  t.id  as id,\n" +
    "  t.pan as pan,\n" +
    "  t.pan_encriptado as pan_encriptado,\n" +
    "  t.estado as estado,\n" +
    "  t.nombre_tarjeta as nombre_tarjeta,\n" +
    "  t.producto as producto,\n" +
    "  t.numero_unico as numero_unico,\n" +
    "  t.fecha_creacion as fecha_creacion,\n" +
    "  t.fecha_actualizacion as fecha_actualizacion,\n" +
    "  t.uuid as uuid,\n" +
    "  t.pan_hash as pan_hash,\n" +
    "  t.id_cuenta as id_cuenta,\n" +
    "  t.expiracion as expiracion\n"+
    "FROM \n" +
    "  %s.prp_tarjeta t\n" +
    "INNER JOIN %s.prp_cuenta c ON t.id_cuenta = c.id\n" +
    "INNER JOIN %s.prp_usuario u on c.id_usuario = u.id\n" +
    "WHERE\n" +
    " u.id = ? AND\n" +
    " (%s)";


  private static final String FIND_INVALID_CARD_BY_USERID = String.format("SELECT \n" +
    "  t.id  as id,\n" +
    "  t.pan as pan,\n" +
    "  t.pan_encriptado as pan_encriptado,\n" +
    "  t.estado as estado,\n" +
    "  t.nombre_tarjeta as nombre_tarjeta,\n" +
    "  t.producto as producto,\n" +
    "  t.numero_unico as numero_unico,\n" +
    "  t.fecha_creacion as fecha_creacion,\n" +
    "  t.fecha_actualizacion as fecha_actualizacion,\n" +
    "  t.uuid as uuid,\n" +
    "  t.pan_hash as pan_hash,\n" +
    "  t.id_cuenta as id_cuenta,\n" +
    "  t.expiracion as expiracion\n"+
    "FROM \n" +
    "  %s.prp_tarjeta t\n" +
    "INNER JOIN %s.prp_cuenta c ON t.id_cuenta = c.id\n" +
    "INNER JOIN %s.prp_usuario u on c.id_usuario = u.id\n" +
    "WHERE\n" +
    " u.id = ? AND\n" +
    "( t.estado = 'LOCKED_HARD' OR \n" +
    " t.estado = 'EXPIRED' OR \n" +
    " t.estado = 'PENDING' )", getSchema(), getSchema(), getSchema());

  private static final String FIND_CARD_BY_PAN_USERID = String.format("SELECT \n" +
    "  t.id  as id,\n" +
    "  t.pan as pan,\n" +
    "  t.pan_encriptado as pan_encriptado,\n" +
    "  t.estado as estado,\n" +
    "  t.nombre_tarjeta as nombre_tarjeta,\n" +
    "  t.producto as producto,\n" +
    "  t.numero_unico as numero_unico,\n" +
    "  t.fecha_creacion as fecha_creacion,\n" +
    "  t.fecha_actualizacion as fecha_actualizacion,\n" +
    "  t.uuid as uuid,\n" +
    "  t.pan_hash as pan_hash,\n" +
    "  t.id_cuenta as id_cuenta,\n" +
    "  t.expiracion as expiracion\n"+
    "FROM \n" +
    "  %s.prp_tarjeta t\n" +
    "  INNER JOIN %s.prp_cuenta c ON t.id_cuenta = c.id\n" +
    "  INNER JOIN %s.prp_usuario u on c.id_usuario = u.id\n" +
    "WHERE\n" +
    "  u.id = ? AND\n" +
    "  t.pan = ?", getSchema(), getSchema(), getSchema());

  private static final String FIND_BY_PAN_ACCOUNTNUMBER = String.format("SELECT \n" +
    "  t.id  as id,\n" +
    "  t.pan as pan,\n" +
    "  t.pan_encriptado as pan_encriptado,\n" +
    "  t.estado as estado,\n" +
    "  t.nombre_tarjeta as nombre_tarjeta,\n" +
    "  t.producto as producto,\n" +
    "  t.numero_unico as numero_unico,\n" +
    "  t.fecha_creacion as fecha_creacion,\n" +
    "  t.fecha_actualizacion as fecha_actualizacion,\n" +
    "  t.uuid as uuid,\n" +
    "  t.pan_hash as pan_hash,\n" +
    "  t.id_cuenta as id_cuenta,\n" +
    "  t.expiracion as expiracion\n"+
    " FROM %s.prp_tarjeta t\n"+
    "   INNER JOIN %s.prp_cuenta c ON t.id_cuenta = c.id\n" +
    "   INNER JOIN %s.prp_usuario u on c.id_usuario = u.id\n" +
    " WHERE\n" +
    "  t.pan = ? AND\n" +
    "  c.cuenta = ?",getSchema(),getSchema(),getSchema());

  private static final String FIND_BY_PAN_HASH_AND_ACCOUNTNUMBER = String.format("SELECT \n" +
    " t.id  as id,\n" +
    " t.pan as pan,\n" +
    " t.pan_encriptado as pan_encriptado,\n" +
    " t.estado as estado,\n" +
    " t.nombre_tarjeta as nombre_tarjeta,\n" +
    " t.producto as producto,\n" +
    " t.numero_unico as numero_unico,\n" +
    " t.fecha_creacion as fecha_creacion,\n" +
    " t.fecha_actualizacion as fecha_actualizacion,\n" +
    " t.uuid as uuid,\n" +
    " t.pan_hash as pan_hash,\n" +
    " t.id_cuenta as id_cuenta,\n" +
    " t.expiracion as expiracion\n"+
    "FROM %s.prp_tarjeta t\n"+
    "INNER JOIN %s.prp_cuenta c ON t.id_cuenta = c.id\n" +
    "WHERE\n" +
    "  t.pan_hash = ? AND\n" +
    "  c.cuenta = ?",getSchema(),getSchema(),getSchema());

  private static String UPDATE_PREPAID_CARD_STATUS = String.format("UPDATE %s.prp_tarjeta SET estado = ? where id = ?",getSchema());

  private static String INSERT_PREPAID_CARD = "INSERT INTO prepago.prp_tarjeta(\n" +
    "            pan, pan_encriptado, estado, \n" +
    "            nombre_tarjeta, producto, numero_unico, fecha_creacion, fecha_actualizacion, \n" +
    "            uuid, pan_hash, id_cuenta,expiracion)\n" +
    "    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

  private static String SEARCH_BY_ACCOUNT_ID = String.format("SELECT * FROM %s.prp_tarjeta where id_cuenta = ?",getSchema());

  public PrepaidCardEJBBean11() {
    super();
  }

  public PrepaidUserEJBBean10 getPrepaidUserEJBBean10() {
    return prepaidUserEJBBean10;
  }

  public void setPrepaidUserEJBBean10(PrepaidUserEJBBean10 prepaidUserEJBBean10) {
    this.prepaidUserEJBBean10 = prepaidUserEJBBean10;
  }

  public AccountEJBBean10 getAccountEJBBean10() {
    return accountEJBBean10;
  }

  public void setAccountEJBBean10(AccountEJBBean10 accountEJBBean10) {
    this.accountEJBBean10 = accountEJBBean10;
  }

  public KafkaEventDelegate10 getKafkaEventDelegate10() {
    return kafkaEventDelegate10;
  }

  public void setKafkaEventDelegate10(KafkaEventDelegate10 kafkaEventDelegate10) {
    this.kafkaEventDelegate10 = kafkaEventDelegate10;
  }

  public AESEncryptCardUtilImpl getAesEncryptCardUtil() {
    if(aesEncryptCardUtil == null){
      aesEncryptCardUtil = AESEncryptCardUtilImpl.getInstance();
    }
    return aesEncryptCardUtil;
  }

  public AzureEncryptCardUtilImpl getAzureEncryptCardUtil() {
    if(azureEncryptCardUtil == null){
      azureEncryptCardUtil = AzureEncryptCardUtilImpl.getInstance();
    }
    return azureEncryptCardUtil;
  }

  public PrepaidCard10 updatePrepaidCardStatus(Long cardId, PrepaidCardStatus status) throws Exception {

      if(cardId == null){
        throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "cardId"));
      }
      if(status == null){
        throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "status"));
      }

      log.info(String.format("[updateBalance] Actualizando tarjeta  [cardId: %d][status: %s]", cardId,status));

      int rows = getDbUtils().getJdbcTemplate().update(connection -> {
        PreparedStatement ps = connection
          .prepareStatement(UPDATE_PREPAID_CARD_STATUS);
        ps.setString(1, status.name());
        ps.setLong(2, cardId);
        return ps;
      });

      if(rows == 0) {
        log.error(String.format("[updateBalance] Error Actualizando tarjeta  [cardId: %d][status: %s]", cardId,status));
        throw new Exception("No se pudo actualizar el saldo");
      }
      log.error(String.format("[updateBalance] Actualizando tarjeta  [cardId: %d][status: %s] Reg:", cardId,status,rows));

      return this.getPrepaidCardById(null,cardId);
  }

  @Override
  public PrepaidCard10 createPrepaidCard(Map<String, Object> headers, PrepaidCard10 prepaidCard10) throws Exception {
    if(prepaidCard10 == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "prepaidCard10"));
    }

    log.info(String.format("[insertPrepaidCard] Guardando tarjeta "));

    KeyHolder keyHolder = new GeneratedKeyHolder();

    getDbUtils().getJdbcTemplate().update(connection -> {
      PreparedStatement ps = connection
        .prepareStatement(INSERT_PREPAID_CARD, new String[] {"id"});
      ps.setString(1, !StringUtils.isAllBlank(prepaidCard10.getPan()) ? prepaidCard10.getPan() : ""); //pan
      ps.setString(2, !StringUtils.isAllBlank(prepaidCard10.getEncryptedPan()) ? prepaidCard10.getEncryptedPan() : ""); //pan_encriptado
      ps.setString(3, prepaidCard10.getStatus().name()); //estado
      ps.setString(4, !StringUtils.isAllBlank(prepaidCard10.getNameOnCard()) ? prepaidCard10.getNameOnCard() : ""); //nombre_tarjeta
      ps.setString(5, !StringUtils.isAllBlank(prepaidCard10.getProducto()) ? prepaidCard10.getProducto() : ""); //producto
      ps.setString(6, !StringUtils.isAllBlank(prepaidCard10.getNumeroUnico()) ? prepaidCard10.getNumeroUnico() : ""); //numero_unico
      ps.setTimestamp(7,Timestamp.valueOf(LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC")))); //fecha_creacion
      ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.ofInstant(Instant.now(), ZoneId.of("UTC")))); //fecha_actualizacion
      ps.setString(9, !StringUtils.isAllBlank(prepaidCard10.getUuid()) ? prepaidCard10.getUuid() : ""); //uuid
      ps.setString(10, !StringUtils.isAllBlank(prepaidCard10.getHashedPan()) ? prepaidCard10.getHashedPan() : ""); //pan_hash
      ps.setLong(11, prepaidCard10.getAccountId()); //id_cuenta
      ps.setInt(12, prepaidCard10.getExpiration() != null ? prepaidCard10.getExpiration() : 0); //expiracion
      return ps;
    }, keyHolder);
    try{
      return  this.getPrepaidCardById(headers,(long) keyHolder.getKey());
    }catch (Exception e){
      return null;
    }
  }

  public List<PrepaidCard10> getPrepaidCards(Map<String, Object> headers, Long id, Long accountId, Integer expiration, String panHash,String encryptedPan, PrepaidCardStatus status) throws ValidationException {

    StringBuilder where = new StringBuilder();

    if(id != null){
      where.append(String.format(" id = %d AND",id));
    }
    if(accountId != null){
      where.append(String.format("  id_cuenta = %d AND", accountId));
    }
    if(expiration != null){
      where.append(String.format(" expiracion = %d AND", expiration));
    }
    if(panHash != null){
      where.append(String.format(" pan_hash = '%s' AND", panHash));
    }
    if(status != null){
      where.append(String.format(" estado = '%s' AND", status.name()));
    }
    if(encryptedPan != null){
      where.append(String.format(" pan_encriptado = '%s' AND", encryptedPan));
    }
    where.append(" 1 = 1");
    log.debug(String.format(FIND_CARD,getSchema(), where.toString()));
    try {
      return getDbUtils().getJdbcTemplate().query(String.format(FIND_CARD,getSchema(), where.toString()), getCardMapper());
    } catch (EmptyResultDataAccessException ex) {
      log.error(String.format("[getPrepaidCards] Tarjeta [id: %d] no existe", id));
      throw new ValidationException(TARJETA_NO_EXISTE);
    }catch (Exception e){
      e.printStackTrace();
      return null;
    }

  }

  public PrepaidCard10 getPrepaidCardByEncryptedPan(Map<String, Object> headers, String encryptedPan) throws Exception{
    List<PrepaidCard10> card10s = this.getPrepaidCards(null,null,null,null,null,encryptedPan,null);
    return card10s.size() != 0 ? card10s.get(0) : null;
  }

  @Override
  public List<PrepaidCard10> getPrepaidCards(Map<String, Object> headers, Long id, Long accountId, Integer expiration, PrepaidCardStatus status) throws Exception {
    return this.getPrepaidCards(headers,id,accountId,expiration,null,null, status);
  }

  @Override
  public PrepaidCard10 getPrepaidCardById(Map<String, Object> headers, Long id) throws Exception {
    if(id == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }
    log.info(String.format("[getPrepaidCardById] Buscando tarjeta [id: %d]", id));
    try {
      return getDbUtils().getJdbcTemplate().queryForObject(FIND_CARD_BY_ID_SQL, getCardMapper(), id);
    } catch (EmptyResultDataAccessException ex) {
      log.error(String.format("[getPrepaidCardById] Tarjeta [id: %d] no existe", id));
      throw new ValidationException(TARJETA_NO_EXISTE);
    }
  }

  @Override
  public PrepaidCard10 getLastPrepaidCardByAccountIdAndStatus(Map<String, Object> headers, Long accountId, PrepaidCardStatus status) throws Exception {
    if(accountId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountId"));
    }
    if(status == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "status"));
    }
    log.info(String.format("[getLastPrepaidCardByAccountIdAndStatus] Buscando tarjeta [accountId: %d]", accountId));

    StringBuilder where = new StringBuilder();
    where.append(String.format(" id_cuenta = %d ",accountId));

    try {
      PrepaidCard10 card =  getDbUtils().getJdbcTemplate().queryForObject(String.format(FIND_LAST_CARD, getSchema(), where), getCardMapper());
      if(card != null && card.getStatus() == status){
        return card;
      }else {
        return null;
      }
    } catch (EmptyResultDataAccessException ex) {
      log.error(String.format("[getLastPrepaidCardByAccountIdAndStatus] Tarjeta [accountId: %d] no existe", accountId));
     return null;
    }
  }

  @Override
  public PrepaidCard10 getLastPrepaidCardByAccountIdAndOneOfStatus(Map<String, Object> headers, Long accountId, PrepaidCardStatus... status) throws Exception {
    if(accountId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountId"));
    }
    if(status == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "status"));
    }
    log.info(String.format("[getLastPrepaidCardByAccountIdAndStatus] Buscando tarjeta [accountId: %d]", accountId));

    StringBuilder where = new StringBuilder();
    where.append(String.format(" id_cuenta = %d AND",accountId));
    where.append(" ( ");
    for(PrepaidCardStatus state : status){
      where.append(String.format(" estado = '%s' OR",state.name()));
    }
    where.append(" 1 = 1 ");
    where.append(" ) ");

    try {
      return getDbUtils().getJdbcTemplate().queryForObject(String.format(FIND_LAST_CARD,getSchema(),where), getCardMapper());
    } catch (EmptyResultDataAccessException ex) {
      log.error(String.format("[getLastPrepaidCardByAccountIdAndStatus] Tarjeta [accountId: %d] no existe", accountId));
      return null;
    }
  }

  @Override
  public PrepaidCard10 getLastPrepaidCardByAccountId(Map<String, Object> headers, Long accountId) throws Exception {
    if(accountId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountId"));
    }
    log.info(String.format("[getLastPrepaidCardByAccountId] Buscando tarjeta [accountId: %d]", accountId));

    StringBuilder where = new StringBuilder();
    where.append(String.format(" id_cuenta = %d ",accountId));

    try {
      return getDbUtils().getJdbcTemplate().queryForObject(String.format(FIND_LAST_CARD,getSchema(), where), getCardMapper());
    } catch (EmptyResultDataAccessException ex) {
      log.error(String.format("[getLastPrepaidCardByAccountId] Tarjeta [accountId: %d] no existe", accountId));
      return null;
    }
  }

  @Override
  public void updatePrepaidCardStatus(Map<String, Object> headers, Long id, PrepaidCardStatus status) throws Exception {

    if(id == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountId"));
    }
    if(status == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "status"));
    }
    log.info(String.format("[updatePrepaidCardStatus] Actualizando Tarjeta [id: %d]", id));

    PrepaidCard10 prepaidCard10 = new PrepaidCard10();
    prepaidCard10.setStatus(status);
    updatePrepaidCard(headers,id,null,prepaidCard10);

    log.info(String.format("[updatePrepaidCardStatus] Tarjeta Actualizada [id: %d]", id));

  }

  @Override
  public PrepaidCard10 getPrepaidCardByPanAndProcessorUserId(Map<String, Object> headers, String pan, String processorUserId) {
    log.info(String.format("[getPrepaidCardById] Buscando tarjeta [pan: %s] [processorUserId: %s]", pan,processorUserId));
    try {
      return getDbUtils().getJdbcTemplate().queryForObject(FIND_BY_PAN_ACCOUNTNUMBER, getCardMapper(), pan,processorUserId);
    } catch (EmptyResultDataAccessException ex) {
      log.error(String.format("[getPrepaidCardById] Tarjeta [pan: %s] [processorUserId: %s]", pan,processorUserId));
      return null;
    }
  }

  @Override
  public PrepaidCard10 getPrepaidCardByPanHashAndAccountNumber(Map<String, Object> headers, String panHash, String accountNumber) {
    log.info(String.format("[getPrepaidCardByPanHash] Buscando tarjeta [panHash: %s] [account: %s]", panHash, accountNumber));
    try {
      return getDbUtils().getJdbcTemplate().queryForObject(FIND_BY_PAN_HASH_AND_ACCOUNTNUMBER, getCardMapper(), panHash, accountNumber);
    } catch (EmptyResultDataAccessException ex) {
      log.error(String.format("[getPrepaidCardByPanHash] Tarjeta [panHash: %s] [account: %s]", panHash, accountNumber));
      return null;
    }
  }

  public void updatePrepaidCard(Map<String, Object> headers, Long cardId, Long accountId, PrepaidCard10 prepaidCard) throws Exception {

    if(cardId == null){
      throw new ValidationException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }

    if(prepaidCard == null){
      throw new ValidationException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "prepaidCard"));
    }

    this.updatePrepaidCard(headers, cardId, accountId, null,  prepaidCard);
  }

  @Override
  public void updatePrepaidCard(Map<String, Object> headers, Long cardId, Long accountId,PrepaidCardStatus status, PrepaidCard10 prepaidCard) throws Exception {


    if(cardId == null){
      throw new ValidationException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }

    if(prepaidCard == null){
      throw new ValidationException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "prepaidCard"));
    }

    log.info(String.format("[updatePrepaidCard] Actualizando tarjeta [id: %d]", cardId));

    StringBuilder sb = new StringBuilder();
    if(!StringUtils.isAllBlank(prepaidCard.getPan())) {
      sb.append("pan = '")
        .append(prepaidCard.getPan())
        .append("', ");
    }
    if(!StringUtils.isAllBlank(prepaidCard.getEncryptedPan())) {
      sb.append("pan_encriptado = '")
        .append(prepaidCard.getEncryptedPan())
        .append("', ");
    }

    if(prepaidCard.getExpiration() != null && prepaidCard.getExpiration() > 0) {
      sb.append("expiracion = ")
        .append(prepaidCard.getExpiration())
        .append(", ");
    }
    if(prepaidCard.getStatus() != null) {
      sb.append("estado = '")
        .append(prepaidCard.getStatus().toString())
        .append("', ");
    }
    if(!StringUtils.isAllBlank(prepaidCard.getNameOnCard())) {
      sb.append("nombre_tarjeta = '")
        .append(prepaidCard.getNameOnCard())
        .append("', ");
    }
    if(!StringUtils.isAllBlank(prepaidCard.getProducto())) {
      sb.append("producto = '")
        .append(prepaidCard.getProducto())
        .append("', ");
    }
    if(!StringUtils.isAllBlank(prepaidCard.getNumeroUnico())) {
      sb.append("numero_unico = '")
        .append(prepaidCard.getNumeroUnico())
        .append("', ");
    }
    if(!StringUtils.isAllBlank(prepaidCard.getHashedPan())) {
      sb.append("pan_hash = '")
        .append(prepaidCard.getHashedPan())
        .append("', ");
    }

    sb.append("fecha_actualizacion = timezone('utc', now())");

    int resp = 0;
    if(status == null){
      resp = getDbUtils().getJdbcTemplate().update(String.format(UPDATE_CARD_BY_ID_SQL, getSchema(), sb.toString(),""), cardId);
    }
    else{
      String where = String.format(" AND estado = '%s'",status.name());
      resp = getDbUtils().getJdbcTemplate().update(String.format(UPDATE_CARD_BY_ID_SQL, getSchema(), sb.toString(),where), cardId);
    }

    if(resp == 0) {
      log.error(String.format("[updatePrepaidCard] Tarjeta [id: %d] no existe", cardId));
      throw new ValidationException(TARJETA_NO_EXISTE);
    }
  }

  /**@aqu
   *  Busca una tarjeta por id y publica evento de tarjeta cerrada
   * @param cardId id interno de la tarjeta
   * @throws Exception
   */
  public void publishCardEvent(String externalUserId, String accountUuid, Long cardId, String endPoint) throws Exception {
    if(StringUtils.isAllBlank(externalUserId)){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "externalUserId"));
    }

    if(StringUtils.isAllBlank(accountUuid)){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountUuid"));
    }

    if(cardId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }

    PrepaidCard10 prepaidCard10 = this.getPrepaidCardById(null, cardId);

    Card card = new Card();
    card.setId(prepaidCard10.getUuid());
    card.setPan(prepaidCard10.getPan());
    card.setStatus(prepaidCard10.getStatus().toString());

    cl.multicaja.prepaid.kafka.events.model.Timestamps timestamps = new cl.multicaja.prepaid.kafka.events.model.Timestamps();
    timestamps.setCreatedAt(prepaidCard10.getTimestamps().getCreatedAt());
    timestamps.setUpdatedAt(prepaidCard10.getTimestamps().getUpdatedAt());
    card.setTimestamps(timestamps);

    CardEvent cardEvent = new CardEvent();
    cardEvent.setCard(card);
    cardEvent.setAccountId(accountUuid);
    cardEvent.setUserId(externalUserId);
    getKafkaEventDelegate10().publishCardEvent(cardEvent, endPoint);
  }
  
  public String hashPan(String accountUuid, String pan) throws Exception {
    if(StringUtils.isAllBlank(accountUuid)){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountUuid"));
    }
    if(StringUtils.isAllBlank(pan)){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "pan"));
    }

    BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder(4, new SecureRandom(String.valueOf(accountUuid).getBytes()));
    return bCryptPasswordEncoder.encode(pan);
  }

  @Override
  public PrepaidCardResponse10 upgradePrepaidCard(Map<String, Object> headers, String userUuid, String accountUuid) throws Exception {

    PrepaidUser10 prepaidUser = getPrepaidUserEJBBean10().findByExtId(null, userUuid);
    if(prepaidUser == null) {
      throw new NotFoundException(CLIENTE_NO_TIENE_PREPAGO);
    }
    if(PrepaidUserLevel.LEVEL_2.equals(prepaidUser.getUserLevel())) {
      throw new ValidationException(CLIENTE_YA_TIENE_NIVEL_2);
    }

    // Validar que la cuenta exista
    Account account = getAccountEJBBean10().findByUuid(accountUuid);
    if(account == null){
      throw new NotFoundException(CUENTA_NO_EXISTE);
    }

    PrepaidCard10 prepaidCard = getByUserIdAndStatus(null, prepaidUser.getId(), PrepaidCardStatus.ACTIVE, PrepaidCardStatus.LOCKED);
    if(prepaidCard == null) {
      throw new NotFoundException(TARJETA_NO_EXISTE);
    }

    // Subir el nivel del usuario
    getPrepaidUserEJBBean10().updatePrepaidUserLevel(prepaidUser.getId(), PrepaidUserLevel.LEVEL_2);

    // Notificar que se ha creado una tarjeta nueva
    publishCardEvent(prepaidUser.getUuid(), accountUuid, prepaidCard.getId(), KafkaEventsRoute10.SEDA_CARD_CREATED_EVENT);

    PrepaidCardResponse10 prepaidCardResponse10 = new PrepaidCardResponse10();
    prepaidCardResponse10.setId(prepaidCard.getUuid());
    prepaidCardResponse10.setPan(prepaidCard.getPan());
    prepaidCardResponse10.setNameOnCard(prepaidCard.getNameOnCard());
    prepaidCardResponse10.setStatus(prepaidCard.getStatus().toString());
    prepaidCardResponse10.setTimestamps(prepaidCard.getTimestamps());
    return prepaidCardResponse10;
  }

  public PrepaidCard10 getByUserIdAndStatus(Map<String, Object> headers, Long userId,PrepaidCardStatus ... lstStatus)  throws Exception{
    try {

      StringBuilder sb = new StringBuilder();

      int i = 1;
      for(PrepaidCardStatus status :lstStatus ) {
        sb.append("t.estado = '");
        sb.append(status.name());
        if(i == lstStatus.length) {
          sb.append("'");
        } else {
          sb.append("' OR ");
        }
        i++;
      }

      String QUERY = String.format(FIND_CARD_BY_USERID_STATUS,getSchema(),getSchema(),getSchema(),sb.toString());

      return getDbUtils().getJdbcTemplate().queryForObject(QUERY, getCardMapper(), userId);
    } catch (EmptyResultDataAccessException ex) {
      log.error(String.format("[getByUserIdAndStatus] Tarjeta [userId: %d, status: %s] no existe", userId, lstStatus));
     return null;
    }
  }

  public PrepaidCard10 getInvalidCardByUserId(Map<String, Object> headers, Long userId)  throws Exception{
    try {
      return getDbUtils().getJdbcTemplate().queryForObject(FIND_INVALID_CARD_BY_USERID, getCardMapper(), userId);
    } catch (EmptyResultDataAccessException ex) {
      log.error(String.format("[getPrepaidCardById] Tarjeta [id: %d] no existe", userId));
      return null;
    }
  }

  public PrepaidCard10 getPrepaidCardByPanAndUserId(String pan, Long userId)  throws Exception {
    try {
      return getDbUtils().getJdbcTemplate().queryForObject(FIND_CARD_BY_PAN_USERID, getCardMapper(), userId,pan);
    } catch (EmptyResultDataAccessException ex) {
      log.error(String.format("[getPrepaidCardById] Tarjeta [id: %d] [pan: %s] no existe", userId,pan));
      return null;
    }
  }

  public PrepaidCard10 getPrepaidCardByAccountId(Long accountId){
    try {
      return getDbUtils().getJdbcTemplate().queryForObject(SEARCH_BY_ACCOUNT_ID, getCardMapper(),accountId);
    } catch (EmptyResultDataAccessException ex) {
      log.error(String.format("[getPrepaidCardById] Tarjeta [accountId: %d] no existe", accountId));
      return null;
    }
  }


  //TODO: Este metodo tiene que ser eliminado cuando las Tarjetas tenga
  public void fixEncryptData(String oldPasswordEncrypt) throws Exception {

    log.info("Update Card Encrypt IN");
    List<PrepaidCard10> cards = this.getPrepaidCards(null,null,null,null,null,null,null);
    for (PrepaidCard10 card:cards) {


      try {
        String cardPanDesencrypted = getAesEncryptCardUtil().decryptPan(card.getEncryptedPan(), oldPasswordEncrypt);
        if(cardPanDesencrypted == null){
          throw new Exception("Este pan no se puede desencriptar");
        }
        String cardReEncrypted = getAzureEncryptCardUtil().encryptPan(cardPanDesencrypted, getConfigUtils().getProperty("encrypt.password",""));
        card.setEncryptedPan(cardReEncrypted);
        updatePrepaidCard(null,card.getId(),card.getAccountId(),card);
        log.info(String.format("Tarjeta Id [%d] Pan Encriptado Actualizado", card.getId()));
      } catch (Exception e) {
        log.error(String.format("Tarjeta Id [{}] ya se encuentra encriptada con Keyvault", card.getId()));
      }
    }
    log.info("Update Card Encrypt OUT");
  }

  private RowMapper<PrepaidCard10> getCardMapper() {
    return (ResultSet rs, int rowNum) -> {
      PrepaidCard10 c = new PrepaidCard10();
      c.setId(rs.getLong("id"));
      c.setPan(rs.getString("pan"));
      c.setEncryptedPan(rs.getString("pan_encriptado"));
      c.setStatus(PrepaidCardStatus.valueOfEnum(rs.getString("estado")));
      c.setNameOnCard(rs.getString("nombre_tarjeta"));
      c.setProducto(rs.getString("producto"));
      c.setExpiration(rs.getInt("expiracion"));
      c.setNumeroUnico(rs.getString("numero_unico"));
      Timestamps timestamps = new Timestamps();
      timestamps.setCreatedAt(rs.getTimestamp("fecha_creacion").toLocalDateTime());
      timestamps.setUpdatedAt(rs.getTimestamp("fecha_actualizacion").toLocalDateTime());
      c.setTimestamps(timestamps);
      c.setUuid(rs.getString("uuid"));
      c.setHashedPan(rs.getString("pan_hash"));
      c.setAccountId(rs.getLong("id_cuenta"));
      return c;
    };
  }

}
