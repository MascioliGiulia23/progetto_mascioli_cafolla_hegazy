package model.user;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Classe che gestisce il salvataggio e caricamento degli utenti da file
 */
public class UserManager {

    // Percorso dove salvare gli utenti
    private static final String USERS_FILE = System.getProperty("user.home")
            + File.separator + "RomaBusTracker"
            + File.separator + "users.dat";

    private static Map<String, User> utenti = new HashMap<>();
    private static boolean caricati = false;

    /**
     * Carica tutti gli utenti dal file
     */
    public static void caricaUtenti() {
        if (caricati) {
            System.out.println("✓ Utenti già caricati (" + utenti.size() + " account)");
            return;
        }

        File file = new File(USERS_FILE);

        if (!file.exists()) {
            System.out.println("✓ File utenti non trovato, creazione nuova.");
            caricati = true;
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            @SuppressWarnings("unchecked")
            Map<String, User> utentiCaricati = (Map<String, User>) ois.readObject();
            utenti = utentiCaricati;
            System.out.println("✓ Caricati " + utenti.size() + " utenti da file:");
            for (String username : utenti.keySet()) {
                System.out.println("   - " + username);
            }
            caricati = true;
        } catch (FileNotFoundException e) {
            System.err.println(" File non trovato: " + USERS_FILE);
            caricati = true;
        } catch (EOFException e) {
            System.err.println(" File vuoto o corrotto");
            utenti = new HashMap<>();
            caricati = true;
        } catch (ClassNotFoundException e) {
            System.err.println(" Errore di versione classe: " + e.getMessage());
            caricati = true;
        } catch (Exception e) {
            System.err.println(" Errore nel caricamento degli utenti: " + e.getMessage());
            e.printStackTrace();
            caricati = true;
        }
    }

    /**
     * Salva tutti gli utenti su file
     */
    public static void salvaUtenti() {
        try {
            // Crea la cartella se non esiste
            Files.createDirectories(Paths.get(System.getProperty("user.home")
                    + File.separator + "RomaBusTracker"));

            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
                oos.writeObject(utenti);
                System.out.println("✓ Utenti salvati con successo (" + utenti.size() + " account)");
            }
        } catch (Exception e) {
            System.err.println(" Errore nel salvataggio degli utenti: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Registra un nuovo utente
     */
    public static boolean registraUtente(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            System.out.println(" Username vuoto");
            return false;
        }

        if (password == null || password.isEmpty()) {
            System.out.println(" Password vuota");
            return false;
        }

        if (utenti.containsKey(username)) {
            System.out.println("Username già esistente: " + username);
            return false;
        }

        User nuovoUtente = new User(username, password);
        utenti.put(username, nuovoUtente);
        salvaUtenti();
        System.out.println("✓ Utente registrato: " + username);
        return true;
    }

    /**
     * Effettua il login di un utente
     */
    public static boolean login(String username, String password) {
        System.out.println(" Tentativo login: " + username);
        System.out.println(" Utenti disponibili: " + utenti.size());

        if (!utenti.containsKey(username)) {
            System.out.println("Utente non trovato: " + username);
            return false;
        }

        User utente = utenti.get(username);
        boolean passwordCorretta = utente.verificaPassword(password);

        if (passwordCorretta) {
            System.out.println("✓ Login riuscito per: " + username);
        } else {
            System.out.println("Password errata per: " + username);
        }

        return passwordCorretta;
    }


     //Verifica se un username esiste

    public static boolean usernameEsiste(String username) {
        return utenti.containsKey(username);
    }

     //Ottiene un utente dal suo username

    public static User ottieniUtente(String username) {
        return utenti.get(username);
    }

     //Ritorna il numero di utenti registrati

    public static int getNumeroUtenti() {
        return utenti.size();
    }

    //  METODI PER PREFERITI


     // Aggiunge un preferito all'utente

    public static boolean aggiungiPreferito(String username, Favorite favorite) {
        if (!utenti.containsKey(username)) {
            System.out.println(" Utente non trovato: " + username);
            return false;
        }

        User utente = utenti.get(username);
        boolean aggiunto = utente.aggiungiPreferito(favorite);

        if (aggiunto) {
            salvaUtenti();
            System.out.println("✓ Preferito aggiunto a " + username + ": " + favorite.getNome());
        }

        return aggiunto;
    }

     //Rimuove un preferito dall'utente

    public static boolean rimuoviPreferito(String username, Favorite favorite) {
        if (!utenti.containsKey(username)) {
            System.out.println("Utente non trovato: " + username);
            return false;
        }

        User utente = utenti.get(username);
        boolean rimosso = utente.rimuoviPreferito(favorite);

        if (rimosso) {
            salvaUtenti();
            System.out.println("✓ Preferito rimosso da " + username + ": " + favorite.getNome());
        }

        return rimosso;
    }


     //Ottiene la lista dei preferiti di un utente

    public static List<Favorite> ottieniPreferiti(String username) {
        if (!utenti.containsKey(username)) {
            System.out.println(" Utente non trovato, nessun preferito: " + username);
            return new ArrayList<>();
        }

        User utente = utenti.get(username);
        List<Favorite> preferiti = utente.getPreferiti();
        System.out.println("✓ Recuperati " + preferiti.size() + " preferiti per " + username);
        return preferiti;
    }

    /**
     * Verifica se un preferito è già salvato per l'utente
     */
    public static boolean haPreferito(String username, Favorite favorite) {
        if (!utenti.containsKey(username)) {
            return false;
        }

        User utente = utenti.get(username);
        return utente.haPreferito(favorite);
    }

    /**
     * Ritorna il numero di preferiti di un utente
     */
    public static int getNumeroPreferiti(String username) {
        if (!utenti.containsKey(username)) {
            return 0;
        }

        return utenti.get(username).getPreferiti().size();
    }

    // ==================== METODI DEBUG ====================


     //Stampa il percorso del file e lo stato


}
