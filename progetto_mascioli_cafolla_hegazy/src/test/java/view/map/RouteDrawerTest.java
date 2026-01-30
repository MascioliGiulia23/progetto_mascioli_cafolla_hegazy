package view.map;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Color;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test della classe RouteDrawer")
class RouteDrawerTest {

    // Fake map viewer per rendere il test unitario e deterministico (no UI reale)
    private static class RecordingMapViewer extends JXMapViewer {
        Painter<JXMapViewer> lastOverlayPainter = null;
        GeoPosition lastAddressLocation = null;

        @Override
        public void setOverlayPainter(Painter<? super JXMapViewer> painter) {
            @SuppressWarnings("unchecked")
            Painter<JXMapViewer> p = (Painter<JXMapViewer>) painter;
            this.lastOverlayPainter = p;
        }

        @Override
        public void setAddressLocation(GeoPosition location) {
            this.lastAddressLocation = location;
        }
    }

    @Test
    @DisplayName("drawGeoPositions - aggiunge painter, imposta overlay e centra su punto medio")
    void testDrawGeoPositionsAddsPainterAndCenters() {
        RecordingMapViewer map = new RecordingMapViewer();
        RouteDrawer drawer = new RouteDrawer(map);

        List<GeoPosition> track = List.of(
                new GeoPosition(41.0, 12.0),
                new GeoPosition(41.5, 12.5),
                new GeoPosition(42.0, 13.0)
        );

        drawer.drawGeoPositions(track, Color.RED);

        assertEquals(1, drawer.getPaintersCountForTest());
        assertNotNull(map.lastOverlayPainter);

        // Centro = elemento a metà (index 1 su 3)
        assertNotNull(map.lastAddressLocation);
        assertEquals(41.5, map.lastAddressLocation.getLatitude(), 0.0001);
        assertEquals(12.5, map.lastAddressLocation.getLongitude(), 0.0001);

        // getGeoPositions deve tornare la track dell'ultimo painter
        List<GeoPosition> out = drawer.getGeoPositions();
        assertEquals(3, out.size());
        assertEquals(41.0, out.get(0).getLatitude(), 0.0001);
        assertEquals(42.0, out.get(2).getLatitude(), 0.0001);
    }

    @Test
    @DisplayName("drawGeoPositions - track vuota o null non modifica lo stato")
    void testDrawGeoPositionsWithEmptyDoesNothing() {
        RecordingMapViewer map = new RecordingMapViewer();
        RouteDrawer drawer = new RouteDrawer(map);

        drawer.drawGeoPositions(List.of(), Color.BLUE);
        assertEquals(0, drawer.getPaintersCountForTest());
        assertNull(map.lastOverlayPainter);
        assertNull(map.lastAddressLocation);

        drawer.drawGeoPositions(null, Color.BLUE);
        assertEquals(0, drawer.getPaintersCountForTest());
        assertNull(map.lastOverlayPainter);
        assertNull(map.lastAddressLocation);
    }

    @Test
    @DisplayName("clearAll - rimuove tutte le tratte e azzera overlay painter")
    void testClearAll() {
        RecordingMapViewer map = new RecordingMapViewer();
        RouteDrawer drawer = new RouteDrawer(map);

        drawer.drawGeoPositions(List.of(new GeoPosition(1, 1)), Color.BLACK);
        assertEquals(1, drawer.getPaintersCountForTest());
        assertNotNull(map.lastOverlayPainter);

        drawer.clearAll();

        assertEquals(0, drawer.getPaintersCountForTest());
        assertNull(map.lastOverlayPainter);
    }

    @Test
    @DisplayName("getGeoPositions - se non ci sono painters ritorna lista vuota")
    void testGetGeoPositionsEmpty() {
        RecordingMapViewer map = new RecordingMapViewer();
        RouteDrawer drawer = new RouteDrawer(map);

        assertTrue(drawer.getGeoPositions().isEmpty());
    }
}
