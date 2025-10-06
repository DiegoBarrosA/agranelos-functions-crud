# Configuracion de Notificaciones por Email

## Resumen
Este sistema envia notificaciones automaticas por email a `di.barros@duocuc.cl` cada vez que:
- Se crea un producto o bodega
- Se actualiza un producto o bodega  
- Se elimina un producto o bodega

## Configuracion con SendGrid

### Paso 1: Crear una Cuenta SendGrid

SendGrid ofrece un tier gratuito con 100 emails por dia, ideal para proyectos universitarios.

#### 1.1 Registrarse en SendGrid

1. Ve a: https://signup.sendgrid.com/
2. Completa el formulario de registro
3. Verifica tu email haciendo clic en el enlace de confirmacion
4. Completa el perfil de tu cuenta

### Paso 2: Generar una API Key

#### 2.1 Crear la API Key

1. Inicia sesion en: https://app.sendgrid.com/
2. En el menu lateral izquierdo, ve a **Settings** → **API Keys**
3. Haz clic en **Create API Key**
4. Configura la key:
   - **API Key Name**: `AgranelosInventario`
   - **API Key Permissions**: Selecciona **Full Access**
5. Haz clic en **Create & View**
6. **Copia la API Key completa** (comienza con `SG.`)

⚠️ **IMPORTANTE**: Esta API Key solo se muestra una vez. Guardala en un lugar seguro.

### Paso 3: Verificar un Sender Email

SendGrid requiere que verifiques el email desde el cual enviaras mensajes.

#### 3.1 Single Sender Verification

1. En SendGrid, ve a **Settings** → **Sender Authentication**
2. En la seccion **Single Sender Verification**, haz clic en **Get Started** o **Verify a Single Sender**
3. Completa el formulario:
   - **From Name**: `Sistema Inventario Agranelos`
   - **From Email Address**: Tu email personal (puede ser Gmail, Outlook, etc.)
   - **Reply To**: El mismo email
   - **Company Address**: Direccion de DuocUC o tu direccion personal
   - **City**: Santiago
   - **State**: Metropolitana
   - **Zip Code**: Codigo postal
   - **Country**: Chile
4. Haz clic en **Create**
5. **Revisa tu bandeja de entrada** del email que proporcionaste
6. Abre el email de SendGrid y haz clic en **Verify Single Sender**
7. Confirma la verificacion en la pagina web

✅ Una vez verificado, este email estara autorizado para enviar mensajes.

### Paso 4: Configurar las Variables de Entorno

#### Opcion A: Para Desarrollo Local

Edita el archivo `local.settings.json`:

```json
{
  "IsEncrypted": false,
  "Values": {
    "AzureWebJobsStorage": "UseDevelopmentStorage=true",
    "FUNCTIONS_WORKER_RUNTIME": "java",
    "DB_HOST": "tu-servidor-postgresql",
    "DB_PORT": "5432",
    "DB_NAME": "inventario_agranelos",
    "DB_USER": "postgres",
    "DB_PASSWORD": "tu-password",
    "DB_SSL_MODE": "disable",
    "EVENT_GRID_ENDPOINT": "https://tu-eventgrid.eventgrid.azure.net/api/events",
    "EVENT_GRID_KEY": "tu-event-grid-key",
    "SENDGRID_API_KEY": "SG.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
    "SENDER_EMAIL": "tu-email-verificado@example.com",
    "RECIPIENT_EMAIL": "di.barros@duocuc.cl"
  }
}
```

Reemplaza:
- `SG.xxxx` con la API Key que generaste en el Paso 2
- `tu-email-verificado@example.com` con el email que verificaste en el Paso 3
- `di.barros@duocuc.cl` es el destinatario de las notificaciones

#### Opcion B: Para Azure (Produccion)

Configura las variables en Azure Portal o con Azure CLI:

```bash
# Configurar SendGrid API Key
az functionapp config appsettings set \
  --name agranelos-inventario-functions \
  --resource-group agranelos-inventario-rg \
  --settings SENDGRID_API_KEY="SG.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx"

# Configurar Sender Email (email verificado en SendGrid)
az functionapp config appsettings set \
  --name agranelos-inventario-functions \
  --resource-group agranelos-inventario-rg \
  --settings SENDER_EMAIL="tu-email-verificado@example.com"

# Configurar Recipient Email (destinatario de notificaciones)
az functionapp config appsettings set \
  --name agranelos-inventario-functions \
  --resource-group agranelos-inventario-rg \
  --settings RECIPIENT_EMAIL="di.barros@duocuc.cl"
```

O desde Azure Portal:
1. Ve a tu Function App
2. **Configuration** → **Application settings**
3. Agrega las 3 variables: `SENDGRID_API_KEY`, `SENDER_EMAIL`, `RECIPIENT_EMAIL`

### Paso 5: Verificar la Configuracion

#### 5.1 Compilar el proyecto

```bash
mvn clean package
```

#### 5.2 Iniciar Azure Functions localmente

```bash
mvn azure-functions:run
```

#### 5.3 Crear un producto de prueba

```bash
curl -X POST http://localhost:7071/api/productos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Producto de Prueba Email",
    "descripcion": "Prueba de notificacion",
    "precio": 1000,
    "stock": 10,
    "bodegaId": 1
  }'
```

