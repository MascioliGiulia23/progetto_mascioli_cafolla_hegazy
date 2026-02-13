package model.utils;

import model.gtfs.*;

import java.io.*;
import java.net.URL;
import java.time.LocalDate;
import java.util.*;


 //Classe Database che gestisce la lettura dei file GTFS statici da resources
 //nella cartella static_gtfs/

public class Database {

    //  LETTURA GTFS STATICI - STOPS

    public static List<Fermate> leggiStopDaFile() {
        List<Fermate> fermate = new ArrayList<>();

        try {
            URL resourceUrl = Database.class.getClassLoader().getResource("static_gtfs/stops.txt");

            try (BufferedReader reader = apriFileGTFS("stops.txt")) {

                String line;
                boolean primaLinea = true;

                while ((line = reader.readLine()) != null) {
                    if (primaLinea) {
                        primaLinea = false;
                        continue;
                    }

                    try {
                        String[] campi = line.split(",");

                        if (campi.length < 6) continue;

                        String stopId = campi[0].trim().replaceAll("\"", "");
                        String stopCode = campi[1].trim().replaceAll("\"", "");
                        String stopName = campi[2].trim().replaceAll("\"", "");
                        String stopDesc = campi.length > 3 ? campi[3].trim().replaceAll("\"", "") : "";
                        double stopLat = Double.parseDouble(campi[4].trim());
                        double stopLon = Double.parseDouble(campi[5].trim());
                        String stopUrl = campi.length > 6 ? campi[6].trim().replaceAll("\"", "") : "";
                        String wheelchairBoarding = campi.length > 7 ? campi[7].trim().replaceAll("\"", "") : "";
                        String stopTimezone = campi.length > 8 ? campi[8].trim().replaceAll("\"", "") : "";
                        String locationType = campi.length > 9 ? campi[9].trim().replaceAll("\"", "") : "0";
                        String parentStation = campi.length > 10 ? campi[10].trim().replaceAll("\"", "") : "";

                        Fermate fermata = new Fermate(
                                stopId,
                                stopName,
                                stopDesc,
                                stopLat,
                                stopLon,
                                stopUrl,
                                locationType,
                                parentStation
                        );
                        fermate.add(fermata);

                    } catch (Exception e) {
                        // Ignora linee con errori di parsing
                    }
                }

                System.out.println("Caricate " + fermate.size() + " fermate da static_gtfs/stops.txt");

            }

        } catch (Exception e) {
            System.err.println("Errore nella lettura di static_gtfs/stops.txt: " + e.getMessage());
        }

        return fermate;
    }

    //  LETTURA GTFS STATICI - ROUTES

    public static List<Route> leggiRouteDaFile() {
        List<Route> rotte = new ArrayList<>();

        try {
            URL resourceUrl = Database.class.getClassLoader().getResource("static_gtfs/routes.txt");

            try (BufferedReader reader = apriFileGTFS("routes.txt")) {

                String line;
                boolean primaLinea = true;

                while ((line = reader.readLine()) != null) {
                    if (primaLinea) {
                        primaLinea = false;
                        continue;
                    }

                    try {
                        String[] campi = line.split(",");

                        if (campi.length < 3) continue;

                        String routeId = campi[0].trim().replaceAll("\"", "");
                        String agencyId = campi.length > 1 ? campi[1].trim().replaceAll("\"", "") : "";
                        String routeShortName = campi.length > 2 ? campi[2].trim().replaceAll("\"", "") : "";
                        String routeLongName = campi.length > 3 ? campi[3].trim().replaceAll("\"", "") : "";
                        String routeDesc = campi.length > 4 ? campi[4].trim().replaceAll("\"", "") : "";
                        int routeType = campi.length > 5 ? parseRouteType(campi[5].trim()) : 3;
                        String routeUrl = campi.length > 6 ? campi[6].trim().replaceAll("\"", "") : "";
                        String routeColor = campi.length > 7 ? campi[7].trim().replaceAll("\"", "") : "000000";
                        String routeTextColor = campi.length > 8 ? campi[8].trim().replaceAll("\"", "") : "FFFFFF";

                        Route rotta = new Route(
                                routeId,
                                agencyId,
                                routeShortName,
                                routeLongName,
                                routeDesc,
                                routeType,
                                routeUrl,
                                routeColor,
                                routeTextColor
                        );
                        rotte.add(rotta);

                    } catch (Exception e) {
                        // Ignora righe errate
                    }
                }

                System.out.println("Caricate " + rotte.size() + " rotte da static_gtfs/routes.txt");

            }

        } catch (Exception e) {
            System.err.println("Errore nella lettura di static_gtfs/routes.txt: " + e.getMessage());
        }

        return rotte;
    }


