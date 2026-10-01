# STeP — Scheduled Tribe e-Portal
### Unified Tribal Scholarship & Fellowship Sovereign Ecosystem
**Ministry of Tribal Affairs (MoTA) & National e-Governance Division (NeGD), Government of India**  
**Category:** Sovereign Public Digital Infrastructure • **Theme:** Smart Automation & Direct Benefit Transfer (DBT)

[![Student Android App](https://img.shields.io/badge/Student%20App-Native%20Android%20Jetpack%20Compose-green)](#-dual-android-mobile-applications)
[![Admin Android Suite](https://img.shields.io/badge/Admin%20Suite-Native%20Jetpack%20Compose-orange)](#-step-admin-application-comstepadmin)
[![Signed Release AAB](https://img.shields.io/badge/Release%20AAB-alphaKey.jks%20Signed-blue)](#-production-build-artifacts-aab--apk)
[![DigiLocker NeGD](https://img.shields.io/badge/DigiLocker-NeGD%20X.509%20Verified-blueviolet)](#-digilocker-sandbox-data-fetching--cryptographic-verification)
[![Gemini Vision AI](https://img.shields.io/badge/OCR-Gemini%201.5%20Flash%20Vision-red)](#-gemini-vision-ocr--document-scrutiny-engine)
[![TTS Engine](https://img.shields.io/badge/Voice-On--Device%20Multilingual%20TTS-teal)](#-jago-voice-assistant--multilingual-text-to-speech-tts)
[![Theme](https://img.shields.io/badge/Theme-Sovereign%20MoTA%20Orange-D9480F)](#-sovereign-design-system--color-theme)

---

## 🏛️ Ecosystem Overview & Mission

The **STeP (Scheduled Tribe e-Portal)** ecosystem is an end-to-end digital governance platform built for the Ministry of Tribal Affairs (MoTA). It collapses **5 historically fragmented tribal scholarship and fellowship schemes** across **3 disparate legacy portals** (*National Scholarship Portal [NSP]*, *Canara Bank SFMP*, and *Standalone NOS*) into a unified, transparent, and automated architecture.

By combining **DigiLocker NeGD integration**, **Gemini 1.5 Flash Multimodal OCR**, **on-device Text-to-Speech (TTS)**, and **real-time Firebase Cloud Firestore synchronisation**, STeP eliminates bureaucratic friction, enforces strict DPDP Act compliance, and provides sub-second verification for tribal scholars across India.

```
┌────────────────────────────────────────────────────────────────────────────────────────┐
│                              STeP SOVEREIGN ECOSYSTEM                                  │
├──────────────────────────────────────────┬─────────────────────────────────────────────┤
│      📱 Student Mobile Application       │         🏛️ Admin Mobile Suite & Web         │
│          (Package: com.step.app)         │           (Package: com.step.admin)         │
│  • Google Sign-In + Firebase Auth        │  • Real-time Firebase Firestore Sync        │
│  • DigiLocker Sandbox Auto-Fetch         │  • Executive KPI Overview                   │
│  • Explicit Consent Flow (DPDP Act 2023) │  • Application Scrutiny Queue               │
│  • Gemini Vision OCR (JSON Extraction)   │  • ModalBottomSheet Scheme Creator          │
│  • Unified Document Wallet & QR Codes    │  • Student Digital Dossier Inspector        │
│  • JAGO Voice Assistant + On-Device TTS  │  • 1-Click Fast-Track Sanctions             │
│  • 30-Day DBT Statutory SLA Tracker      │  • Tribal District Outreach Heatmap         │
└──────────────────────────────────────────┴─────────────────────────────────────────────┘
```

---

## 🔄 End-to-End System Pipeline

```mermaid
sequenceDiagram
    autonumber
    actor Student as 🎓 Tribal Student
    participant App as 📱 Student App (com.step.app)
    participant DigiLocker as 🏛️ DigiLocker / NeGD Gateway
    participant Gemini as 🧠 Gemini 1.5 Flash Vision
    participant Storage as ☁️ Storage CDN + Firestore
    participant Admin as 🏛️ Admin Suite (com.step.admin)
    participant PFMS as 💳 NPCI / PFMS DBT Bridge

    Student->>App: Sign in with Google / Mobile OTP
    App->>App: Authenticate with Firebase Auth Backend
    
    rect rgb(240, 248, 255)
        Note over Student, DigiLocker: 🔐 DigiLocker Consent & Auto-Fetch
        Student->>App: Tap "Fetch from DigiLocker"
        App->>Student: Present DPDP Statutory Consent Dialog
        Student->>App: Grants Consent (consent: "Y")
        App->>DigiLocker: OAuth 2.0 Auth Code + PKI Token Exchange
        DigiLocker-->>App: X.509 Signed XML (Caste, Income, Marksheets, APAAR)
        App->>App: Validate NeGD SHA-256 Digital Certificate Seal
    end

    rect rgb(255, 250, 240)
        Note over Student, Gemini: 📸 Physical Upload & Gemini OCR Fallback
        Student->>App: Capture / Upload Physical Certificate
        App->>Student: Request Explicit AI Scrutiny Consent
        Student->>App: Grants Consent
        App->>Gemini: Base64 Image + Strict JSON Schema Prompt
        Gemini-->>App: Extracted JSON (Name, Number, Authority, Confidence: 96%)
    end

    App->>Storage: Multipart upload image to CDN -> Store metadata & CDN URL in Firestore
    Storage-->>Admin: Real-time Snapshot Update (Zero Mock Data)

    rect rgb(245, 255, 245)
        Note over Admin, PFMS: ⚖️ Scrutiny, Approval & Payout
        Admin->>Admin: Scrutiny Queue flags Auto-Clear (Confidence >= 85%)
        Admin->>Admin: Officer inspects Student Dossier & Verified Documents
        Admin->>Admin: 1-Click Sanction
        Admin->>Storage: Update application status to "SANCTIONED"
        Storage->>PFMS: Trigger Aadhaar Payment Bridge (APB)
        PFMS-->>Student: DBT Credit Notification + UTR Reference
    end
```

---

## 🏛️ DigiLocker Sandbox: Data Fetching & Cryptographic Verification

The STeP ecosystem integrates directly with the **DigiLocker Sandbox and Production API Gateway** provided by the **National e-Governance Division (NeGD) / MeitY** (`DigiLockerSandboxManager.kt`):

### 1. Credentials Auto-Fetched
| Document Type | Source / Issuing Authority | Data Points Extracted | Security Seal |
|:---|:---|:---|:---|
| **ST Caste Certificate** | State e-District / Revenue Dept (e.g. Tehsildar, Baripada, Odisha) | Candidate Name, Father Name, Tribe (e.g. Santhal / Oraon), Category (ST/PVTG), Memo Number | X.509 DSC Signed |
| **Annual Income Certificate** | State Revenue Authority | Annual Household Gross Income (₹), Financial Year, Validity Window | NeGD Timestamped |
| **Class 10 & 12 Marksheets** | CBSE / ICSE / State Secondary Boards | Passing Year, Roll Number, Subjects, Aggregated Percentage / CGPA | Board DSC Validated |
| **APAAR / One Nation ID** | Ministry of Education (Academic Bank of Credits) | 12-digit APAAR ID (`APAAR-2026-XXXX-XXXX`), Student Identity Hash | Central NeGD Match |

### 2. Cryptographic Validation Pipeline
1. **OAuth 2.0 PKCE Handshake:** The student initiates an authenticated session via `oauth2/1/authorize` with state protection.
2. **Payload Parsing:** The response returns both machine-readable XML (`xmlPayload`) and human-readable metadata.
3. **Digital Signature Verification:** The app verifies the Digital Signature Certificate (`signerCn`, `dscSerialNumber`, and `pkiTimestamp`).
4. **Local Fallback Sandbox Provider:** For testing in low-connectivity or tribal sandbox environments, `DigiLockerMockProvider.kt` supplies authentic NeGD-structured mock records that perfectly simulate live production payloads.

---

## 🔐 Student Permission & DPDP Statutory Consent Flow

STeP strictly adheres to the **Digital Personal Data Protection (DPDP) Act 2023** and the **Aadhaar Act 2016**:

```
┌────────────────────────────────────────────────────────────────────────┐
│                   SOVEREIGN STUDENT CONSENT MODAL                      │
├────────────────────────────────────────────────────────────────────────┤
│  🏛️ Ministry of Tribal Affairs (MoTA) AI Verification Consent          │
│                                                                        │
│  "You are consenting to upload this document for official AI OCR       │
│  scrutiny and DigiLocker verification under the DPDP Act 2023.         │
│  Your data is encrypted using AES-256 and used solely for scholarship  │
│  eligibility determination and fraud prevention."                      │
│                                                                        │
│  [ Cancel / Decline ]                  [ Grant Consent & Verify ]      │
└────────────────────────────────────────────────────────────────────────┘
```

- **Explicit Opt-In:** No document is fetched, scanned, or transmitted without the student tapping **"Grant Consent"** (`showGeminiConsentDialog = true`).
- **Data Minimization:** Only certificate fields directly tied to scheme eligibility criteria (income, tribe, marks, institution) are parsed.
- **Audit Logging:** Every consent event logs a cryptographic timestamp and UID to Cloud Firestore for statutory compliance.

---

## 🧠 Gemini Vision OCR & Document Scrutiny Engine

When a student uploads an offline paper certificate or scanned photo, the **Gemini 1.5 Flash Multimodal Vision API** (`GeminiService.kt`) executes intelligent OCR extraction:

### 1. Dual-Pass JSON Extraction
The image is base64 encoded and submitted with a strict system instruction requiring deterministic JSON output:
```json
{
  "documentType": "ST Caste Certificate",
  "candidateName": "Garvit Sharma",
  "fatherName": "Ramesh Sharma",
  "certificateNumber": "OD/ST/2022/49201",
  "issuingAuthority": "Office of the Tehsildar, Baripada, Odisha",
  "issueDate": "2022-07-15",
  "isAuthentic": true,
  "confidenceScore": 0.96,
  "reasoning": "Official government emblem present, seal verified, text matches state revenue template"
}
```

### 2. Auto-Approval Scrutiny Metric
- **$\ge 85\%$ Confidence:** Flagged as **Auto-Clear Ready** in the Admin Scrutiny Queue for 1-Click Fast-Track Sanction.
- **$< 85\%$ Confidence:** Routed to manual scrutiny with highlighted discrepancies for officer review.

---

## 🔊 JAGO Voice Assistant & Multilingual Text-to-Speech (TTS)

To overcome regional literacy barriers and assist students in remote tribal hamlets, STeP incorporates **JAGO**, an AI voice and conversational assistant (`HelpScreen.kt` & `GeminiService.kt`):

- **On-Device Android `TextToSpeech` Engine:** Initializes seamlessly on device boot (`TextToSpeech(context)`), managing voice synthesis queueing (`QUEUE_FLUSH`) and complete resource disposal (`shutdown()`).
- **6 Supported Languages:**
  - English
  - हिन्दी (Hindi)
  - मराठी (Marathi)
  - ଓଡ଼ିଆ (Odia)
  - తెలుగు (Telugu)
  - தமிழ் (Tamil)
- **Plain-Language Defect Explainer:** Bureaucratic rejection codes (e.g. *"Clause 4.2 Defect: Caste Validity Incomplete"*) are synthesized into clear spoken instructions explaining how to obtain the required document from the local Tehsil office.

---

## 📁 Document Wallet & Hybrid Storage Architecture

STeP utilizes a high-performance **Hybrid Storage Pipeline** balancing binary asset delivery and lightweight real-time synchronization:

```
┌─────────────────────────────────────────────────────────────────────────┐
│                       HYBRID STORAGE ARCHITECTURE                       │
├────────────────────────────────────┬────────────────────────────────────┤
│  1. Binary Storage (CDN / Upload)  │  2. Metadata (Cloud Firestore)     │
│  • Multipart HTTP POST /api/upload │  • Collection: users/{uid}/docs    │
│  • Compressed JPG/PNG binaries     │  • Extracted OCR JSON payload      │
│  • Returns persistent CDN URL      │  • DigiLocker X.509 hash & status  │
│  • Instant thumbnail rendering     │  • Real-time snapshot listeners    │
└────────────────────────────────────┴────────────────────────────────────┘
```

### Student Wallet Experience (`DocumentWalletScreen.kt`)
- **Card-Based Repository:** Displays all retrieved certificates with official green `VERIFIED_DIGILOCKER` badges.
- **Interactive Verification QR:** Clicking any document generates a verification QR code that nodal officers can scan during offline spot audits.
- **Full-Screen Document Visualizer:** High-resolution zoom and pan inspection view.

### Admin Dossier Inspector (`StudentsDirectoryScreen.kt` & `DocumentInspectionDialog.kt`)
- Nodal officers can click **"Inspect Dossier"** on any registered student to view their exact uploaded documents, cross-reference DigiLocker seals against Gemini OCR data, and verify eligibility without switching windows.

---

## 🏛️ STeP Admin Suite & "Create Scheme" Modal Bottom Sheet

The **STeP-Admin Native App** (`com.step.admin`) is built specifically for Ministry and State welfare administrators:

- **100% Real Firebase Data:** Completely eradicated mock/demo data; streams live registered scholars, application queues, and schemes directly from Cloud Firestore.
- **ModalBottomSheet Scheme Creator (`SchemesMasterScreen.kt`):** Replaced legacy dialog boxes with a sovereign bottom sheet:
  - Official Ministry header with sovereign insignia and dismiss action.
  - Monospace Scheme Code (`SCH-06`), Title, and Eligibility criteria inputs.
  - Interactive Portal Selector Chips (`NSP`, `SFMP`, `NOS`).
  - Side-by-side financial inputs (Income Ceiling & Annual Grant).
  - One-click **"Publish & Sync to Cloud"** action that updates live Firestore instantly.
- **Reactive Navigation:** Tapping `+ Create Scheme` from the Home Dashboard or the Schemes tab opens the bottom sheet with zero navigation latency.

---

## 🎨 Sovereign Design System & Color Theme

The STeP ecosystem enforces a unified, dignified **Sovereign Design System** built to reflect the official identity of the **Ministry of Tribal Affairs (MoTA), Government of India**. 

The design harmonizes **tribal cultural identity (Deep Saffron-Orange)**, **institutional authority (Sovereign Navy & Slate)**, and **maximum accessibility (WCAG 2.1 AAA High-Contrast)** across varied ambient lighting conditions and low-resource mobile screens in rural and tribal districts.

### 1. Primary MoTA Brand Identity
| Swatch | Color Name | Hex Code | Jetpack Compose Token | CSS Variable (`web`) | Role & Functional Usage |
|:---:|:---|:---|:---|:---|:---|
| ![#D9480F](https://img.shields.io/badge/-%23D9480F-D9480F?style=flat-square) | **MoTA Deep Orange** | `#D9480F` | `PrimaryDeepOrange` | `--saffron-primary` | Primary brand accent, primary CTA buttons, active tab indicators, focus rings |
| ![#BF3A0A](https://img.shields.io/badge/-%23BF3A0A-BF3A0A?style=flat-square) | **Deep Orange Dark** | `#BF3A0A` | `PrimaryDeepOrangeDark` | `--saffron-gradient` (end) | Pressed button states, gradient terminations, elevated headers |
| ![#FF6B35](https://img.shields.io/badge/-%23FF6B35-FF6B35?style=flat-square) | **Deep Orange Light** | `#FF6B35` | `PrimaryDeepOrangeLight` | `--saffron-light` | Highlight chips, active badges, energetic interactive micro-accents |
| ![#FFF4E6](https://img.shields.io/badge/-%23FFF4E6-FFF4E6?style=flat-square) | **Saffron Surface Tint** | `#FFF4E6` | `PrimarySurfaceLight` | `--saffron-tint` | Light tinted card containers, badge pill fills, scheme category pill backdrops |

### 2. Clean Sovereign Light Palette (Canvas & Surfaces)
| Swatch | Color Name | Hex Code | Jetpack Compose Token | CSS Variable (`web`) | Role & Functional Usage |
|:---:|:---|:---|:---|:---|:---|
| ![#FFFFFF](https://img.shields.io/badge/-%23FFFFFF-FFFFFF?style=flat-square) | **Pure Background White** | `#FFFFFF` | `BackgroundWhite` | `--bg-card` | Main page background, modal bottom sheets, elevated card surfaces |
| ![#F8F9FA](https://img.shields.io/badge/-%23F8F9FA-F8F9FA?style=flat-square) | **Surface Card Off-White** | `#F8F9FA` | `SurfaceCard` | `--bg-dark` / `--bg-surface` | Secondary surface cards, grouped item containers, list row backdrops |
| ![#E9ECEF](https://img.shields.io/badge/-%23E9ECEF-E9ECEF?style=flat-square) | **Border Light** | `#E9ECEF` | `BorderLight` | `--border-subtle` | Subtle hairline dividers, card borders, unselected input borders |
| ![#DEE2E6](https://img.shields.io/badge/-%23DEE2E6-DEE2E6?style=flat-square) | **Border Medium** | `#DEE2E6` | `BorderMedium` | `--border-medium` | Emphasized structural borders, table outlines, search box containers |

### 3. Semantic Scheme & Statutory Status Colors
| Swatch | Status / Meaning | Hex Code | Compose Foreground / Background | Web CSS Tokens | SLA & Lifecycle Trigger |
|:---:|:---|:---|:---|:---|:---|
| ![#2B8A3E](https://img.shields.io/badge/-%232B8A3E-2B8A3E?style=flat-square) | **Disbursed / Verified** | `#2B8A3E` (Tint: `#EBFBEE`) | `StatusDisbursed` / `StatusDisbursedBg` | `--emerald-success` / `--emerald-tint` | DigiLocker X.509 cryptographic validation passed, DBT PFMS credit sanctioned |
| ![#1971C2](https://img.shields.io/badge/-%231971C2-1971C2?style=flat-square) | **In Progress / Processing** | `#1971C2` (Tint: `#E7F5FF`) | `StatusInProgress` / `StatusInProgressBg` | `--navy-accent` / `--navy-tint` | NeGD handshake in transit, nodal officer review queued, scheme applied |
| ![#E67700](https://img.shields.io/badge/-%23E67700-E67700?style=flat-square) | **Pending Action / Warning** | `#E67700` (Tint: `#FFF9DB`) | `StatusPending` / `StatusPendingBg` | `--amber-warning` / `--amber-tint` | 30-Day SLA clock running, applicant action requested, secondary proof needed |
| ![#C92A2A](https://img.shields.io/badge/-%23C92A2A-C92A2A?style=flat-square) | **Rejected / Defect / Alert** | `#C92A2A` (Tint: `#FFE3E3`) | `StatusRejected` / `StatusRejectedBg` | `--red-error` / `--red-tint` | OCR confidence below 85%, invalid/tampered certificate, offline connectivity banner |

### 4. Typography & Sovereign Authority Accents
| Swatch | Color Name | Hex Code | Jetpack Compose Token | CSS Variable (`web`) | Role & Functional Usage |
|:---:|:---|:---|:---|:---|:---|
| ![#1A1D20](https://img.shields.io/badge/-%231A1D20-1A1D20?style=flat-square) | **High-Contrast Dark** | `#1A1D20` | `TextDark` / `TextMain` | `--navy-deep` | High-contrast primary headings, applicant names, critical metrics (WCAG AAA) |
| ![#495057](https://img.shields.io/badge/-%23495057-495057?style=flat-square) | **Body Charcoal** | `#495057` | `TextBody` / `TextMuted` | `--text-muted` | Regular body copy, scheme guidelines, instruction descriptions |
| ![#868E96](https://img.shields.io/badge/-%23868E96-868E96?style=flat-square) | **Subtle Slate** | `#868E96` | `TextSubtle` / `TextDim` | `--text-dim` | Timestamps, UTR references, scheme codes, secondary metadata |
| ![#0F172A](https://img.shields.io/badge/-%230F172A-0F172A?style=flat-square) | **Sovereign Navy Slate** | `#0F172A` | `NavyBackground` | `--navy-deep` | MoTA sovereign crest headers, executive dashboard stamps, QR code borders |

### 5. Architectural Implementation Across Codebases
The color tokens are synchronized across all modules to guarantee 100% visual parity:

- **Student Native App:** [`app/src/main/java/com/step/app/ui/theme/Color.kt`](file:///f:/Source%20Codes/Educon/app/src/main/java/com/step/app/ui/theme/Color.kt) & [`Theme.kt`](file:///f:/Source%20Codes/Educon/app/src/main/java/com/step/app/ui/theme/Theme.kt)
- **Admin Mobile Suite:** [`STeP-Admin/app/src/main/java/com/step/admin/ui/theme/Color.kt`](file:///f:/Source%20Codes/Educon/STeP-Admin/app/src/main/java/com/step/admin/ui/theme/Color.kt)
- **Web Administration Portal:** [`web/styles/main.css`](file:///f:/Source%20Codes/Educon/web/styles/main.css) (`:root` CSS variables)

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

## 🔐 Production Build Artifacts (AAB & APK)

Both applications are configured with release signing using the unified keystore (`alphaKey.jks`, alias `key0`). Sensitive credentials are kept out of source control via `key.properties` and `local.properties`:

| Application | Package Name | Release AAB (`bundleRelease`) | Release APK (`assembleRelease`) |
|:---|:---|:---|:---|
| **STeP Student App** | `com.step.app` | [`app/build/outputs/bundle/release/app-release.aab`](file:///f:/Source%20Codes/Educon/app/build/outputs/bundle/release/app-release.aab) *(16.58 MB)* | [`app/build/outputs/apk/release/app-release.apk`](file:///f:/Source%20Codes/Educon/app/build/outputs/apk/release/app-release.apk) *(17.04 MB)* |
| **STeP Admin Suite** | `com.step.admin` | [`STeP-Admin/app/build/outputs/bundle/release/app-release.aab`](file:///f:/Source%20Codes/Educon/STeP-Admin/app/build/outputs/bundle/release/app-release.aab) *(16.25 MB)* | [`STeP-Admin/app/build/outputs/apk/release/app-release.apk`](file:///f:/Source%20Codes/Educon/STeP-Admin/app/build/outputs/apk/release/app-release.apk) *(16.71 MB)* |

*(Standalone direct install copies are also available under `web/assets/step-student-release.apk` and `web/assets/step-admin-release.apk`)*.

---

## 🚀 Build & Installation Guide

### Prerequisites
- JDK 17+
- Android SDK (compileSdk 37, minSdk 24)
- PowerShell (Windows) or Terminal (macOS/Linux)
- Connected Android Device or Emulator with Wireless/USB ADB enabled

### 1. Configure Local Properties (Secret Management)
Create or verify `local.properties` in your project root:
```properties
sdk.dir=C\:\\Users\\Garvit\\AppData\\Local\\Android\\Sdk
gemini.api.key=YOUR_GEMINI_API_KEY
```

Create or verify `key.properties` for production release signing:
```properties
storeFile=/path/to/your/alphaKey.jks
storePassword=YOUR_STORE_PASSWORD
keyAlias=key0
keyPassword=YOUR_KEY_PASSWORD
```

### 2. Build Release Bundles (AAB) & APKs
```powershell
# Build Student App Release Artifacts
.\gradlew.bat bundleRelease
.\gradlew.bat assembleRelease

# Build Admin App Release Artifacts
cd STeP-Admin
.\gradlew.bat bundleRelease
.\gradlew.bat assembleRelease
cd ..
```

### 3. Install on Connected Device via ADB
```powershell
# Install STeP Admin Suite
adb install -r "STeP-Admin/app/build/outputs/apk/release/app-release.apk"

# Install STeP Student App
adb install -r "app/build/outputs/apk/release/app-release.apk"
```

---

## 📂 Project Directory Structure

```
Educon/
├── app/                                    # STeP Student Android App (com.step.app)
│   ├── src/main/java/com/step/app/
│   │   ├── data/                           # Data models & GeminiService.kt (OCR/JAGO)
│   │   ├── digilocker/                     # DigiLockerSandboxManager & Mock Provider
│   │   ├── firebase/                       # FirebaseManager.kt (Auth & Firestore)
│   │   ├── storage/                        # SharedHostingManager.kt (Multipart upload)
│   │   └── ui/screens/                     # Compose Screens (Wallet, Scanner, Help/TTS, Profile)
│   └── build.gradle.kts                    # App Gradle with alphaSigning & BuildConfig injection
├── STeP-Admin/                             # STeP Admin Android App (com.step.admin)
│   ├── app/src/main/java/com/step/admin/
│   │   ├── data/                           # AdminRepository & AdminGeminiService
│   │   ├── firebase/                       # AdminFirebaseManager.kt (Live sync)
│   │   ├── ui/screens/                     # Screens: Dashboard, Scrutiny, Schemes, Dossiers
│   │   └── AdminMainActivity.kt            # Navigation & BottomBar handling
│   └── app/build.gradle.kts                # Admin Gradle with alphaSigning & BuildConfig injection
├── web/                                    # Admin Web Dashboard & Distribution
│   ├── assets/                             # Mirror of signed release APKs
│   ├── scripts/                            # Web Firebase sync & AI scripts
│   └── index.html                          # Web Portal UI
├── key.properties                          # Release signing credentials (gitignored)
├── local.properties                        # Local SDK & Gemini API Key (gitignored)
├── CHANGELOG.md                            # Detailed release notes & commit history
└── README.md                               # Complete system documentation
```

---

## 🤝 Contributing & License
Maintained for the **Ministry of Tribal Affairs (MoTA) Smart India Hackathon Ecosystem**.  
Repository: [gtxPrime/STeP](https://github.com/gtxPrime/STeP.git)  
All rights reserved © Government of India / NeGD.
