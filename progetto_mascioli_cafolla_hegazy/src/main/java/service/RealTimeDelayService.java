package service;

import com.google.transit.realtime.GtfsRealtime.*;
import model.gtfs.*;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

// >>> NUOVE IMPORT (serve per i test)
import java.util.function.Function;   // serve per i test
import java.util.function.LongSupplier; // serve per i test
import java.util.function.Supplier;   // serve per i test

/**
 * Servizio per ottenere i ritardi real-time con cache intelligente e fallback
 * + Monitoraggio automatico qualità del servizio
 */
public class RealTimeDelayService {

    private final RealTimeFetcher fetcher;
    private final RealTimeParser parser;

    // >>> DIPENDENZE INIETTABILI (serve per i test)
    // Default = comportamento originale, quindi l'app NON cambia.
    private Supplier<byte[]> tripFeedFetcher;                 // serve per i test
    private Function<byte[], FeedMessage> tripFeedParser;     // serve per i test
    private LongSupplier clockMillis;                         // serve per i test

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
        // >>> DEFAULT per runtime (serve per i test)
        this.tripFeedFetcher = () -> { // serve per i test
            try {
                return fetcher.fetchTripFeed();
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        }; // serve per i test

        this.tripFeedParser = data -> { // serve per i test
            try {
                return parser.parseTripFeed(data);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }; // serve per i test

        this.clockMillis = System::currentTimeMillis; // serve per i test

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

    // >>> COSTRUTTORE PER TEST (serve per i test)
    // Non rompe nulla perché aggiunge solo un overload; l'app continua a usare il costruttore sopra.
    RealTimeDelayService(List<Trip> trips,
                         List<Route> routes,
                         List<StopTime> stopTimes,
                         Supplier<byte[]> tripFeedFetcher,
                         Function<byte[], FeedMessage> tripFeedParser,
                         LongSupplier clockMillis) { // serve per i test

        // Manteniamo fetcher/parser reali, ma non verranno usati se inietti dipendenze fittizie
        String tripUrl = "https://romamobilita.it/sites/default/files/rome_rtgtfs_trip_updates_feed.pb";
        String vehicleUrl = "https://romamobilita.it/sites/default/files/rome_rtgtfs_vehicle_positions_feed.pb";
        this.fetcher = new RealTimeFetcher(tripUrl, vehicleUrl);
        this.parser = new RealTimeParser();

        this.tripFeedFetcher = Objects.requireNonNull(tripFeedFetcher); // serve per i test
        this.tripFeedParser = Objects.requireNonNull(tripFeedParser);   // serve per i test
        this.clockMillis = Objects.requireNonNull(clockMillis);         // serve per i test

        this.tripsById = trips.stream()
                .collect(Collectors.toMap(Trip::getTripId, t -> t, (a, b) -> a));

        this.routesById = routes.stream()
                .collect(Collectors.toMap(Route::getRouteId, r -> r, (a, b) -> a));

        this.validTripStopPairs = stopTimes.stream()
                .map(st -> st.getTripId() + "#" + st.getStopId())
                .collect(Collectors.toSet());
    }


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
            FeedMessage feed = tripFeedParser.apply(tripData);

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

        } catch (RuntimeException e) {
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
            FeedMessage feed = tripFeedParser.apply(tripData);

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

        } catch (Exception e) {
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
        long now = clockMillis.getAsLong(); // ✅ clock iniettabile

        if (lastTripData == null || now - lastFetchEpochMillis > CACHE_MS) {
            try {
                lastTripData = tripFeedFetcher.get(); // ✅ fetch iniettabile
            } catch (RuntimeException e) {
                Throwable cause = e.getCause();
                if (cause instanceof IOException) throw (IOException) cause;
                if (cause instanceof InterruptedException) throw (InterruptedException) cause;
                throw e;
            }

            lastFetchEpochMillis = now;
            System.out.println("[RealTimeDelayService] >>> FETCH NUOVO");
        } else {
            System.out.println("[RealTimeDelayService] >>> CACHE RIUTILIZZATA");
        }

        return lastTripData;
    }



     // METODO HELPER: Ottiene delay medio per una linea

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
