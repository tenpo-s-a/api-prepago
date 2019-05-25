package cl.multicaja.prepaid.utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;

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


  public List<E> findAll() {
    CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
    CriteriaQuery<E> cq = cb.createQuery(entityClass);
    Root<E> rootEntry = cq.from(entityClass);
    CriteriaQuery<E> all = cq.select(rootEntry);
    TypedQuery<E> allQuery = getEntityManager().createQuery(all);
    return allQuery.getResultList();
  }

  public final E find(final ID id) {
    return getEntityManager().find(entityClass, id);
  }

  public final void delete(final E entity) {
    final EntityManager entityManager = getEntityManager();
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
