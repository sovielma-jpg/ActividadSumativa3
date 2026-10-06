package dao;

import modelo.Entrega;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos de la tabla entregas (CRUD con PreparedStatement). */
public class EntregaDAO {

    // Consulta base con JOIN para mostrar datos legibles en la tabla
    private static final String SELECT_BASE =
            "SELECT e.id, e.id_pedido, e.id_repartidor, e.fecha, e.hora, "
          + "p.direccion, r.nombre "
          + "FROM entregas e "
          + "JOIN pedidos p ON p.id = e.id_pedido "
          + "JOIN repartidores r ON r.id = e.id_repartidor ";

    /** Inserta una entrega y deja el id generado en el objeto. */
    public void create(Entrega e) throws SQLException {
        String sql = "INSERT INTO entregas (id_pedido, id_repartidor, fecha, hora) VALUES (?, ?, ?, ?)";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.getIdPedido());
            ps.setInt(2, e.getIdRepartidor());
            ps.setDate(3, Date.valueOf(e.getFecha()));
            ps.setTime(4, Time.valueOf(e.getHora()));
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) e.setId(keys.getInt(1));
            }
        }
    }

    /** Devuelve todas las entregas. */
    public List<Entrega> readAll() throws SQLException {
        return leer(SELECT_BASE + "ORDER BY e.id", null);
    }

    /** Devuelve las entregas de un pedido. */
    public List<Entrega> readByPedido(int idPedido) throws SQLException {
        return leer(SELECT_BASE + "WHERE e.id_pedido = ? ORDER BY e.id", idPedido);
    }

    /** Devuelve las entregas de un repartidor. */
    public List<Entrega> readByRepartidor(int idRepartidor) throws SQLException {
        return leer(SELECT_BASE + "WHERE e.id_repartidor = ? ORDER BY e.id", idRepartidor);
    }

    /** Actualiza pedido, repartidor, fecha y hora; retorna filas afectadas. */
    public int update(Entrega e) throws SQLException {
        String sql = "UPDATE entregas SET id_pedido = ?, id_repartidor = ?, fecha = ?, hora = ? WHERE id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, e.getIdPedido());
            ps.setInt(2, e.getIdRepartidor());
            ps.setDate(3, Date.valueOf(e.getFecha()));
            ps.setTime(4, Time.valueOf(e.getHora()));
            ps.setInt(5, e.getId());
            return ps.executeUpdate();
        }
    }

    /** Elimina una entrega por id; retorna filas afectadas. */
    public int delete(int id) throws SQLException {
        String sql = "DELETE FROM entregas WHERE id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }

    /** Ejecuta una consulta con un parámetro entero opcional y mapea el ResultSet. */
    private List<Entrega> leer(String sql, Integer parametro) throws SQLException {
        List<Entrega> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (parametro != null) ps.setInt(1, parametro);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Entrega e = new Entrega(
                            rs.getInt("id"),
                            rs.getInt("id_pedido"),
                            rs.getInt("id_repartidor"),
                            rs.getDate("fecha").toLocalDate(),
                            rs.getTime("hora").toLocalTime());
                    e.setDireccionPedido(rs.getString("direccion"));
                    e.setNombreRepartidor(rs.getString("nombre"));
                    lista.add(e);
                }
            }
        }
        return lista;
    }
}
