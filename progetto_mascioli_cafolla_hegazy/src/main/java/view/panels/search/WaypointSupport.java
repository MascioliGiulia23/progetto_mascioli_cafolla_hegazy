package view.panels.search;

import model.gtfs.Fermate;
import model.gtfs.Route;
import model.gtfs.StopTime;
import model.gtfs.Trip;
import org.jxmapviewer.viewer.GeoPosition;
import view.map.BusWaypoint;
import view.map.WaypointDrawer;

import java.util.*;

public class WaypointSupport {

    /**
     * Disegna (o aggiorna) il waypoint della singola fermata in "mostraOrariFermata".
     * Replica 1:1 la logica che hai ora:
     * - clearWaypoints()
     * - controlla che esista almeno un trip valido + rotta valida per quella fermata
     * - se sì, aggiunge 1 waypoint.
     */
    public void updateStopWaypoint(WaypointDrawer waypointDrawer,
                                   Fermate fermata,
                                   Map<String, Trip> tripMap,
                                   Map<String, Route> routeMap,
                                   Map<String, List<StopTime>> stopTimePerFermata) {

        if (waypointDrawer == null || fermata == null) return;

        waypointDrawer.clearWaypoints();
        Set<BusWaypoint> waypoints = new HashSet<>();

        List<StopTime> fermataStops = stopTimePerFermata.get(fermata.getStopId());
        if (fermataStops == null) return;

        boolean hasValidTrip = false;
        for (StopTime st : fermataStops) {
            Trip t = tripMap.get(st.getTripId());
            if (t != null && routeMap.containsKey(t.getRouteId())) {
                hasValidTrip = true;
                break;
            }
        }

        if (hasValidTrip) {
            GeoPosition pos = new GeoPosition(fermata.getStopLat(), fermata.getStopLon());
            waypoints.add(new BusWaypoint(pos));
            waypointDrawer.addWaypoints(waypoints);
        }
    }

    /**
     * Disegna i waypoint di tutte le fermate di una linea (per quella direzione/trip).
     * Replica 1:1 la logica che avevi in mostraFermateLinea:
     * - clearWaypoints()
     * - filtra stopTimes per tripId della direzione
     * - ordina per stopSequence
     * - aggiunge waypoint per ogni fermata trovata.
     */
    public void updateLineWaypoints(WaypointDrawer waypointDrawer,
                                    Trip direzioneScelta,
                                    List<StopTime> stopTimes,
                                    List<Fermate> tutteLeFermate) {

        if (waypointDrawer == null || direzioneScelta == null || stopTimes == null || tutteLeFermate == null) return;

        waypointDrawer.clearWaypoints();
        Set<BusWaypoint> waypoints = new HashSet<>();

        Map<String, Fermate> fermatePerId = new HashMap<>(tutteLeFermate.size());
        for (Fermate f : tutteLeFermate) {
            fermatePerId.put(f.getStopId(), f);
        }

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

    /**
     * Utility semplice se vuoi richiamarla quando premi "torna indietro"
     * (replica waypointDrawer.clearWaypoints()).
     */
    public void clear(WaypointDrawer waypointDrawer) {
        if (waypointDrawer != null) waypointDrawer.clearWaypoints();
    }
}
