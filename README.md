# SpeedFast – Gestión de pedidos (Semana 8, Desarrollo Orientado a Objetos II)

Aplicación Java Swing con CRUD completo sobre MySQL usando JDBC (PreparedStatement y ResultSet).

## Estructura
- `src/modelo`: entidades (`Repartidor`, `Pedido`, `Entrega`) y enums (`TipoPedido`, `EstadoPedido`).
- `src/dao`: `ConexionDB`, `RepartidorDAO`, `PedidoDAO`, `EntregaDAO` (`create`, `readAll`, `update`, `delete`).
- `src/vista`: interfaz Swing (`VentanaPrincipal` con pestañas, un panel por entidad).
- `sql/speedfast_db.sql`: script de la base de datos.

## Cómo ejecutar (IntelliJ IDEA)
1. Ejecuta `sql/speedfast_db.sql` en MySQL Workbench (crea `speedfast_db` y las tablas).
2. Agrega el conector MySQL: File > Project Structure > Libraries > + > From Java > `com.mysql:mysql-connector-j:8.4.0`.
3. Ajusta usuario/clave en `dao/ConexionDB.java` 
4. Ejecuta `Main`.
