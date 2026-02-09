#!/bin/bash

# APK License Bypass Script
# Otomatik olarak license kontrolünü bypass eder

set -e

echo "=== APK License Bypass Tool ==="
echo ""

# Kullanım kontrolü
if [ $# -lt 1 ]; then
    echo "Kullanım: $0 <apk-dosyası>"
    echo "Örnek: $0 myapp.apk"
    exit 1
fi

APK_FILE="$1"
WORK_DIR="apk_work"
OUTPUT_APK="modified_${APK_FILE}"

# APK dosyası kontrolü
if [ ! -f "$APK_FILE" ]; then
    echo "❌ Hata: $APK_FILE bulunamadı!"
    exit 1
fi

echo "✓ APK bulundu: $APK_FILE"
echo ""

# APKTool kontrolü
if ! command -v apktool &> /dev/null; then
    echo "❌ APKTool bulunamadı. Yükleniyor..."
    
    wget -q https://bitbucket.org/iBotPeaches/apktool/downloads/apktool_2.9.3.jar -O /tmp/apktool.jar
    wget -q https://raw.githubusercontent.com/iBotPeaches/Apktool/master/scripts/linux/apktool -O /tmp/apktool
    
    chmod +x /tmp/apktool
    sudo mv /tmp/apktool /usr/local/bin/
    sudo mv /tmp/apktool.jar /usr/local/bin/apktool.jar
    
    echo "✓ APKTool yüklendi"
fi

echo "✓ APKTool hazır"
echo ""

# Temizlik
rm -rf "$WORK_DIR"
mkdir -p "$WORK_DIR"

# Decompile
echo "📦 APK decompile ediliyor..."
apktool d "$APK_FILE" -o "$WORK_DIR" -f

echo "✓ Decompile tamamlandı"
echo ""

# License kontrol methodlarını bul
echo "🔍 License kontrol methodları aranıyor..."
LICENSE_FILES=$(find "$WORK_DIR/smali" -name "*.smali" -exec grep -l "license\|License" {} \; 2>/dev/null)

if [ -z "$LICENSE_FILES" ]; then
    echo "⚠️  License ile ilgili smali dosyası bulunamadı"
else
    echo "✓ Bulunan dosyalar:"
    echo "$LICENSE_FILES"
fi
echo ""

# HttpConnection kullanımını bul
echo "🔍 API çağrıları aranıyor..."
HTTP_FILES=$(find "$WORK_DIR/smali" -name "*.smali" -exec grep -l "HttpConnection\|HttpURLConnection" {} \; 2>/dev/null | head -10)

if [ -n "$HTTP_FILES" ]; then
    echo "✓ API çağrıları bulunan dosyalar:"
    echo "$HTTP_FILES"
    echo ""
    
    echo "🔧 API çağrıları yoruma alınıyor..."
    for file in $HTTP_FILES; do
        # HttpConnection satırlarını yoruma al
        sed -i 's/^\([[:space:]]*invoke.*HttpConnection.*\)$/# BYPASS: \1/' "$file"
        sed -i 's/^\([[:space:]]*invoke.*HttpURLConnection.*\)$/# BYPASS: \1/' "$file"
    done
    echo "✓ API çağrıları devre dışı bırakıldı"
else
    echo "⚠️  HttpConnection kullanımı bulunamadı"
fi
echo ""

# SimpleLicenseDialog ekle
echo "📝 SimpleLicenseDialog ekleniyor..."
mkdir -p "$WORK_DIR/smali/com/miniclip/license"

cat > "$WORK_DIR/smali/com/miniclip/license/SimpleLicenseDialog.smali" << 'SMALI_EOF'
.class public Lcom/miniclip/license/SimpleLicenseDialog;
.super Ljava/lang/Object;

.method public static validateLicense(Ljava/lang/String;)Z
    .locals 2
    
    const-string v0, "lasherinamk"
    
    if-eqz p0, :cond_false
    invoke-virtual {p0, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v1
    if-eqz v1, :cond_false
    
    const-string v1, "SimpleLicenseDialog"
    const-string v2, "License validated: lasherinamk"
    invoke-static {v1, v2}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I
    
    const/4 v0, 0x1
    return v0
    
    :cond_false
    const-string v1, "SimpleLicenseDialog"
    const-string v2, "Invalid license"
    invoke-static {v1, v2}, Landroid/util/Log;->w(Ljava/lang/String;Ljava/lang/String;)I
    
    const/4 v0, 0x0
    return v0
.end method
SMALI_EOF

echo "✓ SimpleLicenseDialog eklendi"
echo ""

# Recompile
echo "📦 APK yeniden compile ediliyor..."
apktool b "$WORK_DIR" -o "$OUTPUT_APK"

echo "✓ APK compile edildi: $OUTPUT_APK"
echo ""

# İmzalama
echo "✏️  APK imzalanıyor..."

# Test anahtarı oluştur
if [ ! -f "test-key.keystore" ]; then
    keytool -genkey -v -keystore test-key.keystore -alias test-alias \
        -keyalg RSA -keysize 2048 -validity 10000 \
        -storepass testpass -keypass testpass \
        -dname "CN=Test, OU=Test, O=Test, L=Test, S=Test, C=US"
fi

# İmzala
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 \
    -keystore test-key.keystore -storepass testpass \
    "$OUTPUT_APK" test-alias

# Zipalign (eğer varsa)
if command -v zipalign &> /dev/null; then
    zipalign -v 4 "$OUTPUT_APK" "aligned_${OUTPUT_APK}"
    mv "aligned_${OUTPUT_APK}" "$OUTPUT_APK"
    echo "✓ Zipalign yapıldı"
fi

echo "✓ APK imzalandı"
echo ""

echo "✅ İŞLEM TAMAMLANDI!"
echo ""
echo "Sonuç APK: $OUTPUT_APK"
echo ""
echo "Yüklemek için:"
echo "  adb install -r $OUTPUT_APK"
echo ""
echo "Test etmek için:"
echo "  adb logcat | grep -i license"
echo ""

# Temizlik (opsiyonel)
read -p "Geçici dosyaları silmek ister misiniz? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    rm -rf "$WORK_DIR"
    echo "✓ Temizlik yapıldı"
fi

echo ""
echo "NOT: License olarak 'lasherinamk' kullanın"
