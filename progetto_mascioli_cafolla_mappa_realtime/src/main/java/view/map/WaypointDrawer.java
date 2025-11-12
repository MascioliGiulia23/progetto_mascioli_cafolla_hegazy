package view.map;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WaypointDrawer {

    private final JXMapViewer mapViewer;
    private final Set<BusWaypoint> waypoints = new HashSet<>();
    private final RouteDrawer routeDrawer;

    public WaypointDrawer(JXMapViewer mapViewer, RouteDrawer routeDrawer) {
        this.mapViewer = mapViewer;
        this.routeDrawer = routeDrawer;
    }

    /**
     * Aggiunge un singolo waypoint sulla mappa
     */
    public void addWaypoint(BusWaypoint waypoint) {
        if (waypoint != null) {
            waypoints.add(waypoint);
            updatePainters();
        }
    }

    /**
     * Aggiunge più waypoint sulla mappa
     */
    public void addWaypoints(Collection<BusWaypoint> newWaypoints) {
        if (newWaypoints != null) {
            waypoints.addAll(newWaypoints);
            updatePainters();
        }
    }

    /**
     * Cancella tutti i waypoint
     */
    public void clearWaypoints() {
        waypoints.clear();
        updatePainters();
        System.out.println("✓ Waypoint rimossi dalla mappa");
    }

    /**
     * Aggiorna i painter sulla mappa
     */
    private void updatePainters() {
        if (!waypoints.isEmpty()) {
            WaypointPainter waypointPainter = new WaypointPainter(waypoints);

            List<Painter<JXMapViewer>> allPainters = new ArrayList<>();

            // Aggiungi prima la linea (dal routeDrawer)
            if (routeDrawer != null) {
                List<GeoPosition> routePoints = routeDrawer.getGeoPositions();
                if (routePoints != null && !routePoints.isEmpty()) {
                    allPainters.add(new RouteDrawer.RoutePainter(routePoints, Color.RED));
                }
            }

            // Poi i waypoint
            allPainters.add(waypointPainter);

            // Combina entrambi
            CompoundPainter<JXMapViewer> compoundPainter = new CompoundPainter<>(allPainters);
            mapViewer.setOverlayPainter(compoundPainter);

            System.out.println("✓ Linea + waypoint disegnati correttamente.");
        } else {
            mapViewer.setOverlayPainter(null);
        }
    }

    /**
     * Classe interna che disegna i waypoint sulla mappa
     */
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

                // Disegna cerchio rosso per il waypoint
                g.setColor(new Color(255, 100, 100, 200));
                g.fillOval((int) pt.getX() - 8, (int) pt.getY() - 8, 16, 16);

                // Bordo scuro
                g.setColor(new Color(200, 0, 0, 255));
                g.setStroke(new BasicStroke(2));
                g.drawOval((int) pt.getX() - 8, (int) pt.getY() - 8, 16, 16);

                // Pallino bianco al centro
                g.setColor(Color.WHITE);
                g.fillOval((int) pt.getX() - 3, (int) pt.getY() - 3, 6, 6);
            }

            g.dispose();
        }
    }
}
