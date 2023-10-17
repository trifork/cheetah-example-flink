package Externallookup.function;

import Externallookup.model.InputEvent;
import Externallookup.model.OutputEvent;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.async.ResultFuture;
import org.apache.flink.streaming.api.functions.async.RichAsyncFunction;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/** ExternalLookupMapper converts from ExternalLookupInputEvent to ExternalLookupOutputEvent. */
public class ExternalLookupMapper extends RichAsyncFunction<InputEvent, OutputEvent> {

    private ObjectMapper mapper;
    private String idServiceHost;
    private HttpClient client;

    @Override
    public void asyncInvoke(InputEvent externalLookupInputEvent, ResultFuture<OutputEvent> resultFuture) throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(idServiceHost + "ExternalLookup"))
                .GET()
                .header("Content-Type", "application/json")
                .header("Accept", "text/plain")
                .build();

        CompletableFuture<HttpResponse<String>> response = client.sendAsync(request,
                HttpResponse.BodyHandlers.ofString());


        CompletableFuture.supplyAsync(() -> {
            try {
                return response.get().body();
            } catch (InterruptedException | ExecutionException e) {
                // Normally handled explicitly.
                return null;
            }
        }).thenAccept( (String body) -> resultFuture.complete(Collections.singleton(new OutputEvent(externalLookupInputEvent, body))));
    }

    @Override
    public void open(Configuration parameters) throws Exception {
        client = HttpClient.newHttpClient();

        mapper = new ObjectMapper();

        ParameterTool parameterTool = (ParameterTool)
                getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
        idServiceHost = parameterTool.get("id-service-url");
        if (!idServiceHost.endsWith("/")) {
            idServiceHost += "/";
        }
    }

    @Override
    public void close() throws Exception {

    }

}