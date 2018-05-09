package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.domain.*;
import cl.multicaja.helpers.ejb.v10.HelpersEJBBean10;
import cl.multicaja.users.ejb.v10.UsersEJBBean10;
import cl.multicaja.users.model.v10.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
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

  @Override
  public Map<String, Object> info() throws Exception{
    Map<String, Object> map = new HashMap<>();
    map.put("class", this.getClass().getSimpleName());
    map.put("ejb_users", this.usersEJB10.info());
    map.put("ejb_helpers", this.helpersEJB10.info());
    return map;
  }

  @Override
  public PrepaidTopup topupUserBalance(Map<String, Object> headers, NewPrepaidTopup topupRequest) throws Exception {
    Boolean isPosTransaction = Boolean.FALSE;

    //TODO: lanzar las excepciones solo con el codigo del error especifico

    if(topupRequest == null || topupRequest.getAmount() == null){
      throw new ValidationException(1024, "El cliente no pasó la validación");
    }
    if(topupRequest.getRut() == null){
      throw new ValidationException(1024, "El cliente no pasó la validación");
    }
    if(StringUtils.isBlank(topupRequest.getMerchantCode())){
      throw new ValidationException(1024, "El cliente no pasó la validación");
    }
    if(StringUtils.isBlank(topupRequest.getTransactionId())){
      throw new ValidationException(1024, "El cliente no pasó la validación");
    }
    if(topupRequest.getAmount().getValue() == null){
      throw new ValidationException(1024, "El cliente no pasó la validación");
    }
    if(topupRequest.getAmount().getCurrencyCode() == null){
      throw new ValidationException(1024, "El cliente no pasó la validación");
    }

    // Obtener Usuario
    //User user = this.usersEJB10.getUserByRut(headers, topupRequest.getRut());
    User user = new User();
    if(user == null){
      throw new NotFoundException(1);
    }

    /*
      Validar nivel del usuario
        - N > 0
        - N = 1 Primera carga
        - N > 1 Carga
     */
    //TODO: Validar nivel de usuario

    // Si N = 0 -> No cliente, No cliente prepago  o Cliente bloqueado
    if(false){
      throw new ValidationException(1024, "El cliente no pasó la validación");
    }

    /*
      Identificar ID Tipo de Movimiento
        - N = 1 -> Primera Carga
        - CodCom = WEB -> Carga WEB
        - CodCom != WEB -> Carga POS
     */
    //TODO: Identificar tipo de movimiento

    /*
      Validar movimiento en CDT, en caso de error lanzar exception
     */
    // TODO: Validar movimiento en CDT

    // Si no cumple con los limites
    if(false){
     /*
      En caso de ser TEF, iniciar proceso de devolucion
     */
      // TODO: Iniciar proceo de devolucion

      throw new ValidationException(1024, "El cliente no pasó la validación");
    }

    /*
      Calcular monto a cargar y comisiones
     */
    //TODO: Calcular monto y comisiones

    PrepaidTopup topup = new PrepaidTopup(topupRequest);
    // Id Solicitud de carga devuelto por CDT
    topup.setId(1);
    // UserId
    // topup.setUserId(user.getId());
    topup.setUserId(1);
    topup.setStatus("exitoso");
    topup.setTimestamps(new Timestamps());

    /*
      Enviar mensaje a cosa de carga
     */
    // TODO: Enviar mensaje a cola de carga

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
