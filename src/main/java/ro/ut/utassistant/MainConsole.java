package ro.ut.utassistant;

public class MainConsole {
    public static void main(String[] args) throws Exception {

        var lessons = JsonScheduleLoader.loadFromResource("/orar.json");
        var scheduleService = new ScheduleService(lessons);

        var rooms = RoomLegendLoader.loadFromResource("/rooms.csv");
        var roomService = new RoomService(rooms);

        // pornește asistentul de consolă
        new ScheduleAssistant(scheduleService, roomService).run();
    }
}