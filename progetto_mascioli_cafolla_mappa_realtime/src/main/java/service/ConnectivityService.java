package service;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

//(serve per i test)
import java.util.Objects;                  // serve per i test
import java.util.function.BooleanSupplier; // serve per i test
import java.util.function.Consumer;        // serve per i test


/**
 * Classe di servizio che gestisce lo stato della connessione Internet.
 * Permette di sapere in ogni momento se l'utente è online o offline.
 * Il controllo della connessione sarà eseguito periodicamente (ogni 30 secondi)
 * dal MapInitializer o dal MapService.
 */
public class ConnectivityService {

    // Stato corrente della connessione (true = online, false = offline)
    private static boolean online = false;
    // >>> NUOVE DIPENDENZE INIETTABILI (serve per i test)
    // Di default puntano al comportamento reale, quindi NON cambia nulla nell'app.
    private static BooleanSupplier probe = ConnectivityService::testConnection; // serve per i test
    private static Consumer<Boolean> notifier = ConnectivityService::showConnectionToast; // serve per i test

    /**
     * Restituisce l'ultimo stato noto della connessione.
     * @return true se online, false se offline
     */
    public static boolean isOnline() {
        return online;
    }

    /**
     * Effettua un controllo della connessione e aggiorna lo stato interno.
     * Se lo stato cambia (da online a offline o viceversa), stampa un messaggio nel log.
     */
    public static void checkConnection() {
        boolean previousState = online;
        //online = testConnection();


        // >>> MODIFICA MINIMA: usa probe (che di default chiama testConnection)
        // Comportamento identico a prima.
        online = probe.getAsBoolean(); // serve per i test

        if (previousState != online) {
            System.out.println("[ConnectivityService] Stato cambiato: " +
                    (online ? "ONLINE" : "OFFLINE"));
           // showConnectionToast(online);
            // >>> MODIFICA MINIMA: usa notifier (che di default chiama showConnectionToast)
            notifier.accept(online); // serve per i test
        }
    }
    // ===== TEST HOOKS (solo per test) =====

    static void setProbeForTest(BooleanSupplier newProbe) { // serve per i test
        probe = Objects.requireNonNull(newProbe);          // serve per i test
    }

    static void setNotifierForTest(Consumer<Boolean> newNotifier) { // serve per i test
        notifier = Objects.requireNonNull(newNotifier);              // serve per i test
    }

    static void resetForTest() { // serve per i test
        online = false;                               // serve per i test
        probe = ConnectivityService::testConnection;  // serve per i test
        notifier = ConnectivityService::showConnectionToast; // serve per i test
    }

    /**
     * Tenta di stabilire una connessione a Internet.
     * In questo esempio prova a collegarsi a "www.google.com" sulla porta 80.
     * @return true se la connessione riesce, false altrimenti
     */
    private static boolean testConnection() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("www.google.com", 80), 2000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }


     //Mostra un messaggio temporaneo che scompare dopo 3 secondi.
// Mostra un messaggio temporaneo che scompare dopo 3 secondi.
     private static void showConnectionToast(boolean isOnline) {
         JWindow toast = new JWindow();
         toast.setBackground(new Color(0, 0, 0, 0));
         toast.setOpacity(0.0f);

         JPanel panel = new JPanel() {
             @Override
             protected void paintComponent(Graphics g) {
                 Graphics2D g2 = (Graphics2D) g.create();
                 g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                 // Ombra
                 g2.setColor(new Color(0, 0, 0, 30));
                 g2.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 15, 15);

                 // Sfondo principale
                 g2.setColor(getBackground());
                 g2.fillRoundRect(0, 0, getWidth() - 4, getHeight() - 4, 15, 15);
                 g2.dispose();
             }
         };

         panel.setOpaque(false);
         panel.setLayout(new BorderLayout(10, 0));
         panel.setBackground(isOnline ? new Color(76, 175, 80) : new Color(244, 67, 54));
         panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

         // Icona
         JLabel icon = new JLabel();
         icon.setFont(new Font("Segoe UI", Font.BOLD, 18));
         icon.setForeground(Color.WHITE);


         // Testo
         JLabel label = new JLabel(isOnline ? "Connessione ripristinata" : "Connessione persa");
         label.setForeground(Color.WHITE);
         label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
         panel.add(label, BorderLayout.CENTER);

         toast.add(panel);
         toast.pack();

         // Posizione in basso a destra
         Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
         toast.setLocation(screen.width - toast.getWidth() - 30,
                 screen.height - toast.getHeight() - 80);

         toast.setVisible(true);

         // --- FADE IN SICURO ---
         Timer fadeIn = new Timer(30, null);
         fadeIn.addActionListener(e -> {
             float opacity = toast.getOpacity();
             float newOpacity = opacity + 0.05f;

             // Evita di superare 1.0f
             if (newOpacity > 1.0f) newOpacity = 1.0f;

             toast.setOpacity(newOpacity);

             if (newOpacity >= 0.95f) {
                 ((Timer) e.getSource()).stop();
             }
         });
         fadeIn.start();

         // --- FADE OUT SICURO ---
         Timer delayTimer = new Timer(2500, e -> {
             Timer fadeOut = new Timer(30, null);
             fadeOut.addActionListener(ev -> {
                 float opacity = toast.getOpacity();
                 float newOpacity = opacity - 0.05f;

                 // FIX: Se scende sotto 0, forzalo a 0
                 if (newOpacity < 0.0f) {
                     newOpacity = 0.0f;
                 }

                 toast.setOpacity(newOpacity);

                 if (newOpacity <= 0.0f) {
                     ((Timer) ev.getSource()).stop();
                     toast.dispose();
                 }
             });
             fadeOut.start();
         });
         delayTimer.setRepeats(false);
         delayTimer.start();
     }

}

