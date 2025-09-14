#!/bin/bash

# =============================================================================
# GraphQL API Test Script - Agranelos Inventario System
# =============================================================================

# Configuration
GRAPHQL_URL="https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql"
CONTENT_TYPE="Content-Type: application/json"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Test counter
TESTS_PASSED=0
TESTS_FAILED=0

# Function to print test header
print_test_header() {
    echo -e "\n${BLUE}===================================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}===================================================${NC}"
}

# Function to print test result
print_test_result() {
    local test_name="$1"
    local response="$2"
    local should_have_errors="$3"
    
    # Extract status code from response
    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)
    
    echo -e "\n${YELLOW}Test: $test_name${NC}"
    echo "Status Code: $status_code"
    echo "Response:"
    echo "$response_body" | jq . 2>/dev/null || echo "$response_body"
    
    # Check for GraphQL errors
    has_errors=$(echo "$response_body" | jq -e '.errors' >/dev/null 2>&1 && echo "true" || echo "false")
    has_data=$(echo "$response_body" | jq -e '.data' >/dev/null 2>&1 && echo "true" || echo "false")
    
    if [ "$should_have_errors" = "true" ]; then
        # This test should have errors
        if [ "$has_errors" = "true" ]; then
            echo -e "${GREEN}✓ PASSED (Expected errors found)${NC}"
            ((TESTS_PASSED++))
        else
            echo -e "${RED}✗ FAILED (Expected errors but got success)${NC}"
            ((TESTS_FAILED++))
        fi
    else
        # This test should succeed
        if [ "$status_code" = "200" ] && [ "$has_errors" = "false" ] && [ "$has_data" = "true" ]; then
            echo -e "${GREEN}✓ PASSED${NC}"
            ((TESTS_PASSED++))
        else
            echo -e "${RED}✗ FAILED${NC}"
            ((TESTS_FAILED++))
        fi
    fi
    echo "---------------------------------------------------"
}

# Function to make GraphQL request
make_graphql_request() {
    local query="$1"
    curl -s -w "\n%{http_code}" -X POST "$GRAPHQL_URL" \
        -H "$CONTENT_TYPE" \
        -d "$query"
}

# Start testing
echo -e "${GREEN}Starting GraphQL API Tests...${NC}"
echo "GraphQL URL: $GRAPHQL_URL"
echo "Timestamp: $(date)"

# =============================================================================
# BASIC CONNECTIVITY TESTS
# =============================================================================
print_test_header "BASIC CONNECTIVITY TESTS"

# 1. Health Check
HEALTH_QUERY='{
  "query": "query { health }"
}'

response=$(make_graphql_request "$HEALTH_QUERY")
print_test_result "Health Check" "$response" "false"

# 2. Initialize Database
INIT_DB_MUTATION='{
  "query": "mutation { inicializarBaseDatos }"
}'

response=$(make_graphql_request "$INIT_DB_MUTATION")
print_test_result "Initialize Database" "$response" "false"

# Wait for database initialization
sleep 2

# =============================================================================
# PRODUCTO QUERY TESTS
# =============================================================================
print_test_header "PRODUCTO QUERY TESTS"

# 1. Get all products (initially might be empty)
GET_ALL_PRODUCTS='{
  "query": "query { productos { id nombre descripcion precio cantidad fechaCreacion fechaActualizacion } }"
}'

response=$(make_graphql_request "$GET_ALL_PRODUCTS")
print_test_result "Get All Products" "$response" "false"

# 2. Get products with minimal fields
GET_PRODUCTS_MINIMAL='{
  "query": "query { productos { id nombre precio cantidad } }"
}'

response=$(make_graphql_request "$GET_PRODUCTS_MINIMAL")
print_test_result "Get Products (Minimal Fields)" "$response" "false"

# 3. Get product by ID (using variables)
GET_PRODUCT_BY_ID='{
  "query": "query GetProducto($id: ID!) { producto(id: $id) { id nombre descripcion precio cantidad fechaCreacion } }",
  "variables": { "id": "1" }
}'

response=$(make_graphql_request "$GET_PRODUCT_BY_ID")
print_test_result "Get Product by ID (with variables)" "$response" "false"

# =============================================================================
# BODEGA QUERY TESTS
# =============================================================================
print_test_header "BODEGA QUERY TESTS"

# 1. Get all bodegas
GET_ALL_BODEGAS='{
  "query": "query { bodegas { id nombre ubicacion capacidad fechaCreacion fechaActualizacion } }"
}'

