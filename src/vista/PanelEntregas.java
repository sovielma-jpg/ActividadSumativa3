package vista;

import dao.EntregaDAO;
import dao.PedidoDAO;
import dao.RepartidorDAO;
import modelo.Entrega;
import modelo.Pedido;
import modelo.Repartidor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Pestaña de gestión de entregas (CRUD).
 * Pedido y Repartidor se eligen desde JComboBox cargados desde la BD.
 */
public class PanelEntregas extends JPanel implements Refrescable {
    private static final DateTimeFormatter FORMATO_HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final EntregaDAO entregaDAO = new EntregaDAO();
    private final PedidoDAO pedidoDAO = new PedidoDAO();
    private final RepartidorDAO repartidorDAO = new RepartidorDAO();

    private final JComboBox<ItemCombo> cmbPedido = new JComboBox<>();
    private final JComboBox<ItemCombo> cmbRepartidor = new JComboBox<>();
    private final JTextField txtFecha = new JTextField(10);
    private final JTextField txtHora = new JTextField(8);
    // Filtros: el item con id -1 representa "Todos"
    private final JComboBox<ItemCombo> cmbFiltroPedido = new JComboBox<>();
    private final JComboBox<ItemCombo> cmbFiltroRepartidor = new JComboBox<>();

    private final DefaultTableModel modelo = new DefaultTableModel(
            new String[]{"ID", "Pedido", "Repartidor", "Fecha", "Hora"}, 0) {
        @Override public boolean isCellEditable(int fila, int col) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    // Ids de la fila (la tabla muestra texto legible; aquí se conservan los ids)
    private final java.util.List<int[]> idsFilas = new java.util.ArrayList<>();

    public PanelEntregas() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel norte = new JPanel(new GridLayout(3, 1));
        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Pedido:"));
        form.add(cmbPedido);
        form.add(new JLabel("Repartidor:"));
        form.add(cmbRepartidor);
        form.add(new JLabel("Fecha (AAAA-MM-DD):"));
        form.add(txtFecha);
        form.add(new JLabel("Hora (HH:mm):"));
        form.add(txtHora);
        JButton btnAhora = new JButton("Ahora");
        form.add(btnAhora);

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
        filtros.add(new JLabel("Listar por pedido:"));
        filtros.add(cmbFiltroPedido);
        filtros.add(new JLabel("por repartidor:"));
        filtros.add(cmbFiltroRepartidor);
        JButton btnFiltrar = new JButton("Filtrar");
        filtros.add(btnFiltrar);

        norte.add(form);
        norte.add(botones);
        norte.add(filtros);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(norte, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        tabla.getSelectionModel().addListSelectionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (!e.getValueIsAdjusting() && fila >= 0) cargarFilaEnFormulario(fila);
        });

        btnAhora.addActionListener(e -> {
            txtFecha.setText(LocalDate.now().toString());
            txtHora.setText(LocalTime.now().withNano(0).format(FORMATO_HORA));
        });
        btnRegistrar.addActionListener(e -> registrar());
        btnEditar.addActionListener(e -> editar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());
        btnFiltrar.addActionListener(e -> cargarTabla());

