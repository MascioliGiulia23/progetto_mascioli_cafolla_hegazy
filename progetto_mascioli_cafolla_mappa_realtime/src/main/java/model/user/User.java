package model.user;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


 //Classe che rappresenta un utente registrato

public class User implements Serializable {
    private static final long serialVersionUID = 1L; // ← CAMBIATO A 2L

    private String username;
    private String password;
    private List<Favorite> preferiti;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.preferiti = new ArrayList<>();  // Inizializza come vuoto
    }


    //Verifica se una password è corretta

    public boolean verificaPassword(String password) {
        return this.password.equals(password);
    }

    /**
     * Aggiunge un preferito
     */
    public boolean aggiungiPreferito(Favorite favorite) {
        if (!preferiti.contains(favorite)) {
            preferiti.add(favorite);
            System.out.println("✓ Aggiunto preferito: " + favorite.getNome());
            return true;
        }
        System.out.println("⚠ Preferito già esistente: " + favorite.getNome());
        return false;
    }


     //Rimuove un preferito

    public boolean rimuoviPreferito(Favorite favorite) {
        if (preferiti.remove(favorite)) {
            System.out.println("✓ Rimosso preferito: " + favorite.getNome());
            return true;
        }
        return false;
    }

    /**
     * Verifica se un preferito è già salvato
     */
    public boolean haPreferito(Favorite favorite) {
        return preferiti.contains(favorite);
    }

    /**
     * Ritorna la lista dei preferiti
     */
    public List<Favorite> getPreferiti() {
        return new ArrayList<>(preferiti);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", preferiti=" + preferiti.size() +
                '}';
    }
}
