package models;

public record Genero(int id, String nombre) {

    @Override
    public String toString() {
        return nombre;
    }

    
}
