package model.user;

import java.io.Serializable;
import java.util.Objects;

public class Favorite implements Serializable {
    private String nome;
    private String tipo; // "FERMATA" o "LINEA"

    public Favorite(String nome, String tipo) {
        this.nome = nome;
        this.tipo = tipo;
    }

    public String getNome() {
        return nome;
    }

    public String getTipo() {
        return tipo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Favorite favorite = (Favorite) o;
        return Objects.equals(nome, favorite.nome) && Objects.equals(tipo, favorite.tipo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nome, tipo);
    }

    @Override
    public String toString() {
        return tipo + ": " + nome;
    }
}
