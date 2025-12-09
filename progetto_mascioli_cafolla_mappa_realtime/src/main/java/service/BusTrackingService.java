package service;

import model.gtfs.*;
import model.realtime.*;
import model.realtime.DelayInfo.DelayStatus;

import java.time.LocalTime;
import java.util.*;

/**
 * Service orchestratore che combina:
 * - Dati GTFS statici (orari programmati)
 * - GTFS Real-Time Trip Updates (ritardi)
 * - GTFS Real-Time Vehicle Positions (posizioni GPS)
 *
 * Per calcolare lo stato puntualità/ritardo/anticipo dei bus
 */
public class BusTrackingService {

    private final GtfsService gtfsService;
    private Map<String, TripUpdate> cachedTripUpdates;
    private long lastUpdateTime;
    private static final long CACHE_DURATION_MS = 30_000; // 30 secondi

    // ⭐ Set per tracciare i trip real-time già usati
    private Set<String> usedTripIds = new HashSet<>();

    public BusTrackingService(GtfsService gtfsService) {
        this.gtfsService = gtfsService;
        this.cachedTripUpdates = new HashMap<>();
        this.lastUpdateTime = 0;
    }

    /**
     * Resetta i trip usati prima di ogni ricerca
     */
    private void resetUsedTrips() {
        usedTripIds.clear();
    }

    /**
     * Trova il miglior TripUpdate controllando: linea, direzione, orario
     * Match SPECIFICO per evitare duplicati
     */
    private TripUpdate findBestMatchingTripUpdate(Trip staticTrip, String stopId, LocalTime scheduledTime) {
        String routeId = staticTrip.getRouteId();
        int directionId = staticTrip.getDirectionId();

        TripUpdate bestMatch = null;
        long minTimeDiff = Long.MAX_VALUE;

        // ⭐ Finestra massima (5 minuti)
        final long MAX_TIME_DIFF = 300; // 5 minuti in secondi

        System.out.println("[MATCH] Cerco trip per route=" + routeId +
                " dir=" + directionId + " orario=" + scheduledTime);
        int candidatesFound = 0;

        for (TripUpdate tu : cachedTripUpdates.values()) {
            String realtimeRouteId = tu.getRouteId();

            // Salta trip già usati
            if (usedTripIds.contains(tu.getTripId())) {
                continue;
            }

            // CONTROLLO 1: LINEA
            if (!realtimeRouteId.equals(routeId)) {
                continue;
            }

            // CONTROLLO 2: DIREZIONE
            if (tu.getDirectionId() != directionId) {
                continue;
            }

            StopTimeUpdate stopUpdate = tu.getUpdateForStop(stopId);
            if (stopUpdate == null) {
                continue;
            }

            long arrivalTime = stopUpdate.getArrivalTime();
            if (arrivalTime <= 0) {
                continue;
            }

            LocalTime realTimeArrival = java.time.Instant.ofEpochSecond(arrivalTime)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalTime();

            // CONTROLLO 3: ORARIO
            long scheduledSeconds = scheduledTime.toSecondOfDay();
            long realTimeSeconds = realTimeArrival.toSecondOfDay();
            long timeDiff = Math.abs(scheduledSeconds - realTimeSeconds);

            // ⭐ NUOVO: Salta se fuori dalla finestra temporale
            if (timeDiff > MAX_TIME_DIFF) {
                continue;
            }

            if (candidatesFound < 3) {
                System.out.println("  → Candidato: trip=" + tu.getTripId() +
                        " route=" + realtimeRouteId +
                        " dir=" + tu.getDirectionId() +
                        " orarioRT=" + realTimeArrival +
                        " diff=" + (timeDiff/60) + "min");
            }
            candidatesFound++;

            if (timeDiff < minTimeDiff) {
                minTimeDiff = timeDiff;
                bestMatch = tu;
            }
        }

        if (bestMatch != null) {
            System.out.println("  ✓ MATCH TROVATO: trip=" + bestMatch.getTripId() +
                    " diff=" + (minTimeDiff/60) + "min");
            // Marca come usato
            usedTripIds.add(bestMatch.getTripId());
        } else {
            System.out.println("  ✗ NESSUN MATCH (candidati: " + candidatesFound + ")");
        }

        return bestMatch;
    }

