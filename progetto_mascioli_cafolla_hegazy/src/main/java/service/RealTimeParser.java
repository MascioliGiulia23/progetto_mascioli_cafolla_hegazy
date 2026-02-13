package service;

import com.google.transit.realtime.GtfsRealtime.FeedMessage;
import java.io.IOException;


 // Servizio per parsare i feed GTFS Realtime (formato Protobuf)

public class RealTimeParser {

    //Parsa il feed TripUpdates
    public FeedMessage parseTripFeed(byte[] data) throws IOException {
        return FeedMessage.parseFrom(data);
    }


     //Parsa il feed VehiclePositions

    public FeedMessage parseVehicleFeed(byte[] data) throws IOException {
        return FeedMessage.parseFrom(data);
    }
}
