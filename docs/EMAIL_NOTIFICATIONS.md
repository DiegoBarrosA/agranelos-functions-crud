# 📧 Configuración de Notificaciones por Email

## 📋 Resumen
Este sistema envía notificaciones automáticas por email a `di.barros@duocuc.cl` cada vez que:
- ✅ Se crea un producto o bodega
- 🔄 Se actualiza un producto o bodega  
- 🗑️ Se elimina un producto o bodega

## 🔧 Configuración con Gmail (RECOMENDADO para proyecto universitario)

### Paso 1: Crear una App Password de Gmail

Para usar Gmail SMTP desde Azure Functions, necesitas una **App Password** (no tu contraseña normal de Gmail).

#### 1.1 Habilitar la verificación en 2 pasos

1. Ve a tu cuenta de Google: https://myaccount.google.com/
2. En el menú izquierdo, selecciona **"Seguridad"**
3. Busca **"Verificación en 2 pasos"** y haz clic en ella
4. Sigue las instrucciones para habilitarla (si no la tienes activada)

#### 1.2 Generar una App Password

1. Una vez habilitada la verificación en 2 pasos, ve a: https://myaccount.google.com/apppasswords
2. En "Selecciona la app", elige **"Correo"**
3. En "Selecciona el dispositivo", elige **"Otro (nombre personalizado)"**
4. Escribe un nombre como: `Azure Functions Agranelos`
5. Haz clic en **"Generar"**
6. **Copia la contraseña de 16 caracteres** que aparece (la necesitarás en el siguiente paso)

⚠️ **IMPORTANTE**: Esta contraseña solo se muestra una vez. Guárdala en un lugar seguro.

### Paso 2: Configurar las Variables de Entorno

#### Opción A: Para Desarrollo Local

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
    "GMAIL_SENDER_EMAIL": "tu-email@gmail.com",
    "GMAIL_APP_PASSWORD": "xxxx xxxx xxxx xxxx"
  }
}
```

Reemplaza:
- `tu-email@gmail.com` con tu cuenta de Gmail
- `xxxx xxxx xxxx xxxx` con la App Password que generaste en el Paso 1.2

#### Opción B: Para Azure (Producción)

Configura las variables en Azure Portal o con Azure CLI:

```bash
# Configurar Gmail Sender Email
az functionapp config appsettings set \
  --name agranelos-inventario-functions \
  --resource-group agranelos-inventario-rg \
  --settings GMAIL_SENDER_EMAIL="tu-email@gmail.com"

# Configurar Gmail App Password
az functionapp config appsettings set \
  --name agranelos-inventario-functions \
  --resource-group agranelos-inventario-rg \
  --settings GMAIL_APP_PASSWORD="xxxx xxxx xxxx xxxx"
```

### Paso 3: Verificar la Configuración

#### 3.1 Compilar el proyecto

```bash
mvn clean package
```

#### 3.2 Iniciar Azure Functions localmente

```bash
mvn azure-functions:run
```

#### 3.3 Crear un producto de prueba

```bash
curl -X POST http://localhost:7071/api/productos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Producto de Prueba Email",
    "descripcion": "Prueba de notificación",
    "precio": 1000,
    "stock": 10,
    "bodegaId": 1
  }'
```

#### 3.4 Verificar logs

Busca en los logs del terminal:

```
[INFO] === Evento ProductoCreado Recibido ===
[INFO] Event Type: Agranelos.Inventario.ProductoCreado
[INFO] ✉️ Email enviado exitosamente a di.barros@duocuc.cl
```

#### 3.5 Revisar tu email

Revisa la bandeja de entrada de `di.barros@duocuc.cl`. Deberías ver un email como:

```
Asunto: ✅ Nuevo Producto Creado - Inventario Agranelos

Hola,

Se ha creado un nuevo producto en el sistema de inventario:

📦 ID del Producto: 123
📝 Nombre: Producto de Prueba Email
⏰ Fecha: 2025-10-05T14:30:00

Este es un mensaje automático del sistema de gestión de inventario Agranelos.

Saludos,
Sistema de Inventario Agranelos
```

## 🎯 Tipos de Notificaciones

El sistema envía 6 tipos de emails:

### Para Productos:
1. **✅ ProductoCreado** - Cuando se crea un nuevo producto
2. **🔄 ProductoActualizado** - Cuando se actualiza un producto existente
3. **🗑️ ProductoEliminado** - Cuando se elimina un producto

### Para Bodegas:
4. **✅ BodegaCreada** - Cuando se crea una nueva bodega
5. **🔄 BodegaActualizada** - Cuando se actualiza una bodega existente
6. **🗑️ BodegaEliminada** - Cuando se elimina una bodega

## ⚠️ Troubleshooting

### Error: "GMAIL_SENDER_EMAIL no configurado"

**Solución**: Verifica que hayas agregado las variables `GMAIL_SENDER_EMAIL` y `GMAIL_APP_PASSWORD` en `local.settings.json`.

### Error: "Authentication failed"

**Causas posibles**:
1. La App Password es incorrecta
2. No has habilitado la verificación en 2 pasos
3. Estás usando tu contraseña normal en vez de la App Password

**Solución**: 
- Genera una nueva App Password siguiendo el Paso 1
- Asegúrate de copiar la App Password completa (16 caracteres con espacios)

### Error: "Could not connect to SMTP host"

**Solución**: Verifica tu conexión a Internet. Gmail SMTP usa el puerto 587.

### Los emails no llegan

**Solución**:
1. Revisa la carpeta de SPAM de `di.barros@duocuc.cl`
2. Verifica que el email del remitente esté bien escrito
3. Revisa los logs de Azure Functions para errores

## 🔐 Seguridad

✅ **Buenas prácticas implementadas**:
- Usa App Password en lugar de la contraseña real
- No incluye credenciales en el código fuente
- Las credenciales se configuran mediante variables de entorno
- El archivo `local.settings.json` está en `.gitignore`

⚠️ **NUNCA HAGAS COMMIT DE**:
- `local.settings.json` con tus credenciales reales
- Tu App Password de Gmail
- Cualquier dato sensible

## 📚 Referencias

- [Configurar App Passwords de Google](https://support.google.com/accounts/answer/185833)
- [Gmail SMTP Settings](https://support.google.com/mail/answer/7126229)
- [JavaMail API Documentation](https://javaee.github.io/javamail/)

## 💡 Próximas Mejoras

Para un proyecto profesional, considera:
- Usar **Azure Communication Services** para emails
- Usar **SendGrid** para mayor escalabilidad
- Implementar templates HTML para emails más bonitos
- Agregar lógica de retry en caso de fallo
- Implementar rate limiting para evitar spam
