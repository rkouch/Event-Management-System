package tickr.mock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tickr.application.apis.email.IEmailAPI;

public class MockEmailAPI implements IEmailAPI {
    static final Logger logger = LogManager.getLogger();
    @Override
    public void sendEmail (String toEmail, String subject, String body) {
        // TODO
        logger.info("Email api received send request: \nto: {}, subject: \"{}\", message:\n{}", toEmail, subject, body);
    }
}
