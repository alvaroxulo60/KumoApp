package io;

import models.Usuario;

public class Sesion {
    private static Usuario usuarioLogueado;

    public static void setUsuario(Usuario usuario) {
        usuarioLogueado = usuario;
    }

    public static Usuario getUsuario() {
        return usuarioLogueado;
    }

    public static void logout() {
        usuarioLogueado = null;
    }
}