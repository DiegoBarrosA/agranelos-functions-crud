package com.agranelos.inventario.events;

/**
 * Tipos de eventos del sistema de inventario
 */
public enum EventType {
    // Eventos de Productos
    PRODUCTO_CREADO("Agranelos.Inventario.ProductoCreado"),
    PRODUCTO_ACTUALIZADO("Agranelos.Inventario.ProductoActualizado"),
    PRODUCTO_ELIMINADO("Agranelos.Inventario.ProductoEliminado"),
    
    // Eventos de Bodegas
    BODEGA_CREADA("Agranelos.Inventario.BodegaCreada"),
    BODEGA_ACTUALIZADA("Agranelos.Inventario.BodegaActualizada"),
    BODEGA_ELIMINADA("Agranelos.Inventario.BodegaEliminada"),
    
    // Eventos de Inventario
    INVENTARIO_ACTUALIZADO("Agranelos.Inventario.InventarioActualizado"),
    STOCK_BAJO("Agranelos.Inventario.StockBajo"),
    
    // Eventos de Movimientos
    MOVIMIENTO_REGISTRADO("Agranelos.Inventario.MovimientoRegistrado");

    private final String value;

    EventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
