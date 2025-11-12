package service;

import com.google.transit.realtime.GtfsRealtime;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;


public class GtfsRealtimeTripUpdatesService {

    // Feed ufficiale Roma Mobilità
    private static final String FEED_URL =
            "https://romamobilita.it/sites/default/files/rome_rtgtfs_trip_updates_feed.pb";

    // Restituisce ritardo in minuti.

    public static Map<String, Integer> getRealtimeDelays() {
        Map<String, Integer> delays = new HashMap<>();

        try (InputStream input = new URL(FEED_URL).openStream()) {
            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(input);

            for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
                if (!entity.hasTripUpdate()) continue;

                GtfsRealtime.TripUpdate tripUpdate = entity.getTripUpdate();

                for (GtfsRealtime.TripUpdate.StopTimeUpdate stu : tripUpdate.getStopTimeUpdateList()) {
                    if (stu.hasStopId() && stu.hasArrival() && stu.getArrival().hasDelay()) {
                        int delaySec = stu.getArrival().getDelay();
                        int delayMin = delaySec / 60;
                        delays.put(stu.getStopId(), delayMin);
                    }
                }
            }

            System.out.println("[GtfsRealtimeTripUpdatesService] Ritardi caricati: " + delays.size());

        } catch (Exception e) {
            System.err.println("[GtfsRealtimeTripUpdatesService] Errore: " + e.getMessage());
        }

        return delays;
    }

    //orario di arrivo effettivo

    public static Map<String, LocalTime> getRealtimeArrivals() {
        Map<String, LocalTime> arrivals = new HashMap<>();

        try (InputStream input = new URL(FEED_URL).openStream()) {
            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(input);

            for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
                if (!entity.hasTripUpdate()) continue;

                GtfsRealtime.TripUpdate tripUpdate = entity.getTripUpdate();

                for (GtfsRealtime.TripUpdate.StopTimeUpdate stu : tripUpdate.getStopTimeUpdateList()) {
                    if (stu.hasStopId() && stu.hasArrival() && stu.getArrival().hasTime()) {
                        long unixTime = stu.getArrival().getTime();
                        LocalTime ora = Instant.ofEpochSecond(unixTime)
                                .atZone(ZoneId.systemDefault())
                                .toLocalTime();
                        arrivals.put(stu.getStopId(), ora);
                    }
                }
            }

            System.out.println("[GtfsRealtimeTripUpdatesService] Arrivi caricati: " + arrivals.size());

        } catch (Exception e) {
            System.err.println("[GtfsRealtimeTripUpdatesService] Errore: " + e.getMessage());
        }

        return arrivals;
    }

//    // --- TEST RAPIDO ---
//    public static void main(String[] args) {
//        Map<String, Integer> delays = getRealtimeDelays();
//        System.out.println("Esempio di ritardi:");
//        delays.entrySet().stream().limit(10).forEach(e ->
//                System.out.printf(" Fermata %s -> %d min di ritardo%n", e.getKey(), e.getValue())
//        );
//
//        Map<String, LocalTime> arr = getRealtimeArrivals();
//        System.out.println("\nEsempio di orari di arrivo:");
//        arr.entrySet().stream().limit(10).forEach(e ->
//                System.out.printf("Fermata %s -> arrivo alle %s%n", e.getKey(), e.getValue())
//        );
//    }
}


