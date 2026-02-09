# GÖRSEL KILAVUZ - LİSANS SİSTEMİ

## 🎯 NE GÖRECEKSİNİZ

### Mevcut Durum (ÇALIŞMIYOR) ❌

```
┌─────────────────────────────────────────┐
│                                         │
│     Copy username:key and tap login    │
│                                         │
│     LOGIN WITH CLIPBOARD                │
│                                         │
│         [TAP TO LOGIN]                  │
│                                         │
└─────────────────────────────────────────┘
                    ↓
           [Clipboard Check]
                    ↓
        ⚠️ "Copyboard is empty"
                    ↓
            [API Request]
                    ↓
        ⚠️ "Malformed request"
```

### Yeni Durum (ÇALIŞACAK) ✅

```
┌─────────────────────────────────────────┐
│         Enter License                   │
│                                         │
│  Please enter your license key:        │
│                                         │
│  ┌───────────────────────────────────┐ │
│  │ lasherinamk                       │ │
│  └───────────────────────────────────┘ │
│                                         │
│         [OK]        [Hint]              │
│                                         │
└─────────────────────────────────────────┘
                    ↓
         [Local Validation]
         "lasherinamk" == "lasherinamk"
                    ↓
            ✅ License valid!
                    ↓
          App continues...
```

---

## 📝 ADIMLARIN GÖRSEL AÇIKLAMASI

### ADIM 1: Script'i Çalıştır

```bash
$ ./bypass_license.sh myapp.apk

=== APK License Bypass Tool ===

✓ APK bulundu: myapp.apk
✓ APKTool hazır

📦 APK decompile ediliyor...
✓ Decompile tamamlandı

🔍 License kontrol methodları aranıyor...
✓ Bulunan dosyalar:
  app-decompiled/smali/com/miniclip/...

🔍 API çağrıları aranıyor...
✓ API çağrıları bulunan dosyalar:
  HttpConnection.smali

🔧 API çağrıları yoruma alınıyor...
✓ API çağrıları devre dışı bırakıldı

📝 SimpleLicenseDialog ekleniyor...
✓ SimpleLicenseDialog eklendi

📦 APK yeniden compile ediliyor...
✓ APK compile edildi: modified_myapp.apk

✏️  APK imzalanıyor...
✓ APK imzalandı

✅ İŞLEM TAMAMLANDI!

Sonuç APK: modified_myapp.apk
```

### ADIM 2: APK'yı Yükle

```bash
$ adb install -r modified_myapp.apk

Performing Streamed Install
Success
```

### ADIM 3: Uygulamayı Aç

```
Telefon Ekranı:
┌─────────────────────────────────────────┐
│  [App Icon]                             │
│                                         │
│         Enter License                   │
│  ────────────────────────────────      │
│                                         │
│  Please enter your license key:        │
│                                         │
│  ┌───────────────────────────────────┐ │
│  │ [Buraya yazın]                    │ │
│  └───────────────────────────────────┘ │
│                                         │
│      [    OK    ]   [  Hint  ]         │
│                                         │
└─────────────────────────────────────────┘
```

### ADIM 4: "lasherinamk" Yaz

```
Telefon Ekranı:
┌─────────────────────────────────────────┐
│  [App Icon]                             │
│                                         │
│         Enter License                   │
│  ────────────────────────────────      │
│                                         │
│  Please enter your license key:        │
│                                         │
│  ┌───────────────────────────────────┐ │
│  │ lasherinamk█                      │ │
│  └───────────────────────────────────┘ │
│                                         │
│      [    OK    ]   [  Hint  ]         │
│                                         │
└─────────────────────────────────────────┘
```

### ADIM 5: OK'e Bas

```
Telefon Ekranı:
┌─────────────────────────────────────────┐
│                                         │
│                                         │
│                                         │
│        ✅ License valid!                │
│                                         │
│                                         │
│      [App başlatılıyor...]              │
│                                         │
│                                         │
└─────────────────────────────────────────┘
```

### ADIM 6: Uygulama Çalışıyor

```
Telefon Ekranı:
┌─────────────────────────────────────────┐
│  ← [Back]        My App      [Menu] ≡  │
│─────────────────────────────────────────│
│                                         │
│      [UYGULAMANIN ANA EKRANI]          │
│                                         │
│      • Feature 1                        │
│      • Feature 2                        │
│      • Feature 3                        │
│                                         │
│                                         │
│      [Tüm özellikler çalışıyor]        │
│                                         │
└─────────────────────────────────────────┘
```

---

## 🔍 LOGLAR

### Başarılı Çalışma Logları

