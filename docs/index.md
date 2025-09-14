---
layout: default
title: GraphQL API Documentation
description: Complete documentation for the Agranelos Inventario GraphQL API
---

# GraphQL API Documentation
## Sistema de Inventario Agranelos

### Base URL
```
Production: https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql
```

### Headers
```
Content-Type: application/json
```

---

## 🔍 QUERIES (Consultas)

### 1. Obtener todos los productos
```bash
curl -X POST https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql \
     -H "Content-Type: application/json" \
     -d '{
   "query": "query { productos { id nombre descripcion precio cantidad fechaCreacion fechaActualizacion } }"
 }'
```

**Respuesta esperada:**
```json
{
  "data": {
    "productos": [
      {
        "id": "1",
        "nombre": "Producto Ejemplo",
        "descripcion": "Descripción del producto",
        "precio": 29.99,
        "cantidad": 100,
        "fechaCreacion": "2025-09-14T10:30:00",
        "fechaActualizacion": "2025-09-14T15:45:00"
      }
    ]
  }
}
```

### 2. Obtener producto por ID
```bash
curl -X POST https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql \
     -H "Content-Type: application/json" \
     -d '{
   "query": "query($id: ID!) { producto(id: $id) { id nombre descripcion precio cantidad fechaCreacion fechaActualizacion } }",
   "variables": { "id": "1" }
 }'
```

### 3. Obtener todas las bodegas
```bash
curl -X POST https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql \
     -H "Content-Type: application/json" \
     -d '{
   "query": "query { bodegas { id nombre ubicacion capacidad fechaCreacion fechaActualizacion } }"
 }'
```

### 4. Obtener bodega por ID
```bash
curl -X POST https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql \
     -H "Content-Type: application/json" \
     -d '{
   "query": "query($id: ID!) { bodega(id: $id) { id nombre ubicacion capacidad fechaCreacion fechaActualizacion } }",
   "variables": { "id": "1" }
 }'
```

### 5. Health Check
```bash
curl -X POST https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql \
     -H "Content-Type: application/json" \
     -d '{
   "query": "query { health }"
 }'
```

---

## ✏️ MUTATIONS (Operaciones de escritura)

### 1. Crear producto
```bash
curl -X POST https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql \
     -H "Content-Type: application/json" \
     -d '{
   "query": "mutation($input: ProductoInput!) { crearProducto(input: $input) { success message error producto { id nombre precio cantidad } } }",
   "variables": {
     "input": {
       "nombre": "Nuevo Producto",
       "descripcion": "Descripción del nuevo producto",
       "precio": 45.50,
       "cantidad": 200
     }
   }
 }'
```

### 2. Actualizar producto
```bash
curl -X POST https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql \
     -H "Content-Type: application/json" \
     -d '{
   "query": "mutation($input: ProductoUpdateInput!) { actualizarProducto(input: $input) { success message error producto { id nombre precio cantidad } } }",
   "variables": {
     "input": {
       "id": "1",
       "nombre": "Producto Actualizado",
       "descripcion": "Nueva descripción",
       "precio": 55.99,
       "cantidad": 150
     }
   }
 }'
```

### 3. Eliminar producto
```bash
curl -X POST https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql \
     -H "Content-Type: application/json" \
     -d '{
   "query": "mutation($id: ID!) { eliminarProducto(id: $id) { success message error } }",
   "variables": { "id": "1" }
 }'
```

### 4. Crear bodega
```bash
curl -X POST https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql \
     -H "Content-Type: application/json" \
     -d '{
   "query": "mutation($input: BodegaInput!) { crearBodega(input: $input) { success message error bodega { id nombre ubicacion capacidad } } }",
   "variables": {
     "input": {
       "nombre": "Bodega Principal",
       "ubicacion": "Zona Industrial Norte",
       "capacidad": 5000
     }
   }
 }'
```

### 5. Actualizar bodega
```bash
curl -X POST https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql \
     -H "Content-Type: application/json" \
     -d '{
   "query": "mutation($input: BodegaUpdateInput!) { actualizarBodega(input: $input) { success message error bodega { id nombre ubicacion capacidad } } }",
   "variables": {
     "input": {
       "id": "1",
       "nombre": "Bodega Central Actualizada",
       "ubicacion": "Nueva Zona Industrial",
       "capacidad": 8000
     }
   }
 }'
```

### 6. Eliminar bodega
```bash
curl -X POST https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql \
     -H "Content-Type: application/json" \
     -d '{
   "query": "mutation($id: ID!) { eliminarBodega(id: $id) { success message error } }",
   "variables": { "id": "1" }
 }'
```

