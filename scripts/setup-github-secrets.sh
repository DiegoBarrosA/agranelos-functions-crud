#!/bin/bash

# Script para configurar GitHub Secrets desde el archivo .env
# Requiere GitHub CLI (gh) instalado y autenticado

set -e

echo "🔐 Configurando GitHub Secrets desde .env..."

# Cargar variables desde .env
if [ ! -f .env ]; then
    echo "❌ Archivo .env no encontrado"
    exit 1
fi

source .env

# Verificar que gh CLI está instalado y autenticado
if ! command -v gh &> /dev/null; then
    echo "❌ GitHub CLI (gh) no está instalado"
    echo "Instala desde: https://cli.github.com/"
    exit 1
fi

# Verificar autenticación
if ! gh auth status &> /dev/null; then
    echo "❌ No estás autenticado en GitHub CLI"
    echo "Ejecuta: gh auth login"
    exit 1
fi

echo "✅ GitHub CLI verificado"

# Configurar secrets
echo "📝 Configurando secrets en el repositorio..."

gh secret set DB_HOST -b"$DB_HOST"
gh secret set DB_PORT -b"$DB_PORT"
gh secret set DB_NAME -b"$DB_NAME"
gh secret set DB_USER -b"$DB_USER"
gh secret set DB_PASSWORD -b"$DB_PASSWORD"

echo "✅ Secrets configurados exitosamente:"
echo "   - DB_HOST"
echo "   - DB_PORT"
echo "   - DB_NAME"
echo "   - DB_USER"
echo "   - DB_PASSWORD"

echo ""
echo "🎉 Configuración completa!"
echo "Los workflows de GitHub Actions ahora tienen acceso a las credenciales de la base de datos."
