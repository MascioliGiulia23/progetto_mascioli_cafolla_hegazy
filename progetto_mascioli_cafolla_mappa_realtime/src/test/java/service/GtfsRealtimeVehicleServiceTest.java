package service;

import com.google.transit.realtime.GtfsRealtime;
import org.jxmapviewer.viewer.GeoPosition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test della classe GtfsRealtimeVehicleService")
class GtfsRealtimeVehicleServiceTest {

    @AfterEach
    void tearDown() {
        // Ripristina sempre comportamento reale dopo ogni test
        GtfsRealtimeVehicleService.resetForTest();
    }

    @Test
    @DisplayName("getRealtimeVehiclePositions - include solo vehicle con trip e position, applica default su campi mancanti")
    void testGetRealtimeVehiclePositionsParsesAndDefaults() throws Exception {
        byte[] feedBytes = buildFeedBytes();

        // Niente rete: stream finto
        GtfsRealtimeVehicleService.setFeedStreamSupplierForTest(() -> new ByteArrayInputStream(feedBytes));

        Map<String, GtfsRealtimeVehicleService.VehicleData> map =
                GtfsRealtimeVehicleService.getRealtimeVehiclePositions();

        assertNotNull(map);
        // Nel feed: t1 valido, t2 valido, t3 valido (routeId assente), + 2 entity ignorate
        assertEquals(3, map.size());

        // t1: tutto presente
        GtfsRealtimeVehicleService.VehicleData t1 = map.get("t1");
        assertNotNull(t1);
        assertEquals("V1", t1.getVehicleId());
        assertEquals("t1", t1.getTripId());
        assertEquals("R1", t1.getRouteId());
        assertEquals(0, t1.getDirectionId());
        assertEquals(41.9000, t1.getLatitude(), 0.0001);
        assertEquals(12.5000, t1.getLongitude(), 0.0001);
        assertEquals(45.0f, t1.getBearing(), 0.0001);

        // toGeoPosition
        GeoPosition gp = t1.toGeoPosition();
        assertEquals(41.9000, gp.getLatitude(), 0.0001);
        assertEquals(12.5000, gp.getLongitude(), 0.0001);

        // t3: routeId assente -> null, directionId assente -> -1, bearing assente -> 0, vehicle assente -> "unknown"
        GtfsRealtimeVehicleService.VehicleData t3 = map.get("t3");
        assertNotNull(t3);
        assertEquals("unknown", t3.getVehicleId());
        assertEquals("t3", t3.getTripId());
        assertNull(t3.getRouteId());
        assertEquals(-1, t3.getDirectionId());
        assertEquals(0.0f, t3.getBearing(), 0.0001);
    }

    @Test
    @DisplayName("getVehiclesForRouteAndDirection - filtra correttamente per routeId e directionId")
    void testGetVehiclesForRouteAndDirection() throws Exception {
        byte[] feedBytes = buildFeedBytes();
        GtfsRealtimeVehicleService.setFeedStreamSupplierForTest(() -> new ByteArrayInputStream(feedBytes));

        List<GtfsRealtimeVehicleService.VehicleData> dir0 =
                GtfsRealtimeVehicleService.getVehiclesForRouteAndDirection("R1", 0);

        assertNotNull(dir0);
        assertEquals(1, dir0.size());
        assertEquals("t1", dir0.get(0).getTripId());

        List<GtfsRealtimeVehicleService.VehicleData> dir1 =
                GtfsRealtimeVehicleService.getVehiclesForRouteAndDirection("R1", 1);

        assertNotNull(dir1);
        assertEquals(1, dir1.size());
        assertEquals("t2", dir1.get(0).getTripId());
    }

    @Test
    @DisplayName("getRealtimeVehiclePositions - su eccezione ritorna mappa vuota e non crasha")
    void testGetRealtimeVehiclePositionsOnException() {
        GtfsRealtimeVehicleService.setFeedStreamSupplierForTest(() -> {
            throw new RuntimeException("Errore simulato");
        });

        assertDoesNotThrow(() -> {
            Map<String, GtfsRealtimeVehicleService.VehicleData> map =
                    GtfsRealtimeVehicleService.getRealtimeVehiclePositions();
            assertNotNull(map);
            assertTrue(map.isEmpty());
        });
    }

