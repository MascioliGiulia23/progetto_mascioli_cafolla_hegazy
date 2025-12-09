package model.realtime;

import java.io.Serializable;

/**
 * Rappresenta l'aggiornamento real-time per una fermata specifica
 * Contiene ritardo/anticipo in arrivo e partenza
 */
public class StopTimeUpdate implements Serializable {
    private static final long serialVersionUID = 1L;

    private String stopId;              // ID della fermata
    private int stopSequence;           // Posizione nella sequenza
    private int arrivalDelay;           // Ritardo arrivo (secondi, può essere negativo)
    private int departureDelay;         // Ritardo partenza (secondi, può essere negativo)
    private long arrivalTime;           // Timestamp previsto arrivo (epoch seconds)
    private long departureTime;         // Timestamp previsto partenza (epoch seconds)
    private ScheduleRelationship scheduleRelationship;

    public enum ScheduleRelationship {
        SCHEDULED,      // Corsa programmata
        SKIPPED,        // Fermata saltata
        NO_DATA         // Nessun dato disponibile
    }

    public StopTimeUpdate(String stopId, int stopSequence, int arrivalDelay, int departureDelay) {
        this.stopId = stopId;
        this.stopSequence = stopSequence;
        this.arrivalDelay = arrivalDelay;
        this.departureDelay = departureDelay;
        this.scheduleRelationship = ScheduleRelationship.SCHEDULED;
    }

    public StopTimeUpdate(String stopId, int stopSequence, int arrivalDelay, int departureDelay,
                          long arrivalTime, long departureTime) {
        this(stopId, stopSequence, arrivalDelay, departureDelay);
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
    }

    // ==================== GETTERS ====================

    public String getStopId() {
        return stopId;
    }

    public int getStopSequence() {
        return stopSequence;
    }

    public int getArrivalDelay() {
        return arrivalDelay;
    }

    public int getDepartureDelay() {
        return departureDelay;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }

    public long getDepartureTime() {
        return departureTime;
    }

    public ScheduleRelationship getScheduleRelationship() {
        return scheduleRelationship;
    }

    public void setScheduleRelationship(ScheduleRelationship scheduleRelationship) {
        this.scheduleRelationship = scheduleRelationship;
    }

    // ==================== METODI UTILI ====================

    /**
     * Verifica se la fermata è in ritardo (>1 min)
     */
    public boolean isDelayed() {
        return arrivalDelay > 60;
    }

    /**
     * Verifica se la fermata è in anticipo (>1 min)
     */
    public boolean isEarly() {
        return arrivalDelay < -60;
    }

    /**
     * Verifica se è puntuale (entro ±1 min)
     */
    public boolean isOnTime() {
        return Math.abs(arrivalDelay) <= 60;
    }

    /**
     * Ritorna il ritardo in minuti (arrotondato)
     */
    public int getDelayMinutes() {
        return Math.round(arrivalDelay / 60.0f);
    }

    /**
     * Verifica se la fermata è stata saltata
     */
    public boolean isSkipped() {
        return scheduleRelationship == ScheduleRelationship.SKIPPED;
    }

    @Override
    public String toString() {
        return "StopTimeUpdate{" +
                "stopId='" + stopId + '\'' +
                ", arrivalDelay=" + arrivalDelay + "s" +
                ", delayMinutes=" + getDelayMinutes() + "min" +
                '}';
    }
}