    //  LETTURA GTFS STATICI - SHAPES

    public static Map<String, ShapeRoute> leggiShapeDaFile() {
        Map<String, ShapeRoute> shapes = new HashMap<>();

        try (BufferedReader reader = apriFileGTFS("shapes.txt")) {

            String line;
            boolean primaLinea = true;

            while ((line = reader.readLine()) != null) {
                if (primaLinea) {
                    primaLinea = false;
                    continue;
                }

                try {
                    String[] campi = line.split(",");

                    String shapeId = campi[0].trim();
                    double shapePtLat = Double.parseDouble(campi[1].trim());
                    double shapePtLon = Double.parseDouble(campi[2].trim());
                    int shapePtSequence = Integer.parseInt(campi[3].trim());
                    double shapeDistTraveled = (campi.length > 4 && !campi[4].trim().isEmpty())
                            ? Double.parseDouble(campi[4].trim())
                            : 0.0;

                    shapes.computeIfAbsent(shapeId, ShapeRoute::new)
                            .aggiungiPunto(shapePtLat, shapePtLon, shapePtSequence, shapeDistTraveled);

                } catch (Exception e) {
                    // Ignora righe errate
                }
            }

            // Ordina tutti i punti di ogni shape dopo la lettura
            for (ShapeRoute shape : shapes.values()) {
                shape.ordinaPunti();
            }

            System.out.println("Caricate " + shapes.size() + " forme da static_gtfs/shapes.txt");

        } catch (Exception e) {
            System.err.println("Errore nella lettura di static_gtfs/shapes.txt: " + e.getMessage());
        }

        return shapes;
    }


    //  LETTURA GTFS STATICI - CALENDAR

    public static Map<String, CalendarDate> leggiCalendarDaFile() {
        Map<String, CalendarDate> calendari = new HashMap<>();

        try {
            URL resourceUrl = Database.class.getClassLoader().getResource("static_gtfs/calendar.txt");
            if (resourceUrl == null) {
                System.err.println("Errore: File static_gtfs/calendar.txt non trovato in resources!");
                return calendari;
            }

            File file = new File(resourceUrl.toURI());
            BufferedReader reader = new BufferedReader(new FileReader(file));

            String line;
            boolean primaLinea = true;

            while ((line = reader.readLine()) != null) {
                if (primaLinea) {
                    primaLinea = false;
                    continue;
                }

                try {
                    String[] campi = line.split(",");

                    String serviceId = campi[0].trim();
                    boolean monday = CalendarDate.parseBooleanGTFS(campi[1]);
                    boolean tuesday = CalendarDate.parseBooleanGTFS(campi[2]);
                    boolean wednesday = CalendarDate.parseBooleanGTFS(campi[3]);
                    boolean thursday = CalendarDate.parseBooleanGTFS(campi[4]);
                    boolean friday = CalendarDate.parseBooleanGTFS(campi[5]);
                    boolean saturday = CalendarDate.parseBooleanGTFS(campi[6]);
                    boolean sunday = CalendarDate.parseBooleanGTFS(campi[7]);
                    LocalDate startDate = CalendarDate.parseDataGTFS(campi[8].trim());
                    LocalDate endDate = CalendarDate.parseDataGTFS(campi[9].trim());

                    CalendarDate cal = new CalendarDate(serviceId, monday, tuesday, wednesday, thursday,
                            friday, saturday, sunday, startDate, endDate);
                    calendari.put(serviceId, cal);

                } catch (Exception e) {
                    // Ignora
                }
            }

            reader.close();
            System.out.println("Caricati " + calendari.size() + " calendari da static_gtfs/calendar.txt");

        } catch (Exception e) {
            System.err.println("Errore nella lettura di static_gtfs/calendar.txt: " + e.getMessage());
        }

        return calendari;
    }

