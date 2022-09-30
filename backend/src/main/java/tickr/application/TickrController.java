package tickr.application;

import tickr.application.entities.TestEntity;
import tickr.application.serialised.requests.UserRegisterRequest;
import tickr.application.serialised.responses.AuthTokenResponse;
import tickr.application.serialised.responses.TestResponses;
import tickr.persistence.ModelSession;
import tickr.server.exceptions.BadRequestException;
import tickr.server.exceptions.NotFoundException;

import java.util.Map;
import java.util.UUID;

/**
 * Class encapsulating business logic. Is created once per user session, which is distinct to ModelSession instances -
 * TickrController instances may persist for the duration a user interacts with the service, though this should not
 * be relied upon
 */
public class TickrController {
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
        return new AuthTokenResponse();
    }
}
