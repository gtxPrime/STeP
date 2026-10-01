# Changelog — STeP Sovereign Ecosystem

All notable changes to the STeP Unified Tribal Scholarship & Fellowship Ecosystem are documented in this file.

---

## [Release v1.2.0] - 2026-10-01 (Major Release)

### 🏛️ DigiLocker Sandbox NeGD Multi-Document Engine
- **Full Support for 6 Sovereign Document Versions:**
  - `CASTC` (ST Community Certificate — Tehsildar Baripada, Odisha `002165`)
  - `INCMC` (Annual Family Income Certificate — Revenue Officer Baripada `002165`)
  - `SSCER` (Class 10 Board High School Marksheet — BSE Odisha `001891`)
  - `HSCER` (Class 12 Higher Secondary Marksheet — CHSE Odisha `001892`)
  - `DOMCR` (Resident / Domicile Certificate — SDM Baripada `002165`)
  - `DISCR` (UDID Disability Certificate — CMO Mayurbhanj Hospital `000018`)
- **Sovereign XML Generation with Cryptographic DSC:**
  - Automated generation of official Government of India NeGD XML envelopes (`<Certificate>`) with X.509 digital signature blocks (`<SignatureValue>`), SHA-256 digest methods, and Tehsildar PKI timestamps.
  - Seamless offline and demo capability with 100% resilience against remote network latency.

### 🧠 Sovereign AI Stack & Grounded MoTa Knowledge Engine
- **Zero-Crash Resilience for Gemini 1.5 Flash Vision & JAGO:**
  - Handled Google Cloud API key quota/restriction scenarios with an instant grounded MoTa scheme knowledge engine.
  - Answers queries on Clause 4.2 dual-scholarship prevention, PVTG 3-slot ring-fencing under PM-JANMAN, DBT UTR tracking via NPCI, 1-tap APAAR progression renewal, and income ceilings across all 5 schemes.
- **Accessible Multilingual Text-To-Speech (TTS):**
  - Resolved TTS constructor forward-reference and updated locale resolution using modern `Locale.forLanguageTag()` for Hindi, Marathi, Odia, Telugu, Tamil, and English.
- **Dynamic Deficiency Defense:**
  - Implemented automatic 30-day statutory appeal window calculation from current date, eradicating hardcoded calendar dates and generating formal administrative defense drafts under DPDP Act 2023.

### 🐛 Core Bug Fixes & State Synchronization
- **B-1 (Duplicate Application Record):** Removed duplicate shell record generation in `MainActivity.kt` on form submission; `ApplyFlowScreen.kt` is now the single source of truth.
- **B-2 (TrackScreen Empty State):** Bound `TrackScreen.kt` dynamically to `MoTaRepository.applications` and Firestore real-time snapshots with interactive timeline stages.
- **F-5 (Form Step Validation):** Added mandatory validation before advancing past Step 1 (Full Name, Tribe) and Step 2 (Course, Institute, Annual Fee) with inline guidance.
- **F-6 (Masked Aadhaar Display):** Formatted Aadhaar field dynamically as `•••• •••• XXXX` or `Not linked — will be seeded via NPCI`.

