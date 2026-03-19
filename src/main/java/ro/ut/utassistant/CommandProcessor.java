package ro.ut.utassistant;

import java.time.LocalDate;
import java.util.Locale;

public class CommandProcessor {

    private final ScheduleService scheduleService;
    private final RoomService roomService;

    private String group;
    private String semi;

    public CommandProcessor(ScheduleService scheduleService, RoomService roomService) {
        this.scheduleService = scheduleService;
        this.roomService = roomService;
    }

    public String handle(String input) {
        String low = normalize(input);

        // setare grupă inițială: "30212 2"
        if (group == null && low.matches("^\\d{5,6}\\s+[12]$")) {
            String[] p = low.split("\\s+");
            group = p[0];
            semi = p[1];
            return "Ok! Acum grupa=" + group + " semigrupa=" + semi + ". Scrie 'help'.";
        }

        if (low.equals("help")) {
            return """
Comenzi:
  30212 2                     -> setează grupa și semigrupa
  set grupa <302xx> <1|2>      -> schimbă grupa și semigrupa
  ce ore am luni / marti ...   -> orele din ziua respectivă
  ce ore am azi                -> orele de azi
  ce ore am maine              -> orele de mâine
  unde e <sala>                -> locație sală
""";
        }

        if (low.startsWith("set grupa")) {
            String[] parts = low.split("\\s+");
            if (parts.length < 4) return "Format: set grupa <302xx> <1|2>";
            group = parts[2];
            semi = parts[3];
            return "Ok! Acum grupa=" + group + " semigrupa=" + semi;
        }

        if (low.startsWith("unde e")) {
            String what = input.substring(input.toLowerCase(Locale.ROOT).indexOf("unde e") + "unde e".length()).trim();
            return roomService.describeRoom(what);
        }

        // ====== ORAR: STRICT (fără locații + fără pauze) ======
        if (low.startsWith("ce ore am") || low.startsWith("arata orele")) {
            if (group == null) return "Scrie mai întâi grupa: ex. 30212 2";

            if (low.contains("azi")) {
                String day = dayName(LocalDate.now().getDayOfWeek());
                return formatShortDay(day, "Azi");
            }
            if (low.contains("maine")) {
                String day = dayName(LocalDate.now().plusDays(1).getDayOfWeek());
                return formatShortDay(day, "Mâine");
            }

            String day = resolveDay(extractDayToken(input));
            if (day == null) return "Spune ziua: luni/marti/... sau 'azi' / 'maine'.";

            // AICI e cheia: ziua cerută -> tot format scurt
            return formatShortDay(day, "Orar");
        }

        return "Nu am înțeles. Scrie 'help'.";
    }

    // --- Format scurt: DOAR orar (time + subject + sala)
    private String formatShortDay(String day, String label) {
        var lessons = scheduleService.getLessons(group, semi, day);

        if (lessons.isEmpty()) {
            if (label.equals("Orar")) return "Orar " + day + ": nu ai ore.";
            return label + " (" + day + "): nu ai ore.";
        }

        StringBuilder sb;
        if (label.equals("Orar")) sb = new StringBuilder("Orar " + day + ":\n");
        else sb = new StringBuilder(label + " (" + day + "):\n");

        for (var l : lessons) {
            String room = (l.roomCode == null || l.roomCode.isBlank()) ? "?" : l.roomCode;
            sb.append(" - ")
                    .append(l.timeSlot)
                    .append("  ")
                    .append(l.subject)
                    .append(" – ")
                    .append(room)
                    .append("\n");
        }

        return sb.toString().trim();
    }

    private String extractDayToken(String input) {
        String low = input.toLowerCase(Locale.ROOT);
        low = low.replace("arăta", "arata");
        low = low.replace("orele", "ore");
        low = low.replaceFirst("^\\s*(ce\\s+ore\\s+am|arata\\s+orele\\s+pentru|arata\\s+orele)\\s*", "").trim();
        return low;
    }

    private String resolveDay(String token) {
        if (token == null) return null;
        String t = normalize(token);

        return switch (t) {
            case "luni" -> "Luni";
            case "marti" -> "Marti";
            case "miercuri" -> "Miercuri";
            case "joi" -> "Joi";
            case "vineri" -> "Vineri";
            case "sambata" -> "Sambata";
            case "duminica" -> "Duminica";
            default -> null;
        };
    }

    private String dayName(java.time.DayOfWeek d) {
        return switch (d) {
            case MONDAY -> "Luni";
            case TUESDAY -> "Marti";
            case WEDNESDAY -> "Miercuri";
            case THURSDAY -> "Joi";
            case FRIDAY -> "Vineri";
            case SATURDAY -> "Sambata";
            case SUNDAY -> "Duminica";
        };
    }

    private String normalize(String s) {
        s = s.trim().toLowerCase(Locale.ROOT);
        s = s.replace("ă", "a").replace("â", "a").replace("î", "i").replace("ș", "s").replace("ţ", "t").replace("ț", "t");
        s = s.replaceAll("\\s+", " ");
        return s;
    }
}