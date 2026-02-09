# APK'YI DÜZENLEME REHBERİ - LİSANS SİSTEMİ DEĞİŞİKLİĞİ

## SORUN
Mevcut APK hala API kullanıyor ve "username:key" formatında çalışıyor. Clipboard boş hatası veriyor.

## ÇÖZÜM
Tamamen yeni, basit bir lisans sistemi. API YOK, direkt text input ile "lasherinamk" girişi.

---

## ADIM 1: GEREKLI ARAÇLARI İNDİR

### APKTool İndir
```bash
wget https://bitbucket.org/iBotPeaches/apktool/downloads/apktool_2.9.3.jar
wget https://raw.githubusercontent.com/iBotPeaches/Apktool/master/scripts/linux/apktool
chmod +x apktool
sudo mv apktool /usr/local/bin/
sudo mv apktool_2.9.3.jar /usr/local/bin/apktool.jar
```

### Uber APK Signer İndir
```bash
wget https://github.com/patrickfav/uber-apk-signer/releases/download/v1.3.0/uber-apk-signer-1.3.0.jar
```

---

## ADIM 2: APK'YI DECOMPILE ET

```bash
# APK'nizi kopyalayın
cp your-app.apk original-app.apk

# Decompile et (smali formatına çevir)
apktool d original-app.apk -o app-decompiled
```

---

## ADIM 3: SimpleLicenseDialog SINIFINI EKLE

### 3a. Dizin Oluştur
```bash
mkdir -p app-decompiled/smali/com/miniclip/license
```

### 3b. Java'yı Smali'ye Çevir

Java kodu hazır: `java_sources/com/miniclip/license/SimpleLicenseDialog.java`

Bu dosyayı smali'ye çevirmek için:

```bash
# Java dosyasını compile et
javac -source 1.8 -target 1.8 java_sources/com/miniclip/license/SimpleLicenseDialog.java

# d8 ile DEX'e çevir  
d8 --output temp.dex java_sources/com/miniclip/license/SimpleLicenseDialog.class

# baksmali ile smali'ye çevir
baksmali d temp.dex -o temp-smali

# Smali dosyasını kopyala
cp temp-smali/com/miniclip/license/SimpleLicenseDialog.smali app-decompiled/smali/com/miniclip/license/
```

**VEYA** Manuel smali yazabilirsiniz (aşağıda sağlanmıştır).

---

## ADIM 4: UYGULAMAYA ENTEGRE ET

### 4a. MainActivity veya İlk Activity'yi Bul

```bash
find app-decompiled/smali -name "MainActivity.smali" -o -name "MCApplication.smali"
```

### 4b. onCreate veya init Methodunu Düzenle

`app-decompiled/smali/com/miniclip/platform/MCApplication.smali` dosyasını aç.

`init()` methodunu bul ve EN BAŞINA ekle:

```smali
.method public static init()V
    .locals 2
    
    # SimpleLicenseDialog çağrısı ekle
    invoke-static {}, Lcom/miniclip/framework/Miniclip;->getActivity()Landroid/app/Activity;
    move-result-object v0
    
    const/4 v1, 0x0
    invoke-static {v0, v1}, Lcom/miniclip/license/SimpleLicenseDialog;->showLicenseDialog(Landroid/app/Activity;Lcom/miniclip/license/SimpleLicenseDialog$LicenseCallback;)V
    
    # Mevcut kod devam eder...
    .line 61
    sget-boolean v0, Lcom/miniclip/platform/MCApplication;->isInitialized:Z
    ...
```

---

## ADIM 5: API ÇAĞRILARINI KALDIR (İSTEĞE BAĞLI)

### HttpConnection kullanımını kaldır

```bash
# HttpConnection'ı arıyoruz
grep -r "HttpConnection" app-decompiled/smali/com/miniclip --include="*.smali"
```

Bulduğunuz her yerde, API çağrısı yapan satırları yoruma alın veya silin:

```smali
# invoke-virtual {v0, v1}, Lcom/miniclip/network/HttpConnection;->start()Z
# API çağrısı kaldırıldı
```

---

## ADIM 6: YEN DEN COMPILE ET

```bash
# APK'yı yeniden compile et
apktool b app-decompiled -o modified-app.apk
```

---

## ADIM 7: İMZALA

```bash
# Uber APK Signer ile imzala (otomatik test anahtarı kullanır)
java -jar uber-apk-signer-1.3.0.jar --apks modified-app.apk
```

Veya kendi anahtarınızla:

```bash
# Anahtar oluştur (ilk kez)
keytool -genkey -v -keystore my-key.keystore -alias my-alias -keyalg RSA -keysize 2048 -validity 10000

# İmzala
jarsigner -verbose -sigalg SHA256withRSA -digestalg SHA-256 -keystore my-key.keystore modified-app.apk my-alias

# Zipalign (opsiyonel ama önerilir)
zipalign -v 4 modified-app.apk modified-app-aligned.apk
```

---

## ADIM 8: KURA VE TEST ET

```bash
# Eski uygulamayı kaldır
adb uninstall com.yourpackage.name

# Yeni APK'yı yükle
adb install modified-app-aligned.apk

# Uygulamayı başlat
adb shell am start -n com.yourpackage.name/.MainActivity

# Logları izle
adb logcat | grep -E "SimpleLicenseDialog|License"
```

---

## BEKLENİLEN SONUÇ

1. Uygulama açılınca bir dialog göreceksiniz
2. "Enter License" başlığı
3. Text input alanı
4. "lasherinamk" yazın ve OK'e basın
5. "License valid!" mesajı görünecek
6. Uygulama normal çalışmaya devam edecek

---

## SORUN GİDERME

### "Class not found" Hatası
- SimpleLicenseDialog.smali dosyasının doğru konumda olduğundan emin olun
- smali/com/miniclip/license/SimpleLicenseDialog.smali

### Dialog Görünmüyor
- MCApplication.smali'deki init() methoduna eklediğinizden emin olun
- Logları kontrol edin: `adb logcat | grep SimpleLicenseDialog`

### API Hala Çağrılıyor
- HttpConnection kullanımlarını bulun ve kaldırın
- Native kod API çağrısı yapıyorsa, o kısmı da değiştirmeniz gerekir

---

## ALTERNATİF: KOLAY YOL

Eğer smali ile uğraşmak istemiyorsanız, native kod seviyesinde değişiklik yapmanız gerekir. Bu durumda:

1. lib/ klasöründeki .so dosyalarını decompile edin
2. License kontrol fonksiyonunu bulun
3. Her zaman true döndürmesini sağlayın

VEYA

Web tabanlı license sistemi varsa (HTMLDialog), HTML/JS kodunu düzenleyin.

---

## ÖZET

✅ SimpleLicenseDialog kullanarak direkt text input
✅ API YOK - Tamamen local
✅ Sadece "lasherinamk" kabul ediyor
✅ Clipboard'a gerek yok
✅ Kullanıcı kendisi yazıyor

---

Herhangi bir sorun olursa:
1. Logları paylaşın: `adb logcat > logcat.txt`
2. APKTool hata mesajlarını kontrol edin
3. smali dosyalarının syntax'ını kontrol edin
