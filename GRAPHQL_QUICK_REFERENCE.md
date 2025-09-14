# GraphQL API - Quick Reference
## Agranelos Inventario System

**Base URL:** `https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql`

---

## 🔍 CONSULTAS BÁSICAS

### Todos los productos
```bash
curl -X POST https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "{ productos { id nombre precio cantidad } }"}'
```

### Producto por ID
```bash
curl -X POST https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "query($id: ID!) { producto(id: $id) { id nombre precio cantidad } }", "variables": {"id": "1"}}'
```

### Todas las bodegas
```bash
curl -X POST https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "{ bodegas { id nombre ubicacion capacidad } }"}'
```

---

## ✏️ OPERACIONES DE ESCRITURA

### Crear producto
```bash
curl -X POST https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "mutation($input: ProductoInput!) { crearProducto(input: $input) { success message producto { id nombre } } }", "variables": {"input": {"nombre": "Nuevo Producto", "descripcion": "Test", "precio": 29.99, "cantidad": 100}}}'
```

### Actualizar producto
```bash
curl -X POST https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "mutation($input: ProductoUpdateInput!) { actualizarProducto(input: $input) { success message } }", "variables": {"input": {"id": "1", "nombre": "Producto Actualizado", "precio": 35.99}}}'
```

### Eliminar producto
```bash
curl -X POST https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "mutation($id: ID!) { eliminarProducto(id: $id) { success message } }", "variables": {"id": "1"}}'
```

### Crear bodega
```bash
curl -X POST https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "mutation($input: BodegaInput!) { crearBodega(input: $input) { success message bodega { id nombre } } }", "variables": {"input": {"nombre": "Nueva Bodega", "ubicacion": "Zona Norte", "capacidad": 1500}}}'
```

---

## 🎯 CONSULTAS ÚTILES

### Health Check
```bash
curl -X POST https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "{ health }"}'
```

### Consulta combinada
```bash
curl -X POST https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "{ productos { id nombre precio } bodegas { id nombre capacidad } }"}'
```

### Solo nombres y precios
```bash
curl -X POST https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "{ productos { nombre precio } }"}'
```

---

## 📋 CAMPOS DISPONIBLES

**Producto:** `id`, `nombre`, `descripcion`, `precio`, `cantidad`, `fechaCreacion`, `fechaActualizacion`

**Bodega:** `id`, `nombre`, `ubicacion`, `capacidad`, `fechaCreacion`, `fechaActualizacion`

---

## ⚠️ NOTAS IMPORTANTES

1. **Campo cantidad**: Usa `cantidad` en GraphQL (mapea a `cantidadEnStock` internamente)
2. **IDs**: Siempre como strings en GraphQL: `"1"`, `"2"`, etc.
3. **Campos requeridos**: Marcados con `!` en el esquema
4. **Fechas**: Formato ISO 8601: `"2025-09-14T10:30:00"`

---

## 🔗 REST Endpoints (alternativa)

- `GET /api/productos` - Listar productos
- `GET /api/productos/{id}` - Obtener producto
- `POST /api/productos` - Crear producto
- `PUT /api/productos/{id}` - Actualizar producto  
- `DELETE /api/productos/{id}` - Eliminar producto
- `GET /api/bodegas` - Listar bodegas
- `POST /api/bodegas` - Crear bodega
- `PUT /api/bodegas/{id}` - Actualizar bodega
- `DELETE /api/bodegas/{id}` - Eliminar bodega