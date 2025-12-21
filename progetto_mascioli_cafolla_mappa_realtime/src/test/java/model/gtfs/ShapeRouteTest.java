package model.gtfs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test della classe ShapeRoute")
class ShapeRouteTest {

    private ShapeRoute shape1;
    private ShapeRoute shape2;

    @BeforeEach
    void setUp() {
        shape1 = new ShapeRoute("shape_001");

        // Percorso reale simulato: Termini → Colosseo → Circo Massimo
        shape1.aggiungiPunto(41.9009, 12.5021, 1, 0.0);      // Termini
        shape1.aggiungiPunto(41.8950, 12.5000, 2, 0.7);      // Punto intermedio
        shape1.aggiungiPunto(41.8902, 12.4922, 3, 1.4);      // Colosseo
        shape1.aggiungiPunto(41.8850, 12.4850, 4, 2.1);      // Circo Massimo

        shape2 = new ShapeRoute("shape_002");
    }

    @Test
    @DisplayName("Test costruttore")
    void testCostruttore() {
        assertNotNull(shape1);
        assertEquals("shape_001", shape1.getShapeId());
        assertNotNull(shape1.getGeoPoints());
    }

    @Test
    @DisplayName("Test aggiungiPunto con oggetto GeoPoint")
    void testAggiungiPuntoConOggetto() {
        ShapeRoute.GeoPoint punto = new ShapeRoute.GeoPoint(41.9, 12.5, 1, 0.0);
        shape2.aggiungiPunto(punto);

        assertEquals(1, shape2.getNumPunti());
    }

    @Test
    @DisplayName("Test aggiungiPunto con parametri completi")
    void testAggiungiPuntoConParametriCompleti() {
        shape2.aggiungiPunto(41.9, 12.5, 1, 1.5);

        assertEquals(1, shape2.getNumPunti());
        assertEquals(41.9, shape2.getGeoPoints().get(0).getLatitude(), 0.0001);
        assertEquals(1.5, shape2.getGeoPoints().get(0).getDistanceTraveled(), 0.001);
    }

    @Test
    @DisplayName("Test aggiungiPunto con parametri senza distanza")
    void testAggiungiPuntoSenzaDistanza() {
        shape2.aggiungiPunto(41.9, 12.5, 1);

        assertEquals(1, shape2.getNumPunti());
        assertEquals(0.0, shape2.getGeoPoints().get(0).getDistanceTraveled(), 0.001);
    }

    @Test
    @DisplayName("Test getNumPunti")
    void testGetNumPunti() {
        assertEquals(4, shape1.getNumPunti());
        assertEquals(0, shape2.getNumPunti());
    }

    @Test
    @DisplayName("Test rimuoviPunto")
    void testRimuoviPunto() {
        int numPuntiIniziali = shape1.getNumPunti();
        shape1.rimuoviPunto(0);

        assertEquals(numPuntiIniziali - 1, shape1.getNumPunti());
    }

    @Test
    @DisplayName("Test rimuoviPunto con indice invalido")
    void testRimuoviPuntoIndiceInvalido() {
        int numPuntiIniziali = shape1.getNumPunti();
        shape1.rimuoviPunto(-1);
        shape1.rimuoviPunto(999);

        // Nessun punto dovrebbe essere rimosso
        assertEquals(numPuntiIniziali, shape1.getNumPunti());
    }

    @Test
    @DisplayName("Test ordinaPunti")
    void testOrdinaPunti() {
        ShapeRoute shapeDisordinata = new ShapeRoute("shape_003");
        shapeDisordinata.aggiungiPunto(41.9, 12.5, 3);
        shapeDisordinata.aggiungiPunto(41.8, 12.4, 1);
        shapeDisordinata.aggiungiPunto(41.85, 12.45, 2);

        shapeDisordinata.ordinaPunti();

        assertEquals(1, shapeDisordinata.getGeoPoints().get(0).getSequence());
        assertEquals(2, shapeDisordinata.getGeoPoints().get(1).getSequence());
        assertEquals(3, shapeDisordinata.getGeoPoints().get(2).getSequence());
    }

    @Test
    @DisplayName("Test calcolaLunghezzaRotta")
    void testCalcolaLunghezzaRotta() {
        double lunghezza = shape1.calcolaLunghezzaRotta();

        // Lunghezza approssimativa dovrebbe essere > 0
        assertTrue(lunghezza > 0);

        // La distanza reale Termini-Circo Massimo è circa 2-3 km
        assertTrue(lunghezza > 1.5 && lunghezza < 4.0,
                "Lunghezza dovrebbe essere ~2-3 km, ma è " + lunghezza);
    }

