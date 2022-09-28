package tickr.mock;

import com.google.gson.Gson;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MockTable<T> implements IMockTable {
    private Class<T> entityClass;
    private List<T> entities;
    private Map<String, Function<T, Object>> columns;

    private List<IMockGenerator> generators;

    public MockTable (Class<T> entityClass) {
        this.entityClass = entityClass;
        entities = new ArrayList<>();
        columns = new HashMap<>();
        generators = new ArrayList<>();
    }

    private MockTable (MockTable<T> other) {
        Gson gson = new Gson();
        this.entityClass = other.entityClass;
        this.entities = other.entities.stream()
                .map(e -> gson.fromJson(gson.toJson(e), entityClass))
                .collect(Collectors.toList());
        this.columns = other.columns;
        this.generators = other.generators.stream()
                .map(IMockGenerator::copy)
                .collect(Collectors.toList());
    }

    public List<T> getAll () {
        return new ArrayList<>(entities);
    }

    public List<T> getAllWith (String col, Object data) {
        if (!columns.containsKey(col)) {
            throw new RuntimeException(String.format("Column %s not in mock table!", col));
        }

        var column = columns.get(col);
        return entities.stream()
                .filter(t -> Objects.equals(column.apply(t), data))
                .collect(Collectors.toList());
    }

    private void addInternal (T entity) {
        entities.add(entity);
        for (var i : generators) {
            i.addGeneratedValue(entity);
        }
    }

    private void removeInternal (T entity) {
        entities.remove(entity);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> List<U> getAll (Class<U> uClass) {
        if (uClass != entityClass) {
            throw new RuntimeException("Incorrect class!");
        }
        return (List<U>)getAll();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> List<U> getAllWith (Class<U> uClass, String col, Object data) {
        if (uClass != entityClass) {
            throw new RuntimeException("Incorrect class!");
        }
        return (List<U>) getAllWith(col, data);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> void addColumn (Class<U> uClass, String col, Function<U, Object> colFunc) {
        if (uClass != entityClass) {
            throw new RuntimeException("Incorrect class!");
        }

        columns.put(col, (Function<T, Object>) colFunc);
    }

    @Override
    public void addGeneratedColumn (IMockGenerator generator) {
        generators.add(generator);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void add (Object entity) {
        if (entityClass.isInstance(entity)) {
            addInternal((T)entity);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void remove (Object entity) {
        if (entityClass.isInstance(entity)) {
            removeInternal((T)entity);
        }
    }

    @Override
    public IMockTable copy () {
        return new MockTable<>(this);
    }
}