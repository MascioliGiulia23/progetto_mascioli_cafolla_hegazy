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

import model.realtime.*;
import service.BusTrackingService;
import service.ConnectivityService;
import view.components.DelayBadge;

public class SearchResultsPanel extends JPanel {
    private JPanel resultsContainer;
    private JScrollPane scrollPane;
    private List<ResultItem> results;
    // METODO MOSTRA FERMATE INDIETRO
    private java.util.List<Fermate> ultimeFermate = new java.util.ArrayList<>();
    private java.util.List<Route> ultimeRotte = new java.util.ArrayList<>();

    private boolean ripristinando = false;

    private String currentTheme = "Blu";
    private BusTrackingService busTrackingService;
    private Map<String, DelayBadge> delayBadgesMap;
    private JButton closeButton;
    private java.util.function.Consumer<Void> onCloseListener;
    private JTable currentTable;

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

    public SearchResultsPanel() {
        results = new ArrayList<>();
        delayBadgesMap = new HashMap<>();
        initializeUI();
    }

    /**
     * Imposta il BusTrackingService
     */
    public void setBusTrackingService(BusTrackingService service) {
        this.busTrackingService = service;
    }

    /**
     * Aggiorna tutti i badge di ritardo visualizzati
     */
    public void aggiornaDelayInfo(BusTrackingService trackingService) {
        if (trackingService == null || delayBadgesMap.isEmpty()) {
            return;
        }

        System.out.println("[SearchResultsPanel] Aggiornamento " + delayBadgesMap.size() + " badge ritardo...");

        for (Map.Entry<String, DelayBadge> entry : delayBadgesMap.entrySet()) {
            DelayBadge badge = entry.getValue();
            DelayInfo oldInfo = badge.getDelayInfo();

            if (oldInfo != null) {
                List<DelayInfo> newInfos = trackingService.getDelayInfoForStop(
                        oldInfo.getStopId(), 20
                );

                for (DelayInfo newInfo : newInfos) {
                    if (newInfo.getTripId().equals(oldInfo.getTripId()) &&
                            newInfo.getScheduledTime().equals(oldInfo.getScheduledTime())) {
                        badge.setDelayInfo(newInfo);
                        break;
                    }
                }
            }
        }

        if (currentTable != null) {
            currentTable.repaint();
        }
        resultsContainer.revalidate();
        resultsContainer.repaint();

        System.out.println("[SearchResultsPanel] ✓ Badge aggiornati");
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
        delayBadgesMap.clear();


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

        LocalTime oraCorrente = LocalTime.now();
        LocalTime oraMax = oraCorrente.plusMinutes(40);

        for (StopTime st : stopTimes) {
            stopTimePerFermata.computeIfAbsent(st.getStopId(), k -> new ArrayList<>()).add(st);

            String tripId = st.getTripId();
            StopTime existing = ultimoStopPerTrip.get(tripId);
            if (existing == null || st.getStopSequence() > existing.getStopSequence()) {
                ultimoStopPerTrip.put(tripId, st);
            }
        }

        //PRE-CALCOLO CAPOLINEA
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

        // ───────────────────────
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
        JPanel idPanel = new JPanel(null); // layout nullo per poter usare setBounds
        idPanel.setOpaque(false);
        idPanel.setPreferredSize(new Dimension(400, 25)); // altezza fissa per ID

        JLabel idLabel = new JLabel("ID: " + fermata.getStopId());
        idLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        idPanel.add(idLabel);

    // regolare  posizione e dimensioni
        idLabel.setBounds(160, 0, 200, 20);

        resultsContainer.add(idPanel);

        // RACCOLTA DATI - Unico loop ottimizzato
        List<String[]> righe = new ArrayList<>();
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
                String chiave = nomeLinea + "|" + orarioFormattato;

                // Evita duplicati
                if (orariGiaAggiunti.add(chiave)) {
                    String capolineaNome = capolineaPerTrip.getOrDefault(trip.getTripId(), "?");
                    String direzione = " → " + capolineaNome;

                    // Solo orario statico programmato
                    righe.add(new String[]{nomeLinea, direzione, orarioFormattato});
                }
            }
        }


        righe.sort(Comparator.comparing(arr -> arr[2]));

        if (righe.isEmpty()) {
            JLabel noResult = new JLabel("Nessun arrivo nei prossimi 40 minuti.", SwingConstants.CENTER);
            noResult.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            noResult.setForeground(Color.DARK_GRAY);
            resultsContainer.add(Box.createVerticalStrut(10));
            resultsContainer.add(noResult);
        } else {
            // Se arrivo da "LINEA" → mostro solo la colonna Orario arrivo
            String[] colonne;
            Object[][] data;

            if ("LINEA".equalsIgnoreCase(contesto)) {
                colonne = new String[]{"Orario arrivo", "Stato"};
                data = new Object[righe.size()][2];


                for (int i = 0; i < righe.size(); i++) {
                    data[i][0] = righe.get(i)[2]; // solo orario
                    data[i][1] = "";
                }

                // ───────────────────────────────
                // Mostra intestazione con linea + direzione
                // ───────────────────────────────
                if (rottaCorrente != null && direzioneCorrente != null) {
                    JLabel infoLinea = new JLabel(
                            "Linea " + rottaCorrente.getRouteShortName() +
                                    " → " + direzioneCorrente.getTripHeadsign(),
                            SwingConstants.CENTER
                    );
                    infoLinea.setFont(new Font("Segoe UI", Font.BOLD, 14));
                    infoLinea.setForeground(new Color(50, 50, 50));

                    // Posizionamento manuale label linea

                    JPanel lineaPanel = new JPanel(null); // layout nullo per posizionamento manuale
                    lineaPanel.setOpaque(false);
                    lineaPanel.setPreferredSize(new Dimension(400, 40)); // altezza area dove la metti
                    lineaPanel.add(infoLinea);

                    // scegli tu le coordinate (x, y, larghezza, altezza)
                    infoLinea.setBounds(80, 10, 200, 25);

                    resultsContainer.add(lineaPanel);

                }

            } else {
                // caso normale (ricerca fermata) → mostro anche la linea e la direzione
                colonne = new String[]{"Linea", "Direzione", "Orario", "Stato"};
                data = new Object[righe.size()][4];

                for (int i = 0; i < righe.size(); i++) {
                    data[i][0] = righe.get(i)[0]; // linea
                    data[i][1] = righe.get(i)[1]; // direzione
                    data[i][2] = righe.get(i)[2]; // orario
                    data[i][3] = ""; // placeholder per badge
                }
            }

            JTable tabella = new JTable(data, colonne) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }

                @Override
                public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                    Component c = super.prepareRenderer(renderer, row, column);
                    // Colonna "Stato" → mostra DelayBadge
                    int statoColumn = ("LINEA".equalsIgnoreCase(contesto)) ? 1 : 3;

                    if (column == statoColumn) {
                        String orario = ("LINEA".equalsIgnoreCase(contesto))
                                ? (String) data[row][0]
                                : (String) data[row][2];

                        DelayBadge badge = delayBadgesMap.get(orario);
                        if (badge != null) {
                            return badge;
                        } else {
                            return DelayBadge.createLoadingBadge();
                        }
                    }
                    c.setForeground(Color.BLACK);
                    return c;
                }
            };

            tabella.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            tabella.setRowHeight(30);
            this.currentTable = tabella;
            if (colonne.length == 4) {
                tabella.getColumnModel().getColumn(0).setPreferredWidth(50);
                tabella.getColumnModel().getColumn(1).setPreferredWidth(180);
                tabella.getColumnModel().getColumn(2).setPreferredWidth(70);
                tabella.getColumnModel().getColumn(3).setPreferredWidth(80);
            } else {
                tabella.getColumnModel().getColumn(0).setPreferredWidth(150);
                tabella.getColumnModel().getColumn(1).setPreferredWidth(100);
            }

            JScrollPane scroll = new JScrollPane(tabella);
            scroll.setPreferredSize(new Dimension(380, 500));
            resultsContainer.add(scroll);

            caricaDelayInfoPerFermata(fermata, stopTimes, trips, rotte, righe, contesto);

        }

        JButton backBtn = new JButton("← Torna alle fermate della linea");
        backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        backBtn.addActionListener(e -> {
            clearResults();
            ripristinando = true;

            if (rottaCorrente != null && direzioneCorrente != null) {
                // Ritorna alla lista fermate della linea
                mostraFermateLinea(
                        rottaCorrente,
                        direzioneCorrente,
                        tuttiITrips,
                        tuttiGliStopTimes,
                        tutteLeFermate
                );
            } else {
                // fallback di sicurezza
                aggiornaRisultati(ultimeFermate);
            }
        });
        resultsContainer.add(backBtn);


        resultsContainer.revalidate();
        resultsContainer.repaint();
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

        // ───────────────────────
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
            tabella.setRowHeight(30);
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
            // Quando clicchi su una fermata nella lista della linea,
