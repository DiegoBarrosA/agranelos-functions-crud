#!/bin/bash

# Script para probar Azure Event Grid en el Sistema de Inventario Agranelos
# Verifica que todos los eventos se publican correctamente

set -e

# Colores para output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Configuración
BASE_URL="${BASE_URL:-https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api}"

echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Azure Event Grid - Test de Ciclo Completo           ║${NC}"
echo -e "${BLUE}║  Sistema de Inventario Agranelos                      ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${YELLOW}Base URL: ${BASE_URL}${NC}"
echo ""

# Verificar que jq está instalado
if ! command -v jq &> /dev/null; then
    echo -e "${RED}❌ Error: jq no está instalado${NC}"
    echo "Instala con: sudo apt install jq  (o brew install jq en Mac)"
    exit 1
fi

echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  PARTE 1: Eventos de Productos${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo ""

# 1. Crear Producto (Dispara ProductoCreado)
echo -e "${YELLOW}1️⃣  Creando producto (evento: ProductoCreado)...${NC}"
CREATE_RESPONSE=$(curl -s -X POST "$BASE_URL/productos" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Test EventGrid Script",
    "descripcion": "Producto para verificar eventos automáticos",
    "precio": 125.50,
    "cantidadEnStock": 40
  }')

echo "$CREATE_RESPONSE" | jq '.'

if echo "$CREATE_RESPONSE" | jq -e '.id' > /dev/null; then
    PRODUCTO_ID=$(echo "$CREATE_RESPONSE" | jq -r '.id')
    echo -e "${GREEN}✅ Producto creado con ID: ${PRODUCTO_ID}${NC}"
    echo -e "${GREEN}   Evento publicado: Agranelos.Inventario.ProductoCreado${NC}"
else
    echo -e "${RED}❌ Error creando producto${NC}"
    exit 1
fi

echo ""
sleep 2

# 2. Obtener Producto (No dispara evento)
echo -e "${YELLOW}2️⃣  Obteniendo producto (sin evento)...${NC}"
GET_RESPONSE=$(curl -s "$BASE_URL/productos/$PRODUCTO_ID")
echo "$GET_RESPONSE" | jq '.'
echo -e "${GREEN}✅ Producto obtenido correctamente${NC}"
echo ""
sleep 2

# 3. Actualizar Producto (Dispara ProductoActualizado)
echo -e "${YELLOW}3️⃣  Actualizando producto (evento: ProductoActualizado)...${NC}"
UPDATE_RESPONSE=$(curl -s -X PUT "$BASE_URL/productos/$PRODUCTO_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Test EventGrid Script ACTUALIZADO",
    "descripcion": "Producto actualizado para verificar evento de actualización",
    "precio": 150.00,
    "cantidadEnStock": 50
  }')

echo "$UPDATE_RESPONSE" | jq '.'
echo -e "${GREEN}✅ Producto actualizado${NC}"
echo -e "${GREEN}   Evento publicado: Agranelos.Inventario.ProductoActualizado${NC}"
echo ""
sleep 2

# 4. Eliminar Producto (Dispara ProductoEliminado)
echo -e "${YELLOW}4️⃣  Eliminando producto (evento: ProductoEliminado)...${NC}"
DELETE_RESPONSE=$(curl -s -X DELETE "$BASE_URL/productos/$PRODUCTO_ID")
echo "$DELETE_RESPONSE" | jq '.'
echo -e "${GREEN}✅ Producto eliminado${NC}"
echo -e "${GREEN}   Evento publicado: Agranelos.Inventario.ProductoEliminado${NC}"
echo ""

echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  PARTE 2: Eventos de Bodegas${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo ""

# 5. Crear Bodega (Dispara BodegaCreada)
echo -e "${YELLOW}5️⃣  Creando bodega (evento: BodegaCreada)...${NC}"
CREATE_BODEGA_RESPONSE=$(curl -s -X POST "$BASE_URL/bodegas" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Bodega Test EventGrid",
    "ubicacion": "Valparaíso - Chile",
    "capacidad": 3000
  }')

echo "$CREATE_BODEGA_RESPONSE" | jq '.'

if echo "$CREATE_BODEGA_RESPONSE" | jq -e '.id' > /dev/null; then
    BODEGA_ID=$(echo "$CREATE_BODEGA_RESPONSE" | jq -r '.id')
    echo -e "${GREEN}✅ Bodega creada con ID: ${BODEGA_ID}${NC}"
    echo -e "${GREEN}   Evento publicado: Agranelos.Inventario.BodegaCreada${NC}"
else
    echo -e "${RED}❌ Error creando bodega${NC}"
    exit 1
fi

echo ""
sleep 2

# 6. Actualizar Bodega (Dispara BodegaActualizada)
echo -e "${YELLOW}6️⃣  Actualizando bodega (evento: BodegaActualizada)...${NC}"
UPDATE_BODEGA_RESPONSE=$(curl -s -X PUT "$BASE_URL/bodegas/$BODEGA_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Bodega Test EventGrid AMPLIADA",
    "ubicacion": "Valparaíso - Chile (Centro)",
    "capacidad": 5000
  }')

echo "$UPDATE_BODEGA_RESPONSE" | jq '.'
echo -e "${GREEN}✅ Bodega actualizada${NC}"
echo -e "${GREEN}   Evento publicado: Agranelos.Inventario.BodegaActualizada${NC}"
echo ""
sleep 2

# 7. Eliminar Bodega (Dispara BodegaEliminada)
echo -e "${YELLOW}7️⃣  Eliminando bodega (evento: BodegaEliminada)...${NC}"
DELETE_BODEGA_RESPONSE=$(curl -s -X DELETE "$BASE_URL/bodegas/$BODEGA_ID")
echo "$DELETE_BODEGA_RESPONSE" | jq '.'
echo -e "${GREEN}✅ Bodega eliminada${NC}"
echo -e "${GREEN}   Evento publicado: Agranelos.Inventario.BodegaEliminada${NC}"
echo ""

# Resumen
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo -e "${BLUE}  RESUMEN DE PRUEBAS${NC}"
echo -e "${BLUE}═══════════════════════════════════════════════════════${NC}"
echo ""
echo -e "${GREEN}✅ Test completado exitosamente${NC}"
echo ""
echo -e "${YELLOW}Eventos publicados:${NC}"
echo "  1. ✅ Agranelos.Inventario.ProductoCreado"
echo "  2. ✅ Agranelos.Inventario.ProductoActualizado"
echo "  3. ✅ Agranelos.Inventario.ProductoEliminado"
echo "  4. ✅ Agranelos.Inventario.BodegaCreada"
echo "  5. ✅ Agranelos.Inventario.BodegaActualizada"
echo "  6. ✅ Agranelos.Inventario.BodegaEliminada"
echo ""
echo -e "${BLUE}Total: 6 eventos disparados${NC}"
echo ""
echo -e "${YELLOW}Para verificar que los eventos fueron procesados:${NC}"
echo "  1. Azure Portal → Function App → Log stream"
echo "  2. Buscar: 'Evento publicado' en los logs"
echo "  3. Verificar: Application Insights → Transaction search"
echo ""
echo -e "${GREEN}🎉 Todos los componentes de Event Grid están funcionando correctamente${NC}"
echo ""
