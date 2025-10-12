# GUÍA PARA PRESENTACIÓN EN VIDEO - SUMATIVA 3
## Sistema de Inventario Agranelos

**Duración Recomendada**: 12-15 minutos  
**Plataforma**: Microsoft Teams / Kaltura  
**Formato**: Screen recording con narración

---

## PREPARACIÓN ANTES DE GRABAR

### Checklist de Prerequisitos

- [ ] **Sistema completamente funcional**
  - [ ] Azure Functions corriendo (puerto 7071)
  - [ ] BFF Microservice corriendo (puerto 8080)
  - [ ] PostgreSQL iniciado
  - [ ] Event Grid configurado

- [ ] **Herramientas abiertas**
  - [ ] VS Code con el código fuente
  - [ ] Postman con la colección cargada
  - [ ] Azure Portal (Event Grid + App Insights)
  - [ ] Terminal con logs visibles
  - [ ] Navegador con documentación

- [ ] **Datos de prueba**
  - [ ] Base de datos inicializada
  - [ ] Algunos productos y bodegas creados
  - [ ] Email de notificaciones configurado

- [ ] **Documentos visibles**
  - [ ] README.md abierto
  - [ ] ARQUITECTURA.md con diagramas
  - [ ] SUMATIVA-3-CHECKLIST.md

---

## ESTRUCTURA DE LA PRESENTACIÓN

### **PARTE 1: INTRODUCCIÓN (2 minutos)**

#### Script Sugerido:
```
"Hola, mi nombre es [Tu Nombre]. Les presento el Sistema de Inventario 
Agranelos, un proyecto completo de arquitectura cloud serverless que cumple 
con todos los requisitos de la Sumativa 3.

Este sistema implementa:
- Microservicios con Spring Boot
- Funciones serverless en Java con Azure Functions
- Arquitectura orientada a eventos con Event Grid
- APIs REST y GraphQL
- Todo desplegado en Azure Cloud"
```

#### Qué Mostrar:
1. **README.md principal** (30 seg)
   - Descripción del proyecto
   - Stack tecnológico
   - Badges y características

2. **Diagrama de Arquitectura** (1 min)
   ```
   Abrir: docs/ARQUITECTURA.md
   Mostrar: Diagrama Mermaid con todos los componentes
   Explicar: Flujo de datos de cliente → BFF → Functions → Event Grid
   ```

3. **Repositorios GitHub** (30 seg)
   - Mostrar URL: https://github.com/DiegoBarrosA/agranelos-functions-crud
   - Mostrar commits recientes
   - Mencionar branch: sumativa-3-staging

---

### **PARTE 2: MICROSERVICIO BFF - SPRING BOOT (2 minutos)**

#### Script Sugerido:
```
"Comenzamos con el componente de microservicios. Implementé un BFF 
(Backend for Frontend) usando Spring Boot que orquesta las llamadas 
a las Azure Functions serverless."
```

#### Qué Mostrar:

1. **Código del BFF** (45 seg)
   ```
   Abrir: agranelos-bff/src/main/java/.../controller/
   Mostrar: ProductoController.java
   Destacar: 
   - @RestController
   - Inyección de WebClient
   - Llamadas a Azure Functions
   - Manejo de errores
   ```

2. **BFF Corriendo** (45 seg)
   ```bash
   # En terminal mostrar:
   curl http://localhost:8080/actuator/health
   
   # Respuesta esperada:
   {
     "status": "UP",
     "components": {
       "db": {"status": "UP"},
       "diskSpace": {"status": "UP"}
     }
   }
   ```

3. **Logs del BFF** (30 seg)
   ```
   Mostrar: Terminal con logs de Spring Boot
   Destacar: 
   - Inicio exitoso
   - Conexiones a Azure Functions
   - Health checks
   ```

---

### **PARTE 3: AZURE FUNCTIONS - APIs REST (3 minutos)**

#### Script Sugerido:
```
"Ahora veamos las funciones serverless. Implementé 10 Azure Functions 
para APIs REST, superando el requisito mínimo de 2. Estas funciones 
manejan todas las operaciones CRUD para productos y bodegas."
```

