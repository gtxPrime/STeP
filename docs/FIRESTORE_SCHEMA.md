# Cloud Firestore Data Architecture

Primary collections: `students`, `applications`, `schemes`, and `users/{uid}/documents`.

## Offline Caching & Snapshot Listeners

Real-time snapshots maintain offline state resilience even during network interruptions.
