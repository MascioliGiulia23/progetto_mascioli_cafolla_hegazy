package service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

//(serve per i test)
import java.util.Objects;                 // serve per i test
import java.util.function.BiFunction;     // serve per i test


//Servizio per scaricare i feed GTFS Realtime da Roma Mobilità

public class RealTimeFetcher {

    private final String tripUrl;
    private final String vehicleUrl;
    private final HttpClient httpClient;

    // DIPENDENZA INIETTABILE (serve per i test)
    // Default = httpClient.send(...) reale, quindi l'app NON cambia.
    private BiFunction<HttpRequest, HttpResponse.BodyHandler<byte[]>, HttpResponse<byte[]>> sender; // serve per i test


    public RealTimeFetcher(String tripUrl, String vehicleUrl) {
        this.tripUrl = tripUrl;
        this.vehicleUrl = vehicleUrl;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.ALWAYS) //  segue 301/302/307/308
                .build();

        // Default runtime (serve per i test)
        this.sender = (req, handler) -> { // serve per i test
            try {
                return httpClient.send(req, handler);
            } catch (IOException | InterruptedException e) {
                // Manteniamo la firma dei metodi: rilanciamo come RuntimeException e poi la riconvertiamo dove serve
                throw new RuntimeException(e);
            }
        }; // serve per i test
    }

    // COSTRUTTORE PER TEST (serve per i test)
    // Non rompe nulla: è un overload usato solo nei test.
    RealTimeFetcher(String tripUrl, String vehicleUrl,
                    BiFunction<HttpRequest, HttpResponse.BodyHandler<byte[]>, HttpResponse<byte[]>> sender) { // serve per i test
        this.tripUrl = tripUrl;
        this.vehicleUrl = vehicleUrl;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.sender = Objects.requireNonNull(sender); // serve per i test
    }



     //Scarica un feed da URL
    public byte[] fetch(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        HttpResponse<byte[]> response;
        try {
            response = sender.apply(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (RuntimeException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) throw (IOException) cause;
            if (cause instanceof InterruptedException) throw (InterruptedException) cause;
            throw e;
        }


        int code = response.statusCode();
        if (code < 200 || code >= 300) { // solo 2xx ok
            throw new IOException("HTTP error " + code);
        }

        return response.body();
    }


     //Scarica il feed TripUpdates

    public byte[] fetchTripFeed() throws IOException, InterruptedException {
        return fetch(tripUrl);
    }


     //Scarica il feed VehiclePositions
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
