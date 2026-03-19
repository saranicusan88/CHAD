package ro.ut.utassistant;
import java.util.Map;

public class RoomService {

    private final Map<String, RoomInfo> rooms;

    public RoomService(Map<String, RoomInfo> rooms) {
        this.rooms = rooms;
    }

    public String describeRoom(String roomCode) {
        if (roomCode == null || roomCode.isBlank()) {
            return "Nu am primit un cod.";
        }

        String normalized = normalizeQuery(roomCode);

        RoomInfo info = rooms.get(normalized);

        if (info == null) {
            return "Nu găsesc informații pentru " + roomCode + ".";
        }

        return info.humanDescription();
    }
    private String normalizeQuery(String input) {
        String s = input.trim().toUpperCase();


        s = s.replace("Ă","A").replace("Â","A").replace("Î","I")
                .replace("Ș","S").replace("Ţ","T").replace("Ț","T");


        if (s.contains("SECRETARIAT")) return "SECRETARIAT";
        if (s.contains("DECANAT")) return "DECANAT";
        if (s.contains("BIBLIOTEC")) return "BIBLIOTECA";


        return RoomLegendLoader.normalizeRoomKey(s);
    }

}
