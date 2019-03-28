package cl.multicaja.prepaid.ejb.v10;

public interface AccountEJB10 {

  /**
   *  Busca una cuenta/contrato por id y publica evento de cuenta creada
   * @param id id interno de la tarjeta
   * @throws Exception
   */
  void publishAccountCreatedEvent(Long id) throws Exception;
}