response=$(make_graphql_request "$GET_ALL_BODEGAS")
print_test_result "Get All Bodegas" "$response" "false"

# 2. Get bodega by ID
GET_BODEGA_BY_ID='{
  "query": "query GetBodega($id: ID!) { bodega(id: $id) { id nombre ubicacion capacidad } }",
  "variables": { "id": "1" }
}'

response=$(make_graphql_request "$GET_BODEGA_BY_ID")
print_test_result "Get Bodega by ID" "$response" "false"

# =============================================================================
# PRODUCTO MUTATION TESTS
# =============================================================================
print_test_header "PRODUCTO MUTATION TESTS"

# 1. Create product
CREATE_PRODUCT='{
  "query": "mutation CreateProducto($input: ProductoInput!) { crearProducto(input: $input) { success message error producto { id nombre precio cantidad } } }",
  "variables": {
    "input": {
      "nombre": "Producto GraphQL Test",
      "descripcion": "Producto creado via GraphQL para testing",
      "precio": 45.99,
      "cantidad": 200
    }
  }
}'

response=$(make_graphql_request "$CREATE_PRODUCT")
print_test_result "Create Product" "$response" "false"

# Extract product ID from response for subsequent tests
PRODUCT_ID=$(echo "$response" | head -n -1 | jq -r '.data.crearProducto.producto.id // "1"' 2>/dev/null)
echo "Using Product ID: $PRODUCT_ID for subsequent tests"

# 2. Update product
UPDATE_PRODUCT='{
  "query": "mutation UpdateProducto($input: ProductoUpdateInput!) { actualizarProducto(input: $input) { success message error producto { id nombre precio cantidad } } }",
  "variables": {
    "input": {
      "id": "'$PRODUCT_ID'",
      "nombre": "Producto GraphQL Test - Actualizado",
      "descripcion": "Producto actualizado via GraphQL",
      "precio": 55.99,
      "cantidad": 250
    }
  }
}'

response=$(make_graphql_request "$UPDATE_PRODUCT")
print_test_result "Update Product" "$response" "false"

# 3. Create another product for more testing
CREATE_PRODUCT_2='{
  "query": "mutation($input: ProductoInput!) { crearProducto(input: $input) { success message producto { id nombre } } }",
  "variables": {
    "input": {
      "nombre": "Segundo Producto GraphQL",
      "descripcion": "Otro producto para testing",
      "precio": 25.50,
      "cantidad": 100
    }
  }
}'

response=$(make_graphql_request "$CREATE_PRODUCT_2")
print_test_result "Create Second Product" "$response" "false"

# =============================================================================
# BODEGA MUTATION TESTS
# =============================================================================
print_test_header "BODEGA MUTATION TESTS"

# 1. Create bodega
CREATE_BODEGA='{
  "query": "mutation CreateBodega($input: BodegaInput!) { crearBodega(input: $input) { success message error bodega { id nombre ubicacion capacidad } } }",
  "variables": {
    "input": {
      "nombre": "Bodega GraphQL Test",
      "ubicacion": "Zona Industrial GraphQL",
      "capacidad": 6000
    }
  }
}'

response=$(make_graphql_request "$CREATE_BODEGA")
print_test_result "Create Bodega" "$response" "false"

# Extract bodega ID from response
BODEGA_ID=$(echo "$response" | head -n -1 | jq -r '.data.crearBodega.bodega.id // "1"' 2>/dev/null)
echo "Using Bodega ID: $BODEGA_ID for subsequent tests"

# 2. Update bodega
UPDATE_BODEGA='{
  "query": "mutation UpdateBodega($input: BodegaUpdateInput!) { actualizarBodega(input: $input) { success message error bodega { id nombre ubicacion capacidad } } }",
  "variables": {
    "input": {
      "id": "'$BODEGA_ID'",
      "nombre": "Bodega GraphQL Test - Actualizada",
      "ubicacion": "Nueva Zona Industrial GraphQL",
      "capacidad": 9000
    }
  }
}'

response=$(make_graphql_request "$UPDATE_BODEGA")
print_test_result "Update Bodega" "$response" "false"

# =============================================================================
# COMPLEX QUERY TESTS
# =============================================================================
print_test_header "COMPLEX QUERY TESTS"

# 1. Multiple entities in one query
MULTI_QUERY='{
  "query": "query { productos { id nombre precio cantidad } bodegas { id nombre ubicacion capacidad } }"
}'

response=$(make_graphql_request "$MULTI_QUERY")
print_test_result "Multiple Entities Query" "$response" "false"

