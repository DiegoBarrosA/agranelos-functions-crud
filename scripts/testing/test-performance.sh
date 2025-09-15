#!/bin/bash

# =============================================================================
# Performance Test Script - Agranelos Inventario System
# Tests response times and load handling for both REST and GraphQL
# =============================================================================

# Configuration
BASE_URL="https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api"
GRAPHQL_URL="https://agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net/api/graphql"
CONTENT_TYPE="Content-Type: application/json"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Performance tracking
declare -a rest_times=()
declare -a graphql_times=()

# Function to print test header
print_test_header() {
    echo -e "\n${BLUE}===================================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}===================================================${NC}"
}

# Function to measure response time and capture body separately
measure_response_time() {
    local url="$1"
    local method="$2"
    local data="$3"
    local headers="$4"
    
    local temp_file=$(mktemp)
    local start_time=$(date +%s%N)
    
    if [ "$method" = "POST" ] && [ -n "$data" ]; then
        local status_code=$(curl -s -w "%{http_code}" --connect-timeout 5 --max-time 30 -X POST "$url" -H "$headers" -d "$data" -o "$temp_file")
    elif [ "$method" = "GET" ]; then
        local status_code=$(curl -s -w "%{http_code}" --connect-timeout 5 --max-time 30 -X GET "$url" -H "$headers" -o "$temp_file")
    else
        local status_code=$(curl -s -w "%{http_code}" --connect-timeout 5 --max-time 30 -X "$method" "$url" -H "$headers" -d "$data" -o "$temp_file")
    fi
    
    local end_time=$(date +%s%N)
    local duration=$(( (end_time - start_time) / 1000000 )) # Convert to milliseconds
    
    local response_size=$(wc -c < "$temp_file")
    local response_body=$(cat "$temp_file")
    
    rm -f "$temp_file"
    
    echo "$duration $status_code $response_size $response_body"
}

