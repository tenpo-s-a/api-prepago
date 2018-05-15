package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.core.exceptions.NotFoundException;
import cl.multicaja.core.exceptions.ValidationException;
import cl.multicaja.core.utils.ConfigUtils;
import cl.multicaja.core.utils.NumberUtils;
import cl.multicaja.core.utils.db.DBUtils;
import cl.multicaja.prepaid.async.v10.PrepaidTopupDelegate10;
import cl.multicaja.helpers.ejb.v10.HelpersEJBBean10;
import cl.multicaja.prepaid.domain.v10.*;
import cl.multicaja.users.ejb.v10.UsersEJBBean10;
import cl.multicaja.users.model.v10.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.ejb.*;
import javax.inject.Inject;
import java.math.BigDecimal;
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

  private final BigDecimal ONE_HUNDRED = new BigDecimal(100);

  // TODO: externalizar estos porcentajes?
  private final BigDecimal POS_COMMISSION_PERCENTAGE = new BigDecimal(0.5);
  private final BigDecimal IVA_PERCENTAGE = new BigDecimal(19);

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
  public PrepaidTopup10 topupUserBalance(Map<String, Object> headers, NewPrepaidTopup10 topupRequest) throws Exception {
    //TODO: lanzar las excepciones solo con el codigo del error especifico

    if(topupRequest == null || topupRequest.getAmount() == null){
      throw new ValidationException(2);
    }
    if(topupRequest.getRut() == null){
      throw new ValidationException(2);
    }
    if(StringUtils.isBlank(topupRequest.getMerchantCode())){
      throw new ValidationException(2);
    }
    if(StringUtils.isBlank(topupRequest.getTransactionId())){
      throw new ValidationException(2);
    }
    if(topupRequest.getAmount().getValue() == null){
      throw new ValidationException(2);
    }
    if(topupRequest.getAmount().getCurrencyCode() == null){
      throw new ValidationException(2);
    }

    // Obtener Usuario
    //User user = this.usersEJB10.getUserByRut(headers, topupRequest.getRut());
    User user = new User();
    if(user == null){
      throw new NotFoundException(1);
    }

    /*
      Validar nivel del usuario
        - N = 0 Usuario MC null, Prepaid user null o usuario bloqueado
        - N = 1 Primera carga
        - N > 1 Carga
     */
    // Buscar usuario local de prepago
    PrepaidUser10 prepaidUser = new PrepaidUser10();
    /*
      if(user.getGlobalStatus().equals("BLOQUEADO") || prepaidUser == null || prepaidUser.getStatus() == PrepaidUserStatus.DISABLED){
        // Si el usuario MC esta bloqueado o si no existe usuario local o el usuario local esta bloqueado, es N = 0
        throw new ValidationException(1024, "El cliente no pasó la validación");
      }
    */
    //if(user.getRut().getStatus().equals("VALIDADO_FOTO")){
    if(true){
      // Si el usuario tiene validacion de foto, es N = 2
      topupRequest.setFirstTopup(Boolean.FALSE);
    }

    /*
      Identificar ID Tipo de Movimiento
        - N = 1 -> Primera Carga
        - CodCom = WEB -> Carga WEB
        - CodCom != WEB -> Carga POS
     */
    CdtTransactionType cdtTpe = topupRequest.getCdtTransactionType();

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

      throw new ValidationException(2);
    }

    PrepaidTopup10 topup = new PrepaidTopup10(topupRequest);
    // Id Solicitud de carga devuelto por CDT
    topup.setId(numberUtils.random(1, Integer.MAX_VALUE));
    // UserId
    // topup.setUserId(user.getId());
    topup.setUserId(1);
    topup.setStatus("exitoso");
    topup.setTimestamps(new Timestamps10());

    /*
      Calcular monto a cargar y comisiones
     */
    this.calculateTopupFeeAndTotal(topup);

    /*
      Enviar mensaje a cosa de carga
     */
    // TODO: Enviar mensaje a cola de carga

    delegate.sendTopUp(topup, user);

    return topup;
  }

  @Override
  public void reverseTopupUserBalance(Map<String, Object> headers, NewPrepaidTopup10 topupRequest) {

  }

  @Override
  public List<PrepaidTopup10> getUserTopups(Map<String, Object> headers, Long userId) {
    return null;
  }

  @Override
  public PrepaidUserSignup10 initUserSignup(Map<String, Object> headers, NewPrepaidUserSignup10 signupRequest) {
    return null;
  }

  @Override
  public PrepaidUserSignup10 getUserSignup(Map<String, Object> headers, Long signupId) {
    return null;
  }

  @Override
  public PrepaidCard10 issuePrepaidCard(Map<String, Object> headers, Long userId) {
    return null;
  }

  @Override
  public PrepaidCard10 getPrepaidCard(Map<String, Object> headers, Long userId) {
    return null;
  }

  /**
   *  Calcula la comision y total a cargar segun el el tipo de carga (POS/WEB)
   *
   * @param topup al que se le calculara la comision y total
   * @throws IllegalStateException si el topup es null
   * @throws IllegalStateException si el topup.amount es null
   * @throws IllegalStateException si el topup.amount.value es null
   * @throws IllegalStateException si el topup.merchantCode es null o vacio
   */
  @Override
  public void calculateTopupFeeAndTotal(PrepaidTopup10 topup) throws Exception {

    if(topup == null || topup.getAmount() == null || topup.getAmount().getValue() == null || StringUtils.isBlank(topup.getMerchantCode())){
      throw  new IllegalStateException();
    }

    NewAmountAndCurrency10 total = new NewAmountAndCurrency10();
    total.setCurrencyCode(152);
    NewAmountAndCurrency10 fee = new NewAmountAndCurrency10();
    fee.setCurrencyCode(152);

    // Calcula las comisiones segun el tipo de carga (WEB o POS)
    switch (topup.getType()) {
      case WEB:
        fee.setValue(new BigDecimal(0));
        break;
      case POS:
        // MAX(100; 0,5% * prepaid_topup_new_amount_value) + IVA

        BigDecimal com = topup.getAmount().getValue().multiply(POS_COMMISSION_PERCENTAGE).divide(ONE_HUNDRED);
        // Calcula el max
        BigDecimal max = com.max(new BigDecimal(100));
        // Suma IVA
        fee.setValue(max.add(max.multiply(IVA_PERCENTAGE).divide(ONE_HUNDRED)));
        break;
    }
    // Calculo el total
    total.setValue(topup.getAmount().getValue().subtract(fee.getValue()));

    topup.setFee(fee);
    topup.setTotal(total);
  }
}
