package cl.multicaja.prepaid.ejb.v11;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.prepaid.ejb.v10.PrepaidCardEJBBean10;
import cl.multicaja.prepaid.helpers.users.model.Timestamps;
import cl.multicaja.prepaid.kafka.events.CardEvent;
import cl.multicaja.prepaid.kafka.events.model.Card;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.security.InvalidParameterException;
import java.security.SecureRandom;
import java.sql.ResultSet;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;

@Stateless
@LocalBean
@TransactionManagement(value= TransactionManagementType.CONTAINER)
public class PrepaidCardEJBBean11 extends PrepaidCardEJBBean10 {

  private static Log log = LogFactory.getLog(PrepaidCardEJBBean11.class);

  private static final String FIND_CARD_BY_ID_SQL = String.format("SELECT * FROM %s.prp_tarjeta WHERE id = ?", getSchema());
  private static final String UPDATE_CARD_BY_ID_SQL = "UPDATE %s.prp_tarjeta SET %s WHERE id = ?";

  public PrepaidCardEJBBean11() {
    super();
  }

  @Override
  public PrepaidCard10 getPrepaidCardById(Map<String, Object> headers, Long id) throws Exception {
    if(id == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }
    log.info(String.format("[getPrepaidCardById] Buscando tarjeta [id: %d]", id));

    RowMapper<PrepaidCard10> rm = (ResultSet rs, int rowNum) -> {
      PrepaidCard10 c = new PrepaidCard10();
      c.setId(rs.getLong("id"));
      c.setIdUser(rs.getLong("id_usuario"));
      c.setPan(rs.getString("pan"));
      c.setEncryptedPan(rs.getString("pan_encriptado"));
      c.setProcessorUserId(rs.getString("contrato"));
      c.setExpiration(rs.getInt("expiracion"));
      c.setStatus(PrepaidCardStatus.valueOfEnum(rs.getString("estado")));
      c.setNameOnCard(rs.getString("nombre_tarjeta"));
      c.setProducto(rs.getString("producto"));
      c.setNumeroUnico(rs.getString("numero_unico"));
      Timestamps timestamps = new Timestamps();
      timestamps.setCreatedAt(rs.getTimestamp("fecha_creacion"));
      timestamps.setUpdatedAt(rs.getTimestamp("fecha_actualizacion"));
      c.setTimestamps(timestamps);

      c.setUuid(rs.getString("uuid"));
      c.setHashedPan(rs.getString("pan_hash"));
      c.setAccountId(rs.getLong("id_cuenta"));
      return c;
    };

    try {
      return getDbUtils().getJdbcTemplate()
        .queryForObject(FIND_CARD_BY_ID_SQL, rm, id);
    } catch (EmptyResultDataAccessException ex) {
      log.error(String.format("[getPrepaidCardById] Tarjeta [id: %d] no existe", id));
      throw new ValidationException(TARJETA_NO_EXISTE);
    }
  }

  public void updatePrepaidCard(Map<String, Object> headers, Long cardId, Long accountId, PrepaidCard10 prepaidCard) throws Exception {

    if(cardId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }

    if(accountId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "accountId"));
    }

    if(prepaidCard == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "prepaidCard"));
    }

    if(prepaidCard.getId() == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "prepaidCard.id"));
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
    if(!StringUtils.isAllBlank(prepaidCard.getProcessorUserId())) {
      sb.append("contrato = '")
        .append(prepaidCard.getProcessorUserId())
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

    sb.append("id_cuenta = ")
      .append(accountId)
      .append(", ");

    sb.append("fecha_actualizacion = timezone('utc', now())");

    int resp = getDbUtils().getJdbcTemplate().update(String.format(UPDATE_CARD_BY_ID_SQL, getSchema(), sb.toString()), prepaidCard.getId());

    if(resp == 0) {
      log.error(String.format("[updatePrepaidCard] Tarjeta [id: %d] no existe", cardId));
      throw new ValidationException(TARJETA_NO_EXISTE);
    }
  }

  /**
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

    timestamps.setCreatedAt(prepaidCard10.getTimestamps().getCreatedAt().toLocalDateTime());

    timestamps.setUpdatedAt(prepaidCard10.getTimestamps().getUpdatedAt().toLocalDateTime());
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

}
