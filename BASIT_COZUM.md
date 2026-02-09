# BASIT ÇÖZÜM - API OLMADAN LİSANS SİSTEMİ

## SORUN ANALİZİ

Kullanıcı APK'yı derledi ancak:
- ❌ Hala API kullanıyor (Malformed request hatası)
- ❌ Clipboard boş hatası veriyor  
- ❌ Java kaynak kodları DEX dosyalarına entegre edilmemiş

## KÖK NEDEN

Daha önce sağlanan Java kaynak kod değişiklikleri APK'nın DEX dosyalarına compile edilmemiş. APK hala orijinal DEX dosyalarını kullanıyor.

---

## ÇÖZÜM 1: NATIVE KOD SEVİYESİNDE BYPASS (EN KOLAY)

Eğer lisans kontrolü native kod (C/C++) tarafında yapılıyorsa:

### Adım 1: lib/ klasörünü kontrol et
```bash
cd your-apk-folder
ls -la lib/*/
```

`libgame.so` veya benzeri dosyalar göreceksiniz.

### Adım 2: .so dosyasını hex editor ile aç

```bash
# Binary dosyayı düzenle
hexedit lib/armeabi-v7a/libgame.so
```

### Adım 3: License kontrol fonksiyonunu bul ve bypass et

Fonksiyon return değerini 1 (true) yapmak için assembly kodunu değiştir:
```asm
mov r0, #1
bx lr
```

---

## ÇÖZÜM 2: SMALI SEVİYESİNDE DEĞİŞİKLİK

### Tam Smali Kodu: SimpleLicenseDialog

Dosya: `smali/com/miniclip/license/SimpleLicenseDialog.smali`

```smali
.class public Lcom/miniclip/license/SimpleLicenseDialog;
.super Ljava/lang/Object;
.source "SimpleLicenseDialog.java"

# static fields
.field private static final TAG:Ljava/lang/String; = "SimpleLicenseDialog"
.field private static final PREFS_NAME:Ljava/lang/String; = "SimpleLicensePrefs"
.field private static final KEY_LICENSE_VALIDATED:Ljava/lang/String; = "license_validated"
.field private static final CORRECT_LICENSE:Ljava/lang/String; = "lasherinamk"

# direct methods
.method public static showLicenseDialog(Landroid/app/Activity;)V
    .locals 4

    # Basit bir dialog göster
    new-instance v0, Landroid/app/AlertDialog$Builder;
    invoke-direct {v0, p0}, Landroid/app/AlertDialog$Builder;-><init>(Landroid/content/Context;)V
    
    const-string v1, "Enter License"
    invoke-virtual {v0, v1}, Landroid/app/AlertDialog$Builder;->setTitle(Ljava/lang/CharSequence;)Landroid/app/AlertDialog$Builder;
    
    const-string v1, "Please enter: lasherinamk"
    invoke-virtual {v0, v1}, Landroid/app/AlertDialog$Builder;->setMessage(Ljava/lang/CharSequence;)Landroid/app/AlertDialog$Builder;
    
    # EditText ekle
    new-instance v2, Landroid/widget/EditText;
    invoke-direct {v2, p0}, Landroid/widget/EditText;-><init>(Landroid/content/Context;)V
    
    const/4 v3, 0x1
    invoke-virtual {v2, v3}, Landroid/widget/EditText;->setInputType(I)V
    
    invoke-virtual {v0, v2}, Landroid/app/AlertDialog$Builder;->setView(Landroid/view/View;)Landroid/app/AlertDialog$Builder;
    
    # OK button - sadece "lasherinamk" kabul et
    const-string v1, "OK"
    new-instance v3, Lcom/miniclip/license/SimpleLicenseDialog$1;
    invoke-direct {v3, v2, p0}, Lcom/miniclip/license/SimpleLicenseDialog$1;-><init>(Landroid/widget/EditText;Landroid/app/Activity;)V
    invoke-virtual {v0, v1, v3}, Landroid/app/AlertDialog$Builder;->setPositiveButton(Ljava/lang/CharSequence;Landroid/content/DialogInterface$OnClickListener;)Landroid/app/AlertDialog$Builder;
    
    const/4 v1, 0x0
    invoke-virtual {v0, v1}, Landroid/app/AlertDialog$Builder;->setCancelable(Z)Landroid/app/AlertDialog$Builder;
    
    invoke-virtual {v0}, Landroid/app/AlertDialog$Builder;->create()Landroid/app/AlertDialog;
    move-result-object v1
    invoke-virtual {v1}, Landroid/app/AlertDialog;->show()V
    
    return-void
.end method

.method public static validateLicense(Ljava/lang/String;)Z
    .locals 2

    const-string v0, "lasherinamk"
    
    if-eqz p0, :cond_0
    invoke-virtual {p0, v0}, Ljava/lang/String;->equals(Ljava/lang/Object;)Z
    move-result v1
    if-eqz v1, :cond_0
    
    const/4 v0, 0x1
    return v0
    
    :cond_0
    const/4 v0, 0x0
    return v0
.end method
```

