package tickr.persistence;

/**
 * Interface for a data source. Is accessed across threads, so all functions must be
 * thread safe.
 */
public interface DataModel {
    ModelSession makeSession ();

    void cleanup ();
}
