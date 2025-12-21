package view.frames;

import model.gtfs.*;
import model.user.UserManager;
import model.utils.Database;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import view.panels.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
//import classi
import service.MapService;
import service.GtfsService;
import service.RealTimeDelayService;
import controller.MapController;
import view.map.BusWaypoint;
import view.map.RouteDrawer;
import view.map.WaypointDrawer;


public class Mappa extends JFrame {
    private JXMapViewer mapViewer;
    private JLayeredPane layeredPane;
    private SearchBar searchBar;
    private SearchResultsPanel resultsPanel;
    private TopRightPanel topRightPanel;
    private FavoritesPanel favoritesPanel;
    private UserProfilePanel userProfilePanel;
    private SettingsPanel settingsPanel;
    private String currentTheme = SettingsPanel.COLOR_BLU;

    //per tornare indietro
    private Route currentSelectedRoute = null;
    private Trip currentSelectedTrip = null;

    // Dati GTFS
    private List<Fermate> fermate;
    private List<Route> rotte;
    private Map<String, ShapeRoute> forme;
    private List<Trip> trips;
    private List<StopTime> stopTimes;
    private Map<String, List<StopTime>> stopTimesPerStopId;  // indice per fermata

    private Map<String, CalendarDate> calendari;
    private List<CalendarDate> eccezioni;
    private RouteDrawer routeDrawer;
    private WaypointDrawer waypointDrawer;
    private MapService mapService;
    private MapController mapController;
    private RealTimeDelayService delayService;
    private ServiceQualityPanel qualityPanel;

