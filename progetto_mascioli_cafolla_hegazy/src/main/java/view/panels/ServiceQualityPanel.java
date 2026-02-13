package view.panels;

import service.RealTimeDelayService;
import model.gtfs.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class ServiceQualityPanel extends JPanel {
    private RealTimeDelayService delayService;
    private JLabel titolo;
    private JPanel contentPanel;
    private JButton closeButton;
    private java.util.function.Consumer<Void> onCloseListener;

    private String currentContext = "IDLE";
    private Object contextData;
    private String currentTheme = "Blu"; // TEMA

    public ServiceQualityPanel(RealTimeDelayService delayService) {
        this.delayService = delayService;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setOpaque(false);

        // Header minimalista
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 10));

        titolo = new JLabel("Analisi Real-Time");
        titolo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        titolo.setForeground(new Color(40, 40, 40));
        headerPanel.add(titolo, BorderLayout.WEST);

        closeButton = new JButton("×");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        closeButton.setForeground(Color.GRAY);
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorder(null);
        closeButton.setPreferredSize(new Dimension(30, 30));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                closeButton.setForeground(Color.RED);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                closeButton.setForeground(Color.GRAY);
            }
        });

        closeButton.addActionListener(e -> {
            if (onCloseListener != null) onCloseListener.accept(null);
        });

        headerPanel.add(closeButton, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Content area
        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(12);

        add(scrollPane, BorderLayout.CENTER);

        mostraMessaggioIniziale();
    }

    // Aggiorna per fermata (design compatto)
    public void aggiornaPerFermata(Fermate fermata, java.util.List<BusArrivo> prossimiBus) {
        aggiornaPerFermata(fermata, prossimiBus, new ArrayList<>());
    }

    //Aggiorna per fermata (con statistiche)
    public void aggiornaPerFermata(Fermate fermata, java.util.List<BusArrivo> prossimiBus,
                                   List<BusArrivo> corseSoppresse) {
        this.currentContext = "FERMATA";
        this.contextData = fermata;

        titolo.setText(fermata.getStopName());
        contentPanel.removeAll();

        // CONTROLLO CONNESSIONE
        if (!isOnline()) {
            mostraMessaggioOffline();
            return;
        }

        if (prossimiBus.isEmpty() && (corseSoppresse == null || corseSoppresse.isEmpty())) {
            mostraNessunDato();
            return;
        }

        // CORSE SOPPRESSE (se presenti)
        if (corseSoppresse != null && !corseSoppresse.isEmpty()) {
            contentPanel.add(creaSezioneCorseSoppresse(corseSoppresse));
            contentPanel.add(Box.createVerticalStrut(10));
        }

        // STATISTICHE FERMATA
        if (!prossimiBus.isEmpty()) {
            contentPanel.add(creaStatisticheFermata(prossimiBus));
            contentPanel.add(Box.createVerticalStrut(12));
        }

        // PROSSIMI ARRIVI
        if (!prossimiBus.isEmpty()) {
            JLabel sezioneTitolo = new JLabel("Prossimi Arrivi");
            sezioneTitolo.setFont(new Font("Segoe UI", Font.BOLD, 12));
            sezioneTitolo.setForeground(new Color(80, 80, 80));
            sezioneTitolo.setBorder(BorderFactory.createEmptyBorder(5, 5, 8, 5));
            contentPanel.add(sezioneTitolo);

            int maxBus = Math.min(5, prossimiBus.size());
            for (int i = 0; i < maxBus; i++) {
                contentPanel.add(creaBusCompatto(prossimiBus.get(i)));
                if (i < maxBus - 1) {
                    contentPanel.add(Box.createVerticalStrut(6));
                }
            }

            // 4️⃣ SUGGERIMENTO
            contentPanel.add(Box.createVerticalStrut(12));
            contentPanel.add(creaSuggerimentoCompatto(prossimiBus));
        }

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // Aggiorna per linea (con statistiche linea)
    public void aggiornaPerLinea(Route linea, Map<String, Integer> delaysRT,
                                 StatisticheLinea statistiche) {
        this.currentContext = "LINEA";
        this.contextData = linea;

        titolo.setText("Linea " + linea.getRouteShortName());
        contentPanel.removeAll();

        // CONTROLLO CONNESSIONE
        if (!isOnline()) {
            mostraMessaggioOffline();
            return;
        }

        if (delaysRT.isEmpty()) {
            mostraNessunDato();
            return;
        }

        // STATISTICHE LINEA
        if (statistiche != null) {
            contentPanel.add(creaStatisticheLineaPanel(statistiche));
            contentPanel.add(Box.createVerticalStrut(12));
        }

        // STATUS GENERALE
        int ritardoMedio = calcolaRitardoMedio(delaysRT);
        int percentualeProblemi = calcolaPercentualeProblemi(delaysRT);
        contentPanel.add(creaStatusCompatto(ritardoMedio, percentualeProblemi, delaysRT.size()));

        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // MESSAGGI

    private void mostraMessaggioIniziale() {
        contentPanel.removeAll();
        JLabel msg = new JLabel("<html><center>Cerca una fermata<br>per visualizzare l'analisi</center></html>");
        msg.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        msg.setForeground(Color.GRAY);
        msg.setHorizontalAlignment(SwingConstants.CENTER);
        msg.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));
        contentPanel.add(msg);
    }

    private void mostraNessunDato() {
        contentPanel.removeAll();
        JLabel msg = new JLabel("<html><center>Nessun dato<br>real-time disponibile</center></html>");
        msg.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        msg.setForeground(Color.GRAY);
        msg.setHorizontalAlignment(SwingConstants.CENTER);
        msg.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));
        contentPanel.add(msg);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private void mostraMessaggioOffline() {
        contentPanel.removeAll();

        JPanel offlinePanel = new JPanel();
        offlinePanel.setLayout(new BoxLayout(offlinePanel, BoxLayout.Y_AXIS));
        offlinePanel.setOpaque(false);
        offlinePanel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));

        // Icona
        JLabel iconLabel = new JLabel("📡");
        iconLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
        iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Messaggio principale
        JLabel msgLabel = new JLabel("<html><center>Monitoraggio non disponibile</center></html>");
        msgLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        msgLabel.setForeground(new Color(80, 80, 80));
        msgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        msgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Descrizione
        JLabel descLabel = new JLabel("<html><center>I dati real-time sono disponibili<br>solo quando sei connesso a Internet</center></html>");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(Color.GRAY);
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        descLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        offlinePanel.add(iconLabel);
        offlinePanel.add(Box.createVerticalStrut(15));
        offlinePanel.add(msgLabel);
        offlinePanel.add(Box.createVerticalStrut(10));
        offlinePanel.add(descLabel);

        contentPanel.add(offlinePanel);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    // CORSE SOPPRESSE

    private JPanel creaSezioneCorseSoppresse(List<BusArrivo> corseSoppresse) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 0, 0), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        panel.setBackground(new Color(255, 245, 245));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 300));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titoloSezione = new JLabel("Corse Soppresse");
        titoloSezione.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titoloSezione.setForeground(new Color(150, 0, 0));
        titoloSezione.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titoloSezione);
        panel.add(Box.createVerticalStrut(6));

        for (BusArrivo corsa : corseSoppresse) {
            JLabel linea = new JLabel("Linea " + corsa.nomeLinea + " - " +
                    estraiOrarioBase(corsa.orarioFormattato) + " (Cancellata)");
            linea.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            linea.setForeground(new Color(100, 0, 0));
            linea.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(linea);
            panel.add(Box.createVerticalStrut(3));
        }

        return panel;
    }

    // STATISTICHE FERMATA

    private JPanel creaStatisticheFermata(List<BusArrivo> buses) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        panel.setBackground(new Color(248, 248, 250));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Titolo
        JLabel titoloSezione = new JLabel("Situazione Attuale Fermata");
        titoloSezione.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titoloSezione.setForeground(new Color(60, 60, 60));
        titoloSezione.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titoloSezione);
        panel.add(Box.createVerticalStrut(6));

        // Calcolo statistiche
        int totBus = buses.size();
        long puntuali = buses.stream().filter(b -> b.ritardoMinuti >= -1 && b.ritardoMinuti <= 2).count();
        long inRitardo = buses.stream().filter(b -> b.ritardoMinuti > 2).count();
        double ritardoMedio = buses.stream().mapToInt(b -> b.ritardoMinuti).average().orElse(0);
        BusArrivo maxRitardo = buses.stream().max(Comparator.comparingInt(b -> b.ritardoMinuti)).orElse(null);

        String[] righe = {
                "Bus in arrivo: " + totBus,
                "Bus puntuali: " + puntuali + " (" + String.format("%.0f", (puntuali * 100.0 / totBus)) + "%)",
                "Bus in ritardo: " + inRitardo + " (" + String.format("%.0f", (inRitardo * 100.0 / totBus)) + "%)",
                "Ritardo medio: " + (ritardoMedio > 0 ? "+" : "") + String.format("%.1f", ritardoMedio) + " min",
                maxRitardo != null ? "Max ritardo: Linea " + maxRitardo.nomeLinea + " (+" + maxRitardo.ritardoMinuti + " min)" : ""
        };

        for (String riga : righe) {
            if (riga.isEmpty()) continue;
            JLabel label = new JLabel(riga);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            label.setForeground(new Color(70, 70, 70));
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(label);
            panel.add(Box.createVerticalStrut(3));
        }

        return panel;
    }


    //STATISTICHE LINEA

    private JPanel creaStatisticheLineaPanel(StatisticheLinea stats) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 180, 180), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        panel.setBackground(new Color(248, 248, 250));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titoloSezione = new JLabel("Statistiche Linea");
        titoloSezione.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titoloSezione.setForeground(new Color(60, 60, 60));
        titoloSezione.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titoloSezione);
        panel.add(Box.createVerticalStrut(6));

        String[] righe = {
                "Frequenza media: Ogni " + stats.frequenzaMinuti + " min",
                "Corse totali oggi: " + stats.corseTotali,
                "Corse completate: " + stats.corseCompletate,
                "Corse soppresse: " + stats.corseSoppresse,
                "Affidabilità: " + String.format("%.0f", stats.affidabilita) + "%",
                "Ritardo medio giornaliero: +" + String.format("%.1f", stats.ritardoMedioGiornaliero) + " min"
        };

        for (String riga : righe) {
            JLabel label = new JLabel(riga);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            label.setForeground(new Color(70, 70, 70));
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(label);
            panel.add(Box.createVerticalStrut(3));
        }

        return panel;
    }

    // BUS COMPATTO (CON AFFOLLAMENTO)

    private JPanel creaBusCompatto(BusArrivo bus) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(8, 0));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(getColorByDelay(bus.ritardoMinuti), 1),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        card.setBackground(new Color(252, 252, 252));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 65));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Indicatore stato (barra colorata)
        JPanel indicator = new JPanel();
        indicator.setPreferredSize(new Dimension(4, 40));
        indicator.setBackground(getColorByDelay(bus.ritardoMinuti));
        card.add(indicator, BorderLayout.WEST);

        // Info principale
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setOpaque(false);

        // Linea + Orario
        JPanel topRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        topRow.setOpaque(false);

        JLabel lineaLabel = new JLabel("Linea " + bus.nomeLinea);
        lineaLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lineaLabel.setForeground(new Color(40, 40, 40));

        JLabel orarioLabel = new JLabel(" - " + estraiOrarioBase(bus.orarioFormattato));
        orarioLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        orarioLabel.setForeground(new Color(80, 80, 80));

        topRow.add(lineaLabel);
        topRow.add(orarioLabel);

        // Direzione
        JLabel direzioneLabel = new JLabel(bus.direzione);
        direzioneLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        direzioneLabel.setForeground(new Color(120, 120, 120));

        // Status + Affollamento
        JPanel statusRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        statusRow.setOpaque(false);

        JLabel statusLabel = creaStatusLabel(bus.ritardoMinuti);
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        statusRow.add(statusLabel);

        // AFFOLLAMENTO
        if (bus.affollamento != AffollamentoBus.SCONOSCIUTO) {
            JLabel separatore = new JLabel(" | ");
            separatore.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            separatore.setForeground(Color.GRAY);
            statusRow.add(separatore);

            JLabel affollLabel = creaAffollamentoLabel(bus.affollamento);
            statusRow.add(affollLabel);
        }

        infoPanel.add(topRow);
        infoPanel.add(Box.createVerticalStrut(2));
        infoPanel.add(direzioneLabel);
        infoPanel.add(statusRow);

        card.add(infoPanel, BorderLayout.CENTER);

        return card;
    }

    private JLabel creaAffollamentoLabel(AffollamentoBus livello) {
        String testo;
        Color colore;

        switch (livello) {
            case BASSO:
                testo = "Affollamento: Basso";
                colore = new Color(0, 140, 0);
                break;
            case MEDIO:
                testo = "Affollamento: Medio";
                colore = new Color(200, 150, 0);
                break;
            case ALTO:
                testo = "Affollamento: Alto";
                colore = new Color(255, 100, 0);
                break;
            case MOLTO_ALTO:
                testo = "Affollamento: Molto Alto";
                colore = new Color(200, 0, 0);
                break;
            default:
                testo = "";
                colore = Color.GRAY;
        }

        JLabel label = new JLabel(testo);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        label.setForeground(colore);
        return label;
    }

    private JLabel creaStatusLabel(int ritardoMinuti) {
        String testo;
        Color colore;

        if (ritardoMinuti > 8) {
            testo = "Ritardo: +" + ritardoMinuti + " min";
            colore = new Color(200, 0, 0);
        } else if (ritardoMinuti > 5) {
            testo = "In ritardo: +" + ritardoMinuti + " min";
            colore = new Color(255, 100, 0);
        } else if (ritardoMinuti > 2) {
            testo = "Lieve ritardo: +" + ritardoMinuti + " min";
            colore = new Color(200, 150, 0);
        } else if (ritardoMinuti >= -1 && ritardoMinuti <= 2) {
            testo = "Puntuale";
            colore = new Color(0, 140, 0);
        } else {
            testo = "In anticipo: " + ritardoMinuti + " min";
            colore = new Color(0, 100, 200);
        }

        JLabel label = new JLabel(testo);
        label.setForeground(colore);
        return label;
    }

    private JPanel creaSuggerimentoCompatto(List<BusArrivo> buses) {
        BusArrivo migliore = buses.stream()
                .min(Comparator.comparingInt(b -> Math.abs(b.ritardoMinuti)))
                .orElse(null);

        if (migliore == null) return new JPanel();

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(6, 0));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 120, 0), 1),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        panel.setBackground(new Color(240, 255, 240));
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel suggLabel = new JLabel("Consigliato: Linea " + migliore.nomeLinea +
                " (" + (migliore.ritardoMinuti == 0 ? "puntuale" :
                migliore.ritardoMinuti > 0 ? "+" + migliore.ritardoMinuti + " min" :
                        migliore.ritardoMinuti + " min") + ")");
        suggLabel.setFont(new Font("Segoe UI", Font.BOLD, 11));
        suggLabel.setForeground(new Color(0, 100, 0));

        panel.add(suggLabel, BorderLayout.CENTER);
        return panel;
    }

    private JPanel creaStatusCompatto(int ritardoMedio, int percentualeProblemi, int totBus) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.setOpaque(false);

        String[] righe = {
                "Situazione: " + (percentualeProblemi > 60 ? "Critica" :
                        percentualeProblemi > 30 ? "Moderata" : "Regolare"),
                "Ritardo medio: +" + ritardoMedio + " min",
                "Bus attivi: " + totBus,
                "In ritardo: " + percentualeProblemi + "%"
        };

        for (String riga : righe) {
            JLabel label = new JLabel(riga);
            label.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            label.setForeground(new Color(60, 60, 60));
            label.setAlignmentX(Component.LEFT_ALIGNMENT);
            panel.add(label);
            panel.add(Box.createVerticalStrut(3));
        }

        return panel;
    }

    // UTILITY

    private Color getColorByDelay(int minuti) {
        if (minuti > 5) return new Color(220, 50, 50);
        if (minuti > 2) return new Color(255, 150, 0);
        if (minuti >= -1) return new Color(50, 180, 50);
        return new Color(0, 120, 200);
    }

    private String estraiOrarioBase(String orarioFormattato) {
        if (orarioFormattato.contains("(")) {
            return orarioFormattato.substring(0, orarioFormattato.indexOf("(")).trim();
        }
        return orarioFormattato.trim();
    }

    private int calcolaRitardoMedio(Map<String, Integer> delays) {
        if (delays.isEmpty()) return 0;
        int sum = delays.values().stream().mapToInt(Integer::intValue).sum();
        return (sum / delays.size()) / 60;
    }

    private int calcolaPercentualeProblemi(Map<String, Integer> delays) {
        if (delays.isEmpty()) return 0;
        long problemi = delays.values().stream()
                .filter(d -> d > 180)
                .count();
        return (int) ((problemi * 100.0) / delays.size());
    }

    // TEMA & GRAFICA

    public void updateTheme(String theme) {
        this.currentTheme = theme;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Ombra esterna
        g2d.setColor(new Color(0, 0, 0, 20));
        g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 20, 20);

        //Sfondo colorato basato sul tema (IDENTICO AGLI ALTRI PANNELLI)
        Color themeColor = SettingsPanel.getThemeColor(currentTheme);
        Color lightColor = new Color(
                Math.min(themeColor.getRed() + 150, 255),
                Math.min(themeColor.getGreen() + 150, 255),
                Math.min(themeColor.getBlue() + 150, 255)
        );
        g2d.setColor(lightColor);
        g2d.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 20, 20);

        // Bordo colorato
        g2d.setStroke(new BasicStroke(1.5f));
        g2d.setColor(themeColor);
        g2d.drawRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 20, 20);
    }

    public void setOnCloseListener(java.util.function.Consumer<Void> listener) {
        this.onCloseListener = listener;
    }

    // CLASSI HELPER

    public static class BusArrivo {
        public String nomeLinea;
        public String direzione;
        public String orarioFormattato;
        public int ritardoMinuti;
        public AffollamentoBus affollamento;

        public BusArrivo(String nomeLinea, String direzione, String orarioFormattato,
                         int ritardoMinuti, AffollamentoBus affollamento) {
            this.nomeLinea = nomeLinea;
            this.direzione = direzione;
            this.orarioFormattato = orarioFormattato;
            this.ritardoMinuti = ritardoMinuti;
            this.affollamento = affollamento;
        }

        // Constructor retrocompatibile
        public BusArrivo(String nomeLinea, String direzione, String orarioFormattato, int ritardoMinuti) {
            this(nomeLinea, direzione, orarioFormattato, ritardoMinuti, AffollamentoBus.SCONOSCIUTO);
        }
    }

    public enum AffollamentoBus {
        SCONOSCIUTO,
        BASSO,
        MEDIO,
        ALTO,
        MOLTO_ALTO
    }

    public static class StatisticheLinea {
        public int frequenzaMinuti;
        public int corseTotali;
        public int corseCompletate;
        public int corseSoppresse;
        public double affidabilita;
        public double ritardoMedioGiornaliero;

        public StatisticheLinea(int frequenzaMinuti, int corseTotali, int corseCompletate,
                                int corseSoppresse, double affidabilita, double ritardoMedioGiornaliero) {
            this.frequenzaMinuti = frequenzaMinuti;
            this.corseTotali = corseTotali;
            this.corseCompletate = corseCompletate;
            this.corseSoppresse = corseSoppresse;
            this.affidabilita = affidabilita;
            this.ritardoMedioGiornaliero = ritardoMedioGiornaliero;
        }
    }

    // serve per i test (NON cambia la logica: default = ConnectivityService)

    protected boolean isOnline() {
        return service.ConnectivityService.isOnline();
    }

    // serve per i test: leggere facilmente lo stato UI
    String getTitoloTextForTest() {
        return titolo.getText();
    }

    // serve per i test: accesso al contenuto mostrato
    int getContentChildrenCountForTest() {
        return contentPanel.getComponentCount();
    }

    // serve per i test: ritorna copia dei componenti contenuti
    java.util.List<Component> getContentChildrenSnapshotForTest() {
        return java.util.List.of(contentPanel.getComponents());
    }
}
