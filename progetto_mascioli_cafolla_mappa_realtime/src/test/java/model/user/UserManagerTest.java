package model.user;

import org.junit.jupiter.api.*;
import java.io.File;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test della classe UserManager")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserManagerTest {

    private static final String TEST_USERNAME = "testuser_unittest";
    private static final String TEST_PASSWORD = "testpass123";
    private Favorite fermataTest;
    private Favorite lineaTest;

    @BeforeEach
    void setUp() {
        // Carica utenti prima di ogni test
        UserManager.caricaUtenti();

        // Rimuovi utente di test se esiste (per partire da stato pulito)
        if (UserManager.usernameEsiste(TEST_USERNAME)) {
            // Non c'è metodo eliminaUtente, quindi registriamo solo se non esiste
        }

        fermataTest = new Favorite("Termini Test", "FERMATA");
        lineaTest = new Favorite("64 Test", "LINEA");
    }

    @Test
    @Order(1)
    @DisplayName("Test caricaUtenti non lancia eccezioni")
    void testCaricaUtenti() {
        assertDoesNotThrow(() -> UserManager.caricaUtenti());
    }

    @Test
    @Order(2)
    @DisplayName("Test registraUtente nuovo utente")
    void testRegistraUtenteNuovo() {
        String username = "newuser_" + System.currentTimeMillis();
        boolean result = UserManager.registraUtente(username, "password");

        assertTrue(result);
        assertTrue(UserManager.usernameEsiste(username));
    }

    @Test
    @Order(3)
    @DisplayName("Test registraUtente username duplicato")
    void testRegistraUtenteDuplicato() {
        String username = "dupuser_" + System.currentTimeMillis();
        UserManager.registraUtente(username, "password1");
        boolean result = UserManager.registraUtente(username, "password2");

        assertFalse(result);
    }

    @Test
    @Order(4)
    @DisplayName("Test registraUtente username vuoto")
    void testRegistraUtenteUsernameVuoto() {
        assertFalse(UserManager.registraUtente("", "password"));
        assertFalse(UserManager.registraUtente(null, "password"));
        assertFalse(UserManager.registraUtente("   ", "password"));
    }

    @Test
    @Order(5)
    @DisplayName("Test registraUtente password vuota")
    void testRegistraUtentePasswordVuota() {
        String username = "user_" + System.currentTimeMillis();
        assertFalse(UserManager.registraUtente(username, ""));
        assertFalse(UserManager.registraUtente(username, null));
    }

    @Test
    @Order(6)
    @DisplayName("Test login con credenziali corrette")
    void testLoginCorretto() {
        String username = "loginuser_" + System.currentTimeMillis();
        UserManager.registraUtente(username, "password123");

        assertTrue(UserManager.login(username, "password123"));
    }

    @Test
    @Order(7)
    @DisplayName("Test login con password errata")
    void testLoginPasswordErrata() {
        String username = "loginuser2_" + System.currentTimeMillis();
        UserManager.registraUtente(username, "password123");

        assertFalse(UserManager.login(username, "wrongpassword"));
    }

    @Test
    @Order(8)
    @DisplayName("Test login con username inesistente")
    void testLoginUsernameInesistente() {
        assertFalse(UserManager.login("user_nonexistent_" + System.currentTimeMillis(), "password"));
    }

    @Test
    @Order(9)
    @DisplayName("Test usernameEsiste")
    void testUsernameEsiste() {
        String username = "existuser_" + System.currentTimeMillis();
        assertFalse(UserManager.usernameEsiste(username));

        UserManager.registraUtente(username, "password");
        assertTrue(UserManager.usernameEsiste(username));
    }

    @Test
    @Order(10)
    @DisplayName("Test ottieniUtente esistente")
    void testOttieniUtenteEsistente() {
        String username = "getuser_" + System.currentTimeMillis();
        UserManager.registraUtente(username, "password");

        User user = UserManager.ottieniUtente(username);
        assertNotNull(user);
        assertEquals(username, user.getUsername());
    }

    @Test
    @Order(11)
    @DisplayName("Test ottieniUtente inesistente")
    void testOttieniUtenteInesistente() {
        User user = UserManager.ottieniUtente("user_nonexistent_" + System.currentTimeMillis());
        assertNull(user);
    }

    @Test
    @Order(12)
    @DisplayName("Test getNumeroUtenti")
    void testGetNumeroUtenti() {
        int numeroIniziale = UserManager.getNumeroUtenti();
        assertTrue(numeroIniziale >= 0);

        String username = "countuser_" + System.currentTimeMillis();
        UserManager.registraUtente(username, "password");

        assertEquals(numeroIniziale + 1, UserManager.getNumeroUtenti());
    }

    @Test
    @Order(13)
    @DisplayName("Test aggiungiPreferito")
    void testAggiungiPreferito() {
        String username = "favuser_" + System.currentTimeMillis();
        UserManager.registraUtente(username, "password");

        boolean result = UserManager.aggiungiPreferito(username, fermataTest);

        assertTrue(result);
        assertTrue(UserManager.haPreferito(username, fermataTest));
    }

    @Test
    @Order(14)
    @DisplayName("Test aggiungiPreferito utente inesistente")
    void testAggiungiPreferitoUtenteInesistente() {
        boolean result = UserManager.aggiungiPreferito("user_nonexistent", fermataTest);
        assertFalse(result);
    }

    @Test
    @Order(15)
    @DisplayName("Test rimuoviPreferito")
    void testRimuoviPreferito() {
        String username = "remfavuser_" + System.currentTimeMillis();
        UserManager.registraUtente(username, "password");
        UserManager.aggiungiPreferito(username, fermataTest);

        boolean result = UserManager.rimuoviPreferito(username, fermataTest);

        assertTrue(result);
        assertFalse(UserManager.haPreferito(username, fermataTest));
    }

    @Test
    @Order(16)
    @DisplayName("Test rimuoviPreferito non esistente")
    void testRimuoviPreferitoNonEsistente() {
        String username = "remfavuser2_" + System.currentTimeMillis();
        UserManager.registraUtente(username, "password");

        boolean result = UserManager.rimuoviPreferito(username, fermataTest);
        assertFalse(result);
    }

    @Test
    @Order(17)
    @DisplayName("Test ottieniPreferiti")
    void testOttieniPreferiti() {
        String username = "getfavuser_" + System.currentTimeMillis();
        UserManager.registraUtente(username, "password");
        UserManager.aggiungiPreferito(username, fermataTest);
        UserManager.aggiungiPreferito(username, lineaTest);

        List<Favorite> preferiti = UserManager.ottieniPreferiti(username);

        assertNotNull(preferiti);
        assertEquals(2, preferiti.size());
    }

    @Test
    @Order(18)
    @DisplayName("Test ottieniPreferiti utente inesistente")
    void testOttieniPreferitiUtenteInesistente() {
        List<Favorite> preferiti = UserManager.ottieniPreferiti("user_nonexistent");

        assertNotNull(preferiti);
        assertTrue(preferiti.isEmpty());
    }

    @Test
    @Order(19)
    @DisplayName("Test haPreferito")
    void testHaPreferito() {
        String username = "hasfavuser_" + System.currentTimeMillis();
        UserManager.registraUtente(username, "password");

        assertFalse(UserManager.haPreferito(username, fermataTest));

        UserManager.aggiungiPreferito(username, fermataTest);
        assertTrue(UserManager.haPreferito(username, fermataTest));
    }

    @Test
    @Order(20)
    @DisplayName("Test getNumeroPreferiti")
    void testGetNumeroPreferiti() {
        String username = "countfavuser_" + System.currentTimeMillis();
        UserManager.registraUtente(username, "password");

        assertEquals(0, UserManager.getNumeroPreferiti(username));

        UserManager.aggiungiPreferito(username, fermataTest);
        assertEquals(1, UserManager.getNumeroPreferiti(username));

        UserManager.aggiungiPreferito(username, lineaTest);
        assertEquals(2, UserManager.getNumeroPreferiti(username));
    }

    @Test
    @Order(21)
    @DisplayName("Test salvaUtenti non lancia eccezioni")
    void testSalvaUtenti() {
        assertDoesNotThrow(() -> UserManager.salvaUtenti());
    }
}
