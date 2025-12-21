package model.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test della classe GeoUtils")
class GeoUtilsTest {

    @Test
    @DisplayName("Test distanza tra Termini e Colosseo (~1.4 km)")
    void testDistanzaTerminiColosseo() {
        // Termini: 41.9009, 12.5021
        // Colosseo: 41.8902, 12.4922
        double distanza = GeoUtils.distanzaKm(41.9009, 12.5021, 41.8902, 12.4922);

        // Distanza reale circa 1.3-1.5 km
        assertTrue(distanza > 1.0 && distanza < 2.0,
                "Distanza Termini-Colosseo dovrebbe essere ~1.4 km, ma è " + distanza);
    }

    @Test
    @DisplayName("Test distanza nulla (stesso punto)")
    void testDistanzaNulla() {
        double distanza = GeoUtils.distanzaKm(41.9009, 12.5021, 41.9009, 12.5021);

        assertEquals(0.0, distanza, 0.001);
    }

    @Test
    @DisplayName("Test distanza Roma-Milano (~480 km)")
    void testDistanzaRomaMilano() {
        // Roma (Colosseo): 41.8902, 12.4922
        // Milano (Duomo): 45.4642, 9.1900
        double distanza = GeoUtils.distanzaKm(41.8902, 12.4922, 45.4642, 9.1900);

        // Distanza reale circa 480 km
        assertTrue(distanza > 450 && distanza < 520,
                "Distanza Roma-Milano dovrebbe essere ~480 km, ma è " + distanza);
    }

    @Test
    @DisplayName("Test distanza Roma-Napoli (~190 km)")
    void testDistanzaRomaNapoli() {
        // Roma: 41.9028, 12.4964
        // Napoli: 40.8518, 14.2681
        double distanza = GeoUtils.distanzaKm(41.9028, 12.4964, 40.8518, 14.2681);

        // Distanza reale circa 190 km
        assertTrue(distanza > 170 && distanza < 220,
                "Distanza Roma-Napoli dovrebbe essere ~190 km, ma è " + distanza);
    }

    @Test
    @DisplayName("Test distanza con coordinate equatore")
    void testDistanzaEquatore() {
        // Due punti sull'equatore distanti 1 grado di longitudine
        double distanza = GeoUtils.distanzaKm(0, 0, 0, 1);

        // 1 grado all'equatore ≈ 111 km
        assertTrue(distanza > 100 && distanza < 120,
                "1 grado all'equatore dovrebbe essere ~111 km, ma è " + distanza);
    }

    @Test
    @DisplayName("Test distanza con coordinate polo nord")
    void testDistanzaPoloNord() {
        // Vicino al polo nord (latitudine alta)
        double distanza = GeoUtils.distanzaKm(89, 0, 89, 1);

        // La distanza è molto piccola vicino al polo
        assertTrue(distanza < 10,
                "Distanza vicino al polo dovrebbe essere < 10 km, ma è " + distanza);
    }

    @Test
    @DisplayName("Test distanza breve (~100 metri)")
    void testDistanzaBreve() {
        // Due punti molto vicini (circa 100 metri)
        double distanza = GeoUtils.distanzaKm(41.9009, 12.5021, 41.9010, 12.5022);

        assertTrue(distanza < 0.2,
                "Distanza dovrebbe essere < 200 metri (0.2 km), ma è " + distanza + " km");
    }

    @Test
    @DisplayName("Test distanza con latitudini negative (emisfero sud)")
    void testDistanzaEmisferoSud() {
        // Sydney (-33.8688, 151.2093) → Melbourne (-37.8136, 144.9631)
        double distanza = GeoUtils.distanzaKm(-33.8688, 151.2093, -37.8136, 144.9631);

        // Distanza reale circa 700 km
        assertTrue(distanza > 650 && distanza < 800,
                "Distanza Sydney-Melbourne dovrebbe essere ~700 km, ma è " + distanza);
    }

    @Test
    @DisplayName("Test distanza attraverso meridiano 180°")
    void testDistanzaMeridiano180() {
        // Due punti vicini ma attraverso il meridiano 180°
        double distanza = GeoUtils.distanzaKm(0, 179.5, 0, -179.5);

        // Dovrebbe essere circa 111 km (1 grado all'equatore)
        assertTrue(distanza > 100 && distanza < 120);
    }

    @Test
    @DisplayName("Test distanza antipodal (massima distanza possibile)")
    void testDistanzaAntipodal() {
        // Punti agli antipodi (es. Roma e il suo punto opposto)
        double distanza = GeoUtils.distanzaKm(41.9028, 12.4964, -41.9028, -167.5036);

        // Distanza massima sulla Terra ≈ 20015 km (metà circonferenza)
        assertTrue(distanza > 18000 && distanza < 21000,
                "Distanza antipodal dovrebbe essere ~20000 km, ma è " + distanza);
    }

    @Test
    @DisplayName("Test simmetria della distanza")
    void testSimmetriaDistanza() {
        double dist1 = GeoUtils.distanzaKm(41.9009, 12.5021, 41.8902, 12.4922);
        double dist2 = GeoUtils.distanzaKm(41.8902, 12.4922, 41.9009, 12.5021);

        assertEquals(dist1, dist2, 0.001, "La distanza deve essere simmetrica");
    }

    @Test
    @DisplayName("Test coordinate valide estreme")
    void testCoordinateEstreme() {
        // Latitudine max/min: ±90, Longitudine max/min: ±180
        assertDoesNotThrow(() -> GeoUtils.distanzaKm(90, 180, -90, -180));
        assertDoesNotThrow(() -> GeoUtils.distanzaKm(0, 0, 0, 0));
    }
}
