package tickr.application;

import io.jsonwebtoken.JwtException;
import jakarta.persistence.PersistenceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.exception.ConstraintViolationException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import tickr.application.entities.AuthToken;
import tickr.application.entities.Category;
import tickr.application.entities.Event;
import tickr.application.entities.Location;
import tickr.application.entities.ResetToken;
import tickr.application.entities.SeatingPlan;
import tickr.application.entities.Tag;
import tickr.application.entities.TestEntity;
import tickr.application.entities.User;
import tickr.application.serialised.SerializedLocation;
import tickr.application.serialised.combined.EventSearch;
import tickr.application.serialised.combined.NotificationManagement;
import tickr.application.serialised.requests.CreateEventRequest;
import tickr.application.serialised.requests.EditProfileRequest;
import tickr.application.serialised.requests.UserChangePasswordRequest;
import tickr.application.serialised.requests.UserCompleteChangePasswordRequest;
import tickr.application.serialised.requests.EventViewRequest;
import tickr.application.serialised.requests.UserLoginRequest;
import tickr.application.serialised.requests.UserLogoutRequest;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.application.serialised.requests.UserRequestChangePasswordRequest;
import tickr.application.serialised.responses.RequestChangePasswordResponse;
import tickr.application.serialised.responses.AuthTokenResponse;
import tickr.application.serialised.responses.CreateEventResponse;
import tickr.application.serialised.responses.EventViewResponse;
import tickr.application.serialised.responses.TestResponses;
import tickr.application.serialised.responses.UserIdResponse;
import tickr.application.serialised.responses.ViewProfileResponse;
import tickr.persistence.ModelSession;
import tickr.server.exceptions.BadRequestException;
import tickr.server.exceptions.ForbiddenException;
import tickr.server.exceptions.NotFoundException;
import tickr.server.exceptions.UnauthorizedException;
import tickr.util.CryptoHelper;
import tickr.util.FileHelper;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    private AuthToken getTokenFromStr (ModelSession session, String authTokenStr) {
        AuthToken token;
        try {
            var parsedToken = CryptoHelper.makeJWTParserBuilder()
                    .build()
                    .parseClaimsJws(authTokenStr);

            var tokenId = UUID.fromString(parsedToken.getBody().getId());

            token = session.getById(AuthToken.class, tokenId).orElseThrow(() -> new UnauthorizedException("Invalid auth token!"));
        } catch (JwtException | IllegalArgumentException e) {
            throw new UnauthorizedException("Invalid auth token!", e);
        }

        if (!token.makeJWT().equals(authTokenStr.trim())) {
            throw new UnauthorizedException("Invalid auth token!");
        }

        return token;
    }

    private User authenticateToken (ModelSession session, String authTokenStr) {
        return getTokenFromStr(session, authTokenStr).getUser();
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

        if (!EMAIL_REGEX.matcher(request.email.trim().toLowerCase()).matches()) {
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

    public AuthTokenResponse userLogin (ModelSession session, UserLoginRequest request) {
        if (!request.isValid()) {
            throw new BadRequestException("Invalid request!");
        }

        var user = session.getByUnique(User.class, "email", request.email)
                .orElseThrow(() -> new ForbiddenException(String.format("Unknown account \"%s\".", request.email)));


        return new AuthTokenResponse(user.authenticatePassword(session, request.password, AUTH_TOKEN_EXPIRY).makeJWT());
    }

    public void userLogout (ModelSession session, UserLogoutRequest request) {
        var token = getTokenFromStr(session, request.authToken);
        var user = token.getUser();
        user.invalidateToken(session, token);
    }

    public NotificationManagement.GetResponse userGetSettings (ModelSession session, Map<String, String> params) {
        if (!params.containsKey("auth_token")) {
            throw new UnauthorizedException("Missing auth token!");
        }
        var user = authenticateToken(session, params.get("auth_token"));
        return new NotificationManagement.GetResponse(user.getSettings());
    }

    public NotificationManagement.GetResponse userUpdateSettings (ModelSession session, NotificationManagement.UpdateRequest request) {
        if (request.authToken == null) {
            throw new UnauthorizedException("Missing auth token!");
        }

        if (request.settings == null) {
            throw new BadRequestException("Missing settings!");
        }

        var user = authenticateToken(session, request.authToken);
        user.setSettings(request.settings);
        return new NotificationManagement.GetResponse(user.getSettings());
    }

    public ViewProfileResponse userGetProfile (ModelSession session, Map<String, String> params) {
        if (params.containsKey("auth_token") == params.containsKey("user_id")) {
            throw new BadRequestException("Invalid request!");
        }

        User user;
        if (params.containsKey("auth_token")) {
            user = authenticateToken(session, params.get("auth_token"));
        } else {
            user = session.getById(User.class, UUID.fromString(params.get("user_id")))
                    .orElseThrow(() -> new ForbiddenException("Unknown user."));
        }

        return user.getProfile();
    }

    public CreateEventResponse createEvent (ModelSession session, CreateEventRequest request) {
        if (request.authToken == null) {
            throw new UnauthorizedException("Missing auth token!");
        }

        if (!request.isValid() ) {
            logger.debug("Missing parameters!");
            throw new BadRequestException("Invalid event request!");
        }

        if (!request.isSeatingDetailsValid()) {
            logger.debug("Missing seating parameters!");
            throw new BadRequestException("Invalid event request!");
        }

        if (!request.isLocationValid()) {
            logger.debug("Missing location parameters!");
            throw new BadRequestException("Invalid event request!");
        }

        LocalDateTime startDate;
        LocalDateTime endDate;

        try {
            startDate = LocalDateTime.parse(request.startDate, DateTimeFormatter.ISO_DATE_TIME);
            endDate = LocalDateTime.parse(request.endDate, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException e) {
            logger.debug("Date is in incorrect format!");
            throw new ForbiddenException("Invalid date time string!");
        }

        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Event start time is later than event end time!");
        }


        // getting user from token
        var user = authenticateToken(session, request.authToken); 
        // creating location from request 
        Location location = new Location(request.location.streetNo, request.location.streetName, request.location.unitNo, request.location.postcode,
                                        request.location.state, request.location.country, request.location.longitude, request.location.latitude);
        session.save(location);

        // creating event from request
        Event event;
        if (request.picture == null) {
            event = new Event(request.eventName, user, startDate, endDate, request.description, location, request.getSeatAvailability(), null);
        } else {
            event = new Event(request.eventName, user, startDate, endDate, request.description, location, request.getSeatAvailability(),
                    FileHelper.uploadFromDataUrl("event", UUID.randomUUID().toString(), request.picture).orElseThrow(() -> new ForbiddenException("Invalid event image!")));
        }
        session.save(event);
        // creating seating plan for each section
        for (CreateEventRequest.SeatingDetails seats : request.seatingDetails) {
            SeatingPlan seatingPlan = new SeatingPlan(event, location, seats.section, seats.availability);
            session.save(seatingPlan);
        }
        
        for (String tagStr : request.tags) {
            Tag newTag = new Tag(tagStr);
            newTag.setEvent(event);
            session.save(newTag);
            event.addTag(newTag);
        }
        for (String catStr : request.categories) {
            Category newCat = new Category(catStr);
            newCat.setEvent(event);
            session.save(newCat);
            event.addCategory(newCat); 
        }
        for (String admin : request.admins) {
            User userAdmin;
            try {
                userAdmin = session.getById(User.class, UUID.fromString(admin))
                .orElseThrow(() -> new ForbiddenException(String.format("Unknown account \"%s\".", admin)));
            } catch (IllegalArgumentException e) {
                throw new ForbiddenException("invalid admin Id");
            }
            
            userAdmin.addHostingEvent(event);
            event.addAdmin(userAdmin);
        }        
        event.setLocation(location);

        return new CreateEventResponse(event.getId().toString());
    }

    public void userEditProfile (ModelSession session, EditProfileRequest request) {
        var user = authenticateToken(session, request.authToken);

        if (request.email != null && !EMAIL_REGEX.matcher(request.email.trim().toLowerCase()).matches()) {
            logger.debug("Email did not match regex!");
            throw new ForbiddenException("Invalid email!");
        }

        if (request.pfpDataUrl == null) {
            user.editProfile(request.username, request.firstName, request.lastName, request.email, request.profileDescription, null);
        } else {
            user.editProfile(request.username, request.firstName, request.lastName, request.email, request.profileDescription,
                    FileHelper.uploadFromDataUrl("profile", UUID.randomUUID().toString(), request.pfpDataUrl)
                            .orElseThrow(() -> new ForbiddenException("Invalid data url!")));
        }
    }

    public AuthTokenResponse loggedChangePassword (ModelSession session, UserChangePasswordRequest request) {
        if (!request.isValid()) {
            throw new BadRequestException("Invalid request!");
        }

        if (!PASS_REGEX.matcher(request.password.trim()).matches()) {
            logger.debug("Password did not match regex!");
            throw new ForbiddenException("Invalid password!");
        }

        if (!PASS_REGEX.matcher(request.newPassword.trim()).matches()) {
            logger.debug("New password did not match regex!");
            throw new ForbiddenException("Invalid new password!");
        }
 
        var user = authenticateToken(session, request.authToken);
        user.authenticatePassword(session, request.password, AUTH_TOKEN_EXPIRY);
        user.changePassword(session, request.newPassword);
        var newAuthToken = user.makeToken(session, AUTH_TOKEN_EXPIRY).makeJWT();
 
        return new AuthTokenResponse(newAuthToken);
    }
 
    public RequestChangePasswordResponse unloggedChangePassword (ModelSession session, UserRequestChangePasswordRequest userRequestChangePasswordRequest) {
        if (!userRequestChangePasswordRequest.isValid()) {
            throw new BadRequestException("Invalid request!");
        }

        if (!EMAIL_REGEX.matcher(userRequestChangePasswordRequest.email.trim()).matches()) {
            logger.debug("Email did not match regex!");
            throw new ForbiddenException("Invalid email!");
        }
 
        var user = session.getByUnique(User.class, "email", userRequestChangePasswordRequest.email)
                .orElseThrow(() -> new ForbiddenException(String.format("Account does not exist.")));
        
        var resetToken = new ResetToken(user, Duration.ofHours(24));
        session.save(resetToken);
 
        return new RequestChangePasswordResponse(true);
    }
 
    public AuthTokenResponse unloggedComplete (ModelSession session, UserCompleteChangePasswordRequest request) {
        if (!request.isValid()) {
            throw new BadRequestException("Invalid request!");
        }

        if (!EMAIL_REGEX.matcher(request.email.trim()).matches()) {
            logger.debug("Email did not match regex!");
            throw new ForbiddenException("Invalid email!");
        }

        if (!PASS_REGEX.matcher(request.newPassword.trim()).matches()) {
            logger.debug("Password did not match regex!");
            throw new ForbiddenException("Invalid password!");
        }

        var user = session.getByUnique(User.class, "email", request.email)
                .orElseThrow(() -> new ForbiddenException(String.format("Account does not exist.")));

        user.changePassword(session, request.newPassword);
        var newAuthToken = user.makeToken(session, AUTH_TOKEN_EXPIRY).makeJWT();
 
        return new AuthTokenResponse(newAuthToken);
    }

    public UserIdResponse userSearch (ModelSession session, Map<String, String> params) {
        if (!params.containsKey("email")) {
            throw new BadRequestException("Missing email parameter!");
        }

        var email = params.get("email");

        if (!EMAIL_REGEX.matcher(email.trim().toLowerCase()).matches()) {
            throw new BadRequestException("Invalid email!");
        }

        var user = session.getByUnique(User.class, "email", email.toLowerCase())
                .orElseThrow(() -> new ForbiddenException("There is no user with email " + email + "."));

        return new UserIdResponse(user.getId().toString());
    }

    public EventViewResponse eventView (ModelSession session, Map<String, String> params) {
        if (!params.containsKey("event_id")) {
            throw new BadRequestException("Missing event_id!");
        }
        Event event = session.getById(Event.class, UUID.fromString(params.get("event_id")))
                        .orElseThrow(() -> new ForbiddenException("Unknown event"));
        List<SeatingPlan> seatingDetails = session.getAllWith(SeatingPlan.class, "event", event);

        List<EventViewResponse.SeatingDetails> seatingResponse = new ArrayList<EventViewResponse.SeatingDetails>();
        for (SeatingPlan seats : seatingDetails) {
            EventViewResponse.SeatingDetails newSeats = new EventViewResponse.SeatingDetails(seats.getSection(), seats.getAvailableSeats());
            seatingResponse.add(newSeats);
        }
        Set<String> tags = new HashSet<String>();
        for (Tag tag : event.getTags()) {
            tags.add(tag.getTags());
        }
        Set<String> categories = new HashSet<String>();
        for (Category category : event.getCategories()) {
            categories.add(category.getCategory());
        }
        Set<String> admins = new HashSet<String>();
        for (User admin : event.getAdmins()) {
            admins.add(admin.getId().toString());
        }
        SerializedLocation location = new SerializedLocation(event.getLocation().getStreetName(), event.getLocation().getStreetNo(), event.getLocation().getUnitNo(), event.getLocation().getSuburb(),
        event.getLocation().getPostcode(), event.getLocation().getState(), event.getLocation().getCountry(), event.getLocation().getLongitude(), event.getLocation().getLatitude());

        return new EventViewResponse(event.getEventName(), event.getEventPicture(), location, event.getEventStart().toString(), event.getEventEnd().toString(), event.getEventDescription(), seatingResponse,
                                    admins, categories, tags);
    }

    public EventSearch.Response searchEvents (ModelSession session, Map<String, String> params) {
        if (!params.containsKey("page_start") || !params.containsKey("max_results")) {
            throw new BadRequestException("Missing paging parameters!");
        }
        int pageStart;
        int maxResults;
        try {
            pageStart = Integer.parseInt(params.get("page_start"));
            maxResults = Integer.parseInt(params.get("max_results"));
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid paging parameters!");
        }

        if (pageStart < 0 || maxResults <= 0 || maxResults > 256) {
            throw new BadRequestException("Invalid paging parameters!");
        }

        User user = null;
        if (params.containsKey("auth_token")) {
            user = authenticateToken(session, params.get("auth_token"));
        }

        EventSearch.Options options = null;
        if (params.containsKey("search_options")) {
            options = EventSearch.fromParams(params.get("search_options"));
        }

        var eventStream = session.getAllStream(Event.class);

        var numItems = new AtomicInteger();
        var eventList = eventStream
                .peek(x -> numItems.incrementAndGet())
                .sorted(Comparator.comparing(Event::getEventStart))
                .skip(pageStart)
                .limit(maxResults)
                .map(Event::getId)
                .map(UUID::toString)
                .collect(Collectors.toList());

        return new EventSearch.Response(eventList, numItems.get());
    }
}
