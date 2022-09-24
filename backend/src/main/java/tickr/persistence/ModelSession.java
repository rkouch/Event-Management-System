package tickr.persistence;

public interface ModelSession {

    void commit ();
    void rollback ();
}
