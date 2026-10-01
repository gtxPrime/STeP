# STeP — Scheduled Tribe e-Portal
### Unified Tribal Scholarship & Fellowship Sovereign Ecosystem
**Ministry of Tribal Affairs (MoTA) & National e-Governance Division (NeGD), Government of India**  
**Category:** Sovereign Public Digital Infrastructure • **Theme:** Smart Automation & Direct Benefit Transfer (DBT)

---

## 🏛️ Ecosystem Overview

The **STeP (Scheduled Tribe e-Portal)** ecosystem is an end-to-end digital governance suite built for the Ministry of Tribal Affairs (MoTA). It collapses **5 historically fragmented tribal scholarship and fellowship schemes** across **3 disparate legacy portals** (*National Scholarship Portal [NSP]*, *Canara Bank SFMP*, and *Standalone NOS*) into a unified, transparent, and automated architecture.

The platform provides a dual-tier mobile application suite for both students and central/state welfare administration, backed by sovereign data registries (DigiLocker, APAAR, NPCI Aadhaar Payment Bridge, and UDISE+):

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           STeP SOVEREIGN ECOSYSTEM                              │
├───────────────────────────────────────┬─────────────────────────────────────────┤
│    📱 Student Mobile Application      │     🏛️ Admin Mobile Suite & Web Panel   │
│         (Package: com.step.app)       │        (Package: com.step.admin)        │
│    • Google Sign-In + Firebase Auth   │    • Real-time Firebase Firestore Sync  │
│    • DigiLocker Sandbox Auto-Fetch    │    • Executive KPI Overview             │
│    • AI Scheme Entitlement Maximizer  │    • Application Scrutiny Queue         │
│    • Unified Document Wallet          │    • ModalBottomSheet Scheme Creator    │
│    • 30-Day DBT Statutory SLA Tracker │    • Student Digital Dossier Inspector  │
└───────────────────────────────────────┴─────────────────────────────────────────┘
```

---

## 📱 Dual Android Mobile Applications

### 1. 🎓 STeP Student Application (`com.step.app`)
Designed for tribal scholars across India, with special PVTG (Particularly Vulnerable Tribal Groups) accessibility features:
- **Authentication:** Integrated Google Sign-In linked to Firebase Authentication (`FirebaseAuth.signInWithCredential`), supporting both Google OAuth and fallback credentials.
- **DigiLocker Sandbox Integration:** Automated one-click verification of Caste Certificates, Income Certificates, Class 10/12 Marksheets, and APAAR IDs with cryptographic SHA-256 seal generation (`DigiLockerSandboxManager.kt`).
- **Document Wallet (`DocumentWalletScreen.kt`):** Interactive digital wallet storing verified credentials with full-screen image previews, QR verification codes, and download mechanisms.
- **5-Scheme Entitlement Wizard:** Analyzes student profile parameters (state, parental income, education level, stream) to recommend and rank eligible schemes by financial yield.
- **DBT Ledger & Statutory SLA Tracker:** Live tracking of NPCI Aadhaar Payment Bridge (APB) disbursement status, UTR reference numbers, and a 30-day escalation countdown.

### 2. 🏛️ STeP Admin Application (`com.step.admin`)
Designed for MoTA Nodal Officers, State Tribal Welfare Officers, and Verification Scrutineers:
- **Zero Mock / 100% Real Firebase Sync:** Direct real-time streaming from Cloud Firestore (`AdminFirebaseManager.kt` & `AdminRepository.kt`). Eradicated mock data; renders live registered students (e.g., `Garvit Sharma`, `gtx prime`), real scheme catalogs, and real application queues.
- **Executive KPI Dashboard:** Real-time visibility into Total Registered Scholars, Active Central Schemes, DBT Sanctioned Volume (₹ Cr), and Scrutiny Queue load.
- **Semi-Automated Scrutiny Queue (`ScrutinyQueueScreen.kt`):** Fast-track verification showing confidence scores (e.g., 96% Auto-Match), instant 1-Click Sanction, and comprehensive dossier inspection.
- **Registered Scholars Directory (`StudentsDirectoryScreen.kt`):** Complete student database searchable by name, APAAR ID, or tribe. Opens full **Digital Dossiers** with embedded document viewing matching the student wallet experience.
- **Scholarships Master & Scheme Creator (`SchemesMasterScreen.kt`):** Modern **`ModalBottomSheet`** interface (eliminating legacy dialogs) allowing administrators to publish new schemes directly to Cloud Firestore:
  - Sovereign Ministry emblem and header styling.
  - Auto-generated monospace scheme codes (`SCH-06`).
  - Nodal portal selector chips (`NSP`, `SFMP`, `NOS`).
  - Inputs for Scheme Title, Target Cohort, Income Ceilings, Annual Grants, Application Deadlines, and Eligibility Rules.
  - Reactive instant launch from both the bottom navigation and Home dashboard quick actions.
- **District Outreach Heatmap (`OutreachHeatmapScreen.kt`):** Geographic breakdown of tribal enrollment deficits across priority districts (Bastar, Gadchiroli, Mayurbhanj).

---

## 🌟 The 5 Unified MoTA Schemes

| Scheme Code | Scheme Name | Target Cohort | Income Ceiling | Maximum Sovereign Entitlement | Source System |
|:---|:---|:---|:---|:---|:---|
| **SCH-01** | **Pre-Matric Scholarship** | Class IX & X | ≤ ₹2.50 Lakh/yr | ₹3,500 (Day Scholar) / ₹7,000 (Hosteller) + ₹1,000 books | NSP |
| **SCH-02** | **Post-Matric Scholarship (PMS-ST)** | Class XI, XII, Degree, PG | ≤ ₹2.50 Lakh/yr | 100% Tuition Waiver + Maintenance up to ₹13,500/yr | NSP (75:25 Central/State) |
| **SCH-03** | **Top Class Education** | Premier Institutes (IIT, IIM, NIT, AIIMS, NLU) | ≤ ₹6.00 Lakh/yr | Full Tuition + ₹3,000/mo Living + ₹5,000/yr Books + ₹45,000 Laptop | SFMP / Canara Bank |
| **SCH-04** | **National Fellowship (NFST)** | Full-time M.Phil & Ph.D. scholars | **NO Income Limit** | JRF: ₹31,000/mo • SRF: ₹35,000/mo + HRA + ₹25,000 Contingency | SFMP (UGC-NET / JRF) |
| **SCH-05** | **National Overseas Scholarship (NOS)** | Top 500 QS World Ranked Foreign Universities | ≤ ₹8.00 Lakh/yr | 100% Tuition + £9,900 (UK) / $15,400 (USA) per annum + Airfare | Standalone NOS Portal |

---

## 🔐 Release Signing & Key Architecture

Both applications are signed using the same unified sovereign keystore:

- **Keystore File:** `F:/MyAppKey/alphaKey.jks`
- **Key Alias:** `key0`
- **Configuration:** Managed via `key.properties` (gitignored for security) in both the root project and `STeP-Admin/`:

```properties
storeFile=/path/to/your/alphaKey.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=key0
keyPassword=YOUR_KEY_PASSWORD
```

### Production Build Outputs

| Application | Artifact Type | Path | Size |
|:---|:---|:---|:---|
| **STeP Student App** (`com.step.app`) | Android App Bundle (`.aab`) | `app/build/outputs/bundle/release/app-release.aab` | ~16.58 MB |
| **STeP Student App** (`com.step.app`) | Universal APK (`.apk`) | `app/build/outputs/apk/release/app-release.apk` | ~17.04 MB |
| **STeP Admin App** (`com.step.admin`) | Android App Bundle (`.aab`) | `STeP-Admin/app/build/outputs/bundle/release/app-release.aab` | ~16.25 MB |
| **STeP Admin App** (`com.step.admin`) | Universal APK (`.apk`) | `STeP-Admin/app/build/outputs/apk/release/app-release.apk` | ~16.71 MB |

*(Direct release copies are also synced under `web/assets/step-student-release.apk` and `web/assets/step-admin-release.apk`)*.

---

## 🚀 Building & Installing

### Prerequisites
- JDK 17+
- Android SDK (compileSdk 37, minSdk 24)
- PowerShell (Windows) or Bash (macOS/Linux)
- Connected Android Device or Emulator with USB/Wireless Debugging enabled

### 1. Build Signed Release AABs (for Google Play / Production)
```powershell
# Build Student App Release Bundle
.\gradlew.bat bundleRelease

