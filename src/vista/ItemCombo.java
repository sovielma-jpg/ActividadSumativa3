package vista;

/**
 * Elemento para JComboBox: muestra un texto legible (p. ej. "3 - Calle 123")
 * pero conserva internamente el id de la entidad.
 */
public class ItemCombo {
    private final int id;
    private final String texto;

    public ItemCombo(int id, String texto) {
        this.id = id;
        this.texto = texto;
    }

    public int getId() { return id; }

    @Override
    public String toString() { return texto; }
}
