package model.realtime;

import java.time.LocalTime;

/**
 * Classe che rappresenta lo stato di ritardo/anticipo/puntualità
 * per una specifica fermata e corsa
 */
public class DelayInfo {

    public enum DelayStatus {
        ON_TIME,        // Puntuale (±1 min)
        DELAYED,        // In ritardo (>1 min)
        EARLY,          // In anticipo (>1 min)
        NO_DATA,        // Nessun dato real-time disponibile
        SKIPPED         // Fermata saltata
    }

    private String stopId;
    private String stopName;
    private String tripId;
    private String routeId;
    private String routeShortName;

    private LocalTime scheduledTime;    // Orario programmato
    private LocalTime predictedTime;    // Orario previsto real-time
    private int delaySeconds;           // Ritardo in secondi (negativo = anticipo)
    private DelayStatus status;
    private long lastUpdate;            // Timestamp ultimo aggiornamento

    public DelayInfo(String stopId, String stopName, String tripId, String routeId,
                     String routeShortName, LocalTime scheduledTime) {
        this.stopId = stopId;
        this.stopName = stopName;
        this.tripId = tripId;
        this.routeId = routeId;
        this.routeShortName = routeShortName;
        this.scheduledTime = scheduledTime;
        this.delaySeconds = 0;
        this.status = DelayStatus.NO_DATA;
        this.lastUpdate = System.currentTimeMillis();
    }

    // ==================== GETTERS ====================

    public String getStopId() {
        return stopId;
    }

    public String getStopName() {
        return stopName;
    }

    public String getTripId() {
        return tripId;
    }

    public String getRouteId() {
        return routeId;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public LocalTime getScheduledTime() {
        return scheduledTime;
    }

    public LocalTime getPredictedTime() {
        return predictedTime;
    }

    public int getDelaySeconds() {
        return delaySeconds;
    }

    public DelayStatus getStatus() {
        return status;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    // ==================== SETTERS ====================

    public void setDelaySeconds(int delaySeconds) {
        this.delaySeconds = delaySeconds;
        this.predictedTime = scheduledTime.plusSeconds(delaySeconds);

        // Determina lo stato automaticamente
        if (Math.abs(delaySeconds) <= 60) {
            this.status = DelayStatus.ON_TIME;
        } else if (delaySeconds > 60) {
            this.status = DelayStatus.DELAYED;
        } else {
            this.status = DelayStatus.EARLY;
        }

        this.lastUpdate = System.currentTimeMillis();
    }

    public void setStatus(DelayStatus status) {
        this.status = status;
        this.lastUpdate = System.currentTimeMillis();
    }

    // ==================== METODI UTILI ====================

    /**
     * Ritorna il ritardo/anticipo in minuti
     */
    public int getDelayMinutes() {
        return Math.round(delaySeconds / 60.0f);
    }

    /**
     * Ritorna una stringa descrittiva del ritardo
     */
    public String getDelayDescription() {
        switch (status) {
            case ON_TIME:
                return "In orario";
            case DELAYED:
                return "+" + getDelayMinutes() + " min";
            case EARLY:
                return getDelayMinutes() + " min";
            case SKIPPED:
                return "Fermata saltata";
            case NO_DATA:
            default:
                return "Dato non disponibile";
        }
    }

    /**
     * Verifica se i dati sono recenti (< 2 minuti)
     */
    public boolean isDataFresh() {
        long age = System.currentTimeMillis() - lastUpdate;
        return age < 120_000; // 2 minuti
    }

    @Override
    public String toString() {
        return "DelayInfo{" +
                "stopName='" + stopName + '\'' +
                ", route=" + routeShortName +
                ", scheduled=" + scheduledTime +
                ", predicted=" + predictedTime +
                ", status=" + status +
                ", delay=" + getDelayMinutes() + "min" +
                '}';
    }
}