    /**
     * Calcola lo stato di ritardo per una fermata specifica
     * Combina orari programmati + trip updates real-time
     */
    public List<DelayInfo> getDelayInfoForStop(String stopId, int maxResults) {
        List<DelayInfo> delayInfos = new ArrayList<>();

        System.out.println("[BusTrackingService] getDelayInfoForStop chiamato per stop_id=" + stopId);
        System.out.println("[BusTrackingService] Trip updates in cache: " + cachedTripUpdates.size());

        // Resetta i trip usati
        resetUsedTrips();

        // Verifica connessione
        if (!ConnectivityService.isOnline()) {
            System.out.println("[BusTrackingService] Offline - nessun dato real-time");
            return delayInfos;
        }

        // Aggiorna cache se necessario
        refreshCacheIfNeeded();

        // Ottieni tutti gli StopTime per questa fermata
        Map<String, List<StopTime>> stopTimesMap = gtfsService.getStopTimesPerStopId();
        List<StopTime> stopTimes = stopTimesMap.get(stopId);

        if (stopTimes == null || stopTimes.isEmpty()) {
            System.out.println("[BusTrackingService] Nessun StopTime trovato per stop_id=" + stopId);
            return delayInfos;
        }

        System.out.println("[BusTrackingService] StopTimes trovati per stop_id=" + stopId + ": " + stopTimes.size());

        // Filtra per orario (prossimi 40 minuti)
        LocalTime now = LocalTime.now();
        LocalTime maxTime = now.plusMinutes(40);

        // Mappa per lookup O(1)
        Map<String, Trip> tripMap = new HashMap<>();
        for (Trip trip : gtfsService.getTrips()) {
            tripMap.put(trip.getTripId(), trip);
        }

        Map<String, Route> routeMap = new HashMap<>();
        for (Route route : gtfsService.getRotte()) {
            routeMap.put(route.getRouteId(), route);
        }

        // Contatori per log
        int processedCount = 0;
        int directMatchCount = 0;
        int prefixMatchCount = 0;
        int routeMatchCount = 0;

        // Processa ogni StopTime
        for (StopTime st : stopTimes) {
            LocalTime arrivalTime = st.getArrivalTime();

            if (arrivalTime == null) continue;
            if (arrivalTime.isBefore(now) || arrivalTime.isAfter(maxTime)) continue;

            Trip trip = tripMap.get(st.getTripId());
            if (trip == null) continue;

            Route route = routeMap.get(trip.getRouteId());
            if (route == null) continue;

            // Crea DelayInfo
            DelayInfo delayInfo = new DelayInfo(
                    stopId,
                    "", // Nome fermata verrà aggiunto dal chiamante
                    trip.getTripId(),
                    route.getRouteId(),
                    route.getRouteShortName(),
                    arrivalTime
            );

            // ⭐ Strategia di matching multi-livello
            String tripId = trip.getTripId();
            TripUpdate tripUpdate = cachedTripUpdates.get(tripId);
            String matchType = "NONE";

            if (tripUpdate != null) {
                directMatchCount++;
                matchType = "DIRECT";
            } else if (tripId.startsWith("1#")) {
                // Prova 1: Rimuovi "1#"
                String tripIdWithoutPrefix = tripId.substring(2);
                tripUpdate = cachedTripUpdates.get(tripIdWithoutPrefix);

                if (tripUpdate != null) {
                    prefixMatchCount++;
                    matchType = "NO_PREFIX";
                    if (prefixMatchCount <= 3) {
                        System.out.println("[BusTrackingService]   ✓ Match senza prefisso: " + tripId + " → " + tripIdWithoutPrefix);
                    }
                } else {
                    // Prova 2: Sostituisci "1#" con "0#"
                    String tripIdWith0Prefix = "0#" + tripIdWithoutPrefix;
                    tripUpdate = cachedTripUpdates.get(tripIdWith0Prefix);

                    if (tripUpdate != null) {
                        prefixMatchCount++;
                        matchType = "PREFIX_0";
                        if (prefixMatchCount <= 3) {
                            System.out.println("[BusTrackingService]   ✓ Match sostituendo prefisso: " + tripId + " → " + tripIdWith0Prefix);
                        }
                    }
                }
            }

            // Prova 3: Match per route + direction + time (fallback)
            if (tripUpdate == null) {
                tripUpdate = findBestMatchingTripUpdate(trip, stopId, arrivalTime);
                if (tripUpdate != null) {
                    routeMatchCount++;
                    matchType = "ROUTE";
                    if (routeMatchCount <= 3) {
                        System.out.println("[BusTrackingService]   ✓ Match per route+time: " + route.getRouteShortName() +
                                " (dir: " + trip.getDirectionId() + ", orario: " + arrivalTime + ")");
                    }
                }
            }

            // Log debug (solo primi 5 trip)
            if (processedCount < 5) {
                System.out.println("[BusTrackingService]   Trip " + tripId +
                        " → TripUpdate: " + matchType +
                        (tripUpdate != null ? " (stops: " + tripUpdate.getStopTimeUpdates().size() + ")" : ""));
            }

            if (tripUpdate != null) {
                StopTimeUpdate stopUpdate = tripUpdate.getUpdateForStop(stopId);

                // Log debug (solo primi 3 stop updates)
                if (processedCount < 3) {
                    System.out.println("[BusTrackingService]     → StopUpdate per stop_id=" + stopId + ": " +
                            (stopUpdate != null ? "FOUND (delay: " + stopUpdate.getArrivalDelay() + "s)" : "NOT FOUND"));
                }

                if (stopUpdate != null) {
                    // Abbiamo dati real-time!
                    if (stopUpdate.isSkipped()) {
                        delayInfo.setStatus(DelayStatus.SKIPPED);
                    } else {
                        delayInfo.setDelaySeconds(stopUpdate.getArrivalDelay());
                    }
                } else {
                    // Trip ha dati real-time ma non per questa fermata
                    delayInfo.setStatus(DelayStatus.NO_DATA);
                }
            } else {
                // Nessun dato real-time per questo trip
                delayInfo.setStatus(DelayStatus.NO_DATA);
            }

            delayInfos.add(delayInfo);
            processedCount++;

            if (delayInfos.size() >= maxResults) {
                break;
            }
        }

        System.out.println("[BusTrackingService] Processati " + processedCount + " StopTime, creati " +
                delayInfos.size() + " DelayInfo");
        System.out.println("[BusTrackingService] Match: direct=" + directMatchCount +
                ", prefix=" + prefixMatchCount + ", route=" + routeMatchCount);

        // Ordina per orario programmato
        delayInfos.sort(Comparator.comparing(DelayInfo::getScheduledTime));

        return delayInfos;
    }

