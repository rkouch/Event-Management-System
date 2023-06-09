package tickr.application;

import io.jsonwebtoken.JwtException;
import jakarta.persistence.PersistenceException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tickr.application.apis.ApiLocator;
import tickr.application.apis.email.IEmailAPI;
import tickr.application.apis.location.ILocationAPI;
import tickr.application.apis.location.LocationPoint;
import tickr.application.apis.location.LocationRequest;
import tickr.application.apis.purchase.IPurchaseAPI;
import tickr.application.entities.*;
import tickr.application.recommendations.InteractionType;
import tickr.application.recommendations.RecommenderEngine;
import tickr.application.serialised.SerializedLocation;
import tickr.application.serialised.combined.*;
import tickr.application.serialised.requests.*;
import tickr.application.serialised.responses.EventReservedSeatsResponse.Reserved;
import tickr.application.serialised.responses.*;
import tickr.persistence.ModelSession;
import tickr.server.exceptions.BadRequestException;
import tickr.server.exceptions.ForbiddenException;
import tickr.server.exceptions.NotFoundException;
import tickr.server.exceptions.UnauthorizedException;
import tickr.util.CryptoHelper;
import tickr.util.FileHelper;
import tickr.util.Pair;
import tickr.util.Utils;

import java.time.*;
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

        ZonedDateTime startDate;
        ZonedDateTime endDate;
        try {
            startDate = ZonedDateTime.parse(request.startDate, DateTimeFormatter.ISO_DATE_TIME);
            endDate = ZonedDateTime.parse(request.endDate, DateTimeFormatter.ISO_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new ForbiddenException("Invalid date time string!", e);
        }

        if (startDate.isAfter(endDate)) {
            throw new BadRequestException("Event start time is later than event end time!");
        }
        if (checkDates && startDate.isBefore(ZonedDateTime.now(ZoneId.of("UTC")))) {
            throw new ForbiddenException("Cannot create event in the past!");
        }

        var user = authenticateToken(session, request.authToken);

        Location location = new Location(request.location.streetNo, request.location.streetName, request.location.unitNo, request.location.postcode,
                    request.location.suburb, request.location.state, request.location.country);
        session.save(location);
        location.lookupLongitudeLatitude();

        Event event;
        if (request.picture == null) {
            event = new Event(request.eventName, user, startDate, endDate, request.description, location, request.getSeatCapacity(), "", request.spotifyPlaylist);
        } else {
            event = new Event(request.eventName, user, startDate, endDate, request.description, location, request.getSeatCapacity(),
                            FileHelper.uploadFromDataUrl("event", UUID.randomUUID().toString(), request.picture)
                                    .orElseThrow(() -> new ForbiddenException("Invalid event image!")), request.spotifyPlaylist);   
        }
        session.save(event);
        event.handleCreateEvent(request, session, location);
        RecommenderEngine.forceRecalculate(session); // TODO
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

        String notification = "";
        if ((request.getStartDate() != null || request.getEndDate() != null) && (!event.getEventStart().equals(request.getStartDate()) || !event.getEventEnd().equals(request.getEndDate()))) {
            notification = String.format("Event dates has changed from %s -> %s to %s -> %s\n",
                event.getEventStart().toString(), event.getEventEnd().toString(), request.getStartDate(), request.getEndDate());
        }
        if (request.getDescription() != null && !event.getEventDescription().equals(request.getDescription())) {
            String notification2 = String.format("Event description has changed: %s", request.getDescription());
            notification.concat(notification2);
        }
        if (!notification.equals("") && !notification.equals(null)) {
            event.makeEventChangeNotification(user, notification);
        }
        
        if (request.picture == null) {
            event.editEvent(request, session, request.getEventName(), null, request.getLocation(),
                    request.getStartDate(), request.getEndDate(), request.getDescription(), request.getCategories(), 
                            request.getTags(), request.getAdmins(), request.getSeatingDetails(), request.published, request.spotifyPlaylist);
        } else {
            event.editEvent(request, session, request.getEventName(), FileHelper.uploadFromDataUrl("profile", UUID.randomUUID().toString(), request.picture)
                    .orElseThrow(() -> new ForbiddenException("Invalid data url!")), request.getLocation(),
                            request.getStartDate(), request.getEndDate(), request.getDescription(), request.getCategories(), 
                                    request.getTags(), request.getAdmins(), request.getSeatingDetails(), request.published, request.spotifyPlaylist);
            
        }
        RecommenderEngine.forceRecalculate(session); // TODO
        return;
    }

    public EventViewResponse eventView (ModelSession session, Map<String, String> params) {
        if (!params.containsKey("event_id")) {
            throw new BadRequestException("Missing event_id!");
        }
        Event event = session.getById(Event.class, UUID.fromString(params.get("event_id")))
                        .orElseThrow(() -> new ForbiddenException("Unknown event"));

        User user = null;
        if (params.containsKey("auth_token")) {
            user = authenticateToken(session, params.get("auth_token"));
        }

        if (!event.canView(user)) {
            throw new ForbiddenException("Unable to view event!");
        }

        if (user != null && !event.getHost().equals(user)) {
            RecommenderEngine.recordInteraction(session, user, event, InteractionType.VIEW);
        }

        return event.getEventViewResponse(session);
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
        RecommenderEngine.forceRecalculate(session); // TODO
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

        if (options != null && ((options.location != null && options.maxDistance == null) || (options.location == null && options.maxDistance != null)
                || (options.maxDistance != null && options.maxDistance < 0))) {
            throw new BadRequestException("Invalid location options!");
        }

        LocationPoint queryLocation = null;
        if (options != null && options.location != null) {
            queryLocation = ApiLocator.locateApi(ILocationAPI.class).getLocation(LocationRequest.fromSerialised(options.location));
        }

        var eventStream = session.getAllStream(Event.class)
                .filter(x -> !x.getEventEnd().isBefore(ZonedDateTime.now(ZoneId.of("UTC"))))
                .filter(Event::isPublished);

        if (options != null) {
            double maxDistance = options.maxDistance != null ? options.maxDistance : -1;
            var options1 = options;
            LocationPoint finalQueryLocation = queryLocation;
            eventStream = eventStream
                    .filter(e -> e.startsAfter(options1.getStartTime()))
                    .filter(e -> e.endsBefore(options1.getEndTime()))
                    .filter(e -> e.matchesCategories(options1.categories))
                    .filter(e -> e.matchesTags(options1.tags))
                    .filter(e -> e.matchesDescription(Utils.toWords(options1.text)))
                    .filter(e -> e.getLocation().getDistance(finalQueryLocation) <= maxDistance);
        }

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

        for (User i : event.getNotificationMembers()) {
            i.getNotificationEvents().remove(event);
        }

        event.makeEventCancelNotification(user);

        event.onDelete(session);
        session.remove(event);
        RecommenderEngine.forceRecalculate(session); // TODO
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
        Event event = session.getById(Event.class, UUID.fromString(request.eventId))
        .orElseThrow(() -> new ForbiddenException("Invalid event id!"));
        
        event.editNotificationMembers(session, user, user.doReminders());
        user.editEventNotificaitons(session, event, user.doReminders());

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

        return new TicketPurchase.Response(purchaseAPI.registerOrder(builder.withUrls(request.successUrl, request.cancelUrl)).getRedirectUrl());
    }

    public void reservationCancel (ModelSession session, ReserveCancelRequest request) {
        var user = authenticateToken(session, request.authToken);
        if (request.reservations.size() == 0) {
            throw new BadRequestException("Empty reservations!");
        }

        for (var i : request.reservations) {
            var entity = session.getById(TicketReservation.class, parseUUID(i))
                    .orElseThrow(() -> new ForbiddenException("Invalid ticket reservation!"));
            if (!entity.canCancel(user)) {
                throw new ForbiddenException("Unable to cancel reservation!");
            }
            session.remove(entity);
        }
    }

    public void ticketPurchaseSuccess (ModelSession session, String reserveId, String paymentId) {
        logger.info("Ticket purchase {} success!", reserveId);
        var purchaseItems = session.getAllWith(PurchaseItem.class, "purchaseId", UUID.fromString(reserveId));
        RecommenderEngine.recordInteraction(session, purchaseItems.get(0).getUser(), purchaseItems.get(0).getEvent(), InteractionType.TICKET_PURCHASE);
        for (var i : purchaseItems) {
            session.save(i.convert(session, paymentId));
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
        // if (!event.getHost().equals(user)) {
        //     throw new ForbiddenException("User is not the host of this event!");
        // }
       
        return new EventAttendeesResponse(event.getAttendees(user));
    }
    
    public ReviewCreate.Response reviewCreate (ModelSession session, ReviewCreate.Request request) {
        var user = authenticateToken(session, request.authToken);
        if (request.eventId == null) {
            throw new BadRequestException("Null event id!");
        }

        var event = session.getById(Event.class, UUID.fromString(request.eventId))
                .orElseThrow(() -> new ForbiddenException("Invalid event id!"));

        var comment = event.addReview(session, user, request.title, request.text, request.rating);
        RecommenderEngine.recordRating(session, user, event, request.rating);
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
                .sorted(Comparator.comparing(Comment::getCommentTime).reversed())
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

        if (!comment.getEvent().getHost().equals(user)) {
            RecommenderEngine.recordInteraction(session, user, comment.getEvent(), InteractionType.COMMENT);
        }

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
                .sorted(Comparator.comparing(Comment::getCommentTime).reversed())
                .skip(pageStart)
                .limit(maxResults)
                .map(Comment::makeSerialisedReply)
                .collect(Collectors.toList());

        return new RepliesViewResponse(replies, numResults.get());
    }

    public UserEventsResponse userEvents (ModelSession session, Map<String, String> params) {
        if (!params.containsKey("page_start") || !params.containsKey("max_results")) {
            throw new BadRequestException("Invalid paging details!");
        }

        var pageStart = Integer.parseInt(params.get("page_start"));
        var maxResults = Integer.parseInt(params.get("max_results"));

        if (pageStart < 0 || maxResults <= 0) {
            throw new BadRequestException("Invalid paging values!");
        }

        ZonedDateTime beforeDate;
        if (params.get("before") != null) {
            try {
                beforeDate = ZonedDateTime.parse(params.get("before"), DateTimeFormatter.ISO_DATE_TIME);
            } catch (DateTimeParseException e) {
                throw new BadRequestException("Invalid date time string!");
            }
        } else {
            beforeDate = null;
        }

        if (beforeDate != null && beforeDate.isBefore(ZonedDateTime.now(ZoneId.of("UTC")))) {
            throw new ForbiddenException("Cannot find events in the past!");
        }

        List<Event> events = session.getAll(Event.class);

        var numResults = new AtomicInteger();

        var eventIds = events.stream()
                .filter(beforeDate != null 
                    ? e -> e.getEventStart().isBefore(beforeDate) && e.getEventStart().isAfter(ZonedDateTime.now(ZoneId.of("UTC"))) 
                    : e -> e.getEventStart().isAfter(ZonedDateTime.now(ZoneId.of("UTC")))
                )
                .filter(e -> e.isPublished())
                .peek(i -> numResults.incrementAndGet())
                .sorted(Comparator.comparing(Event::getEventStart))
                .skip(pageStart)
                .limit(maxResults)
                .map(Event::getId)
                .map(UUID::toString)
                .collect(Collectors.toList());
        
        return new UserEventsResponse(eventIds, numResults.get());
    }

    public UserEventsResponse userEventsPast (ModelSession session, Map<String, String> params) {
        if (!params.containsKey("page_start") || !params.containsKey("max_results")) {
            throw new BadRequestException("Invalid paging details!");
        }

        var pageStart = Integer.parseInt(params.get("page_start"));
        var maxResults = Integer.parseInt(params.get("max_results"));

        if (pageStart < 0 || maxResults <= 0) {
            throw new BadRequestException("Invalid paging values!");
        }

        List<Event> events = session.getAll(Event.class);

        ZonedDateTime afterDate;
        if (params.get("after") != null) {
            try {
                afterDate = ZonedDateTime.parse(params.get("after"), DateTimeFormatter.ISO_DATE_TIME);
            } catch (DateTimeParseException e) {
                throw new BadRequestException("Invalid date time string!");
            }
        } else {
            afterDate = null;
        }

        var numResults = new AtomicInteger();

        var eventIds = events.stream()
                .filter(afterDate != null 
                    ? e -> e.getEventStart().isAfter(afterDate) && e.getEventStart().isBefore(ZonedDateTime.now(ZoneId.of("UTC"))) 
                    : e -> e.getEventStart().isBefore(ZonedDateTime.now(ZoneId.of("UTC")))
                )
                .filter(e -> e.isPublished())
                .peek(i -> numResults.incrementAndGet())
                .sorted(Comparator.comparing(Event::getEventStart))
                .skip(pageStart)
                .limit(maxResults)
                .map(Event::getId)
                .map(UUID::toString)
                .collect(Collectors.toList());
        
        return new UserEventsResponse(eventIds, numResults.get());
    }

    public EventReservedSeatsResponse eventReservedSeats (ModelSession session, Map<String, String> params) {
        if (!params.containsKey("auth_token")) {
            throw new BadRequestException("Missing auth_token!");
        }
        if (!params.containsKey("event_id")) {
            throw new BadRequestException("Missing event ID!");
        }
        User user = authenticateToken(session, params.get("auth_token"));
        Event event = session.getById(Event.class, UUID.fromString(params.get("event_id")))
                .orElseThrow(() -> new ForbiddenException("Invalid event ID!"));
        var seatingPlans = event.getSeatingPlans();
        List<TicketReservation> reservations = new ArrayList<>(); 
        seatingPlans.stream().forEach(s -> reservations.addAll(s.getReservations()));

        List<Reserved> reserved = new ArrayList<>(); 
        reservations.stream().forEach(r -> reserved.add(new Reserved(r.getSeatNum(), r.getSection().getSection())));

        return new EventReservedSeatsResponse(reserved);
    }

    public EventHostingsResponse eventHostings (ModelSession session, Map<String, String> params) {
        if (!params.containsKey("auth_token")) {
            throw new BadRequestException("Missing auth_token!");
        }
        if (!params.containsKey("page_start") || !params.containsKey("max_results")) {
            throw new BadRequestException("Invalid paging details!");
        }
        var pageStart = Integer.parseInt(params.get("page_start"));
        var maxResults = Integer.parseInt(params.get("max_results"));
        if (pageStart < 0 || maxResults <= 0) {
            throw new BadRequestException("Invalid paging values!");
        }

        User user = authenticateToken(session, params.get("auth_token"));
        // var eventHostingIds = user.getPaginatedHostedEvents(pageStart, maxResults);

        ZonedDateTime beforeDate;
        if (params.get("before") != null) {
            try {
                beforeDate = ZonedDateTime.parse(params.get("before"), DateTimeFormatter.ISO_DATE_TIME);
            } catch (DateTimeParseException e) {
                throw new BadRequestException("Invalid date time string!");
            }
        } else {
            beforeDate = null;
        }

        var numResults = new AtomicInteger();
        var eventIds = user.getStreamHostingEvents()
                .filter(beforeDate != null 
                    ? e -> e.getEventStart().isBefore(beforeDate) && e.getEventStart().isAfter(ZonedDateTime.now(ZoneId.of("UTC"))) 
                    : e -> e.getEventStart().isAfter(ZonedDateTime.now(ZoneId.of("UTC")))
                )
                .peek(i -> numResults.incrementAndGet())
                .sorted(Comparator.comparing(Event::getEventStart))
                .skip(pageStart)
                .limit(maxResults)
                .map(Event::getId)
                .map(UUID::toString)
                .collect(Collectors.toList());

        return new EventHostingsResponse(eventIds, numResults.get());
    }

    public EventHostingsResponse eventHostingsPast (ModelSession session, Map<String, String> params) {
        if (!params.containsKey("auth_token")) {
            throw new BadRequestException("Missing auth_token!");
        }
        if (!params.containsKey("page_start") || !params.containsKey("max_results")) {
            throw new BadRequestException("Invalid paging details!");
        }
        var pageStart = Integer.parseInt(params.get("page_start"));
        var maxResults = Integer.parseInt(params.get("max_results"));
        if (pageStart < 0 || maxResults <= 0) {
            throw new BadRequestException("Invalid paging values!");
        }

        User user = authenticateToken(session, params.get("auth_token"));
        // var eventHostingIds = user.getPaginatedHostedEvents(pageStart, maxResults);

        ZonedDateTime afterDate;
        if (params.get("after") != null) {
            try {
                afterDate = ZonedDateTime.parse(params.get("after"), DateTimeFormatter.ISO_DATE_TIME);
            } catch (DateTimeParseException e) {
                throw new BadRequestException("Invalid date time string!");
            }
        } else {
            afterDate = null;
        }

        var numResults = new AtomicInteger();
        var eventIds = user.getStreamHostingEvents()
                .filter(afterDate != null 
                    ? e -> e.getEventStart().isAfter(afterDate) && e.getEventStart().isBefore(ZonedDateTime.now(ZoneId.of("UTC"))) 
                    : e -> e.getEventStart().isBefore(ZonedDateTime.now(ZoneId.of("UTC")))
                )
                .peek(i -> numResults.incrementAndGet())
                .sorted(Comparator.comparing(Event::getEventStart))
                .skip(pageStart)
                .limit(maxResults)
                .map(Event::getId)
                .map(UUID::toString)
                .collect(Collectors.toList());

        return new EventHostingsResponse(eventIds, numResults.get());
    }

    public CustomerEventsResponse customerBookings (ModelSession session, Map<String, String> params) {
        if (!params.containsKey("auth_token")) {
            throw new BadRequestException("Missing auth_token!");
        }
        if (!params.containsKey("page_start") || !params.containsKey("max_results")) {
            throw new BadRequestException("Invalid paging details!");
        }

        User user = authenticateToken(session, params.get("auth_token"));

        var pageStart = Integer.parseInt(params.get("page_start"));
        var maxResults = Integer.parseInt(params.get("max_results"));
        if (pageStart < 0 || maxResults <= 0) {
            throw new BadRequestException("Invalid paging values!");
        }

        ZonedDateTime beforeDate;
        if (params.get("before") != null) {
            try {
                beforeDate = ZonedDateTime.parse(params.get("before"), DateTimeFormatter.ISO_DATE_TIME);
            } catch (DateTimeParseException e) {
                throw new BadRequestException("Invalid date time string!");
            }
        } else {
            beforeDate = null;
        }

        var numResults = new AtomicInteger();
        var eventIds = user.getTickets().stream()
                .map(Ticket::getEvent)
                .filter(beforeDate != null 
                    ? e -> e.getEventStart().isBefore(beforeDate) && e.getEventStart().isAfter(ZonedDateTime.now(ZoneId.of("UTC"))) 
                    : e -> e.getEventStart().isAfter(ZonedDateTime.now(ZoneId.of("UTC")))
                )
                .distinct()
                .peek(i -> numResults.incrementAndGet())
                .sorted(Comparator.comparing(Event::getEventStart))
                .skip(pageStart)
                .limit(maxResults)
                .map(Event::getId)
                .map(UUID::toString)
                .collect(Collectors.toList());

        
        return new CustomerEventsResponse(eventIds, numResults.get());
    }

    public CustomerEventsResponse customerBookingsPast (ModelSession session, Map<String, String> params) {
        if (!params.containsKey("auth_token")) {
            throw new BadRequestException("Missing auth_token!");
        }
        if (!params.containsKey("page_start") || !params.containsKey("max_results")) {
            throw new BadRequestException("Invalid paging details!");
        }

        User user = authenticateToken(session, params.get("auth_token"));

        var pageStart = Integer.parseInt(params.get("page_start"));
        var maxResults = Integer.parseInt(params.get("max_results"));
        if (pageStart < 0 || maxResults <= 0) {
            throw new BadRequestException("Invalid paging values!");
        }

        ZonedDateTime afterDate;
        if (params.get("after") != null) {
            try {
                afterDate = ZonedDateTime.parse(params.get("after"), DateTimeFormatter.ISO_DATE_TIME);
            } catch (DateTimeParseException e) {
                throw new BadRequestException("Invalid date time string!");
            }
        } else {
            afterDate = null;
        }

        var numResults = new AtomicInteger();
        var eventIds = user.getTickets().stream()
                .map(Ticket::getEvent)
                .filter(afterDate != null 
                    ? e -> e.getEventStart().isAfter(afterDate) && e.getEventStart().isBefore(ZonedDateTime.now(ZoneId.of("UTC"))) 
                    : e -> e.getEventStart().isBefore(ZonedDateTime.now(ZoneId.of("UTC")))
                )
                .distinct()
                .peek(i -> numResults.incrementAndGet())
                .sorted(Comparator.comparing(Event::getEventStart))
                .skip(pageStart)
                .limit(maxResults)
                .map(Event::getId)
                .map(UUID::toString)
                .collect(Collectors.toList());

        
        return new CustomerEventsResponse(eventIds, numResults.get());
    }

    public EventHostingFutureResponse eventHostingFuture (ModelSession session, Map<String, String> params) {
        if (!params.containsKey("email")) {
            throw new BadRequestException("Missing email!");
        }
        if (!params.containsKey("page_start") || !params.containsKey("max_results")) {
            throw new BadRequestException("Invalid paging details!");
        }

        User user = session.getByUnique(User.class, "email", params.get("email"))
                .orElseThrow(() -> new ForbiddenException("Invalid email!"));

        var pageStart = Integer.parseInt(params.get("page_start"));
        var maxResults = Integer.parseInt(params.get("max_results"));
        if (pageStart < 0 || maxResults <= 0) {
            throw new BadRequestException("Invalid paging values!");
        }

        var numResults = new AtomicInteger();
        var eventIds = user.getStreamHostingEvents()
                .filter(e -> e.getEventStart().isAfter(ZonedDateTime.now(ZoneId.of("UTC"))))
                .peek(i -> numResults.incrementAndGet())
                .sorted(Comparator.comparing(Event::getEventStart))
                .skip(pageStart)
                .limit(maxResults)
                .map(Event::getId)
                .map(UUID::toString)
                .collect(Collectors.toList());
        return new EventHostingFutureResponse(eventIds, numResults.get());
    }
    
    public EventHostingPastResponse eventHostingPast (ModelSession session, Map<String, String> params) {
        if (!params.containsKey("email")) {
            throw new BadRequestException("Missing email!");
        }
        if (!params.containsKey("page_start") || !params.containsKey("max_results")) {
            throw new BadRequestException("Invalid paging details!");
        }

        User user = session.getByUnique(User.class, "email", params.get("email"))
                .orElseThrow(() -> new ForbiddenException("Invalid email!"));

        var pageStart = Integer.parseInt(params.get("page_start"));
        var maxResults = Integer.parseInt(params.get("max_results"));
        if (pageStart < 0 || maxResults <= 0) {
            throw new BadRequestException("Invalid paging values!");
        }

        var numResults = new AtomicInteger();
        var eventIds = user.getStreamHostingEvents()
                .filter(e -> e.getEventStart().isBefore(ZonedDateTime.now(ZoneId.of("UTC"))))
                .peek(i -> numResults.incrementAndGet())
                .sorted(Comparator.comparing(Event::getEventStart))
                .skip(pageStart)
                .limit(maxResults)
                .map(Event::getId)
                .map(UUID::toString)
                .collect(Collectors.toList());
        return new EventHostingPastResponse(eventIds, numResults.get());
    }
    
    public void commentReact (ModelSession session, ReactRequest request) {
        var user = authenticateToken(session, request.authToken);
        if (request.commentId == null || request.reactType == null) {
            throw new BadRequestException("Missing comment id or react type!");
        }

        var comment = session.getById(Comment.class, UUID.fromString(request.commentId))
                .orElseThrow(() -> new ForbiddenException("Invalid comment id!"));

        comment.react(session, user, request.reactType);

        if (!comment.getEvent().getHost().equals(user)) {
            RecommenderEngine.recordInteraction(session, user, comment.getEvent(), InteractionType.REACT);
        }
    }

    public void makeAnnouncement (ModelSession session, AnnouncementRequest request) {
        var user = authenticateToken(session, request.authToken);
        if (request.eventId == null) {
            throw new BadRequestException("Missing event id!");
        }

        var event = session.getById(Event.class, parseUUID(request.eventId))
                .orElseThrow(() -> new ForbiddenException("Invalid event id!"));

        event.makeAnnouncement(user, request.announcement);
    }

    public String onPaymentCancel (ModelSession session, Map<String, String> params) {
        var orderId = params.get("order_id");
        var url = params.get("url");

        var list = new ArrayList<>(session.getAllWith(PurchaseItem.class, "purchaseId", UUID.fromString(orderId)));

        for (var i : list) {
            i.cancel(session);
            //session.remove(i);
        }

        return url;
    }

    public void reviewDelete (ModelSession session, ReviewDeleteRequest request) {
        if (request.commentId == null) {
            throw new BadRequestException("Missing comment ID!");
        }
        User user = authenticateToken(session, request.authToken);

        Comment review = session.getById(Comment.class, UUID.fromString(request.commentId))
                .orElseThrow(() -> new ForbiddenException("Invalid comment ID!"));

        if (user != review.getAuthor()) {
            throw new ForbiddenException("User is not author of this review!");
        }

        if (review.getParent() == null) {
            for (Comment reply : review.getChildren()) {
                session.remove(reply);
            }
        }

        session.remove(review);
    }   

    public GroupCreateResponse groupCreate (ModelSession session, GroupCreateRequest request) {
        if (request.reservedIds == null) {
            throw new BadRequestException("Missing reserved ids!");
        }
        if (request.hostReserveId == null) {
            throw new BadRequestException("Missing host reserve id!");
        }
        User user = authenticateToken(session, request.authToken);

        if (user.isGroupAlreadyCreated(request.reservedIds)) {
            throw new ForbiddenException("Group has already been created for reserve IDs!");
        }

        TicketReservation reserve = session.getById(TicketReservation.class, UUID.fromString(request.hostReserveId))
                .orElseThrow(() -> new ForbiddenException("Reserve ID does not exist!"));

        Group group = new Group(user, ZonedDateTime.now(ZoneId.of("UTC")), 1, request.getTicketReservations(session, reserve));

        session.save(group);
        user.addGroup(group);
        user.addOwnedGroup(group);
        group.addUser(user);
        group.addGroupToTicketReservations();
        
        return new GroupCreateResponse(group.getId().toString());
    }

    public GroupIdsResponse getGroupIds (ModelSession session, Map<String, String> params) {
        if (!params.containsKey("auth_token")) {
            throw new BadRequestException("Missing auth token!!");
        }
        if (!params.containsKey("page_start") || !params.containsKey("max_results")) {
            throw new BadRequestException("Missing paging details!");
        }
        User user = authenticateToken(session, params.get("auth_token"));

        var pageStart = Integer.parseInt(params.get("page_start"));
        var maxResults = Integer.parseInt(params.get("max_results"));
        if (pageStart < 0 || maxResults <= 0) {
            throw new BadRequestException("Invalid paging values!");
        }

        var numResults = new AtomicInteger();
        var groupIds = user.getGroups().stream()
                .peek(i -> numResults.incrementAndGet())
                .skip(pageStart)
                .limit(maxResults)
                .map(Group::getId)
                .map(UUID::toString)
                .collect(Collectors.toList());
        return new GroupIdsResponse(groupIds, numResults.get());
    }

    public GroupInviteResponse groupInvite(ModelSession session, GroupInviteRequest request) {
        if (request.groupId == null) {
            throw new BadRequestException("Invalid group ID!");
        }
        if (request.reserveId == null) {
            throw new BadRequestException("Invalid reserve ID!");
        }
        if (request.email == null || !EMAIL_REGEX.matcher(request.email.trim().toLowerCase()).matches()) {
            throw new BadRequestException("Invalid Email!");
        }

        User user = authenticateToken(session, request.authToken);

        Group group = session.getById(Group.class, UUID.fromString(request.groupId))
                .orElseThrow(() -> new BadRequestException("Group ID does not exist!"));

        TicketReservation reserve = session.getById(TicketReservation.class, UUID.fromString(request.reserveId))
                .orElseThrow(() -> new BadRequestException("Reserve ID does not exist!"));

        if (reserve.isGroupAccepted()) {
            throw new BadRequestException("Cannot send invitation for a reserve ID that has been accepted!");
        }

        User inviteUser = session.getByUnique(User.class, "email", request.email)
                .orElseThrow(() -> new BadRequestException("User with email does not exist!"));
        if (user.equals(inviteUser)) {
            throw new BadRequestException("Host cannot send invite to themself!");
        }

        reserve.setExpiry(ZonedDateTime.now(ZoneId.of("UTC")).plus(Duration.ofHours(24)));

        Invitation invitation;
        if (reserve.getInvitation() == null) {
            invitation = new Invitation(group, reserve, inviteUser);
            session.save(invitation);
            invitation.handleInvitation(group, reserve, inviteUser);
        } else {
            invitation = session.getByUnique(Invitation.class, "ticketReservation", reserve)
                    .orElseThrow(() -> new BadRequestException("Invitation does not exist for this reserve ID!"));
            if (!invitation.getUser().equals(inviteUser)) {
                throw new BadRequestException("Invitation has already been sent to another user!");
            }
        }

        // logger.info("{}", invitation.getId().toString());

        var inviteUrl = String.format("http://localhost:3000/ticket/purchase/group/%s", invitation.getId().toString());
        var message = String.format("Please click below to view your group invitation <a href=\"%s\">here</a>.\n", inviteUrl);
        ApiLocator.locateApi(IEmailAPI.class).sendEmail(request.email, "User group invitation", message);

        return new GroupInviteResponse(true);
    }

    public GroupAcceptResponse groupAccept(ModelSession session, GroupAcceptRequest request) {
        if (request.inviteId == null) {
            throw new BadRequestException("Invalid invite ID!");
        }
        User user = authenticateToken(session, request.authToken);
        Invitation invitation = session.getById(Invitation.class, UUID.fromString(request.inviteId))
                .orElseThrow(() -> new BadRequestException("Invitation does not exist for this invite ID!"));

        invitation.acceptInvitation(user);
        session.remove(invitation);
        return new GroupAcceptResponse(invitation.getTicketReservation().getId().toString());
    }
    
    public void groupDeny(ModelSession session, GroupDenyRequest request) {
        if (request.inviteId == null) {
            throw new BadRequestException("Invalid invite ID!");
        }
        Invitation invitation = session.getById(Invitation.class, UUID.fromString(request.inviteId))
                .orElseThrow(() -> new BadRequestException("Invitation does not exist for this invite ID!"));

        invitation.denyInvitation();
        session.remove(invitation);
    }

    public GroupDetailsResponse groupDetails(ModelSession session, Map<String, String> params) {
        if (!params.containsKey("auth_token")) {
            throw new BadRequestException("Missing auth token!!");
        }
        if (!params.containsKey("group_id")) {
            throw new BadRequestException("Missing group ID!");
        }
        User host = authenticateToken(session, params.get("auth_token"));
        Group group = session.getById(Group.class, UUID.fromString(params.get("group_id")))
                .orElseThrow(() -> new ForbiddenException("Group ID doesn't exist!"));

        return group.getGroupDetailsResponse(host);
    }

    public RecommenderResponse recommendEventEvent (ModelSession session, Map<String, String> params) {
        if (!params.containsKey("event_id")) {
            throw new BadRequestException("Missing event id!");
        }

        if (!params.containsKey("page_start") || !params.containsKey("max_results")) {
            throw new BadRequestException("Missing paging details!");
        }

        var event = session.getById(Event.class, parseUUID(params.get("event_id")))
                .orElseThrow(() -> new ForbiddenException("Invalid event id!"));

        int pageStart = 0;
        int maxResults = 0;
        try {
            pageStart = Integer.parseInt(params.get("page_start"));
            maxResults = Integer.parseInt(params.get("max_results"));
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid paging details!", e);
        }

        if (pageStart < 0 || maxResults <= 0 || maxResults > 256) {
            throw new BadRequestException("Invalid paging values!");
        }

        var returnNum = new AtomicInteger();
        var recommendEvents = session.getAllStream(Event.class)
                .filter(e -> !e.equals(event))
                .filter(Event::isPublished)
                .filter(e -> !e.getEventEnd().isBefore(ZonedDateTime.now()))
                .map(e -> new Pair<>(e.getId().toString(), RecommenderEngine.calculateSimilarity(session, event, e)))
                .peek(p -> returnNum.getAndIncrement())
                .sorted(Comparator.comparingDouble((Pair<String, Double> p) -> p.getSecond()).reversed())
                .skip(pageStart)
                .limit(maxResults)
                .map(p -> new RecommenderResponse.Event(p.getFirst(), p.getSecond()))
                .collect(Collectors.toList());

        return new RecommenderResponse(recommendEvents, returnNum.get());
    }

    public RecommenderResponse recommendUserEvent (ModelSession session, Map<String, String> params) {
        if (!params.containsKey("auth_token")) {
            throw new UnauthorizedException("Missing auth token!");
        } else if (!params.containsKey("page_start") || !params.containsKey("max_results")) {
            throw new BadRequestException("Missing paging details!");
        }

        var user = authenticateToken(session, params.get("auth_token"));

        int pageStart = 0;
        int maxResults = 0;
        try {
            pageStart = Integer.parseInt(params.get("page_start"));
            maxResults = Integer.parseInt(params.get("max_results"));
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid paging details!", e);
        }

        if (pageStart < 0 || maxResults <= 0 || maxResults > 256) {
            throw new BadRequestException("Invalid paging values!");
        }

        var profileVector = RecommenderEngine.buildUserProfile(session, user);
        var eventNum = new AtomicInteger();
        var recommendEvents = session.getAllStream(Event.class)
                .filter(e -> !e.getHost().equals(user))
                .filter(Event::isPublished)
                .filter(e -> !e.getEventEnd().isBefore(ZonedDateTime.now()))
                .map(e -> new Pair<>(e.getId().toString(), RecommenderEngine.calculateUserScore(session, e, profileVector)))
                .peek(p -> eventNum.getAndIncrement())
                .sorted(Comparator.comparingDouble((Pair<String, Double> p) -> p.getSecond()).reversed())
                .skip(pageStart)
                .limit(maxResults)
                .map(p -> new RecommenderResponse.Event(p.getFirst(), p.getSecond()))
                .collect(Collectors.toList());

        return new RecommenderResponse(recommendEvents, eventNum.get());
    }

    public RecommenderResponse recommendEventUserEvent (ModelSession session, Map<String, String> params) {
        if (!params.containsKey("auth_token")) {
            throw new UnauthorizedException("Missing auth token!");
        } else if (!params.containsKey("event_id")) {
            throw new BadRequestException("Missing event id!");
        } else if (!params.containsKey("page_start") || !params.containsKey("max_results")) {
            throw new BadRequestException("Missing paging details!");
        }

        var user = authenticateToken(session, params.get("auth_token"));
        var event = session.getById(Event.class, parseUUID(params.get("event_id")))
                .orElseThrow(() -> new ForbiddenException("Invalid event id!"));

        int pageStart = 0;
        int maxResults = 0;
        try {
            pageStart = Integer.parseInt(params.get("page_start"));
            maxResults = Integer.parseInt(params.get("max_results"));
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid paging details!", e);
        }

        if (pageStart < 0 || maxResults <= 0 || maxResults > 256) {
            throw new BadRequestException("Invalid paging values!");
        }

        var profileVector = RecommenderEngine.buildUserProfile(session, user);
        var eventNum = new AtomicInteger();
        var recommendEvents = session.getAllStream(Event.class)
                .filter(e -> !e.getId().equals(event.getId()))
                .filter(e -> !e.getHost().equals(user))
                .filter(Event::isPublished)
                .filter(e -> !e.getEventEnd().isBefore(ZonedDateTime.now()))
                .map(e -> new Pair<>(e.getId().toString(), RecommenderEngine.calculateUserEventScore(session, e, event, profileVector)))
                .peek(p -> eventNum.getAndIncrement())
                .sorted(Comparator.comparingDouble((Pair<String, Double> p) -> p.getSecond()).reversed())
                .skip(pageStart)
                .limit(maxResults)
                .map(p -> new RecommenderResponse.Event(p.getFirst(), p.getSecond()))
                .collect(Collectors.toList());

        return new RecommenderResponse(recommendEvents, eventNum.get());
    }

    public void clearDatabase (ModelSession session, Object request) {
        logger.info("Clearing database!");
        session.clear(AuthToken.class);
        //clearType(session, AuthToken.class);
        session.clear(Category.class);
        //clearType(session, Category.class);
        session.clear(Comment.class);
        //clearType(session, Comment.class);
        session.clear(Event.class);
        //clearType(session, Event.class);
        session.clear(Group.class);
        //clearType(session, Group.class);
        session.clear(Location.class);
        //clearType(session, Location.class);
        session.clear(PurchaseItem.class);
        //clearType(session, PurchaseItem.class);
        session.clear(Reaction.class);
        //clearType(session, Reaction.class);
        session.clear(ResetToken.class);
        //clearType(session, ResetToken.class);
        session.clear(SeatingPlan.class);
        //clearType(session, SeatingPlan.class);
        session.clear(Tag.class);
        //clearType(session, Tag.class);
        session.clear(TestEntity.class);
        //clearType(session, TestEntity.class);
        session.clear(Ticket.class);
        //clearType(session, Ticket.class);
        session.clear(TicketReservation.class);
        //clearType(session, TicketReservation.class);
        session.clear(User.class);
        //clearType(session, User.class);
        session.clear(Invitation.class);
        //clearType(session, Invitation.class);
        session.clear(DocumentTerm.class);
        //clearType(session, DocumentTerm.class);
        session.clear(TfIdf.class);
        //clearType(session, TfIdf.class);
    }

    private <T> void clearType (ModelSession session, Class<T> tClass) {
        for (var i : session.getAll(tClass)) {
            session.remove(i);
        }
    }

    public void groupRemoveMember (ModelSession session, GroupRemoveMemberRequest request) {
        if (request.groupId == null) {
            throw new BadRequestException("Invalid group ID!");
        }
        if (request.authToken == null) {
            throw new BadRequestException("Invalid auth token!");
        }
        if (request.email == null || !EMAIL_REGEX.matcher(request.email.trim().toLowerCase()).matches()) {
            throw new BadRequestException("Invalid Email!");
        }

        Group group = session.getById(Group.class, UUID.fromString(request.groupId))
                .orElseThrow(() -> new ForbiddenException("Group does not exist!"));

        User leader = authenticateToken(session, request.authToken);
        if (!leader.equals(group.getLeader())) {
            throw new BadRequestException("Only the group leader can remove members!");
        }

        User removeUser = session.getByUnique(User.class, "email", request.email)
                .orElseThrow(() -> new ForbiddenException("User with email does not exist!"));
        group.removeUser(removeUser);
    }

    public void groupCancel (ModelSession session, GroupCancelRequest request) {
        if (request.groupId == null) {
            throw new BadRequestException("Invalid group ID!");
        }
        if (request.authToken == null) {
            throw new BadRequestException("Invalid auth token!");
        }

        Group group = session.getById(Group.class, UUID.fromString(request.groupId))
                .orElseThrow(() -> new ForbiddenException("Group does not exist!"));

        User leader = authenticateToken(session, request.authToken);

        if (!leader.equals(group.getLeader())) {
            throw new BadRequestException("Only the group leader can cancel the group!");
        }
        session.remove(group);
    }

    public void groupRemoveInvite (ModelSession session, GroupRemoveInviteRequest request) {
        if (request.authToken == null) {
            throw new BadRequestException("Invalid auth token!");
        }
        if (request.groupId == null) {
            throw new BadRequestException("Invalid group ID!");
        }
        if (request.inviteId == null) {
            throw new BadRequestException("Invalid invite ID!");
        }

        User user = authenticateToken(session, request.authToken);

        Group group = session.getById(Group.class, UUID.fromString(request.groupId))
                .orElseThrow(() -> new ForbiddenException("Group does not exist!"));

        if (!user.equals(group.getLeader())) {
            throw new BadRequestException("Only the group leader can remove invites!");               
        }

        groupDeny(session, new GroupDenyRequest(request.inviteId));
    }   

    public ReserveDetailsResponse getReserveDetails (ModelSession session, Map<String, String> params) {
        if (params.get("reserve_id") == null) {
            throw new BadRequestException("Missing reserve ID!");
        }

        TicketReservation reserve = session.getById(TicketReservation.class, UUID.fromString(params.get("reserve_id")))
                .orElseThrow(() -> new ForbiddenException("Ticket reservation does not exist!"));
        
        return reserve.getReserveDetailsResponse();
    }

     // changes the notifcations for the user for an event
     public void eventNotificationsUpdate (ModelSession session, EventNotificationsUpdateRequest request) {
        if (request.authToken == null) {
            throw new UnauthorizedException("Missing auth token!");
        }

        Event event = session.getById(Event.class, parseUUID(request.eventId))
                .orElseThrow(() -> new ForbiddenException("Invalid event id!"));
        var user = authenticateToken(session, request.authToken);

        event.editNotificationMembers(session, user, request.notifications);
        user.editEventNotificaitons(session, event, request.notifications);
    }

    // checks the notifications of user for an event
    public EventNotificationsResponse checkEventNotifications (ModelSession session, Map<String, String> params) {
        if (!params.containsKey("event_id")) {
            throw new BadRequestException("Missing event_id!");
        }
        Event event = session.getById(Event.class, UUID.fromString(params.get("event_id")))
                        .orElseThrow(() -> new ForbiddenException("Unknown event"));

        User user = null;
        if (params.containsKey("auth_token")) {
            user = authenticateToken(session, params.get("auth_token"));
        } else {
            throw new BadRequestException("Missing auth token!");
        }
        var notification = user.doReminders();

        if (event.getNotificationMembers().contains(user)) {
            if (user.getNotificationEvents().contains(event)) {
                notification = true;
            } 
        } else {
            notification = false;
        }

        return new EventNotificationsResponse(notification);
    }

    public void ticketRefund (ModelSession session, TicketRefundRequest request) {
        var user = authenticateToken(session, request.authToken);

        var ticket = session.getById(Ticket.class, parseUUID(request.ticketId))
                .orElseThrow(() -> new ForbiddenException("Invalid ticket id!"));

        ticket.refund(user);
        session.remove(ticket);
    }

    public CategoriesResponse categoriesList (ModelSession session) {
        return new CategoriesResponse(Category.getValidCategories());
    }

    public CategoryEventsResponse eventsByCategory (ModelSession session, Map<String, String> params) {
        if (!params.containsKey("category")) {
            throw new BadRequestException("Missing category!");
        } else if (!params.containsKey("page_start") || !params.containsKey("max_results")) {
            throw new BadRequestException("Missing paging values!");
        }

        var category = params.get("category");
        if (!Category.validCategory(category)) {
            throw new ForbiddenException("Invalid category: \"" + category + "\" (ensure case is correct!)");
        }

        int pageStart = 0;
        int maxResults = 0;
        try {
            pageStart = Integer.parseInt(params.get("page_start"));
            maxResults = Integer.parseInt(params.get("max_results"));
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid paging details!", e);
        }

        if (pageStart < 0 || maxResults <= 0 || maxResults > 256) {
            throw new BadRequestException("Invalid paging values!");
        }

        var eventCount = new AtomicInteger();
        var events = session.getAllWithStream(Category.class, "category", category)
                .map(Category::getEvent)
                .filter(Event::isPublished)
                .filter(e -> e.getEventEnd().isAfter(ZonedDateTime.now(ZoneId.of("UTC"))))
                .peek(c -> eventCount.getAndIncrement())
                .sorted(Comparator.comparing(Event::getEventStart))
                .skip(pageStart)
                .limit(maxResults)
                .map(Event::getId)
                .map(UUID::toString)
                .collect(Collectors.toList());
        return new CategoryEventsResponse(events, eventCount.get());
    }
}