// mostra SOLO gli orari relativi a quella fermata e a quella linea
            // Versione ottimizzata: nessun ciclo inutile
            setOnLineaStopClickListener(fermata -> {
                //  Prepara mappe statiche solo se non già costruite
                Map<String, Trip> tripMap = new HashMap<>(tuttiTrips.size());
                for (Trip t : tuttiTrips) {
                    tripMap.put(t.getTripId(), t);
                }

                // Filtra gli stopTimes una sola volta con lookup diretto
                List<StopTime> stopTimesLineaEFermata = new ArrayList<>();
                for (StopTime st : stopTimes) {
                    if (!st.getStopId().equals(fermata.getStopId())) continue;

                    Trip trip = tripMap.get(st.getTripId());
                    if (trip != null && trip.getRouteId().equals(rotta.getRouteId())) {
                        stopTimesLineaEFermata.add(st);
                    }
                }

                // Mostra solo gli orari relativi alla fermata e linea corrente
                mostraOrariFermata(
                        fermata,
                        stopTimesLineaEFermata,
                        tuttiTrips,
                        List.of(rotta),
                        tutteLeFermate,
                        rotta,              // serve per il "torna alla linea"
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
    /**
     * Carica i DelayInfo in background e aggiorna i badge
     */
    private void caricaDelayInfoPerFermata(Fermate fermata,
                                           List<StopTime> stopTimes,
                                           List<Trip> trips,
                                           List<Route> rotte,
                                           List<String[]> righe,
                                           String contesto) {

        if (busTrackingService == null) {
            System.out.println("[SearchResultsPanel] BusTrackingService non disponibile");
            return;
        }

        if (!ConnectivityService.isOnline()) {
            System.out.println("[SearchResultsPanel] Offline - nessun delay disponibile");
            return;
        }

        SwingWorker<List<DelayInfo>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<DelayInfo> doInBackground() {
                return busTrackingService.getDelayInfoForStop(fermata.getStopId(), 20);
            }

            @Override
            protected void done() {
                try {
                    List<DelayInfo> delayInfos = get();

                    Map<String, DelayInfo> delayMap = new HashMap<>();
                    DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");

                    for (DelayInfo info : delayInfos) {
                        String orario = info.getScheduledTime().format(fmt);
                        delayMap.put(orario, info);
                        System.out.println("[DEBUG] DelayInfo: " + orario +
                                " | Status: " + info.getStatus() +
                                " | Delay: " + info.getDelayMinutes() + " min");
                    }

                    System.out.println("[DEBUG] Orari nella tabella:");
                    for (String[] riga : righe) {
                        String orario = ("LINEA".equalsIgnoreCase(contesto)) ? riga[0] : riga[2];
                        System.out.println("[DEBUG]   - " + orario);

                    }

                    for (String[] riga : righe) {
                        String orario = ("LINEA".equalsIgnoreCase(contesto)) ? riga[0] : riga[2];

                        DelayInfo delayInfo = delayMap.get(orario);
                        DelayBadge badge;

                        if (delayInfo != null) {
                            badge = new DelayBadge(delayInfo);
                        } else {
                            badge = DelayBadge.createNoDataBadge();
                        }

                        delayBadgesMap.put(orario, badge);
                    }
                    if (currentTable != null) {
                        currentTable.repaint();
                        System.out.println("[SearchResultsPanel] ✓ Tabella aggiornata con " + delayBadgesMap.size() + " badge");
                    }
                    resultsContainer.revalidate();
                    resultsContainer.repaint();

                    System.out.println("[SearchResultsPanel] ✓ Caricati " + delayInfos.size() + " delay info");

                } catch (Exception e) {
                    System.err.println("[SearchResultsPanel] Errore caricamento delay: " + e.getMessage());
                }
            }
        };

        worker.execute();
    }
}


