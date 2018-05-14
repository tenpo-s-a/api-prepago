package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.prepaid.domain.*;

import java.util.List;
import java.util.Map;

/**
 * @author vutreras
 */
public interface PrepaidEJB10 {

  Map<String, Object> info() throws Exception;

  PrepaidTopup topupUserBalance(Map<String, Object> headers, NewPrepaidTopup topupRequest) throws Exception;

  void reverseTopupUserBalance(Map<String, Object> headers, NewPrepaidTopup topupRequest);

  List<PrepaidTopup> getUserTopups(Map<String, Object> headers, Long userId);

  PrepaidUserSignup initUserSignup(Map<String, Object> headers, NewPrepaidUserSignup signupRequest);

  PrepaidUserSignup getUserSignup(Map<String, Object> headers, Long signupId);

  PrepaidCard issuePrepaidCard(Map<String, Object> headers, Long userId);

  PrepaidCard getPrepaidCard(Map<String, Object> headers, Long userId);

  /**
   *  Calcula la comision y total a cargar segun el el tipo de carga (POS/WEB)
   *
   * @param topup con el cual se calculara la comision y total
   * @throws IllegalStateException cuando el topup es null
   * @throws IllegalStateException cuando el topup.amount es null
   * @throws IllegalStateException cuando el topup.amount.value es null
   * @throws IllegalStateException cuando el topup.merchantCode es null o vacio
   */
  void calculateTopupFeeAndTotal(PrepaidTopup topup) throws Exception;
}
