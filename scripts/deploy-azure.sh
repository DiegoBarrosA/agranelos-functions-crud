#!/bin/bash

# Script de despliegue para Azure Functions y Event Grid
# Sistema de Inventario Agranelos

set -e

echo "==================================="
echo "Despliegue Sistema Inventario Agranelos"
echo "==================================="

# Variables de configuración
RESOURCE_GROUP="agranelos-inventario-rg"
LOCATION="eastus"
FUNCTION_APP_NAME="agranelos-inventario-functions"
STORAGE_ACCOUNT="agranelosinventario"
EVENT_GRID_TOPIC="agranelos-eventgrid-topic"
DATABASE_SERVER="agranelos-postgresql"
DATABASE_NAME="inventario_agranelos"

echo ""
echo "Paso 1: Crear grupo de recursos"
az group create \
  --name $RESOURCE_GROUP \
  --location $LOCATION

echo ""
echo "Paso 2: Crear cuenta de almacenamiento"
az storage account create \
  --name $STORAGE_ACCOUNT \
  --resource-group $RESOURCE_GROUP \
  --location $LOCATION \
  --sku Standard_LRS

echo ""
echo "Paso 3: Crear Azure Functions App"
az functionapp create \
  --name $FUNCTION_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --storage-account $STORAGE_ACCOUNT \
  --consumption-plan-location $LOCATION \
  --runtime java \
  --runtime-version 11 \
  --functions-version 4 \
  --os-type Windows

echo ""
echo "Paso 4: Crear Event Grid Topic"
az eventgrid topic create \
  --name $EVENT_GRID_TOPIC \
  --resource-group $RESOURCE_GROUP \
  --location $LOCATION

echo ""
echo "Paso 5: Obtener credenciales de Event Grid"
EVENT_GRID_ENDPOINT=$(az eventgrid topic show \
  --name $EVENT_GRID_TOPIC \
  --resource-group $RESOURCE_GROUP \
  --query "endpoint" \
  --output tsv)

EVENT_GRID_KEY=$(az eventgrid topic key list \
  --name $EVENT_GRID_TOPIC \
  --resource-group $RESOURCE_GROUP \
  --query "key1" \
  --output tsv)

echo ""
echo "Paso 6: Crear PostgreSQL Database Server (opcional)"
echo "Nota: Comentado porque ya tienes una base de datos en AWS"
# az postgres server create \
#   --name $DATABASE_SERVER \
#   --resource-group $RESOURCE_GROUP \
#   --location $LOCATION \
#   --admin-user adminuser \
#   --admin-password "SecurePassword123!" \
#   --sku-name B_Gen5_1 \
#   --version 11

echo ""
echo "Paso 7: Configurar variables de entorno en Function App"
az functionapp config appsettings set \
  --name $FUNCTION_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --settings \
    EVENT_GRID_ENDPOINT="$EVENT_GRID_ENDPOINT" \
    EVENT_GRID_KEY="$EVENT_GRID_KEY" \
    DB_HOST="50.19.86.166" \
    DB_PORT="5432" \
    DB_NAME="$DATABASE_NAME" \
    DB_USER="postgres" \
    DB_PASSWORD="JUq2Uh9giFapgp8Vk7q8bWnhwPartBSIbyvgYmKPtGYmGAEIKrrzZYkPVRg4xf9cMsMUgA47JU9fYLMAI66fbatWKB2i5XVJ3JiMkb8NLFwGQgUoaeVa8c7PvuMrM5F4" \
    DB_SSL_MODE="disable"

echo ""
echo "Paso 8: Crear suscripciones a Event Grid"
FUNCTION_APP_ID=$(az functionapp show \
  --name $FUNCTION_APP_NAME \
  --resource-group $RESOURCE_GROUP \
  --query "id" \
  --output tsv)

# Suscripción para ProductoCreado
az eventgrid event-subscription create \
  --name producto-creado-subscription \
  --source-resource-id $(az eventgrid topic show --name $EVENT_GRID_TOPIC --resource-group $RESOURCE_GROUP --query id --output tsv) \
  --endpoint "${FUNCTION_APP_ID}/functions/ProductoCreadoEventHandler" \
  --endpoint-type azurefunction \
  --included-event-types Agranelos.Inventario.ProductoCreado

# Suscripción para ProductoActualizado
az eventgrid event-subscription create \
  --name producto-actualizado-subscription \
  --source-resource-id $(az eventgrid topic show --name $EVENT_GRID_TOPIC --resource-group $RESOURCE_GROUP --query id --output tsv) \
  --endpoint "${FUNCTION_APP_ID}/functions/ProductoActualizadoEventHandler" \
  --endpoint-type azurefunction \
  --included-event-types Agranelos.Inventario.ProductoActualizado

# Suscripción para ProductoEliminado
az eventgrid event-subscription create \
  --name producto-eliminado-subscription \
  --source-resource-id $(az eventgrid topic show --name $EVENT_GRID_TOPIC --resource-group $RESOURCE_GROUP --query id --output tsv) \
  --endpoint "${FUNCTION_APP_ID}/functions/ProductoEliminadoEventHandler" \
  --endpoint-type azurefunction \
  --included-event-types Agranelos.Inventario.ProductoEliminado

# Suscripción para BodegaCreada
az eventgrid event-subscription create \
  --name bodega-creada-subscription \
  --source-resource-id $(az eventgrid topic show --name $EVENT_GRID_TOPIC --resource-group $RESOURCE_GROUP --query id --output tsv) \
  --endpoint "${FUNCTION_APP_ID}/functions/BodegaCreadaEventHandler" \
  --endpoint-type azurefunction \
  --included-event-types Agranelos.Inventario.BodegaCreada

# Suscripción para BodegaActualizada
az eventgrid event-subscription create \
  --name bodega-actualizada-subscription \
  --source-resource-id $(az eventgrid topic show --name $EVENT_GRID_TOPIC --resource-group $RESOURCE_GROUP --query id --output tsv) \
  --endpoint "${FUNCTION_APP_ID}/functions/BodegaActualizadaEventHandler" \
  --endpoint-type azurefunction \
  --included-event-types Agranelos.Inventario.BodegaActualizada

# Suscripción para BodegaEliminada
az eventgrid event-subscription create \
  --name bodega-eliminada-subscription \
  --source-resource-id $(az eventgrid topic show --name $EVENT_GRID_TOPIC --resource-group $RESOURCE_GROUP --query id --output tsv) \
  --endpoint "${FUNCTION_APP_ID}/functions/BodegaEliminadaEventHandler" \
  --endpoint-type azurefunction \
  --included-event-types Agranelos.Inventario.BodegaEliminada

echo ""
echo "Paso 9: Compilar y desplegar Azure Functions"
mvn clean package
mvn azure-functions:deploy

echo ""
echo "==================================="
echo "Despliegue completado exitosamente!"
echo "==================================="
echo ""
echo "Información del despliegue:"
echo "- Resource Group: $RESOURCE_GROUP"
echo "- Function App: $FUNCTION_APP_NAME"
echo "- Event Grid Topic: $EVENT_GRID_TOPIC"
echo "- Event Grid Endpoint: $EVENT_GRID_ENDPOINT"
echo ""
echo "URLs de las funciones:"
echo "- REST API: https://${FUNCTION_APP_NAME}.azurewebsites.net/api"
echo "- GraphQL: https://${FUNCTION_APP_NAME}.azurewebsites.net/api/graphql"
echo ""
echo "Para ver los logs:"
echo "az functionapp log tail --name $FUNCTION_APP_NAME --resource-group $RESOURCE_GROUP"
echo ""
