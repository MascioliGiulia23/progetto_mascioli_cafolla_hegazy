package view.panels;

import javax.swing.SwingUtilities;

import model.gtfs.*;
import model.user.Favorite;
import model.user.UserManager;
import org.jxmapviewer.viewer.GeoPosition;
import view.map.BusWaypoint;
import view.map.RouteDrawer;
import view.map.WaypointDrawer;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SearchResultsPanel extends JPanel {
    private JPanel resultsContainer;
    private JScrollPane scrollPane;
    private List<ResultItem> results;
    // METODO MOSTRA FERMATE INDIETRO
    private java.util.List<Fermate> ultimeFermate = new java.util.ArrayList<>();
    private java.util.List<Route> ultimeRotte = new java.util.ArrayList<>();
    private boolean ripristinando = false;
    private String currentTheme = "Blu";
    private JButton closeButton;
    private java.util.function.Consumer<Void> onCloseListener;

    private java.util.function.Consumer<Fermate> onStopClickListener; // listener per fermate
    private java.util.function.Consumer<Route> onRouteClickListener; // listener per rotte


    // listener per click su fermata nella lista linea
    private java.util.function.Consumer<Fermate> onLineaStopClickListener;
    public void setOnLineaStopClickListener(java.util.function.Consumer<Fermate> listener) {
        this.onLineaStopClickListener = listener;
    }

    // Campi per waypoint
    private WaypointDrawer waypointDrawer;
    private RouteDrawer routeDrawer;
    private Map<String, ShapeRoute> forme;
    private List<Fermate> tutteLeFermate;
    private List<Route> tutteLeRotte;
    private List<Trip> tuttiITrips;
    private List<StopTime> tuttiGliStopTimes;
    private service.RealTimeDelayService delayService;

    public SearchResultsPanel() {
        results = new ArrayList<>();
        initializeUI();
        }
        public void setDelayService(service.RealTimeDelayService service) {
            this.delayService = service;
        }





    // Metodo setter per waypoint
    public void setWaypointDrawer(WaypointDrawer drawer, List<Fermate> fermate,
                                  List<Route> rotte, List<Trip> trips, List<StopTime> stopTimes) {
        this.waypointDrawer = drawer;
        this.tutteLeFermate = fermate;
        this.tutteLeRotte = rotte;
        this.tuttiITrips = trips;
        this.tuttiGliStopTimes = stopTimes;
    }

    // Metodo setter per route drawer
    public void setRouteDrawer(RouteDrawer drawer, Map<String, ShapeRoute> forme) {
        this.routeDrawer = drawer;
        this.forme = forme;
    }

    // listener per click su fermata
    public void setOnStopClickListener(java.util.function.Consumer<Fermate> listener) {
        this.onStopClickListener = listener;
    }

    // listener per click su rotta
    public void setOnRouteClickListener(java.util.function.Consumer<Route> listener) {
        this.onRouteClickListener = listener;
    }

    private void initializeUI() {
        setLayout(new BorderLayout());
        setOpaque(false);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 5, 10));

        JLabel titleLabel = new JLabel("Risultati");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(50, 50, 50));
        headerPanel.add(titleLabel, BorderLayout.WEST);

        // Bottone X (stile identico a FavoritesPanel)
        closeButton = new JButton("x");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        closeButton.setForeground(Color.DARK_GRAY);
        closeButton.setFocusPainted(false);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));
        closeButton.setPreferredSize(new Dimension(25, 25));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        closeButton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                closeButton.setForeground(Color.RED);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                closeButton.setForeground(Color.DARK_GRAY);
            }
        });

        closeButton.addActionListener(e -> {
            if (onCloseListener != null) {
                onCloseListener.accept(null);
            }
        });

        headerPanel.add(closeButton, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);


        resultsContainer = new JPanel();
        resultsContainer.setLayout(new BoxLayout(resultsContainer, BoxLayout.Y_AXIS));
        resultsContainer.setOpaque(false);

        scrollPane = new JScrollPane(resultsContainer);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        scrollPane.getVerticalScrollBar().setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override
            protected void configureScrollBarColors() {
                this.thumbColor = new Color(180, 180, 180);
            }
        });

        add(scrollPane, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(0, 0, 0, 20)); // ombra
        g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 20, 20);

        // Sfondo chiaro basato sul tema
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

    // Aggiunge un risultato generico
    public void addResult(String title, String description, String icon, Fermate fermata) {
        ResultItem item = new ResultItem(title, description, icon, currentTheme, fermata);
        item.setOnClickListener(() -> {
            if (fermata != null && onStopClickListener != null) {
                onStopClickListener.accept(fermata);
            }
        });
        results.add(item);
        resultsContainer.add(item);
    }

    // Pulisce la lista
    public void clearResults() {
        resultsContainer.removeAll();
        results.clear();
    }

    // Aggiorna i colori
    public void updateTheme(String theme) {
        this.currentTheme = theme;
        for (ResultItem item : results) {
            item.updateTheme(theme);
        }
        repaint();
    }

    //  Mostra le fermate
    public void aggiornaRisultati(List<Fermate> fermate) {
        clearResults();
        if (fermate == null || fermate.isEmpty()) {
            resultsContainer.revalidate();
            resultsContainer.repaint();
            return;
        }

        // salva solo se non stai tornando indietro
        if (!ripristinando) {
            ultimeFermate.clear();
            ultimeFermate.addAll(fermate);
            System.out.println("aggiornaRisultati: salvate " + ultimeFermate.size() + " fermate");
        } else {
            System.out.println("aggiornaRisultati: ripristino senza salvare");
            ripristinando = false; // reset
        }

        for (Fermate f : fermate) {
            String descrizione = "ID: " + f.getStopId() + "  (FERMATA)";
            addResult(f.getStopName(), descrizione, "", f);
        }

        resultsContainer.revalidate();
        resultsContainer.repaint();
    }

    // Mostra le linee (rotte)
    public void aggiornaRisultatiRotte(java.util.List<Route> rotte) {
        clearResults();
        if (rotte == null || rotte.isEmpty()) {
            resultsContainer.revalidate();
            resultsContainer.repaint();
            return;
        }

        // Salva la lista solo se NON stai tornando indietro
        if (!ripristinando) {
            ultimeRotte.clear();
            ultimeRotte.addAll(rotte);
            System.out.println("Salvate " + ultimeRotte.size() + " linee trovate.");
        } else {
            ripristinando = false;
            System.out.println("Ripristino linee precedenti, non salvo.");
        }

        for (Route r : rotte) {
            String nomeLinea = r.getRouteShortName();
            String tipo = "LINEA";

            if (nomeLinea != null) {
                String nomeUpper = nomeLinea.toUpperCase();

                // METRO
                if (nomeUpper.matches("M[EABCD]+\\d*")) {
                    tipo = "METRO";
                }
                // TRAM
                else if (nomeUpper.matches("(RMG|2BUS|5BUS|8BUS|14BUS|19NAV|19|3BUS|3BIS|2BIS|3D|3S|8)")) {
                    tipo = "TRAM";
                }
            }

            String descrizione = "ID: " + r.getRouteId() + "  (" + tipo + ")";

            ResultItem item = new ResultItem(nomeLinea, descrizione, "", currentTheme, null);
            item.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));

            item.setOnClickListener(() -> {
                if (onRouteClickListener != null) {
                    onRouteClickListener.accept(r);
                }
            });

            results.add(item);
            resultsContainer.add(item);
        }
        resultsContainer.revalidate();
        resultsContainer.repaint();
    }


    // Classe interna per singolo risultato

    public static class ResultItem extends JPanel {
        private String title;
        private String description;
        private String icon;
        private String currentTheme;
        private Fermate fermata;
        private OnItemClickListener onItemClickListener;
        private boolean isHovered = false;

        public ResultItem(String title, String description, String icon, String theme, Fermate fermata) {
            this.title = title;
            this.description = description;
            this.icon = icon;
            this.currentTheme = theme;
            this.fermata = fermata;
            initializeUI();
        }

        private void initializeUI() {
            setLayout(new BorderLayout(10, 0));
            setBorder(BorderFactory.createEmptyBorder(12, 15, 12, 15));
            setOpaque(false);
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 70));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            JLabel iconLabel = new JLabel(icon);
            iconLabel.setFont(new Font("Arial", Font.PLAIN, 20));
            add(iconLabel, BorderLayout.WEST);

            JPanel infoPanel = new JPanel();
            infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
            infoPanel.setOpaque(false);

            JLabel titleLabel = new JLabel(title);
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            titleLabel.setForeground(new Color(50, 50, 50));
            infoPanel.add(titleLabel);

            JLabel descriptionLabel = new JLabel(description);
            descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            descriptionLabel.setForeground(new Color(120, 120, 120));
            infoPanel.add(Box.createVerticalStrut(4));
            infoPanel.add(descriptionLabel);

            add(infoPanel, BorderLayout.CENTER);

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    setBackground(new Color(245, 245, 245));
                    setOpaque(true);
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    setOpaque(false);
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (onItemClickListener != null) onItemClickListener.onClick();
                }
            });
        }

        public void updateTheme(String theme) {
            this.currentTheme = theme;
        }

        public void setOnClickListener(OnItemClickListener listener) {
            this.onItemClickListener = listener;
        }

        public interface OnItemClickListener {
            void onClick();
        }
    }
    private static class OrarioRow {
        String nomeLinea;
        String direzione;
        String orarioFormattato;
        String tripId;  // ⭐ CHIAVE per matching real-time

        OrarioRow(String nomeLinea, String direzione, String orarioFormattato, String tripId) {
            this.nomeLinea = nomeLinea;
            this.direzione = direzione;
            this.orarioFormattato = orarioFormattato;
            this.tripId = tripId;
        }

        String[] toArray() {
            return new String[]{nomeLinea, direzione, orarioFormattato};
        }
    }
    // metodo per mostrare le fermate
    public void mostraOrariFermata(Fermate fermata,
                                   List<StopTime> stopTimes,
                                   List<Trip> trips,
                                   List<Route> rotte,
                                   List<Fermate> tutteLeFermate,
                                   Route rottaCorrente,
                                   Trip direzioneCorrente,
                                   String contesto) {
        clearResults();

        // Mappe hash per lookups O(1)
        Map<String, Trip> tripMap = new HashMap<>(trips.size());
        Map<String, Route> routeMap = new HashMap<>(rotte.size());
        Map<String, Fermate> fermatePerId = new HashMap<>(tutteLeFermate.size());

        for (Trip trip : trips) tripMap.put(trip.getTripId(), trip);
        for (Route route : rotte) routeMap.put(route.getRouteId(), route);
        for (Fermate f : tutteLeFermate) fermatePerId.put(f.getStopId(), f);

        // Indice stopTimes per fermata
        Map<String, List<StopTime>> stopTimePerFermata = new HashMap<>();
        Map<String, StopTime> ultimoStopPerTrip = new HashMap<>();

        LocalTime oraCorrente = LocalTime.now().minusMinutes(5); // ⭐ Include bus in ritardo
        LocalTime oraMax = oraCorrente.plusMinutes(65); // ⭐ Finestra 60 min effettivi (5 passati + 60 futuri)



        for (StopTime st : stopTimes) {
            stopTimePerFermata.computeIfAbsent(st.getStopId(), k -> new ArrayList<>()).add(st);

            String tripId = st.getTripId();
            StopTime existing = ultimoStopPerTrip.get(tripId);
            if (existing == null || st.getStopSequence() > existing.getStopSequence()) {
                ultimoStopPerTrip.put(tripId, st);
            }
        }

        // PRE-CALCOLO CAPOLINEA
        Map<String, String> capolineaPerTrip = new HashMap<>();
        for (Map.Entry<String, StopTime> entry : ultimoStopPerTrip.entrySet()) {
            Fermate capolinea = fermatePerId.get(entry.getValue().getStopId());
            if (capolinea != null) {
                capolineaPerTrip.put(entry.getKey(), capolinea.getStopName());
            }
        }

        // WAYPOINTS - Loop minimo
        if (waypointDrawer != null) {
            waypointDrawer.clearWaypoints();
            Set<BusWaypoint> waypoints = new HashSet<>();

            List<StopTime> fermataStops = stopTimePerFermata.get(fermata.getStopId());
            if (fermataStops != null) {
                GeoPosition pos = new GeoPosition(fermata.getStopLat(), fermata.getStopLon());
                boolean hasValidTrip = false;

                for (StopTime st : fermataStops) {
                    if (tripMap.containsKey(st.getTripId()) &&
                            routeMap.containsKey(tripMap.get(st.getTripId()).getRouteId())) {
                        hasValidTrip = true;
                        break;
                    }
                }

                if (hasValidTrip) {
                    waypoints.add(new BusWaypoint(pos));
                    waypointDrawer.addWaypoints(waypoints);
                }
            }
        }

        // Titolo + bottone preferiti
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        topPanel.setOpaque(false);
        JLabel titolo = new JLabel("\"" + fermata.getStopName() + "\"");
        titolo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titolo.setOpaque(false);

        JButton favBtn = new JButton(" Preferiti");
        favBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        favBtn.addActionListener(e -> {
            String user = UserProfilePanel.getCurrentUsernameStatic();
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Effettua il login per aggiungere ai preferiti.");
                return;
            }

            Favorite f = new Favorite(fermata.getStopName(), "FERMATA");
            if (UserManager.haPreferito(user, f)) {
                UserManager.rimuoviPreferito(user, f);
                JOptionPane.showMessageDialog(this, "Fermata rimossa dai preferiti!");
            } else {
                UserManager.aggiungiPreferito(user, f);
                JOptionPane.showMessageDialog(this, "Fermata aggiunta ai preferiti!");
            }

            aggiornaPannelloPreferiti(user);
        });

        topPanel.add(titolo);
        topPanel.add(favBtn);
        resultsContainer.add(topPanel);

        // Posizionamento manuale dell'ID
        JPanel idPanel = new JPanel(null);
        idPanel.setOpaque(false);
        idPanel.setPreferredSize(new Dimension(400, 25));

        JLabel idLabel = new JLabel("ID: " + fermata.getStopId());
        idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        idPanel.add(idLabel);
        idLabel.setBounds(160, 0, 200, 20);

        resultsContainer.add(idPanel);

        // RACCOLTA DATI - Unico loop ottimizzato
        List<OrarioRow> righe = new ArrayList<>();
        Set<String> orariGiaAggiunti = new HashSet<>();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");

        List<StopTime> fermataStops = stopTimePerFermata.get(fermata.getStopId());
        if (fermataStops != null) {
            for (StopTime st : fermataStops) {
                if (st.getArrivalTime() == null) continue;

                LocalTime arrivo = st.getArrivalTime();
                if (arrivo.isBefore(oraCorrente) || arrivo.isAfter(oraMax)) continue;

                Trip trip = tripMap.get(st.getTripId());
                if (trip == null) continue;

                Route route = routeMap.get(trip.getRouteId());
                if (route == null) continue;

                String nomeLinea = route.getRouteShortName();
                String orarioFormattato = arrivo.format(fmt);
                String tripId = st.getTripId();
                String chiave = nomeLinea + "|" + orarioFormattato + "|" + tripId;

                if (orariGiaAggiunti.add(chiave)) {
                    String capolineaNome = capolineaPerTrip.getOrDefault(tripId, "?");
                    String direzione = " → " + capolineaNome;
                    righe.add(new OrarioRow(nomeLinea, direzione, orarioFormattato, tripId));
                }
            }
        }

        // ⭐ CARICA RITARDI REAL-TIME
        List<OrarioRow> righeConRT = new ArrayList<>();

        if (delayService != null && service.ConnectivityService.isOnline()) {
            try {
                System.out.println("═══════════════════════════════════════════════");
                System.out.println("[SearchResultsPanel] Richiesta ritardi per fermata: " + fermata.getStopId());

                Map<String, Integer> delaysByLineaOrario = delayService.getDelaysByTripId(fermata.getStopId());
                System.out.println("[SearchResultsPanel] Ritardi ricevuti per " + delaysByLineaOrario.size() + " combinazioni");

                // ⭐ FILTRA: Tieni SOLO le righe con dati RT
                for (OrarioRow riga : righe) {
                    String chiaveRT = riga.nomeLinea + "#" + riga.orarioFormattato;
                    Integer delaySeconds = delaysByLineaOrario.get(chiaveRT);

                    if (delaySeconds != null) {
                        int minutes = delaySeconds / 60;

                        if (Math.abs(minutes) > 1) {
                            if (minutes > 0) {
                                riga.orarioFormattato = riga.orarioFormattato + "  (+" + minutes + " min)";
                            } else {
                                riga.orarioFormattato = riga.orarioFormattato + "  (" + minutes + " min)";
                            }
                        } else {
                            riga.orarioFormattato = riga.orarioFormattato + "  (On Time)";
                        }

                        righeConRT.add(riga);
                        System.out.println("[SearchResultsPanel] ✓ Linea " + riga.nomeLinea + " orario " + riga.orarioFormattato);
                    }
                }

                System.out.println("═══════════════════════════════════════════════");

            } catch (Exception e) {
                System.err.println("[SearchResultsPanel] ✗ Errore caricamento ritardi: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // ⭐ DECISIONE: Mostra RT se disponibili, altrimenti fallback a statico
        // ⭐ DECISIONE: Mostra RT se disponibili, altrimenti fallback a statico
        List<OrarioRow> righeDaMostrare;

        if (!righeConRT.isEmpty()) {
            // ⭐ RIMUOVI DUPLICATI: Una sola riga per linea+orario
            Map<String, OrarioRow> mappaUnica = new LinkedHashMap<>();
            for (OrarioRow riga : righeConRT) {
                String chiaveUnica = riga.nomeLinea + "#" + riga.orarioFormattato;
                mappaUnica.putIfAbsent(chiaveUnica, riga); // Tiene solo la prima
            }
            righeDaMostrare = new ArrayList<>(mappaUnica.values());
            System.out.println("[SearchResultsPanel] ✓ Mostrando " + righeDaMostrare.size() + " linee REAL-TIME (rimosse " + (righeConRT.size() - righeDaMostrare.size()) + " duplicati)");
        } else {
            // ⭐ Anche per lo statico: rimuovi duplicati linea+orario
            Map<String, OrarioRow> mappaUnica = new LinkedHashMap<>();
            for (OrarioRow riga : righe) {
                String chiaveUnica = riga.nomeLinea + "#" + riga.orarioFormattato.replaceAll("\\s*\\([^)]*\\)", ""); // Rimuove eventuali suffix tipo "(+5 min)"
                mappaUnica.putIfAbsent(chiaveUnica, riga);
            }
            righeDaMostrare = new ArrayList<>(mappaUnica.values());
            System.out.println("[SearchResultsPanel] ⚠ Nessun dato RT, mostrando " + righeDaMostrare.size() + " orari statici (rimosse " + (righe.size() - righeDaMostrare.size()) + " duplicati)");
        }


        // ⭐ Converti in String[] per la tabella
        List<String[]> righeTabella = righeDaMostrare.stream()
                .map(OrarioRow::toArray)
                .collect(java.util.stream.Collectors.toList());

        righeTabella.sort(Comparator.comparing(arr -> arr[2]));

        if (righeTabella.isEmpty()) {
            JLabel noResult = new JLabel("Nessun arrivo nei prossimi 40 minuti.", SwingConstants.CENTER);
            noResult.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            noResult.setForeground(Color.DARK_GRAY);
            resultsContainer.add(Box.createVerticalStrut(10));
            resultsContainer.add(noResult);

        } else {
            // ⭐ Se arrivo da "LINEA" → mostro solo la colonna Orario arrivo
            String[] colonne;
            String[][] data;

            if ("LINEA".equalsIgnoreCase(contesto)) {
                colonne = new String[]{"Orario arrivo"};
                data = new String[righeTabella.size()][1];
                for (int i = 0; i < righeTabella.size(); i++) {
                    data[i][0] = righeTabella.get(i)[2]; // solo orario
                }

                // Mostra intestazione con linea + direzione
                if (rottaCorrente != null && direzioneCorrente != null) {
                    JLabel infoLinea = new JLabel(
                            "Linea " + rottaCorrente.getRouteShortName() +
                                    " → " + direzioneCorrente.getTripHeadsign(),
                            SwingConstants.CENTER
                    );
                    infoLinea.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    infoLinea.setForeground(new Color(50, 50, 50));

                    JPanel lineaPanel = new JPanel(null);
                    lineaPanel.setOpaque(false);
                    lineaPanel.setPreferredSize(new Dimension(400, 40));
                    lineaPanel.add(infoLinea);
                    infoLinea.setBounds(80, 10, 200, 25);

                    resultsContainer.add(lineaPanel);
                }

            } else {
                // Caso normale (ricerca fermata)
                colonne = new String[]{"Linea", "Direzione", "Orario arrivo"};
                data = righeTabella.toArray(new String[0][]);
            }

            JTable tabella = new JTable(data, colonne) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                    Component c = super.prepareRenderer(renderer, row, column);

                    int colOrario = colonne.length == 3 ? 2 : 0;

                    if (column == colOrario) {
                        String testo = getValueAt(row, column).toString();
                        if (testo.contains("+")) c.setForeground(new Color(200, 0, 0));
                        else if (testo.contains("-")) c.setForeground(new Color(0, 150, 255));
                        else if (testo.contains("On Time")) c.setForeground(new Color(0, 160, 0));
                        else c.setForeground(Color.BLACK);
                    } else {
                        c.setForeground(Color.BLACK);
                    }
                    return c;
                }
            };

            tabella.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            tabella.setRowHeight(22);

            if (colonne.length == 3) {
                tabella.getColumnModel().getColumn(0).setPreferredWidth(50);
                tabella.getColumnModel().getColumn(1).setPreferredWidth(230);
                tabella.getColumnModel().getColumn(2).setPreferredWidth(80);
            } else {
                tabella.getColumnModel().getColumn(0).setPreferredWidth(150);
            }

            JScrollPane scroll = new JScrollPane(tabella);
            scroll.setPreferredSize(new Dimension(380, 500));
            resultsContainer.add(scroll);
        }

        // ⭐ BOTTONE: Torna alle fermate della linea (SOLO se contesto è "LINEA")
        if ("LINEA".equalsIgnoreCase(contesto) && rottaCorrente != null && direzioneCorrente != null) {
            JButton backBtn = new JButton("← Torna alle fermate della linea");
            backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            backBtn.addActionListener(e -> {
                clearResults();
                ripristinando = true;
                mostraFermateLinea(rottaCorrente, direzioneCorrente, tuttiITrips, tuttiGliStopTimes, tutteLeFermate);
            });
            resultsContainer.add(backBtn);
        }

        resultsContainer.revalidate();
        resultsContainer.repaint();
    }


    // Aggiorna il pannello "I Miei Preferiti" nel frame principale
    private void aggiornaPannelloPreferiti(String username) {
        if (username == null || username.isEmpty()) return;

        SwingUtilities.invokeLater(() -> {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (!(window instanceof JFrame frame)) return;

            Container content = frame.getContentPane();
            for (Component comp : content.getComponents()) {
                if (comp instanceof JLayeredPane lp) {
                    for (Component sub : lp.getComponents()) {
                        if (sub instanceof FavoritesPanel favPanel) {
                            favPanel.caricaPreferiti(username);
                            System.out.println(" Preferiti aggiornati per " + username);
                            return;
                        }
                    }
                }
            }
        });

    }
    public void setOnCloseListener(java.util.function.Consumer<Void> listener) {
        this.onCloseListener = listener;

    }
    public void mostraFermateLinea(Route rotta,
                                   Trip direzioneScelta,
                                   List<Trip> tuttiTrips,
                                   List<StopTime> stopTimes,
                                   List<Fermate> tutteLeFermate) {

        clearResults();

        // Crea mappa per ricerche O(1)
        Map<String, Fermate> fermatePerId = new HashMap<>();
        for (Fermate f : tutteLeFermate) {
            fermatePerId.put(f.getStopId(), f);
        }

        // Mostra waypoint di tutte le fermate della linea
        if (waypointDrawer != null) {
            waypointDrawer.clearWaypoints();
            Set<BusWaypoint> waypoints = new HashSet<>();

            List<StopTime> stopTrip = new ArrayList<>();
            for (StopTime st : stopTimes) {
                if (st.getTripId().equals(direzioneScelta.getTripId())) {
                    stopTrip.add(st);
                }
            }

            stopTrip.sort(Comparator.comparingInt(StopTime::getStopSequence));

            for (StopTime st : stopTrip) {
                Fermate f = fermatePerId.get(st.getStopId());
                if (f != null) {
                    GeoPosition pos = new GeoPosition(f.getStopLat(), f.getStopLon());
                    waypoints.add(new BusWaypoint(pos));
                }
            }

            if (!waypoints.isEmpty()) {
                waypointDrawer.addWaypoints(waypoints);
            }
        }

        // Titolo + bottone preferiti
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        topPanel.setOpaque(false);
        JLabel titolo = new JLabel("\"Linea " + rotta.getRouteShortName() + "\"");
        titolo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titolo.setOpaque(false);
        JButton favBtn = new JButton(" Preferiti");
        favBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        favBtn.addActionListener(e -> {
            String user = UserProfilePanel.getCurrentUsernameStatic();
            if (user == null) {
                JOptionPane.showMessageDialog(this, "Effettua il login per aggiungere ai preferiti.");
                return;
            }

            Favorite f = new Favorite(rotta.getRouteShortName(), "LINEA");
            if (UserManager.haPreferito(user, f)) {
                UserManager.rimuoviPreferito(user, f);
                JOptionPane.showMessageDialog(this, "Linea rimossa dai preferiti!");
            } else {
                UserManager.aggiungiPreferito(user, f);
                JOptionPane.showMessageDialog(this, "Linea aggiunta ai preferiti!");
            }

            aggiornaPannelloPreferiti(user);
        });

        topPanel.add(titolo);
        topPanel.add(favBtn);
        resultsContainer.add(topPanel);

        JLabel dirLabel = new JLabel("Direzione: \"" + direzioneScelta.getTripHeadsign() + "\"", SwingConstants.CENTER);
        dirLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        resultsContainer.add(dirLabel);

        List<StopTime> stopTrip = new ArrayList<>();
        for (StopTime st : stopTimes) {
            if (st.getTripId().equals(direzioneScelta.getTripId())) {
                stopTrip.add(st);
            }
        }

        stopTrip.sort(Comparator.comparingInt(StopTime::getStopSequence));

        String[] colonne = {"Fermata"};
        List<String[]> righe = new ArrayList<>();
        for (StopTime st : stopTrip) {
            Fermate f = fermatePerId.get(st.getStopId());
            if (f != null) {
                righe.add(new String[]{f.getStopName()});
            }
        }

        if (righe.isEmpty()) {
            JLabel noResult = new JLabel("Nessuna fermata trovata per questa direzione.", SwingConstants.CENTER);
            noResult.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            noResult.setForeground(Color.DARK_GRAY);
            resultsContainer.add(Box.createVerticalStrut(10));
            resultsContainer.add(noResult);
        } else {
            JTable tabella = new JTable(righe.toArray(new String[0][]), colonne);
            tabella.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            tabella.setRowHeight(22);
            tabella.setEnabled(true);

            tabella.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int riga = tabella.getSelectedRow();
                    if (riga >= 0 && onLineaStopClickListener != null) {
                        String nomeFermata = (String) tabella.getValueAt(riga, 0);
                        Fermate fermataSelezionata = fermatePerId.get(
                                tutteLeFermate.stream()
                                        .filter(f -> f.getStopName().equalsIgnoreCase(nomeFermata))
                                        .map(Fermate::getStopId)
                                        .findFirst()
                                        .orElse(null)
                        );

                        if (fermataSelezionata != null) {
                            System.out.println("Fermata cliccata: " + fermataSelezionata.getStopName());
                            onLineaStopClickListener.accept(fermataSelezionata);
                        }
                    }
                }
            });

            JScrollPane scroll = new JScrollPane(tabella);
            scroll.setPreferredSize(new Dimension(380, 500));
            resultsContainer.add(scroll);

            // ⭐ LISTENER: Quando clicchi su una fermata nella lista della linea
            setOnLineaStopClickListener(fermata -> {
                Map<String, Trip> tripMap = new HashMap<>(tuttiITrips.size());
                for (Trip t : tuttiITrips) {
                    tripMap.put(t.getTripId(), t);
                }

                // Filtra stopTimes per fermata e linea corrente
                List<StopTime> stopTimesLineaEFermata = new ArrayList<>();
                for (StopTime st : tuttiGliStopTimes) {
                    if (!st.getStopId().equals(fermata.getStopId())) continue;

                    Trip trip = tripMap.get(st.getTripId());
                    if (trip != null && trip.getRouteId().equals(rotta.getRouteId())) {
                        stopTimesLineaEFermata.add(st);
                    }
                }

                // Mostra orari per quella fermata + linea
                mostraOrariFermata(
                        fermata,
                        stopTimesLineaEFermata,
                        tuttiITrips,
                        List.of(rotta),
                        tutteLeFermate,
                        rotta,
                        direzioneScelta,
                        "LINEA"
                );
            });
        }

        JButton backBtn = new JButton("← Torna alle linee");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        backBtn.addActionListener(e -> {
            clearResults();
            ripristinando = true;
            if (!ultimeRotte.isEmpty()) {
                aggiornaRisultatiRotte(ultimeRotte);
            } else {
                JLabel msg = new JLabel("Nessuna linea precedente trovata.", SwingConstants.CENTER);
                msg.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                msg.setForeground(Color.DARK_GRAY);
                resultsContainer.add(msg);
                resultsContainer.revalidate();
                resultsContainer.repaint();
            }
            if (waypointDrawer != null) {
                waypointDrawer.clearWaypoints();
            }
        });

        resultsContainer.add(Box.createVerticalStrut(10));
        resultsContainer.add(backBtn);

        resultsContainer.revalidate();
        resultsContainer.repaint();
    }

}
