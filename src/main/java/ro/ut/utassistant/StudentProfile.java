package ro.ut.utassistant;

public class StudentProfile {
    private String group;       // ex: 30211
    private String semiGroup;   // "1" / "2" sau gol

    public StudentProfile(String group, String semiGroup) {
        this.group = group;
        this.semiGroup = semiGroup;
    }

    public String group() { return group; }
    public String semiGroup() { return semiGroup; }

    public void setGroup(String group) { this.group = group; }
    public void setSemiGroup(String semiGroup) { this.semiGroup = semiGroup; }

    public String effectiveKey() {
        if (semiGroup == null || semiGroup.isBlank()) return group;
        return group + "-" + semiGroup;
    }
}
