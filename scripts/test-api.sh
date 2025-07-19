#!/bin/bash

# API Test Script for OrganSync Matching Service
# Tests all REST endpoints with sample data

BASE_URL="http://localhost:8084/api/v1/matching"
HEALTH_URL="http://localhost:8084/matching/actuator/health"

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${YELLOW}[TEST]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[PASS]${NC} $1"
}

log_error() {
    echo -e "${RED}[FAIL]${NC} $1"
}

# Test health endpoint
test_health() {
    log_info "Testing health endpoint..."

    response=$(curl -s -o /dev/null -w "%{http_code}" $HEALTH_URL)
    if [ $response -eq 200 ]; then
        log_success "Health check passed"
    else
        log_error "Health check failed (HTTP $response)"
        return 1
    fi
}

# Test get all matches
test_get_matches() {
    log_info "Testing get all matches..."

    response=$(curl -s -o /dev/null -w "%{http_code}" \
        -H "Authorization: Bearer $TOKEN" \
        "$BASE_URL/matches")

    if [ $response -eq 200 ]; then
        log_success "Get matches passed"
    else
        log_error "Get matches failed (HTTP $response)"
        return 1
    fi
}

# Test get statistics
test_get_statistics() {
    log_info "Testing get statistics..."

    response=$(curl -s -o /dev/null -w "%{http_code}" \
        -H "Authorization: Bearer $TOKEN" \
        "$BASE_URL/statistics")

    if [ $response -eq 200 ]; then
        log_success "Get statistics passed"
    else
        log_error "Get statistics failed (HTTP $response)"
        return 1
    fi
}

# Test trigger matching
test_trigger_matching() {
    log_info "Testing trigger matching..."

    response=$(curl -s -o /dev/null -w "%{http_code}" \
        -X POST \
        -H "Authorization: Bearer $TOKEN" \
        "$BASE_URL/matches/trigger")

    if [ $response -eq 200 ]; then
        log_success "Trigger matching passed"
    else
        log_error "Trigger matching failed (HTTP $response)"
        return 1
    fi
}

# Main test execution
main() {
    echo "Starting API tests for OrganSync Matching Service..."
    echo "=============================================="

    # Check if service is running
    if ! test_health; then
        log_error "Service is not running. Start the service first."
        exit 1
    fi

    # Note: For full testing, you would need to get an OAuth token
    # For now, we'll test endpoints that don't require authentication

    # Run tests
    test_health

    # Tests requiring authentication would need TOKEN to be set
    if [ -n "$TOKEN" ]; then
        test_get_matches
        test_get_statistics
        test_trigger_matching
    else
        log_info "Skipping authenticated tests (TOKEN not set)"
        log_info "To run full tests, set TOKEN environment variable"
    fi

    echo "=============================================="
    echo "API tests completed"
}

# Run main function
main "$@