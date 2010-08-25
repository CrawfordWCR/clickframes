package ${techspec.packageName}.dao.jpa;

import java.util.HashSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public abstract class GenericAbstractDao<E> {
    protected final Log logger = LogFactory.getLog(getClass());
    @PersistenceContext
    protected EntityManager em;

    protected EntityManager getEntityManager() {
        return em;
    }

    /**
     * This is an emergency measure to manually initialize collection elements Trying to prevent failed to lazily
     * initialize a collection of role: org.chip.isg.grants.data.*.*, no session or session was closed
     */
    @SuppressWarnings("unchecked")
    protected void manuallyTouchCollectionElements(Collection children) {
        if (children == null) {
            logger.error("You passed a null collection");
            return;
        }
        for (Object child : children) {
            if (child == null) {
                logger.error("Why is this element null?  Looping through: " + children);
                continue;
            }
            child.hashCode();
        }
    }

    @SuppressWarnings("hiding")
    // that's the point of this parameter
    public <K, E> List<E> namedQuery(String queryName, String columnName, K key) {
        return namedQuery(queryName, columnName, key, true);
    }

    @SuppressWarnings("hiding")
    // that's the point of this parameter
    public <K, E> List<E> namedQuery(String queryName, String columnName, K key, boolean cacheable) {
        return namedQuery(queryName, columnName, key, null, null, cacheable);
    }

    @SuppressWarnings("hiding")
    // that's the point of this parameter
    public <K1, K2, E> List<E> namedQuery(String queryName, String columnName1, K1 key1, String columnName2, K2 key2,
            boolean cacheable) {
        Query qry = em.createNamedQuery(queryName);
        qry.setParameter(columnName1, key1);
        if (columnName2 != null) {
            qry.setParameter(columnName2, key2);
        }
        qry.setHint("org.hibernate.cacheable", cacheable);
        @SuppressWarnings("unchecked")
        List<E> results = qry.getResultList();
        if (cacheable) {
            // does this fix select parent.children from CachedObject queries?
            manuallyTouchCollectionElements(results);
        }
        return results;
    }

    @SuppressWarnings("hiding")
    // that's the point of this parameter
    public <K1, K2, E> List<E> namedQuery(String queryName, String columnName1, K1 key1, String columnName2, K2 key2) {
        return namedQuery(queryName, columnName1, key1, columnName2, key2, true);
    }

    @SuppressWarnings( { "hiding", "unchecked" })
    // that's the point of this parameter
    public <K, E> E namedQueryWithSingleResult(String queryName, String columnName, K key) {
        Query qry = em.createNamedQuery(queryName);
        qry.setParameter(columnName, key);
        qry.setHint("org.hibernate.cacheable", true);
        return (E) qry.getSingleResult();
    }

    // Why doesn't this method work from classes that extend Abstract DAO!?!?!?!
    public E save(E entity) {
        return em.merge(entity);
    }

    protected <K> List<E> queryBySingleArgument(String query, String columnName, K key) {
        Query qry = em.createQuery(query);
        qry.setParameter(columnName, key);
        @SuppressWarnings("unchecked")
        final List<E> resultList = qry.getResultList();
        if (resultList == null) {
            logger.error("no results, sending empty list");
            return new ArrayList<E>(0);
        }
        return resultList;
    }

    @SuppressWarnings("hiding")
    // we're hiding E on purpose
    protected <K, E> List<E> queryBySingleArgument(@SuppressWarnings("unused") Class<E> outputClass, String query,
            String columnName, K key) {
        Query qry = em.createQuery(query);
        qry.setParameter(columnName, key);
        @SuppressWarnings("unchecked")
        final List<E> resultList = qry.getResultList();
        return resultList;
    }

    @SuppressWarnings("hiding")
    // that's the point of this parameter
    protected <K, E> E queryWithSingleArgumentAndSingleResult(@SuppressWarnings("unused") Class<E> outputClass,
            String query, String columnName, K key) {
        Query qry = em.createQuery(query);
        qry.setParameter(columnName, key);
        try {
            @SuppressWarnings("unchecked")
            E singleResult = (E) qry.getSingleResult();
            return singleResult;
        } catch (javax.persistence.NoResultException e) {
            logger.warn("No result found. " + columnName + " = " + key + " query = " + query);
            return null;
        }
    }

    @SuppressWarnings("hiding")
    protected <K, E> E queryWithSingleArgumentAndSingleResult(String query, String columnName, K key) {
        Query qry = em.createQuery(query);
        qry.setParameter(columnName, key);
        try {
            @SuppressWarnings("unchecked")
            E singleResult = (E) qry.getSingleResult();
            return singleResult;
        } catch (javax.persistence.NoResultException e) {
            logger.warn("No result found. " + columnName + " = " + key + " query = " + query);
            return null;
        }
    }

    protected E stripDuplicates(List<E> resultList) {
        Set<E> tmp = new HashSet<E>(resultList);
        if (tmp.size() > 1) {
            logger.warn(tmp.size() + " results returned.  Your query returned more than one non-duplicate result");
        }
        for (E e : tmp) {
            return e;
        }
        logger.warn("no results returned");
        return null;
    }
}