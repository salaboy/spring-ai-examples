#!/bin/bash

# Script to build and run the Chain Workflow example with verification
# Usage: ./run-chain-workflow.sh

set -e

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

SCRIPT_DIR=$(dirname "$(readlink -f "$0")")
cd "$SCRIPT_DIR"

# Function to display section header
function section() {
    echo -e "\n${YELLOW}======== $1 ========${NC}"
}

# Build the project
section "Building Chain Workflow"
echo "Running mvn clean package..."
./mvnw clean package

# Run the application and capture output
section "Running Chain Workflow"
echo "Starting the application..."
OUTPUT_FILE=$(mktemp)
./mvnw spring-boot:test-run | tee "$OUTPUT_FILE"

# Verify the output
section "Verifying Output"

# Check if markdown table is present
if ! grep -q "| Metric | Value |" "$OUTPUT_FILE"; then
    echo -e "${RED}ERROR: Markdown table header not found in output${NC}"
    rm "$OUTPUT_FILE"
    exit 1
fi

# Check for expected metrics in the output
EXPECTED_METRICS=(
    "Customer Satisfaction"
    "Employee Satisfaction"
    "Product Adoption Rate"
    "Revenue Growth"
    "Operating Margin"
    "Market Share"
    "Customer Churn"
)

ERRORS=0
for metric in "${EXPECTED_METRICS[@]}"; do
    if ! grep -q "| $metric |" "$OUTPUT_FILE"; then
        echo -e "${RED}ERROR: Expected metric not found: $metric${NC}"
        ERRORS=$((ERRORS+1))
    fi
done

# Verify sorting (highest values should come first)
# This is a basic check - values should decrease as we go down the table
if grep -A1 "Customer Satisfaction" "$OUTPUT_FILE" | grep -q "Employee Satisfaction" && \
   grep -A1 "Employee Satisfaction" "$OUTPUT_FILE" | grep -q "Product Adoption Rate"; then
    echo -e "${GREEN}✓ Metrics appear to be correctly sorted by value${NC}"
else
    echo -e "${RED}ERROR: Metrics don't appear to be correctly sorted${NC}"
    ERRORS=$((ERRORS+1))
fi

# Check for completion status
if grep -q "BUILD SUCCESS" "$OUTPUT_FILE"; then
    echo -e "${GREEN}✓ Application completed successfully${NC}"
else
    echo -e "${RED}ERROR: Application did not complete successfully${NC}"
    ERRORS=$((ERRORS+1))
fi

# Clean up
rm "$OUTPUT_FILE"

# Final result
if [ $ERRORS -eq 0 ]; then
    echo -e "\n${GREEN}✓ Chain Workflow executed and verified successfully!${NC}"
    exit 0
else
    echo -e "\n${RED}× Chain Workflow verification failed with $ERRORS errors!${NC}"
    exit 1
fi