    /**
     * Calcola lo stato di ritardo per tutte le fermate di una linea specifica
     */
    public Map<String, List<DelayInfo>> getDelayInfoForRoute(String routeId, int directionId) {
        Map<String, List<DelayInfo>> delaysByStop = new HashMap<>();

        if (!ConnectivityService.isOnline()) {
            return delaysByStop;
        }

        refreshCacheIfNeeded();

        // Resetta i trip usati
        resetUsedTrips();

        // Trova tutti i trip di questa route/direzione
        List<Trip> relevantTrips = new ArrayList<>();
        for (Trip trip : gtfsService.getTrips()) {
            if (trip.getRouteId().equals(routeId) &&
                    (directionId == -1 || trip.getDirectionId() == directionId)) {
                relevantTrips.add(trip);
            }
        }

        // Per ogni trip, calcola i delay per tutte le sue fermate
        for (Trip trip : relevantTrips) {
            List<StopTime> stopTimes = trip.getStopTimes();
            if (stopTimes == null) continue;

            for (StopTime st : stopTimes) {
                String stopId = st.getStopId();
                List<DelayInfo> stopDelays = delaysByStop.computeIfAbsent(stopId, k -> new ArrayList<>());

                // Cerca informazioni real-time con fuzzy matching
                String tripId = trip.getTripId();
                TripUpdate tripUpdate = cachedTripUpdates.get(tripId);

                // Prova strategie di matching
                if (tripUpdate == null && tripId.startsWith("1#")) {
                    String withoutPrefix = tripId.substring(2);
                    tripUpdate = cachedTripUpdates.get(withoutPrefix);

                    if (tripUpdate == null) {
                        tripUpdate = cachedTripUpdates.get("0#" + withoutPrefix);
                    }
                }

                // Fallback: match per route + time
                if (tripUpdate == null && st.getArrivalTime() != null) {
                    tripUpdate = findBestMatchingTripUpdate(trip, stopId, st.getArrivalTime());
                }

                DelayInfo delayInfo = new DelayInfo(
                        stopId,
                        "",
                        tripId,
                        routeId,
                        "",
                        st.getArrivalTime()
                );

                if (tripUpdate != null) {
                    StopTimeUpdate stopUpdate = tripUpdate.getUpdateForStop(stopId);
                    if (stopUpdate != null) {
                        delayInfo.setDelaySeconds(stopUpdate.getArrivalDelay());
                    }
                }

                stopDelays.add(delayInfo);
            }
        }

        return delaysByStop;
    }

