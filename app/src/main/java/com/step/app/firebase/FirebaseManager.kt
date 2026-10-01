package com.step.app.firebase

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.step.app.data.*
import org.json.JSONArray
import org.json.JSONObject

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
    val candidateName: String = "",
    val fatherName: String = "",
    val issuingAuthority: String = "",
    val issueDate: String = "",
    val validity: String = "",
    val confidenceScore: Int = 100,
    val sharedHostingImageUrl: String = "",
    val casteCommunity: String? = null,
    val annualIncome: String? = null,
    val digilockerXml: String = "",
    val signerCn: String = "",
    val dscSerialNumber: String = "",
    val pkiTimestamp: String = "",
    val firestoreCollection: String = "users/guest/documents",
    val uploadedAt: String = "28-Sep-2026, 17:30 IST"
)

object FirebaseManager {
    private const val TAG = "FirebaseManager"

    // Web Client ID configured in Google Cloud Console / Firebase Authentication
    const val WEB_CLIENT_ID = "948854630900-qd0d4mats059a13d9len9qi0rr3p5c6m.apps.googleusercontent.com"

    private val firestore by lazy { FirebaseFirestore.getInstance() }

    // Current authenticated Google user session
    var currentUser by mutableStateOf<GoogleUser?>(null)

    var isGoogleLoggedIn by mutableStateOf(false)

    var appContext: android.content.Context? = null

    // Stored in Firebase Firestore: metadata + Shared Hosting URLs
    val firestoreDocuments = mutableStateListOf<FirestoreDocumentLink>()

    private var isSchemesSyncInitialized = false
    private var currentActiveSyncUserId: String? = null
    private var appsListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var actionsListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var notifsListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var docsListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var profileListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    /**
     * Initializes bidirectional dynamic Firestore synchronization.
     * All changes in Firebase Firestore sync to the app in real time,
     * and app actions save directly to Firebase.
     */
    fun initDynamicFirestore(userId: String = currentUser?.uid ?: "usr_guest") {
        // 1. DYNAMIC SCHEMES SYNC (Firestore Collection: "schemes" - Global)
        if (!isSchemesSyncInitialized) {
            isSchemesSyncInitialized = true
            ensureSchemesSeededToFirestore()

            firestore.collection("schemes")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.e(TAG, "Error listening to schemes in Firestore", error)
                        return@addSnapshotListener
                    }

                    if (snapshot == null || snapshot.isEmpty) {
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
        }

        if (currentActiveSyncUserId == userId) return
        currentActiveSyncUserId = userId

        // Detach prior user-scoped listeners if switching accounts
        appsListenerRegistration?.remove()
        actionsListenerRegistration?.remove()
        notifsListenerRegistration?.remove()
        docsListenerRegistration?.remove()
        profileListenerRegistration?.remove()

        Log.d(TAG, "Initializing dynamic Firestore real-time synchronization for user: $userId")

        // 2. DYNAMIC APPLICATIONS SYNC (Firestore Collection: "users/{userId}/applications")
        val userAppsRef = firestore.collection("users").document(userId).collection("applications")
        appsListenerRegistration = userAppsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to applications in Firestore", error)
                return@addSnapshotListener
            }

            if (snapshot == null || snapshot.isEmpty) {
                MoTaRepository.applications.clear()
            } else {
                val remoteApps = snapshot.documents.mapNotNull { doc ->
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
                appContext?.let { ctx -> saveCachedApplications(ctx, userId, remoteApps) }
                Log.d(TAG, "Loaded ${remoteApps.size} dynamic applications from Firebase Firestore")
            }
        }

