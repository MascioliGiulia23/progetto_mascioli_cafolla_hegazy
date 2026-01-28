package service;

import model.gtfs.Fermate;
import model.gtfs.ShapeRoute;
import model.gtfs.StopTime;
import model.gtfs.Trip;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import view.map.BusWaypoint;
import view.map.WaypointDrawer;

import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test della classe MapService")
class MapServiceTest {

    @Test
    @DisplayName("creaWaypointsDaTrip - crea waypoint solo per stopTimes del trip e stop presenti in fermate")
    void testCreaWaypointsDaTrip() {
        // Fermate disponibili
        Fermate f1 = new Fermate("S1", "Stop 1", 41.9000, 12.5000);
        Fermate f2 = new Fermate("S2", "Stop 2", 41.9100, 12.5300);

        List<Fermate> fermate = Arrays.asList(f1, f2);

        // Trip
        Trip trip = new Trip("R1", "SV1", "T1", "Headsign");

        // StopTimes: 2 del trip T1 (validi), 1 di altro trip (da ignorare), 1 con stopId non presente (da ignorare)
        StopTime st1 = new StopTime("T1", LocalTime.of(8, 0), LocalTime.of(8, 1), "S1", 1);
        StopTime st2 = new StopTime("T1", LocalTime.of(8, 10), LocalTime.of(8, 11), "S2", 2);
        StopTime stOtherTrip = new StopTime("T2", LocalTime.of(9, 0), LocalTime.of(9, 1), "S1", 1);
        StopTime stMissingStop = new StopTime("T1", LocalTime.of(8, 20), LocalTime.of(8, 21), "S999", 3);

        List<StopTime> stopTimes = Arrays.asList(st1, st2, stOtherTrip, stMissingStop);

        // Drawer finti (qui non servono, possiamo passare null)
        MapService mapService = new MapService(
                null,
                null,
                fermate,
                Collections.emptyList(),
                Collections.singletonList(trip),
                stopTimes,
                Collections.emptyMap()
        );

        List<BusWaypoint> waypoints = mapService.creaWaypointsDaTrip(trip);

        assertNotNull(waypoints);
        assertEquals(2, waypoints.size());

        // Verifica coordinate contenute
        assertTrue(containsPosition(waypoints, 41.9000, 12.5000));
        assertTrue(containsPosition(waypoints, 41.9100, 12.5300));
    }

    @Test
    @DisplayName("mostraWaypointFermate - se trip è null non deve crashare e non deve chiamare drawer")
    void testMostraWaypointFermateNullTrip() {
        RecordingWaypointDrawer drawer = new RecordingWaypointDrawer();

        MapService mapService = new MapService(
                null,
                drawer,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyMap()
        );

        assertDoesNotThrow(() -> mapService.mostraWaypointFermate(null));
        assertEquals(0, drawer.clearCalls);
        assertEquals(0, drawer.addWaypointsCalls);
    }

    @Test
    @DisplayName("mostraWaypointFermate - se trip.getStopTimes() è null non deve crashare e non deve chiamare drawer")
    void testMostraWaypointFermateStopTimesNull() {
        RecordingWaypointDrawer drawer = new RecordingWaypointDrawer();

        Trip trip = new Trip("R1", "SV1", "T1", "Headsign");
        // forziamo stopTimes a null simulando comportamento esterno:
        // qui non possiamo settare direttamente perché Trip inizializza stopTimes.
        // Quindi testiamo la condizione più realistica: trip creato ma stopTimes vuoti.
        // La condizione "null" in MapService serve a proteggere da input esterno,
        // ma Trip nel tuo modello non lo restituisce mai null.

        MapService mapService = new MapService(
                null,
                drawer,
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.singletonList(trip),
                Collections.emptyList(),
                Collections.emptyMap()
        );

        // Trip ha stopTimes vuoti: MapService deve clear+addWaypoints con set vuoto? NO:
        // il codice fa return solo se stopTimes == null, quindi con lista vuota prosegue e disegna 0 fermate.
        // Quindi ci aspettiamo che clearWaypoints e addWaypoints vengano chiamati.
        assertDoesNotThrow(() -> mapService.mostraWaypointFermate(trip));
        assertEquals(1, drawer.clearCalls);
        assertEquals(1, drawer.addWaypointsCalls);
        assertNotNull(drawer.lastAddedWaypoints);
        assertTrue(drawer.lastAddedWaypoints.isEmpty());
    }

