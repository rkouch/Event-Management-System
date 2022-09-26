package tickr.persistence;

import tickr.application.entities.TestEntity;

import java.util.List;
import java.util.Optional;

public interface ModelSession {
    <T> List<T> getAll (Class<T> entityClass);
    <T, I> List<T> getAllWith (Class<T> entityClass, String col, I data);
    <T, I> Optional<T> getById (Class<T> entityClass, String idCol, I id);

    <T> void save (T entity);
    <T> void remove (T entity);


    void newTransaction ();

    void commit ();
    void rollback ();

    void close ();
}
