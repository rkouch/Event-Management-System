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
    public List<TestEntity> getTestEntities () {
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<TestEntity> criteriaQuery = criteriaBuilder.createQuery(TestEntity.class);
        Root<TestEntity> criteriaRoot = criteriaQuery.from(TestEntity.class);
        criteriaQuery.select(criteriaRoot);

        Query<TestEntity> query = session.createQuery(criteriaQuery);

        return query.getResultList();
    }

    @Override
    public Optional<TestEntity> getTestEntity (int id) {
        CriteriaBuilder criteriaBuilder = session.getCriteriaBuilder();
        CriteriaQuery<TestEntity> criteriaQuery = criteriaBuilder.createQuery(TestEntity.class);
        Root<TestEntity> criteriaRoot = criteriaQuery.from(TestEntity.class);

        criteriaQuery.select(criteriaRoot).where(criteriaBuilder.equal(criteriaRoot.get("id"), id));

        Query<TestEntity> query = session.createQuery(criteriaQuery);

        var list = query.getResultList();

        return !list.isEmpty() ? Optional.of(list.get(0)) : Optional.empty();
    }

    @Override
    public void saveTestEntity (TestEntity entity) {
        session.persist(entity);
    }

    @Override
    public void removeTestEntity (TestEntity entity) {
        if (session.isReadOnly(entity)) {
            logger.error("Attempted to remove readonly entity!");
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
