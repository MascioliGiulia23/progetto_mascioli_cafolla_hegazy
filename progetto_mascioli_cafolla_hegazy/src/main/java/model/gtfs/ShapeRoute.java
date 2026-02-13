package model.gtfs;

import java.util.*;

// Classe che rappresenta la forma/tracciato di una rotta (GTFS Shapes)
 // Contiene la sequenza di coordinate GPS che descrivono il percorso della linea
 // Legge i dati dal file shapes.txt nella cartella resources

public class ShapeRoute {

    // Attributi della forma
    private String shapeId;              // ID univoco della forma
    private List<GeoPoint> geoPoints;    // Lista di punti geografici in ordine

    // Classe interna per rappresentare un punto geografico

    public static class GeoPoint {
        private double latitude;         // Latitudine
        private double longitude;        // Longitudine
        private int sequence;            // Numero d'ordine nella sequenza
        private double distanceTraveled; // Distanza percorsa dall'inizio (opzionale)

        public GeoPoint(double latitude, double longitude, int sequence, double distanceTraveled) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.sequence = sequence;
            this.distanceTraveled = distanceTraveled;
        }

        public GeoPoint(double latitude, double longitude, int sequence) {
            this(latitude, longitude, sequence, 0.0);
        }

        // Getters
        public double getLatitude() {
            return latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public int getSequence() {
            return sequence;
        }

        public double getDistanceTraveled() {
            return distanceTraveled;
        }

        @Override
        public String toString() {
            return "GeoPoint{" +
                    "lat=" + latitude +
                    ", lon=" + longitude +
                    ", seq=" + sequence +
                    '}';
        }
    }

    //Costruttore della forma

    public ShapeRoute(String shapeId) {
        this.shapeId = shapeId;
        this.geoPoints = new ArrayList<>();
    }

    // GETTERS

    public String getShapeId() {
        return shapeId;
    }

    public List<GeoPoint> getGeoPoints() {
        return geoPoints;
    }

    public int getNumPunti() {
        return geoPoints.size();
    }

    //  SETTERS

    public void setShapeId(String shapeId) {
        this.shapeId = shapeId;
    }

    //METODI DI MANIPOLAZIONE

    //Aggiunge un punto geografico alla forma

    public void aggiungiPunto(GeoPoint punto) {
        geoPoints.add(punto);
    }

    // Aggiunge un punto geografico con parametri

    public void aggiungiPunto(double lat, double lon, int sequence, double distanceTraveled) {
        geoPoints.add(new GeoPoint(lat, lon, sequence, distanceTraveled));
    }

    // Aggiunge un punto geografico con parametri (senza distanza)

    public void aggiungiPunto(double lat, double lon, int sequence) {
        geoPoints.add(new GeoPoint(lat, lon, sequence));
    }

    //Rimuove un punto dalla forma

    public void rimuoviPunto(int indice) {
        if (indice >= 0 && indice < geoPoints.size()) {
            geoPoints.remove(indice);
        }
    }

    // Ordina i punti per numero di sequenza

    public void ordinaPunti() {
        geoPoints.sort(Comparator.comparingInt(GeoPoint::getSequence));
    }

    // Calcola la lunghezza totale della rotta in km (somma delle distanze tra i punti consecutivi usando Haversine)

    public double calcolaLunghezzaRotta() {
        if (geoPoints.size() < 2) {
            return 0.0;
        }

        double lunghezzaTotale = 0.0;
        for (int i = 0; i < geoPoints.size() - 1; i++) {
            lunghezzaTotale += calcolaDistanzaTraPunti(geoPoints.get(i), geoPoints.get(i + 1));
        }
        return lunghezzaTotale;
    }

    // Calcola la distanza tra due punti geografici usando la formula di Haversine

    private double calcolaDistanzaTraPunti(GeoPoint p1, GeoPoint p2) {
        final double RAGGIO_TERRA_KM = 6371;

        double lat1Rad = Math.toRadians(p1.getLatitude());
        double lat2Rad = Math.toRadians(p2.getLatitude());
        double deltaLat = Math.toRadians(p2.getLatitude() - p1.getLatitude());
        double deltaLon = Math.toRadians(p2.getLongitude() - p1.getLongitude());

        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RAGGIO_TERRA_KM * c;
    }

    /**
     * Trova il punto più vicino a una data posizione (utile per localizzazione su mappa)
     *
     * @param lat Latitudine del punto di ricerca
     * @param lon Longitudine del punto di ricerca
     * @return Il punto più vicino e la sua distanza
     */
    public Map.Entry<GeoPoint, Double> trovaPuntoPiuVicino(double lat, double lon) {
        if (geoPoints.isEmpty()) {
            return null;
        }

        GeoPoint puntoCercato = new GeoPoint(lat, lon, 0);
        double distanzaMinima = Double.MAX_VALUE;
        GeoPoint puntoVicino = null;

        for (GeoPoint p : geoPoints) {
            double distanza = calcolaDistanzaTraPunti(puntoCercato, p);
            if (distanza < distanzaMinima) {
                distanzaMinima = distanza;
                puntoVicino = p;
            }
        }

        return new AbstractMap.SimpleEntry<>(puntoVicino, distanzaMinima * 1000); // In metri
    }

    // Ritorna una rappresentazione testuale della forma

    @Override
    public String toString() {
        return "ShapeRoute{" +
                "shapeId='" + shapeId + '\'' +
                ", numPunti=" + geoPoints.size() +
                ", lunghezzaKm=" + String.format("%.2f", calcolaLunghezzaRotta()) +
                '}';
    }

    //Verifica se due forme sono uguali (basato sull'ID)

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ShapeRoute shape = (ShapeRoute) obj;
        return shapeId.equals(shape.shapeId);
    }

    // Genera un hashcode basato sull'ID della forma

    @Override
    public int hashCode() {
        return shapeId.hashCode();
    }
}