#### Demo en Postman:

1. **GET /api/productos** (30 seg)
   ```
   Mostrar en Postman:
   - Request: GET http://localhost:7071/api/productos
   - Response: JSON con lista de productos
   - Status: 200 OK
   ```

2. **POST /api/productos** (45 seg)
   ```json
   Crear un producto nuevo:
   POST http://localhost:7071/api/productos
   Body:
   {
     "nombre": "Laptop HP Pavilion",
     "descripcion": "Laptop gaming 16GB RAM",
     "precio": 899.99,
     "cantidadEnStock": 10
   }
   
   Mostrar Response:
   {
     "success": true,
     "data": {
       "id": 5,
       "nombre": "Laptop HP Pavilion",
       ...
     }
   }
   ```

3. **GET /api/productos/{id}** (30 seg)
   ```
   Verificar producto creado:
   GET http://localhost:7071/api/productos/5
   Mostrar el JSON del producto
   ```

4. **Código de la Function** (1 min)
   ```
   Abrir en VS Code:
   src/main/java/com/agranelos/inventario/Function.java
   
   Mostrar método:
   @FunctionName("CreateProducto")
   public HttpResponseMessage createProducto(...)
   
   Destacar:
   - Anotación @FunctionName
   - HttpTrigger
   - Conexión a PostgreSQL
   - Publicación de evento a Event Grid
   - Respuesta JSON
   ```

---

### **PARTE 4: AZURE FUNCTIONS - GraphQL (2 minutos)**

#### Script Sugerido:
```
"El sistema también implementa GraphQL. A través de un único endpoint, 
podemos ejecutar queries y mutations flexibles."
```

#### Demo GraphQL:

1. **Query - Listar Productos** (45 seg)
   ```graphql
   POST http://localhost:7071/api/graphql
   Body:
   {
     "query": "{ productos { id nombre precio cantidadEnStock } }"
   }
   
   Mostrar Response:
   {
     "data": {
       "productos": [
         {"id": 1, "nombre": "Laptop Dell", "precio": 1299.99, ...},
         {"id": 5, "nombre": "Laptop HP Pavilion", "precio": 899.99, ...}
       ]
     }
   }
   ```

2. **Mutation - Crear Producto** (45 seg)
   ```graphql
   {
     "query": "mutation { crearProducto(input: { nombre: \"Mouse Logitech\", descripcion: \"Mouse inalámbrico\", precio: 29.99, cantidadEnStock: 50 }) { id nombre precio } }"
   }
   
   Mostrar que se crea exitosamente
   ```

3. **Código GraphQL Schema** (30 seg)
   ```
   Abrir: src/main/resources/schema.graphqls
   Mostrar:
   - type Producto
   - type Query
   - type Mutation
   - input ProductoInput
   ```

---

### **PARTE 5: EVENT GRID - ARQUITECTURA DE EVENTOS (3 minutos)**

#### Script Sugerido:
```
"Una característica clave del sistema es la arquitectura orientada a eventos 
usando Azure Event Grid. Cuando ocurre una operación CRUD, se publica un 
evento que es procesado de forma asíncrona."
```

#### Demostración:

1. **Crear un Producto (Trigger Evento)** (45 seg)
   ```
   En Postman:
   POST /api/productos
   {
     "nombre": "Teclado Mecánico",
     "descripcion": "RGB, switches blue",
     "precio": 129.99,
     "cantidadEnStock": 20
   }
   ```

2. **Mostrar Event Publisher** (1 min)
   ```java
   En VS Code mostrar en CreateProducto:
   
   // Publicar evento a Event Grid
   EventGridPublisherClient client = new EventGridPublisherClientBuilder()
       .endpoint(eventGridEndpoint)
       .credential(new AzureKeyCredential(eventGridKey))
       .buildClient();
   
   EventGridEvent event = new EventGridEvent(
       "CreateProducto-" + producto.getId(),
       "Agranelos.Inventario.ProductoCreado",
       BinaryData.fromObject(producto),
       "1.0"
   );
   
   client.sendEvent(event);
   ```

