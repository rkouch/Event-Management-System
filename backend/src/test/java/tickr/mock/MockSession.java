package tickr.mock;

import tickr.persistence.ModelSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class MockSession implements ModelSession {
    private MockModel model;

    private Map<Class<?>, IMockTable> changedTables;

    public MockSession (MockModel model) {
        this.model = model;
        this.changedTables = new HashMap<>();
    }

    private IMockTable getMockTable (Class<?> eClass) {
        if (!changedTables.containsKey(eClass)) {
            changedTables.put(eClass, model.getTable(eClass).copy());
        }

        return changedTables.get(eClass);
    }
    @Override
    public <T> List<T> getAll (Class<T> entityClass) {
        return getMockTable(entityClass).getAll(entityClass);
    }

    @Override
    public <T> Stream<T> getAllStream (Class<T> entityClass) {
        return getAll(entityClass).stream();
    }

    @Override
    public <T, I> List<T> getAllWith (Class<T> entityClass, String col, I data) {
        return getMockTable(entityClass).getAllWith(entityClass, col, data);
    }

    @Override
    public <T, I> Stream<T> getAllWithStream (Class<T> entityClass, String col, I data) {
        return null;
    }

    @Override
    public <T, I> Optional<T> getByUnique (Class<T> entityClass, String col, I data) {
        return Optional.empty();
    }

    @Override
    public <T, I> Optional<T> getById (Class<T> entityClass,  I id) {
        var l = getAllWith(entityClass, "id", id);
        if (l.size() == 0) {
            return Optional.empty();
        } else if (l.size() == 1) {
            return Optional.of(l.get(0));
        } else {
            throw new RuntimeException("Id not unique!");
        }
    }

    @Override
    public <T> void save (T entity) {
        getMockTable(entity.getClass()).add(entity);
    }

    @Override
    public <T> void remove (T entity) {
        getMockTable(entity.getClass()).remove(entity);
    }

    @Override
    public void newTransaction () {

    }

    @Override
    public void commit () {
        model.commit(changedTables);
        changedTables.clear();
    }

    @Override
    public void rollback () {
        changedTables.clear();
    }

    @Override
    public void close () {

    }
}