    public Mappa() {
        super("Roma Bus Tracker");


        UserManager.caricaUtenti();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Caricamento dati GTFS tramite GtfsService
        GtfsService gtfsService = new GtfsService();
        fermate = gtfsService.getFermate();
        rotte = gtfsService.getRotte();
        forme = gtfsService.getForme();
        trips = gtfsService.getTrips();
        stopTimes = gtfsService.getStopTimes();
        eccezioni = gtfsService.getEccezioni();
        stopTimesPerStopId = gtfsService.getStopTimesPerStopId();

        // Setup mappa di base
        mapViewer = MapInitializer.creaMappaBase();


        // Layer principale e pannelli grafici
        layeredPane = new JLayeredPane();
        layeredPane.setLayout(null);
        mapViewer.setBounds(0, 0, getWidth(), getHeight());
        layeredPane.add(mapViewer, JLayeredPane.DEFAULT_LAYER);

        searchBar = new SearchBar();
        searchBar.setBounds(30, 30, 400, 60);
        layeredPane.add(searchBar, JLayeredPane.PALETTE_LAYER);

        resultsPanel = new SearchResultsPanel();
        resultsPanel.setBounds(30, 100, 400, 600);
        resultsPanel.setVisible(false);
        layeredPane.add(resultsPanel, JLayeredPane.PALETTE_LAYER);

        resultsPanel.setOnCloseListener(v -> {
            resultsPanel.setVisible(false);
            resultsPanel.clearResults();
            searchBar.clearSearch();  // ← AGGIUNGI QUESTA RIGA
            if (waypointDrawer != null) waypointDrawer.clearWaypoints();
            if (routeDrawer != null) routeDrawer.clearAll();

            if (mapController != null) {
                mapController.fermaAggiornamentoRealtimeBus();
            }
        });


        topRightPanel = new TopRightPanel();
        topRightPanel.setBounds(getWidth() - 500, 15, 480, 55);
        layeredPane.add(topRightPanel, JLayeredPane.PALETTE_LAYER);

        favoritesPanel = new FavoritesPanel();
        favoritesPanel.setBounds(getWidth() - 410, 100, 380, 400);
        favoritesPanel.setVisible(false);
        layeredPane.add(favoritesPanel, JLayeredPane.PALETTE_LAYER);

        userProfilePanel = new UserProfilePanel();
        userProfilePanel.setBounds(getWidth() - 410, 100, 380, 400);
        userProfilePanel.setVisible(false);
        layeredPane.add(userProfilePanel, JLayeredPane.PALETTE_LAYER);

        settingsPanel = new SettingsPanel();
        settingsPanel.setBounds(getWidth() - 410, 100, 380, 400);
        settingsPanel.setVisible(false);
        layeredPane.add(settingsPanel, JLayeredPane.PALETTE_LAYER);


        // Bottoni e pannelli
        setupButtonListeners();
        userProfilePanel.setOnLoginListener(() -> {
            String usernameAutenticato = userProfilePanel.getCurrentUsername();
            System.out.println("Login detected: " + usernameAutenticato);
            if (usernameAutenticato != null) {
                favoritesPanel.caricaPreferiti();
            }
        });

        // Servizi e controller
        routeDrawer = new RouteDrawer(mapViewer);
        waypointDrawer = new WaypointDrawer(mapViewer, routeDrawer);
        mapService = new MapService(routeDrawer, waypointDrawer, fermate, rotte, trips, stopTimes, forme);

        // CREAZIONE CONTROLLER PRIMA DEI LISTENER
        mapController = new MapController(
                mapViewer,
                mapService,
                routeDrawer,
                waypointDrawer,
                resultsPanel,
                fermate,
                rotte,
                trips,
                stopTimes,
                forme

        );
        // ⭐ INIZIALIZZAZIONE SERVIZIO RITARDI REAL-TIME
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("Inizializzazione servizio ritardi real-time...");
        System.out.println("═══════════════════════════════════════════════");
        delayService = new RealTimeDelayService(trips, rotte, stopTimes);

// ⭐ PASSA IL SERVIZIO AL PANNELLO RISULTATI
        resultsPanel.setDelayService(delayService);
        System.out.println("✓ Servizio ritardi collegato al pannello risultati");

// ⭐ INIZIALIZZA PANNELLO QUALITÀ CONTESTUALE
        qualityPanel = new ServiceQualityPanel(delayService);  // ✅ NUOVO: passa delayService
        qualityPanel.setBounds(getWidth() - 410, 100, 380, 500);
        qualityPanel.setVisible(false);
        layeredPane.add(qualityPanel, JLayeredPane.PALETTE_LAYER);
        qualityPanel.setOnCloseListener(v -> qualityPanel.setVisible(false));

// ⭐ COLLEGA DASHBOARD AL RESULTS PANEL
        resultsPanel.setQualityPanel(qualityPanel);

        System.out.println("✓ Dashboard qualità contestuale inizializzata");
        System.out.println("═══════════════════════════════════════════════");


        // Configura i pannelli con i drawer
        resultsPanel.setRouteDrawer(routeDrawer, forme);
        resultsPanel.setWaypointDrawer(waypointDrawer, fermate, rotte, trips, stopTimes);


        // Listener di ricerca (mapController è inizializzato )
        setupSearchListener();

        // Listener per i preferiti
        favoritesPanel.setOnFavoriteClickListener(preferito -> {
            System.out.println("✓ Preferito cliccato: " + preferito.getNome() + " (" + preferito.getTipo() + ")");
            resultsPanel.clearResults();

            if (preferito.getTipo().equals("FERMATA")) {
                List<Fermate> fermateTrovate = Database.ricercaFermatePerNome(fermate, preferito.getNome());
                if (!fermateTrovate.isEmpty()) {
                    resultsPanel.aggiornaRisultati(fermateTrovate);
                    resultsPanel.setVisible(true);
                    favoritesPanel.setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(this, "Fermata non trovata nel database!");
                }
            } else if (preferito.getTipo().equals("LINEA")) {
                List<Route> rotteTrovate = Database.ricercaRottePerNome(rotte, preferito.getNome());
                if (!rotteTrovate.isEmpty()) {
                    resultsPanel.aggiornaRisultatiRotte(rotteTrovate);
                    resultsPanel.setVisible(true);
                    favoritesPanel.setVisible(false);
                } else {
                    JOptionPane.showMessageDialog(this, "Linea non trovata nel database!");
                }
            }
        });

        // Final UI setup
        add(layeredPane, BorderLayout.CENTER);
        setVisible(true);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }



