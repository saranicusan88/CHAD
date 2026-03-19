package ro.ut.utassistant;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class JsonScheduleLoader {

    public static List<Lesson> loadFromResource(String resourcePath) throws Exception {
        InputStream in = JsonScheduleLoader.class.getResourceAsStream(resourcePath);
        if (in == null) throw new IllegalArgumentException("Nu găsesc resursa: " + resourcePath);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(in);

        JsonNode lessonsNode = root.get("lessons");
        if (lessonsNode == null || !lessonsNode.isArray()) {
            throw new IllegalArgumentException("JSON invalid: lipsește 'lessons' array");
        }

        List<Lesson> out = new ArrayList<>();
        for (JsonNode n : lessonsNode) {
            out.add(new Lesson(
                    text(n, "group"),
                    text(n, "semi"),
                    text(n, "day"),
                    text(n, "time"),
                    text(n, "subject"),
                    text(n, "raw"),
                    text(n, "room")
            ));
        }
        return out;
    }

    private static String text(JsonNode n, String field) {
        JsonNode v = n.get(field);
        return v == null ? "" : v.asText("");
    }
}
