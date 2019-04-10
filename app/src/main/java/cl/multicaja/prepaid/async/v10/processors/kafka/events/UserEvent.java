package cl.multicaja.prepaid.async.v10.processors.kafka.events;

import cl.multicaja.camel.ExchangeData;
import cl.multicaja.prepaid.async.v10.processors.BaseProcessor10;
import cl.multicaja.prepaid.async.v10.routes.BaseRoute10;
import cl.multicaja.prepaid.kafka.events.model.User;
import cl.multicaja.prepaid.model.v10.PrepaidUser10;
import cl.multicaja.prepaid.model.v10.PrepaidUserLevel;
import cl.multicaja.prepaid.model.v10.PrepaidUserStatus;
import cl.multicaja.prepaid.model.v11.DocumentType;
import cl.multicaja.tecnocom.util.json.JsonUtils;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static cl.multicaja.core.test.TestBase.getRandomNumericString;

public class UserEvent extends BaseProcessor10 {
  private static Log log = LogFactory.getLog(UserEvent.class);

  private JsonUtils jsonUtils = new JsonUtils();
  private Map<String,Object> isAllValidMap = new HashMap<>();
  private ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
  private Validator validator = factory.getValidator();

  public UserEvent(BaseRoute10 route) {
    super(route);
  }

  private Boolean validateFields(cl.multicaja.prepaid.kafka.events.model.User userFields){

    Set<ConstraintViolation<cl.multicaja.prepaid.kafka.events.model.User>> violations = validator.validate(userFields);

    isAllValidMap.clear();
    for (ConstraintViolation<User> violation : violations) {
      isAllValidMap.put(violation.getPropertyPath().toString(),violation.getMessage());
    }

    return violations.size() > 0 ? Boolean.FALSE:Boolean.TRUE;
  }


  private PrepaidUser10 buildPrepaidUserToSave(cl.multicaja.prepaid.kafka.events.model.User userIn){

    PrepaidUser10 userToCreate = new PrepaidUser10();

    if(validateFields(userIn)) {

      userToCreate.setUserIdMc(Long.valueOf(getRandomNumericString(10)));
      userToCreate.setDocumentType(DocumentType.DNI_CL);

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
            if (getRoute().getPrepaidUserEJBBean10().findByExtId(null, userResponse.getId()) == null) {
              PrepaidUser10 userCreated = getRoute().getPrepaidUserEJBBean10().createUser(null, userToCreate);
              log.info("[processUserCreatedEvent] Processing USER_CREATED event SUCCESS with this values: "+userCreated.toString());
            }
          }else{
            log.info("[processUserCreatedEvent] Processing USER_CREATED event FAIL with invalid fields: "+isAllValidMap.toString());
          }
        }catch (Exception ex){
          log.error("[processUserCreatedEvent] Processing USER_CREATED event FAIL: "+ex);
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

          PrepaidUser10 userFound = getRoute().getPrepaidUserEJBBean10().findByExtId(null,userResponse.getId());

          if(userFound != null){

            PrepaidUser10 userToUpdate = buildPrepaidUserToUpdate(userResponse,userFound);
            if(userToUpdate!=null){
              PrepaidUser10 userUpdated = getRoute().getPrepaidUserEJBBean10().updatePrepaidUser(null,userToUpdate);
              log.info("[processUserCreatedEvent] Processing USER_UPDATED event SUCCESS with this values: "+userUpdated.toString());
            }else{
              log.info("[processUserCreatedEvent] Processing USER_UPDATED event FAIL with invalid fields: "+isAllValidMap.toString());
            }
          }
        }catch(Exception ex){
          log.error("[processUserCreatedEvent] Processing USER_UPDATED FAIL error: "+ex);
        }

      }
    };
  }
}
