package com.step.admin.data

data class AdminScheme(
    val id: String,
    val code: String,
    val title: String,
    val hindiTitle: String = "",
    val portal: String,
    val targetClass: String,
    val incomeCeiling: Long?,
    val benefitSummary: String,
    val maxBenefitAmount: Long,
    val benefitAmountFormatted: String,
    val deadlineFormatted: String,
    val isActive: Boolean = true,
    val rules: List<String> = emptyList()
)

data class AdminApplication(
    val id: String,
    val docPath: String = "",
    val userId: String = "",
    val studentName: String,
    val district: String,
    val state: String = "Odisha",
    val scheme: String,
    val portal: String,
    val confidenceScore: Int,
    val status: String, // "SUBMITTED", "AUTO_APPROVED", "SANCTIONED", "DISBURSED", "DEFICIENCY_FLAGGED"
    val source: String,
    val docType: String,
    val appliedDate: String,
    val sanctionAmount: Long,
    val stage: String,
    val stageText: String,
    val anomaly: String? = null,
    val certificateNumber: String = "",
    val digilockerVerified: Boolean = true
)

data class AdminStudent(
    val uid: String,
    val fullName: String,
    val apaarId: String,
    val digilockerId: String,
    val email: String,
    val state: String,
    val district: String,
    val community: String = "Scheduled Tribe (ST)",
    val subTribe: String,
    val educationLevel: String,
    val institution: String,
    val annualIncome: Long,
    val bankName: String,
    val maskedAccount: String,
    val npciAadhaarSeeded: Boolean
)

data class AdminDocument(
    val id: String,
    val documentType: String,
    val candidateName: String,
    val fatherName: String,
    val certificateNumber: String,
    val issuingAuthority: String,
    val issueDate: String,
    val validity: String,
    val confidenceScore: Int,
    val autoApproveEligible: Boolean,
    val sharedHostingUrl: String = "",
    val digilockerXml: String = "",
    val signerCn: String = "Digital Tehsildar DSC",
    val dscSerialNumber: String = "",
    val pkiTimestamp: String = "",
    val rollNumber: String = "NAS",
    val passingYear: String = "NAS",
    val boardName: String = "NAS",
    val marksPercentage: Double = 0.0,
    val marksObtained: Int = 0,
    val maxMarks: Int = 0,
    val annualIncome: String = "NAS",
    val casteCommunity: String = "NAS"
)

data class HeatmapDistrict(
    val district: String,
    val state: String,
    val enrolledSt: Int,
    val activeScholarships: Int,
    val gapCount: Int,
    val gapPercent: Double,
    val riskLevel: String, // "CRITICAL", "MODERATE", "OPTIMAL"
    val recommendedAction: String
)

data class AdminKpis(
    val totalScholars: Int = 0,
    val activeSchemes: Int = 0,
    val totalDbtDisbursedCr: Double = 0.0,
    val pendingScrutinyCases: Int = 0
)
