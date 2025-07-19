#!/bin/bash

# OrganSync Matching Service Build Script
# Provides automated build, test, and deployment capabilities

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check prerequisites
check_prerequisites() {
    log_info "Checking prerequisites..."

    if ! command -v java &> /dev/null; then
        log_error "Java is not installed or not in PATH"
        exit 1
    fi

    if ! command -v mvn &> /dev/null; then
        log_error "Maven is not installed or not in PATH"
        exit 1
    fi

    if ! command -v docker &> /dev/null; then
        log_error "Docker is not installed or not in PATH"
        exit 1
    fi

    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose is not installed or not in PATH"
        exit 1
    fi

    log_success "All prerequisites are met"
}

# Clean previous builds
clean() {
    log_info "Cleaning previous builds..."
    mvn clean
    docker-compose down -v 2>/dev/null || true
    log_success "Clean completed"
}

# Build the application
build() {
    log_info "Building OrganSync Matching Service..."
    mvn compile
    log_success "Build completed"
}

# Run tests
test() {
    log_info "Running tests..."
    mvn test
    log_success "Tests completed"
}

# Package the application
package() {
    log_info "Packaging application..."
    mvn package -DskipTests
    log_success "Package completed"
}

# Build Docker image
docker_build() {
    log_info "Building Docker image..."
    docker build -t organsync/matching-service:latest .
    log_success "Docker image built successfully"
}

# Start infrastructure services
start_infrastructure() {
    log_info "Starting infrastructure services..."
    docker-compose up -d postgres redis kafka zookeeper keycloak

    log_info "Waiting for services to be ready..."
    sleep 30

    # Check if services are ready
    if ! docker-compose ps | grep -q "Up"; then
        log_error "Some services failed to start"
        docker-compose logs
        exit 1
    fi

    log_success "Infrastructure services started"
}

# Run the application
run() {
    log_info "Starting OrganSync Matching Service..."

    # Check if infrastructure is running
    if ! docker-compose ps postgres | grep -q "Up"; then
        log_warning "Infrastructure not running. Starting infrastructure first..."
        start_infrastructure
    fi

    # Run the application
    log_info "Starting application on port 8084..."
    mvn spring-boot:run
}

# Run with Docker
docker_run() {
    log_info "Running with Docker..."
    docker-compose up --build
}

# Stop all services
stop() {
    log_info "Stopping all services..."
    docker-compose down
    log_success "All services stopped"
}

# Run integration tests
integration_test() {
    log_info "Running integration tests..."

    # Start infrastructure
    start_infrastructure

    # Run tests
    mvn verify

    # Stop infrastructure
    stop

    log_success "Integration tests completed"
}

# Development setup
dev_setup() {
    log_info "Setting up development environment..."

    # Start infrastructure
    start_infrastructure

    # Create sample data
    log_info "Creating sample data..."
    # This would load sample data into the database

    log_success "Development environment ready"
    log_info "Access the application at: http://localhost:8084"
    log_info "API documentation: http://localhost:8084/matching/swagger-ui.html"
    log_info "H2 Console: http://localhost:8084/matching/h2-console"
    log_info "Kafka UI: http://localhost:8080"
}

# Show help
show_help() {
    echo "OrganSync Matching Service Build Script"
    echo ""
    echo "Usage: $0 [COMMAND]"
    echo ""
    echo "Commands:"
    echo "  check           Check prerequisites"
    echo "  clean           Clean previous builds"
    echo "  build           Build the application"
    echo "  test            Run unit tests"
    echo "  package         Package the application"
    echo "  docker-build    Build Docker image"
    echo "  run             Run the application locally"
    echo "  docker-run      Run with Docker Compose"
    echo "  infrastructure  Start infrastructure services only"
    echo "  stop            Stop all services"
    echo "  integration     Run integration tests"
    echo "  dev-setup       Setup development environment"
    echo "  full-build      Clean, build, test, and package"
    echo "  help            Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 dev-setup    # Setup development environment"
    echo "  $0 run          # Run the application"
    echo "  $0 test         # Run tests"
    echo "  $0 docker-run   # Run with Docker"
}

# Full build process
full_build() {
    log_info "Starting full build process..."
    clean
    build
    test
    package
    docker_build
    log_success "Full build completed successfully"
}

# Main script logic
case "$1" in
    check)
        check_prerequisites
        ;;
    clean)
        clean
        ;;
    build)
        build
        ;;
    test)
        test
        ;;
    package)
        package
        ;;
    docker-build)
        docker_build
        ;;
    run)
        check_prerequisites
        run
        ;;
    docker-run)
        check_prerequisites
        docker_run
        ;;
    infrastructure)
        check_prerequisites
        start_infrastructure
        ;;
    stop)
        stop
        ;;
    integration)
        check_prerequisites
        integration_test
        ;;
    dev-setup)
        check_prerequisites
        dev_setup
        ;;
    full-build)
        check_prerequisites
        full_build
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        log_error "Unknown command: $1"
        show_help
        exit 1
        ;;
esac