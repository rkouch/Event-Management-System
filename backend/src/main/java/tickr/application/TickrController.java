package tickr.application;

import tickr.application.entities.TestEntity;
import tickr.application.responses.TestResponses;
import tickr.persistence.ModelSession;

import java.util.Map;
import java.util.Optional;

public class TickrController {
    public TickrController () {

    }

    public TestResponses.EntityResponse testGet (ModelSession session, Map<String, String> params) {
        if (!params.containsKey("id")) {
            throw new RuntimeException("Invalid params!"); // TODO
        }
        var entity = session.getTestEntity(Integer.parseInt(params.get("id")));

        return entity.map(t -> new TestResponses.EntityResponse(t.getName(), t.getEmail(), t.getId())).orElse(null);
    }

    public TestResponses.GetAll testGetAll (ModelSession session) {
        return new TestResponses.GetAll(session.getTestEntities());
    }

    public TestEntity testPost (ModelSession session, TestResponses.PostRequest request) {
        if (request.name == null || request.email == null) {
            throw new RuntimeException("Invalid request!"); // TODO
        }
        var entity = new TestEntity(request.name, request.email);
        session.saveTestEntity(entity);
        return entity;
    }

    public TestResponses.EntityResponse testPut (ModelSession session, TestResponses.PutRequest request) {
        if (request.newName == null || request.newEmail == null) {
            throw new RuntimeException("Invalid request!"); // TODO
        }

        var entityOpt = session.getTestEntity(request.id);

        if (entityOpt.isEmpty()) {
            throw new RuntimeException("No such id!"); // TODO
        }

        var entity = entityOpt.orElse(null);

        entity.setName(request.newName);
        entity.setEmail(request.newEmail);

        // No save needed, any updates are synced (make sure to use setters)

        return new TestResponses.EntityResponse(entity.getName(), entity.getEmail(), entity.getId());
    }

    public Object testDelete (ModelSession session, TestResponses.DeleteRequest request) {
        var entityOpt = session.getTestEntity(request.id);

        if (entityOpt.isEmpty()) {
            throw new RuntimeException("No such id!"); // TODO
        }

        var entity = entityOpt.orElse(null);
        //assert entity == null;
        session.removeTestEntity(entity);

        return null;
    }
}
