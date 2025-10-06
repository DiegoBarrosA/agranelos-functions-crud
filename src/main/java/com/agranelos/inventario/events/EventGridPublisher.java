package com.agranelos.inventario.events;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Servicio para publicar eventos en Azure Event Grid
 * Implementa el patrón de arquitectura orientada a eventos
 */
public class EventGridPublisher {
    
    private static EventGridPublisherClient client;
    private static final ObjectMapper objectMapper = new ObjectMapper()
        .registerModule(new JavaTimeModule());
    
    private static final String EVENT_GRID_ENDPOINT = System.getenv("EVENT_GRID_ENDPOINT");
    private static final String EVENT_GRID_KEY = System.getenv("EVENT_GRID_KEY");
    private static final String EVENT_SOURCE = "Agranelos.Inventario.Functions";
    
    /**
     * Inicializa el cliente de Event Grid con las credenciales
     */
    private static synchronized void initializeClient() {
        if (client == null) {
            String endpoint = EVENT_GRID_ENDPOINT;
            String key = EVENT_GRID_KEY;
            
            // Si no están configuradas las variables de entorno, usar valores por defecto para desarrollo local
            if (endpoint == null || endpoint.isEmpty()) {
                endpoint = "https://localhost:7071/runtime/webhooks/EventGrid";
                Logger.getLogger(EventGridPublisher.class.getName())
                    .warning("EVENT_GRID_ENDPOINT no configurado. Usando endpoint de desarrollo local.");
            }
            
            if (key == null || key.isEmpty()) {
                key = "local-development-key";
                Logger.getLogger(EventGridPublisher.class.getName())
                    .warning("EVENT_GRID_KEY no configurada. Usando key de desarrollo local.");
            }
            
            client = new EventGridPublisherClientBuilder()
                .endpoint(endpoint)
                .credential(new AzureKeyCredential(key))
                .buildEventGridEventPublisherClient();
        }
    }
    
    /**
     * Publica un evento de producto en Event Grid
     */
    public static void publishProductoEvent(EventType eventType, ProductoEventData eventData, Logger logger) {
        // Skip si no está configurado correctamente (evita errores en tests)
        if (!isConfigured()) {
            logger.warning("Event Grid no configurado, evento no publicado: " + eventType.getValue());
            return;
        }
        
        try {
            initializeClient();
            
            EventGridEvent event = new EventGridEvent(
                String.format("/productos/%d", eventData.getProductoId()),
                eventType.getValue(),
                BinaryData.fromString(objectMapper.writeValueAsString(eventData)),
                "1.0"
            );
            event.setEventTime(OffsetDateTime.now());
            
            List<EventGridEvent> events = new ArrayList<>();
            events.add(event);
            
            client.sendEvents(events);
            
            logger.info(String.format("Evento publicado: %s para Producto ID: %d", 
                eventType.getValue(), eventData.getProductoId()));
            
        } catch (Exception e) {
            logger.severe(String.format("Error publicando evento de producto: %s - %s", 
                eventType.getValue(), e.getMessage()));
            // No lanzar la excepción para no interrumpir el flujo principal
            // Los eventos son importantes pero no críticos
        }
    }
    
    /**
     * Publica un evento de bodega en Event Grid
     */
    public static void publishBodegaEvent(EventType eventType, BodegaEventData eventData, Logger logger) {
        // Skip si no está configurado correctamente (evita errores en tests)
        if (!isConfigured()) {
            logger.warning("Event Grid no configurado, evento no publicado: " + eventType.getValue());
            return;
        }
        
        try {
            initializeClient();
            
            EventGridEvent event = new EventGridEvent(
                String.format("/bodegas/%d", eventData.getBodegaId()),
                eventType.getValue(),
                BinaryData.fromString(objectMapper.writeValueAsString(eventData)),
                "1.0"
            );
            event.setEventTime(OffsetDateTime.now());
            
            List<EventGridEvent> events = new ArrayList<>();
            events.add(event);
            
            client.sendEvents(events);
            
            logger.info(String.format("Evento publicado: %s para Bodega ID: %d", 
                eventType.getValue(), eventData.getBodegaId()));
            
        } catch (Exception e) {
            logger.severe(String.format("Error publicando evento de bodega: %s - %s", 
                eventType.getValue(), e.getMessage()));
            // No lanzar la excepción para no interrumpir el flujo principal
        }
    }
    
    /**
     * Publica múltiples eventos en batch
     */
    public static void publishEvents(List<EventGridEvent> events, Logger logger) {
        try {
            initializeClient();
            
            if (events == null || events.isEmpty()) {
                logger.warning("No hay eventos para publicar");
                return;
            }
            
            client.sendEvents(events);
            
            logger.info(String.format("Se publicaron %d eventos en batch", events.size()));
            
        } catch (Exception e) {
            logger.severe(String.format("Error publicando eventos en batch: %s", e.getMessage()));
        }
    }
    
    /**
     * Valida si Event Grid está configurado correctamente
     */
    public static boolean isConfigured() {
        return EVENT_GRID_ENDPOINT != null && 
               !EVENT_GRID_ENDPOINT.isEmpty() && 
               EVENT_GRID_KEY != null && 
               !EVENT_GRID_KEY.isEmpty();
    }
}
