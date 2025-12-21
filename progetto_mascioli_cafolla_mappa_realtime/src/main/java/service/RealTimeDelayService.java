package service;

import com.google.transit.realtime.GtfsRealtime.*;
import model.gtfs.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Servizio per ottenere i ritardi real-time con cache intelligente e fallback
 * + Monitoraggio automatico qualità del servizio
 */
public class RealTimeDelayService {

    private final RealTimeFetcher fetcher;
    private final RealTimeParser parser;

    // CACHE: Riusa i dati per 30 secondi
    private byte[] lastTripData;
    private long lastFetchEpochMillis = 0;
    private static final long CACHE_MS = 30_000; // 30 secondi

    // MAPPE per lookup O(1)
    private final Map<String, Trip> tripsById;
    private final Map<String, Route> routesById;
    private final Set<String> validTripStopPairs;

    // CACHE DEI DELAY PER TRIP (fallback)
    private final Map<String, Integer> delayByTrip = new HashMap<>();

    /**
     * Costruttore: inizializza con i dati GTFS statici
     */
    public RealTimeDelayService(List<Trip> trips,
                                List<Route> routes,
                                List<StopTime> stopTimes) {

        // URL ATAC ufficiali
        String tripUrl = "https://romamobilita.it/sites/default/files/rome_rtgtfs_trip_updates_feed.pb";
        String vehicleUrl = "https://romamobilita.it/sites/default/files/rome_rtgtfs_vehicle_positions_feed.pb";

        this.fetcher = new RealTimeFetcher(tripUrl, vehicleUrl);
        this.parser = new RealTimeParser();

        // ⭐ CREA MAPPE PER LOOKUP VELOCE
        this.tripsById = trips.stream()
                .collect(Collectors.toMap(Trip::getTripId, t -> t, (a, b) -> a));

        this.routesById = routes.stream()
                .collect(Collectors.toMap(Route::getRouteId, r -> r, (a, b) -> a));

        // ⭐ COPPIE VALIDE (trip_id#stop_id) dallo statico
        this.validTripStopPairs = stopTimes.stream()
                .map(st -> st.getTripId() + "#" + st.getStopId())
                .collect(Collectors.toSet());

        System.out.println("[RealTimeDelayService] ✓ Inizializzato");
        System.out.println("[RealTimeDelayService]   → " + tripsById.size() + " trip");
        System.out.println("[RealTimeDelayService]   → " + validTripStopPairs.size() + " coppie trip/stop valide");
    }

    /**
     * ⭐ SETTER: Collega il monitor di qualità
     */


