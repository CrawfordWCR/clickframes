package ${techspec.packageName}.dao.jpa;

import org.springframework.transaction.annotation.Transactional;

/**
 * @param <E>
 *            Type of entity this DAO covers
 */
@Transactional
public abstract class AbstractJpaDao<E> extends GenericAbstractDao<E> {
    // Not sure why, but persists don't work from parent method.
    @Override
    public E save(E entity) {
        return em.merge(entity);
    }
}