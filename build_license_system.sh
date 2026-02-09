#!/bin/bash

# This script helps compile and integrate the modified license system into the APK
# Prerequisites: Android SDK with build-tools, java, apktool

set -e

echo "=== APK License System Modification Script ==="
echo ""

# Configuration
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
JAVA_SOURCES="$SCRIPT_DIR/java_sources"
BUILD_DIR="$SCRIPT_DIR/build"
DEX_OUTPUT="$BUILD_DIR/classes_license.dex"

# Check prerequisites
check_prerequisites() {
    echo "Checking prerequisites..."
    
    if ! command -v java &> /dev/null; then
        echo "ERROR: Java is not installed"
        exit 1
    fi
    
    echo "✓ Java found: $(java -version 2>&1 | head -n 1)"
    echo ""
}

# Create build directory
setup_build_dir() {
    echo "Setting up build directory..."
    mkdir -p "$BUILD_DIR"
    rm -rf "$BUILD_DIR"/*
    echo "✓ Build directory ready"
    echo ""
}

# Compile Java sources
compile_java() {
    echo "Compiling Java sources..."
    
    # Note: You need to provide the Android SDK android.jar path
    # Example: ANDROID_JAR="/path/to/android-sdk/platforms/android-33/android.jar"
    
    if [ -z "$ANDROID_JAR" ]; then
        echo "WARNING: ANDROID_JAR environment variable not set"
        echo "Please set it to point to your Android SDK's android.jar"
        echo "Example: export ANDROID_JAR=/path/to/android-sdk/platforms/android-33/android.jar"
        echo ""
        echo "Skipping Java compilation..."
        return 1
    fi
    
    # Find all Java files
    find "$JAVA_SOURCES" -name "*.java" > "$BUILD_DIR/sources.txt"
    
    # Compile
    javac -d "$BUILD_DIR/classes" \
          -cp "$ANDROID_JAR" \
          -source 1.8 \
          -target 1.8 \
          @"$BUILD_DIR/sources.txt"
    
    echo "✓ Java compilation complete"
    echo ""
}

# Convert to DEX
convert_to_dex() {
    echo "Converting to DEX format..."
    
    if ! command -v d8 &> /dev/null; then
        echo "WARNING: d8 not found in PATH"
        echo "d8 is part of Android SDK build-tools"
        echo "Add it to PATH or use dx as alternative"
        return 1
    fi
    
    # Convert class files to DEX
    d8 --output "$DEX_OUTPUT" \
       --lib "$ANDROID_JAR" \
       "$BUILD_DIR/classes/**/*.class"
    
    echo "✓ DEX conversion complete"
    echo "✓ Output: $DEX_OUTPUT"
    echo ""
}

# Display instructions
show_instructions() {
    echo "=== Next Steps ==="
    echo ""
    echo "The modified license system has been prepared."
    echo ""
    echo "To integrate into your APK:"
    echo ""
    echo "1. Decompile your APK using apktool:"
    echo "   apktool d your-app.apk -o app-decompiled"
    echo ""
    echo "2. Manual Method - Modify Smali files:"
    echo "   - Use the Java sources in java_sources/ as reference"
    echo "   - Manually edit corresponding smali files in app-decompiled/smali/"
    echo "   - Add LicenseManager class to smali/"
    echo "   - Modify MCApplication to call license check"
    echo ""
    echo "3. Automatic Method - Use JADX + D8:"
    echo "   - Decompile with JADX: jadx -d decompiled classes.dex"
    echo "   - Copy modified files from java_sources/"
    echo "   - Compile back to DEX using d8"
    echo "   - Replace classes.dex in APK"
    echo ""
    echo "4. Rebuild the APK:"
    echo "   apktool b app-decompiled -o modified-app.apk"
    echo ""
    echo "5. Sign the APK:"
    echo "   jarsigner -keystore your-key.jks modified-app.apk alias"
    echo ""
    echo "6. Align the APK (optional but recommended):"
    echo "   zipalign -v 4 modified-app.apk modified-app-aligned.apk"
    echo ""
}

# Main execution
main() {
    check_prerequisites
    setup_build_dir
    
    echo "=== Build Process ==="
    echo ""
    echo "Note: This script requires Android SDK to compile"
    echo "If you don't have Android SDK, you can:"
    echo "  1. Use the provided Java sources as reference"
    echo "  2. Manually edit Smali files in the decompiled APK"
    echo ""
    
    if [ -n "$ANDROID_JAR" ] && [ -f "$ANDROID_JAR" ]; then
        compile_java
        
        if command -v d8 &> /dev/null; then
            convert_to_dex
        else
            echo "Skipping DEX conversion (d8 not available)"
        fi
    else
        echo "Skipping compilation (ANDROID_JAR not set)"
    fi
    
    echo ""
    show_instructions
}

# Run main function
main
