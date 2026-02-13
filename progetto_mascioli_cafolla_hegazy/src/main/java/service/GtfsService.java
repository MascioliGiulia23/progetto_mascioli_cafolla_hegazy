package service;

import model.gtfs.*;
import model.utils.Database;

import java.util.*;

// >>> NUOVE IMPORT (serve per i test)
import java.util.Objects;                  // serve per i test
import java.util.function.BiConsumer;      // serve per i test
import java.util.function.Supplier;        // serve per i test

// Classe di servizio dedicata al caricamento dei dati GTFS.
public class GtfsService {

    private List<Fermate> fermate;
    private List<Route> rotte;
    private Map<String, ShapeRoute> forme;
    private List<Trip> trips;
    private List<StopTime> stopTimes;
    private List<CalendarDate> eccezioni;
    private Map<String, List<StopTime>> stopTimesPerStopId;

    // DIPENDENZE INIETTABILI (serve per i test)
    // Default = comportamento originale basato su Database

    private static Supplier<List<Fermate>> fermateLoader = Database::leggiStopDaFile; // serve per i test
    private static Supplier<List<Route>> rotteLoader = Database::leggiRouteDaFile;    // serve per i test
    private static Supplier<Map<String, ShapeRoute>> formeLoader = Database::leggiShapeDaFile; // serve per i test
    private static Supplier<List<Trip>> tripsLoader = Database::leggiTripsDaFile;     // serve per i test
    private static Supplier<List<StopTime>> stopTimesLoader = Database::leggiStopTimesDaFile; // serve per i test
    private static Supplier<List<CalendarDate>> eccezioniLoader = Database::leggiCalendarDatesDaFile; // serve per i test

    private static BiConsumer<List<Trip>, List<StopTime>> stopTimesTripPopulator =
            Database::popolaStopTimePerTrip; // serve per i test

    // ====== TEST HOOKS (serve per i test) ======
    static void setLoadersForTest( // serve per i test
                                   Supplier<List<Fermate>> fermateL,
                                   Supplier<List<Route>> rotteL,
                                   Supplier<Map<String, ShapeRoute>> formeL,
                                   Supplier<List<Trip>> tripsL,
                                   Supplier<List<StopTime>> stopTimesL,
                                   Supplier<List<CalendarDate>> eccezioniL,
                                   BiConsumer<List<Trip>, List<StopTime>> populator) {

        fermateLoader = Objects.requireNonNull(fermateL);       // serve per i test
        rotteLoader = Objects.requireNonNull(rotteL);           // serve per i test
        formeLoader = Objects.requireNonNull(formeL);           // serve per i test
        tripsLoader = Objects.requireNonNull(tripsL);           // serve per i test
        stopTimesLoader = Objects.requireNonNull(stopTimesL);   // serve per i test
        eccezioniLoader = Objects.requireNonNull(eccezioniL);   // serve per i test
        stopTimesTripPopulator = Objects.requireNonNull(populator); // serve per i test
    }

    static void resetForTest() { // serve per i test
        fermateLoader = Database::leggiStopDaFile; // serve per i test
        rotteLoader = Database::leggiRouteDaFile; // serve per i test
        formeLoader = Database::leggiShapeDaFile; // serve per i test
        tripsLoader = Database::leggiTripsDaFile; // serve per i test
        stopTimesLoader = Database::leggiStopTimesDaFile; // serve per i test
        eccezioniLoader = Database::leggiCalendarDatesDaFile; // serve per i test
        stopTimesTripPopulator = Database::popolaStopTimePerTrip; // serve per i test
    }

    public GtfsService() {
        caricaDatiGTFS();
    }

    //Legge tutti i file GTFS dal Database
    private void caricaDatiGTFS() {
        System.out.println("Caricamento dati GTFS...");

        //  usa i loader (default = Database.* come prima)
        fermate = fermateLoader.get();     // serve per i test
        rotte = rotteLoader.get();         // serve per i test
        forme = formeLoader.get();         // serve per i test
        trips = tripsLoader.get();         // serve per i test
        stopTimes = stopTimesLoader.get(); // serve per i test
        eccezioni = eccezioniLoader.get(); // serve per i test

        // Collega StopTime ai Trip (default = Database.popolaStopTimePerTrip come prima)
        stopTimesTripPopulator.accept(trips, stopTimes); // serve per i test

        // Indicizza StopTime per fermata
        stopTimesPerStopId = new HashMap<>();
        for (StopTime st : stopTimes) {
            stopTimesPerStopId
                    .computeIfAbsent(st.getStopId(), k -> new ArrayList<>())
                    .add(st);
        }

        System.out.println("Dati GTFS caricati con successo!\n");
    }

    // GETTER
    public List<Fermate> getFermate() { return fermate; }
    public List<Route> getRotte() { return rotte; }
    public Map<String, ShapeRoute> getForme() { return forme; }
    public List<Trip> getTrips() { return trips; }
    public List<StopTime> getStopTimes() { return stopTimes; }
    public List<CalendarDate> getEccezioni() { return eccezioni; }
    public Map<String, List<StopTime>> getStopTimesPerStopId() { return stopTimesPerStopId; }
}
