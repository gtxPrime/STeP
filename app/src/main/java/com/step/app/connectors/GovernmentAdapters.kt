package com.step.app.connectors

import com.step.app.core.statemachine.CanonicalApplicationState

/**
 * NSPConnector — Adapter for National Scholarship Portal (NSP 2.0).
 * Handles Pre-Matric (SCH-01) and Post-Matric (SCH-02).
 */
class NSPConnector : GovernmentConnector {
    override val portalName = "National Scholarship Portal (NSP)"
    override val portalCode = "NSP"
    override val isProductionReady = false // Tagged pending central API gateway whitelisting

    override suspend fun submitApplication(
        applicationId: String,
        candidateUid: String,
        schemeCode: String,
        payloadJson: String
    ): ConnectorResult {
        return ConnectorResult(
            success = true,
            externalReferenceNumber = "NSP/2026/ST/${applicationId.takeLast(6)}",
            sourcePortal = portalName,
            mappedState = CanonicalApplicationState.SUBMITTED,
            responsePayloadJson = """{"status": "ACK", "message": "NSP Gateway Submission Queued"}""",
            isSandboxSimulation = true,
            diagnosticMessage = "Connector-ready architecture; external submission adapter pending central MoTA API gateway whitelisting."
        )
    }

    override suspend fun queryApplicationStatus(
        applicationId: String,
        externalReference: String
    ): ConnectorResult {
        return ConnectorResult(
            success = true,
            externalReferenceNumber = externalReference,
            sourcePortal = portalName,
            mappedState = CanonicalApplicationState.INSTITUTION_VERIFIED,
            responsePayloadJson = """{"status": "IN_PROGRESS", "level": "L1_INSTITUTE"}""",
            isSandboxSimulation = true,
            diagnosticMessage = "Status queried from NSP Sandbox listener."
        )
    }
}

/**
 * SFMPConnector — Adapter for Canara Bank Scheme Fellowship Management Portal (SFMP).
 * Handles Top Class Education (SCH-03) and National Fellowship (SCH-04 / NFST).
 */
class SFMPConnector : GovernmentConnector {
    override val portalName = "Canara Bank SFMP (Scholarship Portal)"
    override val portalCode = "SFMP"
    override val isProductionReady = false

    override suspend fun submitApplication(
        applicationId: String,
        candidateUid: String,
        schemeCode: String,
        payloadJson: String
    ): ConnectorResult {
        return ConnectorResult(
            success = true,
            externalReferenceNumber = "SFMP-CANARA-2026-${(10000..99999).random()}",
            sourcePortal = portalName,
            mappedState = CanonicalApplicationState.SUBMITTED,
            responsePayloadJson = """{"canaraRef": "OK", "ack": true}""",
            isSandboxSimulation = true,
            diagnosticMessage = "SFMP Adapter Queued. Live API connection pending bank integration token."
        )
    }

    override suspend fun queryApplicationStatus(
        applicationId: String,
        externalReference: String
    ): ConnectorResult {
        return ConnectorResult(
            success = true,
            externalReferenceNumber = externalReference,
            sourcePortal = portalName,
            mappedState = CanonicalApplicationState.NODAL_VERIFIED,
            responsePayloadJson = """{"canaraStatus": "VERIFIED"}""",
            isSandboxSimulation = true,
            diagnosticMessage = "SFMP status query simulated via sandbox adapter."
        )
    }
}

/**
 * NOSConnector — Adapter for Standalone National Overseas Scholarship Portal (SCH-05).
 */
class NOSConnector : GovernmentConnector {
    override val portalName = "Standalone NOS Portal (MoTA)"
    override val portalCode = "NOS"
    override val isProductionReady = false

    override suspend fun submitApplication(
        applicationId: String,
        candidateUid: String,
        schemeCode: String,
        payloadJson: String
    ): ConnectorResult {
        return ConnectorResult(
            success = true,
            externalReferenceNumber = "NOS/OVERSEAS/2026-${(1000..9999).random()}",
            sourcePortal = portalName,
            mappedState = CanonicalApplicationState.SUBMITTED,
            responsePayloadJson = """{"nos_id": "ST_GLOBAL_2026"}""",
            isSandboxSimulation = true,
            diagnosticMessage = "NOS standalone submission adapter active in sandbox mode."
        )
    }

    override suspend fun queryApplicationStatus(
        applicationId: String,
        externalReference: String
    ): ConnectorResult {
        return ConnectorResult(
            success = true,
            externalReferenceNumber = externalReference,
            sourcePortal = portalName,
            mappedState = CanonicalApplicationState.NODAL_VERIFIED,
            responsePayloadJson = """{"nos_status": "INTERVIEW_CLEARED"}""",
            isSandboxSimulation = true,
            diagnosticMessage = "NOS status synchronized."
        )
    }
}

/**
 * PFMSConnector — Adapter for Public Financial Management System & NPCI APBS Bridge.
 */
class PFMSConnector : GovernmentConnector {
    override val portalName = "Public Financial Management System (PFMS / NPCI APBS)"
    override val portalCode = "PFMS"
    override val isProductionReady = false

    override suspend fun submitApplication(
        applicationId: String,
        candidateUid: String,
        schemeCode: String,
        payloadJson: String
    ): ConnectorResult {
        return ConnectorResult(
            success = true,
            externalReferenceNumber = "PFMS-BATCH-${(100000..999999).random()}",
            sourcePortal = portalName,
            mappedState = CanonicalApplicationState.PAYMENT_INITIATED,
            responsePayloadJson = """{"batchStatus": "QUEUED_FOR_APB"}""",
            isSandboxSimulation = true,
            diagnosticMessage = "Simulated DBT payment status; pending live PFMS bridge credentials."
        )
    }

    override suspend fun queryApplicationStatus(
        applicationId: String,
        externalReference: String
    ): ConnectorResult {
        val utr = "UTR-RBIND2026${(10000000..99999999).random()}"
        return ConnectorResult(
            success = true,
            externalReferenceNumber = utr,
            sourcePortal = portalName,
            mappedState = CanonicalApplicationState.DBT_SUCCESS,
            responsePayloadJson = """{"utr": "$utr", "dbtStatus": "CREDITED"}""",
            isSandboxSimulation = true,
            diagnosticMessage = "Simulated disbursement workflow confirmed with simulated UTR $utr."
        )
    }
}

/**
 * ConnectorRegistry — Factory providing the appropriate adapter based on portal code.
 */
object ConnectorRegistry {
    private val adapters = mapOf(
        "NSP" to NSPConnector(),
        "SFMP" to SFMPConnector(),
        "NOS" to NOSConnector(),
        "PFMS" to PFMSConnector()
    )

    fun getConnector(portalCode: String): GovernmentConnector {
        return adapters[portalCode.uppercase().trim()] ?: NSPConnector()
    }
}
