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

import javax.swing.event.MouseInputListener;
import java.io.File;

// >>> NUOVE IMPORT (serve per i test)
import java.util.Objects;              // serve per i test
import java.util.function.Supplier;    // serve per i test

/**
 * Classe di supporto per inizializzare e configurare JXMapViewer.
 * Mantiene la logica di setup fuori dalla JFrame principale.
 */
public class MapInitializer {

    // =========================
    // DIPENDENZE INIETTABILI (serve per i test)
    // =========================

    private static Supplier<java.util.Timer> timerFactory =
            () -> new java.util.Timer(true); // serve per i test

    private static Runnable connectivityCheck =
            service.ConnectivityService::checkConnection; // serve per i test

    private static boolean enableConnectivityTimer = true; // serve per i test

    // ====== metodi SOLO per i test ======

    static void setTimerFactoryForTest(Supplier<java.util.Timer> factory) { // serve per i test
        timerFactory = Objects.requireNonNull(factory);
    }

    static void setConnectivityCheckForTest(Runnable runnable) { // serve per i test
        connectivityCheck = Objects.requireNonNull(runnable);
    }

    static void setEnableConnectivityTimerForTest(boolean enabled) { // serve per i test
        enableConnectivityTimer = enabled;
    }

    static void resetForTest() { // serve per i test
        timerFactory = () -> new java.util.Timer(true);
        connectivityCheck = service.ConnectivityService::checkConnection;
        enableConnectivityTimer = true;
    }

    // =========================

    public static JXMapViewer creaMappaBase() {
        // usa HTTPS invece di HTTP per le tile di OpenStreetMap
        TileFactoryInfo info = new OSMTileFactoryInfo(
                "OpenStreetMap",
                "https://tile.openstreetmap.org"
        );
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

        // --- AVVIO CONTROLLO PERIODICO CONNETTIVITÀ ---
        if (enableConnectivityTimer) { // serve per i test
            java.util.Timer timer = timerFactory.get(); // serve per i test
            timer.scheduleAtFixedRate(new java.util.TimerTask() {
                @Override
                public void run() {
                    connectivityCheck.run(); // serve per i test
                }
            }, 0, 30000);
        }

        return mapViewer;
    }
}
