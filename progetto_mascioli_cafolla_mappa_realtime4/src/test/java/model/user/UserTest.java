package model.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test della classe User")
class UserTest {

    private User user;
    private Favorite fermataTermini;
    private Favorite linea64;
    private Favorite metroA;

    @BeforeEach
    void setUp() {
        user = new User("testuser", "password123");
        fermataTermini = new Favorite("Termini", "FERMATA");
        linea64 = new Favorite("64", "LINEA");
        metroA = new Favorite("MA", "LINEA");
    }

    @Test
    @DisplayName("Test costruttore")
    void testCostruttore() {
        assertNotNull(user);
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertNotNull(user.getPreferiti());
        assertTrue(user.getPreferiti().isEmpty());
    }

    @Test
    @DisplayName("Test getUsername")
    void testGetUsername() {
        assertEquals("testuser", user.getUsername());
    }

    @Test
    @DisplayName("Test getPassword")
    void testGetPassword() {
        assertEquals("password123", user.getPassword());
    }

    @Test
    @DisplayName("Test verificaPassword corretta")
    void testVerificaPasswordCorretta() {
        assertTrue(user.verificaPassword("password123"));
    }

    @Test
    @DisplayName("Test verificaPassword errata")
    void testVerificaPasswordErrata() {
        assertFalse(user.verificaPassword("wrongpassword"));
        assertFalse(user.verificaPassword(""));
        assertFalse(user.verificaPassword("Password123"));  // case sensitive
    }

    @Test
    @DisplayName("Test aggiungiPreferito nuovo")
    void testAggiungiPreferitoNuovo() {
        boolean result = user.aggiungiPreferito(fermataTermini);

        assertTrue(result);
        assertEquals(1, user.getPreferiti().size());
        assertTrue(user.haPreferito(fermataTermini));
    }

    @Test
    @DisplayName("Test aggiungiPreferito duplicato")
    void testAggiungiPreferitoDuplicato() {
        user.aggiungiPreferito(fermataTermini);
        boolean result = user.aggiungiPreferito(fermataTermini);

        assertFalse(result);
        assertEquals(1, user.getPreferiti().size());
    }

    @Test
    @DisplayName("Test aggiungiPreferito multipli")
    void testAggiungiPreferitiMultipli() {
        user.aggiungiPreferito(fermataTermini);
        user.aggiungiPreferito(linea64);
        user.aggiungiPreferito(metroA);

        assertEquals(3, user.getPreferiti().size());
    }

    @Test
    @DisplayName("Test rimuoviPreferito esistente")
    void testRimuoviPreferitoEsistente() {
        user.aggiungiPreferito(fermataTermini);
        boolean result = user.rimuoviPreferito(fermataTermini);

        assertTrue(result);
        assertEquals(0, user.getPreferiti().size());
        assertFalse(user.haPreferito(fermataTermini));
    }

    @Test
    @DisplayName("Test rimuoviPreferito non esistente")
    void testRimuoviPreferitoNonEsistente() {
        boolean result = user.rimuoviPreferito(fermataTermini);

        assertFalse(result);
        assertEquals(0, user.getPreferiti().size());
    }

    @Test
    @DisplayName("Test haPreferito esistente")
    void testHaPreferitoEsistente() {
        user.aggiungiPreferito(fermataTermini);

        assertTrue(user.haPreferito(fermataTermini));
    }

    @Test
    @DisplayName("Test haPreferito non esistente")
    void testHaPreferitoNonEsistente() {
        assertFalse(user.haPreferito(fermataTermini));
    }

    @Test
    @DisplayName("Test getPreferiti ritorna copia")
    void testGetPreferitiRitornaCopia() {
        user.aggiungiPreferito(fermataTermini);
        List<Favorite> preferiti = user.getPreferiti();

        // Modifica la lista ottenuta
        preferiti.add(linea64);

        // La lista interna non deve essere modificata
        assertEquals(1, user.getPreferiti().size());
    }

    @Test
    @DisplayName("Test getPreferiti lista vuota")
    void testGetPreferitiVuota() {
        List<Favorite> preferiti = user.getPreferiti();

        assertNotNull(preferiti);
        assertTrue(preferiti.isEmpty());
    }

    @Test
    @DisplayName("Test getPreferiti ordine inserimento")
    void testGetPreferitiOrdine() {
        user.aggiungiPreferito(fermataTermini);
        user.aggiungiPreferito(linea64);
        user.aggiungiPreferito(metroA);

        List<Favorite> preferiti = user.getPreferiti();

        assertEquals(fermataTermini, preferiti.get(0));
        assertEquals(linea64, preferiti.get(1));
        assertEquals(metroA, preferiti.get(2));
    }

    @Test
    @DisplayName("Test toString contiene username")
    void testToString() {
        String str = user.toString();

        assertTrue(str.contains("testuser"));
        assertTrue(str.contains("preferiti=0"));
    }

    @Test
    @DisplayName("Test toString con preferiti")
    void testToStringConPreferiti() {
        user.aggiungiPreferito(fermataTermini);
        user.aggiungiPreferito(linea64);

        String str = user.toString();
        assertTrue(str.contains("preferiti=2"));
    }

    @Test
    @DisplayName("Test Serializable")
    void testSerializable() {
        assertTrue(user instanceof java.io.Serializable);
    }
}
