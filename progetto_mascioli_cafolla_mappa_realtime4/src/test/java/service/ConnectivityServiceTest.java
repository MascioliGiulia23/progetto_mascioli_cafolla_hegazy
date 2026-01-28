package service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Test della classe ConnectivityService")
class ConnectivityServiceTest {

    @AfterEach
    void tearDown() {
        // Ripristina sempre lo stato reale dopo ogni test
        ConnectivityService.resetForTest();
    }

    @Test
    @DisplayName("Stato iniziale offline")
    void testDefaultOffline() {
        ConnectivityService.resetForTest();
        assertFalse(ConnectivityService.isOnline());
    }

    @Test
    @DisplayName("Da OFFLINE a ONLINE: aggiorna stato e notifica una volta")
    void testOfflineToOnlineNotifies() {
        ConnectivityService.resetForTest();

        List<Boolean> notifications = new ArrayList<>();
        ConnectivityService.setNotifierForTest(notifications::add);

        // Simuliamo connessione OK in modo deterministico
        ConnectivityService.setProbeForTest(() -> true);

        ConnectivityService.checkConnection();

        assertTrue(ConnectivityService.isOnline());
        assertEquals(1, notifications.size());
        assertTrue(notifications.get(0));
    }

    @Test
    @DisplayName("Da ONLINE a OFFLINE: aggiorna stato e notifica una volta")
    void testOnlineToOfflineNotifies() {
        ConnectivityService.resetForTest();

        // Portiamo lo stato a ONLINE senza notifiche (notifier vuoto)
        ConnectivityService.setNotifierForTest(x -> {});
        ConnectivityService.setProbeForTest(() -> true);
        ConnectivityService.checkConnection();
        assertTrue(ConnectivityService.isOnline());

        // Ora simuliamo OFFLINE e catturiamo la notifica
        List<Boolean> notifications = new ArrayList<>();
        ConnectivityService.setNotifierForTest(notifications::add);
        ConnectivityService.setProbeForTest(() -> false);

        ConnectivityService.checkConnection();

        assertFalse(ConnectivityService.isOnline());
        assertEquals(1, notifications.size());
        assertFalse(notifications.get(0));
    }

    @Test
    @DisplayName("Stato invariato: nessuna notifica")
    void testNoStateChangeNoNotification() {
        ConnectivityService.resetForTest();

        AtomicInteger calls = new AtomicInteger(0);
        ConnectivityService.setNotifierForTest(x -> calls.incrementAndGet());

        // Probe sempre OFFLINE: stato non cambia mai
        ConnectivityService.setProbeForTest(() -> false);

        ConnectivityService.checkConnection(); // OFFLINE -> OFFLINE (no notify)
        ConnectivityService.checkConnection(); // OFFLINE -> OFFLINE (no notify)

        assertFalse(ConnectivityService.isOnline());
        assertEquals(0, calls.get());
    }
}
