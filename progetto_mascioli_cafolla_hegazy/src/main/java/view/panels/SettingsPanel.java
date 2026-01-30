package view.panels;

import service.ConnectivityService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class SettingsPanel extends JPanel {
    private JComboBox<String> colorCombo;
    private JButton saveButton;
    private ActionListener onSaveListener;
    private String currentTheme = "Blu";

    // Componenti per lo status di connessione
    private JLabel statusLabel;
    private JLabel statusIcon;
    private Timer statusUpdateTimer;

    // Colori disponibili
    public static final String COLOR_BLU = "Blu";
    public static final String COLOR_GRIGIO = "Grigio";
    public static final String COLOR_VERDE = "Verde";
    public static final String COLOR_ROSA = "Viola";

    public SettingsPanel() {
        this(true); // ✅ comportamento identico: timer parte sempre
    }

    // ============================================================
    // serve per i test: possibilità di NON avviare il timer
    // ============================================================
    SettingsPanel(boolean startMonitoring) {
        initializeUI();
        if (startMonitoring) {
            startStatusMonitoring();
        } else {
            // In test vogliamo comunque inizializzare lo status una volta
            updateConnectionStatus();
        }
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

        JLabel titleLabel = new JLabel("Impostazioni");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(50, 50, 50));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        add(headerPanel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Selezione Colore
        JLabel colorLabel = new JLabel("Colore tema:");
        colorLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(colorLabel);
        contentPanel.add(Box.createVerticalStrut(5));

        colorCombo = new JComboBox<>(new String[]{COLOR_BLU, COLOR_GRIGIO, COLOR_VERDE, COLOR_ROSA});
        colorCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        colorCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        colorCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        colorCombo.setSelectedItem(COLOR_BLU);
        contentPanel.add(colorCombo);

        contentPanel.add(Box.createVerticalStrut(20));

        // ===== SEZIONE STATUS CONNESSIONE =====
        JLabel connectionLabel = new JLabel("Stato connessione:");
        connectionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(connectionLabel);
        contentPanel.add(Box.createVerticalStrut(5));

        // Panel per lo status
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        statusPanel.setOpaque(false);
        statusPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        // Icona status (pallino colorato)
        statusIcon = new JLabel("●");
        statusIcon.setFont(new Font("Segoe UI", Font.BOLD, 20));
        statusPanel.add(statusIcon);

        // Label con testo status
        statusLabel = new JLabel();
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        statusPanel.add(statusLabel);

        contentPanel.add(statusPanel);

        add(contentPanel, BorderLayout.CENTER);

        // Pannello per il pulsante in basso a destra
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setOpaque(false);

        // Pulsante Salva
        saveButton = new JButton("Salva Impostazioni") {
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
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveButton.setForeground(Color.WHITE);
        saveButton.setBorder(null);
        saveButton.setOpaque(false);
        saveButton.setContentAreaFilled(false);
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.setPreferredSize(new Dimension(180, 40));
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(e -> {
            if (onSaveListener != null) {
                onSaveListener.actionPerformed(e);
            }
        });
        buttonPanel.add(saveButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }

    /*
     * Avvia il monitoraggio periodico dello stato di connessione.
     * Aggiorna l'interfaccia ogni 5 secondi.
     */
    private void startStatusMonitoring() {
        // Aggiorna subito lo status all'avvio
        updateConnectionStatus();

        // Timer che aggiorna lo status ogni 5 secondi
        statusUpdateTimer = new Timer(5000, e -> {
            checkConnection();
            updateConnectionStatus();
        });
        statusUpdateTimer.start();
    }

    /**
     * Aggiorna l'interfaccia in base allo stato corrente della connessione.
     */
    private void updateConnectionStatus() {
        boolean isOnline = isOnline();

        SwingUtilities.invokeLater(() -> {
            if (isOnline) {
                statusIcon.setForeground(new Color(46, 204, 113)); // Verde
                statusLabel.setText("Online");
                statusLabel.setForeground(new Color(46, 204, 113));
            } else {
                statusIcon.setForeground(new Color(231, 76, 60)); // Rosso
                statusLabel.setText("Offline");
                statusLabel.setForeground(new Color(231, 76, 60));
            }
        });
    }

    /*
     * Ferma il timer di monitoraggio quando il pannello viene rimosso.
     * Chiamalo quando chiudi l'applicazione o rimuovi il pannello.
     */
    public void stopStatusMonitoring() {
        if (statusUpdateTimer != null) {
            statusUpdateTimer.stop();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Ombra
        g2d.setColor(new Color(0, 0, 0, 20));
        g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 20, 20);

        // Sfondo con colore del tema (OPACO)
        Color themeColor = getThemeColor(currentTheme);
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

    public JButton getSaveButton() {
        return saveButton;
    }

    public String getSelectedColor() {
        return (String) colorCombo.getSelectedItem();
    }

    public void setSelectedColor(String color) {
        colorCombo.setSelectedItem(color);
    }

    public void setOnSaveListener(ActionListener listener) {
        this.onSaveListener = listener;
    }

    // Metodo per ottenere i colori tema
    public static Color getThemeColor(String colorName) {
        return switch(colorName) {
            case COLOR_BLU -> new Color(70, 130, 180);
            case COLOR_GRIGIO -> new Color(128, 128, 128);
            case COLOR_VERDE -> new Color(31, 156, 0);
            case COLOR_ROSA -> new Color(84, 0, 135);
            default -> new Color(70, 130, 180);
        };
    }

    public static Color getThemeColorDark(String colorName) {
        return switch(colorName) {
            case COLOR_BLU -> new Color(60, 110, 170);
            case COLOR_GRIGIO -> new Color(100, 100, 100);
            case COLOR_VERDE -> new Color(31, 156, 0);
            case COLOR_ROSA -> new Color(116, 0, 158);
            default -> new Color(60, 110, 170);
        };
    }

    public static Color getThemeColorLight(String colorName) {
        return switch(colorName) {
            case COLOR_BLU -> new Color(80, 140, 200);
            case COLOR_GRIGIO -> new Color(150, 150, 150);
            case COLOR_VERDE -> new Color(38, 152, 13);
            case COLOR_ROSA -> new Color(147, 0, 146);
            default -> new Color(80, 140, 200);
        };
    }

    public void updateTheme(String theme) {
        this.currentTheme = theme;
        this.colorCombo.setSelectedItem(theme);
        repaint();
    }

    // ============================================================
    // serve per i test: incapsula le chiamate statiche
    // ============================================================

    protected boolean isOnline() {
        return ConnectivityService.isOnline();
    }

    protected void checkConnection() {
        ConnectivityService.checkConnection();
    }

    // Getter utili ai test (package-private)
    String getStatusTextForTest() {
        return statusLabel.getText();
    }

    Color getStatusColorForTest() {
        return statusLabel.getForeground();
    }

    Color getStatusIconColorForTest() {
        return statusIcon.getForeground();
    }
}