---

## ÇÖZÜM 3: EN KOLAY - MEVCUT KONTROLÜ BYPASS ET

### Adım 1: License kontrol fonksiyonunu bul

```bash
# APK'yı decompile et
apktool d your-app.apk

# License kontrolünü ara
grep -r "license\|License" your-app/smali --include="*.smali" | grep "method"
```

### Adım 2: Method'u her zaman true döndürmesi için değiştir

Örnek: `checkLicense()` methodunu bulduysanız:

**ÖNCE:**
```smali
.method public checkLicense()Z
    .locals 5
    
    # API çağrısı
    invoke-virtual {v0, v1}, HttpConnection;->start()Z
    move-result v2
    
    # Sonuç döndür
    return v2
.end method
```

**SONRA:**
```smali
.method public checkLicense()Z
    .locals 1
    
    # Her zaman true döndür - API YOK
    const/4 v0, 0x1
    return v0
.end method
```

### Adım 3: Yeniden compile ve imzala

```bash
# Yeniden compile
apktool b your-app -o modified.apk

# İmzala
java -jar uber-apk-signer.jar --apks modified.apk
```

---

## ÇÖZÜM 4: WEBVIEW TABANLIYSA (HTMLDialog)

Eğer lisans kontrolü WebView içinde yapılıyorsa:

### Adım 1: assets/ veya res/ klasöründe HTML dosyaları ara

```bash
find assets -name "*.html" -o -name "*.js"
```

### Adım 2: JavaScript'te license kontrolünü değiştir

**Arama:**
```javascript
function validateLicense(license) {
    // API call
    fetch('/api/validate', {
        method: 'POST',
        body: JSON.stringify({license: license})
    })
}
```

**Değiştir:**
```javascript
function validateLicense(license) {
    // LOCAL - NO API
    if (license === "lasherinamk") {
        return true;
    }
    return false;
}
```

---

## ÇÖZÜM 5: CONFIGURATION DOSYALARI

Bazı uygulamalar config dosyalarında API endpoint tanımlar:

### Adım 1: Config dosyalarını kontrol et

```bash
find assets -name "*.json" -o -name "*.xml" -o -name "*.properties"
cat assets/config.json
```

### Adım 2: API URL'lerini kaldır veya localhost yap

**Önce:**
```json
{
    "license_api": "https://api.example.com/validate",
    "license_required": true
}
```

**Sonra:**
```json
{
    "license_api": "",
    "license_required": false
}
```

---

## HANGİ ÇÖZÜMÜ KULLANMALI?

1. **Native kod varsa** → ÇÖZÜM 1 (hex edit)
2. **Smali ile çalışmak istiyorsanız** → ÇÖZÜM 2 veya 3
3. **WebView kullanıyorsa** → ÇÖZÜM 4
4. **Config dosyası varsa** → ÇÖZÜM 5

---

## TEST

Değişiklikten sonra:

```bash
# APK'yı yükle
adb install modified.apk

# Logları izle
adb logcat | grep -i "license\|error"

# Beklenen:
# - API çağrısı OLMASIN
# - "lasherinamk" ile giriş başarılı olsun
# - Clipboard hatası OLMASIN
```

---

## DESTEK

Eğer hala çalışmıyorsa, şunları paylaşın:

1. **Logcat çıktısı:**
   ```bash
   adb logcat > logcat.txt
   ```

2. **APK decompile çıktısı:**
   ```bash
   apktool d your-app.apk
   ls -la your-app/
   ```

3. **Hata mesajları:**
   - "Malformed request" nereden geliyor?
   - Hangi sınıf/method?

---

## SONUÇ

Asıl sorun: Java kaynak kodları DEX'e compile edilmemiş.

✅ **Hızlı Fix:** Mevcut license kontrolünü bypass et (ÇÖZÜM 3)
✅ **Kalıcı Fix:** Yeni SimpleLicenseDialog ekle (ÇÖZÜM 2)
✅ **En Kolay:** Native bypass (ÇÖZÜM 1)
