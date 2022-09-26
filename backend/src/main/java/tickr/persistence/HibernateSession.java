package tickr.persistence;

import jakarta.persistence.RollbackException;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import tickr.application.entities.TestEntity;

import java.util.List;
import java.util.Optional;

public class HibernateSession implements ModelSession {
    static final Logger logger = LogManager.getLogger();

    private Session session;
    boolean inTransaction;

    public HibernateSession (Session session) {
        this.session = session;
        session.beginTransaction();
        inTransaction = true;
    }

    @Override
    public <T> List<T> getAll (Class<T> entityClass) {
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<T> criteriaRoot = criteriaQuery.from(entityClass);
        criteriaQuery.select(criteriaRoot);

        Query<T> query = session.createQuery(criteriaQuery);

        return query.getResultList();
    }

    @Override
    public <T, I> List<T> getAllWith (Class<T> entityClass, String col, I data) {
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = criteriaBuilder.createQuery(entityClass);
        Root<T> criteriaRoot = criteriaQuery.from(entityClass);

        criteriaQuery.select(criteriaRoot).where(criteriaBuilder.equal(criteriaRoot.get(col), data));

        Query<T> query = session.createQuery(criteriaQuery);

        return query.getResultList();
    }

    @Override
    public <T, I> Optional<T> getById (Class<T> entityClass, String idCol, I id) {
        var l = getAllWith(entityClass, idCol, id);
        if (l.size() == 0) {
            return Optional.empty();
        } else if (l.size() == 1) {
            return Optional.of(l.get(0));
        } else {
            throw new RuntimeException(String.format("Id not unique: %s in column %s (%d results)!", id, idCol, l.size()));
        }
    }

    @Override
    public <T> void save (T entity) {
        session.persist(entity);
    }

    @Override
    public <T> void remove (T entity) {
        if (session.isReadOnly(entity)) {
            logger.error("Attempted to remove readonly entity: {}!", entity);
            return;
        }
        session.remove(entity);
    }

    @Override
    public void newTransaction () {
        if (!inTransaction) {
            logger.error("Attempted to create a new transaction in a HibernateSession when already in a transaction!");
            return;
        }
        session.beginTransaction();
    }


    @Override
    public void commit () throws RollbackException {
        logger.debug("Committing session!");
        session.flush();
        session.clear();
        session.getTransaction().commit();
        inTransaction = false;
    }

    @Override
    public void rollback () {
        session.getTransaction().rollback();
        inTransaction = false;
    }

    @Override
    public void close () {
        logger.debug("Closing session!");
        session.close();
    }


}
