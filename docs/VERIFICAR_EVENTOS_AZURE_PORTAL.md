# 🔍 Cómo Verificar Eventos en Azure Portal

## 📊 Opción 1: Metrics del Event Grid Topic (Recomendado)

Esta es la forma más rápida de ver si los eventos están llegando.

### Pasos:

1. **Abre Azure Portal**: https://portal.azure.com

2. **Navega a tu Event Grid Topic**:
   - Click en el menú hamburguesa (☰) arriba a la izquierda
   - Selecciona **"Resource groups"**
   - Click en **`agranelos`**
   - Busca y click en **`agranelosEventGrid`** (tipo: Event Grid Topic)

3. **Abre Metrics**:
   - En el menú izquierdo, busca la sección **"Monitoring"**
   - Click en **"Metrics"**

4. **Configurar la métrica**:
   - En **"Metric"** selecciona: **"Publish Succeeded"** o **"Published Events"**
   - En **"Aggregation"** selecciona: **"Sum"**
   - En **"Time range"** selecciona: **"Last 30 minutes"** o **"Last hour"**

5. **Ver resultados**:
   - Si ves una línea con valores > 0, ¡los eventos están llegando! ✅
   - Si la línea está en 0, los eventos no están siendo publicados ❌

### Métricas Importantes:

| Métrica | Descripción |
|---------|-------------|
| **Published Events** | Total de eventos publicados al topic |
| **Publish Succeeded** | Eventos publicados exitosamente |
| **Publish Failed** | Eventos que fallaron al publicar |
| **Unmatched Events** | Eventos sin subscription que los consuma |
| **Delivery Succeeded** | Eventos entregados exitosamente a subscriptions |
| **Delivery Failed** | Eventos que fallaron al ser entregados |

---

## 📧 Opción 2: Event Subscriptions (Ver entregas)

Para ver si los eventos están siendo entregados a las funciones consumidoras:

### Pasos:

1. **En tu Event Grid Topic** (`agranelosEventGrid`)

2. **Click en "Event Subscriptions"** (menú izquierdo)

3. **Verás tus 2 subscriptions**:
   - `functionAgranelosHub`
   - `CrearBodegas`

4. **Click en cualquiera de ellas**

5. **Click en "Metrics"** (menú izquierdo)

6. **Configurar métrica**:
   - Selecciona: **"Delivery Succeeded"**
   - Time range: **"Last hour"**

7. **Ver Dead Letter Events** (eventos que fallaron):
   - En el mismo Metrics, selecciona: **"Dead Lettered Events"**
   - Si ves valores > 0, hay eventos que no se pudieron entregar

---

## 🔔 Opción 3: Ver Logs en la Function App Consumidora

Para ver los logs de las funciones que consumen los eventos:

### Pasos:

1. **Navega a tu Function App**:
   - Resource group: `agranelos`
   - Busca tu Function App (la que tiene las funciones consumidoras)

2. **Click en "Log stream"** (menú izquierdo, sección Monitoring)

3. **Genera un evento** (crea/actualiza/elimina un producto)

4. **Observa los logs en tiempo real**:
   ```
   === Evento ProductoCreado Recibido ===
   Event Type: Agranelos.Inventario.ProductoCreado
   Subject: /productos/123
   Data: {...}
   Evento procesado exitosamente
   ```

---

## 📝 Opción 4: Application Insights (Análisis Detallado)

Si tienes Application Insights configurado:

### Pasos:

1. **Navega a tu Function App**

2. **Click en "Application Insights"** (menú izquierdo)

3. **Click en "View Application Insights data"**

4. **En Application Insights**:
   - Click en **"Logs"** (menú izquierdo)

5. **Ejecuta una query**:
   ```kusto
   traces
   | where timestamp > ago(1h)
   | where message contains "Evento" or message contains "Event Grid"
   | order by timestamp desc
   | take 50
   ```

6. **O busca por tipo de evento**:
   ```kusto
   traces
   | where timestamp > ago(1h)
   | where message contains "ProductoCreado" or message contains "BodegaCreada"
   | order by timestamp desc
   ```

---

## 🧪 Opción 5: Test Manual con Event Grid Viewer (Desarrollo)

Para testing en desarrollo, puedes usar el Event Grid Viewer:

### Setup:

1. **Crea un Event Grid Viewer**:
   ```bash
   # Deploy Event Grid Viewer (aplicación web simple)
   az deployment group create \
     --resource-group agranelos \
     --template-uri "https://raw.githubusercontent.com/Azure-Samples/azure-event-grid-viewer/master/azuredeploy.json" \
     --parameters siteName=agranelos-eventviewer
   ```

2. **Crea una Event Subscription apuntando al viewer**:
   - En tu Event Grid Topic
   - Click "New Event Subscription"
   - Endpoint Type: **Web Hook**
   - Endpoint: `https://agranelos-eventviewer.azurewebsites.net/api/updates`

3. **Abre el viewer**: https://agranelos-eventviewer.azurewebsites.net

4. **Genera eventos** y los verás aparecer en tiempo real en el viewer

---

## 🔍 Verificación Paso a Paso

### ✅ Checklist de Verificación:

#### 1. ¿Los eventos están siendo PUBLICADOS?
- [ ] Ve a Event Grid Topic → Metrics → "Published Events"
- [ ] Verifica que el número sea > 0 después de crear/actualizar/eliminar