        // 3. DYNAMIC PENDING ACTIONS SYNC (Firestore Collection: "users/{userId}/pending_actions")
        val userActionsRef = firestore.collection("users").document(userId).collection("pending_actions")
        actionsListenerRegistration = userActionsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to pending actions in Firestore", error)
                return@addSnapshotListener
            }

            if (snapshot == null || snapshot.isEmpty) {
                MoTaRepository.pendingActions.clear()
            } else {
                val remoteActions = snapshot.documents.mapNotNull { doc ->
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
        notifsListenerRegistration = userNotifsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to notifications in Firestore", error)
                return@addSnapshotListener
            }

            if (snapshot == null || snapshot.isEmpty) {
                MoTaRepository.notifications.clear()
            } else {
                val remoteNotifs = snapshot.documents.mapNotNull { doc ->
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
        docsListenerRegistration = userDocsRef.addSnapshotListener { snapshot, error ->
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
                            candidateName = doc.getString("candidateName") ?: MoTaRepository.currentStudent.fullName,
                            fatherName = doc.getString("fatherName") ?: "",
                            issuingAuthority = doc.getString("issuingAuthority") ?: "",
                            issueDate = doc.getString("issueDate") ?: doc.getString("uploadedAt") ?: "Permanent",
                            validity = doc.getString("validity") ?: "Permanent",
                            confidenceScore = (doc.getLong("confidenceScore") ?: 100L).toInt(),
                            sharedHostingImageUrl = doc.getString("sharedHostingImageUrl") ?: "",
                            casteCommunity = doc.getString("casteCommunity"),
                            annualIncome = doc.getString("annualIncome"),
                            digilockerXml = doc.getString("digilockerXml") ?: "",
                            signerCn = doc.getString("signerCn") ?: "",
                            dscSerialNumber = doc.getString("dscSerialNumber") ?: "",
                            pkiTimestamp = doc.getString("pkiTimestamp") ?: "",
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
                        candidateName = dl.candidateName.ifBlank { MoTaRepository.currentStudent.fullName },
                        fatherName = dl.fatherName,
                        certificateNumber = dl.certificateNumber,
                        issuingAuthority = dl.issuingAuthority,
                        issueDate = dl.issueDate,
                        validity = dl.validity,
                        isExpired = false,
                        casteCommunity = dl.casteCommunity,
                        annualIncome = dl.annualIncome,
                        confidenceScore = dl.confidenceScore,
                        autoApproveEligible = dl.confidenceScore >= 85,
                        sharedHostingUrl = dl.sharedHostingImageUrl,
                        syncedToFirebase = true,
                        digilockerXml = dl.digilockerXml,
                        signerCn = dl.signerCn,
                        dscSerialNumber = dl.dscSerialNumber,
                        pkiTimestamp = dl.pkiTimestamp
                    )
                }
                MoTaRepository.scannedDocuments.clear()
                MoTaRepository.scannedDocuments.addAll(scannedDocs)
                appContext?.let { ctx -> saveCachedDocuments(ctx, userId, scannedDocs) }
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
                val encAccount = doc.getString("maskedAccount") ?: "NAS"
                val encIfsc = doc.getString("ifsc") ?: "NAS"
                val encAadhaar = doc.getString("aadhaarLast4") ?: "NAS"

                MoTaRepository.currentStudent = MoTaRepository.currentStudent.copy(
                    apaarId = doc.getString("apaarId") ?: MoTaRepository.currentStudent.apaarId,
                    digilockerId = doc.getString("digilockerId") ?: MoTaRepository.currentStudent.digilockerId,
                    fullName = doc.getString("fullName") ?: MoTaRepository.currentStudent.fullName,
                    email = doc.getString("email") ?: MoTaRepository.currentStudent.email,
                    photoUrl = doc.getString("photoUrl") ?: MoTaRepository.currentStudent.photoUrl,
                    institution = doc.getString("institution") ?: MoTaRepository.currentStudent.institution,
                    educationLevel = doc.getString("educationLevel") ?: MoTaRepository.currentStudent.educationLevel,
                    community = doc.getString("community") ?: MoTaRepository.currentStudent.community,
                    subTribe = doc.getString("subTribe") ?: MoTaRepository.currentStudent.subTribe,
                    annualIncome = com.step.app.security.CryptoManager.decrypt(encIncome, userId).toLongOrNull() ?: 0L,
                    bankName = doc.getString("bankName") ?: MoTaRepository.currentStudent.bankName,
                    maskedAccount = com.step.app.security.CryptoManager.decrypt(encAccount, userId).ifEmpty { "NAS" },
                    ifsc = com.step.app.security.CryptoManager.decrypt(encIfsc, userId).ifEmpty { "NAS" },
                    aadhaarLast4 = com.step.app.security.CryptoManager.decrypt(encAadhaar, userId).ifEmpty { "NAS" },
                    state = doc.getString("state") ?: MoTaRepository.currentStudent.state,
                    npciAadhaarSeeded = doc.getBoolean("npciAadhaarSeeded") ?: false
                )
                appContext?.let { ctx -> saveLocalProfile(ctx, MoTaRepository.currentStudent) }
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
        val ctx = context ?: appContext
        if (ctx != null) {
            saveLocalProfile(ctx, profile)
        }

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
        // 1. Save to primary user profile document
        firestore.collection("users").document(userId)
            .collection("profile").document("info")
            .set(data, SetOptions.merge())

        // 2. Save directly to top-level students collection for Admin / MoTA Officer portals
        firestore.collection("students").document(userId)
            .set(data, SetOptions.merge())

        // 3. Also save to normalized email document so returning users are always found
        if (userId != normalizedEmailUid && profile.email.isNotBlank()) {
            firestore.collection("users").document(normalizedEmailUid)
                .collection("profile").document("info")
                .set(data, SetOptions.merge())
            firestore.collection("students").document(normalizedEmailUid)
                .set(data, SetOptions.merge())
        }
    }

    fun submitApplicationToFirestore(app: ApplicationRecord, userId: String = currentUser?.uid ?: "usr_guest") {
        MoTaRepository.applications.removeAll { it.applicationId == app.applicationId }
        MoTaRepository.applications.add(0, app)
        appContext?.let { saveCachedApplications(it, userId, MoTaRepository.applications) }

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
            "studentName" to MoTaRepository.currentStudent.fullName,
            "candidateName" to MoTaRepository.currentStudent.fullName,
            "district" to "${MoTaRepository.currentStudent.subTribe}, ${MoTaRepository.currentStudent.state}",
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
        sharedHostingUrl: String = "",
        userId: String = currentUser?.uid ?: "usr_guest"
    ) {
        val docId = doc.id.ifEmpty { "doc_" + System.currentTimeMillis().toString().takeLast(6) }
        val docMap = hashMapOf(
            "docId" to docId,
            "docType" to doc.documentType,
            "certificateNumber" to com.step.app.security.CryptoManager.encrypt(doc.certificateNumber, userId),
            "candidateName" to doc.candidateName,
            "fatherName" to doc.fatherName,
            "issuingAuthority" to doc.issuingAuthority,
            "issueDate" to doc.issueDate,
            "validity" to doc.validity,
            "confidenceScore" to doc.confidenceScore,
            "sharedHostingImageUrl" to sharedHostingUrl,
            "casteCommunity" to (doc.casteCommunity ?: ""),
            "annualIncome" to (doc.annualIncome ?: ""),
            "rollNumber" to (doc.rollNumber.ifBlank { doc.certificateNumber }),
            "passingYear" to doc.passingYear,
            "boardName" to doc.boardName,
            "marksPercentage" to doc.marksPercentage,
            "marksObtained" to doc.marksObtained,
            "maxMarks" to doc.maxMarks,
            "isDigiLocker" to true,
            "digilockerXml" to doc.digilockerXml,
            "signerCn" to doc.signerCn,
            "dscSerialNumber" to doc.dscSerialNumber,
            "pkiTimestamp" to doc.pkiTimestamp,
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
            candidateName = doc.candidateName,
            fatherName = doc.fatherName,
            issuingAuthority = doc.issuingAuthority,
            issueDate = doc.issueDate,
            validity = doc.validity,
            confidenceScore = doc.confidenceScore,
            sharedHostingImageUrl = sharedHostingUrl,
            casteCommunity = doc.casteCommunity,
            annualIncome = doc.annualIncome,
            digilockerXml = doc.digilockerXml,
            signerCn = doc.signerCn,
            dscSerialNumber = doc.dscSerialNumber,
            pkiTimestamp = doc.pkiTimestamp,
            uploadedAt = "Just now"
        )
        firestoreDocuments.removeAll { it.docId == docId }
        firestoreDocuments.add(0, newRecord)

        val updatedDoc = doc.copy(id = docId, sharedHostingUrl = sharedHostingUrl)
        MoTaRepository.scannedDocuments.removeAll { it.id == docId }
        MoTaRepository.scannedDocuments.add(0, updatedDoc)
        appContext?.let { saveCachedDocuments(it, userId, MoTaRepository.scannedDocuments) }
    }

    fun resolvePendingActionInFirestore(
        actionId: String,
        userId: String = currentUser?.uid ?: "usr_guest"
    ) {
        firestore.collection("users").document(userId)
            .collection("pending_actions").document(actionId)
            .delete()
        MoTaRepository.pendingActions.removeAll { it.id == actionId }
    }

    // --- SEED INITIAL DATA TO CLOUD FIRESTORE IF EMPTY ---

    fun ensureSchemesSeededToFirestore() {
        seedSchemesToFirestore()
    }

    fun seedSchemesToFirestore() {
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
        val encAccount = doc.getString("maskedAccount") ?: "NAS"
        val encIfsc = doc.getString("ifsc") ?: "NAS"
        val encAadhaar = doc.getString("aadhaarLast4") ?: "NAS"

        return StudentProfile(
            uid = uid,
            apaarId = doc.getString("apaarId") ?: "NAS",
            digilockerId = doc.getString("digilockerId") ?: "NAS",
            fullName = doc.getString("fullName") ?: "NAS",
            email = doc.getString("email") ?: emailFallback.ifEmpty { "NAS" },
            photoUrl = doc.getString("photoUrl") ?: "",
            community = doc.getString("community") ?: "NAS",
            subTribe = doc.getString("subTribe") ?: "NAS",
            institution = doc.getString("institution") ?: "NAS",
            educationLevel = doc.getString("educationLevel") ?: "NAS",
            annualIncome = com.step.app.security.CryptoManager.decrypt(encIncome, uid).toLongOrNull() ?: 0L,
            bankName = doc.getString("bankName") ?: "NAS",
            maskedAccount = com.step.app.security.CryptoManager.decrypt(encAccount, uid).ifEmpty { "NAS" },
            ifsc = com.step.app.security.CryptoManager.decrypt(encIfsc, uid).ifEmpty { "NAS" },
            aadhaarLast4 = com.step.app.security.CryptoManager.decrypt(encAadhaar, uid).ifEmpty { "NAS" },
            state = doc.getString("state") ?: "NAS",
            npciAadhaarSeeded = doc.getBoolean("npciAadhaarSeeded") ?: false
        )
    }

    fun saveLocalProfile(context: android.content.Context, profile: StudentProfile) {
        try {
            val prefs = context.getSharedPreferences("step_user_prefs", android.content.Context.MODE_PRIVATE)
            val editor = prefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("last_active_uid", profile.uid)
                .putString("last_active_email", profile.email)
                .putString("last_active_name", profile.fullName)
                .putString("last_active_photo", profile.photoUrl)

            fun writeProfile(prefix: String) {
                editor.putBoolean("reg_$prefix", true)
                    .putString("uid_$prefix", profile.uid)
                    .putString("name_$prefix", profile.fullName)
                    .putString("email_$prefix", profile.email)
                    .putString("photo_$prefix", profile.photoUrl)
                    .putString("apaarId_$prefix", profile.apaarId)
                    .putString("digilockerId_$prefix", profile.digilockerId)
                    .putString("community_$prefix", profile.community)
                    .putString("subTribe_$prefix", profile.subTribe)
                    .putString("school_$prefix", profile.institution)
                    .putString("educationLevel_$prefix", profile.educationLevel)
                    .putLong("annualIncome_$prefix", profile.annualIncome)
                    .putString("bankName_$prefix", profile.bankName)
                    .putString("maskedAccount_$prefix", profile.maskedAccount)
                    .putString("ifsc_$prefix", profile.ifsc)
                    .putString("aadhaarLast4_$prefix", profile.aadhaarLast4)
                    .putString("state_$prefix", profile.state)
                    .putBoolean("npciAadhaarSeeded_$prefix", profile.npciAadhaarSeeded)
            }

            val emailKey = profile.email.lowercase().trim()
            if (emailKey.isNotBlank()) writeProfile(emailKey)
            if (profile.uid.isNotBlank()) writeProfile(profile.uid)
            editor.apply()
            Log.d(TAG, "Saved profile locally for ${profile.fullName} (${profile.email})")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save local profile", e)
        }
    }

    fun getLocalProfile(context: android.content.Context, key: String): StudentProfile? {
        return try {
            val prefs = context.getSharedPreferences("step_user_prefs", android.content.Context.MODE_PRIVATE)
            val cleanKey = key.lowercase().trim()
            val isReg = prefs.getBoolean("reg_$cleanKey", false)
            if (!isReg && !prefs.contains("name_$cleanKey") && !prefs.contains("uid_$cleanKey")) return null

            StudentProfile(
                uid = prefs.getString("uid_$cleanKey", cleanKey) ?: cleanKey,
                apaarId = prefs.getString("apaarId_$cleanKey", "NAS") ?: "NAS",
                digilockerId = prefs.getString("digilockerId_$cleanKey", "NAS") ?: "NAS",
                fullName = prefs.getString("name_$cleanKey", "NAS") ?: "NAS",
                email = prefs.getString("email_$cleanKey", if (cleanKey.contains("@")) cleanKey else "NAS") ?: "NAS",
                photoUrl = prefs.getString("photo_$cleanKey", "") ?: "",
                community = prefs.getString("community_$cleanKey", "NAS") ?: "NAS",
                subTribe = prefs.getString("subTribe_$cleanKey", "NAS") ?: "NAS",
                institution = prefs.getString("school_$cleanKey", "NAS") ?: "NAS",
                educationLevel = prefs.getString("educationLevel_$cleanKey", "NAS") ?: "NAS",
                annualIncome = prefs.getLong("annualIncome_$cleanKey", 0L),
                bankName = prefs.getString("bankName_$cleanKey", "NAS") ?: "NAS",
                maskedAccount = prefs.getString("maskedAccount_$cleanKey", "NAS") ?: "NAS",
                ifsc = prefs.getString("ifsc_$cleanKey", "NAS") ?: "NAS",
                aadhaarLast4 = prefs.getString("aadhaarLast4_$cleanKey", "NAS") ?: "NAS",
                state = prefs.getString("state_$cleanKey", "NAS") ?: "NAS",
                npciAadhaarSeeded = prefs.getBoolean("npciAadhaarSeeded_$cleanKey", false)
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load cached profile", e)
            null
        }
    }

    fun saveCachedDocuments(context: android.content.Context, userId: String, docs: List<ScannedDocument>) {
        try {
            val array = JSONArray()
            docs.forEach { doc ->
                val obj = JSONObject().apply {
                    put("id", doc.id)
                    put("documentType", doc.documentType)
                    put("candidateName", doc.candidateName)
                    put("fatherName", doc.fatherName)
                    put("certificateNumber", doc.certificateNumber)
                    put("issuingAuthority", doc.issuingAuthority)
                    put("issueDate", doc.issueDate)
                    put("validity", doc.validity)
                    put("isExpired", doc.isExpired)
                    put("casteCommunity", doc.casteCommunity ?: "")
                    put("annualIncome", doc.annualIncome ?: "")
                    put("confidenceScore", doc.confidenceScore)
                    put("autoApproveEligible", doc.autoApproveEligible)
                    put("sharedHostingUrl", doc.sharedHostingUrl)
                    put("syncedToFirebase", doc.syncedToFirebase)
                    put("digilockerXml", doc.digilockerXml)
                    put("signerCn", doc.signerCn)
                    put("dscSerialNumber", doc.dscSerialNumber)
                    put("pkiTimestamp", doc.pkiTimestamp)
                }
                array.put(obj)
            }
            val prefs = context.getSharedPreferences("step_user_prefs", android.content.Context.MODE_PRIVATE)
            prefs.edit().putString("cached_docs_$userId", array.toString()).apply()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to cache documents", e)
        }
    }

    fun loadCachedDocuments(context: android.content.Context, userId: String): List<ScannedDocument> {
        return try {
            val prefs = context.getSharedPreferences("step_user_prefs", android.content.Context.MODE_PRIVATE)
            val json = prefs.getString("cached_docs_$userId", null) ?: return emptyList()
            val array = JSONArray(json)
            val list = mutableListOf<ScannedDocument>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    ScannedDocument(
                        id = obj.optString("id"),
                        documentType = obj.optString("documentType"),
                        candidateName = obj.optString("candidateName"),
                        fatherName = obj.optString("fatherName"),
                        certificateNumber = obj.optString("certificateNumber"),
                        issuingAuthority = obj.optString("issuingAuthority"),
                        issueDate = obj.optString("issueDate"),
                        validity = obj.optString("validity"),
                        isExpired = obj.optBoolean("isExpired", false),
                        casteCommunity = obj.optString("casteCommunity").ifEmpty { null },
                        annualIncome = obj.optString("annualIncome").ifEmpty { null },
                        confidenceScore = obj.optInt("confidenceScore", 100),
                        autoApproveEligible = obj.optBoolean("autoApproveEligible", true),
                        sharedHostingUrl = obj.optString("sharedHostingUrl"),
                        syncedToFirebase = obj.optBoolean("syncedToFirebase", true),
                        digilockerXml = obj.optString("digilockerXml"),
                        signerCn = obj.optString("signerCn"),
                        dscSerialNumber = obj.optString("dscSerialNumber"),
                        pkiTimestamp = obj.optString("pkiTimestamp")
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load cached documents", e)
            emptyList()
        }
    }

    fun saveCachedApplications(context: android.content.Context, userId: String, apps: List<ApplicationRecord>) {
        try {
            val array = JSONArray()
            apps.forEach { app ->
                val obj = JSONObject().apply {
                    put("applicationId", app.applicationId)
                    put("schemeId", app.schemeId)
                    put("schemeTitle", app.schemeTitle)
                    put("academicYear", app.academicYear)
                    put("sourcePortal", app.sourcePortal)
                    put("stage", app.stage)
                    put("stageText", app.stageText)
                    put("currentStepIndex", app.currentStepIndex)
                    put("sanctionAmount", app.sanctionAmount)
                    put("nextActionText", app.nextActionText)
                    put("verificationConfidence", app.verificationConfidence)

                    val stepsArr = JSONArray()
                    app.steps.forEach { step ->
                        val sObj = JSONObject().apply {
                            put("label", step.label)
                            put("date", step.date)
                            put("completed", step.completed)
                            put("note", step.note)
                        }
                        stepsArr.put(sObj)
                    }
                    put("steps", stepsArr)

                    if (app.dbtDetails != null) {
                        val dbtObj = JSONObject().apply {
                            put("utr", app.dbtDetails.utr)
                            put("paymentMode", app.dbtDetails.paymentMode)
                            put("disbursedDate", app.dbtDetails.disbursedDate)
                            put("bankName", app.dbtDetails.bankName)
                            put("accountNo", app.dbtDetails.accountNo)
                            put("centralShare", app.dbtDetails.centralShare)
                            put("stateShare", app.dbtDetails.stateShare)
                            put("status", app.dbtDetails.status)
                        }
                        put("dbtDetails", dbtObj)
                    }

                    if (app.deficiency != null) {
                        val defObj = JSONObject().apply {
                            put("code", app.deficiency.code)
                            put("bureaucraticReason", app.deficiency.bureaucraticReason)
                            put("deadlineDate", app.deficiency.deadlineDate)
                            put("daysRemaining", app.deficiency.daysRemaining)
                        }
                        put("deficiency", defObj)
                    }
                }
                array.put(obj)
            }
            val prefs = context.getSharedPreferences("step_user_prefs", android.content.Context.MODE_PRIVATE)
            prefs.edit().putString("cached_apps_$userId", array.toString()).apply()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to cache applications", e)
        }
    }

    fun loadCachedApplications(context: android.content.Context, userId: String): List<ApplicationRecord> {
        return try {
            val prefs = context.getSharedPreferences("step_user_prefs", android.content.Context.MODE_PRIVATE)
            val json = prefs.getString("cached_apps_$userId", null) ?: return emptyList()
            val array = JSONArray(json)
            val list = mutableListOf<ApplicationRecord>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val stepsList = mutableListOf<TimelineStep>()
                val stepsArr = obj.optJSONArray("steps")
                if (stepsArr != null) {
                    for (j in 0 until stepsArr.length()) {
                        val sObj = stepsArr.getJSONObject(j)
                        stepsList.add(
                            TimelineStep(
                                label = sObj.optString("label"),
                                date = sObj.optString("date"),
                                completed = sObj.optBoolean("completed"),
                                note = sObj.optString("note")
                            )
                        )
                    }
                }

                val dbtObj = obj.optJSONObject("dbtDetails")
                val dbtDetails = if (dbtObj != null) {
                    DbtDetails(
                        utr = dbtObj.optString("utr"),
                        paymentMode = dbtObj.optString("paymentMode"),
                        disbursedDate = dbtObj.optString("disbursedDate"),
                        bankName = dbtObj.optString("bankName"),
                        accountNo = dbtObj.optString("accountNo"),
                        centralShare = dbtObj.optString("centralShare"),
                        stateShare = dbtObj.optString("stateShare"),
                        status = dbtObj.optString("status")
                    )
                } else null

                val defObj = obj.optJSONObject("deficiency")
                val deficiency = if (defObj != null) {
                    DeficiencyInfo(
                        code = defObj.optString("code"),
                        bureaucraticReason = defObj.optString("bureaucraticReason"),
                        deadlineDate = defObj.optString("deadlineDate"),
                        daysRemaining = defObj.optInt("daysRemaining")
                    )
                } else null

                list.add(
                    ApplicationRecord(
                        applicationId = obj.optString("applicationId"),
                        schemeId = obj.optString("schemeId"),
                        schemeTitle = obj.optString("schemeTitle"),
                        academicYear = obj.optString("academicYear"),
                        sourcePortal = obj.optString("sourcePortal"),
                        stage = obj.optString("stage"),
                        stageText = obj.optString("stageText"),
                        currentStepIndex = obj.optInt("currentStepIndex"),
                        sanctionAmount = obj.optLong("sanctionAmount"),
                        nextActionText = obj.optString("nextActionText"),
                        verificationConfidence = obj.optInt("verificationConfidence", 90),
                        steps = stepsList,
                        dbtDetails = dbtDetails,
                        deficiency = deficiency
                    )
                )
            }
            list
        } catch (e: Exception) {
            Log.w(TAG, "Failed to load cached applications", e)
            emptyList()
        }
    }

    fun loadCachedDocumentsAndApplications(context: android.content.Context, userId: String) {
        if (MoTaRepository.scannedDocuments.isEmpty()) {
            val cachedDocs = loadCachedDocuments(context, userId)
            if (cachedDocs.isNotEmpty()) {
                MoTaRepository.scannedDocuments.addAll(cachedDocs)
                firestoreDocuments.clear()
                firestoreDocuments.addAll(cachedDocs.map { doc ->
                    FirestoreDocumentLink(
                        docId = doc.id,
                        docType = doc.documentType,
                        certificateNumber = doc.certificateNumber,
                        candidateName = doc.candidateName,
                        fatherName = doc.fatherName,
                        issuingAuthority = doc.issuingAuthority,
                        issueDate = doc.issueDate,
                        validity = doc.validity,
                        confidenceScore = doc.confidenceScore,
                        sharedHostingImageUrl = doc.sharedHostingUrl,
                        casteCommunity = doc.casteCommunity,
                        annualIncome = doc.annualIncome,
                        digilockerXml = doc.digilockerXml,
                        signerCn = doc.signerCn,
                        dscSerialNumber = doc.dscSerialNumber,
                        pkiTimestamp = doc.pkiTimestamp,
                        uploadedAt = "Saved"
                    )
                })
                Log.d(TAG, "Restored ${cachedDocs.size} cached documents from local storage")
            }
        }
        if (MoTaRepository.applications.isEmpty()) {
            val cachedApps = loadCachedApplications(context, userId)
            if (cachedApps.isNotEmpty()) {
                MoTaRepository.applications.addAll(cachedApps)
                Log.d(TAG, "Restored ${cachedApps.size} cached applications from local storage")
            }
        }
    }

    fun autoRestoreSession(context: android.content.Context): Boolean {
        appContext = context.applicationContext
        return try {
            val prefs = context.getSharedPreferences("step_user_prefs", android.content.Context.MODE_PRIVATE)
            val isLoggedInPref = prefs.getBoolean("is_logged_in", false)
            val lastUid = prefs.getString("last_active_uid", "") ?: ""
            val lastEmail = prefs.getString("last_active_email", "") ?: ""
            val lastPhoto = prefs.getString("last_active_photo", "") ?: ""
            val lastName = prefs.getString("last_active_name", "") ?: ""

            val lastGoogleAccount = GoogleSignIn.getLastSignedInAccount(context)

            val hasValidSession = isLoggedInPref || lastGoogleAccount != null ||
                    (lastEmail.isNotBlank() && prefs.getBoolean("reg_${lastEmail.lowercase().trim()}", false))

            if (!hasValidSession) {
                return false
            }

            val resolvedUid = when {
                lastGoogleAccount?.id != null -> lastGoogleAccount.id!!
                lastUid.isNotBlank() -> lastUid
                lastGoogleAccount?.email != null -> "usr_" + lastGoogleAccount.email!!.lowercase().trim().replace(Regex("[^a-zA-Z0-9]"), "_")
                lastEmail.isNotBlank() -> "usr_" + lastEmail.lowercase().trim().replace(Regex("[^a-zA-Z0-9]"), "_")
                else -> "usr_active"
            }

            val resolvedEmail = when {
                lastGoogleAccount?.email != null -> lastGoogleAccount.email!!
                lastEmail.isNotBlank() -> lastEmail
                else -> ""
            }

            val resolvedName = when {
                lastGoogleAccount?.displayName != null -> lastGoogleAccount.displayName!!
                lastName.isNotBlank() -> lastName
                else -> "ST Scholar"
            }

            val resolvedPhoto = when {
                lastGoogleAccount?.photoUrl != null -> lastGoogleAccount.photoUrl.toString()
                lastPhoto.isNotBlank() -> lastPhoto
                else -> ""
            }

            currentUser = GoogleUser(
                uid = resolvedUid,
                displayName = resolvedName,
                email = resolvedEmail,
                photoUrl = resolvedPhoto,
                idToken = lastGoogleAccount?.idToken.orEmpty()
            )

            val cachedProfile = (if (resolvedEmail.isNotBlank()) getLocalProfile(context, resolvedEmail) else null)
                ?: (if (resolvedUid.isNotBlank()) getLocalProfile(context, resolvedUid) else null)

            if (cachedProfile != null) {
                MoTaRepository.currentStudent = cachedProfile
            } else {
                MoTaRepository.currentStudent = MoTaRepository.currentStudent.copy(
                    uid = resolvedUid,
                    fullName = resolvedName,
                    email = resolvedEmail,
                    photoUrl = resolvedPhoto
                )
            }

            loadCachedDocumentsAndApplications(context, resolvedUid)

            isGoogleLoggedIn = true
            initDynamicFirestore(resolvedUid)
            refreshProfileFromFirestore(resolvedUid, resolvedEmail, context)
            Log.i(TAG, "Successfully restored session for $resolvedName ($resolvedEmail)")
            true
        } catch (e: Exception) {
            Log.e(TAG, "autoRestoreSession failed", e)
            false
        }
    }

    private fun refreshProfileFromFirestore(uid: String, email: String, context: android.content.Context) {
        val normalizedEmailUid = if (email.isNotBlank()) "usr_" + email.lowercase().trim().replace(Regex("[^a-zA-Z0-9]"), "_") else uid
        firestore.collection("users").document(uid).collection("profile").document("info")
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val p = parseProfileFromDoc(doc, uid, email)
                    MoTaRepository.currentStudent = p
                    saveLocalProfile(context, p)
                } else if (normalizedEmailUid != uid) {
                    firestore.collection("users").document(normalizedEmailUid).collection("profile").document("info")
                        .get()
                        .addOnSuccessListener { doc2 ->
                            if (doc2 != null && doc2.exists()) {
                                val p2 = parseProfileFromDoc(doc2, normalizedEmailUid, email)
                                MoTaRepository.currentStudent = p2
                                saveLocalProfile(context, p2)
                            }
                        }
                }
            }
    }

    fun checkExistingProfile(
        context: android.content.Context? = null,
        uid: String,
        email: String = "",
        onExisting: (StudentProfile) -> Unit,
        onNewUser: () -> Unit
    ) {
        if (context != null) appContext = context.applicationContext
        val normalizedEmailUid = if (email.isNotBlank()) "usr_" + email.lowercase().trim().replace(Regex("[^a-zA-Z0-9]"), "_") else uid

        // 1. Instant check in local SharedPreferences for fast, offline-safe returning user recognition
        if (context != null && email.isNotBlank()) {
            val cached = getLocalProfile(context, email) ?: getLocalProfile(context, uid)
            if (cached != null && cached.fullName.isNotBlank() && cached.fullName != "ST Scholar") {
                MoTaRepository.currentStudent = cached
                initDynamicFirestore(cached.uid.ifEmpty { uid })
                isGoogleLoggedIn = true
                onExisting(cached)
                return
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
                    if (context != null) saveLocalProfile(context, p)
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
                                if (context != null) saveLocalProfile(context, p2)
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
                                            if (context != null) saveLocalProfile(context, p3)
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
        if (context != null) appContext = context.applicationContext
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
        if (context != null) appContext = context.applicationContext
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

        if (context != null) {
            val prefs = context.getSharedPreferences("step_user_prefs", android.content.Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean("is_logged_in", true)
                .putString("last_active_uid", uid)
                .putString("last_active_email", email)
                .putString("last_active_name", name)
                .putString("last_active_photo", photo)
                .apply()
        }

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

    fun logout(context: android.content.Context? = null) {
        val ctx = context ?: appContext
        currentUser = null
        isGoogleLoggedIn = false
        currentActiveSyncUserId = null
        appsListenerRegistration?.remove()
        actionsListenerRegistration?.remove()
        notifsListenerRegistration?.remove()
        docsListenerRegistration?.remove()
        profileListenerRegistration?.remove()
        MoTaRepository.applications.clear()
        MoTaRepository.pendingActions.clear()
        MoTaRepository.notifications.clear()
        MoTaRepository.scannedDocuments.clear()
        MoTaRepository.currentStudent = MoTaDefaults.currentStudent

        if (ctx != null) {
            try {
                val prefs = ctx.getSharedPreferences("step_user_prefs", android.content.Context.MODE_PRIVATE)
                prefs.edit()
                    .putBoolean("is_logged_in", false)
                    .remove("last_active_uid")
                    .remove("last_active_email")
                    .remove("last_active_name")
                    .remove("last_active_photo")
                    .apply()

                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                val client = GoogleSignIn.getClient(ctx, gso)
                client.signOut()
            } catch (e: Exception) {
                Log.w(TAG, "Error during logout cleanup", e)
            }
        }
    }
}
