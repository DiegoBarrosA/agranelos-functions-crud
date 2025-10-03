package com.agranelos.inventario.events;

import com.agranelos.inventario.model.Producto;
import java.time.LocalDateTime;

/**
 * Datos del evento de Producto para Azure Event Grid
 */
public class ProductoEventData {
    private Integer productoId;
    private String nombre;
    private String descripcion;
    private String precio;
    private Integer cantidadEnStock;
    private LocalDateTime timestamp;
    private String operation;
    private String usuario;

    public ProductoEventData() {
        this.timestamp = LocalDateTime.now();
    }

    public ProductoEventData(Producto producto, String operation, String usuario) {
        this.productoId = producto.getId();
        this.nombre = producto.getNombre();
        this.descripcion = producto.getDescripcion();
        this.precio = producto.getPrecio() != null ? producto.getPrecio().toString() : "0.00";
        this.cantidadEnStock = producto.getCantidadEnStock();
        this.timestamp = LocalDateTime.now();
        this.operation = operation;
        this.usuario = usuario;
    }

    // Getters y Setters
    public Integer getProductoId() {
        return productoId;
    }

    public void setProductoId(Integer productoId) {
        this.productoId = productoId;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getPrecio() {
        return precio;
    }

    public void setPrecio(String precio) {
        this.precio = precio;
    }

    public Integer getCantidadEnStock() {
        return cantidadEnStock;
    }

    public void setCantidadEnStock(Integer cantidadEnStock) {
        this.cantidadEnStock = cantidadEnStock;
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
        return "ProductoEventData{" +
                "productoId=" + productoId +
                ", nombre='" + nombre + '\'' +
                ", operation='" + operation + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
