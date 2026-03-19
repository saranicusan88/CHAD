package ro.ut.utassistant;

public class Lesson {
    public final String group;
    public final String semi;      // "1" / "2" / "" (dacă nu există)
    public final String day;       // Luni...
    public final String timeSlot;  // 08-10
    public final String subject;   // PL / PC / AM...
    public final String rawText;   // text complet din celulă
    public final String roomCode;  // BT 5.01 / 204 / P03 etc

    public Lesson(String group, String semi, String day, String timeSlot, String subject, String rawText, String roomCode) {
        this.group = group;
        this.semi = semi == null ? "" : semi;
        this.day = day;
        this.timeSlot = timeSlot;
        this.subject = subject;
        this.rawText = rawText;
        this.roomCode = roomCode == null ? "" : roomCode;
    }

    @Override
    public String toString() {
        return "Lesson[group=" + group +
                ", semi=" + semi +
                ", day=" + day +
                ", timeSlot=" + timeSlot +
                ", subject=" + subject +
                ", rawText=" + rawText +
                ", roomCode=" + roomCode + "]";
    }
}
