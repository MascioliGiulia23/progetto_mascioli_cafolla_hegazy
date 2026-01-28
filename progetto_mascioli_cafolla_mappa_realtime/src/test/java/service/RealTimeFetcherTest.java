package service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test della classe RealTimeFetcher")
class RealTimeFetcherTest {

    @Test
    @DisplayName("fetch - status 200 ritorna body")
    void testFetchSuccess200() throws Exception {
        byte[] body = new byte[]{1, 2, 3, 4};

        BiFunction<HttpRequest, HttpResponse.BodyHandler<byte[]>, HttpResponse<byte[]>> sender =
                (req, handler) -> fakeResponse(200, body);

        RealTimeFetcher fetcher = new RealTimeFetcher("trip", "veh", sender);

        byte[] result = fetcher.fetch("http://example.com/feed");
        assertArrayEquals(body, result);
    }

    @Test
    @DisplayName("fetch - status 404 lancia IOException")
    void testFetchHttpError() {
        BiFunction<HttpRequest, HttpResponse.BodyHandler<byte[]>, HttpResponse<byte[]>> sender =
                (req, handler) -> fakeResponse(404, new byte[]{9});

        RealTimeFetcher fetcher = new RealTimeFetcher("trip", "veh", sender);

        assertThrows(IOException.class, () -> fetcher.fetch("http://example.com/feed"));
    }

    @Test
    @DisplayName("fetch - sender lancia IOException -> fetch rilancia IOException")
    void testFetchSenderIOException() {
        BiFunction<HttpRequest, HttpResponse.BodyHandler<byte[]>, HttpResponse<byte[]>> sender =
                (req, handler) -> {
                    throw new RuntimeException(new IOException("boom"));
                };

        RealTimeFetcher fetcher = new RealTimeFetcher("trip", "veh", sender);

        assertThrows(IOException.class, () -> fetcher.fetch("http://example.com/feed"));
    }

    @Test
    @DisplayName("fetchTripFeed chiama fetch(tripUrl) e fetchVehicleFeed chiama fetch(vehicleUrl)")
    void testFetchTripAndVehicleUseCorrectUrls() throws Exception {
        class CapturingSender implements BiFunction<HttpRequest, HttpResponse.BodyHandler<byte[]>, HttpResponse<byte[]>> {
            String lastUri = null;

            @Override
            public HttpResponse<byte[]> apply(HttpRequest req, HttpResponse.BodyHandler<byte[]> handler) {
                lastUri = req.uri().toString();
                return fakeResponse(200, new byte[]{7});
            }
        }

        CapturingSender sender = new CapturingSender();
        RealTimeFetcher fetcher = new RealTimeFetcher("http://trip.url", "http://veh.url", sender);

        fetcher.fetchTripFeed();
        assertEquals("http://trip.url", sender.lastUri);

        fetcher.fetchVehicleFeed();
        assertEquals("http://veh.url", sender.lastUri);
    }

    // ==================== helper ====================

    private static HttpResponse<byte[]> fakeResponse(int status, byte[] body) {
        return new HttpResponse<>() {
            @Override
            public int statusCode() {
                return status;
            }

            @Override
            public HttpRequest request() {
                return null;
            }

            @Override
            public Optional<HttpResponse<byte[]>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                // ✅ FIX: HttpHeaders.of vuole (Map<String,List<String>>, BiPredicate<String,String>)
                return HttpHeaders.of(Map.of(), (k, v) -> true);
            }

            @Override
            public byte[] body() {
                return body;
            }

            @Override
            public Optional<javax.net.ssl.SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public URI uri() {
                return URI.create("http://fake");
            }

            @Override
            public java.net.http.HttpClient.Version version() {
                return java.net.http.HttpClient.Version.HTTP_1_1;
            }
        };
    }
}
