# Complete Integration Guide

This guide explains how to integrate the modified license system into your APK.

## Overview of Changes

We've created a complete license system that:
1. Uses a fixed license value: **"lasherinamk"**
2. Validates the license locally (no server communication)
3. Checks license once every 300 years
4. Automatically bypasses clipboard login screens
5. Intercepts all clipboard operations to provide the correct license

## Files Added/Modified

### New Files:
- `java_sources/com/miniclip/license/LicenseManager.java`
- `java_sources/com/miniclip/license/ClipboardInterceptor.java`
- `java_sources/com/miniclip/license/test/LicenseTest.java` (optional testing)

### Modified Files:
- `java_sources/com/miniclip/platform/MCApplication.java`
- `java_sources/com/miniclip/nativeJNI/cocojava.java`

## Integration Steps

### Method 1: Using JADX and D8 (Recommended for Java sources)

#### Step 1: Install Prerequisites
```bash
# Install Java
sudo apt-get install openjdk-11-jdk

# Download JADX
wget https://github.com/skylot/jadx/releases/download/v1.5.1/jadx-1.5.1.zip
unzip jadx-1.5.1.zip
chmod +x bin/jadx

# Download Android SDK command-line tools (for d8)
wget https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
unzip commandlinetools-linux-11076708_latest.zip
```

#### Step 2: Decompile Your APK
```bash
# If you have an APK
/path/to/jadx/bin/jadx -d output_dir your_app.apk

# If you have DEX files
/path/to/jadx/bin/jadx -d output_dir classes.dex classes2.dex ...
```

#### Step 3: Copy Modified Files
```bash
# Copy the new license system files
cp java_sources/com/miniclip/license/*.java output_dir/sources/com/miniclip/license/

# Replace modified files
cp java_sources/com/miniclip/platform/MCApplication.java output_dir/sources/com/miniclip/platform/
cp java_sources/com/miniclip/nativeJNI/cocojava.java output_dir/sources/com/miniclip/nativeJNI/
```

#### Step 4: Compile Java Sources
```bash
# Set Android JAR path (adjust version as needed)
export ANDROID_JAR=/path/to/android-sdk/platforms/android-33/android.jar

# Compile all modified Java files
find output_dir/sources -name "*.java" -path "*/com/miniclip/*" > sources.txt
javac -d classes_output \
      -cp "$ANDROID_JAR" \
      -source 1.8 \
      -target 1.8 \
      @sources.txt
```

#### Step 5: Convert to DEX
```bash
# Using d8 from Android SDK build-tools
/path/to/android-sdk/build-tools/34.0.0/d8 \
    --output classes.dex \
    --lib "$ANDROID_JAR" \
    classes_output/**/*.class
```

#### Step 6: Replace DEX in APK
```bash
# Extract APK
unzip -q your_app.apk -d apk_contents

# Replace classes.dex
cp classes.dex apk_contents/

# Repackage APK
cd apk_contents
zip -r ../modified_app.apk *
cd ..
```

#### Step 7: Sign APK
```bash
# Generate key if you don't have one
keytool -genkey -v \
    -keystore my-release-key.jks \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -alias my-key-alias

# Sign the APK
jarsigner -verbose \
    -sigalg SHA256withRSA \
    -digestalg SHA-256 \
    -keystore my-release-key.jks \
    modified_app.apk \
    my-key-alias

# Verify signature
jarsigner -verify -verbose -certs modified_app.apk

# Zipalign (optional but recommended)
/path/to/android-sdk/build-tools/34.0.0/zipalign -v 4 \
    modified_app.apk \
    modified_app_aligned.apk
```

### Method 2: Using APKTool (Recommended for Smali modification)

#### Step 1: Install APKTool
```bash
wget https://raw.githubusercontent.com/iBotPeaches/Apktool/master/scripts/linux/apktool
wget https://bitbucket.org/iBotPeaches/apktool/downloads/apktool_2.9.3.jar
chmod +x apktool
sudo mv apktool apktool_2.9.3.jar /usr/local/bin/
```

#### Step 2: Decompile APK
```bash
apktool d your_app.apk -o output_dir
```

#### Step 3: Convert Java to Smali
This is the tricky part - you need to manually convert the Java code to Smali format.

