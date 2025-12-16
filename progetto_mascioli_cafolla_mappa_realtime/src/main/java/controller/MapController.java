package controller;

import model.gtfs.*;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.viewer.GeoPosition;
import service.*;
import view.map.BusWaypoint;
import view.map.RouteDrawer;
import view.map.WaypointDrawer;
import view.panels.SearchResultsPanel;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;


public class MapController {

    private final JXMapViewer mapViewer;
    private final MapService mapService;
    private final RouteDrawer routeDrawer;
    private final WaypointDrawer waypointDrawer;
    private final SearchResultsPanel resultsPanel;

    private final List<Fermate> fermate;
    private final List<Route> rotte;
    private final List<Trip> trips;
    private final List<StopTime> stopTimes;
    private final Map<String, ShapeRoute> forme;

    private Route currentSelectedRoute;
    private Trip currentSelectedTrip;

    // Timer per aggiornamento real-time
    private Timer realtimeBusTimer;

    public MapController(JXMapViewer mapViewer,
                         MapService mapService,
                         RouteDrawer routeDrawer,
                         WaypointDrawer waypointDrawer,
                         SearchResultsPanel resultsPanel,
                         List<Fermate> fermate,
                         List<Route> rotte,
                         List<Trip> trips,
                         List<StopTime> stopTimes,
                         Map<String, ShapeRoute> forme) {
        this.mapViewer = mapViewer;
        this.mapService = mapService;
        this.routeDrawer = routeDrawer;
        this.waypointDrawer = waypointDrawer;
        this.resultsPanel = resultsPanel;
        this.fermate = fermate;
        this.rotte = rotte;
        this.trips = trips;
        this.stopTimes = stopTimes;
        this.forme = forme;
    }

    public void mostraLinea(Route rotta) {
        currentSelectedRoute = rotta;
        System.out.println("Linea selezionata: " + rotta.getRouteShortName());

        List<Trip> tripsLinea = new ArrayList<>();
        for (Trip t : trips) {
            if (t.getRouteId().equals(rotta.getRouteId())) {
                tripsLinea.add(t);
            }
        }

        if (tripsLinea.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Nessun trip trovato per questa linea.");
            return;
        }

        Map<String, List<StopTime>> stopTimesPerTrip = new HashMap<>();
        for (StopTime st : stopTimes) {
            stopTimesPerTrip
                    .computeIfAbsent(st.getTripId(), k -> new ArrayList<>())
                    .add(st);
        }

        Trip dir0 = null, dir1 = null;
        double maxDist0 = -1, maxDist1 = -1;

        for (Trip t : tripsLinea) {
            List<StopTime> stopTrip = stopTimesPerTrip.get(t.getTripId());
            if (stopTrip == null || stopTrip.size() < 2) continue;

            stopTrip.sort(Comparator.comparingInt(StopTime::getStopSequence));

            Fermate start = trovaFermata(stopTrip.get(0).getStopId());
            Fermate end = trovaFermata(stopTrip.get(stopTrip.size() - 1).getStopId());
            if (start == null || end == null) continue;

            double dist = model.utils.GeoUtils.distanzaKm(
                    start.getStopLat(), start.getStopLon(),
                    end.getStopLat(), end.getStopLon());

            if (t.getDirectionId() == 0 && dist > maxDist0) {
                maxDist0 = dist;
                dir0 = t;
            } else if (t.getDirectionId() == 1 && dist > maxDist1) {
                maxDist1 = dist;
                dir1 = t;
            }
        }

        List<Trip> direzioni = new ArrayList<>();
        if (dir0 != null) direzioni.add(dir0);
        if (dir1 != null) direzioni.add(dir1);

        if (direzioni.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Nessuna direzione trovata per questa linea.");
            return;
        }

        if (direzioni.size() == 1) {
            disegnaDirezione(rotta, direzioni.get(0), Color.RED);
            return;
        }

        scegliDirezioneEVisualizza(rotta, direzioni);
    }

    private Fermate trovaFermata(String stopId) {
        for (Fermate f : fermate) {
            if (f.getStopId().equals(stopId)) return f;
        }
        return null;
    }