    public static List<CalendarDate> leggiCalendarDatesDaFile() {
        List<CalendarDate> eccezioni = new ArrayList<>();

        try (BufferedReader reader = apriFileGTFS("calendar.txt")) {

            String line;
            boolean primaLinea = true;

            while ((line = reader.readLine()) != null) {
                if (primaLinea) {
                    primaLinea = false;
                    continue;
                }

                try {
                    String[] campi = line.split(",");

                    String serviceId = campi[0].trim();
                    LocalDate date = CalendarDate.parseDataGTFS(campi[1].trim());
                    int exceptionType = Integer.parseInt(campi[2].trim());

                    CalendarDate eccezione = new CalendarDate(serviceId, date, exceptionType);
                    eccezioni.add(eccezione);

                } catch (Exception e) {
                    // Ignora righe errate
                }
            }

        } catch (Exception e) {

        }

        return eccezioni;
    }


    //  LETTURA GTFS STATICI - STOP TIMES

    public static List<StopTime> leggiStopTimesDaFile() {
        List<StopTime> stopTimes = new ArrayList<>();

        try (BufferedReader reader = apriFileGTFS("stop_times.txt")) {

            String line;
            boolean primaLinea = true;

            while ((line = reader.readLine()) != null) {
                if (primaLinea) {
                    primaLinea = false;
                    continue;
                }

                try {
                    String[] campi = line.split(",");

                    String tripId = campi[0].trim();
                    var arrivalTime = StopTime.parseTempoGTFS(campi[1].trim());
                    var departureTime = StopTime.parseTempoGTFS(campi[2].trim());
                    String stopId = campi[3].trim();
                    int stopSequence = Integer.parseInt(campi[4].trim());
                    String stopHeadsign = campi.length > 5 ? campi[5].trim() : "";
                    int pickupType = campi.length > 6 ? StopTime.parsePickupDropoffType(campi[6]) : 0;
                    int dropOffType = campi.length > 7 ? StopTime.parsePickupDropoffType(campi[7]) : 0;
                    double shapeDistTraveled = (campi.length > 8 && !campi[8].trim().isEmpty())
                            ? Double.parseDouble(campi[8].trim())
                            : 0.0;

                    StopTime stopTime = new StopTime(
                            tripId,
                            arrivalTime,
                            departureTime,
                            stopId,
                            stopSequence,
                            stopHeadsign,
                            pickupType,
                            dropOffType,
                            shapeDistTraveled
                    );

                    stopTimes.add(stopTime);

                } catch (Exception e) {
                    // Ignora righe errate
                }
            }

            System.out.println("Caricati " + stopTimes.size() + " stop times da static_gtfs/stop_times.txt");

        } catch (Exception e) {
            System.err.println("Errore nella lettura di static_gtfs/stop_times.txt: " + e.getMessage());
        }

        return stopTimes;
    }


    // LETTURA GTFS STATICI - TRIPS

    public static List<Trip> leggiTripsDaFile() {
        List<Trip> trips = new ArrayList<>();

        try (BufferedReader reader = apriFileGTFS("trips.txt")) {

            String line;
            boolean primaLinea = true;

            while ((line = reader.readLine()) != null) {
                if (primaLinea) {
                    primaLinea = false;
                    continue;
                }

                try {
                    String[] campi = line.split(",");

                    String routeId = campi[0].trim();
                    String serviceId = campi[1].trim();
                    String tripId = campi[2].trim();
                    String tripHeadsign = campi.length > 3 ? campi[3].trim() : "";
                    String tripShortName = campi.length > 4 ? campi[4].trim() : "";
                    int directionId = campi.length > 5 ? Integer.parseInt(campi[5].trim()) : 0;
                    String blockId = campi.length > 6 ? campi[6].trim() : "";
                    String shapeId = campi.length > 7 ? campi[7].trim() : "";
                    int wheelchairAccessible = campi.length > 8 ? Integer.parseInt(campi[8].trim()) : 0;
                    int bikesAllowed = campi.length > 9 ? Integer.parseInt(campi[9].trim()) : 0;

                    Trip trip = new Trip(
                            routeId,
                            serviceId,
                            tripId,
                            tripHeadsign,
                            tripShortName,
                            directionId,
                            blockId,
                            shapeId,
                            wheelchairAccessible,
                            bikesAllowed
                    );

                    trips.add(trip);

                } catch (Exception e) {
                    // Ignora righe errate
                }
            }

            System.out.println("Caricate " + trips.size() + " corse da static_gtfs/trips.txt");

        } catch (Exception e) {
            System.err.println("Errore nella lettura di static_gtfs/trips.txt: " + e.getMessage());
        }

        return trips;
    }

