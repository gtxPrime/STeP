package com.step.admin.data

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

object AdminRepository {

    var kpis by mutableStateOf(
        AdminKpis(
            totalScholars = 2,
            activeSchemes = 5,
            totalDbtDisbursedCr = 0.043,
            pendingScrutinyCases = 2
        )
    )

    val schemes = mutableStateListOf(
        AdminScheme(
            id = "PRE_MATRIC",
            code = "SCH-01",
            title = "Pre-Matric Scholarship for ST Students",
            portal = "National Scholarship Portal (NSP)",
            targetClass = "Class IX & X",
            incomeCeiling = 250000,
            benefitSummary = "₹3,500 (Day Scholar) / ₹7,000 (Hosteller) + ₹1,000 Books",
            maxBenefitAmount = 8000,
            benefitAmountFormatted = "₹8,000/yr",
            deadlineFormatted = "31 Dec 2026",
            rules = listOf("Enrolled in recognized government or affiliated school", "Family income <= 2.50L")
        ),
        AdminScheme(
            id = "POST_MATRIC",
            code = "SCH-02",
            title = "Post-Matric Scholarship for ST Students",
            portal = "National Scholarship Portal (NSP)",
            targetClass = "Class XI, XII, UG, PG",
            incomeCeiling = 250000,
            benefitSummary = "100% Tuition Waiver + Maintenance up to ₹13,500/yr",
            maxBenefitAmount = 35000,
            benefitAmountFormatted = "Tuition + ₹13,500/yr",
            deadlineFormatted = "15 Jan 2027",
            rules = listOf("Class 11 to Post-Graduate courses", "75:25 Central to State funding ratio")
        ),
        AdminScheme(
            id = "TOP_CLASS",
            code = "SCH-03",
            title = "National Scholarship for Higher Education (Top Class)",
            portal = "SFMP / Canara Bank Portal",
            targetClass = "Premier Institutes (IIT, IIM, NIT, AIIMS, NLU)",
            incomeCeiling = 600000,
            benefitSummary = "Full Tuition + ₹3,000/mo Living + ₹45,000 Laptop Grant",
            maxBenefitAmount = 286000,
            benefitAmountFormatted = "Full Tuition + ₹2.86L",
            deadlineFormatted = "28 Feb 2027",
            rules = listOf("Notified 265 Premier Institutes", "Family income <= 6.00L")
        ),
        AdminScheme(
            id = "NFST",
            code = "SCH-04",
            title = "National Fellowship for Higher Education of ST Students",
            portal = "SFMP / Canara Bank Portal",
            targetClass = "Regular M.Phil & Ph.D Scholars",
            incomeCeiling = null,
            benefitSummary = "JRF: ₹31,000/mo | SRF: ₹35,000/mo + HRA + ₹25,000 Contingency",
            maxBenefitAmount = 450000,
            benefitAmountFormatted = "₹35,000/mo + Grants",
            deadlineFormatted = "30 Nov 2026",
            rules = listOf("UGC/CSIR NET qualified", "NO family income limit applies")
        ),
        AdminScheme(
            id = "NOS",
            code = "SCH-05",
            title = "National Overseas Scholarship for ST Students",
            portal = "Standalone NOS Portal (overseas.tribal.gov.in)",
            targetClass = "Top 500 QS World Ranking Foreign Universities",
            incomeCeiling = 800000,
            benefitSummary = "Full Tuition + £9,900 (UK)/$15,400 (US) + Return Airfare",
            maxBenefitAmount = 2200000,
            benefitAmountFormatted = "100% Tuition + £9,900/yr",
            deadlineFormatted = "15 Jan 2027",
            rules = listOf("Acceptance in Top 500 QS Foreign University", "3 slots reserved for PVTGs")
        )
    )

