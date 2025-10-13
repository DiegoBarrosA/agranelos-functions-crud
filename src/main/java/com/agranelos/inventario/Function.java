package com.agranelos.inventario;

import com.agranelos.inventario.db.DatabaseInitializer;
import com.agranelos.inventario.db.DatabaseManager;
import com.agranelos.inventario.model.Producto;
import com.agranelos.inventario.model.Bodega;
import com.agranelos.inventario.graphql.GraphQLSchemaBuilder;
import com.agranelos.inventario.events.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.BindingName;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

/**
 * Azure Functions para el sistema de inventario de Bodegas Agranelos
 */
public class Function {

    private static final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private static final AtomicBoolean databaseInitialized = new AtomicBoolean(false);
    
    /**
     * Capacidad por defecto para bodegas cuando no se especifica
     */
    private static final int DEFAULT_CAPACITY = 1000;

    @FunctionName("InitializeDatabase")
    public HttpResponseMessage initializeDatabase(
        @HttpTrigger(
            name = "req",
            methods = { HttpMethod.POST },
            authLevel = AuthorizationLevel.ANONYMOUS,
            route = "init"
        ) HttpRequestMessage<Optional<String>> request,
        final ExecutionContext context
    ) {
        Logger logger = context.getLogger();
        logger.info("Inicializando base de datos...");

        try {
            DatabaseManager.initialize();
            DatabaseInitializer.initializeDatabase();
            databaseInitialized.set(true);

            return request
                .createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(
                    "{\"mensaje\": \"Base de datos inicializada exitosamente\", \"estado\": \"OK\"}"
                )
                .build();
        } catch (Exception e) {
            logger.severe(
                "Error inicializando la base de datos: " + e.getMessage()
            );
            return request
                .createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body(
                    "{\"error\": \"Error inicializando la base de datos\", \"detalle\": \"" +
                    e.getMessage() +
                    "\"}"
                )
                .build();
        }
    }

    @FunctionName("GetProductos")
    public HttpResponseMessage getProductos(
        @HttpTrigger(
            name = "req",
            methods = { HttpMethod.GET },
            authLevel = AuthorizationLevel.ANONYMOUS,
            route = "productos"
        ) HttpRequestMessage<Optional<String>> request,
        final ExecutionContext context
    ) {
        Logger logger = context.getLogger();
        logger.info("Obteniendo lista de productos...");

        try {
            ensureDatabaseInitialized();
            List<Producto> productos = getAllProductos(logger);

            return request
                .createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(objectMapper.writeValueAsString(productos))
                .build();
        } catch (Exception e) {
            logger.severe("Error obteniendo productos: " + e.getMessage());
            return request
                .createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body(
                    "{\"error\": \"Error obteniendo productos\", \"detalle\": \"" +
                    e.getMessage() +
                    "\"}"
                )
                .build();
        }
    }

    @FunctionName("GetProductoById")
    public HttpResponseMessage getProductoById(
        @HttpTrigger(
            name = "req",
            methods = { HttpMethod.GET },
            authLevel = AuthorizationLevel.ANONYMOUS,
            route = "productos/{id}"
        ) HttpRequestMessage<Optional<String>> request,
        @BindingName("id") String id,
        final ExecutionContext context
    ) {
        Logger logger = context.getLogger();
        String productId = id;
        logger.info("Obteniendo producto con ID: " + productId);

        try {
            ensureDatabaseInitialized();

            if (productId == null) {
                return request
                    .createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"ID de producto requerido\"}")
                    .build();
            }

            Producto producto = getProductoById(
                Integer.parseInt(productId),
                logger
            );

            if (producto == null) {
                return request
                    .createResponseBuilder(HttpStatus.NOT_FOUND)
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"Producto no encontrado\"}")
                    .build();
            }

