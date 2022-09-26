package tickr.persistence;

import tickr.application.entities.TestEntity;

import java.util.List;
import java.util.Optional;

public interface ModelSession {
    List<TestEntity> getTestEntities ();
    Optional<TestEntity> getTestEntity (int id);
    void saveTestEntity (TestEntity entity);
    void removeTestEntity (TestEntity entity);


    void newTransaction ();

    void commit ();
    void rollback ();

    void close ();
}
