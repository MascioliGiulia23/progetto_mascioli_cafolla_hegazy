package model.gtfs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test della classe Fermate")
class FermateTest {

    private Fermate termini;
    private Fermate tiburtina;
    private Fermate colosseo;

    @BeforeEach
    void setUp() {
        // Fermate reali di Roma
        termini = new Fermate("70001", "Termini", 41.9009, 12.5021);
        tiburtina = new Fermate("70002", "Tiburtina", 41.9101, 12.5317);
        colosseo = new Fermate(
                "70003",
                "Colosseo",
                "Fermata storica",
                41.8902,
                12.4922,
                "http://example.com",
                "0",
                ""
        );
    }

    @Test
    @DisplayName("Test costruttore semplificato")
    void testCostruttoreSemplificato() {
        assertNotNull(termini);
        assertEquals("70001", termini.getStopId());
        assertEquals("Termini", termini.getStopName());
        assertEquals(41.9009, termini.getStopLat(), 0.0001);
        assertEquals(12.5021, termini.getStopLon(), 0.0001);
    }

    @Test
    @DisplayName("Test costruttore completo")
    void testCostruttoreCompleto() {
        assertNotNull(colosseo);
        assertEquals("70003", colosseo.getStopId());
        assertEquals("Colosseo", colosseo.getStopName());
        assertEquals("Fermata storica", colosseo.getStopDesc());
        assertEquals("http://example.com", colosseo.getStopUrl());
        assertEquals("0", colosseo.getLocationType());
    }

    @Test
    @DisplayName("Test calcolo distanza tra Termini e Tiburtina")
    void testCalcolaDistanzaTerminiTiburtina() {
        double distanza = termini.calcolaDistanza(tiburtina);

        // Distanza reale circa 2.5 km
        assertTrue(distanza > 2.0 && distanza < 3.5,
                "Distanza dovrebbe essere ~2.5 km, ma è " + distanza);
    }

    @Test
    @DisplayName("Test calcolo distanza in metri")
    void testCalcolaDistanzaMetri() {
        double distanzaMetri = termini.calcolaDistanzaMetri(tiburtina);

        // Distanza in metri
        assertTrue(distanzaMetri > 2000 && distanzaMetri < 3500);
    }

    @Test
    @DisplayName("Test distanza con stessa fermata (dovrebbe essere 0)")
    void testDistanzaNulla() {
        double distanza = termini.calcolaDistanza(termini);
        assertEquals(0.0, distanza, 0.001);
    }

    @Test
    @DisplayName("Test metodo equals con stesso ID")
    void testEqualsStessoId() {
        Fermate termini2 = new Fermate("70001", "Termini Copia", 41.9, 12.5);
        assertEquals(termini, termini2);
    }

    @Test
    @DisplayName("Test metodo equals con ID diverso")
    void testEqualsIdDiverso() {
        assertNotEquals(termini, tiburtina);
    }

    @Test
    @DisplayName("Test hashCode consistente")
    void testHashCode() {
        Fermate termini2 = new Fermate("70001", "Altro nome", 40.0, 10.0);
        assertEquals(termini.hashCode(), termini2.hashCode());
    }

    @Test
    @DisplayName("Test toString contiene informazioni chiave")
    void testToString() {
        String str = termini.toString();
        assertTrue(str.contains("70001"));
        assertTrue(str.contains("Termini"));
        assertTrue(str.contains("41.9009"));
    }

    @Test
    @DisplayName("Test coordinate valide (latitudine)")
    void testCoordinateValide() {
        assertTrue(termini.getStopLat() >= -90 && termini.getStopLat() <= 90);
    }

    @Test
    @DisplayName("Test coordinate valide (longitudine)")
    void testLongitudineValida() {
        assertTrue(termini.getStopLon() >= -180 && termini.getStopLon() <= 180);
    }
}
