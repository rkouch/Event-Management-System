package tickr.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Spark;
import tickr.persistence.DataModel;

public class Server {
    private static final int MIN_THREADS = 2;
    private static final int MAX_THREADS = 8;
    private static final int TIMEOUT_MS = 3000;

    static final Logger logger = LogManager.getLogger();

    private static DataModel dataModel;

    public static void start (int port, DataModel model) {
        dataModel = model;
        
        logger.debug("Adding routes!");
        Spark.port(port);
        Spark.threadPool(MAX_THREADS, MIN_THREADS, TIMEOUT_MS);

        Spark.get("/api/test", ((request, response) -> {
            logger.info("Received test request {} {}!", request.requestMethod(), request.pathInfo());
            String name = request.queryParams("name");
            if (name == null) {
                return "{'message' : 'Hello World!'}";
            } else {
                return String.format("{'message' : 'Hello %s!'}", name);
            }
        }));
    }
}
