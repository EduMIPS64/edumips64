#!/bin/bash
set -e

echo "=================================="
echo "GWT 2.13.0 Bug Reproduction Script"
echo "=================================="
echo ""

cd "$(dirname "$0")"

echo "Step 1: Testing with GWT 2.13.0 (broken version)..."
echo "---------------------------------------------------"
echo "This should fail with InternalCompilerException"
echo ""

# Ensure we're using 2.13.0
sed -i.bak 's/gwt-user:2\.[0-9]*\.[0-9]*/gwt-user:2.13.0/g' build.gradle.kts
sed -i.bak 's/gwt-dev:2\.[0-9]*\.[0-9]*/gwt-dev:2.13.0/g' build.gradle.kts

echo "Running: ./gradlew clean test --tests 'SimpleGWTTest.testSimple'"
if ./gradlew clean test --tests "SimpleGWTTest.testSimple" 2>&1 | tee /tmp/gwt-2.13.0-test.log; then
    echo ""
    echo "❌ UNEXPECTED: Test passed with GWT 2.13.0"
    echo "   The bug may have been fixed!"
else
    echo ""
    echo "✓ EXPECTED: Test failed with GWT 2.13.0"
    echo ""
    echo "Error details:"
    grep -A 5 "InternalCompilerException" /tmp/gwt-2.13.0-test.log | head -20 || echo "(See full log above)"
fi

echo ""
echo ""
echo "Step 2: Testing with GWT 2.12.2 (working version)..."
echo "-----------------------------------------------------"
echo "This should pass successfully"
echo ""

# Switch to 2.12.2
sed -i.bak 's/gwt-user:2\.[0-9]*\.[0-9]*/gwt-user:2.12.2/g' build.gradle.kts
sed -i.bak 's/gwt-dev:2\.[0-9]*\.[0-9]*/gwt-dev:2.12.2/g' build.gradle.kts

echo "Running: ./gradlew clean test --tests 'SimpleGWTTest.testSimple'"
if ./gradlew clean test --tests "SimpleGWTTest.testSimple" 2>&1 | tee /tmp/gwt-2.12.2-test.log; then
    echo ""
    echo "✓ SUCCESS: Test passed with GWT 2.12.2"
    echo "   This confirms the bug is specific to GWT 2.13.0"
else
    echo ""
    echo "❌ UNEXPECTED: Test failed with GWT 2.12.2"
    echo "   There may be a configuration issue"
fi

echo ""
echo "=================================="
echo "Bug Reproduction Complete"
echo "=================================="
echo ""
echo "Summary:"
echo "- GWT 2.13.0: Should fail with InternalCompilerException (double-cast bug)"
echo "- GWT 2.12.2: Should pass successfully"
echo ""
echo "To manually test different versions, edit build.gradle.kts and change:"
echo "  testImplementation(\"org.gwtproject:gwt-user:VERSION\")"
echo "  testImplementation(\"org.gwtproject:gwt-dev:VERSION\")"
echo ""

# Restore original version (2.13.0 to show the bug)
sed -i.bak 's/gwt-user:2\.[0-9]*\.[0-9]*/gwt-user:2.13.0/g' build.gradle.kts
sed -i.bak 's/gwt-dev:2\.[0-9]*\.[0-9]*/gwt-dev:2.13.0/g' build.gradle.kts
rm -f build.gradle.kts.bak
