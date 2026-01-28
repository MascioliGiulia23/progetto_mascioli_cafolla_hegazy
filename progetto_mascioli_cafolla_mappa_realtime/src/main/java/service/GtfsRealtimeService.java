package service;

import com.google.transit.realtime.GtfsRealtime;
import org.jxmapviewer.viewer.GeoPosition;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

// >>> NUOVE IMPORT (serve per i test)
import java.util.Objects;            // serve per i test
import java.util.function.Supplier;  // serve per i test


public class GtfsRealtimeService {

    private static final String FEED_URL =
            "https://romamobilita.it/sites/default/files/rome_rtgtfs_vehicle_positions_feed.pb";
    // >>> NUOVA DIPENDENZA INIETTABILE (serve per i test)
    // Default = comportamento originale (apre URL reale). Quindi l'app NON cambia.
    private static Supplier<InputStream> feedStreamSupplier = () -> { // serve per i test
        try {
            return new URL(FEED_URL).openStream();
        } catch (Exception e) {
            // Lanciamo RuntimeException per mantenere la gestione nel catch di getBusPositions()
            throw new RuntimeException(e);
        }
    }; // serve per i test

    // >>> TEST HOOKS (serve per i test)
    static void setFeedStreamSupplierForTest(Supplier<InputStream> supplier) {
        feedStreamSupplier = Objects.requireNonNull(supplier);
    }

    public static void resetForTest() {
        feedStreamSupplier = () -> {
            try {
                return new URL(FEED_URL).openStream();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    //Restituisce le posizioni GPS dei bus in tempo reale.
    public static List<GeoPosition> getBusPositions() {
        List<GeoPosition> positions = new ArrayList<>();

        try (InputStream input = feedStreamSupplier.get()) {

            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(input);

            for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
                if (entity.hasVehicle()) {
                    GtfsRealtime.VehiclePosition v = entity.getVehicle();
                    if (v.hasPosition()) {
                        double lat = v.getPosition().getLatitude();
                        double lon = v.getPosition().getLongitude();
                        positions.add(new GeoPosition(lat, lon));
                    }
                }
            }

            System.out.println("[GtfsRealtimeService] Bus trovati: " + positions.size());

        } catch (Exception e) {
            System.err.println("[GtfsRealtimeService] Errore: " + e.getMessage());
        }

        return positions;
    }

//    // Test rapido standalone
//    public static void main(String[] args) {
//        List<GeoPosition> buses = getBusPositions();
//        for (GeoPosition p : buses) {
//            System.out.printf(" Bus → %.5f, %.5f%n", p.getLatitude(), p.getLongitude());
//        }
//    }
}
