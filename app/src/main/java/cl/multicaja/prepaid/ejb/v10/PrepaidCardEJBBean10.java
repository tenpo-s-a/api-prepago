package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.BaseException;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.db.InParam;
import cl.multicaja.core.utils.db.NullParam;
import cl.multicaja.core.utils.db.OutParam;
import cl.multicaja.core.utils.db.RowMapper;
import cl.multicaja.prepaid.async.v10.KafkaEventDelegate10;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.prepaid.model.v10.Timestamps;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import javax.inject.Inject;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import static cl.multicaja.core.model.Errors.ERROR_DE_COMUNICACION_CON_BBDD;
import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;

/**
 * @author vutreras
 */
@Stateless
@LocalBean
@TransactionManagement(value=TransactionManagementType.CONTAINER)
public class PrepaidCardEJBBean10 extends PrepaidBaseEJBBean10 implements PrepaidCardEJB10 {

  private static Log log = LogFactory.getLog(PrepaidCardEJBBean10.class);

  @EJB
  private PrepaidUserEJBBean10 prepaidUserEJBBean10;

  @EJB
  private AccountEJBBean10 accountEJBBean10;

  @Inject
  private KafkaEventDelegate10 kafkaEventDelegate10;

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


  /**
   *
   * @param headers
   * @param fetchCount
   * @param id
   * @param userId
   * @param expiration
   * @param status
   * @param processorUserId
   * @return
   * @throws Exception
   */
  private List<PrepaidCard10> getPrepaidCards(Map<String, Object> headers, int fetchCount, Long id, Long userId, Integer expiration, PrepaidCardStatus status, String processorUserId,String encryptedPan, String pan) throws Exception {
    throw new IllegalStateException();
  }

  public PrepaidCard10 getPrepaidCardByEncryptedPan(Map<String, Object> headers, String encryptedPan) throws Exception{
    List<PrepaidCard10> lst = this.getPrepaidCards(headers, 1,null, null, null, null, null,encryptedPan, null);
    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }

  @Override
  public PrepaidCard10 createPrepaidCard(Map<String, Object> headers, PrepaidCard10 prepaidCard) throws Exception {
    throw new IllegalStateException();
  }

  @Override
  public List<PrepaidCard10> getPrepaidCards(Map<String, Object> headers, Long id, Long userId, Integer expiration, PrepaidCardStatus status, String processorUserId) throws Exception {
    //busca todas las tarjetas para los criterios de busqueda (fetchCount = -1 significa todas)
    return this.getPrepaidCards(headers, -1, id, userId, expiration, status, processorUserId,null,null);
  }

  @Override
  public PrepaidCard10 getPrepaidCardById(Map<String, Object> headers, Long id) throws Exception {
    if(id == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }
    List<PrepaidCard10> lst = this.getPrepaidCards(headers, id, null, null, null, null);
    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }

  @Override
  public PrepaidCard10 getLastPrepaidCardByUserId(Map<String, Object> headers, Long userId) throws Exception {

    if(userId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "userId"));
    }

    List<PrepaidCard10> lst = this.getPrepaidCards(headers, 1,null, userId, null, null, null,null,null);

    return lst != null && !lst.isEmpty() ? lst.get(0) : null;
  }

  @Override
  public PrepaidCard10 getLastPrepaidCardByUserIdAndOneOfStatus(Map<String, Object> headers, Long userId, PrepaidCardStatus... status) throws Exception {

    if(userId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "userId"));
    }
    if(status == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "status"));
    }

    List<PrepaidCard10> lst = this.getPrepaidCards(headers, 1,null, userId, null, null, null,null,null);

    PrepaidCard10 prepaidCard10 = lst != null && !lst.isEmpty() ? lst.get(0) : null;

    if (prepaidCard10 == null) {
      return null;
    }

    for (PrepaidCardStatus st : status) {
      if (st.equals(prepaidCard10.getStatus())) {
        return prepaidCard10;
      }
    }
    return null;
  }

  @Override
  public void updatePrepaidCardStatus(Map<String, Object> headers, Long id, PrepaidCardStatus status) throws Exception {

    if(id == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }
    if(status == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "status"));
    }

    Object[] params = {
      id, //id
      status.toString(), //estado
      new OutParam("_error_code", Types.VARCHAR),
      new OutParam("_error_msg", Types.VARCHAR)
    };

    Map<String, Object> resp = getDbUtils().execute(getSchema() + ".mc_prp_actualizar_estado_tarjeta_v10", params);

    if (!"0".equals(resp.get("_error_code"))) {
      log.error("updatePrepaidCardStatus resp: " + resp);
      throw new BaseException(ERROR_DE_COMUNICACION_CON_BBDD);
    }
  }

  @Override
  public void updatePrepaidCard(Map<String, Object> headers, Long cardId, Long userId, PrepaidCardStatus oldStatus, PrepaidCard10 prepaidCard) throws Exception {
    throw new IllegalStateException();
  }

  public PrepaidCard10 getPrepaidCardByPanAndUserId(String pan, Long userId) throws Exception {
    if(pan == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "pan"));
    }
    if(userId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "userId"));
    }

    List<PrepaidCard10> lst = this.getPrepaidCards(null, -1,null, userId, null, null, null,null, pan);

    if(lst != null && !lst.isEmpty()) {
      return lst.get(0);
    }

    return null;
  }

  @Override
  public PrepaidCard10 getPrepaidCardByPanAndProcessorUserId(Map<String, Object> headers, String pan, String processorUserId) throws Exception {
    if(pan == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "pan"));
    }
    if(processorUserId == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "processorUserId"));
    }

    List<PrepaidCard10> lst = this.getPrepaidCards(headers, -1,null, null, null, null, processorUserId,null,null);

    if( lst != null) {
      PrepaidCard10 prepaidCard10 = lst.stream()
        .filter(c -> pan.equals(c.getPan()))
        .findAny()
        .orElse(null);

      return prepaidCard10;
    }

    return null;
  }

  /**
   *  Busca una tarjeta por id y publica evento de tarjeta creada
   * @param cardId id interno de la tarjeta
   * @throws Exception
   */
  @Override
  public void publishCardEvent(String externalUserId, String accountUuid, Long cardId, String endpoint) throws Exception {
    throw new IllegalStateException();
  }

  @Override
  public PrepaidCard10 upgradePrepaidCard(Map<String, Object> headers, String userUuid, String accountUuid) throws Exception {
    throw new IllegalStateException();
  }

}
