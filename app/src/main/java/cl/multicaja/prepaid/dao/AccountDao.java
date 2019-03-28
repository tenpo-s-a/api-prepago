package cl.multicaja.prepaid.dao;

import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.utils.PrepaidCrud;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class AccountDao extends PrepaidCrud<Account,Long>{

  private static final String ENV = "dev";

  public AccountDao() {
    super(Account.class);
  }

  @PersistenceContext(unitName = ENV)
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public void setEm(EntityManager em) {
    this.em = em;
  }

}
