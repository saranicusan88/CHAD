package ro.ut.utassistant;

import java.util.*;
import java.util.stream.Collectors;

public class ScheduleService {
    private final List<Lesson> all;

    public ScheduleService(List<Lesson> allLessons) {
        this.all = new ArrayList<>(allLessons);
    }

    public List<Lesson> getLessons(String group, String semi, String day) {
        String g = normGroup(group);
        String s = normalizeSemi(semi);
        String d = normalizeDay(day);

        return all.stream()
                .filter(l -> normGroup(l.group).equalsIgnoreCase(g))
                .filter(l -> normalizeSemi(l.semi).equalsIgnoreCase(s))
                .filter(l -> normalizeDay(l.day).equalsIgnoreCase(d))
                .sorted(Comparator.comparing(l -> slotStartMinutes(l.timeSlot)))
                .collect(Collectors.toList());
    }

    /**
     * Returnează DOAR pauzele dintre prima si ultima ora a zilei.
     * Ex:
     *  - ai 08-10 si 10-12 => pauze []
     *  - ai 08-10 si 12-14 => pauze ["10-12"]
     */
    public List<String> getBreaks(String group, String semi, String day) {
        List<Lesson> lessons = getLessons(group, semi, day);
        if (lessons.isEmpty()) return List.of();

        // slots standard (extinde dacă ai și altceva)
        List<String> slots = List.of("08-10", "10-12", "12-14", "14-16", "16-18", "18-20", "20-22");

        Set<String> have = lessons.stream()
                .map(l -> l.timeSlot == null ? "" : l.timeSlot.trim())
                .collect(Collectors.toSet());

        // determină intervalul [prima, ultima]
        String firstSlot = lessons.get(0).timeSlot.trim();
        String lastSlot  = lessons.get(lessons.size() - 1).timeSlot.trim();

        int firstIdx = slots.indexOf(firstSlot);
        int lastIdx  = slots.indexOf(lastSlot);

        // dacă nu găsește sloturile în lista standard, nu inventăm pauze
        if (firstIdx == -1 || lastIdx == -1 || lastIdx < firstIdx) return List.of();

        List<String> breaks = new ArrayList<>();
        for (int i = firstIdx; i <= lastIdx; i++) {
            String slot = slots.get(i);
            if (!have.contains(slot)) breaks.add(slot);
        }
        return breaks;
    }

    public String breaksMessage(String group, String semi, String day) {
        List<Lesson> lessons = getLessons(group, semi, day);
        String d = normalizeDay(day);

        if (lessons.isEmpty()) {
            return "Nu ai ore în ziua de " + d + " (grupa " + normGroup(group) + ", semigrupa " + normalizeSemi(semi) + ").";
        }

        List<String> breaks = getBreaks(group, semi, d);

        if (breaks.isEmpty()) {
            String first = lessons.get(0).timeSlot;
            String last = lessons.get(lessons.size() - 1).timeSlot;
            String from = first.split("-")[0];
            String to = last.split("-")[1];
            return "Nu ai intervale de pauze în ziua de " + d + ". Ai ore de la " + from + " la " + to + ".";
        }

        return "Pauzele tale în " + d + " sunt: " + String.join(", ", breaks) + ".";
    }

    // ---------------- helpers ----------------

    private String normGroup(String g) {
        if (g == null) return "";
        return g.trim().replaceAll("\\s+", "");
    }

    private String normalizeSemi(String s) {
        if (s == null) return "";
        s = s.trim();
        if (s.equals("0")) return "";
        return s;
    }

    private String normalizeDay(String s) {
        if (s == null) return "";
        s = s.trim();
        return s.replace("Marți", "Marti")
                .replace("Sâmbătă", "Sambata")
                .replace("Duminică", "Duminica");
    }
    public String shortScheduleMessage(String group, String semi, String day) {
        List<Lesson> lessons = getLessons(group, semi, day);
        if (lessons.isEmpty()) return "Nu ai ore în " + day + ".";

        StringBuilder sb = new StringBuilder("Orar " + day + ":\n");
        for (Lesson l : lessons) {
            String room = (l.roomCode == null || l.roomCode.isBlank()) ? "?" : l.roomCode;
            sb.append(" - ").append(l.timeSlot).append("  ").append(l.subject).append(" – ").append(room).append("\n");
        }
        return sb.toString().trim();
    }

    private int slotStartMinutes(String slot) {
        if (slot == null) return Integer.MAX_VALUE;
        slot = slot.trim();
        // "08-10" -> 08
        String[] p = slot.split("-");
        if (p.length < 1) return Integer.MAX_VALUE;
        try {
            int h = Integer.parseInt(p[0]);
            return h * 60;
        } catch (Exception e) {
            return Integer.MAX_VALUE;
        }
    }
}
