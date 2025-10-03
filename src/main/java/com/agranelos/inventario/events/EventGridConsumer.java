package com.agranelos.inventario.events;

import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.EventGridTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.util.logging.Logger;

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
            
            // Aquí puedes agregar lógica de negocio:
            // - Enviar notificaciones por email/SMS
            // - Actualizar cachés
            // - Sincronizar con sistemas externos
            // - Registrar en sistemas de auditoría
            // - Activar workflows de aprobación
            
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
            
            logger.info("Evento procesado exitosamente");
            
        } catch (Exception e) {
            logger.severe("Error procesando evento BodegaEliminada: " + e.getMessage());
        }
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
}
