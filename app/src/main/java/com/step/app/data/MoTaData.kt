package com.step.app.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class StudentProfile(
    val uid: String = "usr_google_894120",
    val apaarId: String = "9842-1084-2026",
    val digilockerId: String = "DL-ST-883921",
    val fullName: String = "Birsa Munda",
    val email: String = "birsa.munda@student.gov.in",
    val photoUrl: String = "",
    val community: String = "Scheduled Tribe (ST)",
    val subTribe: String = "Santhal",
    val institution: String = "EMRS Baripada, Mayurbhanj, Odisha",
    val educationLevel: String = "Class 12 (Science)",
    val annualIncome: Long = 145000,
    val incomeCertExpiryDays: Int = 28,
    val bankName: String = "State Bank of India",
    val maskedAccount: String = "•••• •••• 4920",
    val ifsc: String = "SBIN0001234",
    val aadhaarLast4: String = "9842",
    val state: String = "Odisha",
    val npciAadhaarSeeded: Boolean = true
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
    val id: String = "doc_caste_01",
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
    val sharedHostingUrl: String = "https://dhaaga.thecoolestportfolio.site/uploads/caste_OD_ST_2022_49201.jpg",
    val syncedToFirebase: Boolean = true
)

object MoTaDefaults {
    var currentStudent = StudentProfile()

    val pendingActions = listOf(
        PendingAction(
            id = "act_inc_renew",
            title = "Upload renewed income certificate",
            scheme = "Pre-Matric Scholarship (NSP)",
            reason = "Income certificate expires in 28 days — application on hold.",
            isUrgent = true,
            actionText = "Renew Certificate"
        ),
        PendingAction(
            id = "act_nos_defect",
            title = "Resolve admission letter condition",
            scheme = "National Overseas Scholarship (NOS)",
            reason = "Clause 7(ii)(b) requires unconditional offer or language waiver proof.",
            isUrgent = true,
            actionText = "Fix Deficiency"
        )
    )

    val notifications = listOf(
        NotificationItem(
            id = "notif_01",
            title = "Scholarship Payment Disbursed",
            body = "₹24,500 successfully credited to SBI A/c •••• 4920 via PFMS APB for PMS-ST.",
            time = "2 hours ago",
            isUnread = true,
            type = "PAYMENT"
        ),
        NotificationItem(
            id = "notif_02",
            title = "Document Expiry Warning",
            body = "Your Annual Income Certificate OD/INC/2025/11093 will expire in 28 days.",
            time = "Yesterday",
            isUnread = true,
            type = "EXPIRY"
        ),
        NotificationItem(
            id = "notif_03",
            title = "State Nodal Verification Complete",
            body = "Top Class Education application verified with 96% DigiLocker match score.",
            time = "3 days ago",
            isUnread = false,
            type = "VERIFICATION"
        ),
        NotificationItem(
            id = "notif_04",
            title = "NOS Deficiency Flagged",
            body = "Scrutiny team issued Defect Code D-402 for National Overseas Scholarship.",
            time = "1 week ago",
            isUnread = false,
            type = "DEFICIENCY"
        )
    )

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