    /**
     * ⭐ METODO PRINCIPALE: Ottiene delay per fermata
     * Restituisce: routeShortName → List<Integer> (delay in secondi)
     */
    public Map<String, List<Integer>> getAllDelaysForStop(String stopId) {
        Map<String, List<Integer>> delays = new HashMap<>();

        byte[] tripData;
        try {
            tripData = getTripDataCached();
        } catch (Exception e) {
            System.err.println("[RealTimeDelayService] ✗ Errore fetch: " + e.getMessage());
            return delays;
        }

        try {
            FeedMessage feed = parser.parseTripFeed(tripData);
            System.out.println("[RealTimeDelayService] Feed entities: " + feed.getEntityCount());

            // ⭐ PASSO 1: Raccogli delay aggregati per trip (fallback)
            delayByTrip.clear();
            for (FeedEntity entity : feed.getEntityList()) {
                if (!entity.hasTripUpdate()) continue;

                TripUpdate tripUpdate = entity.getTripUpdate();
                String tripId = tripUpdate.getTrip().getTripId();

                if (tripId == null || tripId.isEmpty()) continue;

                for (TripUpdate.StopTimeUpdate stu : tripUpdate.getStopTimeUpdateList()) {
                    int delaySeconds = 0;

                    if (stu.hasArrival() && stu.getArrival().hasDelay()) {
                        delaySeconds = stu.getArrival().getDelay();
                    } else if (stu.hasDeparture() && stu.getDeparture().hasDelay()) {
                        delaySeconds = stu.getDeparture().getDelay();
                    } else {
                        continue;
                    }

                    delayByTrip.put(tripId, delaySeconds);
                    break; // Uno per trip basta
                }
            }

            System.out.println("[RealTimeDelayService] Delay raccolti per " + delayByTrip.size() + " trip");

            // ⭐ PASSO 2: Cerca delay specifici per la fermata
            for (FeedEntity entity : feed.getEntityList()) {
                if (!entity.hasTripUpdate()) continue;

                TripUpdate tripUpdate = entity.getTripUpdate();
                String tripId = tripUpdate.getTrip().getTripId();

                if (tripId == null || tripId.isEmpty()) continue;

                Trip staticTrip = tripsById.get(tripId);
                if (staticTrip == null) continue;

                Route route = routesById.get(staticTrip.getRouteId());
                if (route == null || route.getRouteShortName() == null) continue;

                String routeShortName = route.getRouteShortName();
                boolean foundExactStop = false;

                // ⭐ CERCA DELAY ESATTO PER QUESTA FERMATA
                for (TripUpdate.StopTimeUpdate stu : tripUpdate.getStopTimeUpdateList()) {
                    if (!stu.hasStopId() || !stopId.equals(stu.getStopId())) continue;

                    int delaySeconds = 0;
                    if (stu.hasArrival() && stu.getArrival().hasDelay()) {
                        delaySeconds = stu.getArrival().getDelay();
                    } else if (stu.hasDeparture() && stu.getDeparture().hasDelay()) {
                        delaySeconds = stu.getDeparture().getDelay();
                    }

                    delays.computeIfAbsent(routeShortName, k -> new ArrayList<>())
                            .add(delaySeconds);

                    foundExactStop = true;

                    System.out.printf("[RealTimeDelayService] ✓ Match esatto: stop=%s trip=%s route=%s delay=%ds%n",
                            stopId, tripId, routeShortName, delaySeconds);
                }

                // ⭐ FALLBACK: Se non trova delay specifico, usa delay del trip
                if (!foundExactStop && delayByTrip.containsKey(tripId)) {
                    // ⭐ VERIFICA CHE LA COPPIA TRIP/STOP SIA VALIDA
                    String pairKey = tripId + "#" + stopId;
                    if (validTripStopPairs.contains(pairKey)) {
                        int delaySeconds = delayByTrip.get(tripId);
                        delays.computeIfAbsent(routeShortName, k -> new ArrayList<>())
                                .add(delaySeconds);

                        System.out.printf("[RealTimeDelayService] ⚠ Fallback: stop=%s trip=%s route=%s delay=%ds%n",
                                stopId, tripId, routeShortName, delaySeconds);
                    }
                }
            }

        } catch (IOException e) {
            System.err.println("[RealTimeDelayService] ✗ Errore parse: " + e.getMessage());
        }

        System.out.println("[RealTimeDelayService] Risultato finale: " + delays.size() + " linee con delay");
        return delays;
    }

