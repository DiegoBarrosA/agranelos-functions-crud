# CI/CD Workflows# 🔄 CI/CD Workflows



This repository includes automated continuous integration and deployment workflows using GitHub Actions.Este proyecto utiliza GitHub Actions para automatizar el proceso de construcción, testing y despliegue.



## Workflow Overview## 📋 Workflows Disponibles



### Main CI Pipeline (`ci-test.yml`)### 1. CI - Build and Test (`ci-test.yml`)



The main CI pipeline runs on every push and pull request to ensure code quality and functionality.**Propósito**: Verificar que el código compila correctamente y que todos los componentes están en su lugar.



```mermaid**Se ejecuta en**:

graph LR- Push a `main` o `develop`

    A[Push/PR] --> B[Build]- Pull Requests a `main` o `develop`

    B --> C[Verify Functions]- Manual (workflow_dispatch)

    B --> D[Check Dependencies] 

    B --> E[Documentation Check]**Jobs incluidos**:

    C --> F[Event Grid Check]

    D --> G[Test Summary]#### 🏗️ Build

    E --> G- Configura Java 11

    F --> G- Compila el proyecto con Maven

```- Genera los artefactos de Azure Functions



#### Build#### 🔍 Verify Structure

- **Environment**: Ubuntu Latest- Verifica que la estructura de Azure Functions es correcta

- **Java Version**: 11- Comprueba que existen todos los archivos necesarios (`host.json`, JAR, etc.)

- **Build Tool**: Maven- Valida que todas las funciones tienen su `function.json`

- **Actions**:

  - Checkout repository#### 📡 Check Event Grid Integration

  - Setup Java JDK- Verifica que los archivos de integración con Event Grid existen:

  - Display Maven version  - `EventType.java`

  - Build with Maven (skip tests)  - `EventGridPublisher.java`

  - List build artifacts  - `EventGridConsumer.java`

  - `ProductoEventData.java`

#### Verify Azure Functions Structure  - `BodegaEventData.java`

- **Purpose**: Validate Azure Functions deployment structure

- **Checks**:#### 📦 Check Dependencies

  - Azure Functions directory exists- Analiza el árbol de dependencias de Maven

  - Required files present (`host.json`, `local.settings.json`)- Verifica que las dependencias críticas están presentes:

  - Function directories and configuration files  - Azure Event Grid

  - Azure Identity

#### Check Dependencies  - PostgreSQL

- **Purpose**: Ensure all Maven dependencies are properly resolved  - GraphQL Java

- **Actions**:

  - Analyze dependency tree#### 📚 Documentation Check

  - Check for conflicts- Verifica que toda la documentación necesaria existe:

  - Validate critical dependencies  - `README.md`

  - `docs/ARQUITECTURA.md`

#### Documentation Check  - `docs/DEPLOY.md`

- **Purpose**: Verify required documentation files exist  - `azure-deploy.json`

- **Files Checked**:  - Scripts de despliegue

  - `README.md`

  - `docs/ARQUITECTURA.md`#### 📊 Summary

  - `docs/DEPLOY.md`- Genera un resumen de todos los jobs ejecutados

  - `azure-deploy.json`- Reporta el estado final del pipeline

  - `azure-deploy.parameters.json`

### 2. Deploy Azure Functions (`deploy-azure.yml`)

#### Summary

- **Purpose**: Provide overall build status**Propósito**: Desplegar automáticamente a Azure cuando se hace push a `main`.

- **Actions**:

  - Display success/failure status**Se ejecuta en**:

  - Generate summary report- Push a `main`

- Manual (workflow_dispatch)

### Event Grid Integration Check

**Pasos**:

Separate job to verify Event Grid integration components:1. Checkout del código

- Event type definitions2. Setup de Java 11

- Publisher implementation3. Compilación con Maven

- Consumer implementation4. Despliegue a Azure Functions usando publish profile



## How to Use the Workflows## 🚀 Cómo Usar los Workflows



### Automatic Execution### Ejecutar CI manualmente

Workflows run automatically on:

- Push to `main`, `develop`, or `sumativa-3-staging` branches1. Ve a la pestaña "Actions" en GitHub

- Pull requests to these branches2. Selecciona "CI - Build and Test"

- Manual trigger via `workflow_dispatch`3. Click en "Run workflow"

4. Selecciona la rama y click en "Run workflow"

### Manual Execution

1. Go to the "Actions" tab in the repository### Configurar el Despliegue Automático

2. Select the desired workflow

3. Click "Run workflow"Para habilitar el despliegue automático a Azure:

4. Choose the branch and click "Run workflow"

#### Paso 1: Obtener el Publish Profile

### Monitoring Results

1. **GitHub Interface**: Check the "Actions" tab```bash

2. **Badge Status**: View README badges for quick status# Descargar el publish profile de tu Function App

3. **Email Notifications**: Configure in repository settingsaz functionapp deployment list-publishing-profiles \

4. **Slack/Teams**: Set up webhooks for team notifications  --name agranelos-inventario-functions \

  --resource-group agranelos-inventario-rg \

### Customizing Workflows  --xml

```

#### Environment Variables

Modify the `env` section in workflow files:#### Paso 2: Agregar como Secret en GitHub

