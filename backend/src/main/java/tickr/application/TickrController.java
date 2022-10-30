package tickr.application;

import io.jsonwebtoken.JwtException;
import jakarta.persistence.PersistenceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tickr.application.apis.ApiLocator;
import tickr.application.apis.email.IEmailAPI;
import tickr.application.apis.purchase.IPurchaseAPI;
import tickr.application.entities.*;
import tickr.application.serialised.SerializedLocation;
import tickr.application.serialised.combined.*;
import tickr.application.serialised.requests.*;
import tickr.application.serialised.responses.AuthTokenResponse;
import tickr.application.serialised.responses.CreateEventResponse;
import tickr.application.serialised.responses.EventAttendeesResponse;
import tickr.application.serialised.responses.EventViewResponse;
import tickr.application.serialised.responses.RequestChangePasswordResponse;
import tickr.application.serialised.responses.TestResponses;
import tickr.application.serialised.responses.TicketBookingsResponse;
import tickr.application.serialised.responses.TicketViewEmailResponse;
import tickr.application.serialised.responses.TicketViewResponse;
import tickr.application.serialised.responses.UserIdResponse;
import tickr.application.serialised.responses.ViewProfileResponse;
import tickr.application.serialised.responses.*;
import tickr.persistence.ModelSession;
import tickr.server.exceptions.BadRequestException;
import tickr.server.exceptions.ForbiddenException;
import tickr.server.exceptions.NotFoundException;
import tickr.server.exceptions.UnauthorizedException;
import tickr.util.CryptoHelper;
import tickr.util.FileHelper;
import tickr.util.Utils;

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
import java.util.function.Predicate;
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

    public User authenticateToken (ModelSession session, String authTokenStr) {
        return getTokenFromStr(session, authTokenStr).getUser();
    }

    private UUID parseUUID (String uuidStr) {
        try {
            return UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid uuid!", e);
        }
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

    private CreateEventResponse createEventInternal (ModelSession session, CreateEventRequest request, boolean checkDates) {
        if (request.authToken == null) {
            throw new UnauthorizedException("Missing auth token!");
        }

        if (!request.isValid() ) {
            throw new BadRequestException("Invalid event request!");
        }

        if (!request.isSeatingDetailsValid()) {
            throw new BadRequestException("Invalid seating details!");
        }

        if (request.location != null && !request.isLocationValid()) {
            throw new BadRequestException("Invalid location details!");
        }

        LocalDateTime startDate;
        LocalDateTime endDate;

        try {
            startDate = LocalDateTime.parse(request.startDate, DateTimeFormatter.ISO_DATE_TIME);
            endDate = LocalDateTime.parse(request.endDate, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new ForbiddenException("Invalid date time string!");
        }

        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Event start time is later than event end time!");
        }

        if (checkDates && startDate.isBefore(LocalDateTime.now(ZoneId.of("UTC")))) {
            throw new ForbiddenException("Cannot create event in the past!");
        }


        // getting user from token
        var user = authenticateToken(session, request.authToken);
        // creating location from request
        Location location = null;
        if (request.location != null) {
            location = new Location(request.location.streetNo, request.location.streetName, request.location.unitNo, request.location.postcode,
                    request.location.suburb, request.location.state, request.location.country, request.location.longitude, request.location.latitude);
            session.save(location);
        }

        // creating event from request
        Event event;
        if (request.picture == null) {
            event = new Event(request.eventName, user, startDate, endDate, request.description, location, request.getSeatCapacity(), "");
        } else {
            event = new Event(request.eventName, user, startDate, endDate, request.description, location, request.getSeatCapacity(),
                    FileHelper.uploadFromDataUrl("event", UUID.randomUUID().toString(), request.picture).orElseThrow(() -> new ForbiddenException("Invalid event image!")));
        }
        session.save(event);
        // creating seating plan for each section
        if (request.seatingDetails != null) {
            for (CreateEventRequest.SeatingDetails seats : request.seatingDetails) {
                SeatingPlan seatingPlan = new SeatingPlan(event, location, seats.section, seats.availability, seats.ticketPrice, seats.hasSeats);
                session.save(seatingPlan);
            }
        }

        if (request.tags != null) {
            for (String tagStr : request.tags) {
                Tag newTag = new Tag(tagStr);
                newTag.setEvent(event);
                event.addTag(newTag);
                session.save(newTag);
            }
        }

        if (request.categories != null) {
            for (String catStr : request.categories) {
                Category newCat = new Category(catStr);
                newCat.setEvent(event);
                event.addCategory(newCat);
                session.save(newCat);
            }
        }

        if (request.admins != null) {
            for (String admin : request.admins) {
                User userAdmin;
                try {
                    userAdmin = session.getById(User.class, UUID.fromString(admin))
                            .orElseThrow(() -> new ForbiddenException(String.format("Unknown account \"%s\".", admin)));
                } catch (IllegalArgumentException e) {
                    throw new ForbiddenException("invalid admin Id");
                }

                userAdmin.addAdminEvents(event);
                event.addAdmin(userAdmin);
            }
        }

        event.setLocation(location);

        return new CreateEventResponse(event.getId().toString());
    }

    public CreateEventResponse createEvent (ModelSession session, CreateEventRequest request) {
        return createEventInternal(session, request, true);
    }

    public CreateEventResponse createEventUnsafe (ModelSession session, CreateEventRequest request) {
        return createEventInternal(session, request, false);
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

        // localhost:3000/change_password/{email}/{reset_token}
        var resetUrl = String.format("http://localhost:3000/change_password/%s/%s", user.getEmail(), resetToken.getId().toString());

        var messageString = String.format("Please reset your Tickr account password <a href=\"%s\">here</a>.\n", resetUrl);

        ApiLocator.locateApi(IEmailAPI.class).sendEmail(user.getEmail(), "Tickr account password reset", messageString);

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

    public void editEvent (ModelSession session, EditEventRequest request) {
        Event event = session.getById(Event.class, UUID.fromString(request.getEventId()))
                        .orElseThrow(() -> new ForbiddenException("Invalid event"));
        User user = authenticateToken(session, request.getAuthToken());
        if (user.getId() != event.getHost().getId() && !event.getAdmins().contains(user)) {
            throw new ForbiddenException("User is not a host/admin of the event!");
        }

        if (!request.isSeatingDetailsValid()) {
            throw new BadRequestException("Invalid seating details!");
        }

        // if (event.hasTicketsBeenSold() && request.getSeatingDetails()!= null) {
        //     throw new ForbiddenException("Cannot edit seating details where tickets have been sold");
        // }

        if (request.picture == null) {
            event.editEvent(request, session, request.getEventName(), null, request.getLocation(),
         request.getStartDate(), request.getEndDate(), request.getDescription(), request.getCategories()
         , request.getTags(), request.getAdmins(), request.getSeatingDetails(), request.published);
        } else {
            event.editEvent(request, session, request.getEventName(), FileHelper.uploadFromDataUrl("profile", UUID.randomUUID().toString(), request.picture)
            .orElseThrow(() -> new ForbiddenException("Invalid data url!")), request.getLocation(),
         request.getStartDate(), request.getEndDate(), request.getDescription(), request.getCategories()
         , request.getTags(), request.getAdmins(), request.getSeatingDetails(), request.published);
        }
        return;
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
            EventViewResponse.SeatingDetails newSeats = new EventViewResponse.SeatingDetails(seats.getSection(), seats.getAvailableSeats(), seats.ticketPrice, seats.getTotalSeats(), seats.hasSeats);
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

        return new EventViewResponse(event.getHost().getId().toString(), event.getEventName(), event.getEventPicture(), location, event.getEventStart().toString(), event.getEventEnd().toString(), event.getEventDescription(), seatingResponse,
                                    admins, categories, tags, event.isPublished(), event.getSeatAvailability(), event.getSeatCapacity());
    }

    public void makeHost (ModelSession session, EditHostRequest request) {
        User newHost = session.getByUnique(User.class, "email", request.newHostEmail)
                            .orElseThrow(() -> new ForbiddenException("Invalid user"));
        User oldHost = authenticateToken(session, request.authToken);
        Event event = session.getById(Event.class, UUID.fromString(request.eventId))
                        .orElseThrow(() -> new ForbiddenException("Invalid event"));
        
        if (!event.getAdmins().contains(newHost)) {
            throw new BadRequestException("User is not an admin!");
        }
        event.getAdmins().remove(newHost);
        event.addAdmin(oldHost);
        event.setHost(newHost);
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

    public void eventDelete(ModelSession session, EventDeleteRequest request) {
        if (!request.isValid()) {
            throw new BadRequestException("Invalid request details!");
        }
        Event event = session.getById(Event.class, UUID.fromString(request.eventId))
                        .orElseThrow(() -> new ForbiddenException("Invalid event ID!"));
        User user = authenticateToken(session, request.authToken);
        if (!event.getHost().equals(user)) {
            throw new ForbiddenException("User is not the host of this event!"); 
        }
        event.onDelete(session);
        session.remove(event);
    }

    public void userDeleteAccount(ModelSession session, UserDeleteRequest request) {
        if (!request.isValid()) {
            throw new BadRequestException("Invalid request!");
        }

        var token = getTokenFromStr(session, request.authToken);
        User user = token.getUser();
        user.authenticatePassword(session, request.password, AUTH_TOKEN_EXPIRY);
        /*var tokenSet = new HashSet<>(user.getTokens());
        for (var i : tokenSet) {
            user.invalidateToken(session, i);
        }*/
        user.onDelete(session);
        session.remove(user);
    }

    public TicketReserve.Response ticketReserve (ModelSession session, TicketReserve.Request request) {
        var user = authenticateToken(session, request.authToken);
        if (request.eventId == null || request.ticketDateTime == null || request.ticketDetails == null || request.ticketDetails.size() == 0) {
            throw new BadRequestException("Invalid request!");
        }

        var ticketDatetime = request.getTicketTime();
        var eventId = parseUUID(request.eventId);

        return session.getById(Event.class, eventId)
                .map(e -> request.ticketDetails.stream()
                        .map(t -> e.makeReservations(session, user, request.getTicketTime(), t.section, t.quantity, t.seatNums))
                        .flatMap(Collection::stream)
                        .map(TicketReservation::getDetails)
                        .collect(Collectors.toList()))
                .map(TicketReserve.Response::new)
                .orElseThrow(() -> new ForbiddenException("Invalid event!"));
    }

    public TicketPurchase.Response ticketPurchase (ModelSession session, TicketPurchase.Request request) {
        var user = authenticateToken(session, request.authToken);
        if (request.ticketDetails == null || request.ticketDetails.size() == 0 || request.successUrl == null || request.cancelUrl == null
                || !Utils.isValidUrl(request.successUrl) || !Utils.isValidUrl(request.cancelUrl)) {
            throw new BadRequestException("Invalid request!");
        }

        
        var purchaseAPI = ApiLocator.locateApi(IPurchaseAPI.class);
        var orderId = UUID.randomUUID();
        var builder = purchaseAPI.makePurchaseBuilder(orderId.toString());

        for (var i : request.ticketDetails) {
            builder = session.getById(TicketReservation.class, UUID.fromString(i.requestId))
                    .orElseThrow(() -> new ForbiddenException("Invalid ticket reservation!"))
                    .registerPurchaseItem(session, builder, orderId, user, i.firstName, i.lastName, i.email);
        }

        return new TicketPurchase.Response(purchaseAPI.registerOrder(builder.withUrls(request.successUrl, request.cancelUrl)));
    }

    public void ticketPurchaseSuccess (ModelSession session, String reserveId) {
        logger.info("Ticket purchase {} success!", reserveId);
        for (var i : session.getAllWith(PurchaseItem.class, "purchaseId", UUID.fromString(reserveId))) {
            session.save(i.convert(session));
            //session.remove(i);
        }
    }

    public void ticketPurchaseCancel (ModelSession session, String reserveId) {
        logger.info("Order {} was cancelled!", reserveId);
        for (var i : session.getAllWith(PurchaseItem.class, "purchaseId", UUID.fromString(reserveId))) {
            i.cancel(session);
            //session.remove(i);
        }
    }

    public void ticketPurchaseFailure (ModelSession session, String reserveId) {
        logger.info("Order {} failed!", reserveId);
        for (var i : session.getAllWith(PurchaseItem.class, "purchaseId", UUID.fromString(reserveId))) {
            i.cancel(session);
            //session.remove(i);
        }
    }

    public TicketViewResponse ticketView (ModelSession session, Map<String, String> params) {
        if (!params.containsKey("ticket_id")) {
            throw new BadRequestException("Missing ticket ID!");
        }
        Ticket ticket = session.getById(Ticket.class, UUID.fromString(params.get("ticket_id")))
                            .orElseThrow(() -> new ForbiddenException("Invalid ticket ID!"));
        return ticket.getTicketViewResponse();
    }

    public TicketBookingsResponse ticketBookings (ModelSession session, Map<String, String> params) {
        if (!params.containsKey("auth_token")) {
            throw new BadRequestException("Missing token!");
        }
        if (!params.containsKey("event_id")) {
            throw new BadRequestException("Missing event ID!");
        }
        User user = authenticateToken(session, params.get("auth_token"));
        Event event = session.getById(Event.class, UUID.fromString(params.get("event_id")))
                        .orElseThrow(() -> new ForbiddenException("Invalid event ID!"));
        // if (event.getHost() != user && !event.getAdmins().contains(user)) {
        //     throw new BadRequestException("User is not admin/host of the event!");
        // }
        return new TicketBookingsResponse(event.getUserTicketIds(user));
    }

    public TicketViewEmailResponse TicketViewSendEmail (ModelSession session, TicketViewEmailRequest request) {
        // logger.info("{}", request.authToken);
        // logger.info("{}", request.email);
        // logger.info("{}", request.ticketId);
        if (!request.isValid()) {
            throw new BadRequestException("Invalid request details!");
        }

        User recipient = session.getByUnique(User.class, "email", request.email)
                            .orElseThrow(() -> new ForbiddenException("Invalid email!"));
        Ticket ticket = session.getById(Ticket.class, UUID.fromString(request.ticketId))
                            .orElseThrow(() -> new ForbiddenException("Invalid Ticket ID"));
        User user = authenticateToken(session, request.authToken);
        if (!user.getTicketIds().contains(request.ticketId)) {
            throw new BadRequestException("User does not contain this ticket!");
        }

        var ticketUrl = String.format("http://localhost:3000/ticket/%s", request.ticketId);

        var message = String.format("Please click below to view your ticket <a href=\"%s\">here</a>.\n", ticketUrl);

        ApiLocator.locateApi(IEmailAPI.class).sendEmail(recipient.getEmail(), "View user ticket details", message);

        return new TicketViewEmailResponse(true);
    } 

    public EventAttendeesResponse GetEventAttendees (ModelSession session, Map<String, String> params) {
        if (!params.containsKey("auth_token")) {
            throw new BadRequestException("Missing token!");
        }
        if (!params.containsKey("event_id")) {
            throw new BadRequestException("Missing event ID!");
        }

        Event event = session.getById(Event.class, UUID.fromString(params.get("event_id")))
                            .orElseThrow(() -> new ForbiddenException("Invalid Event ID!")); 
        User user = authenticateToken(session, params.get("auth_token"));
        if (!event.getHost().equals(user)) {
            throw new ForbiddenException("User is not the host of this event!");
        }
       
        return new EventAttendeesResponse(event.getAttendees()); 
    }
    
    public ReviewCreate.Response reviewCreate (ModelSession session, ReviewCreate.Request request) {
        var user = authenticateToken(session, request.authToken);
        if (request.eventId == null) {
            throw new BadRequestException("Null event id!");
        }

        var event = session.getById(Event.class, UUID.fromString(request.eventId))
                .orElseThrow(() -> new ForbiddenException("Invalid event id!"));

        var comment = event.addReview(session, user, request.title, request.text, request.rating);
        //session.save(comment);

        return new ReviewCreate.Response(comment.getId().toString());
    }

    public ReviewsViewResponse reviewsView (ModelSession session, Map<String, String> params) {
        User user = null;
        if (params.containsKey("auth_token")) {
            user = authenticateToken(session, params.get("auth_token"));
        }

        if (!params.containsKey("event_id")) {
            throw new BadRequestException("Missing event id!");
        }

        var event = session.getById(Event.class, UUID.fromString(params.get("event_id")))
                .orElseThrow(() -> new ForbiddenException("Invalid event id!"));

        if (!params.containsKey("page_start") || !params.containsKey("max_results")) {
            throw new BadRequestException("Missing paging params!");
        }

        int pageStart;
        int maxResults;
        try {
            pageStart = Integer.parseInt(params.get("page_start"));
            maxResults = Integer.parseInt(params.get("max_results"));
        } catch (NumberFormatException e) {
            throw new ForbiddenException("Invalid paging values!", e);
        }

        if (pageStart < 0 || maxResults <= 0 || maxResults > 256) {
            throw new ForbiddenException("Invalid paging values!");
        }
        var numItems = new AtomicInteger();
        var reviews = session.getAllWithStream(Comment.class, "event", event)
                .filter(Predicate.not(Comment::isReply))
                .peek(c -> numItems.getAndIncrement())
                .sorted(Comparator.comparing(Comment::getCommentTime))
                .skip(pageStart)
                .limit(maxResults)
                .map(Comment::makeSerialisedReview)
                .collect(Collectors.toList());

        return new ReviewsViewResponse(reviews, numItems.get());
    }

    public ReplyCreate.Response replyCreate (ModelSession session, ReplyCreate.Request request) {
        var user = authenticateToken(session, request.authToken);
        if (request.reviewId == null) {
            throw new BadRequestException("Null review id!");
        }

        var comment = session.getById(Comment.class, UUID.fromString(request.reviewId))
                .orElseThrow(() -> new ForbiddenException("Unknown review id!"));

        var reply = comment.addReply(user, request.reply);
        session.save(reply);

        return new ReplyCreate.Response(reply.getId().toString());
    }

    public RepliesViewResponse repliesView (ModelSession session, Map<String, String> params) {
        User user = null;
        if (params.containsKey("auth_token")) {
            user = authenticateToken(session, params.get("auth_token"));
        }
        if (!params.containsKey("review_id") || !params.containsKey("page_start") || !params.containsKey("max_results")) {
            throw new BadRequestException("Invalid request params!");
        }

        var review = session.getById(Comment.class, UUID.fromString(params.get("review_id")))
                .orElseThrow(() -> new ForbiddenException("Invalid review id!"));

        int pageStart;
        int maxResults;
        try {
            pageStart = Integer.parseInt(params.get("page_start"));
            maxResults = Integer.parseInt(params.get("max_results"));
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid paging values!", e);
        }

        if (pageStart < 0 || maxResults <= 0 || maxResults > 256) {
            throw new BadRequestException("Invalid paging values!");
        }


        var numResults = new AtomicInteger();
        var replies = review.getReplies()
                .peek(i -> numResults.incrementAndGet())
                .sorted(Comparator.comparing(Comment::getCommentTime))
                .skip(pageStart)
                .limit(maxResults)
                .map(Comment::makeSerialisedReply)
                .collect(Collectors.toList());

        return new RepliesViewResponse(replies, numResults.get());
    }

    public EventHostingsResponse eventHostings (ModelSession session, Map<String, String> params) {
        if (!params.containsKey("auth_token")) {
            throw new BadRequestException("Missing auth_token!");
        }
        if (!params.containsKey("page_start") || !params.containsKey("max_results")) {
            throw new BadRequestException("Invalid paging details!");
        }
        User user = authenticateToken(session, params.get("auth_token"));
        var eventHostingIds = user.getPaginatedHostedEvents(Integer.parseInt(params.get("page_start")), Integer.parseInt(params.get("max_results")));
        return new EventHostingsResponse(eventHostingIds, user.getHostingEvents().size());
    }
    
    public void commentReact (ModelSession session, ReactRequest request) {
        var user = authenticateToken(session, request.authToken);
        if (request.commentId == null || request.reactType == null) {
            throw new BadRequestException("Missing comment id or react type!");
        }

        var comment = session.getById(Comment.class, UUID.fromString(request.commentId))
                .orElseThrow(() -> new ForbiddenException("Invalid comment id!"));

        comment.react(session, user, request.reactType);
    }
}
