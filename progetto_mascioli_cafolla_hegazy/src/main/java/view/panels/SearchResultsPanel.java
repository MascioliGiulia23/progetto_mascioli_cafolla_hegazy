package view.panels;

import view.panels.search.SearchItem;
import view.panels.search.SearchResultsView;
import view.panels.search.StopScheduleEngine;
import view.panels.search.LineStopsViewBuilder;
import view.panels.search.FavoritesSupport;
import view.panels.search.WaypointSupport;
import view.panels.search.ServiceQualitySupport;

import model.gtfs.*;
import model.user.Favorite;
import model.user.UserManager;
import view.map.RouteDrawer;
import view.map.WaypointDrawer;

import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SearchResultsPanel extends JPanel {
    private List<SearchItem> results;

    private SearchResultsView view;
    private StopScheduleEngine stopScheduleEngine;
    private LineStopsViewBuilder lineStopsViewBuilder;
    private WaypointSupport waypointSupport;
    private ServiceQualitySupport serviceQualitySupport;

    // METODO MOSTRA FERMATE INDIETRO
    private java.util.List<Fermate> ultimeFermate = new java.util.ArrayList<>();
    private java.util.List<Route> ultimeRotte = new java.util.ArrayList<>();
    private boolean ripristinando = false;
    private String currentTheme = "Blu";

    private java.util.function.Consumer<Void> onCloseListener;
    private java.util.function.Consumer<Fermate> onStopClickListener; // listener per fermate
    private java.util.function.Consumer<Route> onRouteClickListener; // listener per rotte
    private java.util.function.Consumer<Fermate> onLineaStopClickListener;    // listener per click su fermata nella lista linea

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
    private ServiceQualityPanel qualityPanel;

    public SearchResultsPanel() {
        results = new ArrayList<>();
        initializeUI();
        setPreferredSize(new Dimension(550, 600));
    }

    public void setDelayService(service.RealTimeDelayService service) {
        this.delayService = service;
        if (stopScheduleEngine == null) stopScheduleEngine = new StopScheduleEngine(service);
        else stopScheduleEngine.setDelayService(service);
    }

    public void setQualityPanel(ServiceQualityPanel panel) {
        this.qualityPanel = panel;
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

        view = new SearchResultsView();
        lineStopsViewBuilder = new LineStopsViewBuilder();
        waypointSupport = new WaypointSupport();
        serviceQualitySupport = new ServiceQualitySupport();

        // collega la X al tuo listener esistente
        view.getCloseButton().addActionListener(e -> {
            if (onCloseListener != null) {
                onCloseListener.accept(null);
            }
        });

        add(view, BorderLayout.CENTER);
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
        SearchItem item = new SearchItem(title, description, icon, currentTheme, fermata);
        item.setOnClickListener(() -> {
            if (fermata != null && onStopClickListener != null) {
                onStopClickListener.accept(fermata);
            }
        });
        results.add(item);
        view.getResultsContainer().add(item);
    }

    // Pulisce la lista
    public void clearResults() {
        view.getResultsContainer().removeAll();
        results.clear();
    }

    // Aggiorna i colori
    public void updateTheme(String theme) {
        this.currentTheme = theme;
        for (SearchItem item : results) {
            item.updateTheme(theme);
        }
        repaint();
    }

    //  Mostra le fermate
    public void aggiornaRisultati(List<Fermate> fermate) {
        clearResults();
        if (fermate == null || fermate.isEmpty()) {
            view.getResultsContainer().revalidate();
            view.getResultsContainer().repaint();
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

        view.getResultsContainer().revalidate();
        view.getResultsContainer().repaint();
    }

    // Mostra le linee (rotte)
    public void aggiornaRisultatiRotte(java.util.List<Route> rotte) {
        clearResults();
        if (rotte == null || rotte.isEmpty()) {
            view.getResultsContainer().revalidate();
            view.getResultsContainer().repaint();
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

            SearchItem item = new SearchItem(nomeLinea, descrizione, "", currentTheme, null);
            item.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));

            item.setOnClickListener(() -> {
                if (onRouteClickListener != null) {
                    onRouteClickListener.accept(r);
                }
            });

            results.add(item);
            view.getResultsContainer().add(item);
        }
        view.getResultsContainer().revalidate();
        view.getResultsContainer().repaint();
    }

    // Aggiunge linee SENZA cancellare i risultati precedenti
    public void aggiungiRisultatiRotte(java.util.List<Route> rotte) {
        if (rotte == null || rotte.isEmpty()) {
            return;
        }

        // NON chiamare clearResults() qui!

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
                if (nomeUpper.matches("M[EABCD]")) {
                    tipo = "METRO";
                } else if (nomeUpper.matches("(RM|G2|BUS5|BUS8|BUS14|BUS19|NAV1|93BUS|3BIS|2BIS|3D|3S|8)")) {
                    tipo = "TRAM";
                }
            }

            String descrizione = "ID: " + r.getRouteId() + " (" + tipo + ")";
            SearchItem item = new SearchItem(nomeLinea, descrizione, "", currentTheme, null);
            item.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 14));
            item.setOnClickListener(() -> {
                if (onRouteClickListener != null) {
                    onRouteClickListener.accept(r);
                }
            });

            results.add(item);
            view.getResultsContainer().add(item);
        }

        view.getResultsContainer().revalidate();
        view.getResultsContainer().repaint();
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

        if (stopScheduleEngine == null) {
            stopScheduleEngine = new StopScheduleEngine(delayService);
        }

        // Mappe hash per lookups O(1)
        Map<String, Trip> tripMap = new HashMap<>(trips.size());
        Map<String, Route> routeMap = new HashMap<>(rotte.size());
        Map<String, Fermate> fermatePerId = new HashMap<>(tutteLeFermate.size());

        for (Trip trip : trips) tripMap.put(trip.getTripId(), trip);
        for (Route route : rotte) routeMap.put(route.getRouteId(), route);
        for (Fermate f : tutteLeFermate) fermatePerId.put(f.getStopId(), f);

        // Indice stopTimes per fermata (serve per WAYPOINTS)
        Map<String, List<StopTime>> stopTimePerFermata = new HashMap<>();

        for (StopTime st : stopTimes) {
            stopTimePerFermata.computeIfAbsent(st.getStopId(), k -> new ArrayList<>()).add(st);
        }

        // WAYPOINTS - Loop minimo
        if (waypointSupport == null) waypointSupport = new WaypointSupport();
        waypointSupport.updateStopWaypoint(
                waypointDrawer,
                fermata,
                tripMap,
                routeMap,
                stopTimePerFermata
        );
        // Titolo + bottone preferiti
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 3));
        topPanel.setOpaque(false);
        JLabel titolo = new JLabel("\"" + fermata.getStopName() + "\" (ID: " + fermata.getStopId() + ")");
        titolo.setFont(new Font("Segoe UI", Font.BOLD, 14));
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

            FavoritesSupport.refreshFavoritesPanel(this, user);

        });

        topPanel.add(titolo);
        topPanel.add(favBtn);
        view.getResultsContainer().add(topPanel);

        List<String[]> righeTabella = stopScheduleEngine.calcolaRigheTabella(
                fermata,
                stopTimes,
                trips,
                rotte,
                tutteLeFermate
        );

        view.getResultsContainer().add(Box.createVerticalStrut(-5));

        if (righeTabella.isEmpty()) {
            JLabel noResult = new JLabel("Nessun arrivo nei prossimi 40 minuti.", SwingConstants.CENTER);
            noResult.setFont(new Font("Segoe UI", Font.ITALIC, 13));
            noResult.setForeground(Color.DARK_GRAY);

            view.getResultsContainer().add(noResult);

        } else {
            // Se arrivo da "LINEA" → mostro solo la colonna Orario arrivo
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
                    JLabel infoLinea = new JLabel("Linea " + rottaCorrente.getRouteShortName() + " → \"" +
                            direzioneCorrente.getTripHeadsign() + "\"", SwingConstants.CENTER);
                    infoLinea.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    infoLinea.setForeground(new Color(50, 50, 50));

                    JPanel lineaPanel = new JPanel(new BorderLayout());
                    lineaPanel.setOpaque(false);
                    lineaPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
                    lineaPanel.add(infoLinea, BorderLayout.CENTER);
                    view.getResultsContainer().add(lineaPanel);
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
                tabella.getColumnModel().getColumn(0).setPreferredWidth(40);
                tabella.getColumnModel().getColumn(1).setPreferredWidth(200);
                tabella.getColumnModel().getColumn(2).setPreferredWidth(100);
            } else {
                tabella.getColumnModel().getColumn(0).setPreferredWidth(150);
            }

            JScrollPane scroll = new JScrollPane(tabella);
            scroll.setPreferredSize(new Dimension(360, 300));
            view.getResultsContainer().add(scroll);

            if (qualityPanel != null && data.length > 0) {
                if (serviceQualitySupport == null) serviceQualitySupport = new ServiceQualitySupport();

                serviceQualitySupport.updateQualityPanel(
                        qualityPanel,
                        fermata,
                        colonne,
                        data,
                        rottaCorrente,
                        direzioneCorrente
                );
            }

        }

        // BOTTONE: Torna alle fermate della linea (SOLO se contesto è "LINEA")
        if ("LINEA".equalsIgnoreCase(contesto) && rottaCorrente != null && direzioneCorrente != null) {
            JButton backBtn = new JButton("← Torna alle fermate della linea");
            backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            backBtn.addActionListener(e -> {
                clearResults();
                ripristinando = true;
                mostraFermateLinea(rottaCorrente, direzioneCorrente, tuttiITrips, tuttiGliStopTimes, tutteLeFermate);
            });
            view.getResultsContainer().add(backBtn);
        }
        if ("RICERCA".equalsIgnoreCase(contesto)) {
            JButton backBtn = new JButton("← Torna alle fermate");
            backBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            backBtn.addActionListener(e -> {
                clearResults();
                ripristinando = true;
                if (!ultimeFermate.isEmpty()) {
                    aggiornaRisultati(ultimeFermate);
                } else {
                    JLabel msg = new JLabel("Nessuna fermata precedente.", SwingConstants.CENTER);
                    view.getResultsContainer().add(msg);
                    view.getResultsContainer().revalidate();
                    view.getResultsContainer().repaint();
                }
            });
            view.getResultsContainer().add(backBtn);
        }

        view.getResultsContainer().revalidate();
        view.getResultsContainer().repaint();
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

        if (lineStopsViewBuilder == null) {
            lineStopsViewBuilder = new LineStopsViewBuilder();
        }
        if (waypointSupport == null) waypointSupport = new WaypointSupport();
        waypointSupport.updateLineWaypoints(
                waypointDrawer,
                direzioneScelta,
                stopTimes,
                tutteLeFermate
        );


        lineStopsViewBuilder.build(
                view.getResultsContainer(),
                waypointDrawer,
                rotta,
                direzioneScelta,
                stopTimes,
                tutteLeFermate,
                fermata -> {
                    Map<String, Trip> tripMap = new HashMap<>(tuttiITrips.size());
                    for (Trip t : tuttiITrips) tripMap.put(t.getTripId(), t);

                    List<StopTime> stopTimesLineaEFermata = new ArrayList<>();
                    for (StopTime st : tuttiGliStopTimes) {
                        if (!st.getStopId().equals(fermata.getStopId())) continue;

                        Trip trip = tripMap.get(st.getTripId());
                        if (trip != null && trip.getRouteId().equals(rotta.getRouteId())) {
                            stopTimesLineaEFermata.add(st);
                        }
                    }

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
                },
                () -> {
                    clearResults();
                    ripristinando = true;
                    if (!ultimeRotte.isEmpty()) {
                        aggiornaRisultatiRotte(ultimeRotte);
                    } else {
                        JLabel msg = new JLabel("Nessuna linea precedente trovata.", SwingConstants.CENTER);
                        msg.setFont(new Font("Segoe UI", Font.ITALIC, 13));
                        msg.setForeground(Color.DARK_GRAY);
                        view.getResultsContainer().add(msg);
                        view.getResultsContainer().revalidate();
                        view.getResultsContainer().repaint();
                    }
                },
                () -> togglePreferitoLinea(rotta)
        );


        view.getResultsContainer().revalidate();
        view.getResultsContainer().repaint();
    }

    private void togglePreferitoLinea(Route rotta) {
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

        FavoritesSupport.refreshFavoritesPanel(this, user);
    }
}
