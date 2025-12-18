package cheetah.example.odataimporter.function;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.apache.flink.api.common.state.ListState;
import org.apache.flink.api.common.state.ListStateDescriptor;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.common.typeinfo.Types;
import org.apache.flink.runtime.state.FunctionInitializationContext;
import org.apache.flink.runtime.state.FunctionSnapshotContext;
import org.apache.flink.streaming.api.checkpoint.CheckpointedFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.watermark.Watermark;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Inspiration: https://github.com/trifork/kamstrup-dp-processing/blob/main/src/main/java/com/kamstrup/skagerak/processing/water/connector/csv/watermeterreading/CsvWaterMeterReadingSourceFunction.java

@SuppressWarnings("deprecation")
public class ODataV2SourceFunction implements SourceFunction<JSONObject>, CheckpointedFunction {

    private static final Logger LOG = LoggerFactory.getLogger(ODataV2SourceFunction.class);

    private volatile AtomicLong totalEntities = new AtomicLong();
    private volatile AtomicLong entitiesImportedCount = new AtomicLong();

    private volatile boolean isRunning = true;
    private final String serviceUrl;
    private final String entitySetNames;
    private final String username;
    private final String password; // todo: keep secret?

    private HttpRequest.Builder requestBuilder;
    private HttpClient client;

    private List<String> entitiesReadCache;
    private transient ListState<String> fileNamesReadState;

    public ODataV2SourceFunction(String serviceUrl, String entitySetNames, String username, String password) {
        this.serviceUrl = serviceUrl;
        this.entitySetNames = entitySetNames;
        this.username = username;
        this.password = password;
    }

    public void prepareRequests() throws Exception {
        client = HttpClient.newBuilder()
                .version(Version.HTTP_1_1)
                .followRedirects(Redirect.NORMAL)
                .authenticator(new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(username, password.toCharArray());
                    }
                })
                .build();

        requestBuilder = HttpRequest.newBuilder()
                .GET()
                .header("User-Agent", "Apache Flink OData source function")
                .header("Accept", "application/json");

        LOG.info("Opening OData source function");
    }

    @Override
    public void run(SourceContext<JSONObject> ctx) throws Exception {
        prepareRequests();
        // while (isRunning) {
        totalEntities.set(0);
        for (String entitySetName : entitySetNames.split(",")) {
            try {

                String requestUrl = serviceUrl + entitySetName;
                HttpResponse<String> response = getResponse(requestUrl);

                JSONObject jsonObject = new JSONObject(response.body());
                jsonObject = getEntities(ctx, jsonObject);

                if (jsonObject.has("__next")) {
                    requestUrl = jsonObject.getString("__next");
                } else {
                    ctx.markAsTemporarilyIdle();
                    LOG.info(String.format("Read %s entities. No more entities of type %s to read.", totalEntities,
                            entitySetName));
                    // Thread.sleep(Time.seconds(1).toMilliseconds()); // todo
                }

            } catch (Exception e) {
                LOG.error("Error while fetching OData feed: {}", e.getMessage());
                throw e;
            }
        }
        // }
    }

    private HttpResponse<String> getResponse(String requestUrl) throws Exception {
        HttpResponse<String> response;

        try {
            // create uri with query params
            Map<String, String> requestParams = new HashMap<>();
            requestParams.put("$top", "1000"); // todo
            requestParams.put("$orderby", "LastChangeDateTime desc");

            String encodedURL = requestParams.keySet().stream()
                    .map(key -> String.format("%s=%s", key, encodeValue(requestParams.get(key))))
                    .collect(Collectors.joining("&", requestUrl + "?", ""));

            URI uri = URI.create(encodedURL);
            HttpRequest request = requestBuilder.copy()
                    .uri(uri)
                    .build();
            response = client
                    .send(request, BodyHandlers.ofString());

        } catch (Exception e) {
            LOG.error("Error while creating request: {}", e.getMessage());
            throw e;
        }

        if (response.statusCode() != 200) {
            throw new Exception("Error while fetching OData feed: " + response.body());
        }
        return response;
    }

    private String encodeValue(String value) {
        var encodedValue = URLEncoder.encode(value, StandardCharsets.UTF_8);
        return encodedValue;
    }

    private JSONObject getEntities(SourceContext<JSONObject> ctx, JSONObject jsonObject) {
        JSONArray entities = jsonObject.getJSONObject("d").getJSONArray("results");
        LOG.info("Fetched {} entities from OData feed", entities.length());
        entitiesImportedCount.set(0);

        for (int i = 0; i < entities.length(); i++) {
            totalEntities.incrementAndGet();

            var entity = entities.getJSONObject(i);
            String entityId = entity.getJSONObject("__metadata").getString("id");
            if (this.entitiesReadCache.contains(entityId)) {
                LOG.info("Skipping already read entity: " + entityId);
                continue;
            }

            synchronized (ctx.getCheckpointLock()) {
                Instant now = Instant.now();
                long timestamp = now.toEpochMilli();
                ctx.collectWithTimestamp(entity, timestamp);
                ctx.emitWatermark(new Watermark(timestamp - 1));
                entitiesImportedCount.incrementAndGet();
            }

            entitiesReadCache.add(entityId); // Add entity to cache after successfully parsing it.
        }
        LOG.info("Ok, read " + entitiesImportedCount.get() + " entities");
        return jsonObject;
    }

    @Override
    public void initializeState(FunctionInitializationContext functionInitializationContext) throws Exception {
        this.entitiesReadCache = new LinkedList<>();

        this.fileNamesReadState = functionInitializationContext
                .getOperatorStateStore()
                // what about timezone?
                // &$filter=LastChangeDate gt datetime%272022-03-11T14:49:52%27
                .getListState(new ListStateDescriptor<>("entity-names-read", Types.STRING));

        if (functionInitializationContext.isRestored()) {
            for (String filename : this.fileNamesReadState.get()) {
                this.entitiesReadCache.add(filename);
            }
        }
    }

    @Override
    public void snapshotState(FunctionSnapshotContext functionSnapshotContext) throws Exception {
        this.fileNamesReadState.clear();
        this.fileNamesReadState.addAll(entitiesReadCache);
    }

    @Override
    public void cancel() {
        isRunning = false;
    }
}
