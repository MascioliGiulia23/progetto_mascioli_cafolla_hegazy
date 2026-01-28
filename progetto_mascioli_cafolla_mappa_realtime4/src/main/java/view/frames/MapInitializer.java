//Questa classe si occupa solo di costruire tutti i componenti grafici
// della mappa e di restituirli già pronti alla Mappa.
// PRIMA: 60 righe di setup
// DOPO:
package view.frames;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.cache.FileBasedLocalCache;
import org.jxmapviewer.input.CenterMapListener;
import org.jxmapviewer.input.PanKeyListener;
import org.jxmapviewer.input.PanMouseInputListener;
import org.jxmapviewer.input.ZoomMouseWheelListenerCursor;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.io.File;

/**
 * Classe di supporto per inizializzare e configurare JXMapViewer.
 * Mantiene la logica di setup fuori dalla JFrame principale.
 */
public class MapInitializer {

    public static JXMapViewer creaMappaBase() {
        // usa HTTPS invece di HTTP per le tile di OpenStreetMap
        TileFactoryInfo info = new OSMTileFactoryInfo("OpenStreetMap", "https://tile.openstreetmap.org");
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);

        // usa una cartella temporanea per la cache
        File cacheDir = new File(System.getProperty("java.io.tmpdir"), "jxmapviewer2");
        tileFactory.setLocalCache(new FileBasedLocalCache(cacheDir, false));

        JXMapViewer mapViewer = new JXMapViewer();
        mapViewer.setTileFactory(tileFactory);

        GeoPosition roma = new GeoPosition(41.8902, 12.4922);
        mapViewer.setZoom(7);
        mapViewer.setAddressLocation(roma);

        mapViewer.addMouseWheelListener(new ZoomMouseWheelListenerCursor(mapViewer));
        MouseInputListener panListener = new PanMouseInputListener(mapViewer);
        mapViewer.addMouseListener(panListener);
        mapViewer.addMouseMotionListener(panListener);
        mapViewer.addKeyListener(new PanKeyListener(mapViewer));
        mapViewer.addMouseListener(new CenterMapListener(mapViewer));

//
//        // --- AVVIO CONTROLLO PERIODICO CONNETTIVITÀ --- //
        java.util.Timer timer = new java.util.Timer(true);
        timer.scheduleAtFixedRate(new java.util.TimerTask() {
            @Override
            public void run() {
                service.ConnectivityService.checkConnection();
            }
        }, 0, 30000); // ogni 30 secondi


        return mapViewer;

    }
}
