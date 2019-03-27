package cl.multicaja.prepaid.dao;

import cl.multicaja.prepaid.model.v11.Cuenta;
import cl.multicaja.prepaid.utils.PrepaidCrud;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class CuentaDao extends PrepaidCrud<Cuenta,Long>{

  private static final String ENV = "dev";

  public CuentaDao() {
    super(Cuenta.class);
  }

  @PersistenceContext(name = ENV)
  private EntityManager em;

  @Override
  protected EntityManager getEntityManager() {
    return em;
  }

  public void setEm(EntityManager em) {
    this.em = em;
  }

}
