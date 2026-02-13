package view.panels;

import model.gtfs.Route;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class TopRightPanel extends JPanel {
    private JButton favoritesButton;
    private JButton userButton;
    private JButton settingsButton;
    private JButton qualityButton;
    private String currentTheme = "Blu";

    public TopRightPanel() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new GridLayout(1, 4, 12, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 30));

        favoritesButton = createButton("Preferiti");
        add(favoritesButton);

        userButton = createButton("Profilo");
        add(userButton);

        settingsButton = createButton("Impostazioni");
        add(settingsButton);

        qualityButton = createButton("Qualità");
        add(qualityButton);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color themeColor = SettingsPanel.getThemeColor(currentTheme);
                Color lightColor = new Color(
                        Math.min(themeColor.getRed() + 150, 255),
                        Math.min(themeColor.getGreen() + 150, 255),
                        Math.min(themeColor.getBlue() + 150, 255)
                );

                if (getModel().isPressed()) {
                    g2d.setColor(SettingsPanel.getThemeColorDark(currentTheme));
                } else if (getModel().isArmed()) {
                    g2d.setColor(themeColor);
                } else {
                    g2d.setColor(lightColor);
                }

                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);

                g2d.setStroke(new BasicStroke(1.5f));
                g2d.setColor(themeColor);
                g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);

                super.paintComponent(g);
            }
        };

        button.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        button.setForeground(new Color(50, 50, 50));
        button.setBorder(null);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFocusPainted(false);

        return button;
    }

    public JButton getFavoritesButton() {
        return favoritesButton;
    }

    public JButton getUserButton() {
        return userButton;
    }

    public JButton getSettingsButton() {
        return settingsButton;
    }


    public JButton getQualityButton() {
        return qualityButton;
    }

    public void updateTheme(String theme) {
        this.currentTheme = theme;
        repaint();
    }

     //Aggiorna le rotte visualizzate

    public void aggiornaRotte(List<Route> rotte) {
        // Metodo per gestire l'aggiornamento delle rotte
        if (rotte != null && !rotte.isEmpty()) {
            System.out.println("Rotte trovate: " + rotte.size());
            for (Route r : rotte) {
                System.out.println("  - " + r.getRouteShortName() + ": " + r.getRouteLongName());
            }
        } else {
            System.out.println("Nessuna rotta trovata");
        }
    }
}
