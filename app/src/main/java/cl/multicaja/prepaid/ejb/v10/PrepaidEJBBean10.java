package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDelegate10;
import cl.multicaja.prepaid.domain.*;
import cl.multicaja.helpers.ejb.v10.HelpersEJBBean10;
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

  @EJB
  private HelpersEJBBean10 helpersEJB10;

  @Inject
  private PrepaidTopupDelegate10 delegate;

  @Override
  public Map<String, Object> info() throws Exception{
    Map<String, Object> map = new HashMap<>();
    map.put("class", this.getClass().getSimpleName());
    map.put("ejb_users", this.usersEJB10.info());
    map.put("ejb_helpers", this.helpersEJB10.info());
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

    topup.setId(1);
    topup.setUserId(1);
    topup.setStatus("exitoso");
    topup.setTimestamps(new Timestamps());

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
  public PrepaidCard getPrepaidCard(Map<String, Object> headers, Long userId) {
    return null;
  }
}
