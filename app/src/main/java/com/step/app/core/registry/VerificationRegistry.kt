package com.step.app.core.registry

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Reusable Document Model under MoTA's "Verify Once → Reuse Everywhere" Framework.
 * Eliminates redundant document uploads across Pre-Matric, Post-Matric, Top Class, NFST, and NOS.
 */
data class ReusableVerifiedDocument(
    val verificationId: String,               // Unique Sovereign VID: e.g. "VID-2026-ST-48201"
    val docType: String,                      // "ST_COMMUNITY", "ANNUAL_INCOME", "BOARD_MARKSHEET_10", etc.
    val docName: String,                      // Human-readable title
    val candidateName: String,
    val fatherName: String,
    val certificateNumber: String,
    val issuingAuthority: String,
    val issueDate: String,
    val expiryDate: String? = null,
    val isPermanent: Boolean = false,
    val negdDscVerified: Boolean = true,
    val dscSerialNumber: String = "",
    val signerCn: String = "",
    val docSha256Hash: String,
    val permittedSchemes: MutableList<String> = mutableListOf("SCH-01", "SCH-02", "SCH-03", "SCH-04", "SCH-05"),
    val reuseHistory: MutableList<ReuseAuditEntry> = mutableListOf()
)

data class ReuseAuditEntry(
    val schemeId: String,
    val schemeTitle: String,
    val timestamp: String,
    val applicationRef: String,
    val consentStatus: String = "CONSENT_GRANTED_DPDP"
)

object VerificationRegistry {

    private val registry = mutableMapOf<String, ReusableVerifiedDocument>()

    /**
     * Registers a verified document and returns its sovereign Verification ID (VID).
     */
    fun registerVerifiedDocument(
        docType: String,
        docName: String,
        candidateName: String,
        fatherName: String,
        certificateNumber: String,
        issuingAuthority: String,
        issueDate: String,
        expiryDate: String?,
        isPermanent: Boolean,
        rawPayloadToHash: String,
        signerCn: String = "e-District NeGD PKI",
        dscSerial: String = "DSC-NeGD-2026-X509"
    ): ReusableVerifiedDocument {
        val vid = "VID-${SimpleDateFormat("yyyy", Locale.getDefault()).format(Date())}-${docType.take(4).uppercase()}-${(100000..999999).random()}"
        val hash = calculateSha256(rawPayloadToHash.ifEmpty { "$certificateNumber|$candidateName|$issueDate" })

        val record = ReusableVerifiedDocument(
            verificationId = vid,
            docType = docType,
            docName = docName,
            candidateName = candidateName,
            fatherName = fatherName,
            certificateNumber = certificateNumber,
            issuingAuthority = issuingAuthority,
            issueDate = issueDate,
            expiryDate = expiryDate,
            isPermanent = isPermanent,
            negdDscVerified = true,
            dscSerialNumber = dscSerial,
            signerCn = signerCn,
            docSha256Hash = hash
        )
        registry[vid] = record
        return record
    }

    /**
     * Consumes an already-verified document for a new scheme without requesting re-upload.
     */
    fun reuseForScheme(vid: String, schemeId: String, schemeTitle: String, applicationRef: String): Boolean {
        val doc = registry[vid] ?: return false
        val dateFormat = SimpleDateFormat("dd-MMM-yyyy HH:mm IST", Locale.getDefault())
        doc.reuseHistory.add(
            ReuseAuditEntry(
                schemeId = schemeId,
                schemeTitle = schemeTitle,
                timestamp = dateFormat.format(Date()),
                applicationRef = applicationRef
            )
        )
        return true
    }

    fun getByVid(vid: String): ReusableVerifiedDocument? = registry[vid]

    fun getAllDocuments(): List<ReusableVerifiedDocument> = registry.values.toList()

    private fun calculateSha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
