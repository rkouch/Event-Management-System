package tickr.application;

import jakarta.persistence.PersistenceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;
import tickr.application.entities.TestEntity;
import tickr.application.entities.User;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.application.serialised.responses.AuthTokenResponse;
import tickr.application.serialised.responses.TestResponses;
import tickr.persistence.ModelSession;
import tickr.server.exceptions.BadRequestException;
import tickr.server.exceptions.ForbiddenException;
import tickr.server.exceptions.NotFoundException;

import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Class encapsulating business logic. Is created once per user session, which is distinct to ModelSession instances -
 * TickrController instances may persist for the duration a user interacts with the service, though this should not
 * be relied upon
 */
public class TickrController {
    // From https://stackoverflow.com/questions/201323/how-can-i-validate-an-email-address-using-a-regular-expression
    private static final Pattern EMAIL_REGEX = Pattern.compile("(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9]))\\.){3}(?:(2(5[0-5]|[0-4][0-9])|1[0-9][0-9]|[1-9]?[0-9])|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)])");
    private final Pattern PASS_REGEX = Pattern.compile("(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[^a-zA-Z0-9]).{8,}$");;

    private static final Duration AUTH_TOKEN_EXPIRY = Duration.ofDays(30);

    static final Logger logger = LogManager.getLogger();
    public TickrController () {

    }

    public TestEntity testGet (ModelSession session, Map<String, String> params) {
        if (!params.containsKey("id")) {
            throw new BadRequestException("Missing parameters!");
        }
        var entity = session.getById(TestEntity.class, UUID.fromString(params.get("id")));

        if (entity.isEmpty()) {
            throw new NotFoundException("No such id: " + UUID.fromString(params.get("id")));
        }

        return entity.orElse(null);
    }

    public TestResponses.GetAll testGetAll (ModelSession session) {
        return new TestResponses.GetAll(session.getAll(TestEntity.class));
    }

    public TestEntity testPost (ModelSession session, TestResponses.PostRequest request) {
        if (request.name == null || request.email == null) {
            throw new BadRequestException("Missing parameters!");
        }
        var entity = new TestEntity(request.name, request.email);
        session.save(entity);
        return entity;
    }

    public TestEntity testPut (ModelSession session, TestResponses.PutRequest request) {
        if (request.newName == null || request.newEmail == null) {
            throw new BadRequestException("Missing parameters!");
        }

        var entityOpt = session.getById(TestEntity.class, request.id);

        if (entityOpt.isEmpty()) {
            throw new NotFoundException("No such id: " + request.id);
        }

        var entity = entityOpt.orElse(null);

        entity.setName(request.newName);
        entity.setEmail(request.newEmail);

        // No save needed, any updates are synced (make sure to use setters)

        return entity;
    }

    public void testDelete (ModelSession session, TestResponses.DeleteRequest request) {
        var entityOpt = session.getById(TestEntity.class, request.id);

        if (entityOpt.isEmpty()) {
            throw new NotFoundException("No such id: " + request.id);
        }

        var entity = entityOpt.orElse(null);
        session.remove(entity);
    }

    public AuthTokenResponse userRegister (ModelSession session, UserRegisterRequest request) {
        if (!request.isValid()) {
            logger.debug("Missing parameters!");
            throw new BadRequestException("Invalid register request!");
        }

        if (!EMAIL_REGEX.matcher(request.email.trim()).matches()) {
            logger.debug("Email did not match regex!");
            throw new ForbiddenException("Invalid email!");
        }

        if (!PASS_REGEX.matcher(request.password.trim()).matches()) {
            logger.debug("Password did not match regex!");
            throw new ForbiddenException("Invalid password!");
        }

        LocalDate dob;
        try {
            dob = LocalDate.parse(request.dateOfBirth, DateTimeFormatter.ISO_DATE);
        } catch (DateTimeParseException e) {
            logger.debug("Date is in incorrect format!");
            throw new ForbiddenException("Invalid date of birth string!");
        }

        if (dob.isAfter(LocalDate.now(ZoneId.of("UTC")))) {
            logger.debug("Date of birth is in the future!");
            throw new ForbiddenException("Invalid date of birth!");
        }

        var user = new User(request.email.trim().toLowerCase(Locale.ROOT), request.password.trim(),
                request.userName.trim(), request.firstName.trim(), request.lastName.trim(), dob);

        try {
            session.save(user); // TODO: error handling
            session.commit();
            session.newTransaction();
        } catch (PersistenceException e) {
            throw new ForbiddenException(String.format("Email %s is already in use!", request.email.trim()), e);
        }

        var authToken = user.makeToken(session, AUTH_TOKEN_EXPIRY);

        return new AuthTokenResponse(authToken.makeJWT());
    }
}