    /**
     * ⭐ METODO CON MONITORAGGIO QUALITÀ: Ottiene delay per route+orario+stop
     * Restituisce: "linea#orario" → delay in secondi
     * + Registra automaticamente nel monitor qualità
     */
    public Map<String, Integer> getDelaysByTripId(String stopId) {
        Map<String, Integer> delaysByLineaOrario = new HashMap<>();

        // Per il monitor qualità: stopName
        String stopName = "N/A"; // Verrà sovrascritto se disponibile

        byte[] tripData;
        try {
            tripData = getTripDataCached();
        } catch (Exception e) {
            System.err.println("[RealTimeDelayService] ✗ Errore fetch: " + e.getMessage());
            return delaysByLineaOrario;
        }

        try {
            FeedMessage feed = parser.parseTripFeed(tripData);
            System.out.println("[RealTimeDelayService] Feed entities: " + feed.getEntityCount());
            System.out.println("[RealTimeDelayService] === MATCHING PER STOP " + stopId + " ===");

            for (FeedEntity entity : feed.getEntityList()) {
                if (!entity.hasTripUpdate()) continue;

                TripUpdate tripUpdate = entity.getTripUpdate();

                // ⭐ Ottieni route_id dal feed RT
                String routeIdRT = tripUpdate.getTrip().hasRouteId() ?
                        tripUpdate.getTrip().getRouteId() : null;

                if (routeIdRT == null) continue;

                // ⭐ Converti route_id in route_short_name (numero linea)
                Route route = routesById.get(routeIdRT);
                if (route == null) continue;

                String nomeLinea = route.getRouteShortName();
                if (nomeLinea == null) continue;

                // ⭐ CERCA DELAY PER QUESTA FERMATA
                for (TripUpdate.StopTimeUpdate stu : tripUpdate.getStopTimeUpdateList()) {
                    if (!stu.hasStopId() || !stopId.equals(stu.getStopId())) continue;

                    int delaySeconds = 0;
                    long scheduledTime = 0;

                    if (stu.hasArrival()) {
                        if (stu.getArrival().hasDelay()) {
                            delaySeconds = stu.getArrival().getDelay();
                        }
                        if (stu.getArrival().hasTime()) {
                            scheduledTime = stu.getArrival().getTime();
                        }
                    } else if (stu.hasDeparture()) {
                        if (stu.getDeparture().hasDelay()) {
                            delaySeconds = stu.getDeparture().getDelay();
                        }
                        if (stu.getDeparture().hasTime()) {
                            scheduledTime = stu.getDeparture().getTime();
                        }
                    }

                    if (scheduledTime > 0) {
                        // ⭐ CALCOLA ORARIO PROGRAMMATO (RT fornisce orario effettivo!)
                        long scheduledTimeCorretto = scheduledTime - delaySeconds;
                        java.time.LocalTime orarioProgrammato = java.time.Instant
                                .ofEpochSecond(scheduledTimeCorretto)
                                .atZone(java.time.ZoneId.of("Europe/Rome"))
                                .toLocalTime();

                        String orarioProgrammatoStr = orarioProgrammato.format(
                                java.time.format.DateTimeFormatter.ofPattern("HH:mm"));

                        // ⭐ USA ORARIO PROGRAMMATO PER MATCHING
                        String chiave = nomeLinea + "#" + orarioProgrammatoStr;

                        delaysByLineaOrario.put(chiave, delaySeconds);

                        System.out.printf("[RealTimeDelayService] ✓ Linea %s @ stop %s, orario programmato %s: %d sec%n",
                                nomeLinea, stopId, orarioProgrammatoStr, delaySeconds);




                    }
                }
            }

        } catch (IOException e) {
            System.err.println("[RealTimeDelayService] ✗ Errore parse: " + e.getMessage());
        }


        System.out.println("[RealTimeDelayService] Delay trovati per " +
                delaysByLineaOrario.size() + " combinazioni linea/orario");
        return delaysByLineaOrario;
    }

    /**
     * ⭐ CACHE: Riusa i dati per 30 secondi
     */
    private byte[] getTripDataCached() throws IOException, InterruptedException {
        long now = System.currentTimeMillis();

        if (lastTripData == null || now - lastFetchEpochMillis > CACHE_MS) {
            lastTripData = fetcher.fetchTripFeed();
            lastFetchEpochMillis = now;
            System.out.println("[RealTimeDelayService] >>> FETCH NUOVO");
        } else {
            System.out.println("[RealTimeDelayService] >>> CACHE RIUTILIZZATA");
        }

        return lastTripData;
    }

    /**
     * ⭐ METODO HELPER: Ottiene delay medio per una linea
     */
    public int getAverageDelay(String stopId, String routeShortName) {
        Map<String, List<Integer>> delays = getAllDelaysForStop(stopId);

        List<Integer> routeDelays = delays.get(routeShortName);
        if (routeDelays == null || routeDelays.isEmpty()) {
            return 0;
        }

        int sum = routeDelays.stream().mapToInt(Integer::intValue).sum();
        return sum / routeDelays.size(); // media in secondi
    }
}
