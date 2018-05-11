package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.KeyValue;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDelegate10;
import cl.multicaja.prepaid.domain.*;
import cl.multicaja.users.ejb.v10.UsersEJBBean10;
import cl.multicaja.users.model.v10.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author vutreras
 */
@Stateless
@LocalBean
@TransactionManagement(value=TransactionManagementType.CONTAINER)
public class PrepaidEJBBean10 implements PrepaidEJB10 {

  private static Log log = LogFactory.getLog(PrepaidEJBBean10.class);

  protected NumberUtils numberUtils = NumberUtils.getInstance();

  private ConfigUtils configUtils;

  private DBUtils dbUtils;

  /**
   *
   * @return
   */
  public ConfigUtils getConfigUtils() {
    if (this.configUtils == null) {
      this.configUtils = new ConfigUtils("api-prepaid");
    }
    return this.configUtils;
  }

  /**
   *
   * @return
   */
  public DBUtils getDbUtils() {
    if (this.dbUtils == null) {
      this.dbUtils = new DBUtils(this.getConfigUtils());
    }
    return this.dbUtils;
  }

  @EJB
  private UsersEJBBean10 usersEJB10;

  @Inject
  private PrepaidTopupDelegate10 delegate;

  @Override
  public Map<String, Object> info() throws Exception{
    Map<String, Object> map = new HashMap<>();
    map.put("class", this.getClass().getSimpleName());
    map.put("ejb_users", this.usersEJB10.info());
    return map;
  }

  @Override
  public PrepaidTopup topupUserBalance(Map<String, Object> headers, NewPrepaidTopup topupRequest) throws ValidationException {

    if(topupRequest == null || topupRequest.getAmount() == null){
        throw new ValidationException(1024, "El cliente no pasó la validación", 422);
    }
    if(topupRequest.getRut() == null){
      throw new ValidationException(1024, "El cliente no pasó la validación", 422);
    }
    if(StringUtils.isBlank(topupRequest.getMerchantCode())){
      throw new ValidationException(1024, "El cliente no pasó la validación", 422);
    }
    if(StringUtils.isBlank(topupRequest.getTransactionId())){
      throw new ValidationException(1024, "El cliente no pasó la validación", 422);
    }
    if(topupRequest.getAmount().getValue() == null){
      throw new ValidationException(1024, "El cliente no pasó la validación", 422);
    }
    if(topupRequest.getAmount().getCurrencyCode() == null){
      throw new ValidationException(1024, "El cliente no pasó la validación", 422);
    }

    PrepaidTopup topup = new PrepaidTopup();

    topup.setAmount(topupRequest.getAmount());
    topup.setTransactionId(topupRequest.getTransactionId());
    topup.setRut(topupRequest.getRut());
    topup.setMerchantCode(topupRequest.getMerchantCode());

    topup.setId(numberUtils.random(1, Integer.MAX_VALUE));
    topup.setUserId(1);
    topup.setStatus("exitoso");
    topup.setTimestamps(new Timestamps());

    User user = new User(); //TODO este user es el que retorna el ejb de users

    delegate.sendTopUp(topup, user);

    return topup;
  }

  @Override
  public void reverseTopupUserBalance(Map<String, Object> headers, NewPrepaidTopup topupRequest) {

  }

  @Override
  public List<PrepaidTopup> getUserTopups(Map<String, Object> headers, Long userId) {
    return null;
  }

  @Override
  public PrepaidUserSignup initUserSignup(Map<String, Object> headers, NewPrepaidUserSignup signupRequest) {
    return null;
  }

  @Override
  public PrepaidUserSignup getUserSignup(Map<String, Object> headers, Long signupId) {
    return null;
  }

  @Override
  public PrepaidCard issuePrepaidCard(Map<String, Object> headers, Long userId) {
    return null;
  }

  @Override
  public PrepaidCard getPrepaidCard(Map<String, Object> headers, Long userId) throws Exception {

    if (userId == null || userId.longValue() <= 0) {
      throw new ValidationException(3).setData(new KeyValue("params", "userId"));
    }

    PrepaidCard card = (PrepaidCard) this.getDbUtils().executeAndGetFirst("prepago.mc_prepago_buscar_tarjeta_por_usuario", (Map<String, Object> row) -> {
        PrepaidCard c = new PrepaidCard();
        c.setId(numberUtils.toLong(row.get("id"), 0));
        c.setExpiration(String.valueOf(row.get("expiration")));
        c.setNameOnCard(String.valueOf(row.get("name_on_card")));
        c.setPan(String.valueOf(row.get("pan")));
        c.setProcessorUserId(String.valueOf(row.get("processor_user_id")));
        c.setStatus(String.valueOf(row.get("status")));
        return c;
    }, userId);

    return card;
  }
}
