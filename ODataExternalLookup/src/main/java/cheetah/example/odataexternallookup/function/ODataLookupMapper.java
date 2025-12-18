package cheetah.example.odataexternallookup.function;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import org.json.JSONObject;

import com.esotericsoftware.minlog.Log;

/**
 * ExternalLookupMapper converts from ExternalLookupInputEvent to
 * ExternalLookupOutputEvent.
 */
public class ODataLookupMapper extends RichAsyncFunction<JSONObject, JSONObject> {

    private HttpClient client;
    private HttpRequest.Builder requestBuilder;

    private final String entityUrl;
    private final String username;
    private final String password;
    private final String entityIdPath;

    public ODataLookupMapper(String entityUrl, String username, String password, String entityIdPath) {

        this.entityUrl = entityUrl;
        this.username = username;
        this.password = password;
        this.entityIdPath = entityIdPath;
    }

    @Override
    public void asyncInvoke(JSONObject externalLookupInputEvent, ResultFuture<JSONObject> resultFuture)
            throws UncategorizedJmsException {

        String externalId = "-1";

        try {
            externalId = (String) externalLookupInputEvent.query(entityIdPath);
        } catch (Exception e) {
            throw new UncategorizedJmsException("Error querying event on path:" + entityIdPath, e);
        }

        Log.debug("ODataLookupMapper", "ExternalId: " + externalId);

        HttpRequest request = requestBuilder.copy()
                .uri(URI.create(String.format("%s('%s')", entityUrl, externalId)))
                .build();

        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request,
                HttpResponse.BodyHandlers.ofString());

        CompletableFuture.supplyAsync(() -> {
            try {
                var body = response.get().body();
                return new JSONObject(body).getJSONObject("d");
            } catch (InterruptedException | ExecutionException e) {
                // Normally handled explicitly.
                return null;
            }
        }).thenAccept((JSONObject body) -> resultFuture.complete(Collections.singleton(body)));
    }

    @Override
    public void open(Configuration parameters) {
        client = HttpClient.newBuilder()
                .followRedirects(Redirect.NORMAL)
                .authenticator(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password.toCharArray());
                    }
                })
                .build();

        requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(entityUrl))
                .GET()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
    }
}