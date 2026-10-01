package com.step.app.core.statemachine

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 11 Canonical Lifecycle States of the STeP Sovereign Scholarship System
 * Complies with MoTA, NSP, SFMP, and NeGD statutory workflow definitions.
 */
enum class CanonicalApplicationState(
    val code: String,
    val title: String,
    val hindiTitle: String,
    val stepIndex: Int,
    val description: String
) {
    DISCOVERED(
        code = "DISCOVERED",
        title = "Scheme Discovered",
        hindiTitle = "योजना खोजी गई",
        stepIndex = 0,
        description = "Candidate profile matched against scheme eligibility criteria."
    ),
    ELIGIBILITY_CHECKED(
        code = "ELIGIBILITY_CHECKED",
        title = "Eligibility Verified",
        hindiTitle = "पात्रता सत्यापित",
        stepIndex = 0,
        description = "Full 6-pillar document audit completed with eligible status."
    ),
    DRAFT(
        code = "DRAFT",
        title = "Application Drafted",
        hindiTitle = "आवेदन प्रारूप",
        stepIndex = 0,
        description = "Form started; candidate currently compiling documents."
    ),
    SUBMITTED(
        code = "SUBMITTED",
        title = "Officially Submitted",
        hindiTitle = "सफलतापूर्वक जमा किया गया",
        stepIndex = 1,
        description = "Application submitted with statutory DPDP consent and digital signature."
    ),
    INSTITUTION_VERIFIED(
        code = "INSTITUTION_VERIFIED",
        title = "Institution Verified (L1)",
        hindiTitle = "संस्थान द्वारा सत्यापित (L1)",
        stepIndex = 2,
        description = "School / College / University Nodal Officer verified bonafide and fee structure."
    ),
    NODAL_VERIFIED(
        code = "NODAL_VERIFIED",
        title = "State Nodal Verified (L2)",
        hindiTitle = "राज्य नोडल अधिकारी द्वारा सत्यापित (L2)",
        stepIndex = 3,
        description = "District / State Welfare Department approved caste, income, and domicile validity."
    ),
    DEFICIENCY(
        code = "DEFICIENCY",
        title = "Deficiency Flagged",
        hindiTitle = "त्रुटि / कमी दर्ज की गई",
        stepIndex = 2,
        description = "Scrutiny officer raised an inquiry or document clarification request."
    ),
    DEFICIENCY_DEFENSE(
        code = "DEFICIENCY_DEFENSE",
        title = "Statutory Defense Contested",
        hindiTitle = "सांविधिक बचाव / अपील दर्ज",
        stepIndex = 2,
        description = "Candidate or JAGO auto-contested deficiency citing official MoTA circulars."
    ),
    RESOLVED(
        code = "RESOLVED",
        title = "Deficiency Resolved",
        hindiTitle = "त्रुटि का समाधान हुआ",
        stepIndex = 2,
        description = "Clarification, fresh certificate, or legal affidavit accepted for re-scrutiny."
    ),
    SANCTIONED(
        code = "SANCTIONED",
        title = "Sanction Order Issued",
        hindiTitle = "स्वीकृति आदेश जारी",
        stepIndex = 4,
        description = "Ministry of Tribal Affairs issued sovereign sanction order and fund allocation."
    ),
    PAYMENT_INITIATED(
        code = "PAYMENT_INITIATED",
        title = "PFMS Payment Initiated",
        hindiTitle = "PFMS भुगतान प्रक्रिया में",
        stepIndex = 4,
        description = "Payment batch pushed to National Payments Corporation of India (NPCI) APBS bridge."
    ),
    DBT_SUCCESS(
        code = "DBT_SUCCESS",
        title = "DBT Credited to Bank",
        hindiTitle = "DBT सीधे बैंक खाते में जमा",
        stepIndex = 5,
        description = "Direct Benefit Transfer credited directly to Aadhaar-seeded bank account with UTR confirmation."
    ),
    REJECTED(
        code = "REJECTED",
        title = "Application Ineligible / Rejected",
        hindiTitle = "आवेदन अस्वीकृत",
        stepIndex = -1,
        description = "Application does not satisfy statutory scheme guidelines."
    );

    companion object {
        fun fromString(value: String): CanonicalApplicationState {
            val upper = value.uppercase().trim()
            return entries.firstOrNull { it.code == upper || it.name == upper } ?: SUBMITTED
        }
    }
}

