package com.step.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class StudentProfile(
    val uid: String = "",
    val apaarId: String = "NFS",
    val digilockerId: String = "NFS",
    val fullName: String = "NFS",
    val email: String = "NFS",
    val photoUrl: String = "",
    val community: String = "Scheduled Tribe (ST)",
    val subTribe: String = "NFS",
    val institution: String = "NFS",
    val educationLevel: String = "NFS",
    val annualIncome: Long = 0L,
    val incomeCertExpiryDays: Int = 0,
    val bankName: String = "NFS",
    val maskedAccount: String = "NFS",
    val ifsc: String = "NFS",
    val aadhaarLast4: String = "NFS",
    val state: String = "NFS",
    val npciAadhaarSeeded: Boolean = false
)

data class Scheme(
    val id: String,
    val code: String,
    val title: String,
    val hindiTitle: String,
    val portal: String,
    val targetClass: String,
    val incomeCeiling: Long?,
    val benefitSummary: String,
    val maxBenefitAmount: Long,
    val benefitAmountFormatted: String,
    val deadlineFormatted: String,
    val eligibilityTag: String, // "Eligible", "Applied", "Check eligibility", "Not eligible"
    val description: String,
    val documentsNeeded: List<String>,
    val rules: List<String>
)

data class TimelineStep(
    val label: String,
    val date: String,
    val completed: Boolean,
    val note: String
)

data class DbtDetails(
    val utr: String,
    val paymentMode: String,
    val disbursedDate: String,
    val bankName: String,
    val accountNo: String,
    val centralShare: String,
    val stateShare: String,
    val status: String
)

data class DeficiencyInfo(
    val code: String,
    val bureaucraticReason: String,
    val deadlineDate: String,
    val daysRemaining: Int
)

data class ApplicationRecord(
    val applicationId: String,
    val schemeId: String,
    val schemeTitle: String,
    val academicYear: String,
    val sourcePortal: String,
    val stage: String,
    val stageText: String,
    val currentStepIndex: Int, // 0 = Submitted, 1 = Verified, 2 = Sanctioned, 3 = Disbursed
    val sanctionAmount: Long,
    val nextActionText: String,
    val verificationConfidence: Int,
    val steps: List<TimelineStep>,
    val dbtDetails: DbtDetails?,
    val deficiency: DeficiencyInfo?
)

data class PendingAction(
    val id: String,
    val title: String,
    val scheme: String,
    val reason: String,
    val isUrgent: Boolean,
    val actionText: String
)

data class NotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val time: String,
    val isUnread: Boolean,
    val type: String // "PAYMENT", "DEFICIENCY", "EXPIRY", "VERIFICATION"
)

data class ScannedDocument(
    val id: String = "",
    val documentType: String,
    val candidateName: String,
    val fatherName: String,
    val certificateNumber: String,
    val issuingAuthority: String,
    val issueDate: String,
    val validity: String,
    val isExpired: Boolean = false,
    val casteCommunity: String? = null,
    val annualIncome: String? = null,
    val confidenceScore: Int,
    val autoApproveEligible: Boolean,
    val sharedHostingUrl: String = "",
    val syncedToFirebase: Boolean = true,
    val digilockerXml: String = "",
    val signerCn: String = "",
    val dscSerialNumber: String = "",
    val pkiTimestamp: String = ""
)

object MoTaDefaults {
    var currentStudent = StudentProfile()

    // 0 demo data — all live data fetched from Firebase Firestore and DigiLocker
    val pendingActions = emptyList<PendingAction>()

    val notifications = emptyList<NotificationItem>()

