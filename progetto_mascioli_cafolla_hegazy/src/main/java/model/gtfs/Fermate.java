package model.gtfs;

// Classe che rappresenta una fermata del trasporto pubblico (GTFS Stop)
 //Legge i dati dal file stops.txt nella cartella resources

public class Fermate {

    // Attributi della fermata
    private String stopId;           // ID univoco della fermata
    private String stopName;         // Nome della fermata
    private String stopDesc;         // Descrizione (opzionale)
    private double stopLat;          // Latitudine
    private double stopLon;          // Longitudine
    private String stopUrl;          // URL della fermata (opzionale)
    private String locationType;     // Tipo di ubicazione (0=fermata, 1=stazione)
    private String parentStation;    // ID stazione padre (se applicabile)

    // Costruttore completo della fermata

    public Fermate(String stopId, String stopName, String stopDesc,
                   double stopLat, double stopLon, String stopUrl,
                   String locationType, String parentStation) {
        this.stopId = stopId;
        this.stopName = stopName;
        this.stopDesc = stopDesc;
        this.stopLat = stopLat;
        this.stopLon = stopLon;
        this.stopUrl = stopUrl;
        this.locationType = locationType;
        this.parentStation = parentStation;
    }

    //Costruttore semplificato (con solo i campi essenziali)

    public Fermate(String stopId, String stopName, double stopLat, double stopLon) {
        this.stopId = stopId;
        this.stopName = stopName;
        this.stopLat = stopLat;
        this.stopLon = stopLon;
        this.stopDesc = "";
        this.stopUrl = "";
        this.locationType = "0";
        this.parentStation = "";
    }

    //  GETTERS

    public String getStopId() {
        return stopId;
    }

    public String getStopName() {
        return stopName;
    }

    public String getStopDesc() {
        return stopDesc;
    }

    public double getStopLat() {
        return stopLat;
    }

    public double getStopLon() {
        return stopLon;
    }

    public String getStopUrl() {
        return stopUrl;
    }

    public String getLocationType() {
        return locationType;
    }



    public double calcolaDistanza(Fermate altroStop) {
        final double RAGGIO_TERRA_KM = 6371; // Raggio medio della Terra in km

        double lat1Rad = Math.toRadians(this.stopLat);
        double lat2Rad = Math.toRadians(altroStop.stopLat);
        double deltaLat = Math.toRadians(altroStop.stopLat - this.stopLat);
        double deltaLon = Math.toRadians(altroStop.stopLon - this.stopLon);

        // Formula di Haversine
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RAGGIO_TERRA_KM * c;
    }


     // Calcola la distanza in metri tra questa fermata e un'altra
     //@param altroStop La fermata con cui calcolare la distanza
     // @return Distanza in metri

    public double calcolaDistanzaMetri(Fermate altroStop) {
        return calcolaDistanza(altroStop) * 1000;
    }

    // Ritorna una rappresentazione testuale della fermata

    @Override
    public String toString() {
        return "Fermate{" +
                "stopId='" + stopId + '\'' +
                ", stopName='" + stopName + '\'' +
                ", stopLat=" + stopLat +
                ", stopLon=" + stopLon +
                ", stopDesc='" + stopDesc + '\'' +
                '}';
    }

    //Verifica se due fermate sono uguali (basato sull'ID)

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Fermate fermate = (Fermate) obj;
        return stopId.equals(fermate.stopId);
    }

    //Genera un hashcode basato sull'ID della fermata

    @Override
    public int hashCode() {
        return stopId.hashCode();
    }
}
