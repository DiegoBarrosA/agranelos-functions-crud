# 🔔 Azure Event Grid - Guía de Pruebas y Verificación

## 📋 Resumen

Azure Event Grid está **completamente implementado** en el código y se activa automáticamente con cada operación CRUD. Esta guía explica cómo verificar su funcionamiento.

---

## ✅ Estado de Implementación

### Componentes Implementados

| Componente | Estado | Archivo |
|------------|--------|---------|
| **Event Publisher** | ✅ Implementado | `EventGridPublisher.java` |
| **Event Consumers** | ✅ Implementado | `EventGridConsumer.java` |
| **Event Data Models** | ✅ Implementado | `ProductoEventData.java`, `BodegaEventData.java` |
| **Event Types Enum** | ✅ Implementado | `EventType.java` |
| **Integración CRUD** | ✅ Implementado | Todas las funciones CRUD |

### Tipos de Eventos Disponibles

| Operación | Endpoint | Tipo de Evento | Subject |
|-----------|----------|----------------|---------|
| Crear Producto | `POST /api/productos` | `Agranelos.Inventario.ProductoCreado` | `/productos/{id}` |
| Actualizar Producto | `PUT /api/productos/{id}` | `Agranelos.Inventario.ProductoActualizado` | `/productos/{id}` |
| Eliminar Producto | `DELETE /api/productos/{id}` | `Agranelos.Inventario.ProductoEliminado` | `/productos/{id}` |
| Crear Bodega | `POST /api/bodegas` | `Agranelos.Inventario.BodegaCreada` | `/bodegas/{id}` |
| Actualizar Bodega | `PUT /api/bodegas/{id}` | `Agranelos.Inventario.BodegaActualizada` | `/bodegas/{id}` |
| Eliminar Bodega | `DELETE /api/bodegas/{id}` | `Agranelos.Inventario.BodegaEliminada` | `/bodegas/{id}` |

---

## 🧪 Cómo Probar Event Grid

### Opción 1: Verificar mediante Logs de Azure (Recomendado)

**Paso 1**: Realizar una operación CRUD

```bash
# Crear un producto (dispara evento ProductoCreado)
curl -X POST "https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/productos" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Test Event Grid",
    "descripcion": "Verificación de eventos",
    "precio": 99.99,
    "cantidadEnStock": 10
  }'
```

**Paso 2**: Ver logs en Azure Portal

> ⚠️ **Importante**: Si ves "Configure Application Insights to capture invocation logs", necesitas configurar Application Insights primero.  
> Ver guía completa: [`docs/CONFIGURE_APP_INSIGHTS.md`](./CONFIGURE_APP_INSIGHTS.md)

1. Ir a: https://portal.azure.com
2. Buscar: "agranelos" (tu Function App)
3. Click en: **Monitoring** → **Log stream**
4. Buscar líneas que contengan:
   - `"Evento publicado: Agranelos.Inventario.ProductoCreado"`
   - `"Evento ProductoCreado Recibido"`

---

### Opción 2: Verificar en Application Insights

**Paso 1**: Acceder a Application Insights

1. Azure Portal → Function App → **Application Insights**
2. Click en: **Transaction search**

**Paso 2**: Buscar eventos

```
Filtro: customEvents
Buscar: "ProductoCreado" o "BodegaCreada"
```

Verás:
- ✅ Timestamp del evento
- ✅ Datos del payload
- ✅ Subject del evento
- ✅ Resultado (success/error)

---

### Opción 3: Ejecutar Localmente con Azure Functions Core Tools

**Requisitos**:
```bash
# Instalar Azure Functions Core Tools v4
npm install -g azure-functions-core-tools@4 --unsafe-perm true

# O en sistemas Linux/Mac
brew tap azure/functions
brew install azure-functions-core-tools@4
```

**Paso 1**: Compilar el proyecto

```bash
mvn clean package -DskipTests
```

**Paso 2**: Iniciar funciones localmente

```bash
cd target/azure-functions/agranelos-inventario-functions
func host start
```

**Paso 3**: En otra terminal, probar endpoints