    val applications = mutableStateListOf(
        AdminApplication(
            id = "APP-MOTA-2026-001",
            userId = "100556049023018388845",
            studentName = "Garvit Sharma",
            district = "Mayurbhanj",
            state = "Odisha",
            scheme = "Post-Matric Scholarship for ST Students",
            portal = "National Scholarship Portal (NSP)",
            confidenceScore = 96,
            status = "PENDING",
            source = "NeGD DigiLocker Auto-Fetch",
            docType = "Caste & Income Certificates",
            appliedDate = "30 Sep 2026",
            sanctionAmount = 35000,
            stage = "SCRUTINY",
            stageText = "100% DSC Verified • Auto-Approval Recommended",
            certificateNumber = "OD/ST/2022/49201",
            digilockerVerified = true
        ),
        AdminApplication(
            id = "APP-MOTA-2026-002",
            userId = "109887084186858187932",
            studentName = "gtx prime",
            district = "Mayurbhanj",
            state = "Odisha",
            scheme = "Pre-Matric Scholarship for ST Students",
            portal = "National Scholarship Portal (NSP)",
            confidenceScore = 98,
            status = "SANCTIONED",
            source = "NeGD DigiLocker Auto-Fetch",
            docType = "Caste Certificate",
            appliedDate = "29 Sep 2026",
            sanctionAmount = 8000,
            stage = "SANCTIONED",
            stageText = "Sanction Order Generated • APBS Bridge Ready",
            certificateNumber = "OD/ST/2023/11092",
            digilockerVerified = true
        )
    )

    val students = mutableStateListOf(
        AdminStudent(
            uid = "100556049023018388845",
            fullName = "Garvit Sharma",
            apaarId = "APAAR-2026-9842-1082",
            digilockerId = "DL-MOTA-98421",
            email = "notopic234232@gmail.com",
            state = "Odisha",
            district = "Mayurbhanj",
            community = "Scheduled Tribe (ST)",
            subTribe = "Oraon",
            educationLevel = "Higher Secondary (Science)",
            institution = "Govt. Autonomous College, Baripada",
            annualIncome = 145000L,
            bankName = "State Bank of India (PFMS Active)",
            maskedAccount = "•••• •••• 4819",
            npciAadhaarSeeded = true
        ),
        AdminStudent(
            uid = "109887084186858187932",
            fullName = "gtx prime",
            apaarId = "APAAR-2026-4819-2041",
            digilockerId = "DL-MOTA-48192",
            email = "gtxprime.com@gmail.com",
            state = "Odisha",
            district = "Mayurbhanj",
            community = "Scheduled Tribe (ST)",
            subTribe = "Santhal",
            educationLevel = "Secondary (Class X)",
            institution = "Mayurbhanj Higher Secondary School",
            annualIncome = 120000L,
            bankName = "Punjab National Bank (PFMS Active)",
            maskedAccount = "•••• •••• 9211",
            npciAadhaarSeeded = true
        )
    )

    val documents = mutableStateListOf(
        AdminDocument(
            id = "DOC-OD-001",
            documentType = "Scheduled Tribe (ST) Certificate",
            candidateName = "Garvit Sharma",
            fatherName = "Guardian (Oraon)",
            certificateNumber = "OD/ST/2022/49201",
            issuingAuthority = "Tehsildar Baripada, Mayurbhanj, Odisha",
            issueDate = "14-Jun-2022",
            validity = "Permanent / Lifetime",
            confidenceScore = 98,
            autoApproveEligible = true,
            casteCommunity = "Oraon (Scheduled Tribe)",
            signerCn = "Pradeep Kumar Jena, Revenue Dept Odisha",
            dscSerialNumber = "0x6A3F9B2C4E01",
            pkiTimestamp = "01-Oct-2026 04:38:38 IST"
        ),
        AdminDocument(
            id = "DOC-OD-002",
            documentType = "Income Certificate",
            candidateName = "Garvit Sharma",
            fatherName = "Guardian (Oraon)",
            certificateNumber = "OD/INC/2026/88129",
            issuingAuthority = "Revenue Officer, Mayurbhanj",
            issueDate = "10-Jul-2026",
            validity = "Valid for FY 2026-27",
            confidenceScore = 95,
            autoApproveEligible = true,
            annualIncome = "₹ 1,45,000 / Year",
            signerCn = "Sub-Divisional Magistrate, Baripada",
            dscSerialNumber = "0x98EF2104ABCD"
        ),
        AdminDocument(
            id = "DOC-OD-003",
            documentType = "Higher Secondary Marksheet (Class XII)",
            candidateName = "Garvit Sharma",
            fatherName = "Guardian (Oraon)",
            certificateNumber = "CHSE/2025/77810",
            issuingAuthority = "Council of Higher Secondary Education, Odisha",
            issueDate = "25-May-2025",
            validity = "Permanent",
            confidenceScore = 99,
            autoApproveEligible = true,
            rollNumber = "12SC-984210",
            passingYear = "2025",
            boardName = "CHSE Odisha",
            marksPercentage = 87.0,
            marksObtained = 435,
            maxMarks = 500
        ),
        AdminDocument(
            id = "DOC-OD-004",
            documentType = "Scheduled Tribe (ST) Certificate",
            candidateName = "gtx prime",
            fatherName = "Guardian (Santhal)",
            certificateNumber = "OD/ST/2023/11092",
            issuingAuthority = "Tehsildar Rairangpur, Mayurbhanj, Odisha",
            issueDate = "19-Aug-2023",
            validity = "Permanent / Lifetime",
            confidenceScore = 98,
            autoApproveEligible = true,
            casteCommunity = "Santhal (Scheduled Tribe)",
            signerCn = "Tehsildar Rairangpur DSC",
            dscSerialNumber = "0x44BC881023EF"
        )
    )


