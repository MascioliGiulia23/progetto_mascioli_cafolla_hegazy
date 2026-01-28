package service;

import com.google.transit.realtime.GtfsRealtime.FeedHeader;
import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test della classe RealTimeParser")
class RealTimeParserTest {

    @Test
    @DisplayName("parseTripFeed - bytes validi -> ritorna FeedMessage")
    void testParseTripFeedValid() throws Exception {
        RealTimeParser parser = new RealTimeParser();

        FeedMessage original = FeedMessage.newBuilder()
                .setHeader(FeedHeader.newBuilder().setGtfsRealtimeVersion("2.0").build())
                .build();

        byte[] data = original.toByteArray();

        FeedMessage parsed = parser.parseTripFeed(data);

        assertNotNull(parsed);
        assertEquals("2.0", parsed.getHeader().getGtfsRealtimeVersion());
        assertEquals(0, parsed.getEntityCount());
    }

    @Test
    @DisplayName("parseVehicleFeed - bytes validi -> ritorna FeedMessage")
    void testParseVehicleFeedValid() throws Exception {
        RealTimeParser parser = new RealTimeParser();

        FeedMessage original = FeedMessage.newBuilder()
                .setHeader(FeedHeader.newBuilder().setGtfsRealtimeVersion("2.0").build())
                .build();

        byte[] data = original.toByteArray();

        FeedMessage parsed = parser.parseVehicleFeed(data);

        assertNotNull(parsed);
        assertEquals("2.0", parsed.getHeader().getGtfsRealtimeVersion());
    }

    @Test
    @DisplayName("parseTripFeed - bytes invalidi -> lancia IOException")
    void testParseTripFeedInvalid() {
        RealTimeParser parser = new RealTimeParser();

        // bytes non protobuf validi
        byte[] invalid = new byte[]{1, 2, 3, 4, 5};

        assertThrows(IOException.class, () -> parser.parseTripFeed(invalid));
    }

    @Test
    @DisplayName("parseVehicleFeed - bytes invalidi -> lancia IOException")
    void testParseVehicleFeedInvalid() {
        RealTimeParser parser = new RealTimeParser();

        byte[] invalid = new byte[]{9, 9, 9};

        assertThrows(IOException.class, () -> parser.parseVehicleFeed(invalid));
    }

    @Test
    @DisplayName("parseTripFeed - data null -> lancia NullPointerException (comportamento protobuf)")
    void testParseTripFeedNull() {
        RealTimeParser parser = new RealTimeParser();
        assertThrows(NullPointerException.class, () -> parser.parseTripFeed(null));
    }
}
