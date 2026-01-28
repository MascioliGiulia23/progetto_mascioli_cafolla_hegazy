
package service;

import model.gtfs.*;
import org.jxmapviewer.viewer.GeoPosition;
import view.map.BusWaypoint;
import view.map.RouteDrawer;
import view.map.WaypointDrawer;

import java.util.*;
import java.util.List;

// Classe di servizio che gestisce la logica di disegno e aggiornamento della mappa.

public class MapService {

    private final RouteDrawer routeDrawer;
    private final WaypointDrawer waypointDrawer;
    private final List<Fermate> fermate;
    private final List<Route> rotte;
    private final List<Trip> trips;
    private final List<StopTime> stopTimes;
    private final Map<String, ShapeRoute> forme;


    // COSTRUTTORE

    public MapService(RouteDrawer routeDrawer,
                      WaypointDrawer waypointDrawer,
                      List<Fermate> fermate,
                      List<Route> rotte,
                      List<Trip> trips,
                      List<StopTime> stopTimes,
                      Map<String, ShapeRoute> forme) {

        this.routeDrawer = routeDrawer;
        this.waypointDrawer = waypointDrawer;
        this.fermate = fermate;
        this.rotte = rotte;
        this.trips = trips;
        this.stopTimes = stopTimes;
        this.forme = forme;
    }


    // CREA I WAYPOINT ASSOCIATI A UN TRIP

    public List<BusWaypoint> creaWaypointsDaTrip(Trip trip) {
        List<BusWaypoint> waypoints = new ArrayList<>();

        for (StopTime st : stopTimes) {
            if (st.getTripId().equals(trip.getTripId())) {
                Fermate f = fermate.stream()
                        .filter(ff -> ff.getStopId().equals(st.getStopId()))
                        .findFirst()
                        .orElse(null);

                if (f != null) {
                    waypoints.add(new BusWaypoint(
                            new GeoPosition(f.getStopLat(), f.getStopLon())
                    ));
                }
            }
        }
        return waypoints;
    }


    // MOSTRA SOLO IL WAYPOINT DELLA FERMATA SELEZIONATA
    // (usato quando clicchi su una fermata)

    public void mostraWaypointLinee(Fermate fermata) {
        if (fermata == null) return;

        // Cancella tutto prima
        routeDrawer.clearAll();
        waypointDrawer.clearWaypoints();

        // Crea un solo waypoint per la fermata
        GeoPosition pos = new GeoPosition(fermata.getStopLat(), fermata.getStopLon());
        BusWaypoint wp = new BusWaypoint(pos);
        waypointDrawer.addWaypoint(wp);

        System.out.println(" Disegnato solo il waypoint per fermata: " + fermata.getStopName());
    }

    // MOSTRA I WAYPOINT DI TUTTE LE FERMATE DI UN TRIP (usato per LINEE)

    public void mostraWaypointFermate(Trip trip) {
        if (trip == null || trip.getStopTimes() == null) return;

        List<StopTime> stopTrip = trip.getStopTimes();
        stopTrip.sort(Comparator.comparingInt(StopTime::getStopSequence));

        Set<BusWaypoint> waypoints = new HashSet<>();
        for (StopTime st : stopTrip) {
            Fermate fermata = this.fermate.stream()
                    .filter(f -> f.getStopId().equals(st.getStopId()))
                    .findFirst()
                    .orElse(null);

            if (fermata != null) {
                GeoPosition pos = new GeoPosition(
                        fermata.getStopLat(),
                        fermata.getStopLon()
                );
                waypoints.add(new BusWaypoint(pos));
            }
        }

        // Disegna la linea e i waypoint della linea
        waypointDrawer.clearWaypoints();
        waypointDrawer.addWaypoints(waypoints);
        System.out.println("🟢 Disegnata la linea con " + waypoints.size() + " fermate");
    }
}
