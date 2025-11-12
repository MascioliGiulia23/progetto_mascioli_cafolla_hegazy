package service;

import model.gtfs.*;
import model.utils.Database;

import java.util.*;

//Classe di servizio dedicata al caricamento dei dati GTFS.

public class GtfsService {

    private List<Fermate> fermate;
    private List<Route> rotte;
    private Map<String, ShapeRoute> forme;
    private List<Trip> trips;
    private List<StopTime> stopTimes;
    private List<CalendarDate> eccezioni;
    private Map<String, List<StopTime>> stopTimesPerStopId;

    public GtfsService() {
        caricaDatiGTFS();
    }

    /** Legge tutti i file GTFS dal Database */
    private void caricaDatiGTFS() {
        System.out.println("Caricamento dati GTFS...");

        fermate = Database.leggiStopDaFile();
        rotte = Database.leggiRouteDaFile();
        forme = Database.leggiShapeDaFile();
        trips = Database.leggiTripsDaFile();
        stopTimes = Database.leggiStopTimesDaFile();
        eccezioni = Database.leggiCalendarDatesDaFile();

        // Collega StopTime ai Trip
        Database.popolaStopTimePerTrip(trips, stopTimes);

        // Indicizza StopTime per fermata
        stopTimesPerStopId = new HashMap<>();
        for (StopTime st : stopTimes) {
            stopTimesPerStopId
                    .computeIfAbsent(st.getStopId(), k -> new ArrayList<>())
                    .add(st);
        }

        System.out.println("✓ Dati GTFS caricati con successo!\n");
    }

    // GETTER

    public List<Fermate> getFermate() {
        return fermate;
    }

    public List<Route> getRotte() {
        return rotte;
    }

    public Map<String, ShapeRoute> getForme() {
        return forme;
    }

    public List<Trip> getTrips() {
        return trips;
    }

    public List<StopTime> getStopTimes() {
        return stopTimes;
    }

    public List<CalendarDate> getEccezioni() {
        return eccezioni;
    }

    public Map<String, List<StopTime>> getStopTimesPerStopId() {
        return stopTimesPerStopId;
    }
}
