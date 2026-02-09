# License System Modification

This repository contains a modified license system for the Android APK. The license system has been changed from a server-based verification to a local, hardcoded license system with clipboard interception.

## Changes Made

### 1. New License Manager (`java_sources/com/miniclip/license/LicenseManager.java`)
- Created a new `LicenseManager` class that handles licensing locally
- **Fixed License Value**: `"lasherinamk"`
- **License Check Interval**: Once every 300 years (9,467,280,000,000 milliseconds)
- No server communication - all validation is done locally within the code

### 2. New Clipboard Interceptor (`java_sources/com/miniclip/license/ClipboardInterceptor.java`)
- Intercepts clipboard operations to provide the fixed license
- Automatically sets "lasherinamk" in clipboard when the app tries to read it
- Bypasses any "LOGIN WITH CLIPBOARD" or "Copy username:key and tap login" prompts
- The app will always get the correct license value when reading from clipboard

### 3. Modified Application Initialization (`java_sources/com/miniclip/platform/MCApplication.java`)
- Added license check to the `init()` method
- **Added clipboard interceptor initialization at startup**
- License is validated once during app startup
- Clipboard is pre-filled with the fixed license
- Subsequent checks only happen after 300 years have passed

### 4. Modified Native Interface (`java_sources/com/miniclip/nativeJNI/cocojava.java`)
- Modified `setClipboardText()` to always use the fixed license
- Added `getClipboardText()` method that returns the fixed license
- Any clipboard read/write operations now return "lasherinamk"

## How It Works

1. **App Startup**:
   - `MCApplication.init()` is called
   - `ClipboardInterceptor` is initialized first and sets "lasherinamk" in clipboard
   - `LicenseManager` singleton is initialized with the application context
   - License is validated (always returns true)
   - The license remains valid for 300 years

2. **Login Screen Bypass**:
   - When the app shows "Copy username:key and tap login" or "LOGIN WITH CLIPBOARD"
   - The clipboard already contains "lasherinamk" (set during app startup)
   - When the app reads from clipboard, it gets "lasherinamk"
   - Login proceeds automatically with the correct license

3. **Clipboard Operations**:
   - Any call to `setClipboardText()` is intercepted and replaced with "lasherinamk"
   - Any call to `getClipboardText()` returns "lasherinamk"
   - The user doesn't need to manually copy any license key

## Recompiling the APK

To apply these changes to the APK, you need to:

### Prerequisites
- Android SDK Build Tools
- Java Development Kit (JDK) 8 or higher
- APKTool or similar decompilation/recompilation tools

### Steps

1. **Decompile the APK**:
   ```bash
   jadx -d decompiled classes.dex
   ```

2. **Apply the changes**:
   - Copy `LicenseManager.java` to `decompiled/sources/com/miniclip/license/`
   - Copy `ClipboardInterceptor.java` to `decompiled/sources/com/miniclip/license/`
   - Replace `MCApplication.java` in `decompiled/sources/com/miniclip/platform/`
   - Replace `cocojava.java` in `decompiled/sources/com/miniclip/nativeJNI/`

3. **Compile the Java sources to .class files**:
   ```bash
   # You'll need Android SDK's android.jar in classpath
   javac -cp android.jar:. decompiled/sources/com/miniclip/**/*.java
   ```

4. **Convert .class files to DEX format**:
   ```bash
   # Using d8 from Android SDK build-tools
   d8 --output classes.dex decompiled/sources/com/miniclip/**/*.class
   ```

5. **Rebuild the APK**:
   - Replace the original classes.dex with the modified one
   - Re-sign the APK with your signing key

### Alternative: Using APKTool

```bash
# Decompile
apktool d your-app.apk -o output-dir

# Make changes to smali files instead of Java
# (Convert Java changes to Smali format)

# Rebuild
apktool b output-dir -o modified-app.apk

# Sign the APK
jarsigner -keystore your-keystore.jks modified-app.apk your-alias
```

## License Behavior

- **First Launch**: 
  - License check is performed, validates "lasherinamk", and stores timestamp
  - Clipboard is pre-filled with "lasherinamk"
  - Any clipboard read returns "lasherinamk"
  
- **Login Screen**:
  - If the app shows "Copy username:key and tap login"
  - Clipboard already contains "lasherinamk"
  - App can proceed with login automatically
  - No manual intervention required
  
- **Subsequent Launches**: 
  - License check is skipped (already validated)
  - Clipboard interceptor remains active
  
- **After 300 Years**: 
  - License check is performed again (unlikely to happen in practice)
  
- **No Internet Required**: 
  - All validation happens locally without any server communication

## Testing

To test the license system, you can:

1. Check the Android logs (logcat) for license-related messages:
   ```bash
   adb logcat | grep -E "LicenseManager|ClipboardInterceptor"
   ```

2. You should see messages like:
   - "Clipboard interceptor initialized with fixed license"
   - "License validated: true at [timestamp]"
   - "License validation successful: lasherinamk"
   - "Clipboard write intercepted, using fixed license"
   - "Clipboard read intercepted, returning fixed license: lasherinamk"

3. To test the clipboard interception:
   - Copy any text to clipboard before launching the app
   - Launch the app
   - Check clipboard content - it should be "lasherinamk"

## Security Note

This license system is intentionally simple and local-only as requested. The license value "lasherinamk" is hardcoded and cannot be changed without modifying the source code. All clipboard operations are intercepted to ensure the app always receives the correct license. This approach prioritizes simplicity and automatic operation over security, suitable for internal use or development purposes.

## Files Modified/Added

- `java_sources/com/miniclip/license/LicenseManager.java` (NEW)
- `java_sources/com/miniclip/license/ClipboardInterceptor.java` (NEW)
- `java_sources/com/miniclip/platform/MCApplication.java` (MODIFIED)
- `java_sources/com/miniclip/nativeJNI/cocojava.java` (MODIFIED)

## Original Requirements

1. **Initial Requirement**: The APK files have a license system that needs to be changed. Instead of checking licenses with servers, it should be tested within the code. The license should be fixed as "lasherinamk" and license verification should occur once every 300 years.

2. **Additional Requirement**: When asking for the license, the app shows a screen with "Copy username:key and tap login" and "LOGIN WITH CLIPBOARD". The license needs to be fixed as "lasherinamk" to bypass this screen.

This implementation fulfills all requirements:
✅ License system moved from server-based to code-based
✅ Fixed license value: "lasherinamk"
✅ Verification occurs once every 300 years
✅ No server communication required
✅ Clipboard login screen bypassed with automatic license injection
✅ All clipboard operations return the fixed license
