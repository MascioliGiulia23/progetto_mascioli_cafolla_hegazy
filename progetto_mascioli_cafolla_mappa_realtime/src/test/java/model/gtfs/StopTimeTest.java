package model.gtfs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalTime;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test della classe StopTime")
class StopTimeTest {

    private StopTime stopTime1;
    private StopTime stopTime2;
    private StopTime stopTime3;

    @BeforeEach
    void setUp() {
        // Stop semplificato
        stopTime1 = new StopTime(
                "trip_001",
                LocalTime.of(8, 30),
                LocalTime.of(8, 32),
                "70001",
                1
        );

        // Stop completo
        stopTime2 = new StopTime(
                "trip_001",
                LocalTime.of(8, 45),
                LocalTime.of(8, 46),
                "70002",
                2,
                "Termini",
                0,  // pickup regolare
                0,  // dropoff regolare
                2.5
        );

        // Stop con no pickup
        stopTime3 = new StopTime(
                "trip_002",
                LocalTime.of(9, 0),
                LocalTime.of(9, 0),
                "70003",
                1,
                "Capolinea",
                1,  // no pickup
                0,  // dropoff regolare
                0.0
        );
    }

    @Test
    @DisplayName("Test costruttore semplificato")
    void testCostruttoreSemplificato() {
        assertNotNull(stopTime1);
        assertEquals("trip_001", stopTime1.getTripId());
        assertEquals("70001", stopTime1.getStopId());
        assertEquals(1, stopTime1.getStopSequence());
        assertEquals(LocalTime.of(8, 30), stopTime1.getArrivalTime());
        assertEquals(LocalTime.of(8, 32), stopTime1.getDepartureTime());
    }

    @Test
    @DisplayName("Test costruttore completo")
    void testCostruttoreCompleto() {
        assertNotNull(stopTime2);
        assertEquals("Termini", stopTime2.getStopHeadsign());
        assertEquals(0, stopTime2.getPickupType());
        assertEquals(0, stopTime2.getDropOffType());
        assertEquals(2.5, stopTime2.getShapeDistTraveled(), 0.001);
    }

    @Test
    @DisplayName("Test tempo di fermata (sosta)")
    void testGetTempoFermata() {
        // stopTime1: arrivo 8:30, partenza 8:32 = 2 minuti = 120 secondi
        long tempoFermata = stopTime1.getTempoFermata();
        assertEquals(120, tempoFermata);
    }

    @Test
    @DisplayName("Test tempo di fermata zero")
    void testGetTempoFermataZero() {
        // stopTime3: arrivo e partenza uguali = 0 secondi
        long tempoFermata = stopTime3.getTempoFermata();
        assertEquals(0, tempoFermata);
    }

    @Test
    @DisplayName("Test isPrimoStop")
    void testIsPrimoStop() {
        assertTrue(stopTime1.isPrimoStop());
        assertFalse(stopTime2.isPrimoStop());
    }

    @Test
    @DisplayName("Test isDropoffPermesso")
    void testIsDropoffPermesso() {
        assertTrue(stopTime1.isDropoffPermesso());
        assertTrue(stopTime2.isDropoffPermesso());
    }

    @Test
    @DisplayName("Test isPickupPermesso")
    void testIsPickupPermesso() {
        assertTrue(stopTime1.isPickupPermesso());
        assertTrue(stopTime2.isPickupPermesso());
        assertFalse(stopTime3.isPickupPermesso());  // no pickup
    }

    @Test
    @DisplayName("Test getPickupTypeDescrizione")
    void testGetPickupTypeDescrizione() {
        assertEquals("Regolare", stopTime1.getPickupTypeDescrizione());
        assertEquals("Nessun ritiro", stopTime3.getPickupTypeDescrizione());
    }

    @Test
    @DisplayName("Test getDropOffTypeDescrizione")
    void testGetDropOffTypeDescrizione() {
        assertEquals("Regolare", stopTime1.getDropOffTypeDescrizione());
    }

    @Test
    @DisplayName("Test parseTempoGTFS formato standard")
    void testParseTempoGTFSStandard() {
        LocalTime tempo = StopTime.parseTempoGTFS("14:30:00");
        assertNotNull(tempo);
        assertEquals(14, tempo.getHour());
        assertEquals(30, tempo.getMinute());
        assertEquals(0, tempo.getSecond());
    }

    @Test
    @DisplayName("Test parseTempoGTFS con ore >= 24 (corse notturne)")
    void testParseTempoGTFSNotturno() {
        LocalTime tempo = StopTime.parseTempoGTFS("25:30:00");
        assertNotNull(tempo);
        // 25:30 diventa 01:30 (25 % 24 = 1)
        assertEquals(1, tempo.getHour());
        assertEquals(30, tempo.getMinute());
    }

    @Test
    @DisplayName("Test parseTempoGTFS con formato invalido")
    void testParseTempoGTFSInvalido() {
        assertNull(StopTime.parseTempoGTFS("invalid"));
        assertNull(StopTime.parseTempoGTFS(""));
        assertNull(StopTime.parseTempoGTFS(null));
    }

    @Test
    @DisplayName("Test parsePickupDropoffType")
    void testParsePickupDropoffType() {
        assertEquals(0, StopTime.parsePickupDropoffType("0"));
        assertEquals(1, StopTime.parsePickupDropoffType("1"));
        assertEquals(2, StopTime.parsePickupDropoffType("2"));
    }

    @Test
    @DisplayName("Test parsePickupDropoffType con formato invalido")
    void testParsePickupDropoffTypeInvalido() {
        assertEquals(0, StopTime.parsePickupDropoffType("invalid"));
        assertEquals(0, StopTime.parsePickupDropoffType(""));
    }

    @Test
    @DisplayName("Test setters")
    void testSetters() {
        stopTime1.setTripId("new_trip");
        stopTime1.setStopId("new_stop");
        stopTime1.setStopSequence(99);

        assertEquals("new_trip", stopTime1.getTripId());
        assertEquals("new_stop", stopTime1.getStopId());
        assertEquals(99, stopTime1.getStopSequence());
    }

    @Test
    @DisplayName("Test equals con stesso tripId e sequence")
    void testEqualsStesso() {
        StopTime stopTime1Copy = new StopTime(
                "trip_001",
                LocalTime.of(10, 0),
                LocalTime.of(10, 0),
                "99999",
                1
        );
        assertEquals(stopTime1, stopTime1Copy);
    }

    @Test
    @DisplayName("Test equals con tripId diverso")
    void testEqualsDiverso() {
        assertNotEquals(stopTime1, stopTime3);
    }

    @Test
    @DisplayName("Test hashCode")
    void testHashCode() {
        StopTime stopTime1Copy = new StopTime(
                "trip_001",
                LocalTime.of(10, 0),
                LocalTime.of(10, 0),
                "99999",
                1
        );
        assertEquals(stopTime1.hashCode(), stopTime1Copy.hashCode());
    }

    @Test
    @DisplayName("Test toString")
    void testToString() {
        String str = stopTime1.toString();
        assertTrue(str.contains("trip_001"));
        assertTrue(str.contains("70001"));
        assertTrue(str.contains("08:30"));
    }
}