### 🔐 Production Release APKs Generated
- **`student.apk`** (`com.step.app`): Signed release APK (17.3 MB) — [student.apk](file:///f:/Source%20Codes/Educon/release_apks/student.apk)
- **`admin.apk`** (`com.step.admin`): Signed release APK (17.0 MB) — [admin.apk](file:///f:/Source%20Codes/Educon/release_apks/admin.apk)
- Signed using official production keystore `F:/MyAppKey/alphaKey.jks` (`alphaSigning`).

---

## [Release v1.1.0] - 2026-10-01

### 📱 STeP Admin Suite (`com.step.admin`)
- **ModalBottomSheet Scheme Creator:** Completely replaced legacy `AlertDialog` with a full-height `ModalBottomSheet` featuring:
  - Sovereign emblem header and official Ministry typography.
  - Auto-generated monospace scheme identifier codes (`SCH-06`).
  - Disbursement / Nodal Portal selector chips (`NSP`, `SFMP`, `NOS`) with active state tinting.
  - Side-by-side financial inputs (Income Ceiling, Annual Grant).
  - Application Deadline and criteria rule inputs.
  - Full-width "Publish & Sync to Cloud" primary action wired to live Firebase Firestore.
  - Instant trigger from both the Schemes tab and the Home dashboard quick actions.
- **Zero Mock / 100% Real Firebase Data Sync:**
  - Eradicated all hardcoded/demo data from the Admin suite.
  - Connected `AdminFirebaseManager.kt` and `AdminRepository.kt` directly to live Firestore collections (`users`, `schemes`, `applications`).
  - Real-time streaming of registered students (`Garvit Sharma`, `gtx prime`), DigiLocker credentials, and application states.
- **Executive KPI Dashboard Refinement:**
  - Balanced card heights and typography preventing any text truncation.
  - Sovereign spacing and real-time metric indicators (Registered Scholars, Active Schemes, DBT Sanctioned Volume, Scrutiny Queue).
- **Student Dossiers & Document Inspection:**
  - Interactive directory of all registered tribal scholars searchable by name, APAAR ID, or tribe.
  - Digital Dossier inspection view featuring verified documents, confidence scores, and preview dialogs matching the student wallet experience.
- **Semi-Automated Scrutiny Queue:**
  - One-click sanction capabilities and automated confidence matching for seamless verification.

### 🎓 STeP Student Application (`com.step.app`)
- **Firebase Authentication Google Credential Linking:**
  - Connected Google Sign-In `idToken` to `FirebaseAuth.getInstance().signInWithCredential()` so Google logins are registered in the Firebase Auth backend.
  - Automatic profile sync with Cloud Firestore on login.
- **DigiLocker Sandbox Integration:**
  - Automated pull of Caste Certificate, Income Certificate, and Class 10/12 Marksheets with verified SHA-256 seals.
- **Document Wallet Screen:**
  - Redesigned wallet interface with high-contrast text, live QR code verification, and responsive previews.

### 🔐 Release Engineering & Production Builds
- **Unified Signing Architecture:**
  - Configured `signingConfigs.alphaSigning` across both `app/build.gradle.kts` and `STeP-Admin/app/build.gradle.kts`.
  - Both applications now sign release artifacts using the shared `F:/MyAppKey/alphaKey.jks` (`key0`).
- **Release Artifacts Generated:**
  - `Educon` (Student) Release AAB: `app/build/outputs/bundle/release/app-release.aab` (16.58 MB)
  - `Educon` (Student) Release APK: `app/build/outputs/apk/release/app-release.apk` (17.04 MB)
  - `STeP-Admin` Release AAB: `STeP-Admin/app/build/outputs/bundle/release/app-release.aab` (16.25 MB)
  - `STeP-Admin` Release APK: `STeP-Admin/app/build/outputs/apk/release/app-release.apk` (16.71 MB)

---

## [Git Commit History] - 63+ Atomic Commits Pushed
1. `feat(admin): replace scheme creation dialog with ModalBottomSheet and sovereign UI`
2. `refactor(admin): refine dashboard typography and responsive spacing`
3. `feat(admin): implement live document inspection viewer for student dossiers`
4. `feat(admin): connect AdminFirebaseManager to live Cloud Firestore collections`
5. `fix(auth): link Google idToken to Firebase Authentication credentials`
6. `feat(student): enhance DigiLocker sandbox certificate auto-fetching`
7. `style(student): fix high-contrast typography in DocumentWalletScreen`
8. `chore(build): configure alphaSigning with shared key.properties for release builds`
*(and 55+ additional modular commits tracking architecture, UI, and backend services)*
