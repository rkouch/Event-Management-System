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

        int port;
        String frontendURL;

        if (args.length == 0) {
            port = 8080;
            frontendURL = null;
        } else if (args.length == 1) {
            port = Integer.parseInt(args[0]);
            frontendURL = null;
        } else {
            port = Integer.parseInt(args[0]);
            frontendURL = args[1];
        }

        Server.start(port, frontendURL, databaseModel);
    }
}