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
    val firestoreCollection: String = "users/birsa_munda/documents",
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

    /**
     * Initializes bidirectional dynamic Firestore synchronization.
     * All changes in Firebase Firestore sync to the app in real time,
     * and app actions save directly to Firebase.
     */
    fun initDynamicFirestore(userId: String = currentUser?.uid ?: "usr_google_birsa_984") {
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
                // Seed initial applications to user's Firestore path
                seedApplicationsToFirestore(userId)
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

                if (remoteApps.isNotEmpty()) {
                    MoTaRepository.applications.clear()
                    MoTaRepository.applications.addAll(remoteApps)
                    Log.d(TAG, "Loaded ${remoteApps.size} dynamic applications from Firebase Firestore")
                }
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
                seedPendingActionsToFirestore(userId)
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
        userNotifsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to notifications in Firestore", error)
                return@addSnapshotListener
            }

            if (snapshot == null || snapshot.isEmpty) {
                seedNotificationsToFirestore(userId)
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
        userDocsRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e(TAG, "Error listening to documents in Firestore", error)
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                val remoteDocs = snapshot.documents.mapNotNull { doc ->
                    try {
                        FirestoreDocumentLink(
                            docId = doc.getString("docId") ?: doc.id,
                            docType = doc.getString("docType") ?: "Certificate",
                            certificateNumber = doc.getString("certificateNumber") ?: "",
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
            } else {
                seedDocumentsToFirestore(userId)
            }
        }

        // 6. DYNAMIC PROFILE SYNC (Firestore Document: "users/{userId}/profile/info")
        val profileDocRef = firestore.collection("users").document(userId).collection("profile").document("info")
        profileDocRef.addSnapshotListener { doc, error ->
            if (error != null || doc == null || !doc.exists()) {
                saveStudentProfileToFirestore(MoTaRepository.currentStudent, userId)
                return@addSnapshotListener
            }
            try {
                MoTaRepository.currentStudent = MoTaRepository.currentStudent.copy(
                    fullName = doc.getString("fullName") ?: MoTaRepository.currentStudent.fullName,
                    email = doc.getString("email") ?: MoTaRepository.currentStudent.email,
                    institution = doc.getString("institution") ?: MoTaRepository.currentStudent.institution,
                    educationLevel = doc.getString("educationLevel") ?: MoTaRepository.currentStudent.educationLevel,
                    community = doc.getString("community") ?: MoTaRepository.currentStudent.community,
                    subTribe = doc.getString("subTribe") ?: MoTaRepository.currentStudent.subTribe,
                    annualIncome = doc.getLong("annualIncome") ?: MoTaRepository.currentStudent.annualIncome,
                    bankName = doc.getString("bankName") ?: MoTaRepository.currentStudent.bankName,
                    state = doc.getString("state") ?: MoTaRepository.currentStudent.state,
                    npciAadhaarSeeded = doc.getBoolean("npciAadhaarSeeded") ?: true
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse profile from Firestore", e)
            }
        }
    }

    // --- WRITE OPERATIONS TO FIREBASE FIRESTORE ---

    fun saveStudentProfileToFirestore(profile: StudentProfile, userId: String = currentUser?.uid ?: "usr_google_birsa_984") {
        val data = hashMapOf(
            "uid" to userId,
            "apaarId" to profile.apaarId,
            "digilockerId" to profile.digilockerId,
            "fullName" to profile.fullName,
            "email" to profile.email,
            "community" to profile.community,
            "subTribe" to profile.subTribe,
            "institution" to profile.institution,
            "educationLevel" to profile.educationLevel,
            "annualIncome" to profile.annualIncome,
            "bankName" to profile.bankName,
            "maskedAccount" to profile.maskedAccount,
            "ifsc" to profile.ifsc,
            "aadhaarLast4" to profile.aadhaarLast4,
            "state" to profile.state,
            "npciAadhaarSeeded" to profile.npciAadhaarSeeded,
            "lastSyncedAt" to System.currentTimeMillis()
        )
        firestore.collection("users").document(userId)
            .collection("profile").document("info")
            .set(data, SetOptions.merge())
    }

    fun submitApplicationToFirestore(app: ApplicationRecord, userId: String = currentUser?.uid ?: "usr_google_birsa_984") {
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
        userId: String = currentUser?.uid ?: "usr_google_birsa_984"
    ) {
        val docId = doc.id.ifEmpty { "doc_" + System.currentTimeMillis().toString().takeLast(6) }
        val docMap = hashMapOf(
            "docId" to docId,
            "docType" to doc.documentType,
            "certificateNumber" to doc.certificateNumber,
            "issuingAuthority" to doc.issuingAuthority,
            "confidenceScore" to doc.confidenceScore,
            "sharedHostingImageUrl" to sharedHostingUrl,
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
        userId: String = currentUser?.uid ?: "usr_google_birsa_984"
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

    private fun seedApplicationsToFirestore(userId: String) {
        Log.d(TAG, "Seeding initial user applications to Firebase Firestore...")
        MoTaDefaults.applications.forEach { app ->
            submitApplicationToFirestore(app, userId)
        }
    }

    private fun seedPendingActionsToFirestore(userId: String) {
        Log.d(TAG, "Seeding pending actions to Firebase Firestore...")
        MoTaDefaults.pendingActions.forEach { action ->
            val data = hashMapOf(
                "id" to action.id,
                "title" to action.title,
                "scheme" to action.scheme,
                "reason" to action.reason,
                "isUrgent" to action.isUrgent,
                "actionText" to action.actionText
            )
            firestore.collection("users").document(userId)
                .collection("pending_actions").document(action.id)
                .set(data, SetOptions.merge())
        }
    }

    private fun seedNotificationsToFirestore(userId: String) {
        Log.d(TAG, "Seeding notifications to Firebase Firestore...")
        MoTaDefaults.notifications.forEach { notif ->
            val data = hashMapOf(
                "id" to notif.id,
                "title" to notif.title,
                "body" to notif.body,
                "time" to notif.time,
                "isUnread" to notif.isUnread,
                "type" to notif.type
            )
            firestore.collection("users").document(userId)
                .collection("notifications").document(notif.id)
                .set(data, SetOptions.merge())
        }
    }

    private fun seedDocumentsToFirestore(userId: String) {
        listOf(
            FirestoreDocumentLink(
                docId = "doc_st_49201",
                docType = "Scheduled Tribe (ST) Certificate",
                certificateNumber = "OD/ST/2022/49201",
                issuingAuthority = "Tehsildar Baripada, Odisha",
                confidenceScore = 98,
                sharedHostingImageUrl = "https://dhaaga.thecoolestportfolio.site/uploads/caste_OD_ST_2022_49201.jpg",
                uploadedAt = "14-Jun-2025"
            ),
            FirestoreDocumentLink(
                docId = "doc_inc_11093",
                docType = "Annual Family Income Certificate",
                certificateNumber = "OD/INC/2025/11093",
                issuingAuthority = "Tehsildar Baripada, Odisha",
                confidenceScore = 95,
                sharedHostingImageUrl = "https://dhaaga.thecoolestportfolio.site/uploads/income_OD_INC_2025_11093.jpg",
                uploadedAt = "25-Oct-2025"
            ),
            FirestoreDocumentLink(
                docId = "doc_nos_0219",
                docType = "Foreign Admission Offer Letter",
                certificateNumber = "ICL-CID-02194812",
                issuingAuthority = "Imperial College London",
                confidenceScore = 78,
                sharedHostingImageUrl = "https://dhaaga.thecoolestportfolio.site/uploads/offer_imperial_02194812.jpg",
                uploadedAt = "20-Aug-2026"
            )
        ).forEach { docLink ->
            val data = hashMapOf(
                "docId" to docLink.docId,
                "docType" to docLink.docType,
                "certificateNumber" to docLink.certificateNumber,
                "issuingAuthority" to docLink.issuingAuthority,
                "confidenceScore" to docLink.confidenceScore,
                "sharedHostingImageUrl" to docLink.sharedHostingImageUrl,
                "uploadedAt" to docLink.uploadedAt
            )
            firestore.collection("users").document(userId)
                .collection("documents").document(docLink.docId)
                .set(data, SetOptions.merge())
        }
    }

    fun checkExistingProfile(
        uid: String,
        onExisting: (StudentProfile) -> Unit,
        onNewUser: () -> Unit
    ) {
        firestore.collection("users").document(uid).collection("profile").document("info")
            .get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val p = StudentProfile(
                        uid = uid,
                        apaarId = doc.getString("apaarId") ?: "9842-1084-2026",
                        digilockerId = doc.getString("digilockerId") ?: "DL-ST-${uid.takeLast(6)}",
                        fullName = doc.getString("fullName") ?: "ST Scholar",
                        email = doc.getString("email") ?: "",
                        community = doc.getString("community") ?: "Scheduled Tribe (ST)",
                        subTribe = doc.getString("subTribe") ?: "ST",
                        institution = doc.getString("institution") ?: "",
                        educationLevel = doc.getString("educationLevel") ?: "Class 12",
                        annualIncome = doc.getLong("annualIncome") ?: 145000L,
                        bankName = doc.getString("bankName") ?: "State Bank of India",
                        maskedAccount = doc.getString("maskedAccount") ?: "•••• •••• 4920",
                        ifsc = doc.getString("ifsc") ?: "SBIN0001234",
                        aadhaarLast4 = doc.getString("aadhaarLast4") ?: "9842",
                        state = doc.getString("state") ?: "Odisha",
                        npciAadhaarSeeded = doc.getBoolean("npciAadhaarSeeded") ?: true
                    )
                    MoTaRepository.currentStudent = p
                    initDynamicFirestore(uid)
                    isGoogleLoggedIn = true
                    onExisting(p)
                } else {
                    onNewUser()
                }
            }
            .addOnFailureListener {
                onNewUser()
            }
    }

    fun completeRegistration(profile: StudentProfile) {
        MoTaRepository.currentStudent = profile
        saveStudentProfileToFirestore(profile, profile.uid)
        initDynamicFirestore(profile.uid)
        isGoogleLoggedIn = true
    }

    fun loginWithGoogleAccount(account: GoogleSignInAccount, onReady: (isNewUser: Boolean) -> Unit) {
        val name = account.displayName ?: account.givenName ?: "ST Scholar"
        val email = account.email ?: "student@step.gov.in"
        val photo = account.photoUrl?.toString().orEmpty()
        val idToken = account.idToken.orEmpty()
        val uid = account.id ?: ("usr_google_" + System.currentTimeMillis().toString().takeLast(6))

        val user = GoogleUser(
            uid = uid,
            displayName = name,
            email = email,
            photoUrl = photo,
            idToken = idToken
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

    fun loginWithGoogle(
        name: String = "Birsa Munda",
        email: String = "birsa.munda@student.gov.in",
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
    }
}
