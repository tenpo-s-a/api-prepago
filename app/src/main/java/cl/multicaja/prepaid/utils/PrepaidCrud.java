package cl.multicaja.prepaid.utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;

public abstract class PrepaidCrud<E extends Serializable, ID extends Serializable> {

  private final transient Class<E> entityClass;
  protected abstract EntityManager getEntityManager();

  protected PrepaidCrud(Class<E> entityClass) {
    this.entityClass = entityClass;
  }

  public E insert(final E entity) {
    final EntityManager entityManager = getEntityManager();
    EntityTransaction tx = entityManager.getTransaction();
    tx.begin();
    entityManager.persist(entity);
    tx.commit();
    return entity;
  }


  public final E find(final ID id) {
    return getEntityManager().find(entityClass, id);
  }

  public final void delete(final ID id) {
    final EntityManager entityManager = getEntityManager();
    E entity = entityManager.find(entityClass, id);
    entityManager.remove(entity);
  }

  public E update(E entity){
    final EntityManager entityManager = getEntityManager();
    EntityTransaction tx = entityManager.getTransaction();
    tx.begin();
    entity = entityManager.merge(entity);
    tx.commit();
    return entity;
  }


}