3. **Mostrar Event Handler** (1 min)
   ```java
   Abrir: src/main/java/com/agranelos/inventario/events/ProductoCreatedHandler.java
   
   @FunctionName("ProductoCreatedHandler")
   public void handleProductoCreated(
       @EventGridTrigger(name = "event") String event,
       final ExecutionContext context
   ) {
       context.getLogger().info("Evento recibido: ProductoCreado");
       // Procesar evento
       // Enviar notificación
       // Actualizar sistemas
   }
   ```

4. **Ver Logs del Handler** (15 seg)
   ```
   En terminal de Azure Functions mostrar:
   
   [2025-10-12T14:30:25.123] Executing 'ProductoCreatedHandler'
   [2025-10-12T14:30:25.145] Evento recibido: ProductoCreado
   [2025-10-12T14:30:25.167] Producto ID: 6
   [2025-10-12T14:30:25.189] Enviando notificación por email...
   [2025-10-12T14:30:25.234] Executed 'ProductoCreatedHandler' (Success)
   ```

---

### **PARTE 6: AZURE PORTAL - EVENT GRID Y MONITORING (2 minutos)**

#### Script Sugerido:
```
"Vamos al Azure Portal para ver la infraestructura en la nube."
```

#### Qué Mostrar:

1. **Event Grid Topic** (45 seg)
   ```
   En Azure Portal:
   - Navegar a Resource Group
   - Abrir Event Grid Topic: agranelos-eventgrid-topic
   - Mostrar Overview con métricas
   - Mostrar Event Subscriptions
   ```

2. **Application Insights** (1 min 15 seg)
   ```
   Abrir Application Insights:
   - Mostrar Live Metrics
   - Ver requests en tiempo real
   - Mostrar Success rate
   - Ver Response times
   - Abrir Logs y mostrar query:
   
   traces
   | where timestamp > ago(1h)
   | where message contains "Producto"
   | project timestamp, message
   | order by timestamp desc
   ```

---

### **PARTE 7: TESTING AUTOMATIZADO (1.5 minutos)**

#### Script Sugerido:
```
"El sistema incluye scripts de testing automatizados para validar 
toda la funcionalidad."
```

#### Demo:

1. **Ejecutar Test Suite** (1 min)
   ```bash
   cd scripts/testing
   ./test-all-apis.sh
   ```

2. **Mostrar Resultados** (30 seg)
   ```
   Mostrar output del script:
   
   ========================================
   AGRANELOS - TEST SUITE COMPLETO
   ========================================
   
   REST API - Productos: PASS (5/5)
   REST API - Bodegas: PASS (5/5)
   GraphQL API - Queries: PASS (4/4)
   GraphQL API - Mutations: PASS (6/6)
   Event Grid - Eventos: PASS (6/6)
   Performance Tests: PASS (3/3)
   
   ========================================
   RESULTADO FINAL: 29/29 tests PASSED ✅
   ========================================
   ```

---

### **PARTE 8: CÓDIGO Y ESTRUCTURA (1.5 minutos)**

#### Script Sugerido:
```
"Veamos rápidamente la estructura del proyecto y algunos componentes clave."
```

#### Qué Mostrar:

1. **Estructura del Proyecto** (30 seg)
   ```
   En VS Code, mostrar árbol de directorios:
   
   agranelos-functions-crud-create/
   ├── src/main/java/com/agranelos/inventario/
   │   ├── Function.java          # 18 Azure Functions
   │   ├── events/                # Event Handlers
   │   ├── db/                    # Database Connection
   │   ├── graphql/               # GraphQL Schema
   │   ├── model/                 # POJOs
   │   └── services/              # Business Logic
   ├── docs/                      # 10+ documentos
   ├── scripts/testing/           # 7 scripts de testing
   └── postman/                   # Colección Postman
   ```

2. **Modelo de Datos** (30 seg)
   ```java
   Mostrar: src/main/java/com/agranelos/inventario/model/Producto.java
   
   public class Producto {
       private int id;
       private String nombre;
       private String descripcion;
       private double precio;
       private int cantidadEnStock;
       private LocalDateTime fechaCreacion;
       private LocalDateTime fechaActualizacion;
   }
   ```