#### 5.4 Verificar logs

Busca en los logs del terminal:

```
[INFO] === Evento ProductoCreado Recibido ===
[INFO] Event Type: Agranelos.Inventario.ProductoCreado
[INFO] Email enviado exitosamente a di.barros@duocuc.cl: Nuevo Producto Creado - Inventario Agranelos (Status: 202)
```

#### 5.5 Revisar tu email

Revisa la bandeja de entrada de `di.barros@duocuc.cl`. Deberias ver un email HTML como:

```
Asunto: Nuevo Producto Creado - Inventario Agranelos

Nuevo Producto Creado

Se ha creado un nuevo producto en el sistema de inventario:

• ID: 123
• Nombre: Producto de Prueba Email
• Fecha: 2025-10-05T14:30:00

Sistema de Inventario Agranelos
```

## Tipos de Notificaciones

El sistema envia 6 tipos de emails:

### Para Productos:
1. **ProductoCreado** - Cuando se crea un nuevo producto
2. **ProductoActualizado** - Cuando se actualiza un producto existente
3. **ProductoEliminado** - Cuando se elimina un producto

### Para Bodegas:
4. **BodegaCreada** - Cuando se crea una nueva bodega
5. **BodegaActualizada** - Cuando se actualiza una bodega existente
6. **BodegaEliminada** - Cuando se elimina una bodega

## Troubleshooting

### Error: "SendGrid API Key no configurada"

**Solucion**: Verifica que hayas agregado la variable `SENDGRID_API_KEY` en `local.settings.json` o en las configuraciones de Azure Function App.

### Error: "Email remitente no configurado"

**Solucion**: Verifica que hayas agregado la variable `SENDER_EMAIL` con el email que verificaste en SendGrid.

### Error: "401 Unauthorized"

**Causas posibles**:
1. La API Key es incorrecta
2. La API Key ha sido eliminada o revocada
3. La API Key no tiene los permisos necesarios

**Solucion**: 
- Genera una nueva API Key en SendGrid con **Full Access**
- Verifica que copiaste la key completa (comienza con `SG.`)

### Error: "403 Forbidden"

**Causa**: El sender email no esta verificado en SendGrid.

**Solucion**: 
1. Ve a SendGrid → Settings → Sender Authentication
2. Verifica el sender email siguiendo el proceso del Paso 3
3. Asegurate de hacer clic en el enlace de verificacion del email

### Error: Status Code 400 o superior

**Solucion**: Revisa los logs detallados. SendGrid devuelve el error en el body del response. Busca en los logs:

```
[WARNING] Error al enviar email. Status: 400, Body: {"errors":[...]}
```

### Los emails no llegan

**Soluciones**:
1. Revisa la carpeta de SPAM de `di.barros@duocuc.cl`
2. Verifica en el SendGrid Dashboard:
   - Ve a https://app.sendgrid.com/
   - Click en **Activity**
   - Busca tus emails enviados y su estado
3. Revisa los logs de Azure Functions para errores
4. Verifica que `RECIPIENT_EMAIL` este correctamente configurado

### Limite de envios alcanzado

**Causa**: El plan gratuito de SendGrid permite 100 emails por dia.

**Solucion**: 
- Monitorea tu uso en el SendGrid Dashboard
- Para proyectos de produccion, considera actualizar a un plan pago
- Durante desarrollo, evita crear loops de eventos que generen multiples emails

## Monitoreo con SendGrid Dashboard

SendGrid proporciona un dashboard completo para monitorear tus emails:

1. Ve a https://app.sendgrid.com/
2. Click en **Activity**
3. Aqui puedes ver:
   - Emails enviados
   - Emails entregados
   - Emails rebotados (bounced)
   - Emails marcados como spam
   - Tasa de apertura (si tienes tracking habilitado)

## Seguridad

Buenas practicas implementadas:
- API Key en lugar de credenciales SMTP
- No incluye credenciales en el codigo fuente
- Las credenciales se configuran mediante variables de entorno
- El archivo `local.settings.json` esta en `.gitignore`
- Sender email verificado por SendGrid

⚠️ **NUNCA HAGAS COMMIT DE**:
- `local.settings.json` con tus credenciales reales
- Tu API Key de SendGrid
- Cualquier dato sensible

## Limites del Plan Gratuito

SendGrid Free Tier incluye:
- 100 emails por dia
- Sin limite de validez
- No requiere tarjeta de credito
- Retencion de logs por 3 dias
- Acceso completo al dashboard de actividad

## Referencias

- [SendGrid Documentation](https://docs.sendgrid.com/)
- [SendGrid Java Library](https://github.com/sendgrid/sendgrid-java)
- [Single Sender Verification Guide](https://docs.sendgrid.com/ui/sending-email/sender-verification)
- [SendGrid API Reference](https://docs.sendgrid.com/api-reference/mail-send/mail-send)

## Proximas Mejoras

Para un proyecto profesional, considera:
- Implementar templates HTML personalizados con branding
- Agregar logica de retry en caso de fallo temporal
- Implementar rate limiting para evitar exceder cuota diaria
- Agregar notificaciones agrupadas (digest diario)
- Configurar webhooks de SendGrid para tracking avanzado
- Implementar diferentes destinatarios segun tipo de evento

- Implementar rate limiting para evitar spam
