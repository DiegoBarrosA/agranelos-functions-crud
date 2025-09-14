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
                String sql = "SELECT id, nombre, ubicacion, capacidad, fecha_creacion, fecha_actualizacion FROM BODEGA ORDER BY id";
                
                try (PreparedStatement statement = connection.prepareStatement(sql);
                     ResultSet resultSet = statement.executeQuery()) {
                    
                    while (resultSet.next()) {
                        Bodega bodega = new Bodega();
                        bodega.setId(resultSet.getInt("id"));
                        bodega.setNombre(resultSet.getString("nombre"));
                        bodega.setUbicacion(resultSet.getString("ubicacion"));
                        bodega.setCapacidad(resultSet.getInt("capacidad"));
                        
                        Timestamp fechaCreacion = resultSet.getTimestamp("fecha_creacion");
                        if (fechaCreacion != null) {
                            bodega.setFechaCreacion(fechaCreacion.toLocalDateTime());
                        }
                        
                        // Nota: Bodega no tiene fechaActualizacion en el modelo actual
                        
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
                String sql = "SELECT id, nombre, ubicacion, capacidad, fecha_creacion, fecha_actualizacion FROM BODEGA WHERE id = ?";
                
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, id);
                    
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (resultSet.next()) {
                            Bodega bodega = new Bodega();
                            bodega.setId(resultSet.getInt("id"));
                            bodega.setNombre(resultSet.getString("nombre"));
                            bodega.setUbicacion(resultSet.getString("ubicacion"));
                            bodega.setCapacidad(resultSet.getInt("capacidad"));
                            
                            Timestamp fechaCreacion = resultSet.getTimestamp("fecha_creacion");
                            if (fechaCreacion != null) {
                                bodega.setFechaCreacion(fechaCreacion.toLocalDateTime());
                            }
                            
                            // Nota: Bodega no tiene fechaActualizacion en el modelo actual
                            
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
                // Validaciones básicas
                String nombre = (String) input.get("nombre");
                if (nombre == null || nombre.trim().isEmpty()) {
                    response.put("success", false);
                    response.put("message", "");
                    response.put("error", "El nombre de la bodega es requerido");
                    return response;
                }
                
                String ubicacion = (String) input.get("ubicacion");
                if (ubicacion == null || ubicacion.trim().isEmpty()) {
                    response.put("success", false);
                    response.put("message", "");
                    response.put("error", "La ubicación de la bodega es requerida");
                    return response;
                }
                
                // Crear bodega
                Bodega bodega = new Bodega();
                bodega.setNombre(nombre);
                bodega.setUbicacion(ubicacion);
                
                // Capacidad opcional (usar default si no se proporciona)
                Integer capacidad = (Integer) input.get("capacidad");
                bodega.setCapacidad(capacidad != null ? capacidad : DEFAULT_CAPACITY);
                
                // Insertar en base de datos
                Integer bodegaId = insertBodega(bodega);
                bodega.setId(bodegaId);
                
                response.put("success", true);
                response.put("message", "Bodega creada exitosamente");
                response.put("bodega", bodega);
                response.put("error", null);
                
            } catch (Exception e) {
                logger.severe("Error creando bodega: " + e.getMessage());
                response.put("success", false);
                response.put("message", "");
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
            String sql = "INSERT INTO BODEGA (nombre, ubicacion, capacidad, fecha_creacion, fecha_actualizacion) VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
            
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
            StringBuilder sql = new StringBuilder("UPDATE BODEGA SET fecha_actualizacion = CURRENT_TIMESTAMP");
            List<Object> params = new ArrayList<>();
            
            if (bodega.getNombre() != null) {
                sql.append(", nombre = ?");
                params.add(bodega.getNombre());
            }
            if (bodega.getUbicacion() != null) {
                sql.append(", ubicacion = ?");
                params.add(bodega.getUbicacion());
            }
            if (bodega.getCapacidad() != null) {
                sql.append(", capacidad = ?");
                params.add(bodega.getCapacidad());
            }
            
            sql.append(" WHERE id = ?");
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
            String sql = "DELETE FROM BODEGA WHERE id = ?";
            
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, id);
                return statement.executeUpdate() > 0;
            }
        }
    }
}