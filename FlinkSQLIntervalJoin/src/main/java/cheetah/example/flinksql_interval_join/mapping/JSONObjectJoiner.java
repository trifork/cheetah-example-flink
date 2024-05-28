package cheetah.example.flinksql_interval_join.mapping;

import org.json.JSONObject;

public class JSONObjectJoiner {

    public static JSONObject joinFirstIntoSecondNoOverride(JSONObject first, JSONObject second) {
        first.keys().forEachRemaining(key -> {
            var obj = first.get(key);
            if (!second.has(key)) {
                second.put(key, obj);
            }
        });

        return second;
    }
}
