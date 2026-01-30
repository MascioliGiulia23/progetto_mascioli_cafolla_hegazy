package view.map;

import org.jxmapviewer.viewer.GeoPosition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test della classe BusWaypoint")
class BusWaypointTest {

    @Test
    @DisplayName("Costruttore fermata imposta tipo STOP")
    void testCostruttoreFermata() {
        GeoPosition pos = new GeoPosition(41.9, 12.5);

        BusWaypoint wp = new BusWaypoint(pos);

        assertEquals(pos, wp.getPosition());
        assertEquals(BusWaypoint.WaypointType.STOP, wp.getType());
        assertEquals(0f, wp.getBearing());
        assertNull(wp.getVehicleId());
    }

    @Test
    @DisplayName("Costruttore bus realtime imposta tipo REALTIME_BUS")
    void testCostruttoreRealtimeBus() {
        GeoPosition pos = new GeoPosition(41.91, 12.51);

        BusWaypoint wp = new BusWaypoint(pos, 135f, "BUS_123");

        assertEquals(pos, wp.getPosition());
        assertEquals(BusWaypoint.WaypointType.REALTIME_BUS, wp.getType());
        assertEquals(135f, wp.getBearing());
        assertEquals("BUS_123", wp.getVehicleId());
    }

    @Test
    @DisplayName("Bearing può essere zero")
    void testBearingZero() {
        BusWaypoint wp = new BusWaypoint(
                new GeoPosition(0, 0),
                0f,
                "BUS_ZERO"
        );

        assertEquals(0f, wp.getBearing());
    }

    @Test
    @DisplayName("VehicleId può essere null per fermate")
    void testVehicleIdNull() {
        BusWaypoint wp = new BusWaypoint(new GeoPosition(10, 10));
        assertNull(wp.getVehicleId());
    }

    @Test
    @DisplayName("GeoPosition viene mantenuta correttamente")
    void testGeoPosition() {
        GeoPosition pos = new GeoPosition(45.0, 9.0);
        BusWaypoint wp = new BusWaypoint(pos);

        assertEquals(45.0, wp.getPosition().getLatitude());
        assertEquals(9.0, wp.getPosition().getLongitude());
    }
}