    val schemes = listOf(
        Scheme(
            id = "PRE_MATRIC",
            code = "SCH-01",
            title = "Pre-Matric Scholarship for ST",
            hindiTitle = "प्री-मैट्रिक छात्रवृत्ति (कक्षा 9-10)",
            portal = "National Scholarship Portal (NSP)",
            targetClass = "Class 9 & 10",
            incomeCeiling = 250000,
            benefitSummary = "₹3,500/yr (Day Scholar) | ₹7,000/yr (Hosteller) + ₹1,000 books",
            maxBenefitAmount = 7000,
            benefitAmountFormatted = "₹ 7,000 / year",
            deadlineFormatted = "31 Oct 2026",
            eligibilityTag = "Eligible",
            description = "Provides financial assistance to Scheduled Tribe students studying in classes IX and X to reduce dropout rates and support their transition to higher secondary education.",
            documentsNeeded = listOf(
                "ST Community Certificate",
                "Family Income Certificate (<= ₹2.50L)",
                "Previous Year School Marksheet",
                "Aadhaar-seeded Bank Passbook"
            ),
            rules = listOf(
                "Must be ST student in Class 9-10 in recognized govt/EMRS school",
                "Annual family income <= ₹2.50 Lakh",
                "100% DBT payment through PFMS"
            )
        ),
        Scheme(
            id = "POST_MATRIC",
            code = "SCH-02",
            title = "Post-Matric Scholarship (PMS-ST)",
            hindiTitle = "पोस्ट-मैट्रिक छात्रवृत्ति",
            portal = "National Scholarship Portal (NSP)",
            targetClass = "Class 11, 12, Degree, PG, ITI, Professional",
            incomeCeiling = 250000,
            benefitSummary = "100% Tuition Fee waiver + Maintenance up to ₹13,500/year",
            maxBenefitAmount = 28500,
            benefitAmountFormatted = "100% Fee + ₹ 13,500",
            deadlineFormatted = "30 Nov 2026",
            eligibilityTag = "Applied",
            description = "Covers complete post-matriculation educational expenses for ST scholars including non-refundable tuition fee waivers and monthly study maintenance allowances.",
            documentsNeeded = listOf(
                "ST Caste Certificate (DigiLocker)",
                "Current Academic Year Income Certificate",
                "Class 10 / 12 Board Passing Certificate",
                "Admission Fee Receipt from Institution",
                "Valid Bank Account seeded with Aadhaar"
            ),
            rules = listOf(
                "ST candidate pursuing higher secondary or university education",
                "Annual family income <= ₹2.50 Lakh",
                "75% Central share + 25% State share credited directly to bank"
            )
        ),
        Scheme(
            id = "TOP_CLASS",
            code = "SCH-03",
            title = "Top Class Education Scheme",
            hindiTitle = "शीर्ष संस्थान योजना (IIT/IIM/NIT)",
            portal = "SFMP (Canara Bank) & NSP",
            targetClass = "Undergraduate/PG in 265 Notified Premier Institutes",
            incomeCeiling = 600000,
            benefitSummary = "Full Tuition Fee + Living ₹3,000/mo + Books ₹5,000/yr + Laptop ₹45,000",
            maxBenefitAmount = 286000,
            benefitAmountFormatted = "₹ 2,86,000 / year",
            deadlineFormatted = "15 Dec 2026",
            eligibilityTag = "Eligible",
            description = "Recognizes and promotes quality education among ST students in 265 premier institutions of national importance like IITs, IIMs, NITs, AIIMS, and NLUs.",
            documentsNeeded = listOf(
                "ST Caste Certificate",
                "Income Certificate (<= ₹6.00L)",
                "JEE / NEET / CAT / CLAT Entrance Rank Card",
                "Premier Institute Admission Offer Letter",
                "First Semester Institute Fee Demand Note"
            ),
            rules = listOf(
                "Secured admission in 265 elite institutes (IIT, IIM, NIT, AIIMS, NLU)",
                "Annual family income up to ₹6.00 Lakhs",
                "Includes ₹45,000 one-time computer grant in 1st year"
            )
        ),
        Scheme(
            id = "NFST",
            code = "SCH-04",
            title = "National Fellowship for ST (NFST)",
            hindiTitle = "राष्ट्रीय अध्येतावृत्ति (M.Phil / Ph.D)",
            portal = "SFMP (Canara Bank)",
            targetClass = "Regular full-time M.Phil & Ph.D scholars",
            incomeCeiling = null,
            benefitSummary = "JRF: ₹31,000/mo | SRF: ₹35,000/mo + HRA + Contingency ₹25,000/yr",
            maxBenefitAmount = 480000,
            benefitAmountFormatted = "₹ 35,000 / month",
            deadlineFormatted = "31 Dec 2026",
            eligibilityTag = "Check eligibility",
            description = "Provides 750 annual fellowships to regular, full-time ST research scholars pursuing M.Phil and Ph.D. in Science, Humanities, Engineering, and Technology.",
            documentsNeeded = listOf(
                "ST Caste Certificate",
                "UGC-NET / CSIR-NET / GATE Qualified Scorecard",
                "Ph.D. / M.Phil Admission & Registration Letter",
                "Research Supervisor Allocation Certificate",
                "Bank Mandate Form"
            ),
            rules = listOf(
                "UGC-NET / CSIR-NET / GATE qualified regular research scholars",
                "NO family income ceiling applies",
                "750 fresh fellowships allocated annually by MoTA"
            )
        ),
        Scheme(
            id = "NOS",
            code = "SCH-05",
            title = "National Overseas Scholarship (NOS)",
            hindiTitle = "राष्ट्रीय प्रवासी छात्रवृत्ति (विदेश अध्ययन)",
            portal = "Standalone NOS Portal (overseas.tribal.gov.in)",
            targetClass = "Master's & Ph.D abroad in Top 500 QS World Ranking Universities",
            incomeCeiling = 800000,
            benefitSummary = "Full Tuition + Living £9,900 (UK)/$15,400 (US)/yr + Airfare + Visa",
            maxBenefitAmount = 2200000,
            benefitAmountFormatted = "100% Tuition + £9,900/yr",
            deadlineFormatted = "15 Jan 2027",
            eligibilityTag = "Eligible",
            description = "Provides financial support to 20 meritorious Scheduled Tribe scholars (with 3 PVTG reserved slots) to pursue Master's and Ph.D. degrees in Top 500 QS ranked global universities.",
            documentsNeeded = listOf(
                "ST Caste Certificate",
                "Income Certificate (<= ₹8.00L)",
                "Unconditional Offer Letter from Top 500 QS Institution",
                "IELTS / TOEFL Scorecard",
                "Passport (Valid 2+ Years)",
                "Two Academic Recommendation Letters"
            ),
            rules = listOf(
                "Unconditional admission offer in Top 500 QS World Ranked University",
                "Family income <= ₹8.00 Lakh per annum",
                "20 slots per year (3 ring-fenced for PVTG communities)"
            )
        )
    )

    // 0 demo applications — applications are fetched dynamically from Firebase Cloud Firestore
    val applications = emptyList<ApplicationRecord>()

}

object MoTaRepository {
    var currentStudent by mutableStateOf(MoTaDefaults.currentStudent)

    val pendingActions = mutableStateListOf<PendingAction>()

    val notifications = mutableStateListOf<NotificationItem>()

    val schemes = mutableStateListOf<Scheme>().apply {
        addAll(MoTaDefaults.schemes)
    }

    val applications = mutableStateListOf<ApplicationRecord>()

    val scannedDocuments = mutableStateListOf<ScannedDocument>()

    val sampleCasteDoc get() = scannedDocuments.firstOrNull { it.documentType.contains("Caste") || it.documentType.contains("ST") }
    val sampleIncomeDoc get() = scannedDocuments.firstOrNull { it.documentType.contains("Income") }
    val sampleMarksheetDoc get() = scannedDocuments.firstOrNull { it.documentType.contains("Marksheet") || it.documentType.contains("Academic") }
}

