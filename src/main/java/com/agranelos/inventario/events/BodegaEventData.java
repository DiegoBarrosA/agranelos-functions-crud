package com.agranelos.inventario.events;

import com.agranelos.inventario.model.Bodega;
import java.time.LocalDateTime;

/**
 * Datos del evento de Bodega para Azure Event Grid
 */
public class BodegaEventData {
    private Integer bodegaId;
    private String nombre;
    private String ubicacion;
    private Integer capacidad;
    private LocalDateTime timestamp;
    private String operation;
    private String usuario;

    public BodegaEventData() {
        this.timestamp = LocalDateTime.now();
    }

    public BodegaEventData(Bodega bodega, String operation, String usuario) {
        this.bodegaId = bodega.getId();
        this.nombre = bodega.getNombre();
        this.ubicacion = bodega.getUbicacion();
        this.capacidad = bodega.getCapacidad();
        this.timestamp = LocalDateTime.now();
        this.operation = operation;
        this.usuario = usuario;
    }

    // Getters y Setters
    public Integer getBodegaId() {
        return bodegaId;
    }

    public void setBodegaId(Integer bodegaId) {
        this.bodegaId = bodegaId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Integer getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(Integer capacidad) {
        this.capacidad = capacidad;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    @Override
    public String toString() {
        return "BodegaEventData{" +
                "bodegaId=" + bodegaId +
                ", nombre='" + nombre + '\'' +
                ", operation='" + operation + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
