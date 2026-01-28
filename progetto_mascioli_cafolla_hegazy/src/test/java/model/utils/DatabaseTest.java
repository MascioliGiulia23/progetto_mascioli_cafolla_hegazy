package model.utils;

import model.gtfs.Fermate;
import model.gtfs.Route;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test della classe Database")
class DatabaseTest {

    private List<Fermate> fermate;
    private List<Route> rotte;

    @BeforeEach
    void setUp() {
        // Crea dati di test
        fermate = new ArrayList<>();
        fermate.add(new Fermate("70001", "Termini", 41.9009, 12.5021));
        fermate.add(new Fermate("70002", "Tiburtina", 41.9101, 12.5317));
        fermate.add(new Fermate("70003", "Colosseo", 41.8902, 12.4922));
        fermate.add(new Fermate("70004", "Piazza Venezia", 41.8960, 12.4833));
        fermate.add(new Fermate("70005", "Termini/Giolitti", 41.9010, 12.5025));

        rotte = new ArrayList<>();
        rotte.add(new Route("100", "ATAC", "64", "Stazione Termini - Casilina", "", 3, "", "", ""));
        rotte.add(new Route("101", "ATAC", "MA", "Metro Linea A", "", 1, "", "", ""));
        rotte.add(new Route("102", "ATAC", "MB", "Metro Linea B", "", 1, "", "", ""));
        rotte.add(new Route("103", "ATAC", "19", "Piazza Risorgimento - Gerani", "", 0, "", "", ""));
        rotte.add(new Route("104", "ATAC", "542", "Termini - Cinquina", "", 3, "", "", ""));
    }

    @Test
    @DisplayName("Test ricercaFermatePerNome - risultato esatto")
    void testRicercaFermatePerNomeEsatto() {
        List<Fermate> risultato = Database.ricercaFermatePerNome(fermate, "Termini");

        // Dovrebbe trovare sia "Termini" che "Termini/Giolitti"
        assertEquals(2, risultato.size());
    }

    @Test
    @DisplayName("Test ricercaFermatePerNome - case insensitive")
    void testRicercaFermatePerNomeCaseInsensitive() {
        List<Fermate> risultato1 = Database.ricercaFermatePerNome(fermate, "termini");
        List<Fermate> risultato2 = Database.ricercaFermatePerNome(fermate, "TERMINI");
        List<Fermate> risultato3 = Database.ricercaFermatePerNome(fermate, "TeRmInI");

        assertEquals(2, risultato1.size());
        assertEquals(2, risultato2.size());
        assertEquals(2, risultato3.size());
    }

    @Test
    @DisplayName("Test ricercaFermatePerNome - ricerca parziale")
    void testRicercaFermatePerNomeParziale() {
        List<Fermate> risultato = Database.ricercaFermatePerNome(fermate, "ti");

        // Trova almeno "Termini" e "Termini/Giolitti"
        assertTrue(risultato.size() >= 2, "Dovrebbe trovare almeno 2 fermate con 'ti'");
    }

    @Test
    @DisplayName("Test ricercaFermatePerNome - nessun risultato")
    void testRicercaFermatePerNomeNessunoRisultato() {
        List<Fermate> risultato = Database.ricercaFermatePerNome(fermate, "XYZ123NonEsiste");

        assertTrue(risultato.isEmpty());
    }

    @Test
    @DisplayName("Test ricercaFermatePerNome - stringa vuota")
    void testRicercaFermatePerNomeStringaVuota() {
        List<Fermate> risultato = Database.ricercaFermatePerNome(fermate, "");

        // Stringa vuota trova tutto
        assertEquals(fermate.size(), risultato.size());
    }

    @Test
    @DisplayName("Test ricercaFermatePerNome - singolo carattere")
    void testRicercaFermatePerNomeSingoloCarattere() {
        List<Fermate> risultato = Database.ricercaFermatePerNome(fermate, "C");

        // Trova "Colosseo"
        assertTrue(risultato.size() >= 1);
    }

    @Test
    @DisplayName("Test ricercaRottePerNome - nome breve esatto")
    void testRicercaRottePerNomeBreveEsatto() {
        List<Route> risultato = Database.ricercaRottePerNome(rotte, "64");

        assertEquals(1, risultato.size());
        assertEquals("64", risultato.get(0).getRouteShortName());
    }

    @Test
    @DisplayName("Test ricercaRottePerNome - nome lungo")
    void testRicercaRottePerNomeLungo() {
        List<Route> risultato = Database.ricercaRottePerNome(rotte, "Metro");

        // Trova "Metro Linea A" e "Metro Linea B"
        assertEquals(2, risultato.size());
    }

