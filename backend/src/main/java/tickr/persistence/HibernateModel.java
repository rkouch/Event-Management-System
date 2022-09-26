package tickr.persistence;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

public class HibernateModel implements DataModel {
    static final Logger logger = LogManager.getLogger();

    private final SessionFactory sessionFactory;
    private final StandardServiceRegistry registry;

    public HibernateModel () {
        registry = new StandardServiceRegistryBuilder().configure().build();
        try {
            sessionFactory = new MetadataSources(registry).buildMetadata().buildSessionFactory();
        } catch (Exception e) {
            logger.error("Failed to initialise Hibernate session factory!");
            registry.close();
            throw new RuntimeException("Failed to initialise Hibernate model", e);
        }
        logger.info("Successfully initialised Hibernate database!");
    }

    @Override
    public synchronized ModelSession makeSession() {
        logger.debug("Making a Hibernate model session!");
        return new HibernateSession(sessionFactory.openSession());
    }

    @Override
    public void cleanup () {
        logger.info("Cleaning up Hibernate model!");
        sessionFactory.close();
        registry.close();
    }
}
