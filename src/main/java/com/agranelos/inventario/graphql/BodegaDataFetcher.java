package com.agranelos.inventario.graphql;

import com.agranelos.inventario.db.DatabaseManager;
import com.agranelos.inventario.model.Bodega;
import graphql.schema.DataFetcher;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Data Fetchers para operaciones GraphQL de Bodegas
 */
public class BodegaDataFetcher {
    
    private static final Logger logger = Logger.getLogger(BodegaDataFetcher.class.getName());
    private static final int DEFAULT_CAPACITY = 10000; // Capacidad por defecto
    
    /**
     * Query: bodegas - Obtener todas las bodegas
     */
    public static DataFetcher<List<Bodega>> getBodegas() {
        return dataFetchingEnvironment -> {
            List<Bodega> bodegas = new ArrayList<>();
            
            try (Connection connection = DatabaseManager.getConnection()) {
                String sql = "SELECT ID, Nombre, Ubicacion, Capacidad, FechaCreacion, FechaActualizacion FROM BODEGA ORDER BY ID";
                
                try (PreparedStatement statement = connection.prepareStatement(sql);
                     ResultSet resultSet = statement.executeQuery()) {
                    
                    while (resultSet.next()) {
                        Bodega bodega = new Bodega();
                        bodega.setId(resultSet.getInt("ID"));
                        bodega.setNombre(resultSet.getString("Nombre"));
                        bodega.setUbicacion(resultSet.getString("Ubicacion"));
                        bodega.setCapacidad(resultSet.getInt("Capacidad"));
                        
                        Timestamp fechaCreacion = resultSet.getTimestamp("FechaCreacion");
                        if (fechaCreacion != null) {
                            bodega.setFechaCreacion(fechaCreacion.toLocalDateTime());
                        }
                        
                        Timestamp fechaActualizacion = resultSet.getTimestamp("FechaActualizacion");
                        if (fechaActualizacion != null) {
                            bodega.setFechaActualizacion(fechaActualizacion.toLocalDateTime());
                        }
                        
                        bodegas.add(bodega);
                    }
                }
            } catch (SQLException e) {
                logger.severe("Error obteniendo bodegas: " + e.getMessage());
                throw new RuntimeException("Error obteniendo bodegas", e);
            }
            
            return bodegas;
        };
    }
    