3. **Configuración** (30 seg)
   ```
   Mostrar: pom.xml
   Destacar dependencias:
   - azure-functions-java-library
   - azure-messaging-eventgrid
   - postgresql
   - graphql-java
   - jackson-databind
   ```

---

### **PARTE 9: DOCKER Y DESPLIEGUE (1 minuto)**

#### Script Sugerido:
```
"El sistema está containerizado con Docker para facilitar el despliegue."
```

#### Qué Mostrar:

1. **docker-compose.yml** (30 seg)
   ```yaml
   Mostrar archivo:
   
   services:
     bff-microservice:
       build: ./bff-microservice
       ports:
         - "8080:8080"
       environment:
         - AZURE_FUNCTIONS_BASE_URL=...
       healthcheck:
         test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
   ```

2. **Comandos de Despliegue** (30 seg)
   ```bash
   # Mostrar en terminal (no ejecutar, solo mostrar):
   
   # Build y start con Docker
   docker-compose up --build
   
   # Deploy a Azure
   mvn azure-functions:deploy
   ```

---

### **PARTE 10: CIERRE Y REQUISITOS CUMPLIDOS (1 minuto)**

#### Script Sugerido:
```
"Para finalizar, veamos el resumen de todos los requisitos cumplidos."
```

#### Qué Mostrar:

1. **Abrir SUMATIVA-3-CHECKLIST.md** (30 seg)
   - Scrollear por la sección "RESUMEN DE CUMPLIMIENTO"
   - Destacar: 12/12 requisitos cumplidos
   - Mencionar extras implementados

2. **Requisitos Destacados** (30 seg)
   ```
   Resumir en pantalla:
   
   Microservicios Spring Boot: BFF implementado
   Funciones Serverless Java: 18 Azure Functions
   APIs REST: 10 endpoints (requisito: 2)
   APIs GraphQL: Completo con queries y mutations
   Event Grid: 6 tipos de eventos + handlers
   Docker: Configurado y funcional
   GIT: 2 repositorios con código organizado
   Documentación: 10+ documentos técnicos
   Testing: 7 scripts + Colección Postman
   
   EXTRAS:
   - Application Insights
   - SendGrid notifications
   - CI/CD con GitHub Actions
   - ARM Templates (IaC)
   ```

---

## TIPS PARA UNA BUENA GRABACIÓN

### Técnicos:
- Resolución mínima: 1920x1080 (Full HD)
- Audio claro sin ruido de fondo
- Velocidad de habla: Normal, no muy rápido
- Pausas de 2-3 segundos entre secciones

### De Contenido:
- Hablar con confianza y claridad
- Explicar "qué" y "por qué", no solo "cómo"
- Conectar cada componente con los requisitos
- Mostrar código, no solo herramientas
- Demostrar funcionamiento real, no screenshots

### De Presentación:
- Empezar con un overview claro
- Seguir un flujo lógico (arquitectura → componentes → demo)
- Usar transiciones suaves entre secciones
- Terminar con un resumen contundente

---

## SCRIPT COMPLETO (Para Leer)

### Apertura:
```
"Buenos días/tardes. Mi nombre es [Tu Nombre] y les voy a presentar 
el Sistema de Inventario Agranelos, un proyecto completo de arquitectura 
cloud serverless para la Sumativa 3.

Este sistema implementa una solución completa de gestión de inventario 
utilizando microservicios con Spring Boot, funciones serverless en Java 
con Azure Functions, y arquitectura orientada a eventos con Event Grid.

Vamos a ver cómo cada componente trabaja en conjunto para cumplir todos 
los requisitos de la evaluación."
```

### Durante la Demo:
```
[Mantener narración fluida]
"Como pueden ver aquí..."
"Esto demuestra que..."
"Noten cómo el sistema..."
"Este componente se encarga de..."
```

