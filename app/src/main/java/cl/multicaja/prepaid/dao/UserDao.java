package cl.multicaja.prepaid.dao;

import cl.multicaja.prepaid.model.v11.User;
import cl.multicaja.prepaid.utils.PrepaidCrud;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;

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

  public User findByUserId(Long userId) {
    User user = null;
    Query query = getEntityManager().createQuery("SELECT u FROM User u WHERE u.userId=:userIdValue");
    query.setParameter("userIdValue", userId);
    try {
      user = (User) query.getSingleResult();
    } catch (Exception e) {
      // Handle exception
    }
    return user;
  }
}
