# STeP Architecture Overview

Sovereign Tribal Education Platform (STeP) connects tribal scholars with Ministry of Tribal Affairs (MoTA) scholarships via NeGD DigiLocker and PFMS DBT bridge.

## Component Flow

1. Student Client: Native Android App with on-device AES-256-GCM encryption.
2. Verification Pipeline: DigiLocker Stage-1 XML ingestion with X.509 DSC validation.
3. Admin Console: Jetpack Compose Executive Suite with 1-click sanctioning.
