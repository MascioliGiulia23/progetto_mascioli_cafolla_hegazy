package view.map;

import org.jxmapviewer.viewer.GeoPosition;

public class BusWaypoint {
    private final GeoPosition position;

    public BusWaypoint(GeoPosition position) {
        this.position = position;
    }

    public GeoPosition getPosition() {
        return position;
    }
}
