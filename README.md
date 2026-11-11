# 🚀 Turbo Trigger — Clean & Offline Touch Mapper (Android 9.0+)

> แอพจำลองการแตะหน้าจอแบบทริกเกอร์ 2 จุด (Trigger & Target Point) สำหรับเล่นเกมและช่วยสั่งการหน้าจอ **ไม่ต้องใช้ Root • ทำงานออฟไลน์ 100% • ไร้โฆษณา • ปลอดภัยไม่เสี่ยงโดนแบน**

---

## 🌟 คุณสมบัติเด่น (Features)

1. **ถอดระบบ Root ออก 100% (Rootless Architecture):**
   * เปลี่ยนมาใช้ระบบ **`AccessibilityService.dispatchGesture()`** ของ Android 9+ อย่างสมบูรณ์
   * ไม่ต้องเชื่อมต่อ ADB / Shizuku และไม่ต้องกังวลเรื่องระบบตรวจจับ Root ในเกม

2. **ระบบ 2 จุด (Trigger Point ➔ Target Point):**
   * **จุดที่ 1 (Trigger Button):** ปุ่มกลมลอยบนจอ วางไว้ตรงที่นิ้วถนัดกด
   * **จุดที่ 2 (Target Point):** เป้าเล็งลากไปวางทับปุ่มในเกม (เช่น ปุ่มยิง, สกิล)
   * **เส้นเชื่อม (Visual Line):** แสดงเส้นโยงระหว่างจุดกดและเป้าหมายแบบ Real-time

3. **โหมดการคลิก:**
   * **Single Tap:** แตะ 1 ครั้งเมื่อกดปุ่ม
   * **Crazy Tap / Rapid Fire:** กดค้างเพื่อแตะซ้ำๆ รัวๆ ปรับความเร็วได้ตั้งแต่ 30ms ถึง 300ms

4. **ออฟไลน์ 100% (Zero Bandwidth & Privacy First):**
   * ไม่มีสิทธิ์ `android.permission.INTERNET` ในระบบ
   * ตัดระบบ Analytics จีน (Aliyun SLS) และโฆษณาออกทั้งหมด ไม่กินเน็ตและประหยัดแบตเตอรี่

5. **คีย์ลายเซ็นถาวร (Permanent Signing Key):**
   * ผูกลายเซ็น `keystore/release.jks` ถาวร (อายุ 27 ปี ถึงปี 2054)
   * Build เวอร์ชันใหม่เมื่อใด สามารถติดตั้งอัปเดตทับตัวแอพเดิมได้ทันทีโดยไม่ต้องลบแอพ

---

## 🏗️ สถาปัตยกรรมโปรเจกต์ (Clean-Room Architecture)

```
Turbo/
├── .github/workflows/
│   └── build-apk.yml            # GitHub Actions CI: Build & Sign APK อัตโนมัติทุกครั้งที่ Push
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml   # สิทธิ์มาตรฐาน (Overlay + Accessibility) ไม่ขอเน็ต
│   │   ├── java/com/turbo/touchtrigger/
│   │   │   ├── MainActivity.kt                  # หน้าจอเปิดสิทธิ์และปรับโหมดความเร็ว
│   │   │   ├── TouchTriggerService.kt           # ตัวสั่งจำลองการแตะ (dispatchGesture)
│   │   │   ├── FloatingOverlayService.kt        # ตัวจัดการหน้าต่างลอย 2 จุด
│   │   │   ├── view/
│   │   │   │   └── ConnectingLineView.kt        # ตัววาดเส้นเชื่อมโยงระหว่าง 2 จุด
│   │   │   └── model/
│   │   │       └── TriggerConfig.kt             # การตั้งค่าพิกัดและโหมดคลิก
│   │   └── res/                                 # Layouts และ UI Assets ที่ดึงมาจากต้นฉบับ
├── keystore/
│   ├── release.jks              # คีย์ลายเซ็นถาวรสำหรับอัปเดตทับแอพ
│   └── KEYSTORE.md              # บันทึกรหัสผ่านและข้อมูล Keystore
├── build.gradle
└── settings.gradle
```

---

## 🚀 การ Build และดาวน์โหลด APK

### วิธีที่ 1: ดาวน์โหลดผ่าน GitHub Actions (แนะนำ)
เมื่อพุชโค้ดขึ้น GitHub ระบบ GitHub Actions ใน `.github/workflows/build-apk.yml` จะทำการ:
1. คอมไพล์โค้ดด้วย Android SDK และ Java 17
2. เซ็นลายเซ็น Release ด้วย `keystore/release.jks` อัตโนมัติ
3. คุณสามารถเข้าไปที่แท็บ **Actions** &rarr; เลือก Workflow ล่าสุด &rarr; ดาวน์โหลดไฟล์ `.apk` ที่หัวข้อ **Artifacts** ไปติดตั้งบนมือถือได้ทันที

### วิธีที่ 2: Build ผ่าน Gradle Local
```bash
./gradlew assembleRelease
```
ไฟล์ APK จะถูกสร้างขึ้นที่: `app/build/outputs/apk/release/app-release.apk`