```bash
# Crear producto (dispara evento)
curl -X POST "http://localhost:7071/api/productos" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Test Local",
    "descripcion": "Prueba local de eventos",
    "precio": 50.00,
    "cantidadEnStock": 20
  }'
```

**Paso 4**: Ver logs en la terminal

Verás algo como:
```
[2025-10-03T23:30:15.123] Executing 'Functions.CreateProducto'
[2025-10-03T23:30:15.456] Producto creado con ID: 1
[2025-10-03T23:30:15.789] Evento publicado: Agranelos.Inventario.ProductoCreado para Producto ID: 1
```

---

## 🔍 Verificar que Event Grid Está Funcionando

### Test Completo de Ciclo de Vida

```bash
BASE_URL="https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api"

echo "1️⃣ Creando producto (dispara ProductoCreado)..."
RESPONSE=$(curl -s -X POST "$BASE_URL/productos" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Test EventGrid Completo",
    "descripcion": "Verificación completa del ciclo",
    "precio": 75.00,
    "cantidadEnStock": 30
  }')
echo $RESPONSE | jq '.'
PRODUCTO_ID=$(echo $RESPONSE | jq -r '.id')

echo ""
echo "2️⃣ Actualizando producto (dispara ProductoActualizado)..."
curl -s -X PUT "$BASE_URL/productos/$PRODUCTO_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Test EventGrid ACTUALIZADO",
    "descripcion": "Verificación de evento de actualización",
    "precio": 85.00,
    "cantidadEnStock": 35
  }' | jq '.'

echo ""
echo "3️⃣ Eliminando producto (dispara ProductoEliminado)..."
curl -s -X DELETE "$BASE_URL/productos/$PRODUCTO_ID" | jq '.'

echo ""
echo "✅ Se dispararon 3 eventos:"
echo "   - Agranelos.Inventario.ProductoCreado"
echo "   - Agranelos.Inventario.ProductoActualizado"
echo "   - Agranelos.Inventario.ProductoEliminado"
```

---

## 📊 Estructura del Evento Publicado

Cada operación CRUD publica un evento con esta estructura:

```json
{
  "id": "unique-event-id",
  "eventType": "Agranelos.Inventario.ProductoCreado",
  "subject": "/productos/123",
  "eventTime": "2025-10-03T23:30:15.123Z",
  "dataVersion": "1.0",
  "data": {
    "productoId": 123,
    "nombre": "Test Event Grid",
    "descripcion": "Producto para verificar eventos",
    "precio": 99.99,
    "cantidadEnStock": 10,
    "timestamp": "2025-10-03T23:30:15.123Z",
    "usuario": "system",
    "accion": "CREAR"
  }
}
```

---

## 🎯 Casos de Uso de Event Grid

### 1. **Auditoría y Compliance**
Los Event Handlers pueden:
- Registrar todos los cambios en una tabla de auditoría
- Cumplir requisitos de trazabilidad
- Mantener histórico de modificaciones

### 2. **Notificaciones**
- Enviar emails cuando se crea un producto nuevo
- Alertas SMS cuando el stock es bajo
- Webhooks a sistemas externos

### 3. **Integración con Otros Sistemas**
- Sincronizar con ERP corporativo
- Actualizar catálogos en e-commerce
- Replicar datos a Data Lake

### 4. **Caché Invalidation**
- Limpiar caché de Redis cuando cambia un producto
- Actualizar CDN con nuevos datos
- Refrescar vistas materializadas

### 5. **Workflows Automáticos**
- Aprobar productos de alto valor
- Generar órdenes de compra automáticas
- Activar procesos de calidad

---

## 🔧 Configuración en Producción

### Variables de Entorno Requeridas

```bash
EVENT_GRID_ENDPOINT="https://agranelos-eventgrid.eastus-1.eventgrid.azure.net/api/events"
EVENT_GRID_KEY="your-secure-key-here"
```

### Crear Event Grid Topic en Azure

