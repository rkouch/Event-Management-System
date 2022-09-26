package tickr.integration;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class HTTPHelper {
    private HttpClient client;
    private Gson gson;
    private String serverUrl;
    public HTTPHelper (String serverUrl) {
        client = HttpClient.newHttpClient();
        gson = new Gson();
        this.serverUrl = serverUrl;
    }

    public Response get (String route, Map<String, String> params) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(buildParamsUrl(serverUrl + route, params)))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(r -> new Response(r.statusCode(), r.body(), gson))
                .orTimeout(1000, TimeUnit.MILLISECONDS)
                .join();
    }

    public Response get (String route) {
        return get(route, Map.of());
    }

    public <T> Response post (String route, T body) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + route))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(r -> new Response(r.statusCode(), r.body(), gson))
                .orTimeout(1000, TimeUnit.MILLISECONDS)
                .join();
    }

    public <T> Response put (String route, T body) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + route))
                .PUT(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(r -> new Response(r.statusCode(), r.body(), gson))
                .orTimeout(1000, TimeUnit.MILLISECONDS)
                .join();
    }

    public <T> Response delete (String route, T body) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + route))
                .method("DELETE", HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(r -> new Response(r.statusCode(), r.body(), gson))
                .orTimeout(1000, TimeUnit.MILLISECONDS)
                .join();
    }

    private String buildParamsUrl (String routeURL, Map<String, String> params) {
        var builder = new StringBuilder(routeURL);
        boolean first = true;

        for (var i : params.entrySet()) {
            if (first) {
                builder.append("?");
                first = false;
            } else {
                builder.append("&");
            }

            builder.append(URLEncoder.encode(i.getKey(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(i.getValue(), StandardCharsets.UTF_8));
        }

        return builder.toString();
    }

    public static class Response {
        private int statusCode;
        private JsonElement jsonBody;

        public Response (int statusCode, String body, Gson gson) {
            this.statusCode = statusCode;
            this.jsonBody = gson.fromJson(body, JsonElement.class);
        }

        public int getStatus () {
            return statusCode;
        }

        public JsonElement getBody () {
            return jsonBody;
        }

        public <T> T getBody (Class<T> tClass) {
            return new Gson().fromJson(jsonBody, tClass);
        }
    }
}
