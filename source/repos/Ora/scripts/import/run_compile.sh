#!/bin/bash
# Build script to compile ProfileScreen changes

cd "c:/Users/chris/source/repos/Ora"

echo "============================================================"
echo "Ora Android App - ProfileScreen Debug Compilation"
echo "============================================================"
echo "Working Directory: $(pwd)"
echo "Command: ./gradlew.bat compileDebugKotlin --stacktrace --info"
echo "Time: $(date)"
echo "============================================================"
echo ""

mkdir -p reports/build

# Run gradle compilation and capture output
./gradlew.bat compileDebugKotlin --stacktrace --info 2>&1 | tee reports/build/build-debug-compilation.log

echo ""
echo "============================================================"
echo "Build completed - Log saved to: reports/build/build-debug-compilation.log"
echo "============================================================"
