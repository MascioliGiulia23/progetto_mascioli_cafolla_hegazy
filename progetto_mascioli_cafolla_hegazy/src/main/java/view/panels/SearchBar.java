package view.panels;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class SearchBar extends JPanel {
    private JTextField searchField;
    private JButton searchButton;
    private String currentTheme = "Blu";
    private ActionListener onSearchListener;
    private javax.swing.Timer searchTimer; // ⭐ NUOVO

    public SearchBar() {
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(12, 0));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 8));

        JLabel searchIcon = new JLabel();
        searchIcon.setFont(new Font("Arial", Font.PLAIN, 18));
        searchIcon.setForeground(new Color(100, 100, 100));
        add(searchIcon, BorderLayout.WEST);

        searchField = new JTextField("Cerca fermate, stazioni...");
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        searchField.setForeground(new Color(160, 160, 160));
        searchField.setBackground(Color.WHITE);
        searchField.setBorder(null);
        searchField.setOpaque(false);
        searchField.setCaretColor(new Color(70, 130, 180));

        // ⭐ NUOVO: Timer con debounce di 300ms
        searchTimer = new javax.swing.Timer(300, e -> {
            String testo = getSearchText();
            if (testo.length() >= 2 && onSearchListener != null) { // minimo 2 caratteri
                onSearchListener.actionPerformed(new ActionEvent(searchField,
                        ActionEvent.ACTION_PERFORMED, "search"));
            }
        });
        searchTimer.setRepeats(false); // esegui una volta sola

        // ⭐ NUOVO: DocumentListener per intercettare ogni cambiamento
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                searchTimer.restart(); // resetta il timer
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                String testo = getSearchText();
                if (testo.isEmpty()) {
                    // Se l'utente cancella tutto, pulisci i risultati
                    searchTimer.stop();
                } else {
                    searchTimer.restart();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                searchTimer.restart();
            }
        });

        searchField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (searchField.getText().equals("Cerca fermate, stazioni...")) {
                    searchField.setText("");
                    searchField.setForeground(new Color(50, 50, 50));
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (searchField.getText().isEmpty()) {
                    searchField.setText("Cerca fermate, stazioni...");
                    searchField.setForeground(new Color(160, 160, 160));
                }
            }
        });

        // ⭐ MODIFICATO: Invio esegue ricerca immediata e ferma il timer
        searchField.addActionListener(e -> {
            searchTimer.stop(); // ferma il timer
            if (onSearchListener != null) {
                onSearchListener.actionPerformed(e);
            }
        });

        add(searchField, BorderLayout.CENTER);

        searchButton = new JButton("Cerca") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color buttonColor;
                if (getModel().isPressed()) {
                    buttonColor = SettingsPanel.getThemeColorDark(currentTheme);
                } else if (getModel().isArmed()) {
                    buttonColor = SettingsPanel.getThemeColorLight(currentTheme);
                } else {
                    buttonColor = SettingsPanel.getThemeColor(currentTheme);
                }

                g2d.setColor(buttonColor);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                super.paintComponent(g);
            }
        };
        searchButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchButton.setForeground(Color.WHITE);
        searchButton.setBorder(null);
        searchButton.setOpaque(false);
        searchButton.setContentAreaFilled(false);
        searchButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        searchButton.setPreferredSize(new Dimension(80, 35));
        searchButton.setFocusPainted(false);

        // ⭐ MODIFICATO: anche il bottone ferma il timer
        searchButton.addActionListener(e -> {
            searchTimer.stop();
            if (onSearchListener != null) {
                onSearchListener.actionPerformed(e);
            }
        });

        add(searchButton, BorderLayout.EAST);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Ombra
        g2d.setColor(new Color(0, 0, 0, 20));
        g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 20, 20);

        // Sfondo con colore del tema (OPACO)
        Color themeColor = SettingsPanel.getThemeColor(currentTheme);
        Color lightColor = new Color(
                Math.min(themeColor.getRed() + 150, 255),
                Math.min(themeColor.getGreen() + 150, 255),
                Math.min(themeColor.getBlue() + 150, 255)
        );
        g2d.setColor(lightColor);
        g2d.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 20, 20);

        // Bordo
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.setColor(themeColor);
        g2d.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 20, 20);
    }

    public String getSearchText() {
        String text = searchField.getText();
        if (text.equals("Cerca fermate, stazioni...")) {
            return "";
        }
        return text.trim();
    }

    public void setOnSearchListener(ActionListener listener) {
        this.onSearchListener = listener;
    }

    public void updateTheme(String theme) {
        this.currentTheme = theme;
        searchField.setCaretColor(SettingsPanel.getThemeColor(theme));
        repaint();
    }

    public void clearSearch() {
        searchTimer.stop(); // ⭐ NUOVO: ferma timer quando pulisci
        searchField.setText("Cerca fermate, stazioni...");
        searchField.setForeground(new Color(160, 160, 160));
    }

    // ==================================================
    // AGGIUNTE (serve per i test)
    // ==================================================

    JTextField getSearchFieldForTest() { // serve per i test
        return searchField; // serve per i test
    }

    JButton getSearchButtonForTest() { // serve per i test
        return searchButton; // serve per i test
    }

    boolean isSearchTimerRunningForTest() { // serve per i test
        return searchTimer != null && searchTimer.isRunning(); // serve per i test
    }

    void triggerDebouncedSearchForTest() { // serve per i test
        // Simula lo “scatto” del debounce senza aspettare 300ms
        String testo = getSearchText(); // serve per i test
        if (testo.length() >= 2 && onSearchListener != null) { // serve per i test
            onSearchListener.actionPerformed(new ActionEvent(searchField, ActionEvent.ACTION_PERFORMED, "search")); // serve per i test
        }
    }
}
