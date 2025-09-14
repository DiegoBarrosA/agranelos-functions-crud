#!/bin/bash

echo "🧪 Testing GraphQL Bodega Fixes"
echo "==============================="

BASE_URL="http://localhost:7071/api"

# Test 1: Query all bodegas
echo "1. 📋 Testing: Query all bodegas"
curl -s -X POST "$BASE_URL/graphql" \
-H "Content-Type: application/json" \
-d '{
    "query": "query { bodegas { id nombre ubicacion capacidad fechaCreacion fechaActualizacion } }"
}' | jq '.'

echo -e "\n"

# Test 2: Create a new bodega
echo "2. ➕ Testing: Create bodega"
curl -s -X POST "$BASE_URL/graphql" \
-H "Content-Type: application/json" \
-d '{
    "query": "mutation { createBodega(input: { nombre: \"Test Bodega\", ubicacion: \"Test Location\", capacidad: 1000 }) { id nombre ubicacion capacidad fechaCreacion fechaActualizacion } }"
}' | jq '.'

echo -e "\n"

# Test 3: Query single bodega by ID (using ID from previous test if successful)
echo "3. 🔍 Testing: Query bodega by ID (ID: 1)"
curl -s -X POST "$BASE_URL/graphql" \
-H "Content-Type: application/json" \
-d '{
    "query": "query { bodega(id: 1) { id nombre ubicacion capacidad fechaCreacion fechaActualizacion } }"
}' | jq '.'

echo -e "\n"

# Test 4: Update bodega
echo "4. ✏️ Testing: Update bodega"
curl -s -X POST "$BASE_URL/graphql" \
-H "Content-Type: application/json" \
-d '{
    "query": "mutation { updateBodega(id: 1, input: { nombre: \"Updated Test Bodega\", ubicacion: \"Updated Location\", capacidad: 1500 }) { id nombre ubicacion capacidad fechaCreacion fechaActualizacion } }"
}' | jq '.'

echo -e "\n"

# Test 5: Delete bodega
echo "5. 🗑️ Testing: Delete bodega"
curl -s -X POST "$BASE_URL/graphql" \
-H "Content-Type: application/json" \
-d '{
    "query": "mutation { deleteBodega(id: 1) }"
}' | jq '.'

echo -e "\n✅ Bodega GraphQL tests completed!"
echo "If all operations returned proper JSON responses without database errors,"
echo "then the PostgreSQL column case sensitivity fixes are working correctly."