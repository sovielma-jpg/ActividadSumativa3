package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Clase de conexión a MySQL.
 * Ajusta URL, USUARIO y CLAVE según tu entorno (o usa las variables de
 * entorno SPEEDFAST_URL, SPEEDFAST_USER y SPEEDFAST_PASS).
 */
public class ConexionDB {
    private static final String URL = "jdbc:mysql://localhost:3306/speedfast_db";
    private static final String USUARIO = "root";
    private static final String CLAVE = "1234";

    private ConexionDB() { }

    /** Entrega una conexión nueva; quien la use debe cerrarla (try-with-resources). */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }
}
