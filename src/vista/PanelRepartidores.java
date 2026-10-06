package vista;

import dao.RepartidorDAO;
import modelo.Repartidor;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

/** Pestaña de gestión de repartidores (CRUD). */
public class PanelRepartidores extends JPanel implements Refrescable {
    private final RepartidorDAO dao = new RepartidorDAO();
    private final JTextField txtNombre = new JTextField(25);
    private final DefaultTableModel modelo = new DefaultTableModel(new String[]{"ID", "Nombre"}, 0) {
        @Override public boolean isCellEditable(int fila, int col) { return false; }
    };
    private final JTable tabla = new JTable(modelo);
    private final Runnable alCambiar; // avisa a otros paneles (para refrescar combos)

    public PanelRepartidores(Runnable alCambiar) {
        this.alCambiar = alCambiar;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel form = new JPanel(new FlowLayout(FlowLayout.LEFT));
        form.add(new JLabel("Nombre:"));
        form.add(txtNombre);
        JButton btnRegistrar = new JButton("Registrar");
        JButton btnEditar = new JButton("Editar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");
        form.add(btnRegistrar);
        form.add(btnEditar);
        form.add(btnEliminar);
        form.add(btnLimpiar);

        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(form, BorderLayout.NORTH);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // Al seleccionar una fila se carga el nombre en el formulario
        tabla.getSelectionModel().addListSelectionListener(e -> {
            int fila = tabla.getSelectedRow();
            if (!e.getValueIsAdjusting() && fila >= 0) {
                txtNombre.setText(String.valueOf(modelo.getValueAt(fila, 1)));
            }
        });

        btnRegistrar.addActionListener(e -> registrar());
        btnEditar.addActionListener(e -> editar());
        btnEliminar.addActionListener(e -> eliminar());
        btnLimpiar.addActionListener(e -> limpiar());

        refrescar();
    }

    /** Valida el nombre; retorna null si hay error (ya se informó al usuario). */
    private String validarNombre() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            Mensajes.advertencia(this, "El nombre del repartidor es obligatorio.");
            return null;
        }
        if (nombre.length() > 100) {
            Mensajes.advertencia(this, "El nombre no puede superar los 100 caracteres.");
            return null;
        }
        return nombre;
    }

    private void registrar() {
        String nombre = validarNombre();
        if (nombre == null) return;
        try {
            Repartidor r = new Repartidor(0, nombre);
            dao.create(r);
            Mensajes.info(this, "Repartidor registrado con ID " + r.getId() + ".");
            limpiar();
            cambiaron();
        } catch (SQLException ex) {
            Mensajes.errorSQL(this, "registrar el repartidor", ex);
        }
    }

    private void editar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Mensajes.advertencia(this, "Seleccione un repartidor de la tabla para editarlo.");
            return;
        }
        String nombre = validarNombre();
        if (nombre == null) return;
        try {
            int id = (int) modelo.getValueAt(fila, 0);
            if (dao.update(new Repartidor(id, nombre)) > 0) {
                Mensajes.info(this, "Repartidor actualizado correctamente.");
            } else {
                Mensajes.advertencia(this, "El repartidor ya no existe.");
            }
            cambiaron();
        } catch (SQLException ex) {
            Mensajes.errorSQL(this, "actualizar el repartidor", ex);
        }
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            Mensajes.advertencia(this, "Seleccione un repartidor de la tabla para eliminarlo.");
            return;
        }
        if (!Mensajes.confirmar(this, "¿Eliminar al repartidor seleccionado?")) return;
        try {
            dao.delete((int) modelo.getValueAt(fila, 0));
            Mensajes.info(this, "Repartidor eliminado.");
            limpiar();
            cambiaron();
        } catch (SQLException ex) {
            Mensajes.errorSQL(this, "eliminar el repartidor", ex);
        }
    }

    private void limpiar() {
        txtNombre.setText("");
        tabla.clearSelection();
    }

    /** Recarga la tabla y avisa a los demás paneles. */
    private void cambiaron() {
        refrescar();
        if (alCambiar != null) alCambiar.run();
    }

    @Override
    public void refrescar() {
        try {
            List<Repartidor> lista = dao.readAll();
            modelo.setRowCount(0);
            for (Repartidor r : lista) modelo.addRow(new Object[]{r.getId(), r.getNombre()});
        } catch (SQLException ex) {
            Mensajes.errorSQL(this, "cargar los repartidores", ex);
        }
    }
}
