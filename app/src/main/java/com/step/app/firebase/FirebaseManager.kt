package com.step.app.firebase

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.step.app.data.*

data class GoogleUser(
    val uid: String,
    val displayName: String,
    val email: String,
    val photoUrl: String = "",
    val idToken: String = "firebase_token_${System.currentTimeMillis()}"
)

data class FirestoreDocumentLink(
    val docId: String,
    val docType: String,
    val certificateNumber: String,
    val issuingAuthority: String,
    val confidenceScore: Int,
    val sharedHostingImageUrl: String,
    val firestoreCollection: String = "users/guest/documents",
    val uploadedAt: String = "28-Sep-2026, 17:30 IST"
)

object FirebaseManager {
    private const val TAG = "FirebaseManager"

    // Web Client ID configured in Google Cloud Console / Firebase Authentication
    const val WEB_CLIENT_ID = "948854630900-qd0d4mats059a13d9len9qi0rr3p5c6m.apps.googleusercontent.com"

    private val firestore by lazy { FirebaseFirestore.getInstance() }

    // Current authenticated Google user session (null by default so app prompts for login on start)
    var currentUser by mutableStateOf<GoogleUser?>(null)

    var isGoogleLoggedIn by mutableStateOf(false)

    // Stored in Firebase Firestore: metadata + Shared Hosting URLs
    val firestoreDocuments = mutableStateListOf<FirestoreDocumentLink>()

    private var isSyncInitialized = false
    private var hasSeededDemoApps = false

    /**
     * Initializes bidirectional dynamic Firestore synchronization.
     * All changes in Firebase Firestore sync to the app in real time,
     * and app actions save directly to Firebase.
     */
    fun initDynamicFirestore(userId: String = currentUser?.uid ?: "usr_guest") {
        if (isSyncInitialized) return
        isSyncInitialized = true

        Log.d(TAG, "Initializing dynamic Firestore real-time synchronization for user: $userId")

        // 1. DYNAMIC SCHEMES SYNC (Firestore Collection: "schemes")
        firestore.collection("schemes")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error listening to schemes in Firestore", error)
                    return@addSnapshotListener
                }

