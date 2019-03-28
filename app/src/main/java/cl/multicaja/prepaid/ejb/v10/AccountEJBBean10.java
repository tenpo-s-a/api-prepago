package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.BadRequestException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.prepaid.async.v10.KafkaEventDelegate10;
import cl.multicaja.prepaid.model.v10.PrepaidCard10;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import javax.inject.Inject;

import static cl.multicaja.core.model.Errors.PARAMETRO_FALTANTE_$VALUE;
import static cl.multicaja.core.model.Errors.TARJETA_NO_EXISTE;

@Stateless
@LocalBean
@TransactionManagement(value= TransactionManagementType.CONTAINER)
public class AccountEJBBean10 extends PrepaidBaseEJBBean10 implements AccountEJB10 {

  private static Log log = LogFactory.getLog(AccountEJBBean10.class);

  @Inject
  private KafkaEventDelegate10 kafkaEventDelegate10;

  @EJB
  private PrepaidCardEJBBean10 prepaidCardEJBBean10;

  public KafkaEventDelegate10 getKafkaEventDelegate10() {
    return kafkaEventDelegate10;
  }

  public void setKafkaEventDelegate10(KafkaEventDelegate10 kafkaEventDelegate10) {
    this.kafkaEventDelegate10 = kafkaEventDelegate10;
  }

  public PrepaidCardEJBBean10 getPrepaidCardEJBBean10() {
    return prepaidCardEJBBean10;
  }

  public void setPrepaidCardEJBBean10(PrepaidCardEJBBean10 prepaidCardEJBBean10) {
    this.prepaidCardEJBBean10 = prepaidCardEJBBean10;
  }

  @Override
  public void publishAccountCreatedEvent(Long id) throws Exception {
    if(id == null){
      throw new BadRequestException(PARAMETRO_FALTANTE_$VALUE).setData(new KeyValue("value", "id"));
    }

    //FIXME: debe buscar en la tabla cuenta/contrato
    PrepaidCard10 prepaidCard10 = getPrepaidCardEJBBean10().getPrepaidCardById(null, id);

    if(prepaidCard10 == null){
      throw new ValidationException(TARJETA_NO_EXISTE);
    }

    getKafkaEventDelegate10().publishAccountCreatedEvent(prepaidCard10);
  }
}
