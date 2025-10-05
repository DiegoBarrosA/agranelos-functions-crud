package com.agranelos.inventario.services;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Servicio para enviar notificaciones por email usando SendGrid
 * Se activa cuando ocurren eventos en el sistema de inventario
 */
public class EmailService {
    
    private static final String SENDGRID_API_KEY = System.getenv("SENDGRID_API_KEY");
    private static final String FROM_EMAIL = System.getenv("SENDER_EMAIL");
    private static final String TO_EMAIL = System.getenv("RECIPIENT_EMAIL");
    
    /**
     * Envía un email de notificación cuando se crea un producto
     */
    public static void sendProductoCreatedEmail(Long productoId, String nombre, Logger logger) {
        String subject = "✅ Nuevo Producto Creado - Inventario Agranelos";
        String body = String.format(
            "<html><body>" +
            "<h2>Nuevo Producto Creado</h2>" +
            "<p>Se ha creado un nuevo producto en el sistema de inventario:</p>" +
            "<ul>" +
            "<li><strong>ID:</strong> %d</li>" +
            "<li><strong>Nombre:</strong> %s</li>" +
            "<li><strong>Fecha:</strong> %s</li>" +
            "</ul>" +
            "<p><em>Sistema de Inventario Agranelos</em></p>" +
            "</body></html>",
            productoId, nombre, java.time.LocalDateTime.now()
        );
        
        sendEmail(subject, body, logger);
    }
    
    /**
     * Envía un email de notificación cuando se actualiza un producto
     */
    public static void sendProductoUpdatedEmail(Long productoId, String nombre, Logger logger) {
        String subject = "🔄 Producto Actualizado - Inventario Agranelos";
        String body = String.format(
            "<html><body>" +
            "<h2>Producto Actualizado</h2>" +
            "<p>Se ha actualizado un producto en el sistema:</p>" +
            "<ul>" +
            "<li><strong>ID:</strong> %d</li>" +
            "<li><strong>Nombre:</strong> %s</li>" +
            "<li><strong>Fecha:</strong> %s</li>" +
            "</ul>" +
            "<p><em>Sistema de Inventario Agranelos</em></p>" +
            "</body></html>",
            productoId, nombre, java.time.LocalDateTime.now()
        );
        
        sendEmail(subject, body, logger);
    }
    
    /**
     * Envía un email de notificación cuando se elimina un producto
     */
    public static void sendProductoDeletedEmail(Long productoId, Logger logger) {
        String subject = "🗑️ Producto Eliminado - Inventario Agranelos";
        String body = String.format(
            "<html><body>" +
            "<h2>Producto Eliminado</h2>" +
            "<p>Se ha eliminado un producto del sistema:</p>" +
            "<ul>" +
            "<li><strong>ID:</strong> %d</li>" +
            "<li><strong>Fecha:</strong> %s</li>" +
            "</ul>" +
            "<p><em>Sistema de Inventario Agranelos</em></p>" +
            "</body></html>",
            productoId, java.time.LocalDateTime.now()
        );
        
        sendEmail(subject, body, logger);
    }
    
    /**
     * Envía un email de notificación cuando se crea una bodega
     */
    public static void sendBodegaCreatedEmail(Long bodegaId, String nombre, Logger logger) {
        String subject = "🏢 Nueva Bodega Creada - Inventario Agranelos";
        String body = String.format(
            "<html><body>" +
            "<h2>Nueva Bodega Creada</h2>" +
            "<p>Se ha creado una nueva bodega en el sistema:</p>" +
            "<ul>" +
            "<li><strong>ID:</strong> %d</li>" +
            "<li><strong>Nombre:</strong> %s</li>" +
            "<li><strong>Fecha:</strong> %s</li>" +
            "</ul>" +
            "<p><em>Sistema de Inventario Agranelos</em></p>" +
            "</body></html>",
            bodegaId, nombre, java.time.LocalDateTime.now()
        );
        
        sendEmail(subject, body, logger);
    }
    
    /**
     * Envía un email de notificación cuando se actualiza una bodega
     */
    public static void sendBodegaUpdatedEmail(Long bodegaId, String nombre, Logger logger) {
        String subject = "🔄 Bodega Actualizada - Inventario Agranelos";
        String body = String.format(
            "<html><body>" +
            "<h2>Bodega Actualizada</h2>" +
            "<p>Se ha actualizado una bodega en el sistema:</p>" +
            "<ul>" +
            "<li><strong>ID:</strong> %d</li>" +
            "<li><strong>Nombre:</strong> %s</li>" +
            "<li><strong>Fecha:</strong> %s</li>" +
            "</ul>" +
            "<p><em>Sistema de Inventario Agranelos</em></p>" +
            "</body></html>",
            bodegaId, nombre, java.time.LocalDateTime.now()
        );
        
        sendEmail(subject, body, logger);
    }
    
    /**
     * Envía un email de notificación cuando se elimina una bodega
     */
    public static void sendBodegaDeletedEmail(Long bodegaId, Logger logger) {
        String subject = "🗑️ Bodega Eliminada - Inventario Agranelos";
        String body = String.format(
            "<html><body>" +
            "<h2>Bodega Eliminada</h2>" +
            "<p>Se ha eliminado una bodega del sistema:</p>" +
            "<ul>" +
            "<li><strong>ID:</strong> %d</li>" +
            "<li><strong>Fecha:</strong> %s</li>" +
            "</ul>" +
            "<p><em>Sistema de Inventario Agranelos</em></p>" +
            "</body></html>",
            bodegaId, java.time.LocalDateTime.now()
        );
        
        sendEmail(subject, body, logger);
    }
    
    /**
     * Método privado para enviar emails usando SendGrid
     */
    private static void sendEmail(String subject, String htmlBody, Logger logger) {
        // Validar configuración
        if (SENDGRID_API_KEY == null || SENDGRID_API_KEY.isEmpty()) {
            logger.warning("SendGrid API Key no configurada. Email no enviado.");
            return;
        }
        
        if (FROM_EMAIL == null || FROM_EMAIL.isEmpty()) {
            logger.warning("Email remitente no configurado. Email no enviado.");
            return;
        }
        
        try {
            String recipient = TO_EMAIL != null && !TO_EMAIL.isEmpty() ? TO_EMAIL : "di.barros@duocuc.cl";
            
            Email from = new Email(FROM_EMAIL);
            Email to = new Email(recipient);
            Content content = new Content("text/html", htmlBody);
            Mail mail = new Mail(from, subject, to, content);
            
            SendGrid sg = new SendGrid(SENDGRID_API_KEY);
            Request request = new Request();
            
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            
            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                logger.info(String.format("Email enviado exitosamente a %s: %s (Status: %d)", 
                    recipient, subject, response.getStatusCode()));
            } else {
                logger.warning(String.format("Error al enviar email. Status: %d, Body: %s", 
                    response.getStatusCode(), response.getBody()));
            }
            
        } catch (IOException e) {
            logger.severe(String.format("Error enviando email: %s", e.getMessage()));
        }
    }
}
