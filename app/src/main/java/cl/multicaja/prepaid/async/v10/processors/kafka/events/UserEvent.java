package cl.multicaja.prepaid.async.v10.processors.kafka.events;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.prepaid.async.v10.processors.BaseProcessor10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.model.v10.PrepaidUser11;
import cl.multicaja.prepaid.model.v11.UserStatus;
import cl.multicaja.tecnocom.util.json.JsonUtils;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class UserEvent extends BaseProcessor10 {
  private static Log log = LogFactory.getLog(UserEvent.class);

  private JsonUtils jsonUtils = new JsonUtils();

  public UserEvent(BaseRoute10 route) {
    super(route);
  }


  public PrepaidUser11 buildPrepaidUserToSave(cl.multicaja.prepaid.kafka.events.model.User userIn){

    PrepaidUser11 userToCreate = new PrepaidUser11();

    userToCreate.setRut(numberUtils.toInt(userIn.getDocumentNumber()));
    userToCreate.setStatus(UserStatus.valueOfEnum(userIn.getState()));
    userToCreate.setName(userIn.getFirstName());
    userToCreate.setLastName(userIn.getLastName());
    userToCreate.setDocumentNumber(userIn.getDocumentNumber());
    userToCreate.setLevel(userIn.getLevel());
    userToCreate.setUuid(userIn.getId());

    return userToCreate;
  }

  public Processor processUserCreatedEvent() {

    return new Processor(){
      @Override
      public void process(Exchange exchange) {
        try {

          log.info("[processUserCreatedEvent] Processing USER_CREATED event");
          log.info(String.format("[processUserCreatedEvent] %s", exchange.getMessage().getBody()));

          ExchangeData exchangeData = (ExchangeData) exchange.getMessage().getBody();
          String data = exchangeData.getData().toString();

          cl.multicaja.prepaid.kafka.events.model.User userResponse = jsonUtils.fromJson(data, cl.multicaja.prepaid.kafka.events.model.User.class);

          PrepaidUser11 userToCreate = buildPrepaidUserToSave(userResponse);

          if(getRoute().getPrepaidUserEJBBean10().findPrepaidUserV11(null,null,userResponse.getId(),null) == null){
            getRoute().getPrepaidUserEJBBean10().createPrepaidUserV11(null,userToCreate);
          }
        }catch (Exception ex){
          log.error("[processUserCreatedEvent] Processing USER_CREATED event error: "+ex);
        }

      }
    };
  }

  public Processor processUserUpdatedEvent(){
    return new Processor() {
      @Override
      public void process(Exchange exchange) {

        try{
          log.info("[processUserUpdatedEvent] Processing USER_UPDATED event");
          log.info(String.format("[processUserUpdatedEvent] %s", exchange.getMessage().getBody()));

          ExchangeData exchangeData = (ExchangeData) exchange.getMessage().getBody();
          String data = exchangeData.getData().toString();

          cl.multicaja.prepaid.kafka.events.model.User userResponse = jsonUtils.fromJson(data, cl.multicaja.prepaid.kafka.events.model.User.class);

          PrepaidUser11 userToUpdate = getRoute().getPrepaidUserEJBBean10().findPrepaidUserV11(null,null,userResponse.getId(),null);

          if(userToUpdate != null){

            //userToUpdate.setUiid(userResponse.getId()); //se supone que nunca cambiará.
            userToUpdate.setDocumentNumber(userResponse.getDocumentNumber());
            userToUpdate.setName(userResponse.getFirstName());
            userToUpdate.setLastName(userResponse.getLastName());
            userToUpdate.setStatus(UserStatus.valueOfEnum(userResponse.getState()));
            userToUpdate.setLevel(userResponse.getLevel());

            getRoute().getPrepaidUserEJBBean10().updatePrepaidUserV11(null,userToUpdate);
          }
        }catch(Exception ex){
          log.error("[processUserCreatedEvent] Processing USER_UPDATED event error: "+ex);
        }

      }
    };
  }
}