### Cierre:
```
"En resumen, el Sistema de Inventario Agranelos cumple y supera todos 
los requisitos de la Sumativa 3:

- Microservicios implementados con Spring Boot
- 18 Azure Functions serverless en Java
- Arquitectura completa de eventos con Event Grid
- APIs REST y GraphQL funcionales
- Todo containerizado con Docker
- Código en repositorios GitHub
- Documentación técnica completa
- Scripts de testing automatizado

El sistema está listo para producción y demuestra un entendimiento 
profundo de arquitecturas cloud modernas.

Gracias por su atención."
```

---

## ⏱️ TIMING DETALLADO

| Sección | Duración | Acumulado |
|---------|----------|-----------|
| Introducción | 2:00 | 2:00 |
| BFF Spring Boot | 2:00 | 4:00 |
| Azure Functions REST | 3:00 | 7:00 |
| Azure Functions GraphQL | 2:00 | 9:00 |
| Event Grid | 3:00 | 12:00 |
| Azure Portal | 2:00 | 14:00 |
| Testing | 1:30 | 15:30 |
| Código | 1:30 | 17:00 |
| Docker | 1:00 | 18:00 |
| Cierre | 1:00 | 19:00 |
| **Total** | **~15-19 min** | - |

**Recomendación**: Apuntar a 15 minutos, máximo 20.

---

## 🎥 HERRAMIENTAS DE GRABACIÓN RECOMENDADAS

### Para Microsoft Teams:
1. Iniciar reunión
2. Compartir pantalla completa
3. Clic en "Grabar"
4. Al terminar, detener grabación
5. Descargar video desde Stream

### Para Kaltura:
1. Usar Kaltura Capture
2. Seleccionar pantalla + audio
3. Grabar
4. Upload a Kaltura My Media
5. Obtener link de compartir

### Alternativas:
- OBS Studio (gratuito, profesional)
- Camtasia (pagado, fácil de editar)
- Zoom (grabar a local)

---

## CHECKLIST POST-GRABACIÓN

Antes de subir el video, verificar:

- [ ] Audio se escucha claro
- [ ] Video tiene buena calidad (1080p mínimo)
- [ ] Se ve todo el texto en pantalla
- [ ] Duración entre 10-20 minutos
- [ ] Se muestran TODOS los requisitos
- [ ] Se ve código funcionando en vivo
- [ ] Se muestra Azure Portal
- [ ] Se ejecutan tests exitosamente
- [ ] Se menciona GitHub repositories
- [ ] Se muestra documento de checklist

---

## 🔗 LINKS A INCLUIR EN LA DESCRIPCIÓN DEL VIDEO

```
📹 Video: Sistema de Inventario Agranelos - Sumativa 3

🔗 Repositorio Principal:
https://github.com/DiegoBarrosA/agranelos-functions-crud
Branch: sumativa-3-staging

Documentación:
https://github.com/DiegoBarrosA/agranelos-functions-crud/tree/sumativa-3-staging/docs

Checklist de Requisitos:
https://github.com/DiegoBarrosA/agranelos-functions-crud/blob/sumativa-3-staging/docs/SUMATIVA-3-CHECKLIST.md

Scripts de Testing:
https://github.com/DiegoBarrosA/agranelos-functions-crud/tree/sumativa-3-staging/scripts/testing

📮 Colección Postman:
https://github.com/DiegoBarrosA/agranelos-functions-crud/blob/sumativa-3-staging/postman/

⏱️ Timestamps:
0:00 - Introducción
2:00 - Microservicio BFF (Spring Boot)
4:00 - Azure Functions REST
7:00 - Azure Functions GraphQL
9:00 - Event Grid (Eventos)
12:00 - Azure Portal & Monitoring
14:00 - Testing Automatizado
15:30 - Estructura del Código
17:00 - Docker y Despliegue
18:00 - Resumen y Cierre

Stack Tecnológico:
- Java 11
- Spring Boot 3.x
- Azure Functions
- Azure Event Grid
- PostgreSQL
- GraphQL
- Docker
- Maven
```

---

**¡Éxito con tu presentación! 🚀**

Si sigues esta guía paso a paso, tendrás una presentación profesional 
que demuestra claramente el cumplimiento de todos los requisitos.
