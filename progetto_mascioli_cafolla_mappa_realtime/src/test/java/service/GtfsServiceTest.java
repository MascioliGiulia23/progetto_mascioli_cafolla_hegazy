
package service;

import model.gtfs.*;
        import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;
        import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test della classe GtfsService")
class GtfsServiceTest {

    @AfterEach
    void tearDown() {
        // Ripristina comportamento reale dopo ogni test
        GtfsService.resetForTest();
    }

    @Test
    @DisplayName("Costruttore: carica dati tramite loader e inizializza indice stopTimesPerStopId")
    void testCostruttoreCaricaDatiGTFS_EmptyData() {
        // Dati finti (vuoti) -> non serve conoscere i costruttori delle classi model.gtfs
        List<Fermate> fermate = Collections.emptyList();
        List<Route> rotte = Collections.emptyList();
        Map<String, ShapeRoute> forme = Collections.emptyMap();
        List<Trip> trips = Collections.emptyList();
        List<StopTime> stopTimes = Collections.emptyList();
        List<CalendarDate> eccezioni = Collections.emptyList();

        AtomicInteger popolaCalled = new AtomicInteger(0);

        // Inietto loader finti deterministici
        GtfsService.setLoadersForTest(
                () -> fermate,
                () -> rotte,
                () -> forme,
                () -> trips,
                () -> stopTimes,
                () -> eccezioni,
                (t, st) -> popolaCalled.incrementAndGet()
        );

        GtfsService service = new GtfsService();

        assertNotNull(service);

        // Verifico che i dati siano quelli forniti dai loader
        assertSame(fermate, service.getFermate());
        assertSame(rotte, service.getRotte());
        assertSame(forme, service.getForme());
        assertSame(trips, service.getTrips());
        assertSame(stopTimes, service.getStopTimes());
        assertSame(eccezioni, service.getEccezioni());

        // Verifico che il populator sia stato chiamato una volta
        assertEquals(1, popolaCalled.get());

        // Indice stopTimesPerStopId deve esistere e, con stopTimes vuoti, essere vuoto
        assertNotNull(service.getStopTimesPerStopId());
        assertTrue(service.getStopTimesPerStopId().isEmpty());
    }

    @Test
    @DisplayName("Reset: ripristina i loader reali (nessuna eccezione)")
    void testResetForTestDoesNotThrow() {
        assertDoesNotThrow(GtfsService::resetForTest);
    }
}
