package model.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test della classe Favorite")
class FavoriteTest {

    private Favorite fermataTermini;
    private Favorite linea64;
    private Favorite fermataTermini2;

    @BeforeEach
    void setUp() {
        fermataTermini = new Favorite("Termini", "FERMATA");
        linea64 = new Favorite("64", "LINEA");
        fermataTermini2 = new Favorite("Termini", "FERMATA");
    }

    @Test
    @DisplayName("Test costruttore")
    void testCostruttore() {
        assertNotNull(fermataTermini);
        assertEquals("Termini", fermataTermini.getNome());
        assertEquals("FERMATA", fermataTermini.getTipo());
    }

    @Test
    @DisplayName("Test getNome")
    void testGetNome() {
        assertEquals("Termini", fermataTermini.getNome());
        assertEquals("64", linea64.getNome());
    }

    @Test
    @DisplayName("Test getTipo")
    void testGetTipo() {
        assertEquals("FERMATA", fermataTermini.getTipo());
        assertEquals("LINEA", linea64.getTipo());
    }

    @Test
    @DisplayName("Test equals con stesso nome e tipo")
    void testEqualsStessi() {
        assertEquals(fermataTermini, fermataTermini2);
    }

    @Test
    @DisplayName("Test equals con tipo diverso")
    void testEqualsTipoDiverso() {
        Favorite fermataConStessoNome = new Favorite("64", "FERMATA");
        assertNotEquals(linea64, fermataConStessoNome);
    }

    @Test
    @DisplayName("Test equals con nome diverso")
    void testEqualsNomeDiverso() {
        assertNotEquals(fermataTermini, linea64);
    }

    @Test
    @DisplayName("Test equals con stesso oggetto")
    void testEqualsStessoOggetto() {
        assertEquals(fermataTermini, fermataTermini);
    }

    @Test
    @DisplayName("Test equals con null")
    void testEqualsNull() {
        assertNotEquals(fermataTermini, null);
    }

    @Test
    @DisplayName("Test equals con classe diversa")
    void testEqualsClasseDiversa() {
        assertNotEquals(fermataTermini, "Stringa");
    }

    @Test
    @DisplayName("Test hashCode consistente")
    void testHashCode() {
        assertEquals(fermataTermini.hashCode(), fermataTermini2.hashCode());
    }

    @Test
    @DisplayName("Test hashCode diverso")
    void testHashCodeDiverso() {
        assertNotEquals(fermataTermini.hashCode(), linea64.hashCode());
    }

    @Test
    @DisplayName("Test toString formato corretto")
    void testToString() {
        assertEquals("FERMATA: Termini", fermataTermini.toString());
        assertEquals("LINEA: 64", linea64.toString());
    }

    @Test
    @DisplayName("Test Serializable")
    void testSerializable() {
        assertTrue(fermataTermini instanceof java.io.Serializable);
    }
}
