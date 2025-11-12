package service;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;



/**
 * Classe di servizio che gestisce lo stato della connessione Internet.
 * Permette di sapere in ogni momento se l'utente è online o offline.
 * Il controllo della connessione sarà eseguito periodicamente (ogni 30 secondi)
 * dal MapInitializer o dal MapService.
 */
public class ConnectivityService {

    // Stato corrente della connessione (true = online, false = offline)
    private static boolean online = false;

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
        online = testConnection();

        if (previousState != online) {
            System.out.println("[ConnectivityService] Stato cambiato: " +
                    (online ? "ONLINE" : "OFFLINE"));
            showConnectionToast(online);
        }
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

    /**
     * Mostra un messaggio temporaneo che scompare dopo 3 secondi.
     */
    private static void showConnectionToast(boolean isOnline) {
        JWindow toast = new JWindow();
        toast.setBackground(new Color(0, 0, 0, 0));

        JPanel panel = new JPanel();
        panel.setBackground(isOnline ? new Color(46, 204, 113) : new Color(231, 76, 60));
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JLabel label = new JLabel(isOnline ? "Connessione ripristinata" : " Connessione persa");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        panel.add(label);

        toast.add(panel);
        toast.pack();
        toast.setSize(250, 50);

        // Posiziona in basso a sinistra
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        toast.setLocation(50, screen.height - toast.getHeight() - 100);


        toast.setVisible(true);

        // Chiudi dopo 3 secondi
        Timer timer = new Timer(3000, e -> toast.dispose());
        timer.setRepeats(false);
        timer.start();
    }
}