#### 2. ¿Los eventos están siendo RECIBIDOS por subscriptions?
- [ ] Ve a Event Subscription → Metrics → "Delivery Succeeded"
- [ ] Verifica que el número coincida con eventos publicados

#### 3. ¿Hay eventos fallidos?
- [ ] Ve a Event Grid Topic → Metrics → "Publish Failed"
- [ ] Ve a Event Subscription → Metrics → "Dead Lettered Events"
- [ ] Idealmente ambos deberían estar en 0

#### 4. ¿Las funciones consumidoras están procesando?
- [ ] Ve a Function App → Log stream
- [ ] Genera un evento
- [ ] Deberías ver logs de la función consumidora

---

## 🎯 Ejemplo Práctico: Flujo Completo

### Paso 1: Genera un evento
```bash
curl -X POST https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/productos \
  -u user:myStrongPassword123 \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Test Event Grid",
    "descripcion": "Verificar eventos",
    "precio": 50.00,
    "cantidad": 100
  }'
```

### Paso 2: Verifica en Azure Portal

#### En Event Grid Topic (agranelosEventGrid):
1. Metrics → "Published Events" → Debería incrementar en 1
2. Si no incrementa: ❌ El problema está en la publicación (revisar EVENT_GRID_KEY)

#### En Event Subscriptions:
1. Click en "functionAgranelosHub" → Metrics → "Delivery Succeeded"
2. Debería incrementar en 1
3. Si Published Events = 1 pero Delivery = 0: ❌ El problema está en la subscription o la función consumidora

#### En Function App:
1. Log stream → Deberías ver:
   ```
   === Evento ProductoCreado Recibido ===
   Event Type: Agranelos.Inventario.ProductoCreado
   ```
2. Si no ves nada: ❌ La función consumidora no está activa o tiene errores

---

## 🚨 Troubleshooting

### No veo eventos en "Published Events"

**Causas posibles**:
- ✅ EVENT_GRID_KEY incorrecta en local.settings.json
- ✅ EVENT_GRID_ENDPOINT incorrecto
- ✅ Azure Functions no están corriendo
- ✅ El código no está llamando a EventGridPublisher

**Solución**:
1. Verifica local.settings.json
2. Revisa logs de Azure Functions locales
3. Busca mensajes: "Evento publicado: ..." o "Error publicando evento..."

### Veo eventos en "Published Events" pero no en "Delivery Succeeded"

**Causas posibles**:
- ✅ Event Subscription mal configurada
- ✅ Función consumidora no existe o está detenida
- ✅ Filtros en la subscription no coinciden con el evento

**Solución**:
1. Verifica que la Function App consumidora esté corriendo
2. Revisa los filtros en Event Subscription
3. Chequea "Dead Lettered Events"

### Veo "Dead Lettered Events" > 0

**Causas posibles**:
- ✅ La función consumidora está fallando
- ✅ Timeout en el procesamiento
- ✅ Endpoint de la función no está disponible

**Solución**:
1. Ve a Function App → Logs
2. Busca errores en las funciones consumidoras
3. Revisa que las funciones estén habilitadas

---

## 📸 Navegación Rápida en Azure Portal

### Para ir directo a métricas:

1. **Event Grid Topic Metrics**:
   ```
   Home → Resource groups → agranelos → agranelosEventGrid → Metrics
   ```

2. **Event Subscription Metrics**:
   ```
   Home → Resource groups → agranelos → agranelosEventGrid → Event Subscriptions → [subscription name] → Metrics
   ```

3. **Function App Logs**:
   ```
   Home → Resource groups → agranelos → [Function App name] → Log stream
   ```

---

## 📊 Dashboard Recomendado

Puedes crear un Dashboard personalizado con estas métricas:

1. En Azure Portal, click en **"Dashboard"** (menú superior)
2. Click en **"New dashboard"** → **"Blank dashboard"**
3. Nombre: "Agranelos Event Grid Monitor"
4. Arrastra widgets de tipo **"Metrics chart"**
5. Configura cada widget con:
   - Resource: agranelosEventGrid
   - Metric: Published Events, Delivery Succeeded, etc.
6. Guarda el dashboard

Ahora tendrás todas las métricas en un solo lugar! 📊

---

## 🔗 Links Directos

Una vez que identifiques tu Event Grid Topic ID, puedes usar estos links directos:

```
# Métricas del Topic
https://portal.azure.com/#@<tenant>/resource/subscriptions/<sub-id>/resourceGroups/agranelos/providers/Microsoft.EventGrid/topics/agranelosEventGrid/metrics

# Event Subscriptions
https://portal.azure.com/#@<tenant>/resource/subscriptions/<sub-id>/resourceGroups/agranelos/providers/Microsoft.EventGrid/topics/agranelosEventGrid/eventSubscriptions
```

---

## 📚 Resumen Visual

```
┌─────────────────────────────────────────────────────────────┐
│  1. Función publica evento → EVENT GRID TOPIC               │
│     ✓ Verifica en: Topic → Metrics → "Published Events"    │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  2. Event Grid enruta evento → EVENT SUBSCRIPTIONS          │
│     ✓ Verifica en: Subscription → Metrics → "Delivery..."  │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  3. Función consumidora procesa evento → LOGS               │
│     ✓ Verifica en: Function App → Log stream               │
└─────────────────────────────────────────────────────────────┘
```

---

**🎯 Recomendación**: Empieza con la **Opción 1 (Metrics)** ya que es la más rápida para confirmar si los eventos están llegando o no.
