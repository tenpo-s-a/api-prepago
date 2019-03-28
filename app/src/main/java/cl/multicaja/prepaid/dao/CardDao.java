package cl.multicaja.prepaid.dao;

import cl.multicaja.prepaid.model.v11.Card;
import cl.multicaja.prepaid.utils.PrepaidCrud;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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

}
