# 🔧 Configuración de Event Grid

## Problema Identificado

Las Azure Functions no están enviando eventos a Event Grid porque las variables de entorno no están configuradas correctamente.

## Variables Actuales (Incorrectas)

```json
{
  "EVENT_GRID_ENDPOINT": "https://agranelos-eventgrid.eastus-1.eventgrid.azure.net/api/events",
  "EVENT_GRID_KEY": "your-event-grid-key-here"
}
```

## Configuración Correcta

### 1. Obtener las credenciales de Event Grid

Basado en la captura de Azure Portal, tu Event Grid Topic es:
- **Nombre**: `agranelosEventGrid`
- **Resource Group**: `agranelos`
- **Location**: `East US 2`
- **Endpoint**: `https://agranelos0ventgrid.eastus2-1.eventgrid.azure.net/api/events`

### 2. Obtener la Access Key

#### Opción A: Desde Azure Portal (GUI)

1. Ve a Azure Portal: https://portal.azure.com
2. Navega a tu Resource Group `agranelos`
3. Selecciona el Event Grid Topic `agranelosEventGrid`
4. En el menú izquierdo, selecciona **"Access keys"**
5. Copia el valor de **"Key 1"** o **"Key 2"**

#### Opción B: Desde Azure CLI

```bash
# Obtener la key del Event Grid Topic
az eventgrid topic key list \
  --name agranelosEventGrid \
  --resource-group agranelos \
  --query "key1" \
  --output tsv
```

### 3. Actualizar `local.settings.json`

```json
{
  "IsEncrypted": false,
  "Values": {
    "AzureWebJobsStorage": "UseDevelopmentStorage=true",
    "FUNCTIONS_WORKER_RUNTIME": "java",
    "DB_HOST": "50.19.86.166",
    "DB_PORT": "5432",
    "DB_NAME": "inventario_agranelos",
    "DB_USER": "postgres",
    "DB_PASSWORD": "JUq2Uh9giFapgp8Vk7q8bWnhwPartBSIbyvgYmKPtGYmGAEIKrrzZYkPVRg4xf9cMsMUgA47JU9fYLMAI66fbatWKB2i5XVJ3JiMkb8NLFwGQgUoaeVa8c7PvuMrM5F4",
    "DB_SSL_MODE": "disable",
    "EVENT_GRID_ENDPOINT": "https://agranelos0ventgrid.eastus2-1.eventgrid.azure.net/api/events",
    "EVENT_GRID_KEY": "<PEGA_AQUI_LA_KEY_REAL>"
  }
}
```

**⚠️ IMPORTANTE**: Reemplaza `<PEGA_AQUI_LA_KEY_REAL>` con la key real obtenida en el paso 2.

### 4. Para Azure (Producción)

Si vas a desplegar las funciones a Azure, configura las variables de entorno en Azure Functions:

```bash
# Configurar Event Grid Endpoint
az functionapp config appsettings set \
  --name <tu-function-app-name> \
  --resource-group agranelos \
  --settings "EVENT_GRID_ENDPOINT=https://agranelos0ventgrid.eastus2-1.eventgrid.azure.net/api/events"

# Configurar Event Grid Key
az functionapp config appsettings set \
  --name <tu-function-app-name> \
  --resource-group agranelos \
  --settings "EVENT_GRID_KEY=<tu-access-key>"
```

## Verificación

### 1. Reiniciar Azure Functions

Después de actualizar `local.settings.json`, reinicia las funciones:

```bash
# Detener funciones si están corriendo
# Ctrl+C en la terminal donde corren las funciones

# Limpiar y recompilar
cd agranelos-functions-crud-create
mvn clean package

# Iniciar funciones
cd target/azure-functions/agranelos-inventario-functions
func host start
```

### 2. Probar envío de eventos

```bash
# Crear un producto (debería enviar evento)
curl -X POST http://localhost:7071/api/productos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Test Event Grid",
    "descripcion": "Producto para probar eventos",
    "precio": 99.99,
    "cantidad": 10
  }'
```

### 3. Verificar en Azure Portal

1. Ve a tu Event Grid Topic en Azure Portal
2. Click en **"Metrics"** en el menú izquierdo
3. Selecciona la métrica **"Published Events"**
4. Deberías ver eventos publicados en la gráfica

