package com.agranelos.inventario.model;

import java.time.LocalDateTime;

public class Bodega {
    private Integer id;
    private String nombre;
    private String ubicacion;
    private Integer capacidad;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;

    // Constructors
    public Bodega() {}

    public Bodega(String nombre, String ubicacion, Integer capacidad) {
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.capacidad = capacidad;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }

    @Override
    public String toString() {
        return "Bodega{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", ubicacion='" + ubicacion + '\'' +
                ", capacidad=" + capacidad +
                ", fechaCreacion=" + fechaCreacion +
                ", fechaActualizacion=" + fechaActualizacion +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Bodega bodega = (Bodega) o;

        return id != null ? id.equals(bodega.id) : bodega.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
