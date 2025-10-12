# Container Configuration

This directory contains Docker/Podman configuration for local development and testing of the Agranelos Inventario Functions.

## Quick Start

### Prerequisites
- Docker or Podman installed
- Java 17+ and Maven 3.6+ for building
- PostgreSQL client (optional, for direct DB access)

### Build and Run

```bash
# Build the functions
mvn clean package

# Start all services
docker-compose up -d

# Or with Podman
podman-compose up -d

# For full-stack testing (includes BFF)
docker-compose --profile full-stack up -d
```

## Services

### azure-functions
- **Port**: 7071 (mapped to container port 80)
- **Purpose**: Runs Azure Functions locally using Microsoft's runtime
- **Health Check**: `/api/GetProductos` endpoint
- **Dependencies**: PostgreSQL database

### postgres
- **Port**: 5432
- **Database**: `agranelos_inventario`
- **User**: `agranelos` (configurable via env vars)
- **Initialization**: Automatically runs `schema.sql` on first start

### bff (full-stack profile)
- **Port**: 8080
- **Purpose**: Integrated testing with BFF microservice
- **Dependencies**: Azure Functions service

## Configuration

### Environment Variables

Create a `.env` file in this directory:

```bash
# Database Configuration
POSTGRES_DB=agranelos_inventario
POSTGRES_USER=agranelos
POSTGRES_PASSWORD=myStrongPassword123

# Optional: Customize ports
FUNCTIONS_PORT=7071
POSTGRES_PORT=5432
BFF_PORT=8080
```

### Volume Mounts

- **Functions**: `./target/azure-functions/agranelos-inventario-functions` → `/home/site/wwwroot` (read-only)
- **Database**: Named volume `postgres_data` for persistence
- **Schema**: `./schema.sql` → `/docker-entrypoint-initdb.d/01-schema.sql` (read-only)

## Development Workflow

### 1. Code Changes
```bash
# Rebuild functions after code changes
mvn clean package

# Restart functions container
docker-compose restart azure-functions
```

### 2. Database Changes
```bash
# Stop services
docker-compose down

# Remove database volume (WARNING: data loss)
docker volume rm agranelos-functions-crud-create_postgres_data

# Update schema.sql as needed
# Restart services
docker-compose up -d
```

### 3. Full-Stack Testing
```bash
# Run with BFF integration
docker-compose --profile full-stack up -d

# Test complete workflow
curl http://localhost:8080/api/v1/productos
```

## Troubleshooting

### Common Issues

1. **Functions not starting**: Check that `mvn package` completed successfully
2. **Database connection failed**: Verify PostgreSQL is running and credentials are correct
3. **Port conflicts**: Ensure ports 5432, 7071, and 8080 are available

### Logs and Debugging

```bash
# View all service logs
docker-compose logs

# Follow specific service logs
docker-compose logs -f azure-functions
docker-compose logs -f postgres

# Execute commands in containers
docker-compose exec postgres psql -U agranelos -d agranelos_inventario
docker-compose exec azure-functions /bin/sh
```

### Health Checks

```bash
# Check service health
docker-compose ps

# Test endpoints directly
curl http://localhost:7071/api/GetProductos
curl http://localhost:7071/api/GetBodegas

# Database connectivity
curl http://localhost:7071/api/InitializeDatabase
```

## Security Notes

- Database uses strong password (change in production)
- Functions run with minimal privileges
- Read-only volume mounts where appropriate
- Network isolation between services

## Production Considerations

This configuration is designed for local development. For production deployment:

1. Use Azure Container Instances or App Service
2. Configure Azure Database for PostgreSQL
3. Set up proper secrets management
4. Enable HTTPS/TLS
5. Configure monitoring and logging