    private void scegliDirezioneEVisualizza(Route rotta, List<Trip> direzioni) {
        String[] opzioni = new String[direzioni.size()];
        for (int i = 0; i < direzioni.size(); i++) {
            Trip t = direzioni.get(i);
            List<StopTime> stopTrip = t.getStopTimes();
            stopTrip.sort(Comparator.comparingInt(StopTime::getStopSequence));

            Fermate start = trovaFermata(stopTrip.get(0).getStopId());
            Fermate end = trovaFermata(stopTrip.get(stopTrip.size() - 1).getStopId());
            opzioni[i] = (start != null && end != null)
                    ? start.getStopName() + " → " + end.getStopName()
                    : "Direzione " + t.getDirectionId();
        }

        Object scelta = JOptionPane.showInputDialog(
                null,
                "Scegli la direzione della linea " + rotta.getRouteShortName() + ":",
                "Seleziona direzione",
                JOptionPane.PLAIN_MESSAGE,
                null,
                opzioni,
                opzioni[0]);

        if (scelta != null) {
            int index = Arrays.asList(opzioni).indexOf(scelta.toString());
            if (index >= 0) {
                Trip scelto = direzioni.get(index);
                Color colore = (scelto.getDirectionId() == 0) ? Color.RED : Color.BLUE;
                disegnaDirezione(rotta, scelto, colore);
            }
        }
    }

    private void disegnaDirezione(Route rotta, Trip trip, Color colore) {
        ShapeRoute shape = forme.get(trip.getShapeId());
        if (shape == null) return;

        routeDrawer.clearAll();
        routeDrawer.drawShape(shape, colore);

        waypointDrawer.clearWaypoints();
        waypointDrawer.addWaypoints(mapService.creaWaypointsDaTrip(trip));
        mapService.mostraWaypointFermate(trip);

        resultsPanel.setVisible(true);
        resultsPanel.mostraFermateLinea(rotta, trip, trips, stopTimes, fermate);

        currentSelectedTrip = trip;

        System.out.println("✓ Direzione disegnata: " + trip.getTripId());
        mapViewer.setZoom(6);
        GeoPosition center = mapViewer.getAddressLocation();
        mapViewer.setAddressLocation(new GeoPosition(center.getLatitude() - 0.01, center.getLongitude() - 0.025));

        // ⭐ AVVIA AGGIORNAMENTO REAL-TIME DEI BUS
        avviaAggiornamentoRealtimeBus(rotta, trip);

    }

    /**
     * Avvia il timer per aggiornare le posizioni dei bus ogni 30 secondi
     */
    private void avviaAggiornamentoRealtimeBus(Route rotta, Trip trip) {
        // Ferma il timer precedente se esiste
        fermaAggiornamentoRealtimeBus();

        // Verifica connessione
        if (!service.ConnectivityService.isOnline()) {
            System.out.println("[Real-time] Utente offline, aggiornamento disabilitato");
            return;
        }

        System.out.println("[Real-time] Avvio aggiornamento bus per route=" +
                rotta.getRouteId() + " direction=" + trip.getDirectionId());

        // Carica immediatamente i bus
        aggiornaPosizoniBus(rotta, trip);

        // Timer per aggiornamento ogni 30 secondi
        realtimeBusTimer = new Timer(30000, e -> {
            if (service.ConnectivityService.isOnline()) {
                aggiornaPosizoniBus(rotta, trip);
            } else {
                System.out.println("[Real-time] Connessione persa, skip aggiornamento");
            }
        });
        realtimeBusTimer.start();
    }

    /**
     * Aggiorna le posizioni dei bus sulla mappa
     */
    private void aggiornaPosizoniBus(Route rotta, Trip trip) {
        System.out.println("[Real-time] Aggiornamento posizioni bus...");

        // Scarica posizioni veicoli filtrate per route e direction
        List<GtfsRealtimeVehicleService.VehicleData> vehicles =
                GtfsRealtimeVehicleService.getVehiclesForRouteAndDirection(
                        rotta.getRouteId(),
                        trip.getDirectionId()
                );

        // Rimuovi i bus precedenti
        waypointDrawer.clearRealtimeBusWaypoints();

        // Aggiungi i nuovi bus
        List<BusWaypoint> busWaypoints = new ArrayList<>();
        for (GtfsRealtimeVehicleService.VehicleData vehicle : vehicles) {
            BusWaypoint busWp = new BusWaypoint(
                    vehicle.toGeoPosition(),
                    vehicle.getBearing(),
                    vehicle.getVehicleId()
            );
            busWaypoints.add(busWp);
        }

        waypointDrawer.addWaypoints(busWaypoints);

        System.out.println("[Real-time] ✓ " + busWaypoints.size() + " bus aggiornati sulla mappa");
    }

    /**
     * Ferma l'aggiornamento real-time dei bus
     */
    public void fermaAggiornamentoRealtimeBus() {
        if (realtimeBusTimer != null && realtimeBusTimer.isRunning()) {
            realtimeBusTimer.stop();
            System.out.println("[Real-time] Timer fermato");
        }
    }

}


