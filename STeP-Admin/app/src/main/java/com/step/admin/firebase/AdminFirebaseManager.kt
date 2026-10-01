package com.step.admin.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.step.admin.data.*
import com.step.admin.security.CryptoManager

object AdminFirebaseManager {
    private const val TAG = "AdminFirebase"
    private var db: FirebaseFirestore? = null

    fun init() {
        try {
            db = FirebaseFirestore.getInstance()
            Log.d(TAG, "Firebase Firestore initialized for STeP-Admin. Listening strictly to real server data.")

            listenToLiveApplications()
            listenToLiveSchemes()
            listenToLiveStudents()
            listenToLiveDocuments()
        } catch (e: Exception) {
            Log.w(TAG, "Firestore initialization error: ${e.message}")
        }
    }

    private fun listenToLiveApplications() {
        val firestore = db ?: return
        firestore.collection("applications")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error listening to applications: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = mutableListOf<AdminApplication>()
                    for (doc in snapshot.documents) {
                        try {
                            val app = AdminApplication(
                                id = doc.getString("applicationId") ?: doc.id,
                                docPath = doc.reference.path,
                                userId = doc.getString("userId") ?: "NAS",
                                studentName = doc.getString("studentName") ?: doc.getString("candidateName") ?: "NAS",
                                district = doc.getString("district") ?: "NAS",
                                scheme = doc.getString("schemeTitle") ?: "NAS",
                                portal = doc.getString("sourcePortal") ?: "NAS",
                                confidenceScore = doc.getLong("verificationConfidence")?.toInt() ?: 0,
                                status = doc.getString("stage") ?: "NAS",
                                source = doc.getString("source") ?: "NAS",
                                docType = doc.getString("docType") ?: "NAS",
                                appliedDate = doc.getString("appliedDate") ?: doc.getString("academicYear") ?: "NAS",
                                sanctionAmount = doc.getLong("sanctionAmount") ?: 0L,
                                stage = doc.getString("stage") ?: "NAS",
                                stageText = doc.getString("stageText") ?: "NAS",
                                certificateNumber = doc.getString("certificateNumber") ?: "NAS",
                                digilockerVerified = doc.getBoolean("digilockerVerified") ?: false
                            )
                            list.add(app)
                        } catch (e: Exception) {
                            Log.w(TAG, "Failed to parse application: ${e.message}")
                        }
                    }
                    if (list.isNotEmpty()) {
                        AdminRepository.applications.clear()
                        AdminRepository.applications.addAll(list)
                        AdminRepository.kpis = AdminRepository.kpis.copy(
                            pendingScrutinyCases = list.count { it.stage != "DISBURSED" && it.stage != "SANCTIONED" },
                            totalDbtDisbursedCr = (list.filter { it.stage == "DISBURSED" }.sumOf { it.sanctionAmount }) / 10000000.0
                        )
                    }
                }
            }
    }

    private fun listenToLiveSchemes() {
        val firestore = db ?: return
        firestore.collection("schemes")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                if (!snapshot.isEmpty) {
                    val list = mutableListOf<AdminScheme>()
                    for (doc in snapshot.documents) {
                        try {
                            val scheme = AdminScheme(
                                id = doc.getString("id") ?: doc.id,
                                code = doc.getString("code") ?: "NAS",
                                title = doc.getString("title") ?: "NAS",
                                portal = doc.getString("portal") ?: "NAS",
                                targetClass = doc.getString("targetClass") ?: "NAS",
                                incomeCeiling = doc.getLong("incomeCeiling"),
                                benefitSummary = doc.getString("benefitSummary") ?: "NAS",
                                maxBenefitAmount = doc.getLong("maxBenefitAmount") ?: 0L,
                                benefitAmountFormatted = doc.getString("benefitAmountFormatted") ?: "NAS",
                                deadlineFormatted = doc.getString("deadlineFormatted") ?: "NAS"
                            )
                            list.add(scheme)
                        } catch (e: Exception) {
                            Log.w(TAG, "Error parsing scheme: ${e.message}")
                        }
                    }
                    if (list.isNotEmpty()) {
                        AdminRepository.schemes.clear()
                        AdminRepository.schemes.addAll(list)
                        AdminRepository.kpis = AdminRepository.kpis.copy(activeSchemes = list.size)
                    }
                }
            }
    }

    /**
     * Listens to live student profiles across the real server.
     * If no real server data exists, list stays empty and displays NAS.
     */
    private fun listenToLiveStudents() {
        val firestore = db ?: return

        // 1. Top-level students collection
        firestore.collection("students")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(TAG, "Error listening to students collection: ${error.message}")
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val list = mutableListOf<AdminStudent>()
                    for (doc in snapshot.documents) {
                        try {
                            val uid = doc.getString("uid") ?: doc.id
                            val rawIncome = doc.get("annualIncome")?.toString() ?: "0"
                            val rawAccount = doc.getString("maskedAccount") ?: "NAS"
                            val rawAadhaar = doc.getString("aadhaarLast4") ?: "NAS"

                            val decryptedIncome = CryptoManager.decrypt(rawIncome, uid).toLongOrNull() ?: 0L
                            val decryptedAccount = CryptoManager.decrypt(rawAccount, uid).ifBlank { "NAS" }

                            val student = AdminStudent(
                                uid = uid,
                                fullName = doc.getString("fullName") ?: "NAS",
                                apaarId = doc.getString("apaarId") ?: "NAS",
                                digilockerId = doc.getString("digilockerId") ?: "NAS",
                                email = doc.getString("email") ?: "NAS",
                                state = doc.getString("state") ?: "NAS",
                                district = doc.getString("district") ?: "NAS",
                                community = doc.getString("community") ?: "NAS",
                                subTribe = doc.getString("subTribe") ?: "NAS",
                                educationLevel = doc.getString("educationLevel") ?: "NAS",
                                institution = doc.getString("institution") ?: "NAS",
                                annualIncome = decryptedIncome,
                                bankName = doc.getString("bankName") ?: "NAS",
                                maskedAccount = decryptedAccount,
                                npciAadhaarSeeded = doc.getBoolean("npciAadhaarSeeded") ?: false
                            )
                            list.add(student)
                        } catch (e: Exception) {
                            Log.w(TAG, "Error parsing student doc ${doc.id}: ${e.message}")
                        }
                    }

                    if (list.isNotEmpty()) {
                        AdminRepository.students.clear()
                        AdminRepository.students.addAll(list)
                        AdminRepository.kpis = AdminRepository.kpis.copy(totalScholars = list.size)
                        Log.d(TAG, "Loaded ${list.size} real student dossiers from Cloud Firestore")
                    }
                }
            }

        // 2. Also listen to collectionGroup("profile") for any users registered in users/{uid}/profile/info
        try {
            firestore.collectionGroup("profile")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    val currentIds = AdminRepository.students.map { it.uid }.toSet()
                    val additional = mutableListOf<AdminStudent>()

                    for (doc in snapshot.documents) {
                        try {
                            val uid = doc.getString("uid") ?: doc.reference.parent.parent?.id ?: doc.id
                            if (!currentIds.contains(uid)) {
                                val rawIncome = doc.get("annualIncome")?.toString() ?: "0"
                                val rawAccount = doc.getString("maskedAccount") ?: "NAS"

                                val student = AdminStudent(
                                    uid = uid,
                                    fullName = doc.getString("fullName") ?: "NAS",
                                    apaarId = doc.getString("apaarId") ?: "NAS",
                                    digilockerId = doc.getString("digilockerId") ?: "NAS",
                                    email = doc.getString("email") ?: "NAS",
                                    state = doc.getString("state") ?: "NAS",
                                    district = doc.getString("district") ?: "NAS",
                                    community = doc.getString("community") ?: "NAS",
                                    subTribe = doc.getString("subTribe") ?: "NAS",
                                    educationLevel = doc.getString("educationLevel") ?: "NAS",
                                    institution = doc.getString("institution") ?: "NAS",
                                    annualIncome = CryptoManager.decrypt(rawIncome, uid).toLongOrNull() ?: 0L,
                                    bankName = doc.getString("bankName") ?: "NAS",
                                    maskedAccount = CryptoManager.decrypt(rawAccount, uid).ifBlank { "NAS" },
                                    npciAadhaarSeeded = doc.getBoolean("npciAadhaarSeeded") ?: false
                                )
                                additional.add(student)
                            }
                        } catch (_: Exception) {}
                    }
                    if (additional.isNotEmpty()) {
                        AdminRepository.students.addAll(additional)
                        AdminRepository.kpis = AdminRepository.kpis.copy(totalScholars = AdminRepository.students.size)
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "collectionGroup(profile) listener exception: ${e.message}")
        }
    }

    /**
     * Listens to live documents across all users from real server via collectionGroup("documents").
     * If no real documents exist, list stays empty and displays NAS.
     */
    private fun listenToLiveDocuments() {
        val firestore = db ?: return
        try {
            firestore.collectionGroup("documents")
                .addSnapshotListener { snapshot, error ->
                    if (error != null || snapshot == null) return@addSnapshotListener
                    if (!snapshot.isEmpty) {
                        val docs = mutableListOf<AdminDocument>()
                        for (doc in snapshot.documents) {
                            try {
                                val item = AdminDocument(
                                    id = doc.id,
                                    documentType = doc.getString("documentType") ?: "NAS",
                                    candidateName = doc.getString("candidateName") ?: "NAS",
                                    fatherName = doc.getString("fatherName") ?: "NAS",
                                    certificateNumber = doc.getString("certificateNumber") ?: "NAS",
                                    issuingAuthority = doc.getString("issuingAuthority") ?: "NAS",
                                    issueDate = doc.getString("issueDate") ?: "NAS",
                                    validity = doc.getString("validity") ?: "NAS",
                                    confidenceScore = doc.getLong("confidenceScore")?.toInt() ?: 0,
                                    autoApproveEligible = doc.getBoolean("autoApproveEligible") ?: false,
                                    digilockerXml = doc.getString("digilockerXml") ?: "NAS",
                                    dscSerialNumber = doc.getString("dscSerialNumber") ?: "NAS",
                                    pkiTimestamp = doc.getString("pkiTimestamp") ?: "NAS",
                                    rollNumber = doc.getString("rollNumber") ?: doc.getString("certificateNumber") ?: "NAS",
                                    passingYear = doc.getString("passingYear") ?: "NAS",
                                    boardName = doc.getString("boardName") ?: "NAS",
                                    marksPercentage = doc.getDouble("marksPercentage") ?: 0.0,
                                    marksObtained = doc.getLong("marksObtained")?.toInt() ?: 0,
                                    maxMarks = doc.getLong("maxMarks")?.toInt() ?: 0,
                                    annualIncome = doc.getString("annualIncome") ?: "NAS",
                                    casteCommunity = doc.getString("casteCommunity") ?: "NAS"
                                )
                                docs.add(item)
                            } catch (e: Exception) {
                                Log.w(TAG, "Error parsing document ${doc.id}: ${e.message}")
                            }
                        }
                        if (docs.isNotEmpty()) {
                            AdminRepository.documents.clear()
                            AdminRepository.documents.addAll(docs)
                        }
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "collectionGroup(documents) exception: ${e.message}")
        }
    }

    fun syncSanctionToFirestore(appId: String, docPath: String) {
        val firestore = db ?: return
        if (docPath.isNotBlank()) {
            firestore.document(docPath).update(
                mapOf(
                    "stage" to "SANCTIONED",
                    "stageText" to "Sanction Order Generated by Ministry",
                    "verificationConfidence" to 99
                )
            )
        }
    }

    fun syncDisbursementToFirestore(appId: String, docPath: String, utr: String) {
        val firestore = db ?: return
        if (docPath.isNotBlank()) {
            firestore.document(docPath).update(
                mapOf(
                    "stage" to "DISBURSED",
                    "stageText" to "Disbursed via APB (UTR: $utr)"
                )
            )
        }
    }

    fun syncDeficiencyToFirestore(appId: String, docPath: String, reason: String) {
        val firestore = db ?: return
        if (docPath.isNotBlank()) {
            firestore.document(docPath).update(
                mapOf(
                    "stage" to "DEFICIENCY_FLAGGED",
                    "stageText" to "Defect Flagged by Officer",
                    "anomaly" to reason
                )
            )
        }
    }

    fun saveNewScheme(scheme: AdminScheme) {
        val firestore = db ?: return
        firestore.collection("schemes").document(scheme.id).set(
            mapOf(
                "id" to scheme.id,
                "code" to scheme.code,
                "title" to scheme.title,
                "portal" to scheme.portal,
                "targetClass" to scheme.targetClass,
                "incomeCeiling" to scheme.incomeCeiling,
                "benefitSummary" to scheme.benefitSummary,
                "maxBenefitAmount" to scheme.maxBenefitAmount,
                "benefitAmountFormatted" to scheme.benefitAmountFormatted,
                "deadlineFormatted" to scheme.deadlineFormatted
            )
        )
    }
}
