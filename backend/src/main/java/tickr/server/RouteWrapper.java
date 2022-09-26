package tickr.server;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Request;
import spark.Response;
import spark.Route;
import tickr.application.TickrController;
import tickr.persistence.DataModel;
import tickr.persistence.ModelSession;

import java.util.function.Function;

public class RouteWrapper<T> implements Route {
    static final Logger logger = LogManager.getLogger();
    private Function<Context, T> route;
    private DataModel model;

    public RouteWrapper (DataModel model, Function<Context, T> route) {
        this.route = route;
        this.model = model;
    }

    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    @Override
    public Object handle (Request request, Response response) throws Exception {
        var userSession = request.session(false);
        if (userSession == null) {
            userSession = request.session(true);
            userSession.attribute("controller", new TickrController());
            logger.debug("Created new session!");
        }
        var controller = (TickrController)userSession.attribute("controller");
        var modelSession = model.makeSession();

        T result;
        try {
            synchronized (controller) {
                // For if multiple threads access the same user session
                result = route.apply(new Context(request, controller, modelSession));
            }
            modelSession.commit();
            modelSession.close();
        } catch (Exception e) {
            logger.debug("Caught exception in route: ", e);
            modelSession.rollback();
            modelSession.close();
            throw e; // TODO: repeat if commits fail?
        }

        response.type("application/json");
        return result;
    }

    public static class Context {
        public final Request request;
        public final TickrController controller;
        public final ModelSession session;

        Context (Request request, TickrController controller, ModelSession session) {
            this.request = request;
            this.controller = controller;
            this.session = session;
        }
    }
}
