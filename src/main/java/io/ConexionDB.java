package io;

import exception.AppException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionDB {


    public static Connection getInstance() throws AppException, SQLException {
        PropertiesReader p = PropertiesReader.getInstance();

        return DriverManager.getConnection(p.get("url"), p.get("usuario.db"), p.get("password.db"));
    }
}