    /**
     * Aggiorna la cache dei trip updates se è scaduta
     */
    private void refreshCacheIfNeeded() {
        long now = System.currentTimeMillis();

        if (now - lastUpdateTime > CACHE_DURATION_MS) {
            System.out.println("[BusTrackingService] Refresh cache trip updates...");
            cachedTripUpdates = GtfsRealtimeTripUpdateService.getTripUpdates();
            lastUpdateTime = now;
            System.out.println("[BusTrackingService] Cache aggiornata: " + cachedTripUpdates.size() + " trip updates");
        }
    }

    /**
     * Forza il refresh della cache
     */
    public void forceRefresh() {
        lastUpdateTime = 0;
        refreshCacheIfNeeded();
    }

    /**
     * Verifica se ci sono dati real-time per un trip specifico
     */
    public boolean hasRealtimeData(String tripId) {
        refreshCacheIfNeeded();

        // Cerca con tutte le strategie
        if (cachedTripUpdates.containsKey(tripId)) {
            return true;
        }

        if (tripId.startsWith("1#")) {
            String withoutPrefix = tripId.substring(2);
            if (cachedTripUpdates.containsKey(withoutPrefix)) {
                return true;
            }
            if (cachedTripUpdates.containsKey("0#" + withoutPrefix)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Ottiene statistiche sui ritardi per una route
     */
    public DelayStatistics getDelayStatistics(String routeId) {
        refreshCacheIfNeeded();

        int totalTrips = 0;
        int tripsWithDelay = 0;
        int totalDelaySeconds = 0;

        for (TripUpdate tu : cachedTripUpdates.values()) {
            if (!tu.getRouteId().equals(routeId)) continue;

            totalTrips++;
            int avgDelay = tu.getAverageDelay();

            if (avgDelay > 60) {
                tripsWithDelay++;
            }

            totalDelaySeconds += avgDelay;
        }

        return new DelayStatistics(totalTrips, tripsWithDelay,
                totalTrips > 0 ? totalDelaySeconds / totalTrips : 0);
    }

    /**
     * Classe interna per statistiche ritardi
     */
    public static class DelayStatistics {
        public final int totalTrips;
        public final int tripsWithDelay;
        public final int averageDelaySeconds;

        public DelayStatistics(int totalTrips, int tripsWithDelay, int averageDelaySeconds) {
            this.totalTrips = totalTrips;
            this.tripsWithDelay = tripsWithDelay;
            this.averageDelaySeconds = averageDelaySeconds;
        }

        public int getAverageDelayMinutes() {
            return Math.round(averageDelaySeconds / 60.0f);
        }

        public double getDelayPercentage() {
            return totalTrips > 0 ? (tripsWithDelay * 100.0 / totalTrips) : 0.0;
        }

        @Override
        public String toString() {
            return String.format("DelayStats{trips=%d, delayed=%d (%.1f%%), avgDelay=%dmin}",
                    totalTrips, tripsWithDelay, getDelayPercentage(), getAverageDelayMinutes());
        }
    }
}