**Option A**: Use JADX to decompile existing similar code and compare Smali
```bash
# Find the existing MCApplication smali file
find output_dir/smali -name "MCApplication.smali"

# Edit it manually to add license initialization
```

**Option B**: Compile Java to .class then use dex2jar/baksmali
```bash
# Compile new classes
javac -cp android.jar java_sources/com/miniclip/license/*.java

# Convert to DEX
d8 --output new_classes.dex *.class

# Convert DEX to Smali
baksmali d new_classes.dex -o smali_output

# Copy Smali files to apktool output
cp -r smali_output/com/miniclip/license output_dir/smali/com/miniclip/
```

#### Step 4: Rebuild APK
```bash
apktool b output_dir -o modified_app.apk
```

#### Step 5: Sign APK
```bash
# Same as Method 1 Step 7
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
    -keystore my-release-key.jks \
    modified_app.apk \
    my-key-alias
```

### Method 3: Quick Script (Automated)

Use the provided `build_license_system.sh` script:

```bash
# Set environment variables
export ANDROID_JAR=/path/to/android-sdk/platforms/android-33/android.jar
export PATH=$PATH:/path/to/android-sdk/build-tools/34.0.0

# Run the script
./build_license_system.sh
```

## Verification

### 1. Install and Test
```bash
# Install modified APK
adb install -r modified_app_aligned.apk

# Launch app
adb shell am start -n com.yourpackage/.MainActivity

# Monitor logs
adb logcat -c  # Clear logs
adb logcat | grep -E "LicenseManager|ClipboardInterceptor|MCApplication"
```

### 2. Expected Log Output
```
I/MCApplication: Clipboard interceptor initialized with fixed license
I/ClipboardInterceptor: Fixed license set in clipboard: lasherinamk
I/LicenseManager: License validated: true at 1707500000000
I/LicenseManager: License validation successful: lasherinamk
I/MCApplication: License check completed. Valid: true
```

### 3. Test Clipboard
```bash
# After app launch, check clipboard content
adb shell dumpsys clipboard

# Should show: "lasherinamk"
```

### 4. Test Login Screen
- Launch the app
- If login screen appears, it should automatically proceed with the license
- Check logs for "Clipboard read intercepted, returning fixed license"

## Troubleshooting

### Issue: App crashes on startup
**Solution**: Check logcat for ClassNotFoundException or NoSuchMethodError
```bash
adb logcat | grep -E "Exception|Error"
```
- Ensure all new classes are properly included in the APK
- Verify package names match exactly

### Issue: License not working
**Solution**: 
```bash
# Check if LicenseManager is initialized
adb logcat | grep LicenseManager

# If no logs, the init() method might not be called
# Verify MCApplication.init() is called at app startup
```

### Issue: Clipboard not intercepted
**Solution**:
```bash
# Verify ClipboardInterceptor is initialized
adb logcat | grep ClipboardInterceptor

# Check if cocojava.java modifications are applied
# Ensure setClipboardText/getClipboardText methods are modified
```

### Issue: Signature verification failed
**Solution**:
```bash
# Remove old app first
adb uninstall com.yourpackage

# Then install new one
adb install modified_app_aligned.apk
```

## Important Notes

1. **Backup Original APK**: Always keep a backup before modification
2. **Match SDK Version**: Use the same Android SDK version as the original APK
3. **ProGuard**: If the APK is obfuscated, class/method names might differ
4. **Multi-DEX**: If app has multiple DEX files, process all of them
5. **Native Libraries**: Our changes don't affect native (.so) files

## Testing Checklist

- [ ] App installs successfully
- [ ] App launches without crashes
- [ ] License check logs appear in logcat
- [ ] Clipboard contains "lasherinamk" after launch
- [ ] Login screen (if any) auto-proceeds with license
- [ ] App functions normally after license check

## Support Files

- `README_LICENSE_CHANGES.md`: Detailed explanation of changes
- `CLIPBOARD_LOGIN_BYPASS.md`: Clipboard interception details
- `build_license_system.sh`: Automated build script
- `java_sources/`: All modified source files

## Security Warning

⚠️ This modification is intended for:
- Development and testing purposes
- Internal/private use
- Environments where license security is not critical

NOT recommended for:
- Production apps with security requirements
- Apps distributed on public stores
- Apps handling sensitive user data

The license value "lasherinamk" is visible in the code and can be easily extracted.
