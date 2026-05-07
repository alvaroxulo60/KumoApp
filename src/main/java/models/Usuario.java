package models;

public class Usuario {

    private String nombre;
    private String email;
    private String password;
    private String numeroTelefono;
    private int idUsuario;

    public Usuario(){
    }

    public String getEmail() {
        return email;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getNumeroTelefono() {
        return numeroTelefono;
    }

    public String getPassword() {
        return password;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setNumeroTelefono(String numeroTelefono) {
        this.numeroTelefono = numeroTelefono;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
