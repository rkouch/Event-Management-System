package tickr;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tickr.persistence.HibernateModel;
import tickr.server.Server;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    public static void main(String[] args) {
        logger.info("Starting tickr server!");
        var databaseModel = new HibernateModel();
        Server.start(8080, databaseModel);
    }
}