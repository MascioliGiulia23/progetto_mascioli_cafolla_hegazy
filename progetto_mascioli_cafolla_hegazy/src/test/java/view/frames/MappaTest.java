package view.frames;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;

import java.awt.GraphicsEnvironment;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test della classe Mappa (test mode)")
class MappaTest {

    @Test
    @DisplayName("showOnly - rende visibile solo il pannello richiesto")
    void testShowOnlyVisibility() {
        // Se l'ambiente è headless (CI/alcuni PC), i test UI vanno skippati per non fallire
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());

        Mappa_Main mappa = new Mappa_Main(true);

        // inizialmente tutti invisibili (in test mode)
        assertFalse(mappa.getFavoritesPanelForTest().isVisible());
        assertFalse(mappa.getUserProfilePanelForTest().isVisible());
        assertFalse(mappa.getSettingsPanelForTest().isVisible());
        assertFalse(mappa.getQualityPanelForTest().isVisible());

        // mostra preferiti
        mappa.showOnlyForTest(mappa.getFavoritesPanelForTest());
        assertTrue(mappa.getFavoritesPanelForTest().isVisible());
        assertFalse(mappa.getUserProfilePanelForTest().isVisible());
        assertFalse(mappa.getSettingsPanelForTest().isVisible());
        assertFalse(mappa.getQualityPanelForTest().isVisible());

        // mostra settings
        mappa.showOnlyForTest(mappa.getSettingsPanelForTest());
        assertFalse(mappa.getFavoritesPanelForTest().isVisible());
        assertFalse(mappa.getUserProfilePanelForTest().isVisible());
        assertTrue(mappa.getSettingsPanelForTest().isVisible());
        assertFalse(mappa.getQualityPanelForTest().isVisible());

        // chiudi tutti
        mappa.showOnlyForTest(null);
        assertFalse(mappa.getFavoritesPanelForTest().isVisible());
        assertFalse(mappa.getUserProfilePanelForTest().isVisible());
        assertFalse(mappa.getSettingsPanelForTest().isVisible());
        assertFalse(mappa.getQualityPanelForTest().isVisible());

        mappa.dispose();
    }

    @Test
    @DisplayName("applyTheme - non lancia eccezioni in test mode")
    void testApplyThemeDoesNotThrow() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless());

        Mappa_Main mappa = new Mappa_Main(true);

        assertDoesNotThrow(() -> mappa.applyThemeForTest("QUALSIASI_TEMA"));

        mappa.dispose();
    }
}

