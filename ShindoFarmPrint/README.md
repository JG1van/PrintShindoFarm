# Shindo Farm 77 Print - Android WebView App

Aplikasi Android minimal untuk membuka web Laravel Shindo Farm 77 dan mencetak nota ke printer thermal Bluetooth (XP-58IIZ) via aplikasi **Bluetooth Print** oleh iyaltamizh.

## Fitur
- Load web Laravel di WebView (full screen)
- Session/cookie Laravel otomatis (login, auth, dll)
- Tombol "Print" di web → panggil `AndroidPrint.cetakNota(html)` → cetak via Bluetooth Print app
- Auto-detect app Bluetooth Print, arahkan ke Play Store jika belum install

## Struktur Project
```
ShindoFarmPrint/
├── app/
│   ├── build.gradle
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/shindofarm/print/MainActivity.java
│       ├── res/
│       │   ├── layout/activity_main.xml
│       │   ├── values/strings.xml
│       │   ├── values/themes.xml
│       │   └── xml/network_security_config.xml
├── build.gradle
├── settings.gradle
└── gradle.properties
```

## Konfigurasi URL (Wajib Saat Deploy)
Edit `app/src/main/res/values/strings.xml`:
```xml
<string name="web_app_url">https://shindo-farm-77.byethost32.com/Shindo_Farm_Stock_77_L10/public</string>
```
Ganti sesuai domain CPanel/produksi Anda.

## Build APK

### Opsi 1: Android Studio (Paling Mudah)
1. Buka Android Studio → **Open** → pilih folder `ShindoFarmPrint`
2. Tunggu Gradle sync selesai
3. **Build** → **Build Bundle(s) / APK(s)** → **Build APK(s)**
4. APK ada di `app/build/outputs/apk/debug/app-debug.apk`

### Opsi 2: Command Line (Butuh Gradle Wrapper)
```bash
# Di folder ShindoFarmPrint
./gradlew assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

## Integrasi di Web Laravel (Sudah Disiapkan)
Di `resources/views/penjualan/nota.blade.php`, sudah ada JS:
```javascript
window.AndroidPrint = {
    printNota: function(content) {
        if (typeof AndroidPrint !== 'undefined' && AndroidPrint.cetakNota) {
            AndroidPrint.cetakNota(content);
        }
    }
};
```
Dan tombol Print di `penjualan/index.blade.php` memanggil fungsi ini.

## Prasyarat di HP User
1. Install **Bluetooth Print** (Play Store, developer: iyaltamizh, package: `com.iyaltamizh.bluetoothprint`)
2. Pair printer XP-58IIZ di Bluetooth settings HP
3. Buka app Bluetooth Print sekali → pilih printer → test print
4. Install APK ShindoFarmPrint → buka → login → cetak

## Troubleshooting
| Masalah | Solusi |
|---------|--------|
| Web tidak load | Cek `web_app_url` di strings.xml, pastikan HTTPS & domain benar |
| Cookie/session tidak tersimpan | Pastikan `CookieManager` accept cookie & third-party cookie (sudah di kode) |
| Print tidak keluar | Pastikan app "Bluetooth Print" terinstall & printer dipair |
| Cleartext HTTP error (lokal) | `network_security_config.xml` sudah allow 10.0.2.2, 192.168.x, localhost |

## Update APK ke Versi Baru
Hanya butuh rebuild jika:
- Ganti `web_app_url` (domain berubah)
- Update package/class Bluetooth Print app
- Perbaikan bug di Java/Kotlin

**Tidak perlu rebuild** untuk update fitur web (dashboard, kalkulator, dll) karena semuanya di Laravel.