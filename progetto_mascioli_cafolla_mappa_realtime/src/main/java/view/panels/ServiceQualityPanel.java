package view.panels;

import service.ServiceQualityMonitor;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class ServiceQualityPanel extends JPanel {
    private ServiceQualityMonitor monitor;
    private JLabel titolo;
    private JPanel metricsPanel;
    private JButton closeButton;
    private java.util.function.Consumer<Void> onCloseListener;

    public ServiceQualityPanel(ServiceQualityMonitor monitor) {
        this.monitor = monitor;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(8, 15, 3, 10));

        titolo = new JLabel("Qualità del Servizio");
        titolo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titolo.setForeground(new Color(50, 50, 50));
        headerPanel.add(titolo, BorderLayout.WEST);

        closeButton = new JButton("x");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        closeButton.setForeground(Color.DARK_GRAY);
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        closeButton.setPreferredSize(new Dimension(25, 25));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        closeButton.addActionListener(e -> {
            if (onCloseListener != null) onCloseListener.accept(null);
        });

        headerPanel.add(closeButton, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Metrics container
        metricsPanel = new JPanel();
        metricsPanel.setLayout(new BoxLayout(metricsPanel, BoxLayout.Y_AXIS));
        metricsPanel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(metricsPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);

        add(scrollPane, BorderLayout.CENTER);

        aggiornaDati();
    }

    public void aggiornaDati() {
        metricsPanel.removeAll();

        // Statistiche globali
        metricsPanel.add(creaSezione("📊 Statistiche Globali"));
        metricsPanel.add(creaMetrica("Arrivi monitorati",
                String.valueOf(monitor.getTotaleArriviMonitorati())));
        metricsPanel.add(creaMetrica("Affidabilità globale",
                String.format("%.1f%%", monitor.getAffidabilitaGlobale())));
        metricsPanel.add(creaMetrica("Ritardo medio",
                String.format("%.1f min", monitor.getRitardoMedioGlobale())));

        metricsPanel.add(Box.createVerticalStrut(15));

        // Top linee affidabili
        metricsPanel.add(creaSezione("✅ Top 5 Linee Affidabili"));
        List<Map.Entry<String, Double>> topAffidabili = monitor.getTop5LineePiuAffidabili();
        for (Map.Entry<String, Double> entry : topAffidabili) {
            metricsPanel.add(creaMetrica("Linea " + entry.getKey(),
                    String.format("%.1f%%", entry.getValue())));
        }

        metricsPanel.add(Box.createVerticalStrut(15));

        // Top linee con problemi
        metricsPanel.add(creaSezione("⚠️ Top 5 Linee con Ritardi"));
        List<Map.Entry<String, Double>> topProblemi = monitor.getTop5LineeMenoAffidabili();
        for (Map.Entry<String, Double> entry : topProblemi) {
            metricsPanel.add(creaMetrica("Linea " + entry.getKey(),
                    String.format("%.1f%%", entry.getValue()), Color.RED));
        }

        metricsPanel.revalidate();
        metricsPanel.repaint();
    }

    private JLabel creaSezione(String testo) {
        JLabel label = new JLabel(testo);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(50, 50, 50));
        label.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        return label;
    }

    private JPanel creaMetrica(String nome, String valore) {
        return creaMetrica(nome, valore, new Color(50, 50, 50));
    }

    private JPanel creaMetrica(String nome, String valore, Color colore) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(3, 20, 3, 20));

        JLabel nomeLabel = new JLabel(nome);
        nomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel valoreLabel = new JLabel(valore);
        valoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        valoreLabel.setForeground(colore);

        panel.add(nomeLabel, BorderLayout.WEST);
        panel.add(valoreLabel, BorderLayout.EAST);

        return panel;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Sfondo
        g2d.setColor(new Color(240, 248, 255));
        g2d.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 20, 20);

        // Bordo
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.setColor(new Color(70, 130, 180));
        g2d.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 20, 20);
    }

    public void setOnCloseListener(java.util.function.Consumer<Void> listener) {
        this.onCloseListener = listener;
    }
}
