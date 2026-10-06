import dao.ConexionDB;
import vista.Mensajes;
import vista.VentanaPrincipal;

import javax.swing.SwingUtilities;
import java.sql.Connection;
import java.sql.SQLException;

/** Punto de entrada de la aplicación SpeedFast. */
public class Main {
    public static void main(String[] args) {
        // Paso 1: comprobar que la conexión a la BD funciona
        try (Connection con = ConexionDB.getConnection()) {
            System.out.println("Conexión a speedfast_db exitosa.");
        } catch (SQLException ex) {
            System.err.println("No se pudo conectar: " + ex.getMessage());
            Mensajes.errorSQL(null, "conectar con la base de datos", ex);
        }
        SwingUtilities.invokeLater(() -> new VentanaPrincipal().setVisible(true));
    }
}
