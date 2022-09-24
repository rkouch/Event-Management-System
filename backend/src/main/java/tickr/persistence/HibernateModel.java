package tickr.persistence;

public class HibernateModel implements DataModel {
    @Override
    public synchronized ModelSession makeSession() {
        return new HibernateSession();
    }
}
