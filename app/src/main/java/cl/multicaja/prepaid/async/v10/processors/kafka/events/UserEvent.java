package cl.multicaja.prepaid.async.v10.processors.kafka.events;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.prepaid.async.v10.processors.BaseProcessor10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserLevel;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;
import cl.multicaja.tecnocom.util.json.JsonUtils;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.HashMap;
import java.util.Map;

public class UserEvent extends BaseProcessor10 {
  private static Log log = LogFactory.getLog(UserEvent.class);

  private JsonUtils jsonUtils = new JsonUtils();
  private Map<String,Object> isAllValidMap = new HashMap<>();

  public UserEvent(BaseRoute10 route) {
    super(route);
  }


  private Boolean validateFields(cl.multicaja.prepaid.kafka.events.model.User userFields){

    final String errMessage = "null or empty";

    Boolean isAllValid;

    isAllValid = userFields.getId() != null && !userFields.getId().isEmpty();
    if (!isAllValid) {
      isAllValidMap.put("uuid:",errMessage);
    }

    isAllValid = userFields.getDocumentNumber() != null && !userFields.getDocumentNumber().isEmpty();
    if(!isAllValid) {
      isAllValidMap.put("rut:",errMessage);
    }

    isAllValid = userFields.getState() != null && !userFields.getState().isEmpty();
    if(!isAllValid) {
      isAllValidMap.put("status:",errMessage);
    }

    isAllValid = userFields.getLevel() != null && !userFields.getLevel().isEmpty();
    if(!isAllValid) {
      isAllValidMap.put("level:",errMessage);
    }

    isAllValid = userFields.getFirstName() != null && !userFields.getFirstName().isEmpty();
    if(!isAllValid) {
      isAllValidMap.put("first name:",errMessage);
    }

    isAllValid = userFields.getLastName() != null && !userFields.getLastName().isEmpty();
    if(!isAllValid) {
      isAllValidMap.put("last name:",errMessage);
    }

    if(isAllValidMap.size()>0){
      isAllValid = false;
    }else{
      isAllValid = true;
    }
    return isAllValid;
  }


  private PrepaidUser10 buildPrepaidUserToSave(cl.multicaja.prepaid.kafka.events.model.User userIn){

    PrepaidUser10 userToCreate = new PrepaidUser10();

    if(validateFields(userIn)) {

      userToCreate.setRut(numberUtils.toInt(userIn.getDocumentNumber()));
      userToCreate.setStatus(PrepaidUserStatus.valueOfEnum(userIn.getState()));
      userToCreate.setName(userIn.getFirstName());
      userToCreate.setLastName(userIn.getLastName());
      userToCreate.setDocumentNumber(userIn.getDocumentNumber());
      userToCreate.setUserLevel(PrepaidUserLevel.valueOfEnum(userIn.getLevel()));
      userToCreate.setUuid(userIn.getId());

    }else{
      userToCreate = null;
    }

    return userToCreate;
  }

  private PrepaidUser10 buildPrepaidUserToUpdate(cl.multicaja.prepaid.kafka.events.model.User userIn, PrepaidUser10 userToUpdate){

    if(validateFields(userIn)) {

      userToUpdate.setStatus(PrepaidUserStatus.valueOfEnum(userIn.getState()));
      userToUpdate.setName(userIn.getFirstName());
      userToUpdate.setLastName(userIn.getLastName());
      userToUpdate.setUserLevel(PrepaidUserLevel.valueOfEnum(userIn.getLevel()));

    }else{
      userToUpdate = null;
    }
    return userToUpdate;
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

          PrepaidUser10 userToCreate = buildPrepaidUserToSave(userResponse);
          if(userToCreate!=null) {
            if (getRoute().getPrepaidUserEJBBean10().findPrepaidUserV10(null, null, userResponse.getId(), null) == null) {
              getRoute().getPrepaidUserEJBBean10().createPrepaidUserV10(null, userToCreate);
            }
          }else{
            log.info("[processUserCreatedEvent] Processing USER_CREATED event with invalid fields: "+isAllValidMap.toString());
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

          PrepaidUser10 userFound = getRoute().getPrepaidUserEJBBean10().findPrepaidUserV10(null,null,userResponse.getId(),null);

          if(userFound != null){

            PrepaidUser10 userToUpdate = buildPrepaidUserToUpdate(userResponse,userFound);
            if(userToUpdate!=null){
              getRoute().getPrepaidUserEJBBean10().updatePrepaidUserV10(null,userToUpdate);
            }else{
              log.info("[processUserCreatedEvent] Processing USER_UPDATED event with invalid fields: "+isAllValidMap.toString());
            }
          }
        }catch(Exception ex){
          log.error("[processUserCreatedEvent] Processing USER_UPDATED event error: "+ex);
        }

      }
    };
  }
}