    /**
     * Query: bodega(id) - Obtener bodega por ID
     */
    public static DataFetcher<Bodega> getBodega() {
        return dataFetchingEnvironment -> {
            String idString = dataFetchingEnvironment.getArgument("id");
            int id = Integer.parseInt(idString);
            
            try (Connection connection = DatabaseManager.getConnection()) {
                String sql = "SELECT ID, Nombre, Ubicacion, Capacidad, FechaCreacion, FechaActualizacion FROM BODEGA WHERE ID = ?";
                
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, id);
                    
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            Bodega bodega = new Bodega();
                            bodega.setId(resultSet.getInt("ID"));
                            bodega.setNombre(resultSet.getString("Nombre"));
                            bodega.setUbicacion(resultSet.getString("Ubicacion"));
                            bodega.setCapacidad(resultSet.getInt("Capacidad"));
                            
                            Timestamp fechaCreacion = resultSet.getTimestamp("FechaCreacion");
                            if (fechaCreacion != null) {
                                bodega.setFechaCreacion(fechaCreacion.toLocalDateTime());
                            }
                            
                            Timestamp fechaActualizacion = resultSet.getTimestamp("FechaActualizacion");
                            if (fechaActualizacion != null) {
                                bodega.setFechaActualizacion(fechaActualizacion.toLocalDateTime());
                            }
                            
                            return bodega;
                        }
                    }
                }
            } catch (SQLException e) {
                logger.severe("Error obteniendo bodega por ID: " + e.getMessage());
                throw new RuntimeException("Error obteniendo bodega", e);
            }
            
            return null;
        };
    }
    
    /**
     * Mutation: crearBodega - Crear nueva bodega
     */
    public static DataFetcher<Map<String, Object>> crearBodega() {
        return dataFetchingEnvironment -> {
            Map<String, Object> input = dataFetchingEnvironment.getArgument("input");
            Map<String, Object> response = new HashMap<>();
            
            try {
                // Validaciones básicas con sanitización
                String nombre = Optional.ofNullable(input.get("nombre"))
                    .map(Object::toString)
                    .map(String::trim)
                    .orElse(null);
                if (nombre == null || nombre.isEmpty()) {
                    response.put("success", false);
                    response.put("message", "Validación fallida: nombre requerido");
                    response.put("error", "El nombre de la bodega es requerido");
                    response.put("bodega", null);
                    return response;
                }
                
                String ubicacion = Optional.ofNullable(input.get("ubicacion"))
                    .map(Object::toString)
                    .map(String::trim)
                    .orElse(null);
                if (ubicacion == null || ubicacion.isEmpty()) {
                    response.put("success", false);
                    response.put("message", "Validación fallida: ubicación requerida");
                    response.put("error", "La ubicación de la bodega es requerida");
                    response.put("bodega", null);
                    return response;
                }
                
                // Validar capacidad defensivamente
                Integer capacidad = DEFAULT_CAPACITY;
                if (input.get("capacidad") != null) {
                    try {
                        Object capacidadObj = input.get("capacidad");
                        if (capacidadObj instanceof Number) {
                            capacidad = ((Number) capacidadObj).intValue();
                        } else {
                            capacidad = Integer.parseInt(capacidadObj.toString().trim());
                        }
                        
                        if (capacidad < 0) {
                            response.put("success", false);
                            response.put("message", "Validación fallida: capacidad negativa");
                            response.put("error", "La capacidad no puede ser negativa");
                            response.put("bodega", null);
                            return response;
                        }
                    } catch (NumberFormatException e) {
                        logger.warning("Capacidad inválida, usando valor por defecto: " + e.getMessage());
                        capacidad = DEFAULT_CAPACITY;
                    }
                }
                
                // Crear bodega
                Bodega bodega = new Bodega();
                bodega.setNombre(nombre);
                bodega.setUbicacion(ubicacion);
                bodega.setCapacidad(capacidad);
                
                // Insertar en base de datos
                Integer bodegaId = insertBodega(bodega);
                bodega.setId(bodegaId);
                
                response.put("success", true);
                response.put("message", "Bodega creada exitosamente");
                response.put("bodega", bodega);
                response.put("error", null);
                
            } catch (Exception e) {
                logger.severe("Error creando bodega: " + e.getMessage());
                e.printStackTrace(); // Log full stack trace
                response.put("success", false);
                response.put("message", "Error interno del servidor");
                response.put("error", "Error creando bodega: " + e.getMessage());
                response.put("bodega", null);
            }
            
            return response;
        };
    }
    
    /**
     * Mutation: actualizarBodega - Actualizar bodega existente
     */
    public static DataFetcher<Map<String, Object>> actualizarBodega() {
        return dataFetchingEnvironment -> {
            Map<String, Object> input = dataFetchingEnvironment.getArgument("input");
            Map<String, Object> response = new HashMap<>();
            
            try {
                String idString = (String) input.get("id");
                int id = Integer.parseInt(idString);
                
                // Crear bodega con los datos a actualizar
                Bodega bodega = new Bodega();
                bodega.setId(id);
                bodega.setNombre((String) input.get("nombre"));
                bodega.setUbicacion((String) input.get("ubicacion"));
                
                if (input.get("capacidad") != null) {
                    bodega.setCapacidad((Integer) input.get("capacidad"));
                }
                
                // Actualizar en base de datos
                boolean updated = updateBodega(bodega);
                
                if (updated) {
                    response.put("success", true);
                    response.put("message", "Bodega actualizada exitosamente");
                    response.put("bodega", bodega);
                    response.put("error", null);
                } else {
                    response.put("success", false);
                    response.put("message", "");
                    response.put("error", "Bodega no encontrada");
                    response.put("bodega", null);
                }
                
            } catch (Exception e) {
                logger.severe("Error actualizando bodega: " + e.getMessage());
                response.put("success", false);
                response.put("message", "");
                response.put("error", "Error actualizando bodega: " + e.getMessage());
                response.put("bodega", null);
            }
            
            return response;
        };
    }
    
    /**
     * Mutation: eliminarBodega - Eliminar bodega
     */
    public static DataFetcher<Map<String, Object>> eliminarBodega() {
        return dataFetchingEnvironment -> {
            String idString = dataFetchingEnvironment.getArgument("id");
            Map<String, Object> response = new HashMap<>();
            
            try {
                int id = Integer.parseInt(idString);
                boolean deleted = deleteBodega(id);
                
                if (deleted) {
                    response.put("success", true);
                    response.put("message", "Bodega eliminada exitosamente");
                    response.put("error", null);
                } else {
                    response.put("success", false);
                    response.put("message", "");
                    response.put("error", "Bodega no encontrada");
                }
                
            } catch (Exception e) {
                logger.severe("Error eliminando bodega: " + e.getMessage());
                response.put("success", false);
                response.put("message", "");
                response.put("error", "Error eliminando bodega: " + e.getMessage());
            }
            
            return response;
        };
    }
    
    // Métodos auxiliares de base de datos
    private static Integer insertBodega(Bodega bodega) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection()) {
            String sql = "INSERT INTO BODEGA (Nombre, Ubicacion, Capacidad, FechaCreacion, FechaActualizacion) VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, bodega.getNombre());
                statement.setString(2, bodega.getUbicacion());
                statement.setInt(3, bodega.getCapacidad());
                
                int affectedRows = statement.executeUpdate();
                if (affectedRows == 0) {
                    throw new SQLException("Falló la creación de la bodega, no se afectaron filas");
                }
                
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    } else {
                        throw new SQLException("Falló la creación de la bodega, no se obtuvo ID");
                    }
                }
            }
        }
    }
    
    private static boolean updateBodega(Bodega bodega) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection()) {
            StringBuilder sql = new StringBuilder("UPDATE BODEGA SET FechaActualizacion = CURRENT_TIMESTAMP");
            List<Object> params = new ArrayList<>();
            
            if (bodega.getNombre() != null) {
                sql.append(", Nombre = ?");
                params.add(bodega.getNombre());
            }
            if (bodega.getUbicacion() != null) {
                sql.append(", Ubicacion = ?");
                params.add(bodega.getUbicacion());
            }
            if (bodega.getCapacidad() != null) {
                sql.append(", Capacidad = ?");
                params.add(bodega.getCapacidad());
            }
            
            sql.append(" WHERE ID = ?");
            params.add(bodega.getId());
            
            try (PreparedStatement statement = connection.prepareStatement(sql.toString())) {
                for (int i = 0; i < params.size(); i++) {
                    statement.setObject(i + 1, params.get(i));
                }
                
                return statement.executeUpdate() > 0;
            }
        }
    }
    
    private static boolean deleteBodega(int id) throws SQLException {
        try (Connection connection = DatabaseManager.getConnection()) {
            String sql = "DELETE FROM BODEGA WHERE ID = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                return statement.executeUpdate() > 0;
            }
        }
    }
}