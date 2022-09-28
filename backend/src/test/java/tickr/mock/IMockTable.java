package tickr.mock;

import java.util.List;
import java.util.function.Function;

public interface IMockTable {
    <T> List<T> getAll (Class<T> tClass);
    <T> List<T> getAllWith (Class<T> tClass, String col, Object data);
    <T> void addColumn (Class<T> tClass, String col, Function<T, Object> colFunc);
    void addGeneratedColumn (IMockGenerator generator);
    void add (Object entity);
    void remove (Object entity);

    IMockTable copy ();
}
