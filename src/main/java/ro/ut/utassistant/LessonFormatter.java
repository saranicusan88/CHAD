package ro.ut.utassistant;

import java.util.List;

public class LessonFormatter {

    public static String describeLessons(List<Lesson> lessons, RoomService roomService) {
        if (lessons == null || lessons.isEmpty()) {
            return "Nu ai ore în intervalul acesta.";
        }

        StringBuilder sb = new StringBuilder();

        for (Lesson l : lessons) {
            sb.append(l.day)
                    .append(" ")
                    .append(l.timeSlot)
                    .append(" — ")
                    .append(l.subject);

            if (l.roomCode != null && !l.roomCode.isBlank()) {
                sb.append(" (Sala ").append(l.roomCode).append(")");

                if (roomService != null) {
                    String roomDesc = roomService.describeRoom(l.roomCode);
                    if (roomDesc != null && !roomDesc.isBlank()) {
                        sb.append("\n   ").append(roomDesc);
                    }
                }
            } else {
                sb.append(" (Sala: necunoscută)");
            }

            sb.append("\n");
        }

        return sb.toString().trim();
    }
}