                if (snapshot == null || snapshot.isEmpty) {
                    // Seed initial schemes into Firebase Firestore
                    seedSchemesToFirestore()
                } else {
                    val remoteSchemes = snapshot.documents.mapNotNull { doc ->
                        try {
                            Scheme(
                                id = doc.getString("id") ?: doc.id,
                                code = doc.getString("code") ?: "SCH-00",
                                title = doc.getString("title") ?: "",
                                hindiTitle = doc.getString("hindiTitle") ?: "",
                                portal = doc.getString("portal") ?: "NSP",
                                targetClass = doc.getString("targetClass") ?: "",
                                incomeCeiling = doc.getLong("incomeCeiling"),
                                benefitSummary = doc.getString("benefitSummary") ?: "",
                                maxBenefitAmount = doc.getLong("maxBenefitAmount") ?: 0L,
                                benefitAmountFormatted = doc.getString("benefitAmountFormatted") ?: "",
                                deadlineFormatted = doc.getString("deadlineFormatted") ?: "",
                                eligibilityTag = doc.getString("eligibilityTag") ?: "Eligible",
                                description = doc.getString("description") ?: "",
                                documentsNeeded = (doc.get("documentsNeeded") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList(),
                                rules = (doc.get("rules") as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to parse scheme document ${doc.id}", e)
                            null
                        }
                    }

                    if (remoteSchemes.isNotEmpty()) {
                        MoTaRepository.schemes.clear()
                        MoTaRepository.schemes.addAll(remoteSchemes)
                        Log.d(TAG, "Loaded ${remoteSchemes.size} dynamic schemes from Firebase Firestore")
                    }
                }
            }

        // 2. DYNAMIC APPLICATIONS SYNC (Firestore Collection: "users/{userId}/applications")
        val userAppsRef = firestore.collection("users").document(userId).collection("applications")
        userAppsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to applications in Firestore", error)
                return@addSnapshotListener
            }

            if (snapshot == null || snapshot.isEmpty) {
                MoTaRepository.applications.clear()
            } else {
                val demoAppIds = setOf("NSP-2025-PMS-74921", "SFMP-2026-TC-09312", "NOS-2027-INT-0042")
                val realDocs = snapshot.documents.filter { doc ->
                    val appId = doc.getString("applicationId") ?: doc.id
                    if (demoAppIds.contains(appId) || demoAppIds.contains(doc.id)) {
                        // Clean up legacy seeded dummy applications from user's account
                        userAppsRef.document(doc.id).delete()
                        false
                    } else {
                        true
                    }
                }

                val remoteApps = realDocs.mapNotNull { doc ->
                    try {
                        val stepsRaw = doc.get("steps") as? List<Map<String, Any>>
                        val steps = stepsRaw?.map { stepMap ->
                            TimelineStep(
                                label = stepMap["label"]?.toString() ?: "",
                                date = stepMap["date"]?.toString() ?: "",
                                completed = stepMap["completed"] as? Boolean ?: false,
                                note = stepMap["note"]?.toString() ?: ""
                            )
                        } ?: emptyList()

                        val dbtRaw = doc.get("dbtDetails") as? Map<String, Any>
                        val dbtDetails = dbtRaw?.let {
                            DbtDetails(
                                utr = it["utr"]?.toString() ?: "",
                                paymentMode = it["paymentMode"]?.toString() ?: "",
                                disbursedDate = it["disbursedDate"]?.toString() ?: "",
                                bankName = it["bankName"]?.toString() ?: "",
                                accountNo = it["accountNo"]?.toString() ?: "",
                                centralShare = it["centralShare"]?.toString() ?: "",
                                stateShare = it["stateShare"]?.toString() ?: "",
                                status = it["status"]?.toString() ?: ""
                            )
                        }

                        val defRaw = doc.get("deficiency") as? Map<String, Any>
                        val deficiency = defRaw?.let {
                            DeficiencyInfo(
                                code = it["code"]?.toString() ?: "",
                                bureaucraticReason = it["bureaucraticReason"]?.toString() ?: "",
                                deadlineDate = it["deadlineDate"]?.toString() ?: "",
                                daysRemaining = (it["daysRemaining"] as? Long)?.toInt() ?: 15
                            )
                        }

                        ApplicationRecord(
                            applicationId = doc.getString("applicationId") ?: doc.id,
                            schemeId = doc.getString("schemeId") ?: "",
                            schemeTitle = doc.getString("schemeTitle") ?: "",
                            academicYear = doc.getString("academicYear") ?: "2026-27",
                            sourcePortal = doc.getString("sourcePortal") ?: "NSP",
                            stage = doc.getString("stage") ?: "SUBMITTED",
                            stageText = doc.getString("stageText") ?: "Submitted",
                            currentStepIndex = (doc.getLong("currentStepIndex") ?: 0L).toInt(),
                            sanctionAmount = doc.getLong("sanctionAmount") ?: 0L,
                            nextActionText = doc.getString("nextActionText") ?: "",
                            verificationConfidence = (doc.getLong("verificationConfidence") ?: 90L).toInt(),
                            steps = steps,
                            dbtDetails = dbtDetails,
                            deficiency = deficiency
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to parse application ${doc.id}", e)
                        null
                    }
                }

                MoTaRepository.applications.clear()
                MoTaRepository.applications.addAll(remoteApps)
                Log.d(TAG, "Loaded ${remoteApps.size} dynamic applications from Firebase Firestore")
            }
        }

        // 3. DYNAMIC PENDING ACTIONS SYNC (Firestore Collection: "users/{userId}/pending_actions")
        val userActionsRef = firestore.collection("users").document(userId).collection("pending_actions")
        userActionsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to pending actions in Firestore", error)
                return@addSnapshotListener
            }

