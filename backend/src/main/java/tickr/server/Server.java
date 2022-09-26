package tickr.server;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Spark;
import tickr.application.TickrController;
import tickr.application.responses.TestResponses;
import tickr.persistence.DataModel;
import tickr.persistence.ModelSession;
import tickr.server.exceptions.ServerException;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Server {
    private static final int MIN_THREADS = 2;
    private static final int MAX_THREADS = 8;
    private static final int TIMEOUT_MS = 3000;

    static final Logger logger = LogManager.getLogger();

    private static DataModel dataModel;

    private static Gson gson;

    public static void start (int port, DataModel model) {
        dataModel = model;
        gson = new Gson();

        logger.debug("Adding routes!");
        Spark.port(port);
        Spark.threadPool(MAX_THREADS, MIN_THREADS, TIMEOUT_MS);

        Spark.get("/api/test", ((request, response) -> {
            logger.info("Received test request {} {}!", request.requestMethod(), request.pathInfo());
            String name = request.queryParams("name");
            if (name == null) {
                return "{'message' : 'Hello World!'}";
            } else {
                return String.format("{'message' : 'Hello %s!'}", name);
            }
        }));

        get("/api/test/get", TickrController::testGet);
        get("/api/test/get/all", TickrController::testGetAll);
        post("/api/test/post", TickrController::testPost, TestResponses.PostRequest.class);
        put("/api/test/put", TickrController::testPut, TestResponses.PutRequest.class);
        delete("/api/test/delete", TickrController::testDelete, TestResponses.DeleteRequest.class);

        Spark.exception(ServerException.class, ((exception, request, response) -> {
            logger.error("{}: {}", exception.getStatusString(), exception.getMessage());
            response.status(exception.getStatusCode());
            response.body(gson.toJson(exception.getSerialised()));
        }));
        Spark.exception(Exception.class, (exception, request, response) -> {
            logger.error("Uncaught exception: ", exception);
            response.status(500);
            response.body("Internal server error");
        });
    }

    /*private static <T> void get (String path, Function<RouteWrapper.Context, T> routeFunc) {
        Spark.get(path, new RouteWrapper<T>(dataModel, routeFunc), gson::toJson);
    }*/

    private static <R> void get (String path, BiFunction<TickrController, ModelSession, R> route) {
        Spark.get(path, new RouteWrapper<>(dataModel, ctx -> route.apply(ctx.controller, ctx.session)), gson::toJson);
    }
    private static <R> void get (String path, TriFunction<TickrController, ModelSession, Map<String, String>, R> route) {
        Spark.get(path, new RouteWrapper<>(dataModel, ctx -> {
            var paramMap = ctx.request.queryParams()
                    .stream()
                    .collect(Collectors.toMap(Function.identity(), ctx.request::queryParams));

            return route.apply(ctx.controller, ctx.session, paramMap);
        }), gson::toJson);
    }

    private static <T, R> void post (String path, TriFunction<TickrController, ModelSession, T, R> route, Class<T> reqClass) {
        Spark.post(path, new RouteWrapper<>(dataModel, ctx -> route.apply(ctx.controller, ctx.session, gson.fromJson(ctx.request.body(), reqClass))),
                gson::toJson);
    }

    private static <T, R> void put (String path, TriFunction<TickrController, ModelSession, T, R> route, Class<T> reqClass) {
        Spark.put(path, new RouteWrapper<>(dataModel, ctx -> route.apply(ctx.controller, ctx.session, gson.fromJson(ctx.request.body(), reqClass))),
                gson::toJson);
    }

    private static <T, R> void delete (String path, TriFunction<TickrController, ModelSession, T, R> route, Class<T> reqClass) {
        Spark.delete(path, new RouteWrapper<>(dataModel, ctx -> route.apply(ctx.controller, ctx.session, gson.fromJson(ctx.request.body(), reqClass))),
                gson::toJson);
    }
}