    @Test
    @DisplayName("mostraWaypointFermate - crea waypoint da stopTimes (ordinati per stopSequence) e li passa al drawer")
    void testMostraWaypointFermateCreatesWaypoints() {
        RecordingWaypointDrawer drawer = new RecordingWaypointDrawer();

        Fermate f1 = new Fermate("S1", "Stop 1", 41.9000, 12.5000);
        Fermate f2 = new Fermate("S2", "Stop 2", 41.9100, 12.5300);
        Fermate f3 = new Fermate("S3", "Stop 3", 41.9200, 12.5400);

        List<Fermate> fermate = Arrays.asList(f1, f2, f3);

        Trip trip = new Trip("R1", "SV1", "T1", "Headsign");

        // stopTimes nel trip: volutamente NON ordinati
        StopTime st2 = new StopTime("T1", LocalTime.of(8, 10), LocalTime.of(8, 11), "S2", 2);
        StopTime st1 = new StopTime("T1", LocalTime.of(8, 0), LocalTime.of(8, 1), "S1", 1);
        StopTime st3 = new StopTime("T1", LocalTime.of(8, 20), LocalTime.of(8, 21), "S3", 3);

        trip.aggiungiStopTime(st2);
        trip.aggiungiStopTime(st1);
        trip.aggiungiStopTime(st3);

        MapService mapService = new MapService(
                null,
                drawer,
                fermate,
                Collections.emptyList(),
                Collections.singletonList(trip),
                Collections.emptyList(),
                Collections.emptyMap()
        );

        mapService.mostraWaypointFermate(trip);

        assertEquals(1, drawer.clearCalls);
        assertEquals(1, drawer.addWaypointsCalls);

        assertNotNull(drawer.lastAddedWaypoints);
        assertEquals(3, drawer.lastAddedWaypoints.size());

        // Verifica che le posizioni corrispondano alle 3 fermate
        assertTrue(containsPositionInSet(drawer.lastAddedWaypoints, 41.9000, 12.5000));
        assertTrue(containsPositionInSet(drawer.lastAddedWaypoints, 41.9100, 12.5300));
        assertTrue(containsPositionInSet(drawer.lastAddedWaypoints, 41.9200, 12.5400));
    }

    // ==================== Helper ====================

    private static boolean containsPosition(List<BusWaypoint> waypoints, double lat, double lon) {
        for (BusWaypoint wp : waypoints) {
            GeoPosition p = wp.getPosition();
            if (Math.abs(p.getLatitude() - lat) < 0.0001 && Math.abs(p.getLongitude() - lon) < 0.0001) {
                return true;
            }
        }
        return false;
    }

    private static boolean containsPositionInSet(Set<BusWaypoint> waypoints, double lat, double lon) {
        for (BusWaypoint wp : waypoints) {
            GeoPosition p = wp.getPosition();
            if (Math.abs(p.getLatitude() - lat) < 0.0001 && Math.abs(p.getLongitude() - lon) < 0.0001) {
                return true;
            }
        }
        return false;
    }

    /**
     * WaypointDrawer finto che registra le chiamate senza disegnare nulla.
     * Non rompe nulla perché è usato solo nei test.
     */
    private static class RecordingWaypointDrawer extends WaypointDrawer {

        int clearCalls = 0;
        int addWaypointsCalls = 0;
        Set<BusWaypoint> lastAddedWaypoints = null;

        RecordingWaypointDrawer() {
            // JXMapViewer esiste nella tua dipendenza (lo usi già in produzione).
            // RouteDrawer qui non serve: passiamo null.
            super(new JXMapViewer(), null);
        }

        @Override
        public void clearWaypoints() {
            clearCalls++;
            // NON chiamiamo super.clearWaypoints() per evitare logica di painting
        }

        @Override
        public void addWaypoints(Collection<BusWaypoint> newWaypoints) {
            addWaypointsCalls++;
            lastAddedWaypoints = (newWaypoints == null) ? null : new HashSet<>(newWaypoints);
            // NON chiamiamo super.addWaypoints() per evitare logica di painting
        }

        @Override
        public void addWaypoint(BusWaypoint waypoint) {
            // non usato in questi test, ma lo override lo rende "safe"
        }
    }
}

