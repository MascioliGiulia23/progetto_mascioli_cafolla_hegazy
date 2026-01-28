package service;

import com.google.transit.realtime.GtfsRealtime.*;
import model.gtfs.Route;
import model.gtfs.StopTime;
import model.gtfs.Trip;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test della classe RealTimeDelayService")
class RealTimeDelayServiceTest {

    @Test
    @DisplayName("getAllDelaysForStop - match esatto per stop + fallback quando la coppia trip/stop è valida")
    void testGetAllDelaysForStopExactAndFallback() {
        // Static GTFS: Trip T1 su route R1 (shortName 64), Trip T2 su route R2 (shortName A)
        Trip t1 = new Trip("R1", "SVC", "T1", "H");
        Trip t2 = new Trip("R2", "SVC", "T2", "H");

        Route r1 = new Route("R1", "ATAC", "64", "Linea 64", "", 3, "", "", "");
        Route r2 = new Route("R2", "ATAC", "A", "Linea A", "", 3, "", "", "");

        // Coppie valide: T1#STOP1 e T2#STOP1 (così fallback passa per T2)
        StopTime st1 = new StopTime("T1", LocalTime.of(8,0), LocalTime.of(8,1), "STOP1", 1);
        StopTime st2 = new StopTime("T2", LocalTime.of(9,0), LocalTime.of(9,1), "STOP1", 1);

        List<Trip> trips = Arrays.asList(t1, t2);
        List<Route> routes = Arrays.asList(r1, r2);
        List<StopTime> stopTimes = Arrays.asList(st1, st2);

        // Feed: per T1 c'è delay ESATTO su STOP1 = 120
        // per T2 c'è delay solo su STOPX = 300 -> delayByTrip[T2]=300 -> fallback su STOP1 (coppia valida)
        FeedMessage feed = buildTripUpdateFeedExactAndFallback();

        // Fetcher finto: non usiamo bytes reali, basta un placeholder
        AtomicInteger fetchCalls = new AtomicInteger(0);
        Supplier<byte[]> fakeFetcher = () -> {
            fetchCalls.incrementAndGet();
            return new byte[]{1, 2, 3};
        };

        // Parser finto: ignora bytes e ritorna il feed costruito
        Function<byte[], FeedMessage> fakeParser = data -> feed;

        // Clock finto: sempre 0 (cache non scade)
        LongSupplier fakeClock = () -> 0L;

        RealTimeDelayService service = new RealTimeDelayService(
                trips, routes, stopTimes, fakeFetcher, fakeParser, fakeClock
        );

        Map<String, List<Integer>> delays = service.getAllDelaysForStop("STOP1");

        assertNotNull(delays);
        assertEquals(2, delays.size());

        assertTrue(delays.containsKey("64"));
        assertEquals(List.of(120), delays.get("64"));

        assertTrue(delays.containsKey("A"));
        assertEquals(List.of(300), delays.get("A"));

        assertEquals(1, fetchCalls.get());
    }

    @Test
    @DisplayName("Cache: due chiamate entro 30s fanno un solo fetch; dopo 30s rifà fetch")
    void testCacheBehavior() {
        Trip t1 = new Trip("R1", "SVC", "T1", "H");
        Route r1 = new Route("R1", "ATAC", "64", "Linea 64", "", 3, "", "", "");
        StopTime st1 = new StopTime("T1", LocalTime.of(8,0), LocalTime.of(8,1), "STOP1", 1);

        FeedMessage emptyFeed = FeedMessage.newBuilder()
                .setHeader(FeedHeader.newBuilder().setGtfsRealtimeVersion("2.0"))
                .build();

        AtomicInteger fetchCalls = new AtomicInteger(0);
        Supplier<byte[]> fakeFetcher = () -> {
            fetchCalls.incrementAndGet();
            return new byte[]{9};
        };

        Function<byte[], FeedMessage> fakeParser = data -> emptyFeed;

        // Clock controllabile
        class MutableClock implements LongSupplier {
            long now;
            @Override public long getAsLong() { return now; }
        }
        MutableClock clock = new MutableClock();

        RealTimeDelayService service = new RealTimeDelayService(
                List.of(t1), List.of(r1), List.of(st1),
                fakeFetcher, fakeParser, clock
        );

        clock.now = 0;
        service.getAllDelaysForStop("STOP1"); // fetch 1

        clock.now = 10_000;
        service.getAllDelaysForStop("STOP1"); // cache -> no fetch

        clock.now = 31_000;
        service.getAllDelaysForStop("STOP1"); // scaduta -> fetch 2

        assertEquals(2, fetchCalls.get());
    }

