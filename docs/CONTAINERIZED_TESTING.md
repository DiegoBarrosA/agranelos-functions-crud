# Containerized Testing Guide

Esta guía explica cómo usar contenedores Podman/Docker para testing automatizado del sistema Agranelos Inventario.

## Prerrequisitos

### Opción 1: Podman (Recomendado)
```bash
# Fedora/RHEL/CentOS
sudo dnf install podman

# Ubuntu/Debian
sudo apt-get install podman

# macOS
brew install podman
```

### Opción 2: Docker
```bash
# Instalar Docker Engine según tu distribución
# Verificar instalación
docker --version
```

## Arquitectura de Testing

### Containerfile
El archivo `Containerfile` define un entorno de testing estándar con:
- Base: Fedora 39
- Herramientas: curl, jq, bash, coreutils
- Usuario no-root: `tester` (UID 1000)
- Scripts de testing pre-instalados
- Variables de entorno configurables

### Docker Compose
El archivo `docker-compose.yml` incluye:
- **bff-microservice**: Servicio principal de producción (sin perfil específico)
- **bff-microservice-local**: Servicio para desarrollo local y testing
  - Perfiles: `local`, `testing`  
  - Puerto: 8081
  - Dependencia requerida para api-tests
- **api-tests**: Contenedor de testing 
  - Perfil: `testing`
  - Depende de: `bff-microservice-local` (con health check)
  - Red: `testing-network`

## Uso Básico

### 1. Testing con Podman (Wrapper Script)
```bash
# Ejecutar todas las pruebas
./test-podman.sh

# Ver opciones disponibles
./test-podman.sh --help
```

### 2. Testing con Docker Compose
```bash
# Opción 1: Solo perfil testing (recomendado - bff-microservice-local está en ambos perfiles)
docker-compose --profile testing up api-tests

# Opción 2: Múltiples perfiles (alternativo si hay problemas de dependencias)
docker-compose --profile local --profile testing up api-tests

# Ver logs del testing
docker-compose --profile testing logs -f api-tests

# Ver logs del BFF microservice
docker-compose --profile testing logs -f bff-microservice-local

# Verificar qué servicios están disponibles en el perfil testing
docker-compose --profile testing config --services
```

### 3. Build Manual del Contenedor
```bash
# Con Podman
podman build -t agranelos-tests:latest -f Containerfile .

# Con Docker
docker build -t agranelos-tests:latest -f Containerfile .
```

## Configuración de Variables

### Variables de Entorno Soportadas
```bash
export REST_API_URL="http://localhost:7071/api"
export GRAPHQL_API_URL="http://localhost:7071/api/graphql"
export CONFIRM_PRODUCTION="true"  # Para testing de producción
```

### Configuración de Red
El contenedor usa `--network host` para acceder a servicios locales:
- **localhost:7071** - Azure Functions local
- **localhost:8080** - BFF Microservice
- **host.docker.internal** - Para Docker Desktop

## Comandos Avanzados

### Testing Específico
```bash
# Solo REST API
./test-podman.sh scripts/testing/test-rest-api.sh

# Solo GraphQL
./test-podman.sh scripts/testing/test-graphql-api.sh

# Solo Event Grid
./test-podman.sh scripts/testing/test-eventgrid.sh
```

### Desarrollo y Debug
```bash
# Ejecutar contenedor interactivo
podman run -it --rm --network host agranelos-tests:latest bash

# Montar directorio local para desarrollo
podman run -it --rm --network host \
  -v $(pwd)/scripts/testing:/opt/testing:Z \
  agranelos-tests:latest bash
```

### Limpieza
```bash
# Eliminar contenedor
podman rm -f agranelos-testing

# Eliminar imagen
podman rmi agranelos-tests:latest

# Limpiar todo
podman system prune -a
```

## Resultados de Testing

### Directorio de Resultados
Los resultados se almacenan en `./test-results/`:
```
test-results/
├── test-summary.txt    # Resumen de pruebas
├── test-errors.txt     # Errores detallados
├── rest-api.log        # Logs de REST API
├── graphql-api.log     # Logs de GraphQL API
└── eventgrid.log       # Logs de Event Grid
```

### Formato de Salida
```bash
# Verificar resultados
cat ./test-results/test-summary.txt

# Ver errores
cat ./test-results/test-errors.txt
```

## Integración con CI/CD

### GitHub Actions
```yaml
name: Containerized Tests
on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Run Podman Tests
        run: |
          # Instalar Podman en GitHub Actions
          sudo apt-get update
          sudo apt-get install -y podman
          
          # Ejecutar tests
          ./test-podman.sh
      
      - name: Upload Test Results
        uses: actions/upload-artifact@v3
        with:
          name: test-results
          path: ./test-results/
```

## Troubleshooting

### Problemas Comunes

#### 1. Error de Perfiles Docker Compose
```bash
# Si api-tests no puede iniciar por dependencias
# Verificar qué servicios están en el perfil testing
docker-compose --profile testing config --services

# Si bff-microservice-local no aparece, usar múltiples perfiles
docker-compose --profile local --profile testing up api-tests

# Verificar configuración de perfiles en docker-compose.yml
grep -A 5 -B 5 "profiles:" docker-compose.yml
```

#### 2. Error de Red
```bash
# Verificar conectividad
curl -f http://localhost:7071/api/health

# Usar host.containers.internal en lugar de localhost
export REST_API_URL="http://host.containers.internal:7071/api"
```

#### 2. Permisos de SELinux
```bash
# En sistemas con SELinux
setsebool -P container_manage_cgroup true

# O usar flag :Z en volúmenes
-v $(pwd)/test-results:/workspace/results:Z
```

#### 3. Podman no Encontrado
```bash
# Fallback automático a scripts nativos
./test-podman.sh --native

# O ejecutar directamente
./scripts/testing/test-all-apis.sh
```

#### 4. Imagen no Construida
```bash
# Forzar rebuild
./test-podman.sh --rebuild

# O construir manualmente
podman build -t agranelos-tests:latest -f Containerfile .
```

## Comparación: Nativo vs Containerizado

| Aspecto | Nativo | Containerizado |
|---------|--------|----------------|
| **Velocidad** | Más rápido | Ligeramente más lento |
| **Consistencia** | Depende del entorno | Entorno estándar |
| **Aislamiento** | Ninguno | Completo |
| **Dependencias** | Instalación manual | Pre-instaladas |
| **CI/CD** | Complejo | Simplificado |
| **Reproducibilidad** | Variable | Garantizada |

## Mejores Prácticas

### 1. Desarrollo
- Usar `--rebuild` al cambiar scripts
- Montar volúmenes para desarrollo iterativo
- Usar modo interactivo para debugging

### 2. CI/CD
- Cachear imágenes entre builds
- Paralelizar diferentes tipos de tests
- Guardar artifacts de resultados

### 3. Producción
- Validar variables de entorno
- Usar timeouts apropiados
- Implementar retry logic para tests flaky

### 4. Mantenimiento
- Actualizar imagen base regularmente
- Limpiar imágenes no utilizadas
- Monitorear tamaño de resultados

## Referencias

- [Podman Documentation](https://docs.podman.io/)
- [Docker Compose Reference](https://docs.docker.com/compose/)
- [Containerfile Specification](https://github.com/containers/common/blob/main/docs/Containerfile.5.md)
- [Testing Scripts Documentation](./scripts/testing/README.md)