/**
 * Immutable historical record of every statutory state change.
 */
data class StateTransitionEvent(
    val fromState: CanonicalApplicationState,
    val toState: CanonicalApplicationState,
    val timestamp: String,
    val actor: String,                  // "STUDENT", "INSTITUTE_OFFICER", "NODAL_OFFICER", "MOTA_ADMIN", "JAGO_AUTOPILOT"
    val reasonOrRemark: String,
    val referenceDocumentHash: String? = null
)

/**
 * ApplicationStateMachine — Enforces canonical state transitions, preventing invalid or illegal state leaps.
 */
object ApplicationStateMachine {

    private val validTransitions: Map<CanonicalApplicationState, Set<CanonicalApplicationState>> = mapOf(
        CanonicalApplicationState.DISCOVERED to setOf(CanonicalApplicationState.ELIGIBILITY_CHECKED, CanonicalApplicationState.DRAFT),
        CanonicalApplicationState.ELIGIBILITY_CHECKED to setOf(CanonicalApplicationState.DRAFT, CanonicalApplicationState.REJECTED),
        CanonicalApplicationState.DRAFT to setOf(CanonicalApplicationState.SUBMITTED),
        CanonicalApplicationState.SUBMITTED to setOf(CanonicalApplicationState.INSTITUTION_VERIFIED, CanonicalApplicationState.DEFICIENCY, CanonicalApplicationState.REJECTED),
        CanonicalApplicationState.INSTITUTION_VERIFIED to setOf(CanonicalApplicationState.NODAL_VERIFIED, CanonicalApplicationState.DEFICIENCY, CanonicalApplicationState.REJECTED),
        CanonicalApplicationState.NODAL_VERIFIED to setOf(CanonicalApplicationState.SANCTIONED, CanonicalApplicationState.DEFICIENCY, CanonicalApplicationState.REJECTED),
        CanonicalApplicationState.DEFICIENCY to setOf(CanonicalApplicationState.DEFICIENCY_DEFENSE, CanonicalApplicationState.RESOLVED, CanonicalApplicationState.REJECTED),
        CanonicalApplicationState.DEFICIENCY_DEFENSE to setOf(CanonicalApplicationState.RESOLVED, CanonicalApplicationState.NODAL_VERIFIED, CanonicalApplicationState.REJECTED),
        CanonicalApplicationState.RESOLVED to setOf(CanonicalApplicationState.INSTITUTION_VERIFIED, CanonicalApplicationState.NODAL_VERIFIED),
        CanonicalApplicationState.SANCTIONED to setOf(CanonicalApplicationState.PAYMENT_INITIATED),
        CanonicalApplicationState.PAYMENT_INITIATED to setOf(CanonicalApplicationState.DBT_SUCCESS, CanonicalApplicationState.DEFICIENCY),
        CanonicalApplicationState.DBT_SUCCESS to emptySet(),
        CanonicalApplicationState.REJECTED to setOf(CanonicalApplicationState.DEFICIENCY_DEFENSE) // Can contest rejection
    )

    fun canTransition(from: CanonicalApplicationState, to: CanonicalApplicationState): Boolean {
        if (from == to) return true
        return validTransitions[from]?.contains(to) == true
    }

    fun createTransitionEvent(
        from: CanonicalApplicationState,
        to: CanonicalApplicationState,
        actor: String,
        remark: String,
        docHash: String? = null
    ): StateTransitionEvent {
        val dateFormat = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss 'IST'", Locale.getDefault())
        return StateTransitionEvent(
            fromState = from,
            toState = to,
            timestamp = dateFormat.format(Date()),
            actor = actor,
            reasonOrRemark = remark,
            referenceDocumentHash = docHash
        )
    }
}