    // ==================== Helpers ====================

    private static byte[] buildFeedBytes() throws Exception {
        // Entity 1: trip t1, route R1, direction 0, position ok, bearing ok, vehicleId V1
        GtfsRealtime.VehicleDescriptor vd1 = GtfsRealtime.VehicleDescriptor.newBuilder()
                .setId("V1")
                .build();

        GtfsRealtime.TripDescriptor td1 = GtfsRealtime.TripDescriptor.newBuilder()
                .setTripId("t1")
                .setRouteId("R1")
                .setDirectionId(0)
                .build();

        GtfsRealtime.VehiclePosition vp1 = GtfsRealtime.VehiclePosition.newBuilder()
                .setTrip(td1)
                .setVehicle(vd1)
                .setPosition(GtfsRealtime.Position.newBuilder()
                        .setLatitude(41.9000f)
                        .setLongitude(12.5000f)
                        .setBearing(45.0f)
                        .build())
                .build();

        GtfsRealtime.FeedEntity e1 = GtfsRealtime.FeedEntity.newBuilder()
                .setId("e1")
                .setVehicle(vp1)
                .build();

        // Entity 2: trip t2, route R1, direction 1, position ok, bearing assente (default 0), vehicleId V2
        GtfsRealtime.VehicleDescriptor vd2 = GtfsRealtime.VehicleDescriptor.newBuilder()
                .setId("V2")
                .build();

        GtfsRealtime.TripDescriptor td2 = GtfsRealtime.TripDescriptor.newBuilder()
                .setTripId("t2")
                .setRouteId("R1")
                .setDirectionId(1)
                .build();

        GtfsRealtime.VehiclePosition vp2 = GtfsRealtime.VehiclePosition.newBuilder()
                .setTrip(td2)
                .setVehicle(vd2)
                .setPosition(GtfsRealtime.Position.newBuilder()
                        .setLatitude(41.9100f)
                        .setLongitude(12.5300f)
                        .build())
                .build();

        GtfsRealtime.FeedEntity e2 = GtfsRealtime.FeedEntity.newBuilder()
                .setId("e2")
                .setVehicle(vp2)
                .build();

        // Entity 3: ha vehicle ma manca position -> ignorata
        GtfsRealtime.VehiclePosition vpNoPos = GtfsRealtime.VehiclePosition.newBuilder()
                .setTrip(td1)
                .build();

        GtfsRealtime.FeedEntity e3 = GtfsRealtime.FeedEntity.newBuilder()
                .setId("e3")
                .setVehicle(vpNoPos)
                .build();

        // Entity 4: ha vehicle ma manca trip -> ignorata
        GtfsRealtime.VehiclePosition vpNoTrip = GtfsRealtime.VehiclePosition.newBuilder()
                .setPosition(GtfsRealtime.Position.newBuilder()
                        .setLatitude(41.8000f)
                        .setLongitude(12.4000f)
                        .build())
                .build();

        GtfsRealtime.FeedEntity e4 = GtfsRealtime.FeedEntity.newBuilder()
                .setId("e4")
                .setVehicle(vpNoTrip)
                .build();

        // Entity 5: trip t3, routeId assente, directionId assente, vehicle assente, position ok, bearing assente
        GtfsRealtime.TripDescriptor td3 = GtfsRealtime.TripDescriptor.newBuilder()
                .setTripId("t3")
                .build();

        GtfsRealtime.VehiclePosition vp3 = GtfsRealtime.VehiclePosition.newBuilder()
                .setTrip(td3)
                .setPosition(GtfsRealtime.Position.newBuilder()
                        .setLatitude(41.7000f)
                        .setLongitude(12.3000f)
                        .build())
                .build();

        GtfsRealtime.FeedEntity e5 = GtfsRealtime.FeedEntity.newBuilder()
                .setId("e5")
                .setVehicle(vp3)
                .build();

        GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.newBuilder()
                .setHeader(GtfsRealtime.FeedHeader.newBuilder()
                        .setGtfsRealtimeVersion("2.0")
                        .build())
                .addEntity(e1)
                .addEntity(e2)
                .addEntity(e3)
                .addEntity(e4)
                .addEntity(e5)
                .build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        feed.writeTo(out);
        return out.toByteArray();
    }
}
