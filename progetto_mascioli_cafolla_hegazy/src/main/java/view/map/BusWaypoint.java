package view.map;

import org.jxmapviewer.viewer.GeoPosition;

public class BusWaypoint {
    private final GeoPosition position;
    private final WaypointType type;
    private final float bearing; // direzione del bus in gradi (0-360)
    private final String vehicleId;

    // Costruttore per fermate (originale)
    public BusWaypoint(GeoPosition position) {
        this(position, WaypointType.STOP, 0f, null);
    }

    // Costruttore per bus in movimento
    public BusWaypoint(GeoPosition position, float bearing, String vehicleId) {
        this(position, WaypointType.REALTIME_BUS, bearing, vehicleId);
    }

    private BusWaypoint(GeoPosition position, WaypointType type, float bearing, String vehicleId) {
        this.position = position;
        this.type = type;
        this.bearing = bearing;
        this.vehicleId = vehicleId;
    }

    public GeoPosition getPosition() {
        return position;
    }

    public WaypointType getType() {
        return type;
    }

    public float getBearing() {
        return bearing;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public enum WaypointType {
        STOP,           // Waypoint di fermata (cerchio rosso)
        REALTIME_BUS    // Bus in tempo reale (icona bus)
    }
}
