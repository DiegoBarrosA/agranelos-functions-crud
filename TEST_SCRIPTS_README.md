# API Testing Scripts

This directory contains comprehensive test scripts for the Agranelos Inventario API system.

## Available Scripts

### 1. `test-all-apis.sh` - Main Test Runner
Complete test suite that runs both REST and GraphQL tests with comparison analysis.

```bash
# Run all tests
./test-all-apis.sh

# Run only REST tests  
./test-all-apis.sh rest

# Run only GraphQL tests
./test-all-apis.sh graphql

# Show help
./test-all-apis.sh help
```

### 2. `test-rest-api.sh` - REST API Tests
Comprehensive testing of all REST endpoints including:
- Database initialization
- CRUD operations for Productos
- CRUD operations for Bodegas  
- Error handling scenarios
- Cleanup operations

### 3. `test-graphql-api.sh` - GraphQL API Tests
Complete GraphQL testing including:
- Health checks and connectivity
- Query operations (productos, bodegas)
- Mutation operations (create, update, delete)
- Complex queries and variables
- Error handling and validation
- Schema introspection

### 4. `test-performance.sh` - Performance Testing
Performance comparison between REST and GraphQL:
- Single request response times
- Multiple request statistics
- Data efficiency analysis
- Complex query performance
- Bandwidth usage comparison

## Quick Start

1. **Make scripts executable:**
```bash
chmod +x *.sh
```

2. **Run complete test suite:**
```bash
./test-all-apis.sh
```

3. **Run performance tests:**
```bash
./test-performance.sh
```

## Prerequisites

- **curl**: Required for making HTTP requests
- **jq**: Optional but recommended for JSON formatting
- **bash**: Scripts are written for Bash shell

Install jq for better output formatting:
```bash
# Ubuntu/Debian
sudo apt-get install jq

# macOS
brew install jq

# CentOS/RHEL
sudo yum install jq
```

## Test Coverage

### REST API Endpoints Tested
- `POST /api/init` - Database initialization
- `GET /api/productos` - List all products
- `GET /api/productos/{id}` - Get product by ID
- `POST /api/productos` - Create product
- `PUT /api/productos/{id}` - Update product
- `DELETE /api/productos/{id}` - Delete product
- `GET /api/bodegas` - List all warehouses
- `GET /api/bodegas/{id}` - Get warehouse by ID
- `POST /api/bodegas` - Create warehouse
- `PUT /api/bodegas/{id}` - Update warehouse
- `DELETE /api/bodegas/{id}` - Delete warehouse

### GraphQL Operations Tested
**Queries:**
- `health` - Health check
- `productos` - Get all products
- `producto(id)` - Get product by ID
- `bodegas` - Get all warehouses
- `bodega(id)` - Get warehouse by ID

**Mutations:**
- `inicializarBaseDatos` - Initialize database
- `crearProducto(input)` - Create product
- `actualizarProducto(input)` - Update product
- `eliminarProducto(id)` - Delete product
- `crearBodega(input)` - Create warehouse
- `actualizarBodega(input)` - Update warehouse
- `eliminarBodega(id)` - Delete warehouse

## Output Features

- **Color-coded output** for easy reading
- **Test statistics** showing passed/failed counts
- **Response time measurements** for performance analysis
- **JSON formatting** when jq is available
- **Detailed error reporting** with request/response details
- **API comparison analysis** highlighting strengths of each approach

## Configuration

All scripts are configured to test against the production environment:
- **REST Base URL**: `https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api`
- **GraphQL URL**: `https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql`

To test against a different environment, modify the URL variables at the top of each script.

## Error Handling

The scripts test various error scenarios including:
- Invalid JSON payloads
- Missing required fields
- Non-existent resource IDs
- Invalid field names (GraphQL)
- Missing arguments
- Type validation errors

## Performance Testing

The performance test script provides insights on:
- **Response times**: Average, median, min, max across multiple requests
- **Data efficiency**: Comparing payload sizes between REST and GraphQL
- **Query complexity**: Single vs multiple request scenarios
- **Bandwidth optimization**: GraphQL's selective field querying benefits

## Integration with CI/CD

These scripts can be integrated into CI/CD pipelines:

```yaml
# Example GitHub Actions step
- name: Run API Tests
  run: |
    chmod +x test-all-apis.sh
    ./test-all-apis.sh
```

The scripts return appropriate exit codes (0 for success, 1 for failure) for automation integration.

## Troubleshooting

**Common Issues:**
1. **Connection errors**: Check if the Azure Function App is running
2. **Permission denied**: Run `chmod +x *.sh` to make scripts executable
3. **jq not found warnings**: Install jq or ignore warnings (tests will still work)
4. **Timeout issues**: Azure Functions may have cold start delays

**Debug Mode:**
Add `-x` flag to bash for verbose output:
```bash
bash -x ./test-all-apis.sh
```

## Contributing

When adding new tests:
1. Follow the existing naming conventions
2. Add proper error handling and status code checking
3. Include descriptive test names and expected outcomes
4. Update this README with new test coverage details

## Test Data

Tests create and clean up their own test data:
- Products: "Producto Test REST", "Producto GraphQL Test", etc.
- Warehouses: "Bodega Test REST", "Bodega GraphQL Test", etc.

Data is automatically cleaned up at the end of test runs.