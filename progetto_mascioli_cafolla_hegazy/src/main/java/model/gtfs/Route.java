package model.gtfs;


 // Classe che rappresenta una rotta/linea del trasporto pubblico (GTFS Route)
 //Legge i dati dal file routes.txt nella cartella resources

public class Route {

    // Attributi della rotta
    private String routeId;           // ID univoco della rotta
    private String agencyId;          // ID dell'agenzia (opzionale)
    private String routeShortName;    // Nome breve (es. "23", "A")
    private String routeLongName;     // Nome lungo (es. "Linea 23 - Centro/Periferia")
    private String routeDesc;         // Descrizione (opzionale)
    private int routeType;            // Tipo: 0=tram, 1=metro, 2=treno, 3=bus
    private String routeUrl;          // URL della rotta (opzionale)
    private String routeColor;        // Colore in esadecimale (es. "FF0000")
    private String routeTextColor;    // Colore testo in esadecimale (es. "FFFFFF")


     // Costruttore completo della rotta

    public Route(String routeId, String agencyId, String routeShortName, String routeLongName,
                 String routeDesc, int routeType, String routeUrl, String routeColor, String routeTextColor) {
        this.routeId = routeId;
        this.agencyId = agencyId;
        this.routeShortName = routeShortName;
        this.routeLongName = routeLongName;
        this.routeDesc = routeDesc;
        this.routeType = routeType;
        this.routeUrl = routeUrl;
        this.routeColor = routeColor;
        this.routeTextColor = routeTextColor;
    }

// Campi e metodi per supportare la direzione e la shape

    private String shapeId;
    private int directionId = -1;

    public String getShapeId() {
        return shapeId;
    }

    public void setShapeId(String shapeId) {
        this.shapeId = shapeId;
    }

    public int getDirectionId() {
        return directionId;
    }

    public void setDirectionId(int directionId) {
        this.directionId = directionId;
    }

    //  GETTERS

    public String getRouteId() {
        return routeId;
    }

    public String getRouteShortName() {
        return routeShortName;
    }

    public String getRouteLongName() {
        return routeLongName;
    }

    public int getRouteType() { return routeType; }



     //Converte il tipo di rotta in una stringa descrittiva
     // @return Descrizione del tipo di rotta
    public String getTipoRottaDescrizione() {
        return switch (routeType) {
            case 0 -> "Tram";
            case 1 -> "Metro";
            case 2 -> "Treno";
            case 3 -> "Bus";
            case 4 -> "Autobus a servizio accelerato";
            case 5 -> "Ascensore";
            case 6 -> "Funicolare";
            case 7 -> "Funivia";
            case 8 -> "Autobus sostitutivo";
            case 9 -> "Barca";
            case 10 -> "Aereo";
            case 11 -> "Elicottero";
            case 12 -> "Navigazione";
            default -> "Sconosciuto";
        };
    }


     // Ritorna una rappresentazione testuale della rotta

    @Override
    public String toString() {
        return "Route{" +
                "routeId='" + routeId + '\'' +
                ", routeShortName='" + routeShortName + '\'' +
                ", routeLongName='" + routeLongName + '\'' +
                ", routeType=" + getTipoRottaDescrizione() +
                ", routeColor='" + routeColor + '\'' +
                '}';
    }

     // Verifica se due rotte sono uguali (basato sull'ID)

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Route route = (Route) obj;
        return routeId.equals(route.routeId);
    }

     // Genera un hashcode basato sull'ID della rotta

    @Override
    public int hashCode() {
        return routeId.hashCode();
    }
}
