package ro.ut.utassistant;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class RoomLegendLoader {

    public static Map<String, RoomInfo> loadFromResource(String resourcePath) throws Exception {
        InputStream in = RoomLegendLoader.class.getResourceAsStream(resourcePath);
        if (in == null) throw new IllegalArgumentException("Nu găsesc resursa: " + resourcePath);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String header = br.readLine();
            if (header == null) throw new IllegalArgumentException("rooms.csv e gol");

            Map<String, Integer> idx = headerIndex(header);
            require(idx, "roomCode");

            Map<String, RoomInfo> out = new HashMap<>();

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = splitCsvLine(line);

                String roomCode = get(parts, idx, "roomCode");
                if (roomCode == null || roomCode.isBlank()) continue;

                RoomInfo info = new RoomInfo(
                        roomCode.trim(),
                        get(parts, idx, "building"),
                        get(parts, idx, "address"),
                        get(parts, idx, "floor"),
                        get(parts, idx, "shortHint"),
                        get(parts, idx, "mapsLink")
                );

                out.put(normalizeRoomKey(roomCode), info);
            }

            return out;
        }
    }

    private static Map<String, Integer> headerIndex(String headerLine) {
        String[] cols = splitCsvLine(headerLine);
        Map<String, Integer> idx = new HashMap<>();
        for (int i = 0; i < cols.length; i++) {
            String key = cols[i].trim();
            if (!key.isEmpty()) idx.put(key, i);
        }
        return idx;
    }

    private static void require(Map<String, Integer> idx, String col) {
        if (!idx.containsKey(col)) {
            throw new IllegalArgumentException("Lipsește coloana obligatorie din rooms.csv: " + col);
        }
    }

    private static String get(String[] parts, Map<String, Integer> idx, String col) {
        Integer i = idx.get(col);
        if (i == null) return "";
        if (i < 0 || i >= parts.length) return "";
        String v = parts[i];
        if (v == null) return "";
        v = v.trim();
        if (v.length() >= 2 && v.startsWith("\"") && v.endsWith("\"")) {
            v = v.substring(1, v.length() - 1).trim();
        }
        return v;
    }

    public static String normalizeRoomKey(String roomCode) {
        return roomCode.trim().toUpperCase().replaceAll("\\s+", " ");
    }

    private static String[] splitCsvLine(String line) {
        var out = new java.util.ArrayList<String>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);

            if (ch == '"') {
                inQuotes = !inQuotes;
                cur.append(ch);
            } else if (ch == ',' && !inQuotes) {
                out.add(cur.toString());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }
}
