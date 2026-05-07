package models;

import java.util.List;

public class Videojuego {

    private int idVideojuego;
    private String titulo;
    private String desarrollador;
    private int añoLanzamiento;
    private List<Plataforma> plataformas;
    private List<Genero> generos;

    // NUEVOS CAMPOS (vienen de la relación usuario_videojuego)
    private int notaPersonal;
    private String estado;

    public Videojuego() {
    }

    public Videojuego(String titulo, String desarrollador, int añoLanzamiento, List<Plataforma> plataforma, List<Genero> genero) {
        this.titulo = titulo;
        this.desarrollador = desarrollador;
        this.añoLanzamiento = añoLanzamiento;
        this.plataformas = plataforma;
        this.generos = genero;
    }

    public Videojuego(int idVideojuego, String titulo, String desarrollador, int añoLanzamiento, List<Plataforma> plataforma, List<Genero> genero) {
        this.idVideojuego = idVideojuego;
        this.titulo = titulo;
        this.desarrollador = desarrollador;
        this.añoLanzamiento = añoLanzamiento;
        this.plataformas = plataforma;
        this.generos = genero;
    }

    public int getIdVideojuego() { return idVideojuego; }
    public String getTitulo() { return titulo; }
    public String getDesarrollador() { return desarrollador; }
    public int getAñoLanzamiento() { return añoLanzamiento; }
    public List<Plataforma> getPlataformas() { return plataformas; }
    public List<Genero> getGeneros() { return generos; }

    // Nuevos Getters
    public int getNotaPersonal() { return notaPersonal; }
    public String getEstado() { return estado; }

    public void setIdVideojuego(int idVideojuego) { this.idVideojuego = idVideojuego; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setDesarrollador(String desarrollador) { this.desarrollador = desarrollador; }
    public void setAñoLanzamiento(int añoLanzamiento) { this.añoLanzamiento = añoLanzamiento; }
    public void setPlataformas(List<Plataforma> plataforma) { this.plataformas = plataforma; }
    public void setGeneros(List<Genero> genero) { this.generos = genero; }

    // Nuevos Setters
    public void setNotaPersonal(int notaPersonal) { this.notaPersonal = notaPersonal; }
    public void setEstado(String estado) { this.estado = estado; }

    @Override
    public String toString() {
        return titulo + ", " + desarrollador + ", " + añoLanzamiento +
                ". Plataformas: " + plataformas + ". Géneros:" + generos +
                ". Nota: " + notaPersonal + " (" + estado + ")";
    }
}