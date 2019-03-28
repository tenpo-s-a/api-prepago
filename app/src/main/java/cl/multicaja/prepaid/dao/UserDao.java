package cl.multicaja.prepaid.dao;

import cl.multicaja.prepaid.model.v11.User;
import cl.multicaja.prepaid.utils.PrepaidCrud;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class UserDao extends PrepaidCrud<User,Long> {

private static final String ENV = "dev";

public UserDao() {

  super(User.class);
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
