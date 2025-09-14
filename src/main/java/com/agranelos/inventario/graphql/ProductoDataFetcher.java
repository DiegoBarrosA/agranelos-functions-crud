package com.agranelos.inventario.graphql;

import com.agranelos.inventario.db.DatabaseManager;
import com.agranelos.inventario.model.Producto;
import graphql.schema.DataFetcher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Data Fetchers para operaciones GraphQL de Productos
 */
public class ProductoDataFetcher {
    
    private static final Logger logger = Logger.getLogger(ProductoDataFetcher.class.getName());
    
    /**
     * Query: productos - Obtener todos los productos
     */
    public static DataFetcher<List<Producto>> getProductos() {
        return dataFetchingEnvironment -> {
            List<Producto> productos = new ArrayList<>();
            
            try (Connection connection = DatabaseManager.getConnection()) {
                String sql = "SELECT id, nombre, descripcion, precio, cantidad, fecha_creacion, fecha_actualizacion FROM PRODUCTO ORDER BY id";
                
                try (PreparedStatement statement = connection.prepareStatement(sql);
                     ResultSet resultSet = statement.executeQuery()) {
                    
                    while (resultSet.next()) {
                        Producto producto = new Producto();
                        producto.setId(resultSet.getInt("id"));
                        producto.setNombre(resultSet.getString("nombre"));
                        producto.setDescripcion(resultSet.getString("descripcion"));
                        producto.setPrecio(resultSet.getBigDecimal("precio"));
                        producto.setCantidadEnStock(resultSet.getInt("cantidad"));
                        
                        Timestamp fechaCreacion = resultSet.getTimestamp("fecha_creacion");
                        if (fechaCreacion != null) {
                            producto.setFechaCreacion(fechaCreacion.toLocalDateTime());
                        }
                        
                        Timestamp fechaActualizacion = resultSet.getTimestamp("fecha_actualizacion");
                        if (fechaActualizacion != null) {
                            producto.setFechaActualizacion(fechaActualizacion.toLocalDateTime());
                        }
                        productos.add(producto);
                    }
                }
            } catch (SQLException e) {
                logger.severe("Error obteniendo productos: " + e.getMessage());
                throw new RuntimeException("Error obteniendo productos", e);
            }
            
            return productos;
        };
    }
    
