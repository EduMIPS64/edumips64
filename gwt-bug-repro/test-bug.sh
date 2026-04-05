#!/bin/bash
# Reproduces: gwt-user:2.12.2 + gwt-dev:2.13.0 version mismatch
# causing InternalCompilerException / NoClassDefFoundError in GWTTestCase tests.
set -e

cd "$(dirname "$0")"

echo "============================================="
echo "GWT version-mismatch bug reproduction"
echo "============================================="
echo ""

# ── Step 1: mismatch (should FAIL) ──────────────────────────────────
echo "Step 1: gwt-user 2.12.2 + gwt-dev 2.13.0  (MISMATCHED — expect failure)"
echo "------------------------------------------------------------------------"

sed -i 's/val gwtUserVersion = .*/val gwtUserVersion = "2.12.2"/' build.gradle.kts
sed -i 's/val gwtDevVersion = .*/val gwtDevVersion = "2.13.0"/'   build.gradle.kts

if ./gradlew clean test --tests "SimpleGWTTest" 2>&1; then
    echo ""
    echo "❌  UNEXPECTED — test passed; the bug may have been fixed."
    step1=PASS
else
    echo ""
    echo "✅  EXPECTED — test failed (version mismatch triggers the bug)."
    step1=FAIL
fi

echo ""

# ── Step 2: both at 2.12.2 (should PASS) ───────────────────────────
echo "Step 2: gwt-user 2.12.2 + gwt-dev 2.12.2  (MATCHED — expect success)"
echo "----------------------------------------------------------------------"

sed -i 's/val gwtDevVersion = .*/val gwtDevVersion = "2.12.2"/' build.gradle.kts

if ./gradlew clean test --tests "SimpleGWTTest" 2>&1; then
    echo ""
    echo "✅  EXPECTED — test passed."
    step2=PASS
else
    echo ""
    echo "❌  UNEXPECTED — test failed even with matched versions."
    step2=FAIL
fi

echo ""

# ── Step 3: both at 2.13.0 (should PASS) ───────────────────────────
echo "Step 3: gwt-user 2.13.0 + gwt-dev 2.13.0  (MATCHED — expect success)"
echo "----------------------------------------------------------------------"

sed -i 's/val gwtUserVersion = .*/val gwtUserVersion = "2.13.0"/' build.gradle.kts
sed -i 's/val gwtDevVersion = .*/val gwtDevVersion = "2.13.0"/'   build.gradle.kts

if ./gradlew clean test --tests "SimpleGWTTest" 2>&1; then
    echo ""
    echo "✅  EXPECTED — test passed."
    step3=PASS
else
    echo ""
    echo "❌  UNEXPECTED — test failed even with matched versions."
    step3=FAIL
fi

# ── Reset to the mismatched state (to show the bug) ────────────────
sed -i 's/val gwtUserVersion = .*/val gwtUserVersion = "2.12.2"/' build.gradle.kts
sed -i 's/val gwtDevVersion = .*/val gwtDevVersion = "2.13.0"/'   build.gradle.kts

echo ""
echo "============================================="
echo "Results"
echo "============================================="
echo "  Step 1 (mismatch 2.12.2 / 2.13.0): $step1  (expected: FAIL)"
echo "  Step 2 (matched  2.12.2 / 2.12.2): $step2  (expected: PASS)"
echo "  Step 3 (matched  2.13.0 / 2.13.0): $step3  (expected: PASS)"
echo ""