            if (snapshot == null || snapshot.isEmpty) {
                MoTaRepository.pendingActions.clear()
            } else {
                val dummyActionIds = setOf("act_income_expiring", "act_bank_npci_fix", "act_photo_rescan")
                val realDocs = snapshot.documents.filter { doc ->
                    if (dummyActionIds.contains(doc.id)) {
                        userActionsRef.document(doc.id).delete()
                        false
                    } else true
                }
                val remoteActions = realDocs.mapNotNull { doc ->
                    try {
                        PendingAction(
                            id = doc.getString("id") ?: doc.id,
                            title = doc.getString("title") ?: "",
                            scheme = doc.getString("scheme") ?: "",
                            reason = doc.getString("reason") ?: "",
                            isUrgent = doc.getBoolean("isUrgent") ?: true,
                            actionText = doc.getString("actionText") ?: "Fix"
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                MoTaRepository.pendingActions.clear()
                MoTaRepository.pendingActions.addAll(remoteActions)
            }
        }

        // 4. DYNAMIC NOTIFICATIONS SYNC (Firestore Collection: "users/{userId}/notifications")
        val userNotifsRef = firestore.collection("users").document(userId).collection("notifications")
        userNotifsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to notifications in Firestore", error)
                return@addSnapshotListener
            }

            if (snapshot == null || snapshot.isEmpty) {
                MoTaRepository.notifications.clear()
            } else {
                val dummyNotifIds = setOf("notif_dbt_credit_01", "notif_income_expiring_02", "notif_offer_cure_03")
                val realDocs = snapshot.documents.filter { doc ->
                    if (dummyNotifIds.contains(doc.id)) {
                        userNotifsRef.document(doc.id).delete()
                        false
                    } else true
                }
                val remoteNotifs = realDocs.mapNotNull { doc ->
                    try {
                        NotificationItem(
                            id = doc.getString("id") ?: doc.id,
                            title = doc.getString("title") ?: "",
                            body = doc.getString("body") ?: "",
                            time = doc.getString("time") ?: "",
                            isUnread = doc.getBoolean("isUnread") ?: false,
                            type = doc.getString("type") ?: "PAYMENT"
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                MoTaRepository.notifications.clear()
                MoTaRepository.notifications.addAll(remoteNotifs)
            }
        }

        // 5. DYNAMIC DOCUMENTS SYNC (Firestore Collection: "users/{userId}/documents")
        val userDocsRef = firestore.collection("users").document(userId).collection("documents")
        userDocsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to documents in Firestore", error)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                val demoDocIds = setOf("doc_st_49201", "doc_inc_11093", "doc_nos_0219", "doc_st_01", "doc_inc_02", "doc_mark_03")
                val realDocs = snapshot.documents.filter { doc ->
                    if (demoDocIds.contains(doc.id)) {
                        userDocsRef.document(doc.id).delete()
                        false
                    } else true
                }

                val remoteDocs = realDocs.mapNotNull { doc ->
                    try {
                        val rawCert = doc.getString("certificateNumber") ?: ""
                        FirestoreDocumentLink(
                            docId = doc.getString("docId") ?: doc.id,
                            docType = doc.getString("docType") ?: "Certificate",
                            certificateNumber = com.step.app.security.CryptoManager.decrypt(rawCert, userId),
                            issuingAuthority = doc.getString("issuingAuthority") ?: "",
                            confidenceScore = (doc.getLong("confidenceScore") ?: 95L).toInt(),
                            sharedHostingImageUrl = doc.getString("sharedHostingImageUrl") ?: "",
                            firestoreCollection = "users/$userId/documents",
                            uploadedAt = doc.getString("uploadedAt") ?: "Just now"
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                firestoreDocuments.clear()
                firestoreDocuments.addAll(remoteDocs)

                val scannedDocs = remoteDocs.map { dl ->
                    ScannedDocument(
                        id = dl.docId,
                        documentType = dl.docType,
                        candidateName = MoTaRepository.currentStudent.fullName,
                        fatherName = "NFS",
                        certificateNumber = dl.certificateNumber,
                        issuingAuthority = dl.issuingAuthority,
                        issueDate = dl.uploadedAt,
                        validity = "Verified via DigiLocker Sandbox*",
                        isExpired = false,
                        confidenceScore = dl.confidenceScore,
                        autoApproveEligible = dl.confidenceScore >= 85,
                        sharedHostingUrl = dl.sharedHostingImageUrl,
                        syncedToFirebase = true
                    )
                }
                MoTaRepository.scannedDocuments.clear()
                MoTaRepository.scannedDocuments.addAll(scannedDocs)
            } else {
                firestoreDocuments.clear()
                MoTaRepository.scannedDocuments.clear()
            }
        }

        // 6. DYNAMIC PROFILE SYNC (Firestore Document: "users/{userId}/profile/info")
        val profileDocRef = firestore.collection("users").document(userId).collection("profile").document("info")
        profileDocRef.addSnapshotListener { doc, error ->
            if (error != null || doc == null || !doc.exists()) {
                return@addSnapshotListener
            }
            try {
                val encIncome = doc.get("annualIncome")?.toString() ?: "0"
                val encAccount = doc.getString("maskedAccount") ?: "NFS"
                val encIfsc = doc.getString("ifsc") ?: "NFS"
                val encAadhaar = doc.getString("aadhaarLast4") ?: "NFS"

                MoTaRepository.currentStudent = MoTaRepository.currentStudent.copy(
                    fullName = doc.getString("fullName") ?: MoTaRepository.currentStudent.fullName,
                    email = doc.getString("email") ?: MoTaRepository.currentStudent.email,
                    photoUrl = doc.getString("photoUrl") ?: MoTaRepository.currentStudent.photoUrl,
                    institution = doc.getString("institution") ?: MoTaRepository.currentStudent.institution,
                    educationLevel = doc.getString("educationLevel") ?: MoTaRepository.currentStudent.educationLevel,
                    community = doc.getString("community") ?: MoTaRepository.currentStudent.community,
                    subTribe = doc.getString("subTribe") ?: MoTaRepository.currentStudent.subTribe,
                    annualIncome = com.step.app.security.CryptoManager.decrypt(encIncome, userId).toLongOrNull() ?: 0L,
                    bankName = doc.getString("bankName") ?: MoTaRepository.currentStudent.bankName,
                    maskedAccount = com.step.app.security.CryptoManager.decrypt(encAccount, userId).ifEmpty { "NFS" },
                    ifsc = com.step.app.security.CryptoManager.decrypt(encIfsc, userId).ifEmpty { "NFS" },
                    aadhaarLast4 = com.step.app.security.CryptoManager.decrypt(encAadhaar, userId).ifEmpty { "NFS" },
                    state = doc.getString("state") ?: MoTaRepository.currentStudent.state,
                    npciAadhaarSeeded = doc.getBoolean("npciAadhaarSeeded") ?: false
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse profile from Firestore", e)
            }
        }
    }

    // --- WRITE OPERATIONS TO FIREBASE FIRESTORE ---

    fun saveStudentProfileToFirestore(
        profile: StudentProfile,
        userId: String = currentUser?.uid ?: "usr_guest",
        context: android.content.Context? = null
    ) {
        val normalizedEmailUid = "usr_" + profile.email.lowercase().trim().replace(Regex("[^a-zA-Z0-9]"), "_")
        val data = hashMapOf(
            "uid" to userId,
            "apaarId" to profile.apaarId,
            "digilockerId" to profile.digilockerId,
            "fullName" to profile.fullName,
            "email" to profile.email,
            "photoUrl" to profile.photoUrl,
            "community" to profile.community,
            "subTribe" to profile.subTribe,
            "institution" to profile.institution,
            "educationLevel" to profile.educationLevel,
            "annualIncome" to com.step.app.security.CryptoManager.encrypt(profile.annualIncome.toString(), userId),
            "bankName" to profile.bankName,
            "maskedAccount" to com.step.app.security.CryptoManager.encrypt(profile.maskedAccount, userId),
            "ifsc" to com.step.app.security.CryptoManager.encrypt(profile.ifsc, userId),
            "aadhaarLast4" to com.step.app.security.CryptoManager.encrypt(profile.aadhaarLast4, userId),
            "state" to profile.state,
            "npciAadhaarSeeded" to profile.npciAadhaarSeeded,
            "encryption" to "AES-256-GCM",
            "lastSyncedAt" to System.currentTimeMillis()
        )
        // 1. Save to primary document
        firestore.collection("users").document(userId)
            .collection("profile").document("info")
            .set(data, SetOptions.merge())

        // 2. Also save to normalized email document so returning users are always found
        if (userId != normalizedEmailUid && profile.email.isNotBlank()) {
            firestore.collection("users").document(normalizedEmailUid)
                .collection("profile").document("info")
                .set(data, SetOptions.merge())
        }

        // 3. Save to local SharedPreferences
        if (context != null && profile.email.isNotBlank()) {
            try {
                val prefs = context.getSharedPreferences("step_user_prefs", android.content.Context.MODE_PRIVATE)
                prefs.edit()
                    .putBoolean("reg_${profile.email.lowercase().trim()}", true)
                    .putString("uid_${profile.email.lowercase().trim()}", userId)
                    .putString("name_${profile.email.lowercase().trim()}", profile.fullName)
                    .putString("subTribe_${profile.email.lowercase().trim()}", profile.subTribe)
                    .putString("school_${profile.email.lowercase().trim()}", profile.institution)
                    .putString("state_${profile.email.lowercase().trim()}", profile.state)
                    .putString("photo_${profile.email.lowercase().trim()}", profile.photoUrl)
                    .apply()
            } catch (e: Exception) {
                Log.w(TAG, "Failed to save local preferences", e)
            }
        }
    }

    fun submitApplicationToFirestore(app: ApplicationRecord, userId: String = currentUser?.uid ?: "usr_guest") {
        val stepsData = app.steps.map { step ->
            hashMapOf(
                "label" to step.label,
                "date" to step.date,
                "completed" to step.completed,
                "note" to step.note
            )
        }

        val dbtData = app.dbtDetails?.let {
            hashMapOf(
                "utr" to it.utr,
                "paymentMode" to it.paymentMode,
                "disbursedDate" to it.disbursedDate,
                "bankName" to it.bankName,
                "accountNo" to it.accountNo,
                "centralShare" to it.centralShare,
                "stateShare" to it.stateShare,
                "status" to it.status
            )
        }

        val defData = app.deficiency?.let {
            hashMapOf(
                "code" to it.code,
                "bureaucraticReason" to it.bureaucraticReason,
                "deadlineDate" to it.deadlineDate,
                "daysRemaining" to it.daysRemaining
            )
        }

        val appMap = hashMapOf(
            "applicationId" to app.applicationId,
            "schemeId" to app.schemeId,
            "schemeTitle" to app.schemeTitle,
            "academicYear" to app.academicYear,
            "sourcePortal" to app.sourcePortal,
            "stage" to app.stage,
            "stageText" to app.stageText,
            "currentStepIndex" to app.currentStepIndex,
            "sanctionAmount" to app.sanctionAmount,
            "nextActionText" to app.nextActionText,
            "verificationConfidence" to app.verificationConfidence,
            "steps" to stepsData,
            "dbtDetails" to dbtData,
            "deficiency" to defData,
            "createdAt" to System.currentTimeMillis()
        )

        firestore.collection("users").document(userId)
            .collection("applications").document(app.applicationId)
            .set(appMap, SetOptions.merge())
            .addOnSuccessListener {
                Log.d(TAG, "Application ${app.applicationId} successfully written to Firebase Firestore!")
            }
    }

    suspend fun saveDocumentToFirestore(
        doc: ScannedDocument,
        sharedHostingUrl: String,
        userId: String = currentUser?.uid ?: "usr_guest"
    ) {
        val docId = doc.id.ifEmpty { "doc_" + System.currentTimeMillis().toString().takeLast(6) }
        val docMap = hashMapOf(
            "docId" to docId,
            "docType" to doc.documentType,
            "certificateNumber" to com.step.app.security.CryptoManager.encrypt(doc.certificateNumber, userId),
            "issuingAuthority" to doc.issuingAuthority,
            "confidenceScore" to doc.confidenceScore,
            "sharedHostingImageUrl" to sharedHostingUrl,
            "encryption" to "AES-256-GCM",
            "uploadedAt" to "Just now",
            "timestamp" to System.currentTimeMillis()
        )

        firestore.collection("users").document(userId)
            .collection("documents").document(docId)
            .set(docMap, SetOptions.merge())

        val newRecord = FirestoreDocumentLink(
            docId = docId,
            docType = doc.documentType,
            certificateNumber = doc.certificateNumber,
            issuingAuthority = doc.issuingAuthority,
            confidenceScore = doc.confidenceScore,
            sharedHostingImageUrl = sharedHostingUrl,
            uploadedAt = "Just now"
        )
        firestoreDocuments.add(0, newRecord)
    }

    fun resolvePendingActionInFirestore(
        actionId: String,
        userId: String = currentUser?.uid ?: "usr_guest"
    ) {
        firestore.collection("users").document(userId)
            .collection("pending_actions").document(actionId)
            .delete()
    }

    // --- SEED INITIAL DATA TO CLOUD FIRESTORE IF EMPTY ---

    private fun seedSchemesToFirestore() {
        Log.d(TAG, "Seeding schemes to Firebase Firestore...")
        MoTaDefaults.schemes.forEach { scheme ->
            val data = hashMapOf(
                "id" to scheme.id,
                "code" to scheme.code,
                "title" to scheme.title,
                "hindiTitle" to scheme.hindiTitle,
                "portal" to scheme.portal,
                "targetClass" to scheme.targetClass,
                "incomeCeiling" to scheme.incomeCeiling,
                "benefitSummary" to scheme.benefitSummary,
                "maxBenefitAmount" to scheme.maxBenefitAmount,
                "benefitAmountFormatted" to scheme.benefitAmountFormatted,
                "deadlineFormatted" to scheme.deadlineFormatted,
                "eligibilityTag" to scheme.eligibilityTag,
                "description" to scheme.description,
                "documentsNeeded" to scheme.documentsNeeded,
                "rules" to scheme.rules
            )
            firestore.collection("schemes").document(scheme.id).set(data, SetOptions.merge())
        }
    }





    private fun parseProfileFromDoc(doc: com.google.firebase.firestore.DocumentSnapshot, uid: String, emailFallback: String): StudentProfile {
        val encIncome = doc.get("annualIncome")?.toString() ?: "0"
        val encAccount = doc.getString("maskedAccount") ?: "NFS"
        val encIfsc = doc.getString("ifsc") ?: "NFS"
        val encAadhaar = doc.getString("aadhaarLast4") ?: "NFS"

        return StudentProfile(
            uid = uid,
            apaarId = doc.getString("apaarId") ?: "NFS",
            digilockerId = doc.getString("digilockerId") ?: "NFS",
            fullName = doc.getString("fullName") ?: "NFS",
            email = doc.getString("email") ?: emailFallback.ifEmpty { "NFS" },
            photoUrl = doc.getString("photoUrl") ?: "",
            community = doc.getString("community") ?: "NFS",
            subTribe = doc.getString("subTribe") ?: "NFS",
            institution = doc.getString("institution") ?: "NFS",
            educationLevel = doc.getString("educationLevel") ?: "NFS",
            annualIncome = com.step.app.security.CryptoManager.decrypt(encIncome, uid).toLongOrNull() ?: 0L,
            bankName = doc.getString("bankName") ?: "NFS",
            maskedAccount = com.step.app.security.CryptoManager.decrypt(encAccount, uid).ifEmpty { "NFS" },
            ifsc = com.step.app.security.CryptoManager.decrypt(encIfsc, uid).ifEmpty { "NFS" },
            aadhaarLast4 = com.step.app.security.CryptoManager.decrypt(encAadhaar, uid).ifEmpty { "NFS" },
            state = doc.getString("state") ?: "NFS",
            npciAadhaarSeeded = doc.getBoolean("npciAadhaarSeeded") ?: false
        )
    }

    private fun saveLocalPrefs(context: android.content.Context, profile: StudentProfile) {
        try {
            val prefs = context.getSharedPreferences("step_user_prefs", android.content.Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean("reg_${profile.email.lowercase().trim()}", true)
                .putString("uid_${profile.email.lowercase().trim()}", profile.uid)
                .putString("name_${profile.email.lowercase().trim()}", profile.fullName)
                .putString("subTribe_${profile.email.lowercase().trim()}", profile.subTribe)
                .putString("school_${profile.email.lowercase().trim()}", profile.institution)
                .putString("state_${profile.email.lowercase().trim()}", profile.state)
                .putString("photo_${profile.email.lowercase().trim()}", profile.photoUrl)
                .apply()
        } catch (_: Exception) {}
    }

    fun checkExistingProfile(
        context: android.content.Context? = null,
        uid: String,
        email: String = "",
        onExisting: (StudentProfile) -> Unit,
        onNewUser: () -> Unit
    ) {
        val normalizedEmailUid = if (email.isNotBlank()) "usr_" + email.lowercase().trim().replace(Regex("[^a-zA-Z0-9]"), "_") else uid

        // 1. Instant check in local SharedPreferences for fast, offline-safe returning user recognition
        if (context != null && email.isNotBlank()) {
            try {
                val prefs = context.getSharedPreferences("step_user_prefs", android.content.Context.MODE_PRIVATE)
                val isReg = prefs.getBoolean("reg_${email.lowercase().trim()}", false)
                if (isReg) {
                    val cachedName = prefs.getString("name_${email.lowercase().trim()}", "") ?: ""
                    if (cachedName.isNotBlank()) {
                        val cachedUid = prefs.getString("uid_${email.lowercase().trim()}", uid) ?: uid
                        val cached = MoTaRepository.currentStudent.copy(
                            uid = cachedUid,
                            fullName = cachedName,
                            email = email,
                            subTribe = prefs.getString("subTribe_${email.lowercase().trim()}", "NFS") ?: "NFS",
                            institution = prefs.getString("school_${email.lowercase().trim()}", "NFS") ?: "NFS",
                            state = prefs.getString("state_${email.lowercase().trim()}", "NFS") ?: "NFS",
                            photoUrl = prefs.getString("photo_${email.lowercase().trim()}", "") ?: ""
                        )
                        MoTaRepository.currentStudent = cached
                        initDynamicFirestore(cachedUid)
                        isGoogleLoggedIn = true
                        onExisting(cached)
                        return
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Local cache lookup exception", e)
            }
        }

        // 2. Query Firestore primary UID
        firestore.collection("users").document(uid).collection("profile").document("info")
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val p = parseProfileFromDoc(doc, uid, email)
                    MoTaRepository.currentStudent = p
                    initDynamicFirestore(uid)
                    isGoogleLoggedIn = true
                    if (context != null) saveLocalPrefs(context, p)
                    onExisting(p)
                } else {
                    // 3. Query Firestore normalized email UID
                    firestore.collection("users").document(normalizedEmailUid).collection("profile").document("info")
                        .get()
                        .addOnSuccessListener { doc2 ->
                            if (doc2 != null && doc2.exists()) {
                                val p2 = parseProfileFromDoc(doc2, normalizedEmailUid, email)
                                MoTaRepository.currentStudent = p2
                                initDynamicFirestore(normalizedEmailUid)
                                isGoogleLoggedIn = true
                                if (context != null) saveLocalPrefs(context, p2)
                                onExisting(p2)
                            } else if (email.isNotBlank()) {
                                // 4. Query profile collectionGroup by email
                                firestore.collectionGroup("profile").whereEqualTo("email", email).limit(1)
                                    .get()
                                    .addOnSuccessListener { snap ->
                                        if (snap != null && !snap.isEmpty) {
                                            val doc3 = snap.documents.first()
                                            val foundUid = doc3.getString("uid") ?: normalizedEmailUid
                                            val p3 = parseProfileFromDoc(doc3, foundUid, email)
                                            MoTaRepository.currentStudent = p3
                                            initDynamicFirestore(foundUid)
                                            isGoogleLoggedIn = true
                                            if (context != null) saveLocalPrefs(context, p3)
                                            onExisting(p3)
                                        } else {
                                            onNewUser()
                                        }
                                    }
                                    .addOnFailureListener { onNewUser() }
                            } else {
                                onNewUser()
                            }
                        }
                        .addOnFailureListener { onNewUser() }
                }
            }
            .addOnFailureListener {
                onNewUser()
            }
    }

    fun completeRegistration(profile: StudentProfile, context: android.content.Context? = null) {
        MoTaRepository.currentStudent = profile
        saveStudentProfileToFirestore(profile, profile.uid, context)
        initDynamicFirestore(profile.uid)
        isGoogleLoggedIn = true
    }

    fun loginWithGoogleAccount(
        context: android.content.Context? = null,
        account: GoogleSignInAccount,
        onReady: (isNewUser: Boolean) -> Unit
    ) {
        val name = account.displayName ?: account.givenName ?: "NFS"
        val email = account.email ?: "NFS"
        val photo = account.photoUrl?.toString().orEmpty()
        val idToken = account.idToken.orEmpty()
        val normalizedEmailUid = "usr_" + email.lowercase().trim().replace(Regex("[^a-zA-Z0-9]"), "_")
        val uid = account.id ?: normalizedEmailUid

        val user = GoogleUser(
            uid = uid,
            displayName = name,
            email = email,
            photoUrl = photo,
            idToken = idToken
        )
        currentUser = user

        checkExistingProfile(
            context = context,
            uid = uid,
            email = email,
            onExisting = {
                onReady(false)
            },
            onNewUser = {
                MoTaRepository.currentStudent = MoTaRepository.currentStudent.copy(
                    fullName = name,
                    email = email,
                    photoUrl = photo,
                    uid = uid
                )
                onReady(true)
            }
        )
    }

    fun loginWithGoogle(
        name: String = "NFS",
        email: String = "NFS",
        onReady: (isNewUser: Boolean) -> Unit = {}
    ) {
        val uid = "usr_google_" + System.currentTimeMillis().toString().takeLast(6)
        val user = GoogleUser(
            uid = uid,
            displayName = name,
            email = email,
            photoUrl = ""
        )
        currentUser = user

        checkExistingProfile(
            uid = uid,
            onExisting = {
                onReady(false)
            },
            onNewUser = {
                MoTaRepository.currentStudent = MoTaRepository.currentStudent.copy(
                    fullName = name,
                    email = email,
                    uid = uid
                )
                onReady(true)
            }
        )
    }

    fun logout() {
        currentUser = null
        isGoogleLoggedIn = false
        isSyncInitialized = false
        MoTaRepository.applications.clear()
        MoTaRepository.pendingActions.clear()
        MoTaRepository.notifications.clear()
    }
}
