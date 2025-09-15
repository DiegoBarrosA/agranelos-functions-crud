#!/bin/bash

# Configuration with environment variable support
REST_BASE_URL="${REST_API_URL:-http://localhost:7071/api}"
GRAPHQL_URL="${GRAPHQL_API_URL:-http://localhost:7071/api/graphql}"

# Production environment guard
if [[ "$REST_BASE_URL" == *"agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net"* ]] || [[ "$GRAPHQL_URL" == *"agranelos-fybpb6duaadaaxfm.eastus2-01.azurewebsites.net"* ]]; then
    if [[ "$CONFIRM_PRODUCTION" != "true" ]] && [[ "$1" != "--confirm-production" ]]; then
        echo "⚠️  WARNING: You are about to run tests against PRODUCTION environment!"
        echo "   REST URL: $REST_BASE_URL"
        echo "   GraphQL URL: $GRAPHQL_URL"
        echo ""
        echo "To proceed, either:"
        echo "  1. Run with flag: $0 --confirm-production"
        echo "  2. Set environment variable: CONFIRM_PRODUCTION=true $0"
        echo ""
        exit 1
    fi
fi

# =============================================================================
# Combined API Test Script - Agranelos Inventario System
# Tests both REST and GraphQL endpoints
# =============================================================================

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Configuration
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REST_SCRIPT="$SCRIPT_DIR/test-rest-api.sh"
GRAPHQL_SCRIPT="$SCRIPT_DIR/test-graphql-api.sh"

# Function to print main header
print_main_header() {
    # Determine environment name
    local env_name="Local Development"
    if [[ "$REST_BASE_URL" == *"staging"* ]] || [[ "$GRAPHQL_URL" == *"staging"* ]]; then
        env_name="Staging"
    elif [[ "$REST_BASE_URL" == *"agranelos-fybpb6duaadaaxfm"* ]] || [[ "$GRAPHQL_URL" == *"agranelos-fybpb6duaadaaxfm"* ]]; then
        env_name="Production"
    fi
    
    echo -e "${CYAN}=============================================================================${NC}"
    echo -e "${CYAN}                    AGRANELOS INVENTARIO API TEST SUITE${NC}"
    echo -e "${CYAN}                      REST + GraphQL Comprehensive Testing${NC}"
    echo -e "${CYAN}=============================================================================${NC}"
    echo -e "Timestamp: $(date)"
    echo -e "Testing Environment: $env_name"
    echo -e "REST Base URL: $REST_BASE_URL"
    echo -e "GraphQL URL: $GRAPHQL_URL"
    echo ""
}

# Function to check prerequisites
check_prerequisites() {
    echo -e "${YELLOW}Checking Prerequisites...${NC}"
    
    # Check if curl is available
    if ! command -v curl &> /dev/null; then
        echo -e "${RED}Error: curl is not installed or not in PATH${NC}"
        exit 1
    fi
    
    # Check if jq is available
    if ! command -v jq &> /dev/null; then
        echo -e "${YELLOW}Warning: jq is not installed. JSON responses may not be pretty-printed${NC}"
        echo -e "${YELLOW}Install jq for better output formatting: sudo apt-get install jq${NC}"
    fi
    
    # Check if test scripts exist and are executable
    if [ ! -f "$REST_SCRIPT" ]; then
        echo -e "${RED}Error: REST test script not found: $REST_SCRIPT${NC}"
        exit 1
    fi
    
    if [ ! -f "$GRAPHQL_SCRIPT" ]; then
        echo -e "${RED}Error: GraphQL test script not found: $GRAPHQL_SCRIPT${NC}"
        exit 1
    fi
    
    # Make scripts executable
    chmod +x "$REST_SCRIPT" "$GRAPHQL_SCRIPT"
    
    echo -e "${GREEN}✓ All prerequisites satisfied${NC}"
    echo ""
}

# Function to run tests with timing
run_test_suite() {
    local test_name="$1"
    local script_path="$2"
    local start_time=$(date +%s)
    
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════════════════════${NC}"
    echo -e "${BLUE}                              $test_name${NC}"
    echo -e "${BLUE}═══════════════════════════════════════════════════════════════════════════════${NC}"
    
    # Run the test script
    if bash "$script_path"; then
        local end_time=$(date +%s)
        local duration=$((end_time - start_time))
        echo -e "\n${GREEN}✓ $test_name completed successfully in ${duration}s${NC}"
        return 0
    else
        local end_time=$(date +%s)
        local duration=$((end_time - start_time))
        echo -e "\n${RED}✗ $test_name failed after ${duration}s${NC}"
        return 1
    fi
}

