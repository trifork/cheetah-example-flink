package cheetah.example.externallookup.function;

import cheetah.example.externallookup.model.InputEvent;
import cheetah.example.externallookup.model.OutputEvent;
import com.trifork.cheetah.processing.auth.CachedTokenProvider;
import com.trifork.cheetah.processing.auth.KeyedTokenProvider;
import com.trifork.cheetah.processing.auth.OAuthTokenProvider;
import org.apache.flink.api.java.utils.ParameterTool;
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

    private String idServiceHost;
    private HttpClient client;
    private KeyedTokenProvider tokenProvider;
    private final static String TOKEN_ID = "ServiceToken";
    private HttpRequest.Builder requestBuilder;
    private final String tokenEndpoint;
    private final String clientId;
    private final String clientSecret;
    private final String scope;

    public ExternalLookupMapper(String tokenEndpoint, String clientId, String clientSecret, String scope) {
        this.tokenEndpoint = tokenEndpoint;
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


        requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(idServiceHost + "ExternalLookup"))
                .GET()
                .header("Content-Type", "application/json")
                .header("Accept", "text/plain");


        tokenProvider = KeyedTokenProvider.getInstance();
        tokenProvider.registerTokenProviderIfAbsent(TOKEN_ID, () -> new CachedTokenProvider(new OAuthTokenProvider(tokenEndpoint, clientId, clientSecret, scope)));

        ParameterTool parameterTool = (ParameterTool)
                getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
        idServiceHost = parameterTool.get("id-service-url");
        if (!idServiceHost.endsWith("/")) {
            idServiceHost += "/";
        }
    }
}