package service;

import com.google.transit.realtime.GtfsRealtime;
import model.realtime.StopTimeUpdate;
import model.realtime.TripUpdate;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Service per recuperare gli aggiornamenti GTFS Real-Time sui trip (ritardi/anticipi)
 * Utilizza il feed trip_updates di Roma Mobilità
 */
public class GtfsRealtimeTripUpdateService {

    // Feed ufficiale Roma Mobilità per trip updates
    private static final String TRIP_UPDATES_URL =
            "https://romamobilita.it/sites/default/files/rome_rtgtfs_trip_updates_feed.pb";

    private static final int TIMEOUT_MS = 15000; // 15 secondi
    private static final int MAX_RETRIES = 3;

    /**
     * Recupera tutti i trip updates dal feed real-time
     * @return Mappa tripId -> TripUpdate
     */
    public static Map<String, TripUpdate> getTripUpdates() {
        Map<String, TripUpdate> tripUpdates = new HashMap<>();

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                if (attempt > 1) {
                    System.out.println("[GtfsRealtimeTripUpdateService] Tentativo " + attempt + "/" + MAX_RETRIES + "...");
                }

                // Crea connessione HTTP con timeout e headers
                URL url = new URL(TRIP_UPDATES_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(TIMEOUT_MS);
                connection.setReadTimeout(TIMEOUT_MS);
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (RomaBusTracker)");
                connection.setRequestProperty("Accept", "application/x-protobuf");

                int responseCode = connection.getResponseCode();
                if (responseCode != 200) {
                    System.err.println("[GtfsRealtimeTripUpdateService] HTTP " + responseCode + " - Retry...");
                    Thread.sleep(1000 * attempt); // Backoff esponenziale
                    continue;
                }

                // ⭐ CHIAVE: Leggi TUTTO lo stream in memoria PRIMA di parsare
                InputStream inputStream = connection.getInputStream();
                byte[] data = inputStream.readAllBytes();
                inputStream.close();
                connection.disconnect();

                System.out.println("[GtfsRealtimeTripUpdateService] Scaricati " + data.length + " bytes");

                // ⭐ Ora parsa dal byte array (non dallo stream che può essere interrotto)
                GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(data);

                System.out.println("[GtfsRealtimeTripUpdateService] Feed parsato: " + feed.getEntityCount() + " entities");

                // ⭐ DEBUG: Mostra primi 10 trip_id
                int debugCount = 0;
                System.out.println("[GtfsRealtimeTripUpdateService] === PRIMI 10 TRIP_ID NEL FEED ===");

                // Processa le entities
                for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
                    if (!entity.hasTripUpdate()) continue;

                    GtfsRealtime.TripUpdate tu = entity.getTripUpdate();

                    // Estrai info trip
                    if (!tu.hasTrip()) continue;

                    String tripId = tu.getTrip().getTripId();

                    // ⭐ LOG DEBUG (solo primi 10)
                    if (debugCount < 10) {
                        System.out.println("[GtfsRealtimeTripUpdateService]   → \"" + tripId + "\"");
                        debugCount++;
                    }

                    String routeId = tu.getTrip().hasRouteId() ? tu.getTrip().getRouteId() : "";
                    int directionId = tu.getTrip().hasDirectionId() ? tu.getTrip().getDirectionId() : -1;

                    // Estrai info veicolo
                    String vehicleId = "";
                    if (tu.hasVehicle() && tu.getVehicle().hasId()) {
                        vehicleId = tu.getVehicle().getId();
                    }

                    long timestamp = tu.hasTimestamp() ? tu.getTimestamp() : System.currentTimeMillis() / 1000;

                    TripUpdate tripUpdate = new TripUpdate(tripId, routeId, directionId, vehicleId, timestamp);

                    // Processa gli aggiornamenti per ogni fermata
                    for (GtfsRealtime.TripUpdate.StopTimeUpdate stu : tu.getStopTimeUpdateList()) {
                        String stopId = stu.hasStopId() ? stu.getStopId() : "";
                        int stopSequence = stu.hasStopSequence() ? stu.getStopSequence() : -1;

                        // Estrai ritardi (in secondi)
                        int arrivalDelay = 0;
                        int departureDelay = 0;
                        long arrivalTime = 0;
                        long departureTime = 0;

                        if (stu.hasArrival()) {
                            arrivalDelay = stu.getArrival().hasDelay() ? stu.getArrival().getDelay() : 0;
                            arrivalTime = stu.getArrival().hasTime() ? stu.getArrival().getTime() : 0;
                        }

                        if (stu.hasDeparture()) {
                            departureDelay = stu.getDeparture().hasDelay() ? stu.getDeparture().getDelay() : 0;
                            departureTime = stu.getDeparture().hasTime() ? stu.getDeparture().getTime() : 0;
                        }

                        StopTimeUpdate stopUpdate = new StopTimeUpdate(
                                stopId, stopSequence, arrivalDelay, departureDelay,
                                arrivalTime, departureTime
                        );

                        // Gestisci schedule relationship
                        if (stu.hasScheduleRelationship()) {
                            switch (stu.getScheduleRelationship()) {
                                case SKIPPED:
                                    stopUpdate.setScheduleRelationship(StopTimeUpdate.ScheduleRelationship.SKIPPED);
                                    break;
                                case NO_DATA:
                                    stopUpdate.setScheduleRelationship(StopTimeUpdate.ScheduleRelationship.NO_DATA);
                                    break;
                                default:
                                    stopUpdate.setScheduleRelationship(StopTimeUpdate.ScheduleRelationship.SCHEDULED);
                            }
                        }

                        tripUpdate.addStopTimeUpdate(stopUpdate);
                    }

                    tripUpdates.put(tripId, tripUpdate);
                }

                System.out.println("[GtfsRealtimeTripUpdateService] ✓ Trip updates caricati: " + tripUpdates.size());

                // ⭐ DEBUG: Mostra anche primi trip_id DOPO l'inserimento nella mappa
                System.out.println("[GtfsRealtimeTripUpdateService] === PRIMI 10 TRIP_ID NELLA MAPPA ===");
                int mapDebugCount = 0;
                for (String key : tripUpdates.keySet()) {
                    if (mapDebugCount < 10) {
                        System.out.println("[GtfsRealtimeTripUpdateService]   → \"" + key + "\"");
                        mapDebugCount++;
                    } else {
                        break;
                    }
                }

                return tripUpdates; // ✅ SUCCESSO - Esci subito

            } catch (Exception e) {
                System.err.println("[GtfsRealtimeTripUpdateService] ✗ Errore tentativo " + attempt + "/" + MAX_RETRIES + ": " + e.getMessage());

                if (attempt == MAX_RETRIES) {
                    System.err.println("[GtfsRealtimeTripUpdateService] ⚠ Tutti i tentativi falliti, ritorno cache vuota");
                    e.printStackTrace();
                } else {
                    try {
                        Thread.sleep(2000 * attempt); // Backoff: 2s, 4s, 6s
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        return tripUpdates; // Ritorna vuoto se tutti i tentativi falliscono
    }

    /**
     * Recupera il ritardo per una specifica fermata e trip
     * @param tripId ID del trip
     * @param stopId ID della fermata
     * @return Ritardo in secondi (negativo = anticipo, 0 = puntuale o no data)
     */
    public static Integer getDelayForStop(String tripId, String stopId) {
        Map<String, TripUpdate> updates = getTripUpdates();

        TripUpdate tripUpdate = updates.get(tripId);
        if (tripUpdate == null) {
            return null; // Nessun dato per questo trip
        }

        StopTimeUpdate stopUpdate = tripUpdate.getUpdateForStop(stopId);
        if (stopUpdate == null) {
            return null; // Nessun dato per questa fermata
        }

        return stopUpdate.getArrivalDelay();
    }

    /**
     * Recupera tutti i trip updates per una specifica route e direzione
     */
    public static List<TripUpdate> getTripUpdatesForRoute(String routeId, int directionId) {
        Map<String, TripUpdate> allUpdates = getTripUpdates();
        List<TripUpdate> filtered = new ArrayList<>();

        for (TripUpdate tu : allUpdates.values()) {
            if (tu.getRouteId().equals(routeId) &&
                    (directionId == -1 || tu.getDirectionId() == directionId)) {
                filtered.add(tu);
            }
        }

        return filtered;
    }

    /**
     * Verifica se ci sono dati real-time disponibili per un trip
     */
    public static boolean hasTripUpdateData(String tripId) {
        Map<String, TripUpdate> updates = getTripUpdates();
        return updates.containsKey(tripId);
    }
}
