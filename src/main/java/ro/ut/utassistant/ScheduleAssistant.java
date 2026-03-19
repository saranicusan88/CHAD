package ro.ut.utassistant;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class ScheduleAssistant {

    private final ScheduleService scheduleService;
    private final RoomService roomService;

    private String group;
    private String semi;

    public ScheduleAssistant(ScheduleService scheduleService, RoomService roomService) {
        this.scheduleService = scheduleService;
        this.roomService = roomService;
    }

    public void run() {
        Scanner sc = new Scanner(System.in);

        // 1) cere grupa + semigrupa o singură dată
        while (true) {
            System.out.println("Hei! Am auzit că ai nevoie de ajutor! Ce grupă și semigrupă ești?");
            System.out.println("Ex: 30212 2");
            System.out.print("> ");

            String line = sc.nextLine();
            if (line == null) return;

            String input = line.trim();
            if (input.isEmpty()) continue;

            String low = normalize(input);

            if (low.matches("^\\d{5,6}\\s+[12]$")) {
                String[] p = low.split("\\s+");
                this.group = p[0];
                this.semi = p[1];
                break;
            }

            System.out.println("Format invalid. Exemplu corect: 30212 2\n");
        }

        // 2) mesaj după ce ai grupa
        System.out.println("Ok! Acum grupa=" + group + " semigrupa=" + semi + ".");
        System.out.println("Scrie 'help' pentru a vedea cu ce te pot ajuta.\n");

        // 3) bucla de comenzi
        while (true) {
            System.out.print("> ");
            String line = sc.nextLine();
            if (line == null) break;

            String input = line.trim();
            if (input.isEmpty()) continue;

            String low = normalize(input);

            if (low.equals("exit")) {
                System.out.println("Pa! 👋");
                break;
            }

            if (low.equals("help")) {
                printHelp();
                continue;
            }

            // schimbare grupa ulterioară
            if (low.startsWith("set grupa")) {
                String[] parts = low.split("\\s+");
                if (parts.length < 4) {
                    System.out.println("Format: set grupa <302xx> <1|2>");
                    continue;
                }
                this.group = parts[2];
                this.semi = parts[3];
                System.out.println("Ok! Acum grupa=" + group + " semigrupa=" + semi);
                continue;
            }

            // unde e ...
            if (low.startsWith("unde e")) {
                String what = input.substring(input.toLowerCase(Locale.ROOT).indexOf("unde e") + "unde e".length()).trim();
                System.out.println(roomService.describeRoom(what));
                continue;
            }

            // ce ore am ...
            if (low.startsWith("ce ore am") || low.startsWith("arata orele")) {
                if (low.contains("azi")) {
                    String day = dayName(LocalDate.now().getDayOfWeek());
                    printShortDay(day, "Azi");
                    continue;
                }
                if (low.contains("maine")) {
                    String day = dayName(LocalDate.now().plusDays(1).getDayOfWeek());
                    printShortDay(day, "Mâine");
                    continue;
                }

                String dayToken = extractDayToken(input);
                String day = resolveDay(dayToken);

                if (day == null) {
                    System.out.println("Spune ziua: luni/marti/... sau 'azi' / 'maine'.");
                    continue;
                }

                printDetailedDay(day);
                continue;
            }

            System.out.println("Nu am înțeles. Scrie 'help'.");
        }
    }


    // --- Format scurt: doar materie + sală
    private void printShortDay(String day, String label) {
        List<Lesson> lessons = scheduleService.getLessons(group, semi, day);

        if (lessons.isEmpty()) {
            System.out.println(label + " (" + day + "): nu ai ore.");
            return;
        }

        System.out.println(label + " (" + day + "):");
        for (Lesson l : lessons) {
            String room = (l.roomCode == null || l.roomCode.isBlank()) ? "?" : l.roomCode;
            System.out.println(" - " + l.timeSlot + "  " + l.subject + " – " + room);
        }
    }

    // --- Format detaliat (cum aveai)
    private void printDetailedDay(String day) {
        List<Lesson> lessons = scheduleService.getLessons(group, semi, day);

        if (lessons.isEmpty()) {
            System.out.println("Nu ai ore în ziua de " + day + " (grupa " + group + ", semigrupa " + semi + ").");
            return;
        }

        System.out.println("Orar " + day + " (grupa " + group + ", semigrupa " + semi + "):");
        for (Lesson l : lessons) {
            System.out.println(" - " + l.timeSlot + "  " + l.subject + "  (" + l.rawText + ")");
            if (l.roomCode != null && !l.roomCode.isBlank()) {
                System.out.println("    " + roomService.describeRoom(l.roomCode));
            }
        }

        System.out.println(scheduleService.breaksMessage(group, semi, day));
    }

    private void printHelp() {
        System.out.println("""
Comenzi:
  30212 2                     -> setează grupa și semigrupa (la început)
  set grupa <302xx> <1|2>      -> schimbă grupa și semigrupa
  ce ore am luni / marti ...   -> arată orele din ziua respectivă (detaliat)
  ce ore am azi                -> arată orele de azi (scurt)
  ce ore am maine              -> arată orele de mâine (scurt)
  unde e <sala>                -> descriere + adresă + link (din rooms.csv)
  help                         -> comenzi
  exit                         -> ieșire
""");
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

    private String dayName(DayOfWeek d) {
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
