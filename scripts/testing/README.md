# Scripts de Testing - Sistema de Inventario Agranelos

Este directorio contiene los scripts para testing automatizado del sistema de inventario.

## 📁 Estructura

```
scripts/testing/
├── test-all-apis.sh          # Script principal - ejecuta todos los tests
├── test-rest-api.sh         # Tests específicos para API REST  
├── test-graphql-api.sh      # Tests específicos para API GraphQL
├── test-performance.sh      # Tests de rendimiento y carga
└── README.md               # Esta documentación
```

## 🚀 Uso Rápido

### Ejecutar todos los tests
```bash
./scripts/testing/test-all-apis.sh
```

### Ejecutar tests específicos
```bash
# Solo REST API
./scripts/testing/test-rest-api.sh

# Solo GraphQL API  
./scripts/testing/test-graphql-api.sh

# Tests de rendimiento
./scripts/testing/test-performance.sh
```

## ⚙️ Configuración

Los scripts están configurados para usar localhost por defecto, pero pueden ser sobrescritos con variables de entorno:

### URLs por Defecto
- **REST API**: `http://localhost:7071/api`
- **GraphQL API**: `http://localhost:7071/api/graphql`

### Sobrescribir con Variables de Entorno

```bash
# Para cambiar a staging
export REST_API_URL="https://agranelos-staging.azurewebsites.net/api"
export GRAPHQL_API_URL="https://agranelos-staging.azurewebsites.net/api/graphql"

# Para cambiar a producción
export REST_API_URL="https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api"
export GRAPHQL_API_URL="https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql"

# Ejecutar tests con URLs personalizadas
./test-all-apis.sh
```

## 📊 Resultados

### Test Principal (test-all-apis.sh)
- Ejecuta una suite completa de testing
- Compara REST vs GraphQL
- Genera reporte de rendimiento
- Incluye tests de error handling

### Tests Específicos
- **REST**: CRUD completo, validaciones, casos edge
- **GraphQL**: Queries, mutations, introspección, field mapping
- **Performance**: Tests de carga, concurrencia, stress

## 🔍 Características Testadas

### Funcionalidades Clave
- ✅ CRUD de Productos y Bodegas
- ✅ Field mapping (`cantidad` ↔ `cantidadEnStock`)
- ✅ PostgreSQL column case sensitivity
- ✅ Error handling y validaciones
- ✅ Schema introspection (GraphQL)
- ✅ Consultas complejas y variables

### Casos de Uso
- Operaciones básicas CRUD
- Consultas combinadas
- Manejo de errores
- Validación de tipos
- Tests de concurrencia

## 📋 Prerrequisitos

```bash
# Herramientas requeridas
curl    # Para llamadas HTTP
jq      # Para procesamiento JSON
```

## 🎯 Interpretación de Resultados

- **✓ PASSED**: Test exitoso
- **✗ FAILED**: Test fallido (revisar logs)
- **Status Codes**: 200 (OK), 201 (Created), 404 (Not Found), etc.

Los scripts generan logs detallados para debugging y análisis de performance.