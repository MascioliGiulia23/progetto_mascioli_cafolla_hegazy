package model.gtfs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test della classe Trip")
class TripTest {

    private Trip trip1;
    private Trip trip2;
    private StopTime stop1;
    private StopTime stop2;
    private StopTime stop3;

    @BeforeEach
    void setUp() {
        // Trip semplificato
        trip1 = new Trip("100", "WD", "trip_001", "Casilina");

        // Trip completo
        trip2 = new Trip(
                "101", "WE", "trip_002", "Termini",
                "64/A", 0, "block_1", "shape_1", 1, 0
        );

        // StopTimes di test
        stop1 = new StopTime("trip_001", LocalTime.of(8, 0), LocalTime.of(8, 0), "70001", 1);
        stop2 = new StopTime("trip_001", LocalTime.of(8, 10), LocalTime.of(8, 11), "70002", 2);
        stop3 = new StopTime("trip_001", LocalTime.of(8, 20), LocalTime.of(8, 20), "70003", 3);
    }

    @Test
    @DisplayName("Test costruttore semplificato")
    void testCostruttoreSemplificato() {
        assertNotNull(trip1);
        assertEquals("100", trip1.getRouteId());
        assertEquals("trip_001", trip1.getTripId());
        assertEquals("Casilina", trip1.getTripHeadsign());
    }

    @Test
    @DisplayName("Test costruttore completo")
    void testCostruttoreCompleto() {
        assertNotNull(trip2);
        assertEquals("101", trip2.getRouteId());
        assertEquals("64/A", trip2.getTripShortName());
        assertEquals(0, trip2.getDirectionId());
        assertEquals("shape_1", trip2.getShapeId());
        assertEquals(1, trip2.getWheelchairAccessible());
    }

    @Test
    @DisplayName("Test aggiunta stop times")
    void testAggiungiStopTime() {
        trip1.aggiungiStopTime(stop1);
        trip1.aggiungiStopTime(stop2);

        assertEquals(2, trip1.getNumeroFermate());
        assertEquals(2, trip1.getStopTimes().size());
    }

    @Test
    @DisplayName("Test get primo stop")
    void testGetPrimoStop() {
        trip1.aggiungiStopTime(stop1);
        trip1.aggiungiStopTime(stop2);
        trip1.aggiungiStopTime(stop3);

        StopTime primo = trip1.getPrimoStop();
        assertNotNull(primo);
        assertEquals(1, primo.getStopSequence());
        assertEquals("70001", primo.getStopId());
    }

    @Test
    @DisplayName("Test get ultimo stop")
    void testGetUltimoStop() {
        trip1.aggiungiStopTime(stop1);
        trip1.aggiungiStopTime(stop2);
        trip1.aggiungiStopTime(stop3);

        StopTime ultimo = trip1.getUltimoStop();
        assertNotNull(ultimo);
        assertEquals(3, ultimo.getStopSequence());
        assertEquals("70003", ultimo.getStopId());
    }

    @Test
    @DisplayName("Test trip senza fermate")
    void testTripVuoto() {
        assertEquals(0, trip1.getNumeroFermate());
        assertNull(trip1.getPrimoStop());
        assertNull(trip1.getUltimoStop());
    }

    @Test
    @DisplayName("Test durata totale corsa")
    void testGetDurataTotaleCorse() {
        trip1.aggiungiStopTime(stop1); // 08:00
        trip1.aggiungiStopTime(stop3); // 08:20

        long durata = trip1.getDurataTotaleCorse();

        // 20 minuti = 1200 secondi
        assertEquals(1200, durata);
    }

    @Test
    @DisplayName("Test get stop by sequence")
    void testGetStopAtSequence() {
        trip1.aggiungiStopTime(stop1);
        trip1.aggiungiStopTime(stop2);
        trip1.aggiungiStopTime(stop3);

        StopTime found = trip1.getStopAtSequence(2);
        assertNotNull(found);
        assertEquals("70002", found.getStopId());
    }

    @Test
    @DisplayName("Test get stop by fermata ID")
    void testGetStopByFermataId() {
        trip1.aggiungiStopTime(stop1);
        trip1.aggiungiStopTime(stop2);
        trip1.aggiungiStopTime(stop3);

        StopTime found = trip1.getStopByFermataId("70002");
        assertNotNull(found);
        assertEquals(2, found.getStopSequence());
    }

    @Test
    @DisplayName("Test getStopIds ritorna lista IDs")
    void testGetStopIds() {
        trip1.aggiungiStopTime(stop1);
        trip1.aggiungiStopTime(stop2);
        trip1.aggiungiStopTime(stop3);

        List<String> stopIds = trip1.getStopIds();

        assertEquals(3, stopIds.size());
        assertTrue(stopIds.contains("70001"));
        assertTrue(stopIds.contains("70002"));
        assertTrue(stopIds.contains("70003"));
    }

    @Test
    @DisplayName("Test wheelchair accessibile")
    void testIsWheelchairAccessibile() {
        assertTrue(trip2.isWheelchairAccessibile());
        assertFalse(trip1.isWheelchairAccessibile());
    }

    @Test
    @DisplayName("Test bikes consentite")
    void testIsBikesConsentite() {
        assertFalse(trip2.isBikesConsentite());
        assertFalse(trip1.isBikesConsentite());
    }

    @Test
    @DisplayName("Test wheelchair descrizione")
    void testGetWheelchairAccessibileDescrizione() {
        assertEquals("Accessibile", trip2.getWheelchairAccessibileDescrizione());
        assertEquals("Nessuna informazione", trip1.getWheelchairAccessibileDescrizione());
    }

    @Test
    @DisplayName("Test bikes descrizione")
    void testGetBikesAllowedDescrizione() {
        assertEquals("Nessuna informazione", trip2.getBikesAllowedDescrizione());
    }

    @Test
    @DisplayName("Test equals con stesso trip ID")
    void testEqualsStessoId() {
        Trip trip1Copy = new Trip("999", "X", "trip_001", "X");
        assertEquals(trip1, trip1Copy);
    }

    @Test
    @DisplayName("Test equals con trip ID diverso")
    void testEqualsIdDiverso() {
        assertNotEquals(trip1, trip2);
    }

    @Test
    @DisplayName("Test hashCode")
    void testHashCode() {
        Trip trip1Copy = new Trip("999", "X", "trip_001", "X");
        assertEquals(trip1.hashCode(), trip1Copy.hashCode());
    }

    @Test
    @DisplayName("Test toString")
    void testToString() {
        trip1.aggiungiStopTime(stop1);
        String str = trip1.toString();

        assertTrue(str.contains("trip_001"));
        assertTrue(str.contains("100"));
        assertTrue(str.contains("Casilina"));
    }
}
