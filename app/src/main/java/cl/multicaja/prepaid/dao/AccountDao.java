package cl.multicaja.prepaid.dao;

import cl.multicaja.prepaid.model.v11.Account;
import cl.multicaja.prepaid.utils.PrepaidCrud;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

public class AccountDao extends PrepaidCrud<Account,Long>{

  private static final String ENV = "dev";
  private static final String TABLE = "prepago.prp_cuenta";

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

  public Account findByUserId(Long userId) {
    TypedQuery<Account> query = em.createQuery("SELECT c FROM Account c WHERE c.userId = :userId", Account.class);
    return query.setParameter("userId", userId).getSingleResult();
  }

}
