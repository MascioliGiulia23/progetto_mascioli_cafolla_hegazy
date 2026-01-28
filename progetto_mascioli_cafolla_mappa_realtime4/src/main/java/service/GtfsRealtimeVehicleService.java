package service;

import com.google.transit.realtime.GtfsRealtime;
import org.jxmapviewer.viewer.GeoPosition;
import java.io.InputStream;
import java.net.URL;
import java.util.*;


// >>> NUOVE IMPORT (serve per i test)
import java.util.Objects;            // serve per i test
import java.util.function.Supplier;  // serve per i test


public class GtfsRealtimeVehicleService {

    // Feed ufficiale Roma Mobilità per le posizioni dei veicoli
    private static final String VEHICLE_POSITIONS_URL =
            "https://romamobilita.it/sites/default/files/rome_rtgtfs_vehicle_positions_feed.pb";
    // >>> NUOVA DIPENDENZA INIETTABILE (serve per i test)
    // Default = comportamento originale (apre URL reale). Quindi l'app NON cambia.
    private static Supplier<InputStream> feedStreamSupplier = () -> { // serve per i test
        try {
            return new URL(VEHICLE_POSITIONS_URL).openStream();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }; // serve per i test

    // >>> TEST HOOKS (serve per i test)
    static void setFeedStreamSupplierForTest(Supplier<InputStream> supplier) { // serve per i test
        feedStreamSupplier = Objects.requireNonNull(supplier);                 // serve per i test
    }

    static void resetForTest() { // serve per i test
        feedStreamSupplier = () -> { // serve per i test
            try {
                return new URL(VEHICLE_POSITIONS_URL).openStream();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }; // serve per i test
    }
    /**
     * Recupera le posizioni in tempo reale di tutti i veicoli
     * Restituisce una mappa: tripId -> VehicleData
     */
    public static Map<String, VehicleData> getRealtimeVehiclePositions() {
        Map<String, VehicleData> vehicles = new HashMap<>();

        try (InputStream input = feedStreamSupplier.get()) {

            GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.parseFrom(input);

            for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
                if (!entity.hasVehicle()) continue;

                GtfsRealtime.VehiclePosition vp = entity.getVehicle();

                // Verifica che abbia trip e posizione
                if (!vp.hasTrip() || !vp.hasPosition()) continue;

                String tripId = vp.getTrip().getTripId();
                String routeId = vp.getTrip().hasRouteId() ? vp.getTrip().getRouteId() : null;
                int directionId = vp.getTrip().hasDirectionId() ? vp.getTrip().getDirectionId() : -1;

                double lat = vp.getPosition().getLatitude();
                double lon = vp.getPosition().getLongitude();
                float bearing = vp.getPosition().hasBearing() ? vp.getPosition().getBearing() : 0f;

                String vehicleId = vp.hasVehicle() ? vp.getVehicle().getId() : "unknown";

                VehicleData vehicleData = new VehicleData(
                        vehicleId, tripId, routeId, directionId,
                        lat, lon, bearing
                );

                vehicles.put(tripId, vehicleData);
            }

            System.out.println("[GtfsRealtimeVehicleService] Posizioni veicoli caricate: " + vehicles.size());

        } catch (Exception e) {
            System.err.println("[GtfsRealtimeVehicleService] Errore nel caricamento: " + e.getMessage());
        }

        return vehicles;
    }

    /**
     * Filtra i veicoli per routeId e directionId specifici
     */
    public static List<VehicleData> getVehiclesForRouteAndDirection(
            String routeId, int directionId) {

        Map<String, VehicleData> allVehicles = getRealtimeVehiclePositions();
        List<VehicleData> filtered = new ArrayList<>();

        for (VehicleData vd : allVehicles.values()) {
            if (vd.getRouteId() != null &&
                    vd.getRouteId().equals(routeId) &&
                    vd.getDirectionId() == directionId) {
                filtered.add(vd);
            }
        }

        System.out.println("[Filter] Bus trovati per route=" + routeId +
                " direction=" + directionId + ": " + filtered.size());
        return filtered;
    }

    /**
     * Classe interna per rappresentare i dati di un veicolo
     */
    public static class VehicleData {
        private final String vehicleId;
        private final String tripId;
        private final String routeId;
        private final int directionId;
        private final double latitude;
        private final double longitude;
        private final float bearing;

        public VehicleData(String vehicleId, String tripId, String routeId,
                           int directionId, double latitude, double longitude, float bearing) {
            this.vehicleId = vehicleId;
            this.tripId = tripId;
            this.routeId = routeId;
            this.directionId = directionId;
            this.latitude = latitude;
            this.longitude = longitude;
            this.bearing = bearing;
        }

        public String getVehicleId() { return vehicleId; }
        public String getTripId() { return tripId; }
        public String getRouteId() { return routeId; }
        public int getDirectionId() { return directionId; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public float getBearing() { return bearing; }

        public GeoPosition toGeoPosition() {
            return new GeoPosition(latitude, longitude);
        }
    }
}

