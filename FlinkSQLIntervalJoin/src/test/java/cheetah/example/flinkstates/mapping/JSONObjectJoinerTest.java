package cheetah.example.flinkstates.mapping;

import cheetah.example.flinksqlintervaljoin.mapping.JSONObjectJoiner;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ClassLoaderUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JSONObjectJoinerTest {
    @Test
    public void TestJoinAndNoOverride() throws IOException, URISyntaxException {
        var leftJsonResourceURI = ClassLoaderUtils.getDefaultClassLoader().getResource("LeftJsonObjectForJoin.json").toURI();
        var leftJson = new JSONObject(Files.readString(Paths.get(leftJsonResourceURI)));
        var rightJsonResourceURI = ClassLoaderUtils.getDefaultClassLoader().getResource("RightJsonObjectForJoin.json").toURI();
        var rightJson = new JSONObject(Files.readString(Paths.get(rightJsonResourceURI)));

        var joinedJson = JSONObjectJoiner.joinFirstIntoSecondNoOverride(leftJson, rightJson);

        Assertions.assertTrue(joinedJson.has("KeyOnlyInLeft"));
        Assertions.assertTrue(joinedJson.has("KeyOnlyInRight"));
        Assertions.assertEquals(joinedJson.get("KeyInBoth"), "ValueInRight");
        Assertions.assertTrue(joinedJson.getJSONObject("ObjectInBoth").has("KeyInRight"));
        Assertions.assertFalse(joinedJson.getJSONObject("ObjectInBoth").has("KeyInLeft"));
        Assertions.assertEquals(joinedJson.getJSONArray("ArrayInBoth").length(), 1);
        Assertions.assertEquals(joinedJson.getJSONArray("ArrayInBoth").get(0), "ValueInRight");
    }
}
