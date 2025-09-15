package com.agranelos.inventario.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.logging.Logger;

public class DatabaseInitializer {

    private static final Logger logger = Logger.getLogger(
        DatabaseInitializer.class.getName()
    );

    public static void initializeDatabase() {
        logger.info("Iniciando inicialización de la base de datos...");

        try (Connection conn = DatabaseManager.getConnection()) {
            createTables(conn);
            insertMockData(conn);
            logger.info("Base de datos inicializada exitosamente");
        } catch (SQLException e) {
            logger.severe(
                "Error inicializando la base de datos: " + e.getMessage()
            );
            throw new RuntimeException("Database initialization failed", e);
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        logger.info("Creando tablas...");

        // Create PRODUCTO table
        String createProductoTable =
            "CREATE TABLE IF NOT EXISTS PRODUCTO (" +
            "ID SERIAL PRIMARY KEY, " +
            "Nombre VARCHAR(255) NOT NULL, " +
            "Descripcion TEXT, " +
            "Precio DECIMAL(10,2) NOT NULL DEFAULT 0.00, " +
            "CantidadEnStock INTEGER NOT NULL DEFAULT 0, " +
            "FechaCreacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "FechaActualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")";

        // Create BODEGA table
        String createBodegaTable =
            "CREATE TABLE IF NOT EXISTS BODEGA (" +
            "ID SERIAL PRIMARY KEY, " +
            "Nombre VARCHAR(255) NOT NULL, " +
            "Ubicacion VARCHAR(255) NOT NULL, " +
            "Capacidad INTEGER NOT NULL DEFAULT 1000, " +
            "FechaCreacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "FechaActualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")";

        // Create INVENTARIO table
        String createInventarioTable =
            "CREATE TABLE IF NOT EXISTS INVENTARIO (" +
            "ID SERIAL PRIMARY KEY, " +
            "IDProducto INTEGER NOT NULL, " +
            "IDBodega INTEGER NOT NULL, " +
            "Cantidad INTEGER NOT NULL DEFAULT 0, " +
            "FechaActualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "FOREIGN KEY (IDProducto) REFERENCES PRODUCTO(ID) ON DELETE CASCADE, " +
            "FOREIGN KEY (IDBodega) REFERENCES BODEGA(ID) ON DELETE CASCADE, " +
            "UNIQUE(IDProducto, IDBodega)" +
            ")";

        // Create MOVIMIENTO table
        String createMovimientoTable =
            "CREATE TABLE IF NOT EXISTS MOVIMIENTO (" +
            "ID SERIAL PRIMARY KEY, " +
            "IDProducto INTEGER NOT NULL, " +
            "IDBodega INTEGER NOT NULL, " +
            "Tipo VARCHAR(50) NOT NULL CHECK (Tipo IN ('ENTRADA', 'SALIDA', 'TRANSFERENCIA', 'AJUSTE')), " +
            "Cantidad INTEGER NOT NULL, " +
            "Fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
            "Comentario TEXT, " +
            "UsuarioResponsable VARCHAR(255), " +
            "FOREIGN KEY (IDProducto) REFERENCES PRODUCTO(ID) ON DELETE CASCADE, " +
            "FOREIGN KEY (IDBodega) REFERENCES BODEGA(ID) ON DELETE CASCADE" +
            ")";

        // Execute table creation
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createProductoTable);
            logger.info("Tabla PRODUCTO creada");

            stmt.executeUpdate(createBodegaTable);
            logger.info("Tabla BODEGA creada");

            stmt.executeUpdate(createInventarioTable);
            logger.info("Tabla INVENTARIO creada");

            stmt.executeUpdate(createMovimientoTable);
            logger.info("Tabla MOVIMIENTO creada");
        }
    }

    private static void insertMockData(Connection conn) throws SQLException {
        logger.info("Insertando datos de prueba...");

        // Check if data already exists
        if (dataExists(conn)) {
            logger.info("Los datos de prueba ya existen, omitiendo inserción");
            return;
        }

        insertBodegas(conn);
        insertProductos(conn);
        insertInventario(conn);
        insertMovimientos(conn);
    }

    private static boolean dataExists(Connection conn) throws SQLException {
        String query = "SELECT COUNT(*) FROM PRODUCTO";
        try (
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery()
        ) {
            return rs.next() && rs.getInt(1) > 0;
        }
    }

    private static void insertBodegas(Connection conn) throws SQLException {
        String sql =
            "INSERT INTO BODEGA (Nombre, Ubicacion, Capacidad) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Bodegas españolas realistas
            String[][] bodegas = {
                { "Bodega Central Madrid", "Madrid, España", "5000" },
                { "Almacén Barcelona Norte", "Barcelona, Catalunya", "3500" },
                { "Depósito Sevilla Sur", "Sevilla, Andalucía", "2800" },
                {
                    "Centro Logístico Valencia",
                    "Valencia, Comunidad Valenciana",
                    "4200",
                },
                { "Bodega Bilbao Industrial", "Bilbao, País Vasco", "3000" },
            };

            for (String[] bodega : bodegas) {
                pstmt.setString(1, bodega[0]);
                pstmt.setString(2, bodega[1]);
                pstmt.setInt(3, Integer.parseInt(bodega[2]));
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            logger.info("Bodegas insertadas exitosamente");
        }
    }

    private static void insertProductos(Connection conn) throws SQLException {
        String sql =
            "INSERT INTO PRODUCTO (Nombre, Descripcion, Precio, CantidadEnStock) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Productos españoles diversos
            Object[][] productos = {
                {
                    "Aceite de Oliva Virgen Extra",
                    "Aceite de oliva de primera calidad, origen Andalucía",
                    12.50,
                    150,
                },
                {
                    "Jamón Ibérico de Bellota",
                    "Jamón curado de cerdo ibérico alimentado con bellota",
                    89.99,
                    25,
                },
                {
                    "Queso Manchego Curado",
                    "Queso de oveja manchega con 12 meses de curación",
                    18.75,
                    80,
                },
                {
                    "Vino Tinto Reserva Rioja",
                    "Vino tinto con denominación de origen Rioja, cosecha 2019",
                    24.90,
                    120,
                },
                {
                    "Paella Valenciana (Kit)",
                    "Kit completo para preparar paella valenciana para 6 personas",
                    15.30,
                    60,
                },
                {
                    "Chorizo Ibérico Picante",
                    "Chorizo artesanal ibérico con pimentón picante",
                    8.95,
                    200,
                },
                {
                    "Turron de Alicante",
                    "Turrón duro tradicional de Alicante con almendras",
                    6.75,
                    180,
                },
                {
                    "Gazpacho Andaluz Natural",
                    "Gazpacho andaluz natural refrigerado, envase 1L",
                    4.20,
                    300,
                },
                {
                    "Cava Brut Nature Catalán",
                    "Cava brut nature de denominación Cava",
                    19.50,
                    90,
                },
                {
                    "Morcilla de Burgos",
                    "Morcilla tradicional de Burgos con arroz",
                    7.40,
                    110,
                },
            };

            for (Object[] producto : productos) {
                pstmt.setString(1, (String) producto[0]);
                pstmt.setString(2, (String) producto[1]);
                pstmt.setDouble(3, (Double) producto[2]);
                pstmt.setInt(4, (Integer) producto[3]);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            logger.info("Productos insertados exitosamente");
        }
    }

    private static void insertInventario(Connection conn) throws SQLException {
        String sql =
            "INSERT INTO INVENTARIO (IDProducto, IDBodega, Cantidad) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // Distribuir productos entre bodegas
            for (int productoId = 1; productoId <= 10; productoId++) {
                for (int bodegaId = 1; bodegaId <= 5; bodegaId++) {
                    int cantidad = (int) (Math.random() * 100) + 10; // Cantidad aleatoria entre 10 y 109
                    pstmt.setInt(1, productoId);
                    pstmt.setInt(2, bodegaId);
                    pstmt.setInt(3, cantidad);
                    pstmt.addBatch();
                }
            }
            pstmt.executeBatch();
            logger.info("Inventario inicial insertado exitosamente");
        }
    }

    private static void insertMovimientos(Connection conn) throws SQLException {
        String sql =
            "INSERT INTO MOVIMIENTO (IDProducto, IDBodega, Tipo, Cantidad, Fecha, Comentario, UsuarioResponsable) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String[] tipos = { "ENTRADA", "SALIDA", "TRANSFERENCIA", "AJUSTE" };
            String[] comentarios = {
                "Recepción de mercancía del proveedor",
                "Venta al cliente final",
                "Transferencia entre bodegas",
                "Ajuste por inventario físico",
                "Devolución de cliente",
                "Producto defectuoso retirado",
            };
            String[] usuarios = {
                "Juan García",
                "María López",
                "Carlos Ruiz",
                "Ana Martínez",
                "Pedro Sánchez",
            };

            // Generar movimientos históricos
            for (int i = 0; i < 50; i++) {
                int productoId = (int) (Math.random() * 10) + 1;
                int bodegaId = (int) (Math.random() * 5) + 1;
                String tipo = tipos[(int) (Math.random() * tipos.length)];
                int cantidad = (int) (Math.random() * 50) + 1;

                Timestamp fecha = Timestamp.valueOf(
                    LocalDateTime.now().minusDays((int) (Math.random() * 30))
                );
                String comentario = comentarios[(int) (Math.random() *
                    comentarios.length)];
                String usuario = usuarios[(int) (Math.random() *
                    usuarios.length)];

                pstmt.setInt(1, productoId);
                pstmt.setInt(2, bodegaId);
                pstmt.setString(3, tipo);
                pstmt.setInt(4, cantidad);
                pstmt.setTimestamp(5, fecha);
                pstmt.setString(6, comentario);
                pstmt.setString(7, usuario);
                pstmt.addBatch();
            }
            pstmt.executeBatch();
            logger.info("Movimientos históricos insertados exitosamente");
        }
    }
}