            return request
                .createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(objectMapper.writeValueAsString(producto))
                .build();
        } catch (NumberFormatException e) {
            logger.warning("ID de producto inválido: " + productId);
            return request
                .createResponseBuilder(HttpStatus.BAD_REQUEST)
                .header("Content-Type", "application/json")
                .body(
                    "{\"error\": \"ID de producto debe ser un número válido\"}"
                )
                .build();
        } catch (Exception e) {
            logger.severe("Error obteniendo producto: " + e.getMessage());
            return request
                .createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body(
                    "{\"error\": \"Error obteniendo producto\", \"detalle\": \"" +
                    e.getMessage() +
                    "\"}"
                )
                .build();
        }
    }

    @FunctionName("CreateProducto")
    public HttpResponseMessage createProducto(
        @HttpTrigger(
            name = "req",
            methods = { HttpMethod.POST },
            authLevel = AuthorizationLevel.ANONYMOUS,
            route = "productos"
        ) HttpRequestMessage<Optional<String>> request,
        final ExecutionContext context
    ) {
        Logger logger = context.getLogger();
        logger.info("Creando nuevo producto...");

        try {
            ensureDatabaseInitialized();
            String requestBody = request.getBody().orElse("");

            if (requestBody.isEmpty()) {
                return request
                    .createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"Cuerpo de la petición requerido\"}")
                    .build();
            }

            Producto producto;
            try {
                producto = objectMapper.readValue(requestBody, Producto.class);
            } catch (JsonProcessingException e) {
                logger.warning("Error parsing JSON: " + e.getMessage());
                return request
                    .createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"JSON inválido\"}")
                    .build();
            }

            // Validaciones básicas
            if (
                producto.getNombre() == null ||
                producto.getNombre().trim().isEmpty()
            ) {
                return request
                    .createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body(
                        "{\"error\": \"El nombre del producto es requerido\"}"
                    )
                    .build();
            }

            Integer productoId = insertProducto(producto, logger);
            
            // Publicar evento en Event Grid
            producto.setId(productoId);
            ProductoEventData eventData = new ProductoEventData(producto, "CREATE", "system");
            EventGridPublisher.publishProductoEvent(EventType.PRODUCTO_CREADO, eventData, logger);

            return request
                .createResponseBuilder(HttpStatus.CREATED)
                .header("Content-Type", "application/json")
                .body(
                    "{\"mensaje\": \"Producto creado exitosamente\", \"id\": " +
                    productoId +
                    "}"
                )
                .build();
        } catch (Exception e) {
            logger.severe("Error creando producto: " + e.getMessage());
            return request
                .createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body(
                    "{\"error\": \"Error creando producto\", \"detalle\": \"" +
                    e.getMessage() +
                    "\"}"
                )
                .build();
        }
    }

    @FunctionName("UpdateProducto")
    public HttpResponseMessage updateProducto(
        @HttpTrigger(
            name = "req",
            methods = { HttpMethod.PUT },
            authLevel = AuthorizationLevel.ANONYMOUS,
            route = "productos/{id}"
        ) HttpRequestMessage<Optional<String>> request,
        @BindingName("id") String id,
        final ExecutionContext context
    ) {
        Logger logger = context.getLogger();
        String productId = id;
        logger.info("Actualizando producto con ID: " + productId);

        try {
            ensureDatabaseInitialized();
            String requestBody = request.getBody().orElse("");

            if (productId == null) {
                return request
                    .createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"ID de producto requerido\"}")
                    .build();
            }

            if (requestBody.isEmpty()) {
                return request
                    .createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"Cuerpo de la petición requerido\"}")
                    .build();
            }

            Producto producto;
            try {
                producto = objectMapper.readValue(requestBody, Producto.class);
            } catch (JsonProcessingException e) {
                logger.warning("Error parsing JSON: " + e.getMessage());
                return request
                    .createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"JSON inválido\"}")
                    .build();
            }
            producto.setId(Integer.parseInt(productId));

            boolean updated = updateProducto(producto, logger);

            if (!updated) {
                return request
                    .createResponseBuilder(HttpStatus.NOT_FOUND)
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"Producto no encontrado\"}")
                    .build();
            }
            
            // Publicar evento en Event Grid
            ProductoEventData eventData = new ProductoEventData(producto, "UPDATE", "system");
            EventGridPublisher.publishProductoEvent(EventType.PRODUCTO_ACTUALIZADO, eventData, logger);

            return request
                .createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body("{\"mensaje\": \"Producto actualizado exitosamente\"}")
                .build();
        } catch (NumberFormatException e) {
            logger.warning("ID de producto inválido: " + productId);
            return request
                .createResponseBuilder(HttpStatus.BAD_REQUEST)
                .header("Content-Type", "application/json")
                .body(
                    "{\"error\": \"ID de producto debe ser un número válido\"}"
                )
                .build();
        } catch (Exception e) {
            logger.severe("Error actualizando producto: " + e.getMessage());
            return request
                .createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body(
                    "{\"error\": \"Error actualizando producto\", \"detalle\": \"" +
                    e.getMessage() +
                    "\"}"
                )
                .build();
        }
    }

    @FunctionName("DeleteProducto")
    public HttpResponseMessage deleteProducto(
        @HttpTrigger(
            name = "req",
            methods = { HttpMethod.DELETE },
            authLevel = AuthorizationLevel.ANONYMOUS,
            route = "productos/{id}"
        ) HttpRequestMessage<Optional<String>> request,
        @BindingName("id") String id,
        final ExecutionContext context
    ) {
        Logger logger = context.getLogger();
        String productId = id;
        logger.info("Eliminando producto con ID: " + productId);

        try {
            ensureDatabaseInitialized();

            if (productId == null) {
                return request
                    .createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"ID de producto requerido\"}")
                    .build();
            }

            boolean deleted = deleteProducto(
                Integer.parseInt(productId),
                logger
            );

            if (!deleted) {
                return request
                    .createResponseBuilder(HttpStatus.NOT_FOUND)
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"Producto no encontrado\"}")
                    .build();
            }
            
            // Publicar evento en Event Grid
            ProductoEventData eventData = new ProductoEventData();
            eventData.setProductoId(Integer.parseInt(productId));
            eventData.setOperation("DELETE");
            eventData.setUsuario("system");
            EventGridPublisher.publishProductoEvent(EventType.PRODUCTO_ELIMINADO, eventData, logger);

            return request
                .createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body("{\"mensaje\": \"Producto eliminado exitosamente\"}")
                .build();
        } catch (NumberFormatException e) {
            logger.warning("ID de producto inválido: " + productId);
            return request
                .createResponseBuilder(HttpStatus.BAD_REQUEST)
                .header("Content-Type", "application/json")
                .body(
                    "{\"error\": \"ID de producto debe ser un número válido\"}"
                )
                .build();
        } catch (Exception e) {
            logger.severe("Error eliminando producto: " + e.getMessage());
            return request
                .createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body(
                    "{\"error\": \"Error eliminando producto\", \"detalle\": \"" +
                    e.getMessage() +
                    "\"}"
                )
                .build();
        }
    }

    // Métodos auxiliares privados

    private void ensureDatabaseInitialized() {
        if (databaseInitialized.compareAndSet(false, true)) {
            DatabaseManager.initialize();
        }
    }

    private List<Producto> getAllProductos(Logger logger) throws SQLException {
        String sql =
            "SELECT ID, Nombre, Descripcion, Precio, CantidadEnStock, FechaCreacion, FechaActualizacion FROM PRODUCTO ORDER BY Nombre";
        List<Producto> productos = new ArrayList<>();

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()
        ) {
            while (rs.next()) {
                Producto producto = new Producto();
                producto.setId(rs.getInt("ID"));
                producto.setNombre(rs.getString("Nombre"));
                producto.setDescripcion(rs.getString("Descripcion"));
                producto.setPrecio(rs.getBigDecimal("Precio"));
                producto.setCantidadEnStock(rs.getInt("CantidadEnStock"));
                if (rs.getTimestamp("FechaCreacion") != null) {
                    producto.setFechaCreacion(
                        rs.getTimestamp("FechaCreacion").toLocalDateTime()
                    );
                }
                if (rs.getTimestamp("FechaActualizacion") != null) {
                    producto.setFechaActualizacion(
                        rs.getTimestamp("FechaActualizacion").toLocalDateTime()
                    );
                }
                productos.add(producto);
            }

            logger.info("Se obtuvieron " + productos.size() + " productos");
            return productos;
        } catch (SQLException e) {
            logger.severe(
                "Error en la consulta de productos: " + e.getMessage()
            );
            throw e;
        }
    }

    private Producto getProductoById(Integer id, Logger logger)
        throws SQLException {
        String sql =
            "SELECT ID, Nombre, Descripcion, Precio, CantidadEnStock, FechaCreacion, FechaActualizacion FROM PRODUCTO WHERE ID = ?";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Producto producto = new Producto();
                    producto.setId(rs.getInt("ID"));
                    producto.setNombre(rs.getString("Nombre"));
                    producto.setDescripcion(rs.getString("Descripcion"));
                    producto.setPrecio(rs.getBigDecimal("Precio"));
                    producto.setCantidadEnStock(rs.getInt("CantidadEnStock"));
                    if (rs.getTimestamp("FechaCreacion") != null) {
                        producto.setFechaCreacion(
                            rs.getTimestamp("FechaCreacion").toLocalDateTime()
                        );
                    }
                    if (rs.getTimestamp("FechaActualizacion") != null) {
                        producto.setFechaActualizacion(
                            rs
                                .getTimestamp("FechaActualizacion")
                                .toLocalDateTime()
                        );
                    }
                    logger.info("Producto encontrado: " + producto.getNombre());
                    return producto;
                }
            }
        } catch (SQLException e) {
            logger.severe("Error buscando producto por ID: " + e.getMessage());
            throw e;
        }

        return null;
    }

    private Integer insertProducto(Producto producto, Logger logger)
        throws SQLException {
        String sql =
            "INSERT INTO PRODUCTO (Nombre, Descripcion, Precio, CantidadEnStock) VALUES (?, ?, ?, ?) RETURNING ID";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, producto.getNombre());
            pstmt.setString(2, producto.getDescripcion());
            pstmt.setBigDecimal(
                3,
                producto.getPrecio() != null
                    ? producto.getPrecio()
                    : BigDecimal.ZERO
            );
            pstmt.setInt(
                4,
                producto.getCantidadEnStock() != null
                    ? producto.getCantidadEnStock()
                    : 0
            );

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Integer id = rs.getInt("ID");
                    logger.info(
                        "Producto creado con ID: " +
                        id +
                        " - " +
                        producto.getNombre()
                    );
                    return id;
                }
            }
        } catch (SQLException e) {
            logger.severe("Error insertando producto: " + e.getMessage());
            throw e;
        }

        throw new SQLException("No se pudo crear el producto");
    }

    private boolean updateProducto(Producto producto, Logger logger)
        throws SQLException {
        String sql =
            "UPDATE PRODUCTO SET Nombre = ?, Descripcion = ?, Precio = ?, CantidadEnStock = ?, FechaActualizacion = CURRENT_TIMESTAMP WHERE ID = ?";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, producto.getNombre());
            pstmt.setString(2, producto.getDescripcion());
            pstmt.setBigDecimal(
                3,
                producto.getPrecio() != null
                    ? producto.getPrecio()
                    : BigDecimal.ZERO
            );
            pstmt.setInt(
                4,
                producto.getCantidadEnStock() != null
                    ? producto.getCantidadEnStock()
                    : 0
            );
            pstmt.setInt(5, producto.getId());

            int rowsAffected = pstmt.executeUpdate();
            boolean updated = rowsAffected > 0;

            if (updated) {
                logger.info(
                    "Producto actualizado: ID " +
                    producto.getId() +
                    " - " +
                    producto.getNombre()
                );
            } else {
                logger.warning(
                    "No se encontró producto con ID: " + producto.getId()
                );
            }

            return updated;
        } catch (SQLException e) {
            logger.severe("Error actualizando producto: " + e.getMessage());
            throw e;
        }
    }

    private boolean deleteProducto(Integer id, Logger logger)
        throws SQLException {
        String sql = "DELETE FROM PRODUCTO WHERE ID = ?";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, id);

            int rowsAffected = pstmt.executeUpdate();
            boolean deleted = rowsAffected > 0;

            if (deleted) {
                logger.info("Producto eliminado: ID " + id);
            } else {
                logger.warning("No se encontró producto con ID: " + id);
            }

            return deleted;
        } catch (SQLException e) {
            logger.severe("Error eliminando producto: " + e.getMessage());
            throw e;
        }
    }

    // ======================== FUNCIONES SERVERLESS PARA BODEGAS ========================

    @FunctionName("GetBodegas")
    public HttpResponseMessage getBodegas(
        @HttpTrigger(
            name = "req",
            methods = { HttpMethod.GET },
            authLevel = AuthorizationLevel.ANONYMOUS,
            route = "bodegas"
        ) HttpRequestMessage<Optional<String>> request,
        final ExecutionContext context
    ) {
        Logger logger = context.getLogger();
        logger.info("Obteniendo lista de bodegas...");

        try {
            ensureDatabaseInitialized();
            List<Bodega> bodegas = getAllBodegas(logger);

            return request
                .createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(objectMapper.writeValueAsString(bodegas))
                .build();
        } catch (Exception e) {
            logger.severe("Error obteniendo bodegas: " + e.getMessage());
            return request
                .createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body(
                    "{\"error\": \"Error obteniendo bodegas\", \"detalle\": \"" +
                    e.getMessage() +
                    "\"}"
                )
                .build();
        }
    }

    @FunctionName("GetBodegaById")
    public HttpResponseMessage getBodegaById(
        @HttpTrigger(
            name = "req",
            methods = { HttpMethod.GET },
            authLevel = AuthorizationLevel.ANONYMOUS,
            route = "bodegas/{id}"
        ) HttpRequestMessage<Optional<String>> request,
        @BindingName("id") String id,
        final ExecutionContext context
    ) {
        Logger logger = context.getLogger();
        String bodegaId = id;
        logger.info("Obteniendo bodega con ID: " + bodegaId);

        try {
            ensureDatabaseInitialized();

            if (bodegaId == null) {
                return request
                    .createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"ID de bodega requerido\"}")
                    .build();
            }

            Bodega bodega = getBodegaById(
                Integer.parseInt(bodegaId),
                logger
            );

            if (bodega == null) {
                return request
                    .createResponseBuilder(HttpStatus.NOT_FOUND)
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"Bodega no encontrada\"}")
                    .build();
            }

            return request
                .createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(objectMapper.writeValueAsString(bodega))
                .build();
        } catch (NumberFormatException e) {
            logger.warning("ID de bodega inválido: " + bodegaId);
            return request
                .createResponseBuilder(HttpStatus.BAD_REQUEST)
                .header("Content-Type", "application/json")
                .body(
                    "{\"error\": \"ID de bodega debe ser un número válido\"}"
                )
                .build();
        } catch (Exception e) {
            logger.severe("Error obteniendo bodega: " + e.getMessage());
            return request
                .createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body(
                    "{\"error\": \"Error obteniendo bodega\", \"detalle\": \"" +
                    e.getMessage() +
                    "\"}"
                )
                .build();
        }
    }

    @FunctionName("CreateBodega")
    public HttpResponseMessage createBodega(
        @HttpTrigger(
            name = "req",
            methods = { HttpMethod.POST },
            authLevel = AuthorizationLevel.ANONYMOUS,
            route = "bodegas"
        ) HttpRequestMessage<Optional<String>> request,
        final ExecutionContext context
    ) {
        Logger logger = context.getLogger();
        logger.info("Creando nueva bodega...");

        try {
            ensureDatabaseInitialized();
            String requestBody = request.getBody().orElse("");

            if (requestBody.isEmpty()) {
                return request
                    .createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"Cuerpo de la petición requerido\"}")
                    .build();
            }

            Bodega bodega;
            try {
                bodega = objectMapper.readValue(requestBody, Bodega.class);
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                logger.warning("JSON mal formado: " + e.getMessage());
                return request
                    .createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"JSON mal formado\", \"detalle\": \"" + e.getMessage() + "\"}")
                    .build();
            }

            // Validaciones básicas
            if (
                bodega.getNombre() == null ||
                bodega.getNombre().trim().isEmpty()
            ) {
                return request
                    .createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body(
                        "{\"error\": \"El nombre de la bodega es requerido\"}"
                    )
                    .build();
            }

            if (
                bodega.getUbicacion() == null ||
                bodega.getUbicacion().trim().isEmpty()
            ) {
                return request
                    .createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body(
                        "{\"error\": \"La ubicación de la bodega es requerida\"}"
                    )
                    .build();
            }

            Integer bodegaId = insertBodega(bodega, logger);
            
            // Publicar evento en Event Grid
            bodega.setId(bodegaId);
            BodegaEventData eventData = new BodegaEventData(bodega, "CREATE", "system");
            EventGridPublisher.publishBodegaEvent(EventType.BODEGA_CREADA, eventData, logger);

            return request
                .createResponseBuilder(HttpStatus.CREATED)
                .header("Content-Type", "application/json")
                .body(
                    "{\"mensaje\": \"Bodega creada exitosamente\", \"id\": " +
                    bodegaId +
                    "}"
                )
                .build();
        } catch (Exception e) {
            logger.severe("Error creando bodega: " + e.getMessage());
            return request
                .createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body(
                    "{\"error\": \"Error creando bodega\", \"detalle\": \"" +
                    e.getMessage() +
                    "\"}"
                )
                .build();
        }
    }

    @FunctionName("UpdateBodega")
    public HttpResponseMessage updateBodega(
        @HttpTrigger(
            name = "req",
            methods = { HttpMethod.PUT },
            authLevel = AuthorizationLevel.ANONYMOUS,
            route = "bodegas/{id}"
        ) HttpRequestMessage<Optional<String>> request,
        @BindingName("id") String id,
        final ExecutionContext context
    ) {
        Logger logger = context.getLogger();
        String bodegaId = id;
        logger.info("Actualizando bodega con ID: " + bodegaId);

        try {
            ensureDatabaseInitialized();
            String requestBody = request.getBody().orElse("");

            if (bodegaId == null) {
                return request
                    .createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"ID de bodega requerido\"}")
                    .build();
            }

            if (requestBody.isEmpty()) {
                return request
                    .createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"Cuerpo de la petición requerido\"}")
                    .build();
            }

            Bodega bodega;
            try {
                bodega = objectMapper.readValue(requestBody, Bodega.class);
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                logger.warning("JSON mal formado: " + e.getMessage());
                return request
                    .createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"JSON mal formado\", \"detalle\": \"" + e.getMessage() + "\"}")
                    .build();
            }
            bodega.setId(Integer.parseInt(bodegaId));

            boolean updated = updateBodega(bodega, logger);

            if (!updated) {
                return request
                    .createResponseBuilder(HttpStatus.NOT_FOUND)
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"Bodega no encontrada\"}")
                    .build();
            }
            
            // Publicar evento en Event Grid
            BodegaEventData eventData = new BodegaEventData(bodega, "UPDATE", "system");
            EventGridPublisher.publishBodegaEvent(EventType.BODEGA_ACTUALIZADA, eventData, logger);

            return request
                .createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body("{\"mensaje\": \"Bodega actualizada exitosamente\"}")
                .build();
        } catch (NumberFormatException e) {
            logger.warning("ID de bodega inválido: " + bodegaId);
            return request
                .createResponseBuilder(HttpStatus.BAD_REQUEST)
                .header("Content-Type", "application/json")
                .body(
                    "{\"error\": \"ID de bodega debe ser un número válido\"}"
                )
                .build();
        } catch (Exception e) {
            logger.severe("Error actualizando bodega: " + e.getMessage());
            return request
                .createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body(
                    "{\"error\": \"Error actualizando bodega\", \"detalle\": \"" +
                    e.getMessage() +
                    "\"}"
                )
                .build();
        }
    }

    @FunctionName("DeleteBodega")
    public HttpResponseMessage deleteBodega(
        @HttpTrigger(
            name = "req",
            methods = { HttpMethod.DELETE },
            authLevel = AuthorizationLevel.ANONYMOUS,
            route = "bodegas/{id}"
        ) HttpRequestMessage<Optional<String>> request,
        @BindingName("id") String id,
        final ExecutionContext context
    ) {
        Logger logger = context.getLogger();
        String bodegaId = id;
        logger.info("Eliminando bodega con ID: " + bodegaId);

        try {
            ensureDatabaseInitialized();

            if (bodegaId == null) {
                return request
                    .createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"ID de bodega requerido\"}")
                    .build();
            }

            boolean deleted = deleteBodega(
                Integer.parseInt(bodegaId),
                logger
            );

            if (!deleted) {
                return request
                    .createResponseBuilder(HttpStatus.NOT_FOUND)
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"Bodega no encontrada\"}")
                    .build();
            }
            
            // Publicar evento en Event Grid
            BodegaEventData eventData = new BodegaEventData();
            eventData.setBodegaId(Integer.parseInt(bodegaId));
            eventData.setOperation("DELETE");
            eventData.setUsuario("system");
            EventGridPublisher.publishBodegaEvent(EventType.BODEGA_ELIMINADA, eventData, logger);

            return request
                .createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body("{\"mensaje\": \"Bodega eliminada exitosamente\"}")
                .build();
        } catch (NumberFormatException e) {
            logger.warning("ID de bodega inválido: " + bodegaId);
            return request
                .createResponseBuilder(HttpStatus.BAD_REQUEST)
                .header("Content-Type", "application/json")
                .body(
                    "{\"error\": \"ID de bodega debe ser un número válido\"}"
                )
                .build();
        } catch (Exception e) {
            logger.severe("Error eliminando bodega: " + e.getMessage());
            return request
                .createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body(
                    "{\"error\": \"Error eliminando bodega\", \"detalle\": \"" +
                    e.getMessage() +
                    "\"}"
                )
                .build();
        }
    }

    // ======================== FUNCIONES DE INVENTARIO ========================

    @FunctionName("GetBodegaProductos")
    public HttpResponseMessage getBodegaProductos(
        @HttpTrigger(
            name = "req",
            methods = { HttpMethod.GET },
            authLevel = AuthorizationLevel.ANONYMOUS,
            route = "bodegas/{bodegaId}/productos"
        ) HttpRequestMessage<Optional<String>> request,
        @BindingName("bodegaId") String bodegaId,
        final ExecutionContext context
    ) {
        Logger logger = context.getLogger();
        logger.info("Obteniendo productos de bodega ID: " + bodegaId);

        try {
            int id = Integer.parseInt(bodegaId);
            List<Map<String, Object>> productosEnBodega = getProductosEnBodega(id, logger);

            String jsonResponse = objectMapper.writeValueAsString(productosEnBodega);
            return request
                .createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(jsonResponse)
                .build();
        } catch (NumberFormatException e) {
            logger.warning("ID de bodega inválido: " + bodegaId);
            return request
                .createResponseBuilder(HttpStatus.BAD_REQUEST)
                .header("Content-Type", "application/json")
                .body("{\"error\": \"ID de bodega debe ser un número válido\"}")
                .build();
        } catch (Exception e) {
            logger.severe("Error obteniendo productos de bodega: " + e.getMessage());
            return request
                .createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body(
                    "{\"error\": \"Error obteniendo productos de bodega\", \"detalle\": \"" +
                    e.getMessage() + "\"}"
                )
                .build();
        }
    }

    @FunctionName("AsignarProductoABodega")
    public HttpResponseMessage asignarProductoABodega(
        @HttpTrigger(
            name = "req",
            methods = { HttpMethod.POST },
            authLevel = AuthorizationLevel.ANONYMOUS,
            route = "bodegas/{bodegaId}/productos"
        ) HttpRequestMessage<Optional<String>> request,
        @BindingName("bodegaId") String bodegaId,
        final ExecutionContext context
    ) {
        Logger logger = context.getLogger();
        logger.info("Asignando producto a bodega ID: " + bodegaId);

        try {
            int bId = Integer.parseInt(bodegaId);
            String requestBody = request.getBody().orElse("{}");
            
            @SuppressWarnings("unchecked")
            Map<String, Object> asignacionData = objectMapper.readValue(requestBody, Map.class);
            
            int productoId = ((Number) asignacionData.get("productoId")).intValue();
            int cantidad = ((Number) asignacionData.get("cantidad")).intValue();

            // Verificar que el producto y bodega existen
            if (!existeProducto(productoId, logger)) {
                return request
                    .createResponseBuilder(HttpStatus.NOT_FOUND)
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"Producto no encontrado\"}")
                    .build();
            }

            if (!existeBodega(bId, logger)) {
                return request
                    .createResponseBuilder(HttpStatus.NOT_FOUND)
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"Bodega no encontrada\"}")
                    .build();
            }

            // Asignar o actualizar inventario
            asignarProductoBodega(productoId, bId, cantidad, logger);

            return request
                .createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(
                    "{\"mensaje\": \"Producto asignado a bodega exitosamente\", " +
                    "\"productoId\": " + productoId + ", " +
                    "\"bodegaId\": " + bId + ", " +
                    "\"cantidad\": " + cantidad + "}"
                )
                .build();

        } catch (NumberFormatException e) {
            logger.warning("ID inválido: " + e.getMessage());
            return request
                .createResponseBuilder(HttpStatus.BAD_REQUEST)
                .header("Content-Type", "application/json")
                .body("{\"error\": \"IDs deben ser números válidos\"}")
                .build();
        } catch (Exception e) {
            logger.severe("Error asignando producto a bodega: " + e.getMessage());
            return request
                .createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body(
                    "{\"error\": \"Error asignando producto a bodega\", \"detalle\": \"" +
                    e.getMessage() + "\"}"
                )
                .build();
        }
    }

    @FunctionName("RemoverProductoDeBodega")
    public HttpResponseMessage removerProductoDeBodega(
        @HttpTrigger(
            name = "req",
            methods = { HttpMethod.DELETE },
            authLevel = AuthorizationLevel.ANONYMOUS,
            route = "bodegas/{bodegaId}/productos/{productoId}"
        ) HttpRequestMessage<Optional<String>> request,
        @BindingName("bodegaId") String bodegaId,
        @BindingName("productoId") String productoId,
        final ExecutionContext context
    ) {
        Logger logger = context.getLogger();
        logger.info("Removiendo producto " + productoId + " de bodega " + bodegaId);

        try {
            int bId = Integer.parseInt(bodegaId);
            int pId = Integer.parseInt(productoId);

            boolean removido = removerProductoBodega(pId, bId, logger);

            if (removido) {
                return request
                    .createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(
                        "{\"mensaje\": \"Producto removido de bodega exitosamente\", " +
                        "\"productoId\": " + pId + ", " +
                        "\"bodegaId\": " + bId + "}"
                    )
                    .build();
            } else {
                return request
                    .createResponseBuilder(HttpStatus.NOT_FOUND)
                    .header("Content-Type", "application/json")
                    .body("{\"error\": \"Producto no encontrado en esta bodega\"}")
                    .build();
            }

        } catch (NumberFormatException e) {
            logger.warning("ID inválido: " + e.getMessage());
            return request
                .createResponseBuilder(HttpStatus.BAD_REQUEST)
                .header("Content-Type", "application/json")
                .body("{\"error\": \"IDs deben ser números válidos\"}")
                .build();
        } catch (Exception e) {
            logger.severe("Error removiendo producto de bodega: " + e.getMessage());
            return request
                .createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body(
                    "{\"error\": \"Error removiendo producto de bodega\", \"detalle\": \"" +
                    e.getMessage() + "\"}"
                )
                .build();
        }
    }

    // ======================== MÉTODOS AUXILIARES PARA BODEGAS ========================

    private List<Bodega> getAllBodegas(Logger logger) throws SQLException {
        String sql =
            "SELECT ID, Nombre, Ubicacion, Capacidad, FechaCreacion FROM BODEGA ORDER BY Nombre";
        List<Bodega> bodegas = new ArrayList<>();

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()
        ) {
            while (rs.next()) {
                Bodega bodega = new Bodega();
                bodega.setId(rs.getInt("ID"));
                bodega.setNombre(rs.getString("Nombre"));
                bodega.setUbicacion(rs.getString("Ubicacion"));
                bodega.setCapacidad(rs.getInt("Capacidad"));
                if (rs.getTimestamp("FechaCreacion") != null) {
                    bodega.setFechaCreacion(
                        rs.getTimestamp("FechaCreacion").toLocalDateTime()
                    );
                }
                bodegas.add(bodega);
            }

            logger.info("Se obtuvieron " + bodegas.size() + " bodegas");
            return bodegas;
        } catch (SQLException e) {
            logger.severe(
                "Error en la consulta de bodegas: " + e.getMessage()
            );
            throw e;
        }
    }

    private Bodega getBodegaById(Integer id, Logger logger)
        throws SQLException {
        String sql =
            "SELECT ID, Nombre, Ubicacion, Capacidad, FechaCreacion FROM BODEGA WHERE ID = ?";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, id);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Bodega bodega = new Bodega();
                    bodega.setId(rs.getInt("ID"));
                    bodega.setNombre(rs.getString("Nombre"));
                    bodega.setUbicacion(rs.getString("Ubicacion"));
                    bodega.setCapacidad(rs.getInt("Capacidad"));
                    if (rs.getTimestamp("FechaCreacion") != null) {
                        bodega.setFechaCreacion(
                            rs.getTimestamp("FechaCreacion").toLocalDateTime()
                        );
                    }
                    logger.info("Bodega encontrada: " + bodega.getNombre());
                    return bodega;
                }
            }
        } catch (SQLException e) {
            logger.severe("Error buscando bodega por ID: " + e.getMessage());
            throw e;
        }

        return null;
    }

    private Integer insertBodega(Bodega bodega, Logger logger)
        throws SQLException {
        String sql =
            "INSERT INTO BODEGA (Nombre, Ubicacion, Capacidad) VALUES (?, ?, ?) RETURNING ID";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, bodega.getNombre());
            pstmt.setString(2, bodega.getUbicacion());
            pstmt.setInt(
                3,
                bodega.getCapacidad() != null
                    ? bodega.getCapacidad()
                    : DEFAULT_CAPACITY
            );

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Integer id = rs.getInt("ID");
                    logger.info(
                        "Bodega creada con ID: " +
                        id +
                        " - " +
                        bodega.getNombre()
                    );
                    return id;
                }
            }
        } catch (SQLException e) {
            logger.severe("Error insertando bodega: " + e.getMessage());
            throw e;
        }

        throw new SQLException("No se pudo crear la bodega");
    }

    private boolean updateBodega(Bodega bodega, Logger logger)
        throws SQLException {
        String sql =
            "UPDATE BODEGA SET Nombre = ?, Ubicacion = ?, Capacidad = ? WHERE ID = ?";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setString(1, bodega.getNombre());
            pstmt.setString(2, bodega.getUbicacion());
            pstmt.setInt(
                3,
                bodega.getCapacidad() != null
                    ? bodega.getCapacidad()
                    : DEFAULT_CAPACITY
            );
            pstmt.setInt(4, bodega.getId());

            int rowsAffected = pstmt.executeUpdate();
            boolean updated = rowsAffected > 0;

            if (updated) {
                logger.info(
                    "Bodega actualizada: ID " +
                    bodega.getId() +
                    " - " +
                    bodega.getNombre()
                );
            } else {
                logger.warning(
                    "No se encontró bodega con ID: " + bodega.getId()
                );
            }

            return updated;
        } catch (SQLException e) {
            logger.severe("Error actualizando bodega: " + e.getMessage());
            throw e;
        }
    }

    private boolean deleteBodega(Integer id, Logger logger)
        throws SQLException {
        String sql = "DELETE FROM BODEGA WHERE ID = ?";

        try (
            Connection conn = DatabaseManager.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)
        ) {
            pstmt.setInt(1, id);

            int rowsAffected = pstmt.executeUpdate();
            boolean deleted = rowsAffected > 0;

            if (deleted) {
                logger.info("Bodega eliminada: ID " + id);
            } else {
                logger.warning("No se encontró bodega con ID: " + id);
            }

            return deleted;
        } catch (SQLException e) {
            logger.severe("Error eliminando bodega: " + e.getMessage());
            throw e;
        }
    }

    /**
     * GraphQL Endpoint - Maneja consultas y mutaciones GraphQL
     * Alternativa moderna a los endpoints REST tradicionales
     */
    @FunctionName("GraphQL")
    public HttpResponseMessage graphql(
        @HttpTrigger(
            name = "req",
            methods = { HttpMethod.POST },
            authLevel = AuthorizationLevel.ANONYMOUS,
            route = "graphql"
        ) HttpRequestMessage<Optional<String>> request,
        final ExecutionContext context
    ) {
        Logger logger = context.getLogger();
        logger.info("Ejecutando consulta GraphQL...");

        try {
            ensureDatabaseInitialized();
            
            String requestBody = request.getBody().orElse("");
            if (requestBody.isEmpty()) {
                return request
                    .createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body("{\"errors\": [{\"message\": \"Cuerpo de la petición requerido\"}]}")
                    .build();
            }

            // Parsear el request GraphQL
            GraphQLRequest graphQLRequest;
            try {
                graphQLRequest = objectMapper.readValue(requestBody, GraphQLRequest.class);
            } catch (JsonProcessingException e) {
                logger.warning("Error parsing GraphQL request JSON: " + e.getMessage());
                return request
                    .createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body("{\"errors\": [{\"message\": \"JSON inválido\"}]}")
                    .build();
            }

            if (graphQLRequest.getQuery() == null || graphQLRequest.getQuery().trim().isEmpty()) {
                return request
                    .createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body("{\"errors\": [{\"message\": \"Query GraphQL requerida\"}]}")
                    .build();
            }

            // Ejecutar la consulta GraphQL
            graphql.ExecutionResult result = GraphQLSchemaBuilder.executeQuery(
                graphQLRequest.getQuery(),
                graphQLRequest.getVariables(),
                graphQLRequest.getOperationName()
            );

            // Convertir resultado a JSON
            String responseJson = objectMapper.writeValueAsString(result.toSpecification());

            return request
                .createResponseBuilder(HttpStatus.OK)
                .header("Content-Type", "application/json")
                .body(responseJson)
                .build();

        } catch (Exception e) {
            logger.severe("Error ejecutando consulta GraphQL: " + e.getMessage());
            return request
                .createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                .header("Content-Type", "application/json")
                .body("{\"errors\": [{\"message\": \"Error interno del servidor\"}]}")
                .build();
        }
    }

    /**
     * Clase interna para parsear requests GraphQL
     */
    public static class GraphQLRequest {
        private String query;
        private Map<String, Object> variables;
        private String operationName;

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public Map<String, Object> getVariables() {
            return variables;
        }

        public void setVariables(Map<String, Object> variables) {
            this.variables = variables;
        }

        public String getOperationName() {
            return operationName;
        }

        public void setOperationName(String operationName) {
            this.operationName = operationName;
        }
    }
    // ======================== MÉTODOS AUXILIARES PARA INVENTARIO ========================

    private List<Map<String, Object>> getProductosEnBodega(int bodegaId, Logger logger) throws SQLException {
        String sql = "SELECT p.ID as ProductoId, p.Nombre, p.Descripcion, p.Precio, " +
                     "i.Cantidad, p.FechaCreacion, p.FechaActualizacion " +
                     "FROM INVENTARIO i " +
                     "INNER JOIN PRODUCTO p ON i.IDProducto = p.ID " +
                     "WHERE i.IDBodega = ? " +
                     "ORDER BY p.Nombre";
        
        List<Map<String, Object>> productos = new ArrayList<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, bodegaId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> producto = new HashMap<>();
                    producto.put("id", rs.getInt("ProductoId"));
                    producto.put("nombre", rs.getString("Nombre"));
                    producto.put("descripcion", rs.getString("Descripcion"));
                    producto.put("precio", rs.getDouble("Precio"));
                    producto.put("cantidad", rs.getInt("Cantidad"));
                    producto.put("fechaCreacion", rs.getString("FechaCreacion"));
                    producto.put("fechaActualizacion", rs.getString("FechaActualizacion"));
                    productos.add(producto);
                }
            }
        }
        
        return productos;
    }

    private boolean existeProducto(int productoId, Logger logger) throws SQLException {
        String sql = "SELECT 1 FROM PRODUCTO WHERE ID = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productoId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean existeBodega(int bodegaId, Logger logger) throws SQLException {
        String sql = "SELECT 1 FROM BODEGA WHERE ID = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, bodegaId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void asignarProductoBodega(int productoId, int bodegaId, int cantidad, Logger logger) throws SQLException {
        String checkSql = "SELECT Cantidad FROM INVENTARIO WHERE IDProducto = ? AND IDBodega = ?";
        String updateSql = "UPDATE INVENTARIO SET Cantidad = ?, FechaActualizacion = CURRENT_TIMESTAMP WHERE IDProducto = ? AND IDBodega = ?";
        String insertSql = "INSERT INTO INVENTARIO (IDProducto, IDBodega, Cantidad) VALUES (?, ?, ?)";
        
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setInt(1, productoId);
                checkStmt.setInt(2, bodegaId);
                
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setInt(1, cantidad);
                            updateStmt.setInt(2, productoId);
                            updateStmt.setInt(3, bodegaId);
                            updateStmt.executeUpdate();
                            logger.info("Actualizada cantidad de producto " + productoId + " en bodega " + bodegaId);
                        }
                    } else {
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                            insertStmt.setInt(1, productoId);
                            insertStmt.setInt(2, bodegaId);
                            insertStmt.setInt(3, cantidad);
                            insertStmt.executeUpdate();
                            logger.info("Asignado producto " + productoId + " a bodega " + bodegaId);
                        }
                    }
                }
            }
        }
    }

    private boolean removerProductoBodega(int productoId, int bodegaId, Logger logger) throws SQLException {
        String sql = "DELETE FROM INVENTARIO WHERE IDProducto = ? AND IDBodega = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, productoId);
            pstmt.setInt(2, bodegaId);
            
            int rowsAffected = pstmt.executeUpdate();
            logger.info("Removido producto " + productoId + " de bodega " + bodegaId + ". Filas afectadas: " + rowsAffected);
            
            return rowsAffected > 0;
        }
    }
}