### 4. Verificar logs de las funciones

En los logs de Azure Functions deberías ver:

```
Evento publicado: Agranelos.Inventario.ProductoCreado para Producto ID: 123
```

Si ves un error como:
```
Error publicando evento de producto: Agranelos.Inventario.ProductoCreado - <error>
```

Entonces revisa:
- ✅ El endpoint es correcto
- ✅ La key es válida
- ✅ El Event Grid Topic existe y está activo
- ✅ No hay reglas de firewall bloqueando

## Tipos de Eventos Implementados

Las funciones publican los siguientes eventos:

### Productos
- `Agranelos.Inventario.ProductoCreado` - Cuando se crea un producto
- `Agranelos.Inventario.ProductoActualizado` - Cuando se actualiza un producto
- `Agranelos.Inventario.ProductoEliminado` - Cuando se elimina un producto

### Bodegas
- `Agranelos.Inventario.BodegaCreada` - Cuando se crea una bodega
- `Agranelos.Inventario.BodegaActualizada` - Cuando se actualiza una bodega
- `Agranelos.Inventario.BodegaEliminada` - Cuando se elimina una bodega

## Event Subscriptions

Veo en tu Azure Portal que tienes 2 Event Subscriptions configuradas:
1. **functionAgranelosHub** - Endpoint: AzureFunction
2. **CrearBodegas** - Endpoint: AzureFunction

Estas subscriptions están configuradas para escuchar eventos específicos y ejecutar funciones cuando ocurran.

## Estructura del Evento

Los eventos se envían con esta estructura:

```json
{
  "id": "unique-event-id",
  "eventType": "Agranelos.Inventario.ProductoCreado",
  "subject": "/productos/123",
  "eventTime": "2025-10-04T10:30:00Z",
  "data": {
    "productoId": 123,
    "nombre": "Test Event Grid",
    "descripcion": "Producto para probar eventos",
    "precio": 99.99,
    "cantidad": 10,
    "operacion": "CREATE",
    "usuario": "system",
    "timestamp": "2025-10-04T10:30:00Z"
  },
  "dataVersion": "1.0"
}
```

## Troubleshooting

### Los eventos no aparecen en Azure Portal

**Solución 1**: Verificar configuración
```bash
# Ver configuración actual
cat local.settings.json | grep EVENT_GRID
```

**Solución 2**: Verificar conectividad
```bash
# Probar conexión al endpoint
curl -X POST https://agranelos0ventgrid.eastus2-1.eventgrid.azure.net/api/events \
  -H "aeg-sas-key: <tu-key>" \
  -H "Content-Type: application/json" \
  -d '[{"eventType":"test","subject":"test","data":{"test":"data"},"dataVersion":"1.0"}]'
```

**Solución 3**: Revisar logs detallados
```bash
# Ejecutar funciones con más logging
func host start --verbose
```

### Error "Unauthorized"

- ✅ Verifica que la `EVENT_GRID_KEY` sea correcta
- ✅ Asegúrate de no tener espacios extras en la key
- ✅ Prueba con ambas keys (Key 1 y Key 2)

### Error "Bad Request" o "Invalid endpoint"

- ✅ Verifica que el endpoint termine en `/api/events`
- ✅ Asegúrate de usar `https://` no `http://`
- ✅ Verifica que el nombre del topic sea correcto

## Próximos Pasos

1. ✅ Obtener la Event Grid Access Key
2. ✅ Actualizar `local.settings.json` con valores correctos
3. ✅ Reiniciar Azure Functions
4. ✅ Crear/actualizar/eliminar un producto o bodega
5. ✅ Verificar eventos en Azure Portal Metrics
6. ✅ Verificar logs de funciones consumidoras

## Referencias

- [Azure Event Grid Documentation](https://docs.microsoft.com/azure/event-grid/)
- [Event Grid Publisher Java SDK](https://docs.microsoft.com/java/api/overview/azure/messaging-eventgrid-readme)
- [Azure Functions Event Grid Trigger](https://docs.microsoft.com/azure/azure-functions/functions-bindings-event-grid-trigger)
