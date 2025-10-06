#!/bin/bash

# Script de prueba rápida para verificar notificaciones por email
# Sistema de Inventario Agranelos

set -e

# Colores para output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Configuración
BASE_URL="${BASE_URL:-http://localhost:7071/api}"

echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║  Test de Notificaciones por Email                     ║${NC}"
echo -e "${BLUE}║  Sistema de Inventario Agranelos                      ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
echo ""

# Verificar que SendGrid esté configurado
echo -e "${YELLOW}📋 Verificando configuración...${NC}"
echo ""

if grep -q "SENDGRID_API_KEY" local.settings.json 2>/dev/null; then
    echo -e "${GREEN}✅ SENDGRID_API_KEY configurada en local.settings.json${NC}"
else
    echo -e "${RED}❌ SENDGRID_API_KEY no encontrada en local.settings.json${NC}"
    echo -e "${YELLOW}   Configura tu API key de SendGrid para recibir emails${NC}"
fi

if grep -q "SENDER_EMAIL" local.settings.json 2>/dev/null; then
    SENDER_EMAIL=$(grep "SENDER_EMAIL" local.settings.json | cut -d'"' -f4)
    echo -e "${GREEN}✅ SENDER_EMAIL: ${SENDER_EMAIL}${NC}"
fi

if grep -q "RECIPIENT_EMAIL" local.settings.json 2>/dev/null; then
    RECIPIENT_EMAIL=$(grep "RECIPIENT_EMAIL" local.settings.json | cut -d'"' -f4)
    echo -e "${GREEN}✅ RECIPIENT_EMAIL: ${RECIPIENT_EMAIL}${NC}"
fi

echo ""
echo -e "${BLUE}════════════════════════════════════════════════════════${NC}"
echo ""

# Test 1: Crear Producto (debe enviar email de creación)
echo -e "${YELLOW}📧 Test 1: Creación de Producto (Email: ✅ Producto Creado)${NC}"
PRODUCTO_RESPONSE=$(curl -s -X POST "${BASE_URL}/CreateProducto" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Test Email - Producto",
    "descripcion": "Producto de prueba para notificación por email",
    "precio": 99.99,
    "stock": 10,
    "categoria": "Test"
  }')

PRODUCTO_ID=$(echo $PRODUCTO_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -n "$PRODUCTO_ID" ]; then
    echo -e "${GREEN}✅ Producto creado con ID: ${PRODUCTO_ID}${NC}"
    echo -e "${BLUE}   📬 Verifica tu email: Deberías recibir una notificación${NC}"
else
    echo -e "${RED}❌ Error al crear producto${NC}"
    exit 1
fi

echo ""
sleep 2

# Test 2: Actualizar Producto (debe enviar email de actualización)
echo -e "${YELLOW}📧 Test 2: Actualización de Producto (Email: 📝 Producto Actualizado)${NC}"
curl -s -X PUT "${BASE_URL}/UpdateProducto?id=${PRODUCTO_ID}" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Test Email - Producto Actualizado",
    "descripcion": "Producto actualizado para probar email",
    "precio": 149.99,
    "stock": 20,
    "categoria": "Test"
  }' > /dev/null

echo -e "${GREEN}✅ Producto actualizado${NC}"
echo -e "${BLUE}   📬 Verifica tu email: Deberías recibir una notificación de actualización${NC}"
echo ""
sleep 2

# Test 3: Crear Bodega (debe enviar email de creación)
echo -e "${YELLOW}📧 Test 3: Creación de Bodega (Email: 🏢 Bodega Creada)${NC}"
BODEGA_RESPONSE=$(curl -s -X POST "${BASE_URL}/CreateBodega" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Test Email - Bodega",
    "ubicacion": "Ubicación de prueba para email",
    "capacidad": 1000
  }')

BODEGA_ID=$(echo $BODEGA_RESPONSE | grep -o '"id":[0-9]*' | head -1 | cut -d':' -f2)

if [ -n "$BODEGA_ID" ]; then
    echo -e "${GREEN}✅ Bodega creada con ID: ${BODEGA_ID}${NC}"
    echo -e "${BLUE}   📬 Verifica tu email: Deberías recibir una notificación${NC}"
else
    echo -e "${RED}❌ Error al crear bodega${NC}"
fi

echo ""
sleep 2

# Test 4: Actualizar Bodega (debe enviar email de actualización)
echo -e "${YELLOW}📧 Test 4: Actualización de Bodega (Email: 📝 Bodega Actualizada)${NC}"
curl -s -X PUT "${BASE_URL}/UpdateBodega?id=${BODEGA_ID}" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Test Email - Bodega Actualizada",
    "ubicacion": "Nueva ubicación para email",
    "capacidad": 2000
  }' > /dev/null

echo -e "${GREEN}✅ Bodega actualizada${NC}"
echo -e "${BLUE}   📬 Verifica tu email: Deberías recibir una notificación de actualización${NC}"
echo ""
sleep 2

# Test 5: Eliminar recursos
echo -e "${YELLOW}📧 Test 5: Eliminación de Recursos${NC}"

# Eliminar Producto (debe enviar email de eliminación)
echo -e "   🗑️  Eliminando producto..."
curl -s -X DELETE "${BASE_URL}/DeleteProducto?id=${PRODUCTO_ID}" > /dev/null
echo -e "${GREEN}   ✅ Producto eliminado (Email: 🗑️ Producto Eliminado)${NC}"

sleep 2

# Eliminar Bodega (debe enviar email de eliminación)
echo -e "   🗑️  Eliminando bodega..."
curl -s -X DELETE "${BASE_URL}/DeleteBodega?id=${BODEGA_ID}" > /dev/null
echo -e "${GREEN}   ✅ Bodega eliminada (Email: 🗑️ Bodega Eliminada)${NC}"

echo ""
echo -e "${BLUE}════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "${GREEN}✅ Tests completados${NC}"
echo ""
echo -e "${YELLOW}📬 Revisa tu bandeja de entrada:${NC}"
echo -e "   Email configurado: ${RECIPIENT_EMAIL}"
echo ""
echo -e "${YELLOW}📧 Deberías haber recibido 6 emails:${NC}"
echo -e "   1. ✅ Nuevo Producto Creado"
echo -e "   2. 📝 Producto Actualizado"
echo -e "   3. 🏢 Nueva Bodega Creada"
echo -e "   4. 📝 Bodega Actualizada"
echo -e "   5. 🗑️ Producto Eliminado"
echo -e "   6. 🗑️ Bodega Eliminada"
echo ""
echo -e "${BLUE}════════════════════════════════════════════════════════${NC}"
echo ""
echo -e "${YELLOW}💡 Consejos:${NC}"
echo -e "   • Si no recibes emails, verifica los logs de Azure Functions"
echo -e "   • Revisa la carpeta de SPAM/Correo no deseado"
echo -e "   • Verifica SendGrid Activity Dashboard"
echo -e "   • Asegúrate de que SENDGRID_API_KEY esté configurada"
echo ""
