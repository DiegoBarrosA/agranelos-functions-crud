package com.agranelos.inventario.events;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.agranelos.inventario.services.EmailService;
import com.agranelos.inventario.db.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.Map;

/**
 * Azure Functions que consumen eventos de Event Grid
 * Permite reaccionar a cambios en productos y bodegas
 */
public class EventGridConsumer {
    
    private static final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule());
    
    /**
     * Función que se dispara cuando se crea un producto
     * Útil para: notificaciones, auditoría, sincronización con otros sistemas
     */
    @FunctionName("ProductoCreadoEventHandler")
    public void handleProductoCreado(
        @EventGridTrigger(name = "event") String event,
        final ExecutionContext context
    ) {
        Logger logger = context.getLogger();
        logger.info("=== Evento ProductoCreado Recibido ===");
        
        try {
            // Parsear el evento
            EventGridEventSchema eventSchema = objectMapper.readValue(event, EventGridEventSchema.class);
            
            logger.info(String.format("Event Type: %s", eventSchema.getEventType()));
            logger.info(String.format("Subject: %s", eventSchema.getSubject()));
            logger.info(String.format("Data: %s", eventSchema.getData()));
            
            // Extraer información del producto
            Map<String, Object> data = (Map<String, Object>) eventSchema.getData();
            Long productoId = getLongFromData(data, "productoId");
            String nombre = (String) data.get("nombre");
            
            // 📧 Enviar notificación por email
            if (productoId != null && nombre != null) {
                EmailService.sendProductoCreatedEmail(productoId, nombre, logger);
            }
            
            // 🏭 FUNCIONALIDAD REQUERIDA: Asignar bodega por defecto automáticamente
            if (productoId != null) {
                try {
                    asignarBodegaPorDefecto(productoId, logger);
                    logger.info("Bodega por defecto asignada al producto ID: " + productoId);
                } catch (Exception e) {
                    logger.severe("Error asignando bodega por defecto al producto " + productoId + ": " + e.getMessage());
                }
            }
            
            logger.info("Evento procesado exitosamente");
            
        } catch (Exception e) {
            logger.severe("Error procesando evento ProductoCreado: " + e.getMessage());
        }
    }
    
    /**
     * Función que se dispara cuando se actualiza un producto
     */
    @FunctionName("ProductoActualizadoEventHandler")
    public void handleProductoActualizado(
        @EventGridTrigger(name = "event") String event,
        final ExecutionContext context
    ) {
        Logger logger = context.getLogger();
        logger.info("=== Evento ProductoActualizado Recibido ===");
        
        try {
            EventGridEventSchema eventSchema = objectMapper.readValue(event, EventGridEventSchema.class);
            
            logger.info(String.format("Event Type: %s", eventSchema.getEventType()));
            logger.info(String.format("Subject: %s", eventSchema.getSubject()));
            
            // Extraer información del producto
            Map<String, Object> data = (Map<String, Object>) eventSchema.getData();
            Long productoId = getLongFromData(data, "productoId");
            String nombre = (String) data.get("nombre");
            
            // 📧 Enviar notificación por email
            if (productoId != null && nombre != null) {
                EmailService.sendProductoUpdatedEmail(productoId, nombre, logger);
            }
            
            // Lógica de negocio para actualizaciones
            // - Verificar cambios significativos de precio
            // - Alertar sobre cambios de stock
            // - Actualizar reportes
            
            logger.info("Evento procesado exitosamente");
            
        } catch (Exception e) {
            logger.severe("Error procesando evento ProductoActualizado: " + e.getMessage());
        }
    }
    
    /**
     * Función que se dispara cuando se elimina un producto
     */
    @FunctionName("ProductoEliminadoEventHandler")
    public void handleProductoEliminado(
        @EventGridTrigger(name = "event") String event,
        final ExecutionContext context
    ) {
        Logger logger = context.getLogger();
        logger.info("=== Evento ProductoEliminado Recibido ===");
        
        try {
            EventGridEventSchema eventSchema = objectMapper.readValue(event, EventGridEventSchema.class);
            
            logger.info(String.format("Event Type: %s", eventSchema.getEventType()));
            logger.info(String.format("Subject: %s", eventSchema.getSubject()));
            
            // Extraer información del producto
            Map<String, Object> data = (Map<String, Object>) eventSchema.getData();
            Long productoId = getLongFromData(data, "productoId");
            
            // 📧 Enviar notificación por email
            if (productoId != null) {
                EmailService.sendProductoDeletedEmail(productoId, logger);
            }
            
            // Lógica de negocio para eliminaciones
            // - Limpiar cachés
            // - Archivar información
            // - Notificar a sistemas dependientes
            
            logger.info("Evento procesado exitosamente");
            
        } catch (Exception e) {
            logger.severe("Error procesando evento ProductoEliminado: " + e.getMessage());
        }
    }
    
    /**
     * Función que se dispara cuando se crea una bodega
     */
    @FunctionName("BodegaCreadaEventHandler")
    public void handleBodegaCreada(
        @EventGridTrigger(name = "event") String event,
        final ExecutionContext context
    ) {
        Logger logger = context.getLogger();
        logger.info("=== Evento BodegaCreada Recibido ===");
        
        try {
            EventGridEventSchema eventSchema = objectMapper.readValue(event, EventGridEventSchema.class);
            
            logger.info(String.format("Event Type: %s", eventSchema.getEventType()));
            logger.info(String.format("Subject: %s", eventSchema.getSubject()));
            
            // Extraer información de la bodega
            Map<String, Object> data = (Map<String, Object>) eventSchema.getData();
            Long bodegaId = getLongFromData(data, "bodegaId");
            String nombre = (String) data.get("nombre");
            
            // 📧 Enviar notificación por email
            if (bodegaId != null && nombre != null) {
                EmailService.sendBodegaCreatedEmail(bodegaId, nombre, logger);
            }
            
            // Lógica de negocio para nuevas bodegas
            // - Inicializar inventario
            // - Configurar permisos
            // - Notificar al equipo de logística
            
            logger.info("Evento procesado exitosamente");
            
        } catch (Exception e) {
            logger.severe("Error procesando evento BodegaCreada: " + e.getMessage());
        }
    }
    
    /**
     * Función que se dispara cuando se actualiza una bodega
     */
    @FunctionName("BodegaActualizadaEventHandler")
    public void handleBodegaActualizada(
        @EventGridTrigger(name = "event") String event,
        final ExecutionContext context
    ) {
        Logger logger = context.getLogger();
        logger.info("=== Evento BodegaActualizada Recibido ===");
        
        try {
            EventGridEventSchema eventSchema = objectMapper.readValue(event, EventGridEventSchema.class);
            
            logger.info(String.format("Event Type: %s", eventSchema.getEventType()));
            logger.info(String.format("Subject: %s", eventSchema.getSubject()));
            
            // Extraer información de la bodega
            Map<String, Object> data = (Map<String, Object>) eventSchema.getData();
            Long bodegaId = getLongFromData(data, "bodegaId");
            String nombre = (String) data.get("nombre");
            
            // 📧 Enviar notificación por email
            if (bodegaId != null && nombre != null) {
                EmailService.sendBodegaUpdatedEmail(bodegaId, nombre, logger);
            }
            
            logger.info("Evento procesado exitosamente");
            
        } catch (Exception e) {
            logger.severe("Error procesando evento BodegaActualizada: " + e.getMessage());
        }
    }
    
    /**
     * Función que se dispara cuando se elimina una bodega
     */
    @FunctionName("BodegaEliminadaEventHandler")
    public void handleBodegaEliminada(
        @EventGridTrigger(name = "event") String event,
        final ExecutionContext context
    ) {
        Logger logger = context.getLogger();
        logger.info("=== Evento BodegaEliminada Recibido ===");
        
        try {
            EventGridEventSchema eventSchema = objectMapper.readValue(event, EventGridEventSchema.class);
            
            logger.info(String.format("Event Type: %s", eventSchema.getEventType()));
            logger.info(String.format("Subject: %s", eventSchema.getSubject()));
            
            // Extraer información de la bodega
            Map<String, Object> data = (Map<String, Object>) eventSchema.getData();
            Long bodegaId = getLongFromData(data, "bodegaId");
            
            // 📧 Enviar notificación por email
            if (bodegaId != null) {
                EmailService.sendBodegaDeletedEmail(bodegaId, logger);
            }
            
            // 🏭 FUNCIONALIDAD REQUERIDA: Reasignar productos de bodega eliminada
            if (bodegaId != null) {
                try {
                    reasignarProductosDeBodegaEliminada(bodegaId, logger);
                    logger.info("Productos reasignados exitosamente de bodega eliminada ID: " + bodegaId);
                } catch (Exception e) {
                    logger.severe("Error reasignando productos de bodega eliminada " + bodegaId + ": " + e.getMessage());
                }
            }
            
            logger.info("Evento procesado exitosamente");
            
        } catch (Exception e) {
            logger.severe("Error procesando evento BodegaEliminada: " + e.getMessage());
        }
    }
    
    /**
     * Método auxiliar para extraer valores Long de forma segura
     */
    private Long getLongFromData(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof Long) {
            return (Long) value;
        }
        return null;
    }
    
    /**
     * Clase interna para representar el esquema de eventos de Event Grid
     */
    public static class EventGridEventSchema {
        private String id;
        private String eventType;
        private String subject;
        private String eventTime;
        private Object data;
        private String dataVersion;
        private String metadataVersion;
        private String topic;
        
        // Getters y Setters
        public String getId() {
            return id;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public String getEventType() {
            return eventType;
        }
        
        public void setEventType(String eventType) {
            this.eventType = eventType;
        }
        
        public String getSubject() {
            return subject;
        }
        
        public void setSubject(String subject) {
            this.subject = subject;
        }
        
        public String getEventTime() {
            return eventTime;
        }
        
        public void setEventTime(String eventTime) {
            this.eventTime = eventTime;
        }
        
        public Object getData() {
            return data;
        }
        
        public void setData(Object data) {
            this.data = data;
        }
        
        public String getDataVersion() {
            return dataVersion;
        }
        
        public void setDataVersion(String dataVersion) {
            this.dataVersion = dataVersion;
        }
        
        public String getMetadataVersion() {
            return metadataVersion;
        }
        
        public void setMetadataVersion(String metadataVersion) {
            this.metadataVersion = metadataVersion;
        }
        
        public String getTopic() {
            return topic;
        }
        
        public void setTopic(String topic) {
            this.topic = topic;
        }
    }
    
    /**
     * FUNCIONALIDAD REQUERIDA: Asignar bodega por defecto cuando se crea un producto
     * Requisito: "Cuando se cree un producto, automáticamente se le deberá asignar una bodega por defecto"
     */
    private void asignarBodegaPorDefecto(Long productoId, Logger logger) throws SQLException {
        logger.info("Iniciando asignación de bodega por defecto para producto ID: " + productoId);
        
        try (Connection connection = DatabaseManager.getConnection()) {
            // 1. Buscar bodega por defecto (primera bodega disponible o la de mayor capacidad)
            Long bodegaDefecto = obtenerBodegaPorDefecto(connection, logger);
            
            if (bodegaDefecto == null) {
                logger.warning("No se encontró ninguna bodega disponible para asignar al producto " + productoId);
                return;
            }
            
            // 2. Verificar si ya existe entrada en INVENTARIO para este producto
            if (existeEnInventario(connection, productoId, bodegaDefecto)) {
                logger.info("El producto " + productoId + " ya tiene asignada la bodega " + bodegaDefecto);
                return;
            }
            
            // 3. Crear entrada en tabla INVENTARIO con cantidad inicial 0
            String insertSql = "INSERT INTO INVENTARIO (IDProducto, IDBodega, Cantidad, FechaActualizacion) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
            
            try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
                stmt.setLong(1, productoId);
                stmt.setLong(2, bodegaDefecto);
                stmt.setInt(3, 0); // Cantidad inicial en 0
                
                int rowsAffected = stmt.executeUpdate();
                
                if (rowsAffected > 0) {
                    logger.info(String.format(
                        "✅ Producto ID %d asignado exitosamente a bodega por defecto ID %d", 
                        productoId, bodegaDefecto
                    ));
                } else {
                    logger.warning("No se pudo insertar en INVENTARIO para producto " + productoId);
                }
            }
            
        } catch (SQLException e) {
            logger.severe("Error en base de datos al asignar bodega por defecto: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Obtiene la bodega por defecto (primera disponible o criterio de negocio)
     */
    private Long obtenerBodegaPorDefecto(Connection connection, Logger logger) throws SQLException {
        // Estrategia: Buscar la bodega con mayor capacidad disponible
        String selectSql = "SELECT ID FROM BODEGA ORDER BY Capacidad DESC, ID ASC LIMIT 1";
        
        try (PreparedStatement stmt = connection.prepareStatement(selectSql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                Long bodegaId = rs.getLong("ID");
                logger.info("Bodega por defecto seleccionada: ID " + bodegaId);
                return bodegaId;
            }
        }
        
        logger.warning("No se encontraron bodegas disponibles");
        return null;
    }
    
    /**
     * Verifica si ya existe una entrada en INVENTARIO para el producto y bodega dados
     */
    private boolean existeEnInventario(Connection connection, Long productoId, Long bodegaId) throws SQLException {
        String selectSql = "SELECT COUNT(*) as count FROM INVENTARIO WHERE IDProducto = ? AND IDBodega = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
            stmt.setLong(1, productoId);
            stmt.setLong(2, bodegaId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count") > 0;
                }
            }
        }
        
        return false;
    }
    
    /**
     * FUNCIONALIDAD REQUERIDA: Reasignar productos cuando se elimina una bodega
     * Requisito: "Cuando se elimine una bodega automáticamente se deberá actualizar los productos pertenecientes a esa bodega y se les deberá quitar"
     */
    private void reasignarProductosDeBodegaEliminada(Long bodegaEliminadaId, Logger logger) throws SQLException {
        logger.info("Iniciando reasignación de productos de bodega eliminada ID: " + bodegaEliminadaId);
        
        try (Connection connection = DatabaseManager.getConnection()) {
            // 1. Buscar productos que estaban en la bodega eliminada
            String selectProductosSql = "SELECT IDProducto, Cantidad FROM INVENTARIO WHERE IDBodega = ?";
            
            try (PreparedStatement selectStmt = connection.prepareStatement(selectProductosSql)) {
                selectStmt.setLong(1, bodegaEliminadaId);
                
                try (ResultSet rs = selectStmt.executeQuery()) {
                    int productosReasignados = 0;
                    
                    while (rs.next()) {
                        Long productoId = rs.getLong("IDProducto");
                        int cantidad = rs.getInt("Cantidad");
                        
                        // 2. Buscar bodega alternativa (excluyendo la eliminada)
                        Long bodegaAlternativa = obtenerBodegaAlternativa(connection, bodegaEliminadaId, logger);
                        
                        if (bodegaAlternativa != null) {
                            // 3. Reasignar producto a nueva bodega
                            reasignarProducto(connection, productoId, bodegaEliminadaId, bodegaAlternativa, cantidad, logger);
                            productosReasignados++;
                            
                            logger.info(String.format(
                                "Producto ID %d reasignado de bodega %d a bodega %d (cantidad: %d)", 
                                productoId, bodegaEliminadaId, bodegaAlternativa, cantidad
                            ));
                        } else {
                            // 4. Si no hay bodega alternativa, eliminar del inventario
                            eliminarDeInventario(connection, productoId, bodegaEliminadaId, logger);
                            
                            logger.info(String.format(
                                "Producto ID %d eliminado del inventario (no hay bodegas alternativas)", 
                                productoId
                            ));
                        }
                    }
                    
                    logger.info(String.format(
                        "✅ Reasignación completada: %d productos procesados de bodega eliminada ID %d", 
                        productosReasignados, bodegaEliminadaId
                    ));
                }
            }
            
        } catch (SQLException e) {
            logger.severe("Error en base de datos al reasignar productos: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Obtiene una bodega alternativa (excluyendo la bodega eliminada)
     */
    private Long obtenerBodegaAlternativa(Connection connection, Long bodegaEliminadaId, Logger logger) throws SQLException {
        String selectSql = "SELECT ID FROM BODEGA WHERE ID != ? ORDER BY Capacidad DESC, ID ASC LIMIT 1";
        
        try (PreparedStatement stmt = connection.prepareStatement(selectSql)) {
            stmt.setLong(1, bodegaEliminadaId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("ID");
                }
            }
        }
        
        return null; // No hay bodegas alternativas disponibles
    }
    
    /**
     * Reasigna un producto de una bodega a otra
     */
    private void reasignarProducto(Connection connection, Long productoId, Long bodegaOrigen, 
                                 Long bodegaDestino, int cantidad, Logger logger) throws SQLException {
        
        // Estrategia: UPDATE si ya existe entrada en bodega destino, INSERT si no existe
        String checkExistsSql = "SELECT Cantidad FROM INVENTARIO WHERE IDProducto = ? AND IDBodega = ?";
        
        try (PreparedStatement checkStmt = connection.prepareStatement(checkExistsSql)) {
            checkStmt.setLong(1, productoId);
            checkStmt.setLong(2, bodegaDestino);
            
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    // Ya existe, sumar las cantidades
                    int cantidadExistente = rs.getInt("Cantidad");
                    String updateSql = "UPDATE INVENTARIO SET Cantidad = ?, FechaActualizacion = CURRENT_TIMESTAMP WHERE IDProducto = ? AND IDBodega = ?";
                    
                    try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, cantidadExistente + cantidad);
                        updateStmt.setLong(2, productoId);
                        updateStmt.setLong(3, bodegaDestino);
                        updateStmt.executeUpdate();
                    }
                } else {
                    // No existe, crear nueva entrada
                    String insertSql = "INSERT INTO INVENTARIO (IDProducto, IDBodega, Cantidad, FechaActualizacion) VALUES (?, ?, ?, CURRENT_TIMESTAMP)";
                    
                    try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                        insertStmt.setLong(1, productoId);
                        insertStmt.setLong(2, bodegaDestino);
                        insertStmt.setInt(3, cantidad);
                        insertStmt.executeUpdate();
                    }
                }
            }
        }
        
        // Eliminar entrada de bodega origen
        eliminarDeInventario(connection, productoId, bodegaOrigen, logger);
    }
    
    /**
     * Elimina un producto del inventario de una bodega específica
     */
    private void eliminarDeInventario(Connection connection, Long productoId, Long bodegaId, Logger logger) throws SQLException {
        String deleteSql = "DELETE FROM INVENTARIO WHERE IDProducto = ? AND IDBodega = ?";
        
        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
            deleteStmt.setLong(1, productoId);
            deleteStmt.setLong(2, bodegaId);
            
            int rowsDeleted = deleteStmt.executeUpdate();
            
            if (rowsDeleted > 0) {
                logger.info(String.format("Entrada eliminada del inventario: Producto %d, Bodega %d", productoId, bodegaId));
            }
        }
    }
}
