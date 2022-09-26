package tickr.persistence;

public interface DataModel {
    ModelSession makeSession ();

    void cleanup ();
}