    private void setupSearchListener() {
        searchBar.setOnSearchListener(e -> {
            String testo = searchBar.getSearchText().trim();
            System.out.println("Ricerca attivata: " + testo);

            if (testo.isEmpty()) return;

            resultsPanel.clearResults();
            boolean trovataFermata = false;
            boolean trovataRotta = false;

            // Se la ricerca è numerica → controllo anche per ID
            if (testo.matches("\\d+")) {
                System.out.println("→ Ricerca numerica: controllo ID fermata e ID linea");

                // 1 Cerca fermata con stop_id uguale
                Fermate fermataTrovata = fermate.stream()
                        .filter(f -> f.getStopId().equals(testo))
                        .findFirst()
                        .orElse(null);

                if (fermataTrovata != null) {
                    trovataFermata = true;
                    System.out.println("✓ Fermata trovata per ID: " + fermataTrovata.getStopName());
                    resultsPanel.aggiornaRisultati(List.of(fermataTrovata));
                }

                // 2 Cerca linea con route_id uguale
                Route rottaTrovata = rotte.stream()
                        .filter(r -> r.getRouteId().equals(testo))
                        .findFirst()
                        .orElse(null);

                if (rottaTrovata != null) {
                    trovataRotta = true;
                    System.out.println("✓ Linea trovata per ID: " + rottaTrovata.getRouteShortName());
                    resultsPanel.aggiornaRisultatiRotte(List.of(rottaTrovata));
                }

                // 3 Se non trova nulla come ID → cerca per nome numerico
                if (!trovataFermata && !trovataRotta) {
                    List<Route> rotteTrovate = Database.ricercaRottePerNome(rotte, testo);
                    trovataRotta = !rotteTrovate.isEmpty();
                    resultsPanel.aggiornaRisultatiRotte(rotteTrovate);
                }

            } else {
                // Ricerca testuale (nome)
                System.out.println("→ Ricerca testuale: cerco fermate e linee per nome");

                List<Fermate> fermateTrovate = Database.ricercaFermatePerNome(fermate, testo);
                List<Route> rotteTrovate = Database.ricercaRottePerNome(rotte, testo);

                trovataFermata = !fermateTrovate.isEmpty();
                trovataRotta = !rotteTrovate.isEmpty();

                // MOSTRA PRIMA LE FERMATE
                if (trovataFermata) {
                    resultsPanel.aggiornaRisultati(fermateTrovate);
                }

                // POI AGGIUNGI LE LINEE (senza cancellare)
                if (trovataRotta) {
                    resultsPanel.aggiungiRisultatiRotte(rotteTrovate); // ← USA IL NUOVO METODO
                }
            }

            if (!trovataFermata && !trovataRotta) {
                resultsPanel.addResult("Nessun risultato", "Nessuna fermata o linea trovata", "", null);
            }

            resultsPanel.setVisible(true);
        });

        // Listener già presenti — non modificare
        resultsPanel.setOnStopClickListener(fermata -> {
            resultsPanel.mostraOrariFermata(fermata, stopTimes, trips, rotte, fermate, null, null, "RICERCA");
            mapService.mostraWaypointLinee(fermata);
        });

        resultsPanel.setOnRouteClickListener(mapController::mostraLinea);

        resultsPanel.setOnLineaStopClickListener(fermata -> {
            System.out.println("Fermata cliccata: " + fermata.getStopName());
            GeoPosition pos = new GeoPosition(fermata.getStopLat(), fermata.getStopLon());
            mapViewer.setAddressLocation(pos);
            mapViewer.setZoom(6);

            waypointDrawer.clearWaypoints();
            waypointDrawer.addWaypoints(java.util.List.of(new BusWaypoint(pos)));

            Route lineaSelezionata = currentSelectedRoute;
            if (lineaSelezionata == null) {
                System.out.println("Nessuna linea selezionata. Impossibile filtrare gli orari.");
                return;
            }

            List<Trip> tripsLinea = new ArrayList<>();
            for (Trip t : trips) {
                if (t.getRouteId().equals(lineaSelezionata.getRouteId())) {
                    tripsLinea.add(t);
                }
            }

            List<StopTime> stopTimesFermata =
                    stopTimesPerStopId.getOrDefault(fermata.getStopId(), Collections.emptyList());

            List<StopTime> stopTimesLinea = new ArrayList<>();
            for (StopTime st : stopTimesFermata) {
                for (Trip t : tripsLinea) {
                    if (st.getTripId().equals(t.getTripId())) {
                        stopTimesLinea.add(st);
                        break;
                    }
                }
            }

            resultsPanel.clearResults();
            resultsPanel.mostraOrariFermata(
                    fermata, stopTimesLinea, tripsLinea,
                    List.of(lineaSelezionata), fermate,
                    null, null, "RICERCA"
            );
        });
    }