    @Test
    @DisplayName("Test calcolaLunghezzaRotta con shape vuota")
    void testCalcolaLunghezzaRottaVuota() {
        assertEquals(0.0, shape2.calcolaLunghezzaRotta(), 0.001);
    }

    @Test
    @DisplayName("Test calcolaLunghezzaRotta con un solo punto")
    void testCalcolaLunghezzaRottaUnPunto() {
        shape2.aggiungiPunto(41.9, 12.5, 1);
        assertEquals(0.0, shape2.calcolaLunghezzaRotta(), 0.001);
    }

    @Test
    @DisplayName("Test trovaPuntoPiuVicino")
    void testTrovaPuntoPiuVicino() {
        // Cerchiamo il punto vicino al Colosseo (41.8902, 12.4922)
        Map.Entry<ShapeRoute.GeoPoint, Double> risultato =
                shape1.trovaPuntoPiuVicino(41.8902, 12.4922);

        assertNotNull(risultato);
        assertNotNull(risultato.getKey());

        // Il punto più vicino dovrebbe essere quello del Colosseo (sequence 3)
        assertEquals(3, risultato.getKey().getSequence());

        // La distanza dovrebbe essere molto piccola (quasi 0 metri)
        assertTrue(risultato.getValue() < 10,
                "Distanza dovrebbe essere < 10 metri, ma è " + risultato.getValue());
    }

    @Test
    @DisplayName("Test trovaPuntoPiuVicino con shape vuota")
    void testTrovaPuntoPiuVicinoVuota() {
        assertNull(shape2.trovaPuntoPiuVicino(41.9, 12.5));
    }

    @Test
    @DisplayName("Test trovaPuntoPiuVicino con punto lontano")
    void testTrovaPuntoPiuVicinoLontano() {
        // Cerchiamo da Milano (45.4642, 9.1900)
        Map.Entry<ShapeRoute.GeoPoint, Double> risultato =
                shape1.trovaPuntoPiuVicino(45.4642, 9.1900);

        assertNotNull(risultato);

        // La distanza dovrebbe essere > 400 km (400.000 metri)
        assertTrue(risultato.getValue() > 400000,
                "Distanza Roma-Milano dovrebbe essere > 400 km");
    }

    @Test
    @DisplayName("Test GeoPoint costruttore completo")
    void testGeoPointCostruttoreCompleto() {
        ShapeRoute.GeoPoint punto = new ShapeRoute.GeoPoint(41.9, 12.5, 1, 1.5);

        assertEquals(41.9, punto.getLatitude(), 0.0001);
        assertEquals(12.5, punto.getLongitude(), 0.0001);
        assertEquals(1, punto.getSequence());
        assertEquals(1.5, punto.getDistanceTraveled(), 0.001);
    }

    @Test
    @DisplayName("Test GeoPoint costruttore senza distanza")
    void testGeoPointCostruttoreSemplice() {
        ShapeRoute.GeoPoint punto = new ShapeRoute.GeoPoint(41.9, 12.5, 1);

        assertEquals(0.0, punto.getDistanceTraveled(), 0.001);
    }

    @Test
    @DisplayName("Test GeoPoint toString")
    void testGeoPointToString() {
        ShapeRoute.GeoPoint punto = new ShapeRoute.GeoPoint(41.9, 12.5, 1);
        String str = punto.toString();

        assertTrue(str.contains("41.9"));
        assertTrue(str.contains("12.5"));
        assertTrue(str.contains("seq=1"));
    }

    @Test
    @DisplayName("Test setShapeId")
    void testSetShapeId() {
        shape1.setShapeId("new_shape_id");
        assertEquals("new_shape_id", shape1.getShapeId());
    }

    @Test
    @DisplayName("Test getGeoPoints ritorna lista non null")
    void testGetGeoPointsNonNull() {
        assertNotNull(shape1.getGeoPoints());
        assertNotNull(shape2.getGeoPoints());
    }

    @Test
    @DisplayName("Test toString")
    void testToString() {
        String str = shape1.toString();

        assertTrue(str.contains("shape_001"));
        assertTrue(str.contains("numPunti=4"));
        assertTrue(str.contains("lunghezzaKm"));
    }

    @Test
    @DisplayName("Test equals con stesso shapeId")
    void testEqualsStessoId() {
        ShapeRoute shape1Copy = new ShapeRoute("shape_001");
        assertEquals(shape1, shape1Copy);
    }

    @Test
    @DisplayName("Test equals con shapeId diverso")
    void testEqualsIdDiverso() {
        assertNotEquals(shape1, shape2);
    }

    @Test
    @DisplayName("Test hashCode consistente")
    void testHashCode() {
        ShapeRoute shape1Copy = new ShapeRoute("shape_001");
        assertEquals(shape1.hashCode(), shape1Copy.hashCode());
    }
}
