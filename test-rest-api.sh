#!/bin/bash

# =============================================================================
# REST API Test Script - Agranelos Inventario System
# =============================================================================

# Configuration
BASE_URL="https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api"
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
    local expected_status="$3"
    
    # Extract status code from response
    status_code=$(echo "$response" | tail -n1)
    response_body=$(echo "$response" | head -n -1)
    
    echo -e "\n${YELLOW}Test: $test_name${NC}"
    echo "Expected Status: $expected_status"
    echo "Actual Status: $status_code"
    echo "Response:"
    echo "$response_body" | jq . 2>/dev/null || echo "$response_body"
    
    if [ "$status_code" = "$expected_status" ]; then
        echo -e "${GREEN}✓ PASSED${NC}"
        ((TESTS_PASSED++))
    else
        echo -e "${RED}✗ FAILED${NC}"
        ((TESTS_FAILED++))
    fi
    echo "---------------------------------------------------"
}

# Function to make HTTP request with status code
make_request() {
    curl -s -w "\n%{http_code}" "$@"
}

# Start testing
echo -e "${GREEN}Starting REST API Tests...${NC}"
echo "Base URL: $BASE_URL"
echo "Timestamp: $(date)"

# =============================================================================
# DATABASE INITIALIZATION
# =============================================================================
print_test_header "DATABASE INITIALIZATION"

echo "Initializing database..."
response=$(make_request -X POST "$BASE_URL/init" -H "$CONTENT_TYPE")
print_test_result "Initialize Database" "$response" "200"

# Wait a moment for database to be ready
sleep 2

# =============================================================================
# PRODUCTOS TESTS
# =============================================================================
print_test_header "PRODUCTOS - CRUD OPERATIONS"

# 1. Get all products (initially empty)
response=$(make_request -X GET "$BASE_URL/productos" -H "$CONTENT_TYPE")
print_test_result "Get All Products (Initial)" "$response" "200"

# 2. Get product by ID (non-existent)
response=$(make_request -X GET "$BASE_URL/productos/999" -H "$CONTENT_TYPE")
print_test_result "Get Product by Non-existent ID" "$response" "404"

# 3. Create new product
CREATE_PRODUCT_DATA='{
  "nombre": "Producto Test REST",
  "descripcion": "Producto creado para testing REST",
  "precio": 29.99,
  "cantidadEnStock": 100
}'

response=$(make_request -X POST "$BASE_URL/productos" -H "$CONTENT_TYPE" -d "$CREATE_PRODUCT_DATA")
print_test_result "Create Product" "$response" "201"

# Extract product ID from response for subsequent tests
PRODUCT_ID=$(echo "$response" | head -n -1 | jq -r '.id // empty' 2>/dev/null)
if [ -z "$PRODUCT_ID" ]; then
    # Try to extract from message if ID is in the response message
    PRODUCT_ID=$(echo "$response" | head -n -1 | jq -r '.id // empty' 2>/dev/null)
    if [ -z "$PRODUCT_ID" ]; then
        PRODUCT_ID="1" # Fallback to 1
    fi
fi

echo "Using Product ID: $PRODUCT_ID for subsequent tests"

# 4. Get all products (should now have one)
response=$(make_request -X GET "$BASE_URL/productos" -H "$CONTENT_TYPE")
print_test_result "Get All Products (After Create)" "$response" "200"

# 5. Get product by ID (should exist now)
response=$(make_request -X GET "$BASE_URL/productos/$PRODUCT_ID" -H "$CONTENT_TYPE")
print_test_result "Get Product by ID" "$response" "200"

# 6. Update product
UPDATE_PRODUCT_DATA='{
  "nombre": "Producto Test REST - Actualizado",
  "descripcion": "Producto actualizado para testing REST",
  "precio": 39.99,
  "cantidadEnStock": 150
}'

response=$(make_request -X PUT "$BASE_URL/productos/$PRODUCT_ID" -H "$CONTENT_TYPE" -d "$UPDATE_PRODUCT_DATA")
print_test_result "Update Product" "$response" "200"

# 7. Verify update
response=$(make_request -X GET "$BASE_URL/productos/$PRODUCT_ID" -H "$CONTENT_TYPE")
print_test_result "Verify Product Update" "$response" "200"

# 8. Create another product for testing
CREATE_PRODUCT_DATA_2='{
  "nombre": "Segundo Producto Test",
  "descripcion": "Segundo producto para testing",
  "precio": 19.99,
  "cantidadEnStock": 75
}'

response=$(make_request -X POST "$BASE_URL/productos" -H "$CONTENT_TYPE" -d "$CREATE_PRODUCT_DATA_2")
print_test_result "Create Second Product" "$response" "201"