# Function to print comparison summary
print_comparison_summary() {
    echo -e "\n${CYAN}═══════════════════════════════════════════════════════════════════════════════${NC}"
    echo -e "${CYAN}                              API COMPARISON SUMMARY${NC}"
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════════════════════${NC}"
    
    echo -e "\n${YELLOW}REST API Characteristics:${NC}"
    echo -e "• ✅ Simple HTTP methods (GET, POST, PUT, DELETE)"
    echo -e "• ✅ Familiar request/response pattern"
    echo -e "• ✅ Easy caching and HTTP status codes"
    echo -e "• ✅ Mature tooling and widespread adoption"
    echo -e "• ❌ Over-fetching (getting unnecessary data)"
    echo -e "• ❌ Multiple requests for related data"
    echo -e "• ❌ API versioning challenges"
    
    echo -e "\n${YELLOW}GraphQL API Characteristics:${NC}"
    echo -e "• ✅ Request exactly the data you need"
    echo -e "• ✅ Single request for multiple resources"
    echo -e "• ✅ Strong type system and schema introspection"
    echo -e "• ✅ Real-time subscriptions capability"
    echo -e "• ✅ Self-documenting with built-in tools"
    echo -e "• ❌ More complex caching strategies"
    echo -e "• ❌ Learning curve for new developers"
    echo -e "• ❌ Potential for expensive queries"
    
    echo -e "\n${YELLOW}Use REST when:${NC}"
    echo -e "• Simple CRUD operations are sufficient"
    echo -e "• Caching is critical"
    echo -e "• Team is more familiar with REST"
    echo -e "• File uploads are primary use case"
    
    echo -e "\n${YELLOW}Use GraphQL when:${NC}"
    echo -e "• Complex data fetching requirements"
    echo -e "• Multiple clients with different data needs"
    echo -e "• Rapid frontend development is priority"
    echo -e "• Real-time features are needed"
}

# Function to print final summary
print_final_summary() {
    local rest_success=$1
    local graphql_success=$2
    local total_time=$3
    
    echo -e "\n${CYAN}═══════════════════════════════════════════════════════════════════════════════${NC}"
    echo -e "${CYAN}                                FINAL TEST SUMMARY${NC}"
    echo -e "${CYAN}═══════════════════════════════════════════════════════════════════════════════${NC}"
    
    echo -e "\nTest Results:"
    if [ $rest_success -eq 0 ]; then
        echo -e "• REST API Tests: ${GREEN}✓ PASSED${NC}"
    else
        echo -e "• REST API Tests: ${RED}✗ FAILED${NC}"
    fi
    
    if [ $graphql_success -eq 0 ]; then
        echo -e "• GraphQL API Tests: ${GREEN}✓ PASSED${NC}"
    else
        echo -e "• GraphQL API Tests: ${RED}✗ FAILED${NC}"
    fi
    
    echo -e "\nTotal Execution Time: ${total_time}s"
    echo -e "Both APIs tested against production environment"
    
    if [ $rest_success -eq 0 ] && [ $graphql_success -eq 0 ]; then
        echo -e "\n${GREEN}🎉 ALL API TESTS PASSED! Both REST and GraphQL APIs are working correctly! 🎉${NC}"
        echo -e "${GREEN}Your Agranelos Inventario System is ready for production use!${NC}"
        return 0
    else
        echo -e "\n${RED}❌ SOME API TESTS FAILED${NC}"
        echo -e "${RED}Please check the detailed output above for specific issues.${NC}"
        return 1
    fi
}

# Main execution
main() {
    local start_time=$(date +%s)
    
    print_main_header
    check_prerequisites
    
    # Parse command line arguments
    local run_rest=true
    local run_graphql=true
    
    case "${1:-}" in
        "rest")
            run_graphql=false
            echo -e "${YELLOW}Running REST tests only${NC}\n"
            ;;
        "graphql")
            run_rest=false
            echo -e "${YELLOW}Running GraphQL tests only${NC}\n"
            ;;
        "help"|"-h"|"--help")
            echo -e "${YELLOW}Usage:${NC}"
            echo -e "  $0          - Run both REST and GraphQL tests"
            echo -e "  $0 rest     - Run only REST tests"
            echo -e "  $0 graphql  - Run only GraphQL tests"
            echo -e "  $0 help     - Show this help message"
            exit 0
            ;;
        "")
            echo -e "${YELLOW}Running both REST and GraphQL tests${NC}\n"
            ;;
        *)
            echo -e "${RED}Unknown argument: $1${NC}"
            echo -e "Use '$0 help' for usage information"
            exit 1
            ;;
    esac
    
    local rest_success=0
    local graphql_success=0
    
    # Run REST tests
    if [ "$run_rest" = true ]; then
        if ! run_test_suite "REST API TESTS" "$REST_SCRIPT"; then
            rest_success=1
        fi
    fi
    
    # Run GraphQL tests
    if [ "$run_graphql" = true ]; then
        if ! run_test_suite "GRAPHQL API TESTS" "$GRAPHQL_SCRIPT"; then
            graphql_success=1
        fi
    fi
    
    # Print comparison only if both APIs were tested
    if [ "$run_rest" = true ] && [ "$run_graphql" = true ]; then
        print_comparison_summary
    fi
    
    # Final summary
    local end_time=$(date +%s)
    local total_time=$((end_time - start_time))
    
    print_final_summary $rest_success $graphql_success $total_time
    
    # Exit with appropriate code
    if [ $rest_success -eq 0 ] && [ $graphql_success -eq 0 ]; then
        exit 0
    else
        exit 1
    fi
}

# Execute main function with all arguments
main "$@"