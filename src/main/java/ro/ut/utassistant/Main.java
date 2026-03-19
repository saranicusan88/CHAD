package ro.ut.utassistant;

import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {

        //  room legend
        Map<String, RoomInfo> rooms = RoomLegendLoader.loadFromResource("/rooms.csv");

        //  schedule din JSON
        List<Lesson> lessons = JsonScheduleLoader.loadFromResource("/orar.json");

        // services
        ScheduleService scheduleService = new ScheduleService(lessons);
        RoomService roomService = new RoomService(rooms);

        //  assistant
        new ScheduleAssistant(scheduleService, roomService).run();
    }
}