```bash
$ adb logcat | grep -i license

I/SimpleLicenseDialog: License validated successfully: lasherinamk
I/SimpleLicenseDialog: License match: lasherinamk
I/SharedPreferences: Saved license validation
D/MainActivity: License check passed, continuing...
```

### Hatalı Loglar (Düzeltilmesi Gerekenler)

```bash
$ adb logcat | grep -i license

E/HttpConnection: Failed to connect to server
W/License: Malformed request error
E/License: Copyboard is empty
W/HttpConnection: API call failed
```

Bunları görürseniz → API çağrıları hala aktif → Script tekrar çalıştırın veya manual olarak kaldırın.

---

## 📱 EKSİK GELEN HATALAR VE ÇÖZÜMLER

### Hata 1: Dialog Görünmüyor

```
Ekran:
[Sadece siyah ekran veya splash screen]
```

**Çözüm:**
```bash
# Logları kontrol et
adb logcat | grep SimpleLicenseDialog

# Eğer hiç log yoksa, dialog eklenmemiş
# APK_DUZENLEME_REHBERI.md'ye göre tekrar ekle
```

### Hata 2: "Invalid license" Diyor

```
Ekran:
┌─────────────────────────────────────┐
│  ⚠️ Invalid license!                │
│     Please try again.               │
└─────────────────────────────────────┘
```

**Çözüm:**
```
Dikkat! "lasherinamk" yazımı:
✅ DOĞRU: lasherinamk (küçük harf, boşluksuz)
❌ YANLIŞ: Lasherinamk (büyük L)
❌ YANLIŞ: lasher inamk (boşluklu)
❌ YANLIŞ: lasherimnak (harf sırası yanlış)
```

### Hata 3: Uygulama Crash Oluyor

```
Ekran:
┌─────────────────────────────────────┐
│  Unfortunately, App has stopped.    │
│                                     │
│          [   OK   ]                 │
└─────────────────────────────────────┘
```

**Çözüm:**
```bash
# Crash logunu al
adb logcat > crash_log.txt

# En son 100 satırı kontrol et
tail -100 crash_log.txt

# "FATAL EXCEPTION" satırını ara
grep "FATAL EXCEPTION" crash_log.txt
```

---

## 🎮 ÇALIŞMA AKIŞI

### Başarılı Senaryo (Hedeflenen)

```
1. [Uygulama İkonu] TAP
      ↓
2. [Splash Screen] (2 sn)
      ↓
3. [License Dialog] GÖSTER
      ↓
4. Kullanıcı "lasherinamk" YAZAR
      ↓
5. [OK] butonu TAP
      ↓
6. Local doğrulama: TRUE
      ↓
7. "License valid!" TOAST
      ↓
8. Ana ekran AÇILIR
      ↓
9. Tüm özellikler ÇALIŞIR ✅
```

### Başarısız Senaryo (Eski Durum)

```
1. [Uygulama İkonu] TAP
      ↓
2. [Splash Screen] (2 sn)
      ↓
3. [Username:Key Dialog] GÖSTER
      ↓
4. "LOGIN WITH CLIPBOARD" TAP
      ↓
5. Clipboard READ
      ↓
6. "Copyboard is empty" HATA ❌
      ↓
VEYA
      ↓
6. API çağrısı YAP
      ↓
7. "Malformed request" HATA ❌
      ↓
8. Uygulama AÇILMIYOR ❌
```

---

## ✅ BAŞARI KRİTERLERİ

Aşağıdakileri görürseniz başarılısınız:

- ✅ Dialog "Enter License" başlığıyla açılıyor
- ✅ Text input alanı var
- ✅ "lasherinamk" yazabiliyorsunuz
- ✅ OK'e basınca "License valid!" mesajı
- ✅ Uygulama ana ekranı açılıyor
- ✅ Logda "SimpleLicenseDialog: License validated" görünüyor
- ✅ Logda API hatası YOK
- ✅ Logda clipboard hatası YOK

---

## 🎯 ÖZET

**Ne bekliyorsunuz:**
- Direkt text input ile lisans girişi
- "lasherinamk" yazıp OK'e basma
- Başarılı mesaj ve uygulama açılması

**Ne BEKLEMİyorsunuz:**
- Clipboard kullanımı
- API çağrıları
- Hata mesajları

**Sorun devam ederse:**
1. SON_DURUM_VE_COZUM.md oku
2. BASIT_COZUM.md'den başka yöntem dene
3. Logları paylaş

---

**Son Güncelleme:** 2026-02-09
**Durum:** ✅ GÖRSEL KILAVUZ HAZIR