# 2. Named operation with variables
NAMED_OPERATION='{
  "query": "query GetInventoryOverview($productId: ID!) { producto(id: $productId) { id nombre precio cantidad } productos { id nombre } bodegas { nombre capacidad } }",
  "variables": { "productId": "'$PRODUCT_ID'" },
  "operationName": "GetInventoryOverview"
}'

response=$(make_graphql_request "$NAMED_OPERATION")
print_test_result "Named Operation with Variables" "$response" "false"

# =============================================================================
# ERROR HANDLING TESTS
# =============================================================================
print_test_header "ERROR HANDLING TESTS"

# 1. Invalid field name
INVALID_FIELD='{
  "query": "query { productos { id nombre precioInvalido } }"
}'

response=$(make_graphql_request "$INVALID_FIELD")
print_test_result "Invalid Field Name" "$response" "true"

# 2. Missing required argument
MISSING_ARGUMENT='{
  "query": "query { producto { id nombre } }"
}'

response=$(make_graphql_request "$MISSING_ARGUMENT")
print_test_result "Missing Required Argument" "$response" "true"

# 3. Invalid syntax
INVALID_SYNTAX='{
  "query": "query { productos { id nombre "
}'

response=$(make_graphql_request "$INVALID_SYNTAX")
print_test_result "Invalid Query Syntax" "$response" "true"

# 4. Missing required fields in mutation
MISSING_REQUIRED_FIELDS='{
  "query": "mutation { crearProducto(input: { descripcion: \"Sin nombre\" }) { success message } }"
}'

response=$(make_graphql_request "$MISSING_REQUIRED_FIELDS")
print_test_result "Missing Required Fields in Mutation" "$response" "true"

# 5. Invalid variable type
INVALID_VARIABLE_TYPE='{
  "query": "query($id: String!) { producto(id: $id) { id nombre } }",
  "variables": { "id": 123 }
}'

response=$(make_graphql_request "$INVALID_VARIABLE_TYPE")
print_test_result "Invalid Variable Type" "$response" "true"

# =============================================================================
# CLEANUP TESTS (DELETE OPERATIONS)
# =============================================================================
print_test_header "CLEANUP - DELETE OPERATIONS"

# Delete the created product
DELETE_PRODUCT='{
  "query": "mutation DeleteProducto($id: ID!) { eliminarProducto(id: $id) { success message error } }",
  "variables": { "id": "'$PRODUCT_ID'" }
}'

response=$(make_graphql_request "$DELETE_PRODUCT")
print_test_result "Delete Product" "$response" "false"

# Try to delete the same product again (should fail gracefully)
response=$(make_graphql_request "$DELETE_PRODUCT")
print_test_result "Delete Already Deleted Product" "$response" "false"

# Delete bodega
DELETE_BODEGA='{
  "query": "mutation DeleteBodega($id: ID!) { eliminarBodega(id: $id) { success message error } }",
  "variables": { "id": "'$BODEGA_ID'" }
}'

response=$(make_graphql_request "$DELETE_BODEGA")
print_test_result "Delete Bodega" "$response" "false"

# =============================================================================
# INTROSPECTION TESTS
# =============================================================================
print_test_header "INTROSPECTION TESTS"

# 1. Get schema types
SCHEMA_TYPES='{
  "query": "query { __schema { types { name kind } } }"
}'

response=$(make_graphql_request "$SCHEMA_TYPES")
print_test_result "Schema Introspection - Types" "$response" "false"

# 2. Get specific type information
PRODUCT_TYPE_INFO='{
  "query": "query { __type(name: \"Producto\") { name fields { name type { name kind } } } }"
}'

response=$(make_graphql_request "$PRODUCT_TYPE_INFO")
print_test_result "Type Introspection - Producto" "$response" "false"

# =============================================================================
# TEST SUMMARY
# =============================================================================
print_test_header "TEST SUMMARY"

TOTAL_TESTS=$((TESTS_PASSED + TESTS_FAILED))
echo -e "Total Tests: $TOTAL_TESTS"
echo -e "${GREEN}Tests Passed: $TESTS_PASSED${NC}"
echo -e "${RED}Tests Failed: $TESTS_FAILED${NC}"

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "\n${GREEN}🎉 ALL GRAPHQL TESTS PASSED! 🎉${NC}"
    exit 0
else
    echo -e "\n${RED}❌ SOME GRAPHQL TESTS FAILED ❌${NC}"
    exit 1
fi