package service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Servizio per scaricare i feed GTFS Realtime da Roma Mobilità
 */
public class RealTimeFetcher {

    private final String tripUrl;
    private final String vehicleUrl;
    private final HttpClient httpClient;

    public RealTimeFetcher(String tripUrl, String vehicleUrl) {
        this.tripUrl = tripUrl;
        this.vehicleUrl = vehicleUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    /**
     * Scarica un feed da URL
     */
    public byte[] fetch(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        HttpResponse<byte[]> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofByteArray()
        );

        System.out.println("[RealTimeFetcher] Status: " + response.statusCode() +
                " | Size: " + response.body().length + " bytes");

        if (response.statusCode() < 200 || response.statusCode() >= 400) {
            throw new IOException("HTTP error " + response.statusCode());
        }

        return response.body();
    }

    /**
     * Scarica il feed TripUpdates
     */
    public byte[] fetchTripFeed() throws IOException, InterruptedException {
        return fetch(tripUrl);
    }

    /**
     * Scarica il feed VehiclePositions
     */
    public byte[] fetchVehicleFeed() throws IOException, InterruptedException {
        return fetch(vehicleUrl);
    }

    public String getTripUrl() {
        return tripUrl;
    }

    public String getVehicleUrl() {
        return vehicleUrl;
    }
}
