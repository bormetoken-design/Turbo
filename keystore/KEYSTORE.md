# 🔐 Permanent Android Release Keystore Documentation

ไฟล์ Keystore นี้ถูกสร้างขึ้นเพื่อให้แอพพลิเคชัน **Turbo Trigger** มีลายเซ็นถาวร (Permanent Signing Key)
ทำให้ทุกครั้งที่มีการ Build เวอร์ชันใหม่ จะสามารถ**ติดตั้งอัปเดตทับแอพเดิมได้ทันทีโดยไม่ต้องถอนการติดตั้งออกก่อน**

---

## 📋 ข้อมูลลายเซ็น (Key Details)

* **Keystore File:** `keystore/release.jks`
* **Key Alias:** `turbokey`
* **Keystore Password:** `turbo12345`
* **Key Password:** `turbo12345`
* **Algorithm:** RSA 2048-bit (SHA384withRSA)
* **Validity:** 10,000 วัน (~27 ปี จนถึงปี ค.ศ. 2054)
* **Distinguished Name:** `CN=Turbo Developer, OU=Mobile, O=Turbo, L=Bangkok, ST=Bangkok, C=TH`

---

## ⚙️ การตั้งค่าใน `build.gradle`
โปรเจกต์ได้ผูกลายเซ็นนี้เข้ากับ `signingConfigs.release` เรียบร้อยแล้ว:
```groovy
signingConfigs {
    release {
        storeFile file("${project.rootDir}/keystore/release.jks")
        storePassword "turbo12345"
        keyAlias "turbokey"
        keyPassword "turbo12345"
        v1SigningEnabled true
        v2SigningEnabled true
    }
}
```

เมื่อสั่ง Build Release ผ่าน GitHub Actions หรือ Gradle ตัวไฟล์ `.apk` จะถูก Sign ด้วยคีย์นี้อัตโนมัติ 100% ครับ
