package service;

import com.google.transit.realtime.GtfsRealtime;
import org.jxmapviewer.viewer.GeoPosition;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class GtfsRealtimeService {

    private static final String FEED_URL =
            "https://romamobilita.it/sites/default/files/rome_rtgtfs_vehicle_positions_feed.pb";

    //Restituisce le posizioni GPS dei bus in tempo reale.
    public static List<GeoPosition> getBusPositions() {
        List<GeoPosition> positions = new ArrayList<>();

        try (InputStream input = new URL(FEED_URL).openStream()) {
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
