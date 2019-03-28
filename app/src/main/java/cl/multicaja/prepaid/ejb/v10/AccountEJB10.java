package cl.multicaja.prepaid.ejb.v10;

import cl.multicaja.prepaid.model.v11.Account;

public interface AccountEJB10 {

  /**
   *  Busca una cuenta/contrato por id y publica evento de cuenta creada
   * @param account cuenta a publicar
   * @throws Exception
   */
  void publishAccountCreatedEvent(Account account) throws Exception;
}
