package view.map;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.Set;

public class WaypointDrawer {

    private final JXMapViewer mapViewer;
    private final Set<BusWaypoint> waypoints = new HashSet<>();
    private final RouteDrawer routeDrawer;


   //(serve per i test)

    private Painter<JXMapViewer> lastOverlayPainterForTest = null; // serve per i test

    public WaypointDrawer(JXMapViewer mapViewer, RouteDrawer routeDrawer) {
        this.mapViewer = mapViewer;
        this.routeDrawer = routeDrawer;
    }

    public void addWaypoint(BusWaypoint waypoint) {
        if (waypoint != null) {
            waypoints.add(waypoint);
            updatePainters();
        }
    }

    public void addWaypoints(Collection<BusWaypoint> newWaypoints) {
        if (newWaypoints != null) {
            waypoints.addAll(newWaypoints);
            updatePainters();
        }
    }

    public void clearWaypoints() {
        waypoints.clear();
        updatePainters();
        System.out.println("Waypoint rimossi dalla mappa");
    }


     //Rimuove solo i waypoint dei bus real-time, mantenendo quelli delle fermate

    public void clearRealtimeBusWaypoints() {
        waypoints.removeIf(wp -> wp.getType() == BusWaypoint.WaypointType.REALTIME_BUS);
        updatePainters();
    }

    private void updatePainters() {
        if (!waypoints.isEmpty()) {
            WaypointPainter waypointPainter = new WaypointPainter(waypoints);

            List<Painter<JXMapViewer>> allPainters = new ArrayList<>();

            if (routeDrawer != null) {
                List<GeoPosition> routePoints = routeDrawer.getGeoPositions();
                if (routePoints != null && !routePoints.isEmpty()) {
                    allPainters.add(new RouteDrawer.RoutePainter(routePoints, Color.RED));
                }
            }

            allPainters.add(waypointPainter);

            CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter<>(allPainters);
            mapViewer.setOverlayPainter(compoundPainter);


            // (serve per i test)

            lastOverlayPainterForTest = compoundPainter; // serve per i test

            System.out.println(" Linea + waypoint disegnati correttamente");
        } else {
            mapViewer.setOverlayPainter(null);


         // (serve per i test)

            lastOverlayPainterForTest = null; // serve per i test
        }
    }

    // METODI DI SUPPORTO (serve per i test)

    int getWaypointsCountForTest() {          // serve per i test
        return waypoints.size();
    }

    Set<BusWaypoint> getWaypointsSnapshotForTest() { // serve per i test
        return Collections.unmodifiableSet(new HashSet<>(waypoints));
    }

    Painter<JXMapViewer> getLastOverlayPainterForTest() { // serve per i test
        return lastOverlayPainterForTest;
    }

    // CLASSE INTERNA DI DISEGNO (INTOCCATA)
    private static class WaypointPainter implements Painter<JXMapViewer> {
        private final Set<BusWaypoint> waypoints;

        public WaypointPainter(Set<BusWaypoint> waypoints) {
            this.waypoints = waypoints;
        }

        @Override
        public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
            g = (Graphics2D) g.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Rectangle rect = map.getViewportBounds();
            g.translate(-rect.x, -rect.y);

            for (BusWaypoint waypoint : waypoints) {
                GeoPosition gp = waypoint.getPosition();
                Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());

                if (waypoint.getType() == BusWaypoint.WaypointType.STOP) {
                    drawStopWaypoint(g, pt);
                } else if (waypoint.getType() == BusWaypoint.WaypointType.REALTIME_BUS) {
                    drawBusWaypoint(g, pt, waypoint.getBearing());
                }
            }

            g.dispose();
        }

        private void drawStopWaypoint(Graphics2D g, Point2D pt) {
            g.setColor(new Color(255, 100, 100, 200));
            g.fillOval((int) pt.getX() - 8, (int) pt.getY() - 8, 16, 16);

            g.setColor(new Color(200, 0, 0, 255));
            g.setStroke(new BasicStroke(2));
            g.drawOval((int) pt.getX() - 8, (int) pt.getY() - 8, 16, 16);

            g.setColor(Color.WHITE);
            g.fillOval((int) pt.getX() - 3, (int) pt.getY() - 3, 6, 6);
        }

        private void drawBusWaypoint(Graphics2D g, Point2D pt, float bearing) {
            g.setColor(new Color(76, 175, 80, 230));
            g.fillOval((int) pt.getX() - 10, (int) pt.getY() - 10, 20, 20);

            g.setColor(new Color(27, 94, 32, 255));
            g.setStroke(new BasicStroke(2));
            g.drawOval((int) pt.getX() - 10, (int) pt.getY() - 10, 20, 20);

            g.setColor(Color.WHITE);
            g.fillOval((int) pt.getX() - 4, (int) pt.getY() - 4, 8, 8);
        }
    }
}