    val applications = listOf(
        ApplicationRecord(
            applicationId = "NSP-2025-PMS-74921",
            schemeId = "POST_MATRIC",
            schemeTitle = "Post-Matric Scholarship for ST",
            academicYear = "2025-26",
            sourcePortal = "NSP (National Scholarship Portal)",
            stage = "DISBURSED",
            stageText = "Disbursed via DBT",
            currentStepIndex = 3,
            sanctionAmount = 24500,
            nextActionText = "Auto-Renewal active for 2026-27",
            verificationConfidence = 96,
            steps = listOf(
                TimelineStep("Submitted", "12-Aug-2025", true, "Applied via STeP Unified Portal"),
                TimelineStep("Verified", "24-Aug-2025", true, "Institute & State Nodal verified (96% DigiLocker match)"),
                TimelineStep("Sanctioned", "15-Dec-2025", true, "Order: MoTA/PMS/25-26/1842"),
                TimelineStep("Disbursed", "12-Feb-2026", true, "UTR: RBI492810488219 (SBI Bank)")
            ),
            dbtDetails = DbtDetails(
                utr = "RBI492810488219",
                paymentMode = "Aadhaar Payment Bridge (APB / PFMS)",
                disbursedDate = "12-Feb-2026",
                bankName = "State Bank of India",
                accountNo = "•••• •••• 4920",
                centralShare = "₹18,375 (75%)",
                stateShare = "₹6,125 (25%)",
                status = "CREDITED_SUCCESSFULLY"
            ),
            deficiency = null
        ),
        ApplicationRecord(
            applicationId = "SFMP-2026-TC-09312",
            schemeId = "TOP_CLASS",
            schemeTitle = "Top Class Education (Premier Institutes)",
            academicYear = "2026-27",
            sourcePortal = "SFMP (Canara Bank / MoTA)",
            stage = "SANCTIONED",
            stageText = "Sanctioned - In PFMS Queue",
            currentStepIndex = 2,
            sanctionAmount = 185000,
            nextActionText = "PFMS token released, awaiting bank clearing",
            verificationConfidence = 94,
            steps = listOf(
                TimelineStep("Submitted", "10-Jul-2026", true, "Auto-filled via DigiLocker + JEE"),
                TimelineStep("Verified", "18-Jul-2026", true, "Dean, IIT Bhubaneswar & Nodal Officer"),
                TimelineStep("Sanctioned", "02-Sep-2026", true, "Sanction Order MoTA/TC/0894"),
                TimelineStep("Disbursed", "Est. 10-Oct-2026", false, "PFMS token released, bank clearing")
            ),
            dbtDetails = DbtDetails(
                utr = "PFMS_BATCH_621_PENDING",
                paymentMode = "PFMS Direct Benefit Transfer",
                disbursedDate = "Pending Bank Clearing",
                bankName = "State Bank of India",
                accountNo = "•••• •••• 4920",
                centralShare = "₹1,85,000 (100%)",
                stateShare = "₹0 (0%)",
                status = "IN_PFMS_QUEUE"
            ),
            deficiency = null
        ),
        ApplicationRecord(
            applicationId = "NOS-2027-INT-0042",
            schemeId = "NOS",
            schemeTitle = "National Overseas Scholarship (NOS)",
            academicYear = "2027-28",
            sourcePortal = "Standalone NOS Portal",
            stage = "DEFICIENCY_FLAGGED",
            stageText = "Pending Action (Deficiency)",
            currentStepIndex = 1,
            sanctionAmount = 2200000,
            nextActionText = "Upload unconditional offer or English waiver",
            verificationConfidence = 78,
            steps = listOf(
                TimelineStep("Submitted", "20-Aug-2026", true, "Uploaded IELTS 7.5 & Offer"),
                TimelineStep("Verified", "28-Aug-2026", false, "Flagged with Defect Code D-402 (Conditional Offer)"),
                TimelineStep("Sanctioned", "Pending Cure", false, "Selection Committee review"),
                TimelineStep("Disbursed", "Pending", false, "Direct foreign bank transfer")
            ),
            dbtDetails = null,
            deficiency = DeficiencyInfo(
                code = "D-402",
                bureaucraticReason = "Conditional admission offer letter submitted. Under MoTA NOS Guidelines Clause 7(ii)(b), scholarship cannot be sanctioned on conditional admission offers lacking tuition deposit proof.",
                deadlineDate = "15-Oct-2026",
                daysRemaining = 16
            )
        )
    )

    val sampleCasteDoc = ScannedDocument(
        id = "doc_st_01",
        documentType = "Scheduled Tribe (ST) Certificate",
        candidateName = "Birsa Munda",
        fatherName = "Kanhu Munda",
        certificateNumber = "OD/ST/2022/49201",
        issuingAuthority = "Tehsildar Baripada, Mayurbhanj, Odisha",
        issueDate = "14-Jun-2022",
        validity = "Permanent / Lifetime",
        casteCommunity = "Santhal (Scheduled Tribe)",
        annualIncome = null,
        confidenceScore = 98,
        autoApproveEligible = true,
        sharedHostingUrl = "https://dhaaga.thecoolestportfolio.site/uploads/caste_OD_ST_2022_49201.jpg",
        syncedToFirebase = true
    )

    val sampleIncomeDoc = ScannedDocument(
        id = "doc_inc_02",
        documentType = "Annual Family Income Certificate",
        candidateName = "Birsa Munda",
        fatherName = "Kanhu Munda",
        certificateNumber = "OD/INC/2025/11093",
        issuingAuthority = "Revenue Officer, Baripada, Odisha",
        issueDate = "25-Oct-2025",
        validity = "Expires in 28 Days (25-Oct-2026)",
        isExpired = false,
        casteCommunity = null,
        annualIncome = "₹ 1,45,000 / annum",
        confidenceScore = 95,
        autoApproveEligible = true,
        sharedHostingUrl = "https://dhaaga.thecoolestportfolio.site/uploads/income_OD_INC_2025_11093.jpg",
        syncedToFirebase = true
    )

    val sampleMarksheetDoc = ScannedDocument(
        id = "doc_mark_03",
        documentType = "Class 12 Board Marksheet",
        candidateName = "Birsa Munda",
        fatherName = "Kanhu Munda",
        certificateNumber = "CHSE-2025-881924",
        issuingAuthority = "Council of Higher Secondary Education, Odisha",
        issueDate = "28-May-2025",
        validity = "Permanent",
        confidenceScore = 99,
        autoApproveEligible = true,
        sharedHostingUrl = "https://dhaaga.thecoolestportfolio.site/uploads/marksheet_chse_881924.jpg",
        syncedToFirebase = true
    )
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

    val sampleCasteDoc get() = MoTaDefaults.sampleCasteDoc
    val sampleIncomeDoc get() = MoTaDefaults.sampleIncomeDoc
    val sampleMarksheetDoc get() = MoTaDefaults.sampleMarksheetDoc
}