# Build Admin App Release Bundle
cd STeP-Admin
.\gradlew.bat bundleRelease
cd ..
```

### 2. Build Signed Release APKs (for Direct Installation)
```powershell
# Build Student App Release APK
.\gradlew.bat assembleRelease

# Build Admin App Release APK
cd STeP-Admin
.\gradlew.bat assembleRelease
cd ..
```

### 3. Install on Connected Device via ADB
```powershell
# Install Admin App
adb install -r "STeP-Admin/app/build/outputs/apk/release/app-release.apk"

# Install Student App
adb install -r "app/build/outputs/apk/release/app-release.apk"
```

---

## 📂 Project Directory Structure

```
Educon/
├── app/                                    # STeP Student Android App (com.step.app)
│   ├── src/main/java/com/step/app/
│   │   ├── digilocker/                     # DigiLocker Sandbox & mock provider
│   │   ├── firebase/                       # FirebaseManager (Auth & Firestore)
│   │   ├── storage/                        # Shared hosting upload client
│   │   └── ui/screens/                     # Jetpack Compose Screens (Wallet, Schemes, Profile, etc.)
│   └── build.gradle.kts                    # App Gradle configuration with alphaSigning
├── STeP-Admin/                             # STeP Admin Android App (com.step.admin)
│   ├── app/src/main/java/com/step/admin/
│   │   ├── data/                           # AdminRepository & Data Models
│   │   ├── firebase/                       # AdminFirebaseManager (Live cloud sync)
│   │   ├── ui/screens/                     # Screens: Dashboard, Scrutiny, Schemes, Dossiers
│   │   └── AdminMainActivity.kt            # Single-activity navigation & bottom bar
│   └── app/build.gradle.kts                # Admin Gradle configuration with alphaSigning
├── web/                                    # Admin Web Panel & Asset Distribution
│   ├── assets/                             # Released APKs & media assets
│   ├── index.html                          # Web Dashboard UI
│   └── server.py                           # Python HTTP server
├── key.properties                          # Signing configuration (alphaKey.jks)
├── CHANGELOG.md                            # Comprehensive version and commit changelog
└── README.md                               # Project documentation
```

---

## 🤝 Contributing & License
Maintained for the **Ministry of Tribal Affairs (MoTA) Smart India Hackathon Ecosystem**.  
Repository: [gtxPrime/STeP](https://github.com/gtxPrime/STeP.git)  
All rights reserved © Government of India / NeGD.
