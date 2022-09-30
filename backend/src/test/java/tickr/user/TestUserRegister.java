package tickr.user;

import static org.junit.jupiter.api.Assertions.*;

import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tickr.TestHelper;
import tickr.application.TickrController;
import tickr.application.entities.User;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.persistence.DataModel;
import tickr.persistence.HibernateModel;
import tickr.server.exceptions.BadRequestException;
import tickr.server.exceptions.ForbiddenException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

public class TestUserRegister {
    private DataModel model;
    private TickrController controller;
    @BeforeEach
    public void setup () {
        model = new HibernateModel("hibernate-test.cfg.xml");
        controller = new TickrController();
    }


    @Test
    public void testEmptyValues () {
        var session = model.makeSession();
        assertThrows(BadRequestException.class, () -> controller.userRegister(session,
                new UserRegisterRequest("", "first", "last", "test@example.com",
                        "password123!!", "2022-04-14")));
        assertThrows(BadRequestException.class, () -> controller.userRegister(session,
                new UserRegisterRequest("test", "", "last", "test@example.com",
                        "password123!!", "2022-04-14")));
        assertThrows(BadRequestException.class, () -> controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "",
                        "password123!!", "2022-04-14")));
        assertThrows(BadRequestException.class, () -> controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "test@example.com",
                        "", "2022-04-14")));
        assertThrows(BadRequestException.class, () -> controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "test@example.com",
                        "password123!!", "")));
    }

    @Test
    public void testBadPassword () {
        // Too short
        var session = model.makeSession();
        assertThrows(ForbiddenException.class, () -> controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "test@example.com", "aA1!", "2022-04-14")));

        // No lower
        assertThrows(ForbiddenException.class, () -> controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "test@example.com", "AAAAAAAA1!", "2022-04-14")));

        // No upper
        assertThrows(ForbiddenException.class, () -> controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "test@example.com", "aaaaaaaa1!", "2022-04-14")));

        // No number
        assertThrows(ForbiddenException.class, () -> controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "test@example.com", "aaaaaaaaA!", "2022-04-14")));

        // No symbol
        assertThrows(ForbiddenException.class, () -> controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "test@example.com", "aaaaaaaaA1", "2022-04-14")));
    }

    @Test
    public void testBadEmail () {
        var session = model.makeSession();

        assertThrows(ForbiddenException.class, () -> controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "test", "Password123!", "2022-04-14")));

        assertThrows(ForbiddenException.class, () -> controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "test@", "Password123!", "2022-04-14")));

        assertThrows(ForbiddenException.class, () -> controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "test@example", "Password123!", "2022-04-14")));

        assertThrows(ForbiddenException.class, () -> controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "test@example.c", "Password123!", "2022-04-14")));

        assertThrows(ForbiddenException.class, () -> controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "@example.com", "Password123!", "2022-04-14")));
    }

    @Test
    public void testBadDOB () {
        var session = model.makeSession();

        assertThrows(ForbiddenException.class, () -> controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "test@example.com", "Password123!", "abdefg")));

        assertThrows(ForbiddenException.class, () -> controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "test", "Password123!", "2022")));

        assertThrows(ForbiddenException.class, () -> controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "test", "Password123!", "2022-04")));

        assertThrows(ForbiddenException.class, () -> controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "test", "Password123!", "04-14")));

        assertThrows(ForbiddenException.class, () -> controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "test", "Password123!", "20220414")));

        assertThrows(ForbiddenException.class, () -> controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "test", "Password123!", "14-04-2022")));

        assertThrows(ForbiddenException.class, () -> controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "test", "Password123!", "2022-13-14")));

        assertThrows(ForbiddenException.class, () -> controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "test", "Password123!", "2022-02-30")));

        assertThrows(ForbiddenException.class, () -> controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "test", "Password123!", "2022-02-29")));

        assertThrows(ForbiddenException.class, () -> controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "test", "Password123!", "2100-12-25")));
    }

    @Test
    public void testRegister () {
        var session = model.makeSession();
        var beforeDate = new Date();
        var authTokenString = controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "test@example.com",
                        "Password123!", "2022-04-14")).authToken;
        var afterDate = new Date();
        session = TestHelper.commitMakeSession(model, session);


        var authToken = Jwts.parserBuilder().build().parseClaimsJws(authTokenString);

        var id = authToken.getBody().getSubject();

        var userOpt = session.getById(User.class, id);
        assertTrue(userOpt.isPresent());
        var user = userOpt.orElse(null);

        assertTrue(beforeDate.before(authToken.getBody().getIssuedAt()) && afterDate.after(authToken.getBody().getIssuedAt()));
        assertEquals("test", user.getUsername());
        assertEquals("first", user.getFirstName());
        assertEquals("last", user.getLastName());
        assertEquals("test@example.com", user.getEmail());
        assertEquals(LocalDate.of(2022, 4, 14), user.getDob());
        assertEquals(UUID.fromString(id), user.getId());
    }

    @Test
    public void testTwoRegister () {
        var session = model.makeSession();

        var authTokenString1 = controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "test@example.com",
                        "Password123!", "2022-04-14")).authToken;
        session = TestHelper.commitMakeSession(model, session);


        var authToken1 = Jwts.parserBuilder().build().parseClaimsJws(authTokenString1);
        var id1 = authToken1.getBody().getSubject();

        var authTokenString2 = controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "test1@example.com",
                        "Password123!", "2022-04-14")).authToken;
        session = TestHelper.commitMakeSession(model, session);


        var authToken2 = Jwts.parserBuilder().build().parseClaimsJws(authTokenString1);
        var id2 = authToken2.getBody().getSubject();

        assertNotEquals(id1, id2);
        assertNotEquals(authTokenString1, authTokenString2);
    }

    @Test
    public void testSameEmail () {
        var session = model.makeSession();
        controller.userRegister(session,
                new UserRegisterRequest("test", "first", "last", "test@example.com",
                        "Password123!", "2022-04-14"));
        session = TestHelper.commitMakeSession(model, session);

        var finalSession = session;
        assertThrows(ForbiddenException.class, () -> controller.userRegister(finalSession,
                new UserRegisterRequest("test", "first", "last", "test@example.com",
                        "Password123!", "2022-04-14")));
    }
}