        refrescar();
    }

    /** Selecciona en un combo el item con el id indicado. */
    private static void seleccionarId(JComboBox<ItemCombo> combo, int id) {
        for (int i = 0; i < combo.getItemCount(); i++) {
            if (combo.getItemAt(i).getId() == id) {
                combo.setSelectedIndex(i);
                return;
            }
        }
    }

    private void cargarFilaEnFormulario(int fila) {
        int[] ids = idsFilas.get(fila); // {idPedido, idRepartidor}
        seleccionarId(cmbPedido, ids[0]);
        seleccionarId(cmbRepartidor, ids[1]);
        txtFecha.setText(String.valueOf(modelo.getValueAt(fila, 3)));
        txtHora.setText(String.valueOf(modelo.getValueAt(fila, 4)));
    }

    /** Valida el formulario y arma la Entrega; retorna null si hay errores. */
    private Entrega leerFormulario(int id) {
        ItemCombo pedido = (ItemCombo) cmbPedido.getSelectedItem();
        ItemCombo repartidor = (ItemCombo) cmbRepartidor.getSelectedItem();
        if (pedido == null || repartidor == null) {
            Mensajes.advertencia(this, "Debe existir y seleccionarse un pedido y un repartidor.\n"
                    + "Registre primero pedidos y repartidores.");
            return null;
        }
        String fechaTxt = txtFecha.getText().trim();
        String horaTxt = txtHora.getText().trim();
        if (fechaTxt.isEmpty() || horaTxt.isEmpty()) {
            Mensajes.advertencia(this, "La fecha y la hora son obligatorias.");
            return null;
        }
        LocalDate fecha;
        LocalTime hora;
        try {
            fecha = LocalDate.parse(fechaTxt);
        } catch (DateTimeParseException ex) {
            Mensajes.advertencia(this, "Fecha inválida. Use el formato AAAA-MM-DD (ej.: 2026-10-05).");
            return null;
        }
        try {
            hora = LocalTime.parse(horaTxt);
        } catch (DateTimeParseException ex) {
            Mensajes.advertencia(this, "Hora inválida. Use el formato HH:mm (ej.: 14:30).");
            return null;
        }
        return new Entrega(id, pedido.getId(), repartidor.getId(), fecha, hora);
    }

    private void registrar() {
        Entrega e = leerFormulario(0);
        if (e == null) return;
        try {
            entregaDAO.create(e);
            Mensajes.info(this, "Entrega registrada con ID " + e.getId() + ".");
            limpiar();
            cargarTabla();
        } catch (SQLException ex) {
            Mensajes.errorSQL(this, "registrar la entrega", ex);
        }
    }

    private void editar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Mensajes.advertencia(this, "Seleccione una entrega de la tabla para editarla.");
            return;
        }
        Entrega e = leerFormulario((int) modelo.getValueAt(fila, 0));
        if (e == null) return;
        try {
            if (entregaDAO.update(e) > 0) {
                Mensajes.info(this, "Entrega actualizada correctamente.");
            } else {
                Mensajes.advertencia(this, "La entrega ya no existe.");
            }
            cargarTabla();
        } catch (SQLException ex) {
            Mensajes.errorSQL(this, "actualizar la entrega", ex);
        }
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Mensajes.advertencia(this, "Seleccione una entrega de la tabla para eliminarla.");
            return;
        }
        if (!Mensajes.confirmar(this, "¿Eliminar la entrega seleccionada?")) return;
        try {
            entregaDAO.delete((int) modelo.getValueAt(fila, 0));
            Mensajes.info(this, "Entrega eliminada.");
            limpiar();
            cargarTabla();
        } catch (SQLException ex) {
            Mensajes.errorSQL(this, "eliminar la entrega", ex);
        }
    }

    private void limpiar() {
        txtFecha.setText("");
        txtHora.setText("");
        if (cmbPedido.getItemCount() > 0) cmbPedido.setSelectedIndex(0);
        if (cmbRepartidor.getItemCount() > 0) cmbRepartidor.setSelectedIndex(0);
        tabla.clearSelection();
    }

    /** Recarga los combos desde la BD conservando la selección actual. */
    private void cargarCombos() throws SQLException {
        ItemCombo selPedido = (ItemCombo) cmbPedido.getSelectedItem();
        ItemCombo selRepartidor = (ItemCombo) cmbRepartidor.getSelectedItem();
        ItemCombo selFP = (ItemCombo) cmbFiltroPedido.getSelectedItem();
        ItemCombo selFR = (ItemCombo) cmbFiltroRepartidor.getSelectedItem();

        cmbPedido.removeAllItems();
        cmbFiltroPedido.removeAllItems();
        cmbFiltroPedido.addItem(new ItemCombo(-1, "Todos"));
        for (Pedido p : pedidoDAO.readAll()) {
            ItemCombo item = new ItemCombo(p.getId(), p.getId() + " - " + p.getDireccion());
            cmbPedido.addItem(item);
            cmbFiltroPedido.addItem(item);
        }

        cmbRepartidor.removeAllItems();
        cmbFiltroRepartidor.removeAllItems();
        cmbFiltroRepartidor.addItem(new ItemCombo(-1, "Todos"));
        for (Repartidor r : repartidorDAO.readAll()) {
            ItemCombo item = new ItemCombo(r.getId(), r.getId() + " - " + r.getNombre());
            cmbRepartidor.addItem(item);
            cmbFiltroRepartidor.addItem(item);
        }

        if (selPedido != null) seleccionarId(cmbPedido, selPedido.getId());
        if (selRepartidor != null) seleccionarId(cmbRepartidor, selRepartidor.getId());
        if (selFP != null) seleccionarId(cmbFiltroPedido, selFP.getId());
        if (selFR != null) seleccionarId(cmbFiltroRepartidor, selFR.getId());
    }

    /** Carga la tabla según los filtros (pedido y/o repartidor). */
    private void cargarTabla() {
        ItemCombo fp = (ItemCombo) cmbFiltroPedido.getSelectedItem();
        ItemCombo fr = (ItemCombo) cmbFiltroRepartidor.getSelectedItem();
        boolean porPedido = fp != null && fp.getId() > 0;
        boolean porRepartidor = fr != null && fr.getId() > 0;
        try {
            List<Entrega> lista;
            if (porPedido) {
                lista = entregaDAO.readByPedido(fp.getId());
                if (porRepartidor) lista.removeIf(e -> e.getIdRepartidor() != fr.getId());
            } else if (porRepartidor) {
                lista = entregaDAO.readByRepartidor(fr.getId());
            } else {
                lista = entregaDAO.readAll();
            }
            modelo.setRowCount(0);
            idsFilas.clear();
            for (Entrega e : lista) {
                modelo.addRow(new Object[]{
                        e.getId(),
                        e.getIdPedido() + " - " + e.getDireccionPedido(),
                        e.getIdRepartidor() + " - " + e.getNombreRepartidor(),
                        e.getFecha(),
                        e.getHora()});
                idsFilas.add(new int[]{e.getIdPedido(), e.getIdRepartidor()});
            }
        } catch (SQLException ex) {
            Mensajes.errorSQL(this, "cargar las entregas", ex);
        }
    }

    @Override
    public void refrescar() {
        try {
            cargarCombos();
        } catch (SQLException ex) {
            Mensajes.errorSQL(this, "cargar pedidos y repartidores", ex);
        }
        cargarTabla();
    }
}