    /**
     * Query: producto(id) - Obtener producto por ID
     */
    public static DataFetcher<Producto> getProducto() {
        return dataFetchingEnvironment -> {
            String idString = dataFetchingEnvironment.getArgument("id");
            int id = Integer.parseInt(idString);
            
            try (Connection connection = DatabaseManager.getConnection()) {
                String sql = "SELECT id, nombre, descripcion, precio, cantidad, fecha_creacion, fecha_actualizacion FROM PRODUCTO WHERE id = ?";
                
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, id);
                    
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            Producto producto = new Producto();
                            producto.setId(resultSet.getInt("id"));
                            producto.setNombre(resultSet.getString("nombre"));
                            producto.setDescripcion(resultSet.getString("descripcion"));
                            producto.setPrecio(resultSet.getBigDecimal("precio"));
                            producto.setCantidadEnStock(resultSet.getInt("cantidad"));
                            
                            Timestamp fechaCreacion = resultSet.getTimestamp("fecha_creacion");
                            if (fechaCreacion != null) {
                                producto.setFechaCreacion(fechaCreacion.toLocalDateTime());
                            }
                            
                            Timestamp fechaActualizacion = resultSet.getTimestamp("fecha_actualizacion");
                            if (fechaActualizacion != null) {
                                producto.setFechaActualizacion(fechaActualizacion.toLocalDateTime());
                            }
                            return producto;
                        }
                    }
                }
            } catch (SQLException e) {
                logger.severe("Error obteniendo producto por ID: " + e.getMessage());
                throw new RuntimeException("Error obteniendo producto", e);
            }
            
            return null;
        };
    }
    
    /**
     * Mutation: crearProducto - Crear nuevo producto
     */
    public static DataFetcher<Map<String, Object>> crearProducto() {
        return dataFetchingEnvironment -> {
            Map<String, Object> input = dataFetchingEnvironment.getArgument("input");
            Map<String, Object> response = new HashMap<>();
            
            try {
                // Validaciones básicas
                String nombre = (String) input.get("nombre");
                if (nombre == null || nombre.trim().isEmpty()) {
                    response.put("success", false);
                    response.put("message", "");
                    response.put("error", "El nombre del producto es requerido");
                    return response;
                }
                
                // Crear producto
                Producto producto = new Producto();
                producto.setNombre(nombre);
                producto.setDescripcion((String) input.get("descripcion"));
                producto.setPrecio(new BigDecimal(input.get("precio").toString()));
                producto.setCantidadEnStock((Integer) input.get("cantidad"));
                
                // Insertar en base de datos
                Integer productoId = insertProducto(producto);
                producto.setId(productoId);
                
                response.put("success", true);
                response.put("message", "Producto creado exitosamente");
                response.put("producto", producto);
                response.put("error", null);
                
            } catch (Exception e) {
                logger.severe("Error creando producto: " + e.getMessage());
                response.put("success", false);
                response.put("message", "");
                response.put("error", "Error creando producto: " + e.getMessage());
                response.put("producto", null);
            }
            
            return response;
        };
    }
    
    /**
     * Mutation: actualizarProducto - Actualizar producto existente
     */
    public static DataFetcher<Map<String, Object>> actualizarProducto() {
        return dataFetchingEnvironment -> {
            Map<String, Object> input = dataFetchingEnvironment.getArgument("input");
            Map<String, Object> response = new HashMap<>();
            
            try {
                String idString = (String) input.get("id");
                int id = Integer.parseInt(idString);
                
                // Crear producto con los datos a actualizar
                Producto producto = new Producto();
                producto.setId(id);
                producto.setNombre((String) input.get("nombre"));
                producto.setDescripcion((String) input.get("descripcion"));
                
                if (input.get("precio") != null) {
                    producto.setPrecio(new BigDecimal(input.get("precio").toString()));
                }
                if (input.get("cantidad") != null) {
                    producto.setCantidadEnStock((Integer) input.get("cantidad"));
                }
                
                // Actualizar en base de datos
                boolean updated = updateProducto(producto);
                
                if (updated) {
                    response.put("success", true);
                    response.put("message", "Producto actualizado exitosamente");
                    response.put("producto", producto);
                    response.put("error", null);
                } else {
                    response.put("success", false);
                    response.put("message", "");
                    response.put("error", "Producto no encontrado");
                    response.put("producto", null);
                }
                
            } catch (Exception e) {
                logger.severe("Error actualizando producto: " + e.getMessage());
                response.put("success", false);
                response.put("message", "");
                response.put("error", "Error actualizando producto: " + e.getMessage());
                response.put("producto", null);
            }
            
            return response;
        };
    }
    
    /**
     * Mutation: eliminarProducto - Eliminar producto
     */
    public static DataFetcher<Map<String, Object>> eliminarProducto() {
        return dataFetchingEnvironment -> {
            String idString = dataFetchingEnvironment.getArgument("id");
            Map<String, Object> response = new HashMap<>();
            
            try {
                int id = Integer.parseInt(idString);
                boolean deleted = deleteProducto(id);
                
                if (deleted) {
                    response.put("success", true);
                    response.put("message", "Producto eliminado exitosamente");
                    response.put("error", null);
                } else {
                    response.put("success", false);
                    response.put("message", "");
                    response.put("error", "Producto no encontrado");
                }
                
            } catch (Exception e) {
                logger.severe("Error eliminando producto: " + e.getMessage());
                response.put("success", false);
                response.put("message", "");
                response.put("error", "Error eliminando producto: " + e.getMessage());
            }
            
            return response;
        };
    }
    
    // Métodos auxiliares de base de datos
    private static Integer insertProducto(Producto producto) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO PRODUCTO (nombre, descripcion, precio, cantidad, fecha_creacion, fecha_actualizacion) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, producto.getNombre());
                statement.setString(2, producto.getDescripcion());
                statement.setBigDecimal(3, producto.getPrecio());
                statement.setInt(4, producto.getCantidadEnStock());
                
                int affectedRows = statement.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Falló la creación del producto, no se afectaron filas");
                }
                
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Falló la creación del producto, no se obtuvo ID");
                    }
                }
            }
        }
    }
    
    private static boolean updateProducto(Producto producto) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection()) {
            StringBuilder sql = new StringBuilder("UPDATE PRODUCTO SET fecha_actualizacion = CURRENT_TIMESTAMP");
            List<Object> params = new ArrayList<>();
            
            if (producto.getNombre() != null) {
                sql.append(", nombre = ?");
                params.add(producto.getNombre());
            }
            if (producto.getDescripcion() != null) {
                sql.append(", descripcion = ?");
                params.add(producto.getDescripcion());
            }
            if (producto.getPrecio() != null) {
                sql.append(", precio = ?");
                params.add(producto.getPrecio());
            }
            if (producto.getCantidadEnStock() != null) {
                sql.append(", cantidad = ?");
                params.add(producto.getCantidadEnStock());
            }
            
            sql.append(" WHERE id = ?");
            params.add(producto.getId());
            
            try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    statement.setObject(i + 1, params.get(i));
                }
                
                return statement.executeUpdate() > 0;
            }
        }
    }
    
    private static boolean deleteProducto(int id) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection()) {
            String sql = "DELETE FROM PRODUCTO WHERE id = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                return statement.executeUpdate() > 0;
            }
        }
    }
}