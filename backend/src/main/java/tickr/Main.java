package tickr;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tickr.application.apis.ApiLocator;
import tickr.application.apis.purchase.IPurchaseAPI;
import tickr.application.apis.purchase.NullPurchaseAPI;
import tickr.persistence.HibernateModel;
import tickr.server.Server;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    public static void main(String[] args) {
        var databaseModel = new HibernateModel();

        int port;
        String frontendURL;

        if (args.length == 0) {
            // Use defaults
            port = 8080;
            frontendURL = null;
        } else if (args.length == 1) {
            // Parse server port
            port = Integer.parseInt(args[0]);
            frontendURL = null;
        } else {
            // Parse server port and frontend URL for CORS setup
            port = Integer.parseInt(args[0]);
            frontendURL = args[1];
        }

        ApiLocator.addLocator(IPurchaseAPI.class, () -> new NullPurchaseAPI(databaseModel));

        logger.info("Starting tickr server on http://localhost:{}!", port);

        Server.start(port, frontendURL, databaseModel);
    }
}