package tickr.mock;

import tickr.persistence.DataModel;
import tickr.persistence.ModelSession;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class MockModel implements DataModel {
    private final Map<Class<?>, IMockTable> tables;

    public MockModel () {
        tables = new HashMap<>();
    }
    @Override
    public ModelSession makeSession () {
        return new MockSession(this);
    }

    @Override
    public void cleanup () {

    }

    public <T> void addTable (Class<T> tableClass) {
        tables.put(tableClass, new MockTable<>(tableClass));
    }

    public <T, V> void addGeneratedColumn (Class<T> tableClass, BiConsumer<T, V> generatedSetter, Function<Set<V>, V> generatorFunc, V start) {
        getTable(tableClass).addGeneratedColumn(IMockGenerator.makeGenerator(tableClass, generatedSetter, generatorFunc, start));
    }

    public <T, R> void registerTableColumn (Class<T> tableClass, String columnName, Function<T, R> tableFunc) {
        tables.get(tableClass).addColumn(tableClass, columnName, tableFunc::apply);
    }

    public void commit (Map<Class<?>, IMockTable> changedTables) {
        tables.putAll(changedTables);
    }

    public IMockTable getTable (Class<?> tableClass) {
        if (!tables.containsKey(tableClass)) {
            throw new RuntimeException(String.format("Table %s does not exist!", tableClass.getName()));
        }

        return tables.get(tableClass);
    }

    public ModelSession commitRemakeSession (ModelSession session) {
        session.commit();
        session.close();

        return makeSession();
    }
}
