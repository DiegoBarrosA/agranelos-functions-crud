---
title: Azure Deployment Guide
layout: default
---

<link rel="stylesheet" href="https://diegobarrosa.github.io/diegobarrosaraya-assets/shared-theme.css">
<link rel="stylesheet" href="https://diegobarrosa.github.io/diegobarrosaraya-assets/shared-footer.css">
<script src="https://diegobarrosa.github.io/diegobarrosaraya-assets/shared-theme.js"></script>

# Guía de Despliegue en Azure

Esta guía te llevará paso a paso por el proceso de despliegue del Sistema de Inventario Agranelos en Azure Cloud.

## Tabla de Contenidos

1. [Prerrequisitos](#prerrequisitos)
2. [Configuración Inicial](#configuración-inicial)
3. [Opción 1: Despliegue Automático](#opción-1-despliegue-automático)
4. [Opción 2: Despliegue Manual](#opción-2-despliegue-manual)
5. [Opción 3: Despliegue con ARM Template](#opción-3-despliegue-con-arm-template)
6. [Configuración Post-Despliegue](#configuración-post-despliegue)
7. [Verificación](#verificación)
8. [Solución de Problemas](#solución-de-problemas)

---

## Prerrequisitos

### Software Requerido

- **Java JDK 11** - [Descargar](https://adoptium.net/)
- **Maven 3.6+** - [Descargar](https://maven.apache.org/download.cgi)
- **Azure CLI** - [Descargar](https://docs.microsoft.com/cli/azure/install-azure-cli)
- **Azure Functions Core Tools** - [Descargar](https://docs.microsoft.com/azure/azure-functions/functions-run-local)
- **Git** - [Descargar](https://git-scm.com/)

### Cuenta Azure

- Una suscripción activa de Azure ([Crear cuenta gratuita](https://azure.microsoft.com/free/))
- Permisos para crear recursos en la suscripción

### Verificar Instalación

```bash
# Verificar Java
java -version  # Debe mostrar Java 11

# Verificar Maven
mvn -version

# Verificar Azure CLI
az --version

# Verificar Azure Functions Core Tools
func --version  # Debe ser 4.x
```

---

## Configuración Inicial

### 1. Clonar el Repositorio

```bash
git clone https://github.com/DiegoBarrosA/agranelos-functions-crud.git
cd agranelos-functions-crud
```

### 2. Iniciar Sesión en Azure

```bash
az login

# Seleccionar la suscripción correcta
az account list --output table
az account set --subscription "SUBSCRIPTION_ID"
```

### 3. Configurar Variables de Entorno

Edita el archivo `azure-deploy.parameters.json`:

```json
{
  "$schema": "https://schema.management.azure.com/schemas/2019-04-01/deploymentParameters.json#",
  "contentVersion": "1.0.0.0",
  "parameters": {
    "functionAppName": {
      "value": "agranelos-inventario-functions"
    },
    "storageAccountName": {
      "value": "agranelosinventario"
    },
    "eventGridTopicName": {
      "value": "agranelos-eventgrid-topic"
    },
    "dbHost": {
      "value": "50.19.86.166"
    },
    "dbName": {
      "value": "inventario_agranelos"
    },
    "dbUser": {
      "value": "postgres"
    },
    "dbPassword": {
      "value": "TU_PASSWORD_AQUI"
    }
  }
}
```

---

## Opción 1: Despliegue Automático

La forma más rápida de desplegar todo el sistema.

### Ejecutar Script de Despliegue

```bash
# Dar permisos de ejecución
chmod +x scripts/deploy-azure.sh

# Ejecutar el script
./scripts/deploy-azure.sh
```

Este script:
1. Crea el grupo de recursos
2. Crea la cuenta de almacenamiento
3. Crea Azure Functions App
4. Crea Event Grid Topic
5. Configura variables de entorno
6. Crea suscripciones a eventos
7. Compila y despliega las funciones

### Tiempo Estimado
⏱️ **15-20 minutos**

---

## Opción 2: Despliegue Manual

Para tener control total sobre cada paso.

### Paso 1: Crear Grupo de Recursos

```bash
az group create \
  --name agranelos-inventario-rg \
  --location eastus
```

### Paso 2: Crear Cuenta de Almacenamiento

```bash
az storage account create \
  --name agranelosinventario \
  --resource-group agranelos-inventario-rg \
  --location eastus \
  --sku Standard_LRS
```

### Paso 3: Crear Plan de Hosting (Consumption)

```bash
az functionapp plan create \
  --name agranelos-functions-plan \
  --resource-group agranelos-inventario-rg \
  --location eastus \
  --sku Y1 \
  --is-linux false
```

### Paso 4: Crear Function App

```bash
az functionapp create \
  --name agranelos-inventario-functions \
  --resource-group agranelos-inventario-rg \
  --storage-account agranelosinventario \
  --plan agranelos-functions-plan \
  --runtime java \
  --runtime-version 11 \
  --functions-version 4
```

### Paso 5: Crear Event Grid Topic

```bash
az eventgrid topic create \
  --name agranelos-eventgrid-topic \
  --resource-group agranelos-inventario-rg \
  --location eastus
```

### Paso 6: Obtener Credenciales

```bash
# Event Grid Endpoint
EVENT_GRID_ENDPOINT=$(az eventgrid topic show \
  --name agranelos-eventgrid-topic \
  --resource-group agranelos-inventario-rg \
  --query "endpoint" \
  --output tsv)

# Event Grid Key
EVENT_GRID_KEY=$(az eventgrid topic key list \
  --name agranelos-eventgrid-topic \
  --resource-group agranelos-inventario-rg \
  --query "key1" \
  --output tsv)

echo "Event Grid Endpoint: $EVENT_GRID_ENDPOINT"
echo "Event Grid Key: $EVENT_GRID_KEY"
```

### Paso 7: Configurar Variables de Entorno

```bash
az functionapp config appsettings set \
  --name agranelos-inventario-functions \
  --resource-group agranelos-inventario-rg \
  --settings \
    EVENT_GRID_ENDPOINT="$EVENT_GRID_ENDPOINT" \
    EVENT_GRID_KEY="$EVENT_GRID_KEY" \
    DB_HOST="50.19.86.166" \
    DB_PORT="5432" \
    DB_NAME="inventario_agranelos" \
    DB_USER="postgres" \
    DB_PASSWORD="TU_PASSWORD" \
    DB_SSL_MODE="disable"
```

### Paso 8: Compilar el Proyecto

```bash
mvn clean package
```

### Paso 9: Desplegar las Funciones

```bash
mvn azure-functions:deploy
```

### Paso 10: Crear Suscripciones a Event Grid

```bash
# Obtener el ID de la Function App
FUNCTION_APP_ID=$(az functionapp show \
  --name agranelos-inventario-functions \
  --resource-group agranelos-inventario-rg \
  --query "id" \
  --output tsv)

# Topic Resource ID
TOPIC_ID=$(az eventgrid topic show \
  --name agranelos-eventgrid-topic \
  --resource-group agranelos-inventario-rg \
  --query id \
  --output tsv)

# Suscripción: ProductoCreado
az eventgrid event-subscription create \
  --name producto-creado-subscription \
  --source-resource-id $TOPIC_ID \
  --endpoint "${FUNCTION_APP_ID}/functions/ProductoCreadoEventHandler" \
  --endpoint-type azurefunction \
  --included-event-types Agranelos.Inventario.ProductoCreado

# Suscripción: ProductoActualizado
az eventgrid event-subscription create \
  --name producto-actualizado-subscription \
  --source-resource-id $TOPIC_ID \
  --endpoint "${FUNCTION_APP_ID}/functions/ProductoActualizadoEventHandler" \
  --endpoint-type azurefunction \
  --included-event-types Agranelos.Inventario.ProductoActualizado

# Suscripción: ProductoEliminado
az eventgrid event-subscription create \
  --name producto-eliminado-subscription \
  --source-resource-id $TOPIC_ID \
  --endpoint "${FUNCTION_APP_ID}/functions/ProductoEliminadoEventHandler" \
  --endpoint-type azurefunction \
  --included-event-types Agranelos.Inventario.ProductoEliminado

# Suscripción: BodegaCreada
az eventgrid event-subscription create \
  --name bodega-creada-subscription \
  --source-resource-id $TOPIC_ID \
  --endpoint "${FUNCTION_APP_ID}/functions/BodegaCreadaEventHandler" \
  --endpoint-type azurefunction \
  --included-event-types Agranelos.Inventario.BodegaCreada

# Suscripción: BodegaActualizada
az eventgrid event-subscription create \
  --name bodega-actualizada-subscription \
  --source-resource-id $TOPIC_ID \
  --endpoint "${FUNCTION_APP_ID}/functions/BodegaActualizadaEventHandler" \
  --endpoint-type azurefunction \
  --included-event-types Agranelos.Inventario.BodegaActualizada

# Suscripción: BodegaEliminada
az eventgrid event-subscription create \
  --name bodega-eliminada-subscription \
  --source-resource-id $TOPIC_ID \
  --endpoint "${FUNCTION_APP_ID}/functions/BodegaEliminadaEventHandler" \
  --endpoint-type azurefunction \
  --included-event-types Agranelos.Inventario.BodegaEliminada
```

---

## Opción 3: Despliegue con ARM Template

Infraestructura como código para despliegue repetible.

### Paso 1: Crear Grupo de Recursos

```bash
az group create \
  --name agranelos-inventario-rg \
  --location eastus
```

### Paso 2: Desplegar con ARM Template

```bash
az deployment group create \
  --resource-group agranelos-inventario-rg \
  --template-file azure-deploy.json \
  --parameters azure-deploy.parameters.json
```

### Paso 3: Desplegar el Código

```bash
mvn clean package
mvn azure-functions:deploy
```

### Paso 4: Configurar Suscripciones (ver Opción 2, Paso 10)

---

## Configuración Post-Despliegue

### 1. Inicializar la Base de Datos

```bash
# Obtener la URL de la Function App
FUNCTION_URL=$(az functionapp show \
  --name agranelos-inventario-functions \
  --resource-group agranelos-inventario-rg \
  --query "defaultHostName" \
  --output tsv)

# Llamar al endpoint de inicialización
curl -X POST "https://${FUNCTION_URL}/api/init"
```

### 2. Configurar CORS (Opcional)

```bash
az functionapp cors add \
  --name agranelos-inventario-functions \
  --resource-group agranelos-inventario-rg \
  --allowed-origins "https://tu-frontend.com"
```

### 3. Habilitar Application Insights

```bash
# Ya está habilitado por defecto, pero puedes verificar
az monitor app-insights component show \
  --app agranelos-inventario-functions-insights \
  --resource-group agranelos-inventario-rg
```

### 4. Configurar Alertas

```bash
# Alerta para alta tasa de errores
az monitor metrics alert create \
  --name "High Error Rate" \
  --resource-group agranelos-inventario-rg \
  --scopes "/subscriptions/{subscription-id}/resourceGroups/agranelos-inventario-rg/providers/Microsoft.Web/sites/agranelos-inventario-functions" \
  --condition "total Failed Requests > 100" \
  --window-size 5m \
  --evaluation-frequency 1m
```

---

## Verificación

### 1. Verificar que las Funciones estén Desplegadas

```bash
az functionapp function list \
  --name agranelos-inventario-functions \
  --resource-group agranelos-inventario-rg \
  --output table
```

### 2. Probar los Endpoints

```bash
# Obtener URL base
FUNCTION_URL=$(az functionapp show \
  --name agranelos-inventario-functions \
  --resource-group agranelos-inventario-rg \
  --query "defaultHostName" \
  --output tsv)

# Probar GET productos
curl "https://${FUNCTION_URL}/api/productos"

# Probar GET bodegas
curl "https://${FUNCTION_URL}/api/bodegas"

# Probar GraphQL
curl -X POST "https://${FUNCTION_URL}/api/graphql" \
  -H "Content-Type: application/json" \
  -d '{"query": "{ productos { id nombre } }"}'
```

### 3. Verificar Event Grid

```bash
# Listar suscripciones
az eventgrid event-subscription list \
  --source-resource-id $(az eventgrid topic show \
    --name agranelos-eventgrid-topic \
    --resource-group agranelos-inventario-rg \
    --query id --output tsv) \
  --output table
```

### 4. Ver Logs en Tiempo Real

```bash
az functionapp log tail \
  --name agranelos-inventario-functions \
  --resource-group agranelos-inventario-rg
```

### 5. Probar Publicación de Eventos

```bash
# Crear un producto (esto debería publicar un evento)
curl -X POST "https://${FUNCTION_URL}/api/productos" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Producto Test",
    "descripcion": "Test desde despliegue",
    "precio": 10.00,
    "cantidadEnStock": 50
  }'

# Verificar que el evento se procesó en los logs
```

---

## Solución de Problemas

### Problema: Función no responde

**Solución:**
```bash
# Verificar estado de la Function App
az functionapp show \
  --name agranelos-inventario-functions \
  --resource-group agranelos-inventario-rg \
  --query "state"

# Reiniciar si es necesario
az functionapp restart \
  --name agranelos-inventario-functions \
  --resource-group agranelos-inventario-rg
```

### Problema: Error de conexión a base de datos

**Solución:**
```bash
# Verificar configuración de conexión
az functionapp config appsettings list \
  --name agranelos-inventario-functions \
  --resource-group agranelos-inventario-rg \
  --query "[?name=='DB_HOST' || name=='DB_PORT' || name=='DB_NAME']"

# Verificar conectividad desde Azure
# Nota: Azure debe poder alcanzar 50.19.86.166:5432
```

### Problema: Eventos no se están procesando

**Solución:**
```bash
# Verificar estado de las suscripciones
az eventgrid event-subscription list \
  --source-resource-id $(az eventgrid topic show \
    --name agranelos-eventgrid-topic \
    --resource-group agranelos-inventario-rg \
    --query id --output tsv)

# Verificar logs de Event Grid
az monitor activity-log list \
  --resource-group agranelos-inventario-rg \
  --namespace Microsoft.EventGrid
```

### Problema: Cold Start muy lento

**Solución:**
```bash
# Considerar cambiar a Premium Plan para reducir cold starts
az functionapp plan create \
  --name agranelos-premium-plan \
  --resource-group agranelos-inventario-rg \
  --location eastus \
  --sku EP1 \
  --is-linux false

# Actualizar Function App para usar el nuevo plan
az functionapp update \
  --name agranelos-inventario-functions \
  --resource-group agranelos-inventario-rg \
  --plan agranelos-premium-plan
```

### Problema: Maven deploy falla

**Solución:**
```bash
# Limpiar y recompilar
mvn clean
rm -rf target/
mvn package

# Verificar que el pom.xml tenga la configuración correcta
# del azure-functions-maven-plugin
```

---

## Comandos Útiles

### Ver información del despliegue

```bash
# URL de la Function App
az functionapp show \
  --name agranelos-inventario-functions \
  --resource-group agranelos-inventario-rg \
  --query "defaultHostName" -o tsv

# Lista de funciones desplegadas
az functionapp function list \
  --name agranelos-inventario-functions \
  --resource-group agranelos-inventario-rg

# Ver configuración
az functionapp config show \
  --name agranelos-inventario-functions \
  --resource-group agranelos-inventario-rg
```

### Gestión de recursos

```bash
# Detener Function App
az functionapp stop \
  --name agranelos-inventario-functions \
  --resource-group agranelos-inventario-rg

# Iniciar Function App
az functionapp start \
  --name agranelos-inventario-functions \
  --resource-group agranelos-inventario-rg

# Eliminar todos los recursos
az group delete \
  --name agranelos-inventario-rg \
  --yes --no-wait
```

---

## Próximos Pasos

Una vez desplegado:

1. Configurar CI/CD con GitHub Actions
2. Implementar autenticación con Azure AD
3. Configurar API Management para gestión avanzada
4. Implementar caché con Azure Redis Cache
5. Configurar backup automático de base de datos
6. Implementar monitoring avanzado con dashboards personalizados

---

## Recursos Adicionales

- [Documentación de Azure Functions](https://docs.microsoft.com/azure/azure-functions/)
- [Documentación de Event Grid](https://docs.microsoft.com/azure/event-grid/)
- [Application Insights](https://docs.microsoft.com/azure/azure-monitor/app/app-insights-overview)
- [ARM Templates](https://docs.microsoft.com/azure/azure-resource-manager/templates/)

---

## Soporte

Para preguntas o problemas:
- Crear un issue en el repositorio de GitHub
- Revisar la documentación en `/docs`
- Contactar al equipo de desarrollo

---

**¡Felicitaciones! Tu Sistema de Inventario Agranelos está ahora en la nube.** ☁️🎉