    @Test
    @DisplayName("Test ricercaRottePerNome - case insensitive")
    void testRicercaRottePerNomeCaseInsensitive() {
        List<Route> risultato1 = Database.ricercaRottePerNome(rotte, "ma");
        List<Route> risultato2 = Database.ricercaRottePerNome(rotte, "MA");
        List<Route> risultato3 = Database.ricercaRottePerNome(rotte, "Ma");

        assertEquals(1, risultato1.size());
        assertEquals(1, risultato2.size());
        assertEquals(1, risultato3.size());
    }

    @Test
    @DisplayName("Test ricercaRottePerNome - nessun risultato")
    void testRicercaRottePerNomeNessunoRisultato() {
        List<Route> risultato = Database.ricercaRottePerNome(rotte, "ZZZ999");

        assertTrue(risultato.isEmpty());
    }

    @Test
    @DisplayName("Test ricercaRottePerNome - ricerca parziale")
    void testRicercaRottePerNomeParziale() {
        List<Route> risultato = Database.ricercaRottePerNome(rotte, "Termini");

        // Trova "Stazione Termini - Casilina" e "Termini - Cinquina"
        assertEquals(2, risultato.size());
    }

    @Test
    @DisplayName("Test ricercaRotteMetroTram - metro e tram")
    void testRicercaRotteMetroTramSoloMetro() {
        List<Route> risultato = Database.ricercaRotteMetroTram(rotte, "M");

        // Trova le linee metro/tram che contengono "M"
        assertTrue(risultato.size() >= 2, "Dovrebbe trovare almeno 2 linee");

        // Verifica che tutte siano metro (1) o tram (0)
        for (Route r : risultato) {
            int tipo = r.getRouteType();
            assertTrue(tipo == 0 || tipo == 1,
                    "Route " + r.getRouteShortName() + " dovrebbe essere metro/tram, ma è tipo " + tipo);
        }
    }



    @Test
    @DisplayName("Test ricercaRotteMetroTram - solo tram")
    void testRicercaRotteMetroTramSoloTram() {
        List<Route> risultato = Database.ricercaRotteMetroTram(rotte, "19");

        assertEquals(1, risultato.size());
        assertEquals(0, risultato.get(0).getRouteType(), "Dovrebbe essere tram (tipo 0)");
    }

    @Test
    @DisplayName("Test ricercaRotteMetroTram - esclude bus")
    void testRicercaRotteMetroTramEscludeBus() {
        List<Route> risultato = Database.ricercaRotteMetroTram(rotte, "64");

        // La linea 64 è un bus (tipo 3), non deve essere trovata
        assertTrue(risultato.isEmpty());
    }

    @Test
    @DisplayName("Test ricercaRotteMetroTram - stringa vuota")
    void testRicercaRotteMetroTramStringaVuota() {
        List<Route> risultato = Database.ricercaRotteMetroTram(rotte, "");

        assertTrue(risultato.isEmpty());
    }

    @Test
    @DisplayName("Test ricercaRotteMetroTram - null")
    void testRicercaRotteMetroTramNull() {
        List<Route> risultato = Database.ricercaRotteMetroTram(rotte, null);

        assertTrue(risultato.isEmpty());
    }

    @Test
    @DisplayName("Test ricercaRotteMetroTram - case insensitive")
    void testRicercaRotteMetroTramCaseInsensitive() {
        List<Route> risultato1 = Database.ricercaRotteMetroTram(rotte, "ma");
        List<Route> risultato2 = Database.ricercaRotteMetroTram(rotte, "MA");

        assertEquals(1, risultato1.size());
        assertEquals(1, risultato2.size());
    }

    @Test
    @DisplayName("Test leggiStopDaFile non lancia eccezioni")
    void testLeggiStopDaFile() {
        // Test che il metodo non lanci eccezioni
        assertDoesNotThrow(() -> {
            List<Fermate> result = Database.leggiStopDaFile();
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("Test leggiRouteDaFile non lancia eccezioni")
    void testLeggiRouteDaFile() {
        assertDoesNotThrow(() -> {
            List<Route> result = Database.leggiRouteDaFile();
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("Test leggiShapeDaFile non lancia eccezioni")
    void testLeggiShapeDaFile() {
        assertDoesNotThrow(() -> {
            var result = Database.leggiShapeDaFile();
            assertNotNull(result);
        });
    }

    @Test
    @DisplayName("Test leggiTripsDaFile non lancia eccezioni")
    void testLeggiTripsDaFile() {
        assertDoesNotThrow(() -> {
            var result = Database.leggiTripsDaFile();
            assertNotNull(result);
        });
    }

}
