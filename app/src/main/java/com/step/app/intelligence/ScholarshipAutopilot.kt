package com.step.app.intelligence

import com.step.app.data.MoTaRepository
import com.step.app.data.ScannedDocument
import com.step.app.data.Scheme
import com.step.app.data.StudentProfile

data class MissingDocumentItem(
    val docName: String,
    val schemeCodesNeededFor: List<String>,
    val howToGetIt: String,
    val digitalSource: String = "DigiLocker NeGD"
)

data class SchemeEligibilityMatch(
    val scheme: Scheme,
    val isEligible: Boolean,
    val matchingScore: Int,                 // 0 to 100%
    val missingRequirements: List<String>,
    val explainWhy: String,
    val explainWhat: String,
    val explainHow: String
)

data class AutoplanResult(
    val otrCompletionPercentage: Int,       // 0 to 100%
    val eligibleSchemeCount: Int,
    val totalAvailableSchemes: Int,
    val bestScheme: Scheme?,
    val bestSchemeAnnualEntitlement: Long,
    val missingDocuments: List<MissingDocumentItem>,
    val schemeMatches: List<SchemeEligibilityMatch>,
    val upcomingDeadlinesSummary: String
)

/**
 * ScholarshipAutopilot — Generates the complete Autoplan for the scholar:
 * 1. Document Requirement Graph (what & how missing)
 * 2. Multi-scheme eligibility count
 * 3. OTR completion status
 * 4. Best scheme recommendation
 * 5. Expiration & deadline engine
 */
object ScholarshipAutopilot {

    fun generateAutoplan(
        student: StudentProfile,
        documents: List<ScannedDocument>,
        schemes: List<Scheme> = MoTaRepository.schemes
    ): AutoplanResult {
        // 1. Check Document Availability
        val hasCaste = documents.any { it.documentType.contains("Caste", true) || it.documentType.contains("ST", true) }
        val hasIncome = documents.any { it.documentType.contains("Income", true) && !it.isExpired }
        val hasMarksheet = documents.any { it.documentType.contains("Marksheet", true) || it.documentType.contains("Board", true) }
        val hasBank = student.npciAadhaarSeeded && student.maskedAccount != "NAS"

        // 2. Missing Documents Requirement Graph
        val missingDocs = mutableListOf<MissingDocumentItem>()
        if (!hasCaste) {
            missingDocs.add(
                MissingDocumentItem(
                    docName = "ST Caste Community Certificate",
                    schemeCodesNeededFor = listOf("SCH-01", "SCH-02", "SCH-03", "SCH-04", "SCH-05"),
                    howToGetIt = "Pull via DigiLocker e-District using your State Revenue Memo / Application Number."
                )
            )
        }
        if (!hasIncome) {
            missingDocs.add(
                MissingDocumentItem(
                    docName = "Current FY Family Income Certificate",
                    schemeCodesNeededFor = listOf("SCH-01", "SCH-02", "SCH-03", "SCH-05"),
                    howToGetIt = "Obtain from Tehsildar / SDO office or sync digital copy from DigiLocker revenue services."
                )
            )
        }
        if (!hasMarksheet) {
            missingDocs.add(
                MissingDocumentItem(
                    docName = "Class 10 / 12 Board Marksheet",
                    schemeCodesNeededFor = listOf("SCH-01", "SCH-02", "SCH-03"),
                    howToGetIt = "Link Academic Bank of Credits (ABC / APAAR) in DigiLocker."
                )
            )
        }
        if (!hasBank) {
            missingDocs.add(
                MissingDocumentItem(
                    docName = "NPCI Aadhaar-Seeded Bank Account",
                    schemeCodesNeededFor = listOf("SCH-01", "SCH-02", "SCH-03", "SCH-04", "SCH-05"),
                    howToGetIt = "Visit your bank branch or use Aadhaar Seeding Portal to enable APBS bridge.",
                    digitalSource = "NPCI APBS Gateway"
                )
            )
        }

        // 3. OTR Completion Score Calculation
        var otrPoints = 0
        if (student.fullName != "NAS" && student.fullName.isNotBlank()) otrPoints += 20
        if (student.apaarId != "NAS" && student.apaarId.isNotBlank()) otrPoints += 20
        if (hasCaste) otrPoints += 20
        if (hasIncome) otrPoints += 20
        if (hasBank) otrPoints += 20
        val otrPercentage = otrPoints

        // 4. Scheme Evaluation & Explain Why/What/How
        val matches = schemes.map { scheme ->
            evaluateScheme(scheme, student, documents, hasCaste, hasIncome, hasBank)
        }

        val eligibleMatches = matches.filter { it.isEligible }
        val bestMatch = eligibleMatches.maxByOrNull { it.scheme.maxBenefitAmount }?.scheme
            ?: schemes.firstOrNull()

        return AutoplanResult(
            otrCompletionPercentage = otrPercentage,
            eligibleSchemeCount = eligibleMatches.size,
            totalAvailableSchemes = schemes.size,
            bestScheme = bestMatch,
            bestSchemeAnnualEntitlement = bestMatch?.maxBenefitAmount ?: 0L,
            missingDocuments = missingDocs,
            schemeMatches = matches,
            upcomingDeadlinesSummary = schemes
                .filter { it.deadlineFormatted.isNotBlank() }
                .take(3)
                .joinToString(" • ") { "${it.deadlineFormatted} (${it.code})" }
                .ifBlank { "Check NSP portal for current deadlines" }
        )
    }

