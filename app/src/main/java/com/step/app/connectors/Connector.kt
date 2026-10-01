package com.step.app.connectors

import com.step.app.core.statemachine.CanonicalApplicationState

/**
 * Standard Connector Result returned by all Government System Adapters.
 */
data class ConnectorResult(
    val success: Boolean,
    val externalReferenceNumber: String,
    val sourcePortal: String,
    val mappedState: CanonicalApplicationState,
    val responsePayloadJson: String,
    val isSandboxSimulation: Boolean,
    val statutoryTimestamp: String = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.getDefault()).format(java.util.Date()),
    val diagnosticMessage: String
)

/**
 * Connector — Common integration contract for external government systems.
 * Provides abstraction across NSP, SFMP, NOS, DigiLocker, and PFMS.
 */
interface GovernmentConnector {
    val portalName: String
    val portalCode: String
    val isProductionReady: Boolean

    suspend fun submitApplication(
        applicationId: String,
        candidateUid: String,
        schemeCode: String,
        payloadJson: String
    ): ConnectorResult

    suspend fun queryApplicationStatus(
        applicationId: String,
        externalReference: String
    ): ConnectorResult
}
