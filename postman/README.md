# Postman Collections - Sistema de Inventario Agranelos

Esta carpeta contiene las colecciones de Postman para testing interactivo de las APIs.

## 📦 Colecciones Disponibles

### Agranelos-Inventario-API-Collection.postman_collection.json
Colección completa para testing de APIs REST y GraphQL.

## 🚀 Importar en Postman

1. Abre Postman
2. Click en "Import" 
3. Arrastra el archivo JSON o selecciona "Upload Files"
4. Importa la colección

## ⚙️ Variables de Entorno

La colección incluye variables pre-configuradas:

```json
{
  "base_url": "https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api",
  "graphql_url": "https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql",
  "producto_id": "1",
  "bodega_id": "1"
}
```

## 📁 Estructura de la Colección

### 0. Database Setup
- Inicialización de base de datos

### 1. REST API - Productos  
- CRUD completo de productos
- Auto-actualización de `producto_id`

### 2. REST API - Bodegas
- CRUD completo de bodegas
- Auto-actualización de `bodega_id`

### 3. GraphQL - Query Operations
- Consultas de productos y bodegas
- Tests de field mapping (`cantidad`)
- Consultas combinadas

### 4. GraphQL - Mutation Operations
- Mutaciones CRUD con variables
- Responses estructuradas

### 5. GraphQL - Advanced Operations
- Introspección de schema
- Validación de tipos
- Tests de error handling

### 6. Error Handling Tests
- Tests de errores 404, 400
- Validación de respuestas de error

## 🎯 Características Especiales

- **Auto-Variables**: Los IDs se actualizan automáticamente después de crear recursos
- **Field Mapping**: Tests específicos para el mapeo `cantidad` ↔ `cantidadEnStock`
- **Error Testing**: Validación completa de manejo de errores
- **Schema Introspection**: Exploración completa del schema GraphQL

## 💡 Tips de Uso

1. Ejecuta "Initialize Database" primero
2. Usa las carpetas en orden para flujo completo
3. Las variables se actualizan automáticamente
4. Revisa los tests en la pestaña "Tests" de cada request