    val heatmapDistricts = mutableStateListOf(
        HeatmapDistrict(
            district = "Bastar",
            state = "Chhattisgarh",
            enrolledSt = 48200,
            activeScholarships = 18940,
            gapCount = 29260,
            gapPercent = 60.7,
            riskLevel = "CRITICAL",
            recommendedAction = "Deploy Mobile Enrollment Van & Ashram School camps"
        ),
        HeatmapDistrict(
            district = "Gadchiroli",
            state = "Maharashtra",
            enrolledSt = 34500,
            activeScholarships = 14320,
            gapCount = 20180,
            gapPercent = 58.5,
            riskLevel = "CRITICAL",
            recommendedAction = "Coordinate with Zilla Parishad & Tribal Welfare Officer"
        ),
        HeatmapDistrict(
            district = "Mayurbhanj",
            state = "Odisha",
            enrolledSt = 62100,
            activeScholarships = 34650,
            gapCount = 27450,
            gapPercent = 44.2,
            riskLevel = "MODERATE",
            recommendedAction = "Automate DigiLocker camp issuance for Santhal habitations"
        ),
        HeatmapDistrict(
            district = "Dahod",
            state = "Gujarat",
            enrolledSt = 41200,
            activeScholarships = 24100,
            gapCount = 17100,
            gapPercent = 41.5,
            riskLevel = "MODERATE",
            recommendedAction = "Issue push SMS alerts in Gujarati & Bhili dialects"
        ),
        HeatmapDistrict(
            district = "Paschim Medinipur",
            state = "West Bengal",
            enrolledSt = 29800,
            activeScholarships = 21400,
            gapCount = 8400,
            gapPercent = 28.2,
            riskLevel = "OPTIMAL",
            recommendedAction = "Monitor renewal progression via APAAR ID matching"
        )
    )

    fun sanctionApplication(appId: String): Boolean {
        val idx = applications.indexOfFirst { it.id == appId }
        if (idx != -1) {
            val app = applications[idx]
            applications[idx] = app.copy(
                status = "SANCTIONED",
                stage = "SANCTIONED",
                stageText = "Sanction Order Generated by Ministry",
                confidenceScore = 99
            )
            kpis = kpis.copy(
                pendingScrutinyCases = (kpis.pendingScrutinyCases - 1).coerceAtLeast(0)
            )
            return true
        }
        return false
    }

    fun disburseApplication(appId: String): Boolean {
        val idx = applications.indexOfFirst { it.id == appId }
        if (idx != -1) {
            val app = applications[idx]
            val utr = "RBI${System.currentTimeMillis().toString().takeLast(9)}"
            applications[idx] = app.copy(
                status = "DISBURSED",
                stage = "DISBURSED",
                stageText = "Disbursed via APB (UTR: $utr)"
            )
            kpis = kpis.copy(
                totalDbtDisbursedCr = kpis.totalDbtDisbursedCr + (app.sanctionAmount / 10000000.0)
            )
            return true
        }
        return false
    }

    fun flagDeficiency(appId: String, reason: String): Boolean {
        val idx = applications.indexOfFirst { it.id == appId }
        if (idx != -1) {
            val app = applications[idx]
            applications[idx] = app.copy(
                status = "DEFICIENCY_FLAGGED",
                stage = "DEFICIENCY_FLAGGED",
                stageText = "Defect Flagged by Officer",
                anomaly = reason
            )
            return true
        }
        return false
    }

    fun addScheme(scheme: AdminScheme) {
        schemes.add(0, scheme)
        kpis = kpis.copy(activeSchemes = schemes.size)
    }
}
