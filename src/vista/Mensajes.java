package vista;

import javax.swing.JOptionPane;
import java.awt.Component;
import java.sql.SQLException;

/** Utilidades para mostrar mensajes claros al usuario (JOptionPane). */
public final class Mensajes {
    private Mensajes() { }

    public static void info(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "SpeedFast", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void advertencia(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Validación", JOptionPane.WARNING_MESSAGE);
    }

    /** Muestra un error SQL traducido a un texto comprensible. */
    public static void errorSQL(Component padre, String accion, SQLException ex) {
        JOptionPane.showMessageDialog(padre,
                "No se pudo " + accion + ".\n" + traducir(ex),
                "Error de base de datos", JOptionPane.ERROR_MESSAGE);
    }

    /** Pide confirmación (Sí/No) antes de una acción destructiva. */
    public static boolean confirmar(Component padre, String pregunta) {
        return JOptionPane.showConfirmDialog(padre, pregunta, "Confirmar",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION;
    }

    /** Convierte códigos de error de MySQL en mensajes entendibles. */
    public static String traducir(SQLException ex) {
        switch (ex.getErrorCode()) {
            case 1451:
                return "El registro está asociado a otros datos (por ejemplo, entregas) "
                     + "y no puede eliminarse. Elimine primero los registros relacionados.";
            case 1452:
                return "El pedido o repartidor seleccionado ya no existe.";
            case 1045:
                return "Acceso denegado: revise el usuario y la clave en ConexionDB.";
            case 1049:
                return "La base de datos speedfast_db no existe. Ejecute el script sql/speedfast_db.sql.";
            default:
                break;
        }
        String estado = ex.getSQLState();
        if (estado != null && estado.startsWith("08")) {
            return "No hay conexión con MySQL. Verifique que el servidor esté activo.";
        }
        return "Detalle: " + ex.getMessage();
    }
}