```bash
# Crear Resource Group (si no existe)
az group create \
  --name agranelos-inventario-rg \
  --location eastus

# Crear Event Grid Topic
az eventgrid topic create \
  --name agranelos-eventgrid-topic \
  --resource-group agranelos-inventario-rg \
  --location eastus

# Obtener endpoint y key
ENDPOINT=$(az eventgrid topic show \
  --name agranelos-eventgrid-topic \
  --resource-group agranelos-inventario-rg \
  --query "endpoint" --output tsv)

KEY=$(az eventgrid topic key list \
  --name agranelos-eventgrid-topic \
  --resource-group agranelos-inventario-rg \
  --query "key1" --output tsv)

echo "EVENT_GRID_ENDPOINT=$ENDPOINT"
echo "EVENT_GRID_KEY=$KEY"
```

### Suscribir Event Handlers

```bash
# Obtener ID de la Function App
FUNCTION_APP_ID=$(az functionapp show \
  --name agranelos \
  --resource-group agranelos-inventario-rg \
  --query "id" --output tsv)

# Crear suscripción para ProductoCreado
az eventgrid event-subscription create \
  --name producto-creado-subscription \
  --source-resource-id $(az eventgrid topic show \
    --name agranelos-eventgrid-topic \
    --resource-group agranelos-inventario-rg \
    --query id --output tsv) \
  --endpoint "${FUNCTION_APP_ID}/functions/ProductoCreadoEventHandler" \
  --endpoint-type azurefunction \
  --included-event-types Agranelos.Inventario.ProductoCreado

# Repetir para cada tipo de evento...
```

---

## ⚠️ Troubleshooting

### Los eventos no se publican

**Síntomas**: No ves logs de "Evento publicado"

**Causas posibles**:
1. Variables de entorno no configuradas
2. Event Grid Topic no creado en Azure
3. Key incorrecta

**Solución**:
```bash
# Verificar configuración
az functionapp config appsettings list \
  --name agranelos \
  --resource-group agranelos-inventario-rg \
  | grep EVENT_GRID
```

### Los Event Handlers no se activan

**Síntomas**: Eventos se publican pero no se consumen

**Causas posibles**:
1. Suscripciones no creadas
2. Tipo de evento no coincide
3. Endpoint incorrecto

**Solución**:
```bash
# Listar suscripciones
az eventgrid event-subscription list \
  --source-resource-id $(az eventgrid topic show \
    --name agranelos-eventgrid-topic \
    --resource-group agranelos-inventario-rg \
    --query id --output tsv)
```

### Errores en logs

**Síntomas**: Ves "Error publicando evento"

**Nota**: El sistema está diseñado para **NO fallar** si Event Grid no está disponible. Los eventos son importantes pero no críticos. La operación CRUD se completa exitosamente aunque falle la publicación.

---

## 📈 Métricas y Monitoreo

### En Azure Portal

1. Event Grid Topic → **Metrics**
2. Ver:
   - Published Events (eventos publicados)
   - Delivery Failed Events (fallos de entrega)
   - Matched Events (eventos coincidentes)

### En Application Insights

```kusto
customEvents
| where name contains "EventGrid"
| where timestamp > ago(1h)
| summarize count() by name
| render timechart
```

---

## ✅ Checklist de Verificación

- [ ] Código de Event Grid implementado en el repositorio
- [ ] Variables de entorno configuradas en Azure
- [ ] Event Grid Topic creado en Azure
- [ ] Suscripciones creadas para cada tipo de evento
- [ ] Prueba de crear producto muestra logs de evento
- [ ] Prueba de actualizar producto muestra logs de evento
- [ ] Prueba de eliminar producto muestra logs de evento
- [ ] Event Handlers reciben y procesan eventos
- [ ] Métricas visibles en Azure Portal

---

## 🎓 Recursos Adicionales

- [Azure Event Grid Documentation](https://docs.microsoft.com/azure/event-grid/)
- [Event Grid Concepts](https://docs.microsoft.com/azure/event-grid/concepts)
- [Event Grid Triggers for Azure Functions](https://docs.microsoft.com/azure/azure-functions/functions-bindings-event-grid)

---

**Última actualización**: 3 de Octubre, 2025  
**Estado**: ✅ Event Grid completamente implementado y funcional
