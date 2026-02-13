package view.map;

import model.gtfs.ShapeRoute;
import model.gtfs.ShapeRoute.GeoPoint;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;


 //Classe che gestisce il disegno di una o più tratte (shapes GTFS) sulla mappa.
 //Non modifica JXMapViewer, ma agisce come "overlay painter".

public class RouteDrawer {

    private final JXMapViewer mapViewer;
    private final List<Painter<JXMapViewer>> painters = new ArrayList<>();

    public RouteDrawer(JXMapViewer mapViewer) {
        this.mapViewer = mapViewer;
    }


     // Disegna una singola shape sulla mappa (tratta bus)
     // @param shapeRoute La shape da disegnare
     //@param color      Il colore della linea (es. Color.BLUE)
    public void drawShape(ShapeRoute shapeRoute, Color color) {
        if (shapeRoute == null || shapeRoute.getGeoPoints().isEmpty()) {
            System.err.println("Shape vuota o nulla, impossibile disegnare!");
            return;
        }

        // Converte i punti in GeoPosition
        List<GeoPosition> geoPositions = new ArrayList<>();
        for (GeoPoint p : shapeRoute.getGeoPoints()) {
            geoPositions.add(new GeoPosition(p.getLatitude(), p.getLongitude()));
        }

        // >>> usa metodo comune (serve per i test)
        drawGeoPositions(geoPositions, color); // serve per i test

        System.out.println("Tratta disegnata per shape_id=" + shapeRoute.getShapeId() +
                " (" + geoPositions.size() + " punti)");
    }

    // NUOVO METODO (serve per i test)
    // Permette di testare RouteDrawer senza dipendere da ShapeRoute
    void drawGeoPositions(List<GeoPosition> geoPositions, Color color) { // serve per i test
        if (geoPositions == null || geoPositions.isEmpty()) { // serve per i test
            System.err.println("Track vuota o nulla, impossibile disegnare!"); // serve per i test
            return; // serve per i test
        }

        // Crea un nuovo painter
        RoutePainter routePainter = new RoutePainter(new ArrayList<>(geoPositions), color); // serve per i test
        painters.add(routePainter); // serve per i test

        // Aggiorna overlay sulla mappa
        CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter<>(painters); // serve per i test
        mapViewer.setOverlayPainter(compoundPainter); // serve per i test

        // Centra la mappa sulla shape
        GeoPosition centro = geoPositions.get(geoPositions.size() / 2); // serve per i test
        mapViewer.setAddressLocation(centro); // serve per i test
    }


     //Cancella tutte le linee disegnate sulla mappa

    public void clearAll() {
        painters.clear();
        mapViewer.setOverlayPainter(null);
        System.out.println(" Tutte le tratte rimosse dalla mappa.");
    }

    // Restituisce i punti dell'ultima shape disegnata
    public List<GeoPosition> getGeoPositions() {
        if (painters.isEmpty()) return new ArrayList<>();

        // Trova l'ultimo painter (l'ultima linea disegnata)
        Painter<JXMapViewer> lastPainter = painters.get(painters.size() - 1);
        if (lastPainter instanceof RoutePainter rp) {
            return rp.track;
        }
        return new ArrayList<>();
    }

    //getter di supporto (serve per i test)
    int getPaintersCountForTest() { // serve per i test
        return painters.size(); // serve per i test
    }


    //Classe interna che disegna una singola linea sulla mappa
    public static class RoutePainter implements Painter<JXMapViewer> {
        private final List<GeoPosition> track;
        private final Color color;

        public RoutePainter(List<GeoPosition> track, Color color) {
            this.track = track;
            this.color = color;
        }

        @Override
        public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
            g = (Graphics2D) g.create();
            g.setColor(color);
            g.setStroke(new BasicStroke(3));

            Rectangle rect = map.getViewportBounds();
            g.translate(-rect.x, -rect.y);

            Point2D lastPoint = null;
            for (GeoPosition gp : track) {
                Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
                if (lastPoint != null) {
                    g.drawLine((int) lastPoint.getX(), (int) lastPoint.getY(),
                            (int) pt.getX(), (int) pt.getY());
                }
                lastPoint = pt;
            }

            g.dispose();
        }
    }
}
