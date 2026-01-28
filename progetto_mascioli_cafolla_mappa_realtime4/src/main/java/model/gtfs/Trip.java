package model.gtfs;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe che rappresenta una singola corsa (GTFS Trip)
 * Una corsa è un'istanza di una rotta in un giorno specifico
 * Legge i dati dal file trips.txt nella cartella resources
 */
public class Trip {

    // Attributi della corsa
    private String routeId;             // ID della rotta (collegamento a routes.txt)
    private String serviceId;           // ID del servizio (collegamento a calendar.txt)
    private String tripId;              // ID univoco della corsa
    private String tripHeadsign;        // Destinazione mostrata sui cartellini
    private String tripShortName;       // Nome breve della corsa (opzionale)
    private int directionId;            // 0=verso A, 1=verso B (opzionale)
    private String blockId;             // ID blocco (per raggruppare corse sequenziali)
    private String shapeId;             // ID della forma/tracciato
    private int wheelchairAccessible;   // 0=nessuna info, 1=accessibile, 2=non accessibile
    private int bikesAllowed;           // 0=nessuna info, 1=consentite, 2=non consentite
    private List<StopTime> stopTimes;   // Lista degli orari di fermata per questa corsa

    /**
     * Costruttore completo
     */
    public Trip(String routeId, String serviceId, String tripId, String tripHeadsign,
                String tripShortName, int directionId, String blockId, String shapeId,
                int wheelchairAccessible, int bikesAllowed) {
        this.routeId = routeId;
        this.serviceId = serviceId;
        this.tripId = tripId;
        this.tripHeadsign = tripHeadsign;
        this.tripShortName = tripShortName;
        this.directionId = directionId;
        this.blockId = blockId;
        this.shapeId = shapeId;
        this.wheelchairAccessible = wheelchairAccessible;
        this.bikesAllowed = bikesAllowed;
        this.stopTimes = new ArrayList<>();
    }

    /**
     * Costruttore semplificato
     */
    public Trip(String routeId, String serviceId, String tripId, String tripHeadsign) {
        this.routeId = routeId;
        this.serviceId = serviceId;
        this.tripId = tripId;
        this.tripHeadsign = tripHeadsign;
        this.tripShortName = "";
        this.directionId = 0;
        this.blockId = "";
        this.shapeId = "";
        this.wheelchairAccessible = 0;
        this.bikesAllowed = 0;
        this.stopTimes = new ArrayList<>();
    }

    // ==================== GETTERS ====================

    public String getRouteId() {
        return routeId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public String getTripId() {
        return tripId;
    }

    public String getTripHeadsign() {
        return tripHeadsign;
    }

    public String getTripShortName() {
        return tripShortName;
    }

    public int getDirectionId() {
        return directionId;
    }

    public String getBlockId() {
        return blockId;
    }

    public String getShapeId() {
        return shapeId;
    }

    public int getWheelchairAccessible() {
        return wheelchairAccessible;
    }

    public int getBikesAllowed() {
        return bikesAllowed;
    }

    public List<StopTime> getStopTimes() {
        return stopTimes;
    }

    // ==================== SETTERS ====================

    public void setRouteId(String routeId) {
        this.routeId = routeId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public void setTripHeadsign(String tripHeadsign) {
        this.tripHeadsign = tripHeadsign;
    }

    public void setTripShortName(String tripShortName) {
        this.tripShortName = tripShortName;
    }

    public void setDirectionId(int directionId) {
        this.directionId = directionId;
    }

    public void setBlockId(String blockId) {
        this.blockId = blockId;
    }

    public void setShapeId(String shapeId) {
        this.shapeId = shapeId;
    }

    public void setWheelchairAccessible(int wheelchairAccessible) {
        this.wheelchairAccessible = wheelchairAccessible;
    }

    public void setBikesAllowed(int bikesAllowed) {
        this.bikesAllowed = bikesAllowed;
    }

    // ==================== METODI PER STOP TIMES ====================

    /**
     * Aggiunge un orario di fermata a questa corsa
     */
    public void aggiungiStopTime(StopTime stopTime) {
        stopTimes.add(stopTime);
    }

    /**
     * Ritorna il numero di fermate di questa corsa
     */
    public int getNumeroFermate() {
        return stopTimes.size();
    }

    /**
     * Ritorna l'orario di inizio della corsa (primo stop)
     */
    public StopTime getPrimoStop() {
        return stopTimes.isEmpty() ? null : stopTimes.get(0);
    }

    /**
     * Ritorna l'orario di fine della corsa (ultimo stop)
     */
    public StopTime getUltimoStop() {
        return stopTimes.isEmpty() ? null : stopTimes.get(stopTimes.size() - 1);
    }

    /**
     * Ritorna la durata totale della corsa
     */
    public long getDurataTotaleCorse() {
        StopTime primo = getPrimoStop();
        StopTime ultimo = getUltimoStop();

        if (primo == null || ultimo == null) {
            return 0;
        }

        return java.time.temporal.ChronoUnit.SECONDS.between(
                primo.getArrivalTime(),
                ultimo.getDepartureTime()
        );
    }

    /**
     * Ricerca uno stop per posizione nella sequenza
     */
    public StopTime getStopAtSequence(int sequence) {
        for (StopTime st : stopTimes) {
            if (st.getStopSequence() == sequence) {
                return st;
            }
        }
        return null;
    }

    /**
     * Ricerca uno stop per ID fermata
     */
    public StopTime getStopByFermataId(String stopId) {
        for (StopTime st : stopTimes) {
            if (st.getStopId().equals(stopId)) {
                return st;
            }
        }
        return null;
    }

    /**
     * Ritorna tutti gli ID delle fermate di questa corsa
     */
    public List<String> getStopIds() {
        List<String> stopIds = new ArrayList<>();
        for (StopTime st : stopTimes) {
            stopIds.add(st.getStopId());
        }
        return stopIds;
    }

    // ==================== METODI UTILI ====================

    /**
     * Verifica se la corsa è accessibile ai disabili
     */
    public boolean isWheelchairAccessibile() {
        return wheelchairAccessible == 1;
    }

    /**
     * Verifica se le bici sono permesse
     */
    public boolean isBikesConsentite() {
        return bikesAllowed == 1;
    }

    /**
     * Ritorna una descrizione dell'accessibilità disabili
     */
    public String getWheelchairAccessibileDescrizione() {
        return switch (wheelchairAccessible) {
            case 0 -> "Nessuna informazione";
            case 1 -> "Accessibile";
            case 2 -> "Non accessibile";
            default -> "Sconosciuto";
        };
    }

    /**
     * Ritorna una descrizione delle bici
     */
    public String getBikesAllowedDescrizione() {
        return switch (bikesAllowed) {
            case 0 -> "Nessuna informazione";
            case 1 -> "Bici consentite";
            case 2 -> "Bici non consentite";
            default -> "Sconosciuto";
        };
    }

    /**
     * Ritorna una rappresentazione testuale
     */
    @Override
    public String toString() {
        return "Trip{" +
                "tripId='" + tripId + '\'' +
                ", routeId='" + routeId + '\'' +
                ", tripHeadsign='" + tripHeadsign + '\'' +
                ", numeroFermate=" + getNumeroFermate() +
                ", durata=" + getDurataTotaleCorse() + " secondi" +
                '}';
    }

    /**
     * Verifica se due corse sono uguali
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Trip that = (Trip) obj;
        return tripId.equals(that.tripId);
    }

    /**
     * Genera un hashcode
     */
    @Override
    public int hashCode() {
        return tripId.hashCode();
    }
}
