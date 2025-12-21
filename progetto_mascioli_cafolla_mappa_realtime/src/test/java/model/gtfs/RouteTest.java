package model.gtfs;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test della classe Route")
class RouteTest {

    private Route bus64;
    private Route metroA;
    private Route tram19;

    @BeforeEach
    void setUp() {
        // Route di test
        bus64 = new Route(
                "100", "ATAC", "64", "Stazione Termini - Casilina",
                "Bus urbano", 3, "http://atac.roma.it", "FF0000", "FFFFFF"
        );

        metroA = new Route(
                "101", "ATAC", "MA", "Metro Linea A",
                "Metropolitana", 1, "", "E74C3C", "FFFFFF"
        );

        tram19 = new Route(
                "102", "ATAC", "19", "Piazza Risorgimento - Gerani",
                "Tram", 0, "", "FFD700", "000000"
        );
    }

    @Test
    @DisplayName("Test costruttore completo")
    void testCostruttoreCompleto() {
        assertNotNull(bus64);
        assertEquals("100", bus64.getRouteId());
        assertEquals("64", bus64.getRouteShortName());
        assertEquals("Stazione Termini - Casilina", bus64.getRouteLongName());
        assertEquals(3, bus64.getRouteType());
    }

    @Test
    @DisplayName("Test tipo rotta Bus (tipo 3)")
    void testTipoRottaBus() {
        assertEquals("Bus", bus64.getTipoRottaDescrizione());
    }

    @Test
    @DisplayName("Test tipo rotta Metro (tipo 1)")
    void testTipoRottaMetro() {
        assertEquals("Metro", metroA.getTipoRottaDescrizione());
    }

    @Test
    @DisplayName("Test tipo rotta Tram (tipo 0)")
    void testTipoRottaTram() {
        assertEquals("Tram", tram19.getTipoRottaDescrizione());
    }

    @Test
    @DisplayName("Test getter RouteShortName")
    void testGetRouteShortName() {
        assertEquals("64", bus64.getRouteShortName());
        assertEquals("MA", metroA.getRouteShortName());
    }

    @Test
    @DisplayName("Test equals con stesso ID")
    void testEqualsStessoId() {
        Route bus64_copy = new Route(
                "100", "ALTRO", "999", "Nome diverso", "", 99, "", "", ""
        );
        assertEquals(bus64, bus64_copy);
    }

    @Test
    @DisplayName("Test equals con ID diverso")
    void testEqualsIdDiverso() {
        assertNotEquals(bus64, metroA);
    }

    @Test
    @DisplayName("Test hashCode consistente")
    void testHashCode() {
        Route bus64_copy = new Route(
                "100", "X", "X", "X", "X", 0, "", "", ""
        );
        assertEquals(bus64.hashCode(), bus64_copy.hashCode());
    }

    @Test
    @DisplayName("Test toString contiene informazioni chiave")
    void testToString() {
        String str = bus64.toString();
        assertTrue(str.contains("100"));
        assertTrue(str.contains("64"));
        assertTrue(str.contains("Bus"));
    }

    @Test
    @DisplayName("Test setShapeId e getShapeId")
    void testShapeId() {
        bus64.setShapeId("shape_001");
        assertEquals("shape_001", bus64.getShapeId());
    }

    @Test
    @DisplayName("Test setDirectionId e getDirectionId")
    void testDirectionId() {
        bus64.setDirectionId(0);
        assertEquals(0, bus64.getDirectionId());

        bus64.setDirectionId(1);
        assertEquals(1, bus64.getDirectionId());
    }

    @Test
    @DisplayName("Test tipo rotta sconosciuto")
    void testTipoRottaSconosciuto() {
        Route routeInvalida = new Route(
                "999", "ATAC", "X", "X", "X", 999, "", "", ""
        );
        assertEquals("Sconosciuto", routeInvalida.getTipoRottaDescrizione());
    }
}
