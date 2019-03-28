package cl.multicaja.prepaid.ejb.v11;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.prepaid.dao.CardDao;
import cl.multicaja.prepaid.ejb.v10.PrepaidCardEJBBean10;
import cl.multicaja.prepaid.helpers.users.model.Timestamps;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import cl.multicaja.prepaid.model.v10.PrepaidCardStatus;
import cl.multicaja.prepaid.model.v11.Card;
import cl.multicaja.prepaid.model.v11.CardStatus;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.Map;

import static cl.multicaja.core.model.Errors.*;

public class PrepaidCardEJBBean11 extends PrepaidCardEJBBean10 {

  private static Log log = LogFactory.getLog(PrepaidCardEJBBean11.class);

  public PrepaidCardEJBBean11() {
    super();
  }

  @Inject
  private CardDao cardDao;

  public CardDao getCardDao() {
    return cardDao;
  }

  public void setCardDao(CardDao cardDao) {
    this.cardDao = cardDao;
  }

  @Override
  public PrepaidCard10 getPrepaidCardById(Map<String, Object> headers, Long id) throws Exception {
    if(id == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }

    Card card = cardDao.find(id);

    if(card == null) {
      return null;
    }

    PrepaidCard10 c = new PrepaidCard10();
    c.setId(card.getId());
    c.setIdUser(card.getIdUsuario());
    c.setPan(card.getPan());
    c.setEncryptedPan(card.getEncryptedPan());
    c.setProcessorUserId(card.getContrato());
    c.setExpiration(card.getExpiracion());
    c.setStatus(PrepaidCardStatus.valueOfEnum(card.getStatus().toString()));
    c.setNameOnCard(card.getCardName());
    c.setProducto(card.getProducto());
    c.setNumeroUnico(card.getNumeroUnico());
    Timestamps timestamps = new Timestamps();
    timestamps.setCreatedAt(Timestamp.valueOf(card.getCreatedAt()));
    timestamps.setUpdatedAt(Timestamp.valueOf(card.getUpdatedAt()));
    c.setTimestamps(timestamps);

    return c;
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

    Card card = cardDao.find(cardId);

    if(card == null) {
      throw new ValidationException(TARJETA_NO_EXISTE);
    }

    card.setPan(prepaidCard.getPan());
    card.setEncryptedPan(prepaidCard.getEncryptedPan());

    card.setExpiracion(prepaidCard.getExpiration());
    card.setStatus(CardStatus.valueOf(prepaidCard.getStatus().toString()));
    card.setCardName(prepaidCard.getNameOnCard());
    card.setProducto(prepaidCard.getProducto());
    card.setNumeroUnico(prepaidCard.getNumeroUnico());
    card.setAccountId(accountId);

    cardDao.update(card);
  }
}
