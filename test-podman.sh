#!/bin/bash

# Podman Test Wrapper for Agranelos Inventario System
# Reemplaza los scripts bash nativos con contenedores Podman

set -euo pipefail

# Configuration
CONTAINER_NAME="agranelos-testing"
IMAGE_NAME="agranelos-tests:latest"
RESULTS_DIR="./test-results"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Function to print header
print_header() {
    echo -e "${CYAN}=============================================================================${NC}"
    echo -e "${CYAN}  Agranelos Inventario - Containerized Testing with Podman${NC}"
    echo -e "${CYAN}=============================================================================${NC}"
}

# Function to check if podman is installed
check_podman() {
    if ! command -v podman &> /dev/null; then
        echo -e "${RED}ERROR: Podman is not installed or not in PATH${NC}"
        echo -e "${YELLOW}Please install Podman to use containerized testing${NC}"
        echo -e "${YELLOW}Falling back to native bash scripts...${NC}"
        echo ""
        exec ./scripts/testing/test-all-apis.sh "$@"
    fi
}

# Function to build test image
build_image() {
    echo -e "${BLUE}Building test container image...${NC}"
    if ! podman build -t "$IMAGE_NAME" -f Containerfile .; then
        echo -e "${RED}ERROR: Failed to build container image${NC}"
        exit 1
    fi
    echo -e "${GREEN}Container image built successfully${NC}"
    echo ""
}

# Function to create results directory
setup_results() {
    mkdir -p "$RESULTS_DIR"
    chmod 755 "$RESULTS_DIR"
}

# Function to run tests in container
run_tests() {
    local test_args=()
    
    # Pass through command line arguments
    for arg in "$@"; do
        test_args+=("$arg")
    done
    
    echo -e "${BLUE}Running tests in container...${NC}"
    echo -e "${YELLOW}Container: $CONTAINER_NAME${NC}"
    echo -e "${YELLOW}Image: $IMAGE_NAME${NC}"
    echo -e "${YELLOW}Results: $RESULTS_DIR${NC}"
    echo ""
    
    # Remove existing container if it exists
    podman rm -f "$CONTAINER_NAME" 2>/dev/null || true
    
    # Run tests with volume mount for results
    if podman run \
        --name "$CONTAINER_NAME" \
        --rm \
        --network host \
        -v "$(pwd)/$RESULTS_DIR:/workspace/results:Z" \
        -e "REST_API_URL=${REST_API_URL:-http://localhost:7071/api}" \
        -e "GRAPHQL_API_URL=${GRAPHQL_API_URL:-http://localhost:7071/api/graphql}" \
        -e "CONFIRM_PRODUCTION=${CONFIRM_PRODUCTION:-}" \
        "$IMAGE_NAME" \
        "${test_args[@]}"; then
        
        echo -e "${GREEN}Tests completed successfully${NC}"
        
        # Show results if available
        if [[ -f "$RESULTS_DIR/test-summary.txt" ]]; then
            echo -e "${CYAN}Test Summary:${NC}"
            cat "$RESULTS_DIR/test-summary.txt"
        fi
        
        return 0
    else
        echo -e "${RED}Tests failed${NC}"
        
        # Show error logs if available
        if [[ -f "$RESULTS_DIR/test-errors.txt" ]]; then
            echo -e "${RED}Error Details:${NC}"
            cat "$RESULTS_DIR/test-errors.txt"
        fi
        
        return 1
    fi
}

# Function to show usage
show_usage() {
    cat << EOF
Usage: $0 [OPTIONS] [TEST_ARGS]

Containerized testing wrapper for Agranelos Inventario System using Podman.

OPTIONS:
    --rebuild           Rebuild container image before running tests
    --native           Use native bash scripts instead of containers
    --help             Show this help message

TEST_ARGS:
    Any arguments will be passed through to the test container

ENVIRONMENT VARIABLES:
    REST_API_URL       Base URL for REST API (default: http://localhost:7071/api)
    GRAPHQL_API_URL    Base URL for GraphQL API (default: http://localhost:7071/api/graphql)
    CONFIRM_PRODUCTION Set to 'true' to allow testing against production

EXAMPLES:
    $0                               # Run all tests with default settings
    $0 --rebuild                     # Rebuild image and run tests
    $0 --confirm-production          # Allow production testing
    $0 --native                      # Use native bash scripts
    
    # With custom URLs
    REST_API_URL=https://prod.example.com/api $0
    
EOF
}

# Main execution
main() {
    local rebuild=false
    local use_native=false
    local test_args=()
    
    # Parse command line arguments
    while [[ $# -gt 0 ]]; do
        case $1 in
            --rebuild)
                rebuild=true
                shift
                ;;
            --native)
                use_native=true
                shift
                ;;
            --help|-h)
                show_usage
                exit 0
                ;;
            *)
                test_args+=("$1")
                shift
                ;;
        esac
    done
    
    print_header
    
    # Use native scripts if requested
    if [[ "$use_native" == "true" ]]; then
        echo -e "${YELLOW}Using native bash scripts...${NC}"
        exec ./scripts/testing/test-all-apis.sh "${test_args[@]}"
    fi
    
    check_podman
    setup_results
    
    # Build image if needed
    if [[ "$rebuild" == "true" ]] || ! podman image exists "$IMAGE_NAME"; then
        build_image
    fi
    
    # Run tests
    run_tests "${test_args[@]}"
}

# Execute main function with all arguments
main "$@"