    @Test
    @DisplayName("getDelaysByTripId - chiave linea#orarioProgrammato e delay")
    void testGetDelaysByTripIdComputesScheduledTimeKey() {
        Route r1 = new Route("RID_RT", "ATAC", "64", "Linea 64", "", 3, "", "", "");
        Trip dummyTrip = new Trip("RID_STATIC", "SVC", "T1", "H"); // non usato in questo metodo
        StopTime st = new StopTime("T1", LocalTime.of(8,0), LocalTime.of(8,1), "STOP1", 1);

        // Feed: routeId presente nel TripDescriptor, delay=60, time=epochX
        long epochTime = 1_700_000_000L; // valore stabile
        int delay = 60;

        FeedMessage feed = buildTripUpdateFeedForDelaysByTripId("RID_RT", "STOP1", delay, epochTime);

        Supplier<byte[]> fakeFetcher = () -> new byte[]{7};
        Function<byte[], FeedMessage> fakeParser = data -> feed;
        LongSupplier fakeClock = () -> 0L;

        RealTimeDelayService service = new RealTimeDelayService(
                List.of(dummyTrip),
                List.of(r1),
                List.of(st),
                fakeFetcher,
                fakeParser,
                fakeClock
        );

        Map<String, Integer> map = service.getDelaysByTripId("STOP1");

        assertNotNull(map);
        assertEquals(1, map.size());

        // Calcolo atteso della chiave usando la stessa logica (deterministico)
        long scheduledCorrect = epochTime - delay;
        String hhmm = java.time.Instant.ofEpochSecond(scheduledCorrect)
                .atZone(java.time.ZoneId.of("Europe/Rome"))
                .toLocalTime()
                .format(java.time.format.DateTimeFormatter.ofPattern("HH:mm"));

        String expectedKey = "64#" + hhmm;

        assertTrue(map.containsKey(expectedKey));
        assertEquals(delay, map.get(expectedKey));
    }

    // ==================== Helpers ====================

    private static FeedMessage buildTripUpdateFeedExactAndFallback() {
        // T1: stop STOP1 delay 120 (match esatto)
        TripDescriptor td1 = TripDescriptor.newBuilder()
                .setTripId("T1")
                .build();

        TripUpdate.StopTimeEvent arr1 = TripUpdate.StopTimeEvent.newBuilder()
                .setDelay(120)
                .build();

        TripUpdate.StopTimeUpdate stu1 = TripUpdate.StopTimeUpdate.newBuilder()
                .setStopId("STOP1")
                .setArrival(arr1)
                .build();

        TripUpdate tu1 = TripUpdate.newBuilder()
                .setTrip(td1)
                .addStopTimeUpdate(stu1)
                .build();

        FeedEntity e1 = FeedEntity.newBuilder()
                .setId("e1")
                .setTripUpdate(tu1)
                .build();

        // T2: nessun STOP1, ma delay su STOPX=300 -> delayByTrip[T2]=300 (fallback)
        TripDescriptor td2 = TripDescriptor.newBuilder()
                .setTripId("T2")
                .build();

        TripUpdate.StopTimeEvent arr2 = TripUpdate.StopTimeEvent.newBuilder()
                .setDelay(300)
                .build();

        TripUpdate.StopTimeUpdate stu2 = TripUpdate.StopTimeUpdate.newBuilder()
                .setStopId("STOPX")
                .setArrival(arr2)
                .build();

        TripUpdate tu2 = TripUpdate.newBuilder()
                .setTrip(td2)
                .addStopTimeUpdate(stu2)
                .build();

        FeedEntity e2 = FeedEntity.newBuilder()
                .setId("e2")
                .setTripUpdate(tu2)
                .build();

        return FeedMessage.newBuilder()
                .setHeader(FeedHeader.newBuilder().setGtfsRealtimeVersion("2.0"))
                .addEntity(e1)
                .addEntity(e2)
                .build();
    }

    private static FeedMessage buildTripUpdateFeedForDelaysByTripId(String routeId, String stopId, int delay, long epochTime) {
        TripDescriptor td = TripDescriptor.newBuilder()
                .setTripId("ANY")
                .setRouteId(routeId)
                .build();

        TripUpdate.StopTimeEvent arr = TripUpdate.StopTimeEvent.newBuilder()
                .setDelay(delay)
                .setTime(epochTime)
                .build();

        TripUpdate.StopTimeUpdate stu = TripUpdate.StopTimeUpdate.newBuilder()
                .setStopId(stopId)
                .setArrival(arr)
                .build();

        TripUpdate tu = TripUpdate.newBuilder()
                .setTrip(td)
                .addStopTimeUpdate(stu)
                .build();

        FeedEntity e = FeedEntity.newBuilder()
                .setId("e")
                .setTripUpdate(tu)
                .build();

        return FeedMessage.newBuilder()
                .setHeader(FeedHeader.newBuilder().setGtfsRealtimeVersion("2.0"))
                .addEntity(e)
                .build();
    }
}

