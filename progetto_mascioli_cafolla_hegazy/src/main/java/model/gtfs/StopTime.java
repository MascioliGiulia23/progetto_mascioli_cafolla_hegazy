package model.gtfs;

import java.time.LocalTime;

 // Classe che rappresenta l'orario di arrivo/partenza in una fermata (GTFS Stop Times)
 // Legge i dati dal file stop_times.txt nella cartella resources

public class StopTime {

    // Attributi dell'orario di fermata
    private String tripId;              // ID della corsa (collegamento a trips.txt)
    private LocalTime arrivalTime;      // Orario di arrivo (HH:MM:SS)
    private LocalTime departureTime;    // Orario di partenza (HH:MM:SS)
    private String stopId;              // ID della fermata (collegamento a stops.txt)
    private int stopSequence;           // Posizione nella sequenza della corsa
    private String stopHeadsign;        // Destinazione mostrata (opzionale)
    private int pickupType;             // 0=regolare, 1=nessun ritiro, 2=chiama preventivamente, 3=accordi driver
    private int dropOffType;            // 0=regolare, 1=nessuno scalo, 2=chiama preventivamente, 3=accordi driver
    private double shapeDistTraveled;   // Distanza percorsa sulla forma (opzionale)

    // Costruttore completo

    public StopTime(String tripId, LocalTime arrivalTime, LocalTime departureTime, String stopId,
                    int stopSequence, String stopHeadsign, int pickupType, int dropOffType,
                    double shapeDistTraveled) {
        this.tripId = tripId;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.stopId = stopId;
        this.stopSequence = stopSequence;
        this.stopHeadsign = stopHeadsign;
        this.pickupType = pickupType;
        this.dropOffType = dropOffType;
        this.shapeDistTraveled = shapeDistTraveled;
    }

    //Costruttore semplificato

    public StopTime(String tripId, LocalTime arrivalTime, LocalTime departureTime, String stopId, int stopSequence) {
        this.tripId = tripId;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.stopId = stopId;
        this.stopSequence = stopSequence;
        this.stopHeadsign = "";
        this.pickupType = 0;
        this.dropOffType = 0;
        this.shapeDistTraveled = 0.0;
    }

    public String getTripId() {
        return tripId;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public String getStopId() {
        return stopId;
    }

    public int getStopSequence() {
        return stopSequence;
    }

    public String getStopHeadsign() {
        return stopHeadsign;
    }

    public int getPickupType() {
        return pickupType;
    }

    public int getDropOffType() {
        return dropOffType;
    }

    public double getShapeDistTraveled() {
        return shapeDistTraveled;
    }


    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public void setStopId(String stopId) {
        this.stopId = stopId;
    }

    public void setStopSequence(int stopSequence) {
        this.stopSequence = stopSequence;
    }


    // Ritorna il tempo di fermata (quanto tempo l'autobus sta fermo)

    public long getTempoFermata() {
        if (arrivalTime == null || departureTime == null) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.SECONDS.between(arrivalTime, departureTime);
    }

    // Verifica se questo è il primo stop della corsa

    public boolean isPrimoStop() {
        return stopSequence == 1;
    }

    // Verifica se è possibile scendere a questa fermata

    public boolean isDropoffPermesso() {
        return dropOffType == 0; // 0 = regolare
    }

    // Verifica se è possibile salire a questa fermata

    public boolean isPickupPermesso() {
        return pickupType == 0; // 0 = regolare
    }

    // Ritorna una descrizione del tipo di ritiro

    public String getPickupTypeDescrizione() {
        return switch (pickupType) {
            case 0 -> "Regolare";
            case 1 -> "Nessun ritiro";
            case 2 -> "Chiama preventivamente";
            case 3 -> "Accordi con driver";
            default -> "Sconosciuto";
        };
    }

    //Ritorna una descrizione del tipo di scalo

    public String getDropOffTypeDescrizione() {
        return switch (dropOffType) {
            case 0 -> "Regolare";
            case 1 -> "Nessuno scalo";
            case 2 -> "Chiama preventivamente";
            case 3 -> "Accordi con driver";
            default -> "Sconosciuto";
        };
    }

    // Converte una stringa di tempo in formato HH:MM:SS a LocalTime
    // Supporta anche formati con ore >= 24 (utile per corse notturne)

    public static LocalTime parseTempoGTFS(String tempoStr) {
        if (tempoStr == null || tempoStr.isEmpty()) {
            return null;
        }
        try {
            String[] parti = tempoStr.trim().split(":");
            if (parti.length != 3) {
                return null;
            }

            int ore = Integer.parseInt(parti[0]);
            int minuti = Integer.parseInt(parti[1]);
            int secondi = Integer.parseInt(parti[2]);

            // Se le ore sono >= 24, le riduciamo modulo 24 (per corse notturne)
            if (ore >= 24) {
                ore = ore % 24;
            }

            return LocalTime.of(ore, minuti, secondi);
        } catch (Exception e) {
            System.err.println("Errore nel parsing del tempo: " + tempoStr);
            return null;
        }
    }

    //Converte un integer (0/1) a integer di pickup/dropoff

    public static int parsePickupDropoffType(String typeStr) {
        try {
            return Integer.parseInt(typeStr.trim());
        } catch (Exception e) {
            return 0; // Default a regolare
        }
    }

    //Ritorna una rappresentazione testuale

    @Override
    public String toString() {
        return "StopTime{" +
                "tripId='" + tripId + '\'' +
                ", stopId='" + stopId + '\'' +
                ", arrivalTime=" + arrivalTime +
                ", departureTime=" + departureTime +
                ", stopSequence=" + stopSequence +
                '}';
    }

    //Verifica se due orari di fermata sono uguali

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        StopTime that = (StopTime) obj;
        return tripId.equals(that.tripId) && stopSequence == that.stopSequence;
    }

    // Genera un hashcode

    @Override
    public int hashCode() {
        return (tripId + stopSequence).hashCode();
    }
}
