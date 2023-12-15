package cheetah.example.externallookup.function;

import cheetah.example.externallookup.model.InputEvent;
import cheetah.example.externallookup.model.OutputEvent;
import com.trifork.cheetah.processing.auth.CachedTokenProvider;
import com.trifork.cheetah.processing.auth.KeyedTokenProvider;
import com.trifork.cheetah.processing.auth.OAuthTokenProvider;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * ExternalLookupMapper converts from ExternalLookupInputEvent to ExternalLookupOutputEvent.
 */
public class ExternalLookupMapper extends RichAsyncFunction<InputEvent, OutputEvent> {

    private final String idServiceHost;
    private HttpClient client;
    private KeyedTokenProvider tokenProvider;
    private final static String TOKEN_ID = "ServiceToken";
    private HttpRequest.Builder requestBuilder;
    private final String tokenUrl;
    private final String clientId;
    private final String clientSecret;
    private final String scope;

    public ExternalLookupMapper(String idServiceHost, String tokenUrl, String clientId, String clientSecret, String scope) {
        if (!idServiceHost.endsWith("/")) {
            idServiceHost += "/";
        }
        this.idServiceHost = idServiceHost;
        this.tokenUrl = tokenUrl;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
    }

    @Override
    public void asyncInvoke(InputEvent externalLookupInputEvent, ResultFuture<OutputEvent> resultFuture) {

        HttpRequest request = requestBuilder.copy().header("Authorization", tokenProvider.getToken(TOKEN_ID)).build();

        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request,
                HttpResponse.BodyHandlers.ofString());

        CompletableFuture.supplyAsync(() -> {
            try {
                return response.get().body();
            } catch (InterruptedException | ExecutionException e) {
                // Normally handled explicitly.
                return null;
            }
        }).thenAccept((String body) -> resultFuture.complete(Collections.singleton(new OutputEvent(externalLookupInputEvent, body))));
    }

    @Override
    public void open(Configuration parameters) {
        client = HttpClient.newHttpClient();

        tokenProvider = KeyedTokenProvider.getInstance();
        tokenProvider.registerTokenProviderIfAbsent(TOKEN_ID, () -> new CachedTokenProvider(new OAuthTokenProvider(tokenUrl, clientId, clientSecret, scope)));

        requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(idServiceHost + "ExternalLookup"))
                .GET()
                .header("Content-Type", "application/json")
                .header("Accept", "text/plain");
    }
}