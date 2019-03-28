package cl.multicaja.prepaid.dao;

import cl.multicaja.prepaid.model.v11.Card;
import cl.multicaja.prepaid.utils.PrepaidCrud;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

public class CardDao extends PrepaidCrud<Card,Long> {

  private static final String ENV = "dev";

  public CardDao() {
    super(Card.class);
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

  public Card findByAccountId(Long accountId) {
    TypedQuery<Card> query = em.createQuery("SELECT c FROM Card c WHERE c.accountId = :accountId", Card.class);
    return query.setParameter("accountId", accountId).getSingleResult();
  }
}