### 7. Inicializar base de datos
```bash
curl -X POST https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql \
     -H "Content-Type: application/json" \
     -d '{
   "query": "mutation { inicializarBaseDatos }"
 }'
```

---

## 📋 ESQUEMA DE DATOS

### Tipo Producto
```graphql
type Producto {
    id: ID!
    nombre: String!
    descripcion: String
    precio: Float!
    cantidad: Int!
    fechaCreacion: DateTime
    fechaActualizacion: DateTime
}
```

### Tipo Bodega
```graphql
type Bodega {
    id: ID!
    nombre: String!
    ubicacion: String!
    capacidad: Int!
    fechaCreacion: DateTime
    fechaActualizacion: DateTime
}
```

### Inputs para Mutations

#### ProductoInput
```graphql
input ProductoInput {
    nombre: String!
    descripcion: String
    precio: Float!
    cantidad: Int!
}
```

#### ProductoUpdateInput
```graphql
input ProductoUpdateInput {
    id: ID!
    nombre: String
    descripcion: String
    precio: Float
    cantidad: Int
}
```

#### BodegaInput
```graphql
input BodegaInput {
    nombre: String!
    ubicacion: String!
    capacidad: Int
}
```

#### BodegaUpdateInput
```graphql
input BodegaUpdateInput {
    id: ID!
    nombre: String
    ubicacion: String
    capacidad: Int
}
```

---

## 🎯 EJEMPLOS DE CONSULTAS COMPLEJAS

### Consulta múltiple (productos y bodegas)
```bash
curl -X POST https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql \
     -H "Content-Type: application/json" \
     -d '{
   "query": "query { productos { id nombre precio cantidad } bodegas { id nombre ubicacion capacidad } }"
 }'
```

### Consulta con campos específicos
```bash
curl -X POST https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql \
     -H "Content-Type: application/json" \
     -d '{
   "query": "query { productos { id nombre precio } }"
 }'
```

### Operación con nombre y variables
```bash
curl -X POST https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql \
     -H "Content-Type: application/json" \
     -d '{
   "query": "query GetProductoDetalle($productId: ID!) { producto(id: $productId) { id nombre descripcion precio cantidad fechaCreacion } }",
   "variables": { "productId": "1" },
   "operationName": "GetProductoDetalle"
 }'
```

---

## ⚠️ MANEJO DE ERRORES

### Error de validación
```json
{
  "errors": [
    {
      "message": "Validation error (FieldUndefined@[productos/campoInexistente]) : Field 'campoInexistente' in type 'Producto' is undefined",
      "locations": [{"line": 1, "column": 20}],
      "extensions": {"classification": "ValidationError"}
    }
  ]
}
```

### Error de servidor
```json
{
  "errors": [
    {
      "message": "Error interno del servidor"
    }
  ]
}
```

### Respuesta de mutation con error
```json
{
  "data": {
    "crearProducto": {
      "success": false,
      "message": "",
      "error": "El nombre del producto es requerido",
      "producto": null
    }
  }
}
```

---

## 🔗 COMPARACIÓN CON REST API

| GraphQL Endpoint | REST Equivalente |
|------------------|------------------|
| `query { productos }` | `GET /api/productos` |
| `query { producto(id: "1") }` | `GET /api/productos/1` |
| `mutation { crearProducto(...) }` | `POST /api/productos` |
| `mutation { actualizarProducto(...) }` | `PUT /api/productos/1` |
| `mutation { eliminarProducto(...) }` | `DELETE /api/productos/1` |
| `query { bodegas }` | `GET /api/bodegas` |
| `query { bodega(id: "1") }` | `GET /api/bodegas/1` |

---

## 📝 NOTAS TÉCNICAS

1. **Campo `cantidad`**: En el esquema GraphQL se llama `cantidad`, pero internamente mapea al campo Java `cantidadEnStock`
2. **Fechas**: Se manejan como scalar `DateTime` en formato ISO 8601
3. **IDs**: Se manejan como strings en GraphQL pero son integers en la base de datos
4. **Validaciones**: Los campos marcados con `!` son obligatorios
5. **Capacidad por defecto**: Las bodegas sin capacidad especificada usan 1000 como valor por defecto

---

## 🚀 VENTAJAS DE GraphQL vs REST

- ✅ **Queries flexibles**: Solicita solo los campos necesarios
- ✅ **Una sola request**: Obtén datos de múltiples recursos
- ✅ **Tipado fuerte**: Validación automática del esquema
- ✅ **Introspección**: El esquema es autodocumentado
- ✅ **Versionado**: No necesita versiones de API
- ✅ **Desarrollo ágil**: Evolución del schema sin breaking changes