package view.panels.search;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

import view.panels.FavoritesPanel;

/**
 * Utility per aggiornare il pannello "I Miei Preferiti" nel frame principale.
 * Spostata qui per evitare duplicazioni nei pannelli/ builder.
 */
public final class FavoritesSupport {

    private FavoritesSupport() {}

    /**
     * Aggiorna il pannello FavoritesPanel (se presente) all'interno del JFrame che contiene "componentInWindow".
     *
     * @param componentInWindow un qualunque componente presente nel frame (es. SearchResultsPanel)
     * @param username username corrente
     */
    public static void refreshFavoritesPanel(Component componentInWindow, String username) {
        if (username == null || username.isEmpty()) return;

        SwingUtilities.invokeLater(() -> {
            Window window = SwingUtilities.getWindowAncestor(componentInWindow);
            if (!(window instanceof JFrame frame)) return;

            Container content = frame.getContentPane();
            for (Component comp : content.getComponents()) {
                if (comp instanceof JLayeredPane lp) {
                    for (Component sub : lp.getComponents()) {
                        if (sub instanceof FavoritesPanel favPanel) {
                            favPanel.caricaPreferiti();
                            System.out.println(" Preferiti aggiornati per " + username);
                            return;
                        }
                    }
                }
            }
        });
    }

}