    private fun evaluateScheme(
        scheme: Scheme,
        student: StudentProfile,
        documents: List<ScannedDocument>,
        hasCaste: Boolean,
        hasIncome: Boolean,
        hasBank: Boolean
    ): SchemeEligibilityMatch {
        val missing = mutableListOf<String>()
        var isEligible = true

        val isSt = student.community.contains("ST", true) || student.community.contains("Scheduled Tribe", true) || hasCaste
        if (!isSt) {
            missing.add("Must belong to Scheduled Tribe (ST) category")
            isEligible = false
        }

        if (scheme.incomeCeiling != null) {
            if (student.annualIncome > scheme.incomeCeiling) {
                missing.add("Family income ₹${student.annualIncome} exceeds limit of ₹${scheme.incomeCeiling}")
                isEligible = false
            }
        }

        if (!hasBank) {
            missing.add("NPCI Aadhaar-seeded bank account required for DBT")
        }

        val why = when (scheme.id) {
            "PRE_MATRIC" -> "You are eligible if you are an ST student enrolled in Classes 9 or 10 with annual family income up to ₹2.50 Lakh."
            "POST_MATRIC" -> "You qualify as an ST scholar pursuing post-matriculation higher education with income within ₹2.50 Lakh."
            "TOP_CLASS" -> "You qualify by admission to premier notified institutes (IIT, IIM, NIT, AIIMS, NLU) with family income up to ₹6.00 Lakh."
            "NFST" -> "Open to all regular full-time M.Phil & Ph.D. scholars in recognized universities. NO income limit applied!"
            "NOS" -> "Eligible for Masters & Ph.D. in QS Top 500 foreign universities with family income up to ₹8.00 Lakh."
            else -> "Based on your verified student profile and academic status."
        }

        val what = "Entitlement: ${scheme.benefitSummary} (Annual Cap: ${scheme.benefitAmountFormatted})"
        val how = "Disbursed 100% electronically via the Aadhaar Payment Bridge (APB) directly to your bank account."

        val score = if (isEligible) 95 else (100 - (missing.size * 30)).coerceAtLeast(10)

        return SchemeEligibilityMatch(
            scheme = scheme,
            isEligible = isEligible,
            matchingScore = score,
            missingRequirements = missing,
            explainWhy = why,
            explainWhat = what,
            explainHow = how
        )
    }
}