```yaml

env:1. Ve a tu repositorio en GitHub

  JAVA_VERSION: '11'2. Settings → Secrets and variables → Actions

  MAVEN_OPTS: '-Xmx1024m'3. Click en "New repository secret"

```4. Nombre: `AZURE_FUNCTIONAPP_PUBLISH_PROFILE`

5. Valor: Pega el contenido XML del publish profile

#### Adding Steps6. Click en "Add secret"

Add new steps in the `steps` section:

```yaml#### Paso 3: Push a Main

- name: 'Your custom step'

  run: |```bash

    echo "Custom command here"git add .

```git commit -m "Enable CI/CD"

git push origin main

#### Conditional Execution```

Use conditions for specific scenarios:

```yamlEl workflow se ejecutará automáticamente y desplegará a Azure.

- name: 'Deploy to staging'

  if: github.ref == 'refs/heads/develop'## 📊 Estado de los Workflows

  run: |

    # Deployment commandsPuedes ver el estado de los workflows en:

```- Badge en el README

- Pestaña "Actions" en GitHub

## Workflow Status- Notificaciones por email (si están habilitadas)



### Build Status### Agregar Badge al README

Current status of the main CI pipeline.

Agrega este código al inicio de tu README.md:

### Success Criteria

All jobs must pass for successful pipeline execution:```markdown

- Build completes without errors![CI](https://github.com/DiegoBarrosA/agranelos-functions-crud/workflows/CI%20-%20Build%20and%20Test/badge.svg)

- All Azure Functions directories exist![Deploy](https://github.com/DiegoBarrosA/agranelos-functions-crud/workflows/Deploy%20Azure%20Functions/badge.svg)

- Dependencies resolve correctly```

- Documentation files present

- Event Grid integration validated## 🐛 Solución de Problemas



### Failure Handling### El workflow falla en la compilación

When workflows fail:

1. **Check logs** in the Actions tab**Causa**: Errores de sintaxis o dependencias faltantes.

2. **Review error messages** for specific issues

3. **Fix issues** and push corrections**Solución**:

4. **Re-run workflow** if needed1. Revisa los logs del job "Build"

2. Ejecuta localmente: `mvn clean package`

### Performance Metrics3. Corrige los errores y haz push nuevamente

- **Average build time**: 2-3 minutes

- **Success rate**: Target >95%### El workflow falla en "Verify Structure"

- **Dependency resolution**: <30 seconds

- **Documentation validation**: <10 seconds**Causa**: La estructura de Azure Functions no es correcta.



## Future Improvements**Solución**:

1. Verifica que el `pom.xml` tiene configurado correctamente `azure-functions-maven-plugin`

1. **Automated Testing**: Add unit and integration tests2. Ejecuta localmente: `mvn clean package`

2. **Security Scanning**: Include dependency vulnerability checks3. Verifica que el directorio `target/azure-functions/` se crea correctamente

3. **Code Quality**: Integrate SonarQube or similar tools

4. **Deployment**: Add automated deployment to Azure### El despliegue falla

5. **Notifications**: Enhanced reporting and alerting

6. **Caching**: Optimize build times with better caching**Causa**: Publish profile incorrecto o expirado.

7. **Parallel Jobs**: Run independent checks in parallel

**Solución**:

## Logs and Monitoring1. Regenera el publish profile desde Azure

2. Actualiza el secret `AZURE_FUNCTIONAPP_PUBLISH_PROFILE` en GitHub

### Accessing Logs3. Re-ejecuta el workflow

1. Navigate to repository Actions tab

2. Click on specific workflow run### Los tests de Event Grid fallan

3. Expand job sections to view detailed logs

4. Download logs for offline analysis**Causa**: Archivos de integración con Event Grid no existen.



### Log Retention**Solución**:

- **Standard**: 90 days for public repositories1. Verifica que todos los archivos en `src/main/java/com/agranelos/inventario/events/` existen

- **Artifacts**: 90 days retention period2. Haz commit de los archivos faltantes

- **Cleanup**: Automatic cleanup of old runs3. Push al repositorio



### Debugging Failed Runs## 📈 Mejoras Futuras

1. Enable debug logging by setting `ACTIONS_STEP_DEBUG` secret

2. Use `echo` statements for custom logging- [ ] Agregar tests unitarios automatizados

3. Check environment variables and secrets- [ ] Implementar tests de integración con base de datos

4. Verify file paths and permissions- [ ] Agregar análisis de código (SonarQube, CodeQL)

- [ ] Implementar despliegue a múltiples ambientes (dev, staging, prod)

## References- [ ] Agregar notificaciones a Slack/Teams

- [ ] Implementar rollback automático en caso de fallos

- [GitHub Actions Documentation](https://docs.github.com/en/actions)

- [Maven CI/CD Best Practices](https://maven.apache.org/guides/introduction/introduction-to-the-lifecycle.html)## 📝 Logs y Monitoreo

- [Azure Functions CI/CD](https://docs.microsoft.com/en-us/azure/azure-functions/functions-continuous-deployment)

- [Java CI/CD Patterns](https://docs.github.com/en/actions/automating-builds-and-tests/building-and-testing-java-with-maven)Los logs de los workflows se guardan por 90 días en GitHub Actions.



---Para ver logs detallados:

1. Ve a la pestaña "Actions"

**Ready to test?** Simply push your code and watch GitHub Actions handle the entire validation pipeline automatically.2. Click en el workflow run específico
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
