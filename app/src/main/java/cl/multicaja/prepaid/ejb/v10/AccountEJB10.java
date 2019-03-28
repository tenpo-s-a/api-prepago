package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.prepaid.model.v11.Account;

public interface AccountEJB10 {

  /**
   *  Inserta una cuenta al usuario
   * @param userId id interno del usuario
   * @param accountNumber numero de cuenta
   * @throws Exception
   */
  Account insertAccount(Long userId, String accountNumber) throws Exception;

  /**
   *  Busca una cuenta/contrato por id y publica evento de cuenta creada
   * @param externalUserId id externo del usuario
   * @param acc cuenta a publicar
   * @throws Exception
   */
  void publishAccountCreatedEvent(Long externalUserId, Account acc) throws Exception;

}
