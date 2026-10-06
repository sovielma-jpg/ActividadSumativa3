package vista;

import javax.swing.*;

/** Ventana principal de SpeedFast: una pestaña por entidad. */
public class VentanaPrincipal extends JFrame {

    public VentanaPrincipal() {
        super("SpeedFast - Gestión de pedidos");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(900, 560);
        setLocationRelativeTo(null);

        JTabbedPane pestanas = new JTabbedPane();
        // Se crea primero el panel de entregas para que los otros paneles puedan refrescar sus combos
        PanelEntregas entregas = new PanelEntregas();
        PanelRepartidores repartidores = new PanelRepartidores(entregas::refrescar);
        PanelPedidos pedidos = new PanelPedidos(entregas::refrescar);

        pestanas.addTab("Repartidores", repartidores);
        pestanas.addTab("Pedidos", pedidos);
        pestanas.addTab("Entregas", entregas);

        // Al cambiar de pestaña se recargan los datos (combos y tablas al día)
        pestanas.addChangeListener(e -> {
            java.awt.Component c = pestanas.getSelectedComponent();
            if (c instanceof Refrescable) ((Refrescable) c).refrescar();
        });

        add(pestanas);
    }
}