    public static void popolaStopTimePerTrip(List<Trip> trips, List<StopTime> stopTimes) {
        Map<String, Trip> tripMap = new HashMap<>();
        for (Trip t : trips) {
            tripMap.put(t.getTripId(), t);
        }

        for (StopTime st : stopTimes) {
            Trip trip = tripMap.get(st.getTripId());
            if (trip != null) {
                trip.aggiungiStopTime(st);
            }
        }
    }

    // METODI DI RICERCA

    public static List<Fermate> ricercaFermatePerNome(List<Fermate> fermate, String nomeParziale) {
        List<Fermate> risultati = new ArrayList<>();
        String ricerca = nomeParziale.toLowerCase();

        for (Fermate f : fermate) {
            if (f.getStopName().toLowerCase().contains(ricerca)) {
                System.out.println("Fermata trovata: " + f.getStopName() + " (type: " + f.getLocationType() + ")");

                risultati.add(f);
            }
        }

        return risultati;
    }


    public static List<Route> ricercaRottePerNome(List<Route> rotte, String nomeParziale) {
        List<Route> risultati = new ArrayList<>();
        String ricerca = nomeParziale.toLowerCase();

        for (Route r : rotte) {
            if (r.getRouteShortName().toLowerCase().contains(ricerca) ||
                    r.getRouteLongName().toLowerCase().contains(ricerca)) {
                risultati.add(r);
            }
        }

        return risultati;
    }

    // Ricerca solo linee Metro e Tram
    public static List<Route> ricercaRotteMetroTram(List<Route> rotte, String nomeParziale) {
        List<Route> risultati = new ArrayList<>();
        if (nomeParziale == null || nomeParziale.isBlank()) return risultati;

        String ricerca = nomeParziale.toLowerCase();

        for (Route r : rotte) {
            int type = r.getRouteType();
            // Metro = 1, Tram = 0
            if (type == 0 || type == 1) {
                String shortName = r.getRouteShortName() != null ? r.getRouteShortName().toLowerCase() : "";
                String longName = r.getRouteLongName() != null ? r.getRouteLongName().toLowerCase() : "";
                String id = r.getRouteId() != null ? r.getRouteId().toLowerCase() : "";

                if (shortName.contains(ricerca) || longName.contains(ricerca) || id.contains(ricerca)) {
                    risultati.add(r);
                }
            }
        }

        System.out.println("Trovate " + risultati.size() + " linee metro/tram per ricerca \"" + nomeParziale + "\"");
        return risultati;
    }

    //  METODI HELPER


     //Parsa il route type in modo robusto

    private static int parseRouteType(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception e) {
            return 3; // Default: Bus
        }
    }



     // Apre un file GTFS dalla cartella resources/static_gtfs e restituisce un BufferedReader.
     //Usa un try-catch leggero e gestisce il percorso in modo portabile.

    private static BufferedReader apriFileGTFS(String nomeFile) throws IOException {
        InputStream inputStream = Database.class.getResourceAsStream("/static_gtfs/" + nomeFile);
        if (inputStream == null) {
            throw new FileNotFoundException("File static_gtfs/" + nomeFile + " non trovato!");
        }
        return new BufferedReader(new InputStreamReader(inputStream, java.nio.charset.StandardCharsets.UTF_8));
    }

}
