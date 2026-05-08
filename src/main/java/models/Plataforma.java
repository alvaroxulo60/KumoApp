package models;

public record Plataforma(int id, String nombre) {

    @Override
    public String toString() {
        return nombre;
    }
}
