package tickr;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    public static void main(String[] args) {
        System.out.println("Hello world!");
        logger.debug("Test debug!");
        logger.info("Test info!");
        logger.warn("Test warning!");
        logger.error("Test error!");
        logger.fatal("Test fatal!");
    }
}