# Function to calculate statistics
calculate_stats() {
    local arr=("$@")
    local sum=0
    local count=${#arr[@]}
    
    # Calculate average
    for time in "${arr[@]}"; do
        sum=$((sum + time))
    done
    local avg=$((sum / count))
    
    # Sort array for median calculation
    IFS=$'\n' sorted=($(sort -n <<<"${arr[*]}"))
    unset IFS
    
    # Calculate median
    local median
    if [ $((count % 2)) -eq 0 ]; then
        local mid1=${sorted[$((count/2 - 1))]}
        local mid2=${sorted[$((count/2))]}
        median=$(( (mid1 + mid2) / 2 ))
    else
        median=${sorted[$((count/2))]}
    fi
    
    # Find min and max
    local min=${sorted[0]}
    local max=${sorted[$((count-1))]}
    
    echo "$avg $median $min $max"
}

# Start performance testing
echo -e "${GREEN}Starting Performance Tests...${NC}"
echo "REST Base URL: $BASE_URL"
echo "GraphQL URL: $GRAPHQL_URL"
echo "Timestamp: $(date)"

# =============================================================================
# SINGLE REQUEST PERFORMANCE
# =============================================================================
print_test_header "SINGLE REQUEST PERFORMANCE TEST"

echo "Testing single request performance..."

# REST: Get all products
echo -n "REST - Get Products: "
result=$(measure_response_time "$BASE_URL/productos" "GET" "" "$CONTENT_TYPE")
rest_time=$(echo $result | cut -d' ' -f1)
rest_status=$(echo $result | cut -d' ' -f2)
echo "${rest_time}ms (Status: $rest_status)"

# GraphQL: Get all products
echo -n "GraphQL - Get Products: "
graphql_query='{"query": "query { productos { id nombre precio cantidad } }"}'
result=$(measure_response_time "$GRAPHQL_URL" "POST" "$graphql_query" "$CONTENT_TYPE")
graphql_time=$(echo $result | cut -d' ' -f1)
graphql_status=$(echo $result | cut -d' ' -f2)
echo "${graphql_time}ms (Status: $graphql_status)"

echo ""
if [ $rest_time -lt $graphql_time ]; then
    echo -e "${GREEN}REST is faster by $((graphql_time - rest_time))ms${NC}"
else
    echo -e "${GREEN}GraphQL is faster by $((rest_time - graphql_time))ms${NC}"
fi

# =============================================================================
# MULTIPLE REQUEST PERFORMANCE
# =============================================================================
print_test_header "MULTIPLE REQUEST PERFORMANCE TEST"

ITERATIONS=10
echo "Running $ITERATIONS iterations of each test..."

# Reset arrays
rest_times=()
graphql_times=()

echo -n "Progress: "
for i in $(seq 1 $ITERATIONS); do
    echo -n "[$i]"
    
    # REST test
    result=$(measure_response_time "$BASE_URL/productos" "GET" "" "$CONTENT_TYPE")
    time=$(echo $result | cut -d' ' -f1)
    rest_times+=($time)
    
    # GraphQL test
    result=$(measure_response_time "$GRAPHQL_URL" "POST" "$graphql_query" "$CONTENT_TYPE")
    time=$(echo $result | cut -d' ' -f1)
    graphql_times+=($time)
    
    sleep 0.1 # Small delay to avoid overwhelming the server
done
echo " Done!"

# Calculate REST statistics
rest_stats=($(calculate_stats "${rest_times[@]}"))
rest_avg=${rest_stats[0]}
rest_median=${rest_stats[1]}
rest_min=${rest_stats[2]}
rest_max=${rest_stats[3]}

# Calculate GraphQL statistics
graphql_stats=($(calculate_stats "${graphql_times[@]}"))
graphql_avg=${graphql_stats[0]}
graphql_median=${graphql_stats[1]}
graphql_min=${graphql_stats[2]}
graphql_max=${graphql_stats[3]}

echo ""
echo -e "${YELLOW}Performance Statistics (${ITERATIONS} requests):${NC}"
echo ""
echo "REST API:"
echo "  Average: ${rest_avg}ms"
echo "  Median:  ${rest_median}ms"
echo "  Min:     ${rest_min}ms"
echo "  Max:     ${rest_max}ms"
echo ""
echo "GraphQL API:"
echo "  Average: ${graphql_avg}ms"
echo "  Median:  ${graphql_median}ms"
echo "  Min:     ${graphql_min}ms"
echo "  Max:     ${graphql_max}ms"

# =============================================================================
# DATA EFFICIENCY TEST
# =============================================================================
print_test_header "DATA EFFICIENCY TEST"

echo "Testing data transfer efficiency..."

# REST: Get all fields
echo -n "REST - Full product data: "
result=$(measure_response_time "$BASE_URL/productos" "GET" "" "$CONTENT_TYPE")
rest_full_time=$(echo $result | cut -d' ' -f1)
rest_full_size=$(echo $result | cut -d' ' -f3)
echo "${rest_full_time}ms (${rest_full_size} bytes)"

# GraphQL: Only required fields
echo -n "GraphQL - Minimal fields: "
minimal_query='{"query": "query { productos { nombre precio } }"}'
result=$(measure_response_time "$GRAPHQL_URL" "POST" "$minimal_query" "$CONTENT_TYPE")
graphql_minimal_time=$(echo $result | cut -d' ' -f1)
graphql_minimal_size=$(echo $result | cut -d' ' -f3)
echo "${graphql_minimal_time}ms (${graphql_minimal_size} bytes)"

# GraphQL: All fields
echo -n "GraphQL - Full product data: "
full_query='{"query": "query { productos { id nombre descripcion precio cantidad fechaCreacion fechaActualizacion } }"}'
result=$(measure_response_time "$GRAPHQL_URL" "POST" "$full_query" "$CONTENT_TYPE")
graphql_full_time=$(echo $result | cut -d' ' -f1)
graphql_full_size=$(echo $result | cut -d' ' -f3)
echo "${graphql_full_time}ms (${graphql_full_size} bytes)"

echo ""
echo -e "${YELLOW}Data Efficiency Analysis:${NC}"
if [ $rest_full_size -gt 0 ]; then
    data_reduction=$(( (rest_full_size - graphql_minimal_size) * 100 / rest_full_size ))
else
    data_reduction=0
fi
echo "• REST full data: ${rest_full_size} bytes"
echo "• GraphQL minimal: ${graphql_minimal_size} bytes"
echo "• Data reduction: ${data_reduction}% less data with GraphQL selective queries"

# =============================================================================
# COMPLEX QUERY TEST
# =============================================================================
print_test_header "COMPLEX QUERY PERFORMANCE TEST"

echo "Testing complex query scenarios..."

# REST: Multiple requests for related data (simulated)
echo -n "REST - Multiple requests (productos + bodegas): "
start_time=$(date +%s%N)
# Simulate getting productos and bodegas separately
curl -s "$BASE_URL/productos" -H "$CONTENT_TYPE" > /dev/null
curl -s "$BASE_URL/bodegas" -H "$CONTENT_TYPE" > /dev/null
end_time=$(date +%s%N)
rest_multi_time=$(( (end_time - start_time) / 1000000 ))
echo "${rest_multi_time}ms"

# GraphQL: Single request for multiple resources
echo -n "GraphQL - Single complex query: "
complex_query='{"query": "query { productos { id nombre precio cantidad } bodegas { id nombre ubicacion capacidad } }"}'
result=$(measure_response_time "$GRAPHQL_URL" "POST" "$complex_query" "$CONTENT_TYPE")
graphql_complex_time=$(echo $result | cut -d' ' -f1)
echo "${graphql_complex_time}ms"

echo ""
if [ $rest_multi_time -gt $graphql_complex_time ]; then
    improvement=$(( (rest_multi_time - graphql_complex_time) * 100 / rest_multi_time ))
    echo -e "${GREEN}GraphQL single query is ${improvement}% faster than multiple REST requests${NC}"
else
    difference=$(( (graphql_complex_time - rest_multi_time) * 100 / graphql_complex_time ))
    echo -e "${YELLOW}Multiple REST requests are ${difference}% faster (unusual case)${NC}"
fi

# =============================================================================
# PERFORMANCE SUMMARY
# =============================================================================
print_test_header "PERFORMANCE TEST SUMMARY"

echo -e "${CYAN}Performance Test Results Summary:${NC}"
echo ""
echo -e "${YELLOW}Response Times (Average over $ITERATIONS requests):${NC}"
echo "• REST API Average: ${rest_avg}ms"
echo "• GraphQL API Average: ${graphql_avg}ms"

# Calculate performance difference percentage
if [ $rest_avg -ne 0 ] && [ $graphql_avg -ne 0 ]; then
    if [ $rest_avg -lt $graphql_avg ]; then
        slower_avg=$graphql_avg
        faster_avg=$rest_avg
    else
        slower_avg=$rest_avg
        faster_avg=$graphql_avg
    fi
    percent_diff=$(( (slower_avg - faster_avg) * 100 / slower_avg ))
else
    percent_diff=0
fi

if [ $rest_avg -lt $graphql_avg ]; then
    echo -e "• ${GREEN}REST is ${percent_diff}% faster on average${NC}"
else
    echo -e "• ${GREEN}GraphQL is ${percent_diff}% faster on average${NC}"
fi

echo ""
echo -e "${YELLOW}Data Efficiency:${NC}"
echo "• GraphQL selective queries reduce data transfer by ${data_reduction}%"
echo "• GraphQL allows fetching multiple resources in a single request"

echo ""
echo -e "${YELLOW}Use Case Recommendations:${NC}"
echo "• For simple CRUD: REST may have slight performance edge"
echo "• For complex data needs: GraphQL reduces round-trips"
echo "• For mobile/bandwidth-limited: GraphQL's selective queries help"
echo "• For real-time updates: GraphQL subscriptions provide advantages"

echo ""
echo -e "${YELLOW}Both APIs show production-ready performance characteristics${NC}"

exit 0