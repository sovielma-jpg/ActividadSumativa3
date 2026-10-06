package vista;

import dao.PedidoDAO;
import modelo.EstadoPedido;
import modelo.Pedido;
import modelo.TipoPedido;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/** Pestaña de gestión de pedidos (CRUD + filtros por estado y tipo). */
public class PanelPedidos extends JPanel implements Refrescable {
    private static final String TODOS = "Todos";

    private final PedidoDAO dao = new PedidoDAO();
    private final JTextField txtDireccion = new JTextField(25);
    private final JComboBox<TipoPedido> cmbTipo = new JComboBox<>(TipoPedido.values());
    private final JComboBox<EstadoPedido> cmbEstado = new JComboBox<>(EstadoPedido.values());
    private final JComboBox<Object> cmbFiltroEstado = new JComboBox<>();
    private final JComboBox<Object> cmbFiltroTipo = new JComboBox<>();
    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Dirección", "Tipo", "Estado"}, 0) {
        @Override public boolean isCellEditable(int fila, int col) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final Runnable alCambiar;

    public PanelPedidos(Runnable alCambiar) {
        this.alCambiar = alCambiar;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Combos de filtro: "Todos" + valores del enum
        cmbFiltroEstado.addItem(TODOS);
        for (EstadoPedido e : EstadoPedido.values()) cmbFiltroEstado.addItem(e);
        cmbFiltroTipo.addItem(TODOS);
        for (TipoPedido t : TipoPedido.values()) cmbFiltroTipo.addItem(t);

        JPanel norte = new JPanel(new GridLayout(3, 1));
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Dirección:"));
        form.add(txtDireccion);
        form.add(new JLabel("Tipo:"));
        form.add(cmbTipo);
        form.add(new JLabel("Estado:"));
        form.add(cmbEstado);

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRegistrar = new JButton("Registrar");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");
        botones.add(btnRegistrar);
        botones.add(btnEditar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);

        JPanel filtros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtros.add(new JLabel("Filtrar por estado:"));
        filtros.add(cmbFiltroEstado);
        filtros.add(new JLabel("por tipo:"));
        filtros.add(cmbFiltroTipo);
        JButton btnFiltrar = new JButton("Filtrar");
        filtros.add(btnFiltrar);

        norte.add(form);
        norte.add(botones);
        norte.add(filtros);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Al seleccionar una fila se cargan sus datos en el formulario
        tabla.getSelectionModel().addListSelectionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (!e.getValueIsAdjusting() && fila >= 0) {
                txtDireccion.setText(String.valueOf(modelo.getValueAt(fila, 1)));
                cmbTipo.setSelectedItem(TipoPedido.valueOf(String.valueOf(modelo.getValueAt(fila, 2))));
                cmbEstado.setSelectedItem(EstadoPedido.valueOf(String.valueOf(modelo.getValueAt(fila, 3))));
            }
        });

        btnRegistrar.addActionListener(e -> registrar());
        btnEditar.addActionListener(e -> editar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnFiltrar.addActionListener(e -> refrescar());

        refrescar();
    }

    /** Construye un Pedido desde el formulario; retorna null si la validación falla. */
    private Pedido leerFormulario(int id) {
        String direccion = txtDireccion.getText().trim();
        if (direccion.isEmpty()) {
            Mensajes.advertencia(this, "La dirección es obligatoria.");
            return null;
        }
        if (direccion.length() > 100) {
            Mensajes.advertencia(this, "La dirección no puede superar los 100 caracteres.");
            return null;
        }
        return new Pedido(id, direccion,
                (TipoPedido) cmbTipo.getSelectedItem(),
                (EstadoPedido) cmbEstado.getSelectedItem());
    }

    private void registrar() {
        Pedido p = leerFormulario(0);
        if (p == null) return;
        try {
            dao.create(p);
            Mensajes.info(this, "Pedido registrado con ID " + p.getId() + ".");
            limpiar();
            cambiaron();
        } catch (SQLException ex) {
            Mensajes.errorSQL(this, "registrar el pedido", ex);
        }
    }

    private void editar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Mensajes.advertencia(this, "Seleccione un pedido de la tabla para editarlo.");
            return;
        }
        Pedido p = leerFormulario((int) modelo.getValueAt(fila, 0));
        if (p == null) return;
        try {
            if (dao.update(p) > 0) {
                Mensajes.info(this, "Pedido actualizado correctamente.");
            } else {
                Mensajes.advertencia(this, "El pedido ya no existe.");
            }
            cambiaron();
        } catch (SQLException ex) {
            Mensajes.errorSQL(this, "actualizar el pedido", ex);
        }
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Mensajes.advertencia(this, "Seleccione un pedido de la tabla para eliminarlo.");
            return;
        }
        if (!Mensajes.confirmar(this, "¿Eliminar el pedido seleccionado?")) return;
        try {
            dao.delete((int) modelo.getValueAt(fila, 0));
            Mensajes.info(this, "Pedido eliminado.");
            limpiar();
            cambiaron();
        } catch (SQLException ex) {
            Mensajes.errorSQL(this, "eliminar el pedido", ex);
        }
    }

    private void limpiar() {
        txtDireccion.setText("");
        cmbTipo.setSelectedIndex(0);
        cmbEstado.setSelectedIndex(0);
        tabla.clearSelection();
    }

    private void cambiaron() {
        refrescar();
        if (alCambiar != null) alCambiar.run();
    }

    /** Recarga la tabla aplicando los filtros seleccionados. */
    @Override
    public void refrescar() {
        Object fe = cmbFiltroEstado.getSelectedItem();
        Object ft = cmbFiltroTipo.getSelectedItem();
        EstadoPedido estado = (fe instanceof EstadoPedido) ? (EstadoPedido) fe : null;
        TipoPedido tipo = (ft instanceof TipoPedido) ? (TipoPedido) ft : null;
        try {
            List<Pedido> lista = dao.readFiltrado(estado, tipo);
            modelo.setRowCount(0);
            for (Pedido p : lista) {
                modelo.addRow(new Object[]{p.getId(), p.getDireccion(), p.getTipo(), p.getEstado()});
            }
        } catch (SQLException ex) {
            Mensajes.errorSQL(this, "cargar los pedidos", ex);
        }
    }
}