    private void setupButtonListeners() {
        topRightPanel.getFavoritesButton().addActionListener(e -> {
            if (favoritesPanel.isVisible()) {
                favoritesPanel.setVisible(false);
            } else {
                favoritesPanel.caricaPreferiti();
                favoritesPanel.setVisible(true);
                userProfilePanel.setVisible(false);
                settingsPanel.setVisible(false);
            }
        });

        topRightPanel.getUserButton().addActionListener(e -> {
            if (userProfilePanel.isVisible()) {
                userProfilePanel.setVisible(false);
            } else {
                userProfilePanel.setVisible(true);
                favoritesPanel.setVisible(false);
                settingsPanel.setVisible(false);
            }
        });

        topRightPanel.getSettingsButton().addActionListener(e -> {
            if (settingsPanel.isVisible()) {
                settingsPanel.setVisible(false);
            } else {
                settingsPanel.setVisible(true);
                favoritesPanel.setVisible(false);
                userProfilePanel.setVisible(false);
            }
        });

        settingsPanel.setOnSaveListener(e -> {
            String newTheme = settingsPanel.getSelectedColor();
            if (!newTheme.equals(currentTheme)) {
                currentTheme = newTheme;
                applyTheme(currentTheme);
                JOptionPane.showMessageDialog(this, "Impostazioni salvate!", "Successo", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        topRightPanel.getQualityButton().addActionListener(e -> {
            if (qualityPanel.isVisible()) {
                qualityPanel.setVisible(false);
            } else {
                // La dashboard si aggiorna automaticamente quando cerchi fermata
                qualityPanel.setVisible(true);
                favoritesPanel.setVisible(false);
                userProfilePanel.setVisible(false);
                settingsPanel.setVisible(false);
            }
        });

    }

    private void applyTheme(String colorTheme) {
        searchBar.updateTheme(colorTheme);
        resultsPanel.updateTheme(colorTheme);
        topRightPanel.updateTheme(colorTheme);
        favoritesPanel.updateTheme(colorTheme);
        userProfilePanel.updateTheme(colorTheme);
        settingsPanel.updateTheme(colorTheme);
        qualityPanel.updateTheme(colorTheme);
        layeredPane.repaint();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (mapViewer != null && layeredPane != null) {
            mapViewer.setBounds(0, 0, layeredPane.getWidth(), layeredPane.getHeight());

            if (topRightPanel != null) {
                topRightPanel.setBounds(layeredPane.getWidth() - 500, 15, 480, 55);
            }
            if (favoritesPanel != null) {
                favoritesPanel.setBounds(layeredPane.getWidth() - 410, 100, 380, 400);
            }
            if (userProfilePanel != null) {
                userProfilePanel.setBounds(layeredPane.getWidth() - 410, 100, 380, 400);
            }
            if (settingsPanel != null) {
                settingsPanel.setBounds(layeredPane.getWidth() - 410, 100, 380, 400);
            }

            if (qualityPanel != null) {
                qualityPanel.setBounds(layeredPane.getWidth() - 410, 100, 380, 500);

            }

        }
    }
    @Override
    public void dispose() {
        if (mapController != null) {
            mapController.fermaAggiornamentoRealtimeBus();
        }
        super.dispose();
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Mappa frame = new Mappa();
        });
    }
}
