package view.map;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test della classe WaypointDrawer")
class WaypointDrawerTest {

    // Map viewer finto per evitare dipendenze UI reali
    private static class RecordingMapViewer extends JXMapViewer {
        Painter<JXMapViewer> lastOverlayPainter = null;

        @Override
        public void setOverlayPainter(Painter<? super JXMapViewer> painter) {
            @SuppressWarnings("unchecked")
            Painter<JXMapViewer> p = (Painter<JXMapViewer>) painter;
            lastOverlayPainter = p;
        }
    }

    @Test
    @DisplayName("addWaypoint - aggiunge waypoint e imposta overlay painter")
    void testAddWaypoint() {
        RecordingMapViewer map = new RecordingMapViewer();
        WaypointDrawer drawer = new WaypointDrawer(map, null);

        BusWaypoint wp = new BusWaypoint(new GeoPosition(41.9, 12.5));
        drawer.addWaypoint(wp);

        assertEquals(1, drawer.getWaypointsCountForTest());
        assertNotNull(drawer.getLastOverlayPainterForTest());
        assertNotNull(map.lastOverlayPainter);
    }

    @Test
    @DisplayName("addWaypoints - aggiunge collezione e imposta overlay painter")
    void testAddWaypoints() {
        RecordingMapViewer map = new RecordingMapViewer();
        WaypointDrawer drawer = new WaypointDrawer(map, null);

        drawer.addWaypoints(List.of(
                new BusWaypoint(new GeoPosition(1, 1)),
                new BusWaypoint(new GeoPosition(2, 2)),
                new BusWaypoint(new GeoPosition(3, 3))
        ));

        assertEquals(3, drawer.getWaypointsCountForTest());
        assertNotNull(drawer.getLastOverlayPainterForTest());
    }

    @Test
    @DisplayName("clearWaypoints - svuota e rimuove overlay painter")
    void testClearWaypoints() {
        RecordingMapViewer map = new RecordingMapViewer();
        WaypointDrawer drawer = new WaypointDrawer(map, null);

        drawer.addWaypoint(new BusWaypoint(new GeoPosition(1, 1)));
        assertEquals(1, drawer.getWaypointsCountForTest());
        assertNotNull(drawer.getLastOverlayPainterForTest());

        drawer.clearWaypoints();

        assertEquals(0, drawer.getWaypointsCountForTest());
        assertNull(drawer.getLastOverlayPainterForTest());
        assertNull(map.lastOverlayPainter);
    }

    @Test
    @DisplayName("clearRealtimeBusWaypoints - rimuove solo REALTIME_BUS")
    void testClearRealtimeBusWaypoints() {
        RecordingMapViewer map = new RecordingMapViewer();
        WaypointDrawer drawer = new WaypointDrawer(map, null);

        BusWaypoint stop1 = new BusWaypoint(new GeoPosition(1, 1));
        BusWaypoint bus1 = new BusWaypoint(new GeoPosition(2, 2), 90f, "busA");
        BusWaypoint stop2 = new BusWaypoint(new GeoPosition(3, 3));
        BusWaypoint bus2 = new BusWaypoint(new GeoPosition(4, 4), 180f, "busB");

        drawer.addWaypoints(List.of(stop1, bus1, stop2, bus2));
        assertEquals(4, drawer.getWaypointsCountForTest());

        drawer.clearRealtimeBusWaypoints();

        Set<BusWaypoint> remaining = drawer.getWaypointsSnapshotForTest();
        assertEquals(2, remaining.size());
        assertTrue(remaining.stream().allMatch(wp -> wp.getType() == BusWaypoint.WaypointType.STOP));
    }

    @Test
    @DisplayName("addWaypoint - se null non cambia nulla")
    void testAddWaypointNull() {
        RecordingMapViewer map = new RecordingMapViewer();
        WaypointDrawer drawer = new WaypointDrawer(map, null);

        drawer.addWaypoint(null);

        assertEquals(0, drawer.getWaypointsCountForTest());
        assertNull(drawer.getLastOverlayPainterForTest());
        assertNull(map.lastOverlayPainter);
    }
}

