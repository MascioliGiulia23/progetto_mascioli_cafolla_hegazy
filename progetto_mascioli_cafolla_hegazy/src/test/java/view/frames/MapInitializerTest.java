package view.frames;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test della classe MapInitializer")
class MapInitializerTest {

    @AfterEach
    void tearDown() {
        MapInitializer.resetForTest();
    }

    @Test
    @DisplayName("creaMappaBase - crea una mappa con tileFactory, zoom e posizione di Roma")
    void testCreaMappaBaseBasicSetup() {
        // Disabilita timer/connessione per test deterministico
        MapInitializer.setEnableConnectivityTimerForTest(false);

        JXMapViewer map = MapInitializer.creaMappaBase();

        assertNotNull(map);
        assertNotNull(map.getTileFactory());

        // Zoom e location
        assertEquals(7, map.getZoom());

        GeoPosition pos = map.getAddressLocation();
        assertNotNull(pos);

        // Roma (tolleranza minima)
        assertEquals(41.8902, pos.getLatitude(), 0.0001);
        assertEquals(12.4922, pos.getLongitude(), 0.0001);
    }

    @Test
    @DisplayName("creaMappaBase - se timer disabilitato non deve lanciare eccezioni")
    void testCreaMappaBaseNoTimerDoesNotThrow() {
        MapInitializer.setEnableConnectivityTimerForTest(false);

        assertDoesNotThrow(MapInitializer::creaMappaBase);
    }
}

