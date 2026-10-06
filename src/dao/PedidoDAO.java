package dao;

import modelo.EstadoPedido;
import modelo.Pedido;
import modelo.TipoPedido;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acceso a datos de la tabla pedidos (CRUD con PreparedStatement). */
public class PedidoDAO {

    /** Inserta un pedido y deja el id generado en el objeto. */
    public void create(Pedido p) throws SQLException {
        String sql = "INSERT INTO pedidos (direccion, tipo, estado) VALUES (?, ?, ?)";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getDireccion());
            ps.setString(2, p.getTipo().name());
            ps.setString(3, p.getEstado().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.setId(keys.getInt(1));
            }
        }
    }

    /** Devuelve todos los pedidos. */
    public List<Pedido> readAll() throws SQLException {
        return readFiltrado(null, null);
    }

    /**
     * Devuelve pedidos con filtros opcionales (null = sin filtro).
     * El SQL solo se arma con fragmentos fijos; los valores van como parámetros.
     */
    public List<Pedido> readFiltrado(EstadoPedido estado, TipoPedido tipo) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT id, direccion, tipo, estado FROM pedidos WHERE 1=1");
        if (estado != null) sql.append(" AND estado = ?");
        if (tipo != null) sql.append(" AND tipo = ?");
        sql.append(" ORDER BY id");

        List<Pedido> lista = new ArrayList<>();
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            int i = 1;
            if (estado != null) ps.setString(i++, estado.name());
            if (tipo != null) ps.setString(i, tipo.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Pedido(
                            rs.getInt("id"),
                            rs.getString("direccion"),
                            TipoPedido.valueOf(rs.getString("tipo")),
                            EstadoPedido.valueOf(rs.getString("estado"))));
                }
            }
        }
        return lista;
    }

    /** Actualiza dirección, tipo y estado; retorna filas afectadas. */
    public int update(Pedido p) throws SQLException {
        String sql = "UPDATE pedidos SET direccion = ?, tipo = ?, estado = ? WHERE id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, p.getDireccion());
            ps.setString(2, p.getTipo().name());
            ps.setString(3, p.getEstado().name());
            ps.setInt(4, p.getId());
            return ps.executeUpdate();
        }
    }

    /** Elimina un pedido por id; retorna filas afectadas. */
    public int delete(int id) throws SQLException {
        String sql = "DELETE FROM pedidos WHERE id = ?";
        try (Connection con = ConexionDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate();
        }
    }
}
