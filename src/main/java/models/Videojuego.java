package models;

import java.util.List;

public class Videojuego {

    private int idVideojuego;
    private String titulo;
    private String desarrollador;
    private int añoLanzamiento;
    private List<Plataforma> plataformas;
    private List<Genero> generos;

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

    public int getIdVideojuego() {
        return idVideojuego;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDesarrollador() {
        return desarrollador;
    }

    public int getAñoLanzamiento() {
        return añoLanzamiento;
    }

    public List<Plataforma> getPlataformas() {
        return plataformas;
    }

    public List<Genero> getGeneros() {
        return generos;
    }

    public void setIdVideojuego(int idVideojuego) {
        this.idVideojuego = idVideojuego;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public void setDesarrollador(String desarrollador) {
        this.desarrollador = desarrollador;
    }

    public void setAñoLanzamiento(int añoLanzamiento) {
        this.añoLanzamiento = añoLanzamiento;
    }

    public void setPlataformas(List<Plataforma> plataforma) {
        this.plataformas = plataforma;
    }

    public void setGeneros(List<Genero> genero) {
        this.generos = genero;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
       return sb.append(titulo).append(", ").append(desarrollador).append(", ").append(añoLanzamiento).append(". Plataformas: ").append(plataformas).append(". Géneros:").append(generos).toString();
    }
}
