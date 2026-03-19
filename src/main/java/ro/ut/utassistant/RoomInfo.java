package ro.ut.utassistant;

public class RoomInfo {
    public final String roomCode;
    public final String building;
    public final String address;
    public final String floor;
    public final String shortHint;
    public final String mapsLink;

    public RoomInfo(String roomCode, String building, String address, String floor, String shortHint, String mapsLink) {
        this.roomCode = roomCode;
        this.building = building == null ? "" : building;
        this.address = address == null ? "" : address;
        this.floor = floor == null ? "" : floor;
        this.shortHint = shortHint == null ? "" : shortHint;
        this.mapsLink = mapsLink == null ? "" : mapsLink;
    }
    public String humanDescription() {
        StringBuilder sb = new StringBuilder();

        if (building != null && !building.isBlank())
            sb.append(building);

        if (floor != null && !floor.isBlank())
            sb.append(", etaj ").append(floor);

        if (shortHint != null && !shortHint.isBlank())
            sb.append(" (").append(shortHint).append(")");

        if (address != null && !address.isBlank())
            sb.append(". ").append(address);

        if (mapsLink != null && !mapsLink.isBlank())
            sb.append(" ").append(mapsLink);

        return sb.toString().trim();
    }

    @Override
    public String toString() {
        return "RoomInfo[" +
                "roomCode=" + roomCode +
                ", building=" + building +
                ", address=" + address +
                ", floor=" + floor +
                ", shortHint=" + shortHint +
                ", mapsLink=" + mapsLink +
                ']';
    }
}
