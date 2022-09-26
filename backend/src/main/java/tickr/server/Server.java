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

    private static void addRoutes () {
        logger.debug("Adding routes!");

        get("/api/test/get", TickrController::testGet);
        get("/api/test/get/all", TickrController::testGetAll);
        post("/api/test/post", TickrController::testPost, TestResponses.PostRequest.class);
        put("/api/test/put", TickrController::testPut, TestResponses.PutRequest.class);
        delete("/api/test/delete", TickrController::testDelete, TestResponses.DeleteRequest.class);
    }

    public static void start (int port, String frontendUrl, DataModel model) {
        dataModel = model;
        gson = new Gson();

        Spark.port(port);
        Spark.threadPool(MAX_THREADS, MIN_THREADS, TIMEOUT_MS);

        Spark.before(((request, response) -> {
            if (request.queryString() != null) {
                logger.info("{} {}?{}", request.requestMethod(), request.pathInfo(), request.queryString());
            } else {
                logger.info("{} {}", request.requestMethod(), request.pathInfo());
            }
        }));

        addRoutes();

        Spark.exception(ServerException.class, ((exception, request, response) -> {
            logger.info("\t{}: {}", exception.getStatusString(), exception.getMessage());
            response.status(exception.getStatusCode());
            response.type("application/json");
            response.body(gson.toJson(exception.getSerialised()));
        }));
        Spark.exception(Exception.class, (exception, request, response) -> {
            logger.error("Uncaught exception: ", exception);
            response.status(500);
            response.body("Internal server error");
        });

        Spark.after((request, response) -> {
            if (response.status() == 200) {
                logger.info("\t200 OK");
            }
        });

        if (frontendUrl != null) {
            addCORS(frontendUrl);
        }
    }

    private static void addCORS (String frontendUrl) {
        Spark.options("/*", ((request, response) -> {
            // TODO: request filtering?
            var accessControlReqHeaders = request.headers("Access-Control-Request");
            if (accessControlReqHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlReqHeaders);
            }
            var accessControlReqMethods = request.headers("Access-Control-Request-Method");
            if (accessControlReqMethods != null) {
                response.header("Access-Control-Allow-Method", accessControlReqMethods);
            }

            response.header("Access-Control-Allow-Credentials", "true");
            response.header("Access-Control-Allow-Origin", frontendUrl);

            return "OK";
        }));

        /*Spark.after(((request, response) -> {

        }));*/
    }


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

    private static <T> void post (String path, TriConsumer<TickrController, ModelSession, T> route, Class<T> reqClass) {
        post(path, (t, u, v) -> {
            route.consume(t, u, v);
            return new Object();
        }, reqClass);
    }

    private static <T, R> void put (String path, TriFunction<TickrController, ModelSession, T, R> route, Class<T> reqClass) {
        Spark.put(path, new RouteWrapper<>(dataModel, ctx -> route.apply(ctx.controller, ctx.session, gson.fromJson(ctx.request.body(), reqClass))),
                gson::toJson);
    }

    private static <T> void put (String path, TriConsumer<TickrController, ModelSession, T> route, Class<T> reqClass) {
        put(path, (t, u, v) -> {
            route.consume(t, u, v);
            return new Object();
        }, reqClass);
    }

    private static <T, R> void delete (String path, TriFunction<TickrController, ModelSession, T, R> route, Class<T> reqClass) {
        Spark.delete(path, new RouteWrapper<>(dataModel, ctx -> route.apply(ctx.controller, ctx.session, gson.fromJson(ctx.request.body(), reqClass))),
                gson::toJson);
    }

    private static <T> void delete (String path, TriConsumer<TickrController, ModelSession, T> route, Class<T> reqClass) {
        delete(path, (t, u, v) -> {
            route.consume(t, u, v);
            return new Object();
        }, reqClass);
    }
}
