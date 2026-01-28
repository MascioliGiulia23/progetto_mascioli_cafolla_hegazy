package service;

import com.google.transit.realtime.GtfsRealtime;
import org.jxmapviewer.viewer.GeoPosition;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test della classe GtfsRealtimeService")
class GtfsRealtimeServiceTest {

    @AfterEach
    void tearDown() {
        // Ripristina sempre comportamento reale dopo ogni test
        GtfsRealtimeService.resetForTest();
    }

    @Test
    @DisplayName("getBusPositions - ritorna solo i vehicle che hanno position")
    void testGetBusPositionsFiltersCorrectly() throws Exception {
        byte[] feedBytes = buildFeedBytesWithMixedEntities();

        // Inietto uno stream finto (no rete)
        GtfsRealtimeService.setFeedStreamSupplierForTest(() -> new ByteArrayInputStream(feedBytes));

        List<GeoPosition> positions = GtfsRealtimeService.getBusPositions();

        // Nel feed costruito: 2 posizioni valide
        assertNotNull(positions);
        assertEquals(2, positions.size());

        // Verifico coordinate
        GeoPosition p1 = positions.get(0);
        GeoPosition p2 = positions.get(1);

        assertEquals(41.9000, p1.getLatitude(), 0.0001);
        assertEquals(12.5000, p1.getLongitude(), 0.0001);

        assertEquals(41.9100, p2.getLatitude(), 0.0001);
        assertEquals(12.5300, p2.getLongitude(), 0.0001);
    }

    @Test
    @DisplayName("getBusPositions - se lo stream lancia eccezione ritorna lista vuota e non crasha")
    void testGetBusPositionsOnExceptionReturnsEmpty() {
        // Supplier che simula errore (niente rete)
        GtfsRealtimeService.setFeedStreamSupplierForTest(() -> {
            throw new RuntimeException("Errore simulato");
        });

        assertDoesNotThrow(() -> {
            List<GeoPosition> positions = GtfsRealtimeService.getBusPositions();
            assertNotNull(positions);
            assertTrue(positions.isEmpty());
        });
    }

    // ==================== Helpers ====================

    private static byte[] buildFeedBytesWithMixedEntities() throws Exception {
        // Entity 1: vehicle con position (valida)
        GtfsRealtime.VehiclePosition vp1 = GtfsRealtime.VehiclePosition.newBuilder()
                .setPosition(GtfsRealtime.Position.newBuilder()
                        .setLatitude(41.9000f)
                        .setLongitude(12.5000f)
                        .build())
                .build();

        GtfsRealtime.FeedEntity e1 = GtfsRealtime.FeedEntity.newBuilder()
                .setId("e1")
                .setVehicle(vp1)
                .build();

        // Entity 2: vehicle senza position (da ignorare)
        GtfsRealtime.VehiclePosition vpNoPos = GtfsRealtime.VehiclePosition.newBuilder().build();

        GtfsRealtime.FeedEntity e2 = GtfsRealtime.FeedEntity.newBuilder()
                .setId("e2")
                .setVehicle(vpNoPos)
                .build();

        // Entity 3: senza vehicle (da ignorare)
        GtfsRealtime.FeedEntity e3 = GtfsRealtime.FeedEntity.newBuilder()
                .setId("e3")
                .build();

        // Entity 4: vehicle con position (valida)
        GtfsRealtime.VehiclePosition vp2 = GtfsRealtime.VehiclePosition.newBuilder()
                .setPosition(GtfsRealtime.Position.newBuilder()
                        .setLatitude(41.9100f)
                        .setLongitude(12.5300f)
                        .build())
                .build();

        GtfsRealtime.FeedEntity e4 = GtfsRealtime.FeedEntity.newBuilder()
                .setId("e4")
                .setVehicle(vp2)
                .build();

        // FeedMessage
        GtfsRealtime.FeedMessage feed = GtfsRealtime.FeedMessage.newBuilder()
                .setHeader(GtfsRealtime.FeedHeader.newBuilder()
                        .setGtfsRealtimeVersion("2.0")
                        .build())
                .addEntity(e1)
                .addEntity(e2)
                .addEntity(e3)
                .addEntity(e4)
                .build();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        feed.writeTo(out);
        return out.toByteArray();
    }
}

