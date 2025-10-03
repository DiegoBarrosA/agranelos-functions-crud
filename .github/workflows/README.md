# 🔄 CI/CD Workflows

Este proyecto utiliza GitHub Actions para automatizar el proceso de construcción, testing y despliegue.

## 📋 Workflows Disponibles

### 1. CI - Build and Test (`ci-test.yml`)

**Propósito**: Verificar que el código compila correctamente y que todos los componentes están en su lugar.

**Se ejecuta en**:
- Push a `main` o `develop`
- Pull Requests a `main` o `develop`
- Manual (workflow_dispatch)

**Jobs incluidos**:

#### 🏗️ Build
- Configura Java 11
- Compila el proyecto con Maven
- Genera los artefactos de Azure Functions

#### 🔍 Verify Structure
- Verifica que la estructura de Azure Functions es correcta
- Comprueba que existen todos los archivos necesarios (`host.json`, JAR, etc.)
- Valida que todas las funciones tienen su `function.json`

#### 📡 Check Event Grid Integration
- Verifica que los archivos de integración con Event Grid existen:
  - `EventType.java`
  - `EventGridPublisher.java`
  - `EventGridConsumer.java`
  - `ProductoEventData.java`
  - `BodegaEventData.java`

#### 📦 Check Dependencies
- Analiza el árbol de dependencias de Maven
- Verifica que las dependencias críticas están presentes:
  - Azure Event Grid
  - Azure Identity
  - PostgreSQL
  - GraphQL Java

#### 📚 Documentation Check
- Verifica que toda la documentación necesaria existe:
  - `README.md`
  - `docs/ARQUITECTURA.md`
  - `docs/DEPLOY.md`
  - `azure-deploy.json`
  - Scripts de despliegue

#### 📊 Summary
- Genera un resumen de todos los jobs ejecutados
- Reporta el estado final del pipeline

### 2. Deploy Azure Functions (`deploy-azure.yml`)

**Propósito**: Desplegar automáticamente a Azure cuando se hace push a `main`.

**Se ejecuta en**:
- Push a `main`
- Manual (workflow_dispatch)

**Pasos**:
1. Checkout del código
2. Setup de Java 11
3. Compilación con Maven
4. Despliegue a Azure Functions usando publish profile

## 🚀 Cómo Usar los Workflows

### Ejecutar CI manualmente

1. Ve a la pestaña "Actions" en GitHub
2. Selecciona "CI - Build and Test"
3. Click en "Run workflow"
4. Selecciona la rama y click en "Run workflow"

### Configurar el Despliegue Automático

Para habilitar el despliegue automático a Azure:

#### Paso 1: Obtener el Publish Profile

```bash
# Descargar el publish profile de tu Function App
az functionapp deployment list-publishing-profiles \
  --name agranelos-inventario-functions \
  --resource-group agranelos-inventario-rg \
  --xml
```

#### Paso 2: Agregar como Secret en GitHub

1. Ve a tu repositorio en GitHub
2. Settings → Secrets and variables → Actions
3. Click en "New repository secret"
4. Nombre: `AZURE_FUNCTIONAPP_PUBLISH_PROFILE`
5. Valor: Pega el contenido XML del publish profile
6. Click en "Add secret"

#### Paso 3: Push a Main

```bash
git add .
git commit -m "Enable CI/CD"
git push origin main
```

El workflow se ejecutará automáticamente y desplegará a Azure.

## 📊 Estado de los Workflows

Puedes ver el estado de los workflows en:
- Badge en el README
- Pestaña "Actions" en GitHub
- Notificaciones por email (si están habilitadas)

### Agregar Badge al README

Agrega este código al inicio de tu README.md:

```markdown
![CI](https://github.com/DiegoBarrosA/agranelos-functions-crud/workflows/CI%20-%20Build%20and%20Test/badge.svg)
![Deploy](https://github.com/DiegoBarrosA/agranelos-functions-crud/workflows/Deploy%20Azure%20Functions/badge.svg)
```

## 🐛 Solución de Problemas

### El workflow falla en la compilación

**Causa**: Errores de sintaxis o dependencias faltantes.

**Solución**:
1. Revisa los logs del job "Build"
2. Ejecuta localmente: `mvn clean package`
3. Corrige los errores y haz push nuevamente

### El workflow falla en "Verify Structure"

**Causa**: La estructura de Azure Functions no es correcta.

**Solución**:
1. Verifica que el `pom.xml` tiene configurado correctamente `azure-functions-maven-plugin`
2. Ejecuta localmente: `mvn clean package`
3. Verifica que el directorio `target/azure-functions/` se crea correctamente

### El despliegue falla

**Causa**: Publish profile incorrecto o expirado.

**Solución**:
1. Regenera el publish profile desde Azure
2. Actualiza el secret `AZURE_FUNCTIONAPP_PUBLISH_PROFILE` en GitHub
3. Re-ejecuta el workflow

### Los tests de Event Grid fallan

**Causa**: Archivos de integración con Event Grid no existen.

**Solución**:
1. Verifica que todos los archivos en `src/main/java/com/agranelos/inventario/events/` existen
2. Haz commit de los archivos faltantes
3. Push al repositorio

## 📈 Mejoras Futuras

- [ ] Agregar tests unitarios automatizados
- [ ] Implementar tests de integración con base de datos
- [ ] Agregar análisis de código (SonarQube, CodeQL)
- [ ] Implementar despliegue a múltiples ambientes (dev, staging, prod)
- [ ] Agregar notificaciones a Slack/Teams
- [ ] Implementar rollback automático en caso de fallos

## 📝 Logs y Monitoreo

Los logs de los workflows se guardan por 90 días en GitHub Actions.

Para ver logs detallados:
1. Ve a la pestaña "Actions"
2. Click en el workflow run específico
3. Click en el job que quieres revisar
4. Expande los pasos para ver logs detallados

## 🔒 Seguridad

- Los secrets nunca se exponen en los logs
- Los publish profiles deben rotarse regularmente
- Se recomienda usar Azure Service Principals en lugar de publish profiles para producción

## 📚 Referencias

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Azure Functions GitHub Actions](https://github.com/Azure/functions-action)
- [Maven in GitHub Actions](https://github.com/actions/setup-java)

---

**¿Listo para probar?** Simplemente haz push de tu código y observa cómo GitHub Actions hace todo el trabajo por ti. 🚀