# =============================================================================
# BODEGAS TESTS
# =============================================================================
print_test_header "BODEGAS - CRUD OPERATIONS"

# 1. Get all bodegas (initially empty)
response=$(make_request -X GET "$BASE_URL/bodegas" -H "$CONTENT_TYPE")
print_test_result "Get All Bodegas (Initial)" "$response" "200"

# 2. Create new bodega
CREATE_BODEGA_DATA='{
  "nombre": "Bodega Test REST",
  "ubicacion": "Zona Industrial Test",
  "capacidad": 5000
}'

response=$(make_request -X POST "$BASE_URL/bodegas" -H "$CONTENT_TYPE" -d "$CREATE_BODEGA_DATA")
print_test_result "Create Bodega" "$response" "201"

# Extract bodega ID from response
BODEGA_ID=$(echo "$response" | head -n -1 | jq -r '.id // empty' 2>/dev/null)
if [ -z "$BODEGA_ID" ]; then
    BODEGA_ID="1" # Fallback to 1
fi

echo "Using Bodega ID: $BODEGA_ID for subsequent tests"

# 3. Get all bodegas (should now have one)
response=$(make_request -X GET "$BASE_URL/bodegas" -H "$CONTENT_TYPE")
print_test_result "Get All Bodegas (After Create)" "$response" "200"

# 4. Get bodega by ID
response=$(make_request -X GET "$BASE_URL/bodegas/$BODEGA_ID" -H "$CONTENT_TYPE")
print_test_result "Get Bodega by ID" "$response" "200"

# 5. Update bodega
UPDATE_BODEGA_DATA='{
  "nombre": "Bodega Test REST - Actualizada",
  "ubicacion": "Nueva Zona Industrial",
  "capacidad": 8000
}'

response=$(make_request -X PUT "$BASE_URL/bodegas/$BODEGA_ID" -H "$CONTENT_TYPE" -d "$UPDATE_BODEGA_DATA")
print_test_result "Update Bodega" "$response" "200"

# 6. Verify update
response=$(make_request -X GET "$BASE_URL/bodegas/$BODEGA_ID" -H "$CONTENT_TYPE")
print_test_result "Verify Bodega Update" "$response" "200"

# =============================================================================
# ERROR HANDLING TESTS
# =============================================================================
print_test_header "ERROR HANDLING TESTS"

# 1. Invalid JSON
response=$(make_request -X POST "$BASE_URL/productos" -H "$CONTENT_TYPE" -d '{"invalid": json}')
print_test_result "Invalid JSON" "$response" "400"

# 2. Missing required fields
response=$(make_request -X POST "$BASE_URL/productos" -H "$CONTENT_TYPE" -d '{"descripcion": "Sin nombre"}')
print_test_result "Missing Required Fields" "$response" "400"

# 3. Invalid product ID
response=$(make_request -X GET "$BASE_URL/productos/invalid" -H "$CONTENT_TYPE")
print_test_result "Invalid Product ID Format" "$response" "400"

# 4. Non-existent product update
response=$(make_request -X PUT "$BASE_URL/productos/999" -H "$CONTENT_TYPE" -d "$UPDATE_PRODUCT_DATA")
print_test_result "Update Non-existent Product" "$response" "404"

# =============================================================================
# CLEANUP TESTS (DELETE OPERATIONS)
# =============================================================================
print_test_header "CLEANUP - DELETE OPERATIONS"

# Delete the created products
response=$(make_request -X DELETE "$BASE_URL/productos/$PRODUCT_ID" -H "$CONTENT_TYPE")
print_test_result "Delete First Product" "$response" "200"

# Try to delete the same product again (should fail)
response=$(make_request -X DELETE "$BASE_URL/productos/$PRODUCT_ID" -H "$CONTENT_TYPE")
print_test_result "Delete Already Deleted Product" "$response" "404"

# Delete bodega
response=$(make_request -X DELETE "$BASE_URL/bodegas/$BODEGA_ID" -H "$CONTENT_TYPE")
print_test_result "Delete Bodega" "$response" "200"

# =============================================================================
# TEST SUMMARY
# =============================================================================
print_test_header "TEST SUMMARY"

TOTAL_TESTS=$((TESTS_PASSED + TESTS_FAILED))
echo -e "Total Tests: $TOTAL_TESTS"
echo -e "${GREEN}Tests Passed: $TESTS_PASSED${NC}"
echo -e "${RED}Tests Failed: $TESTS_FAILED${NC}"

if [ $TESTS_FAILED -eq 0 ]; then
    echo -e "\n${GREEN}🎉 ALL TESTS PASSED! 🎉${NC}"
    exit 0
else
    echo -e "\n${RED}❌ SOME TESTS FAILED ❌${NC}"
    exit 1
fi