package model.realtime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Rappresenta un aggiornamento GTFS-RT per una corsa (trip)
 * Contiene informazioni su ritardi/anticipi per ogni fermata della corsa
 */
public class TripUpdate implements Serializable {
    private static final long serialVersionUID = 1L;

    private String tripId;              // ID della corsa
    private String routeId;             // ID della linea
    private int directionId;            // Direzione (0 o 1)
    private String vehicleId;           // ID del veicolo
    private long timestamp;             // Timestamp dell'aggiornamento
    private List<StopTimeUpdate> stopTimeUpdates;  // Lista aggiornamenti per fermata

    // Costruttore completo
    public TripUpdate(String tripId, String routeId, int directionId, String vehicleId, long timestamp) {
        this.tripId = tripId;
        this.routeId = routeId;
        this.directionId = directionId;
        this.vehicleId = vehicleId;
        this.timestamp = timestamp;
        this.stopTimeUpdates = new ArrayList<>();
    }

    // Costruttore semplificato (per compatibilità)
    public TripUpdate(String tripId, String routeId) {
        this(tripId, routeId, -1, "", System.currentTimeMillis() / 1000);
    }

    // ==================== GETTERS ====================

    public String getTripId() {
        return tripId;
    }

    public String getRouteId() {
        return routeId;
    }

    public int getDirectionId() {
        return directionId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public List<StopTimeUpdate> getStopTimeUpdates() {
        return stopTimeUpdates;
    }

    // ==================== METODI UTILI ====================

    /**
     * Aggiunge un aggiornamento per una fermata
     */
    public void addStopTimeUpdate(StopTimeUpdate update) {
        stopTimeUpdates.add(update);
    }

    /**
     * Trova l'aggiornamento per una specifica fermata (con fuzzy matching)
     */
    public StopTimeUpdate getUpdateForStop(String stopId) {
        if (stopId == null || stopId.isEmpty()) {
            return null;
        }

        // 1. Cerca match esatto
        for (StopTimeUpdate update : stopTimeUpdates) {
            if (stopId.equals(update.getStopId())) {
                return update;
            }
        }

        // 2. Prova fuzzy matching (rimuove spazi, ignora case)
        String normalizedStopId = stopId.trim().toLowerCase();
        for (StopTimeUpdate update : stopTimeUpdates) {
            String updateStopId = update.getStopId();
            if (updateStopId != null && normalizedStopId.equals(updateStopId.trim().toLowerCase())) {
                return update;
            }
        }

        // 3. Prova match parziale (per stop_id con prefissi)
        for (StopTimeUpdate update : stopTimeUpdates) {
            String updateStopId = update.getStopId();
            if (updateStopId != null &&
                    (updateStopId.endsWith(stopId) || stopId.endsWith(updateStopId))) {
                return update;
            }
        }

        return null;
    }

    /**
     * Trova l'aggiornamento per stop_sequence
     */
    public StopTimeUpdate getUpdateForStopSequence(int stopSequence) {
        for (StopTimeUpdate update : stopTimeUpdates) {
            if (update.getStopSequence() == stopSequence) {
                return update;
            }
        }
        return null;
    }

    /**
     * Trova l'aggiornamento per fermata (prova prima stop_id, poi stop_sequence)
     */
    public StopTimeUpdate getUpdateForStopFuzzy(String stopId, int stopSequence) {
        // Prova prima con stop_id
        StopTimeUpdate update = getUpdateForStop(stopId);
        if (update != null) {
            return update;
        }

        // Fallback: prova con stop_sequence
        if (stopSequence >= 0) {
            return getUpdateForStopSequence(stopSequence);
        }

        return null;
    }

    /**
     * Verifica se la corsa ha ritardi
     */
    public boolean hasDelays() {
        for (StopTimeUpdate update : stopTimeUpdates) {
            if (update.getArrivalDelay() > 60) { // Ritardo > 1 minuto
                return true;
            }
        }
        return false;
    }

    /**
     * Calcola il ritardo medio della corsa
     */
    public int getAverageDelay() {
        if (stopTimeUpdates.isEmpty()) {
            return 0;
        }

        int totalDelay = 0;
        int count = 0;

        for (StopTimeUpdate update : stopTimeUpdates) {
            // Ignora valori anomali (oltre 1 ora di ritardo/anticipo)
            int delay = update.getArrivalDelay();
            if (Math.abs(delay) < 3600) {
                totalDelay += delay;
                count++;
            }
        }

        return count > 0 ? totalDelay / count : 0;
    }

    /**
     * Ritorna il ritardo massimo della corsa
     */
    public int getMaxDelay() {
        int maxDelay = 0;
        for (StopTimeUpdate update : stopTimeUpdates) {
            int delay = update.getArrivalDelay();
            if (delay > maxDelay) {
                maxDelay = delay;
            }
        }
        return maxDelay;
    }

    /**
     * Verifica se la corsa è in anticipo
     */
    public boolean isEarly() {
        for (StopTimeUpdate update : stopTimeUpdates) {
            if (update.getArrivalDelay() < -60) { // Anticipo > 1 minuto
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica se ha dati per una specifica fermata
     */
    public boolean hasDataForStop(String stopId) {
        return getUpdateForStop(stopId) != null;
    }

    /**
     * Ottiene tutte le stop_id presenti negli updates
     */
    public List<String> getStopIds() {
        List<String> stopIds = new ArrayList<>();
        for (StopTimeUpdate update : stopTimeUpdates) {
            stopIds.add(update.getStopId());
        }
        return stopIds;
    }

    /**
     * Debug: stampa tutti gli stop_id
     */
    public void printStopIds() {
        System.out.println("TripUpdate " + tripId + " ha " + stopTimeUpdates.size() + " fermate:");
        for (int i = 0; i < Math.min(5, stopTimeUpdates.size()); i++) {
            StopTimeUpdate update = stopTimeUpdates.get(i);
            System.out.println("  - " + update.getStopId() + " (seq: " + update.getStopSequence() +
                    ", delay: " + update.getArrivalDelay() + "s)");
        }
        if (stopTimeUpdates.size() > 5) {
            System.out.println("  ... e altre " + (stopTimeUpdates.size() - 5) + " fermate");
        }
    }

    @Override
    public String toString() {
        return "TripUpdate{" +
                "tripId='" + tripId + '\'' +
                ", routeId='" + routeId + '\'' +
                ", directionId=" + directionId +
                ", stopUpdates=" + stopTimeUpdates.size() +
                ", avgDelay=" + getAverageDelay() + "s" +
                ", maxDelay=" + getMaxDelay() + "s" +
                '}';
    }
}
