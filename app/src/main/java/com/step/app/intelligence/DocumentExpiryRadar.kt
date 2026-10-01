package com.step.app.intelligence

import com.step.app.data.ScannedDocument
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class ExpiryUrgency {
    NORMAL,
    EXPIRING_SOON,   // <= 30 days
    CRITICAL,        // <= 15 days
    EXPIRED          // <= 0 days
}

data class ExpiryAlert(
    val docId: String,
    val documentTitle: String,
    val certificateNumber: String,
    val expiryDate: String,
    val daysRemaining: Long,
    val urgency: ExpiryUrgency,
    val actionRequired: String,
    val autoRenewableViaDigiLocker: Boolean = true
)

/**
 * DocumentExpiryRadar — Proactive watchdog for tribal scholar certificate validity.
 * Automatically watches expiration windows for annual Income Certificates, Caste validity,
 * and hostel renewals to prevent mid-year scholarship cancellation.
 */
object DocumentExpiryRadar {

    fun scanDocumentsForExpiry(documents: List<ScannedDocument>): List<ExpiryAlert> {
        val alerts = mutableListOf<ExpiryAlert>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val altDateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val now = Date()

        for (doc in documents) {
            if (doc.validity.contains("Permanent", ignoreCase = true) || doc.validity.contains("Lifetime", ignoreCase = true)) {
                continue
            }

            val expiryString = extractDateString(doc.validity)
            val parsedExpiry = try {
                dateFormat.parse(expiryString) ?: altDateFormat.parse(expiryString)
            } catch (_: Exception) {
                null
            }

            if (parsedExpiry != null) {
                val diffMillis = parsedExpiry.time - now.time
                val daysRemaining = TimeUnit.MILLISECONDS.toDays(diffMillis)

                val urgency = when {
                    daysRemaining <= 0 -> ExpiryUrgency.EXPIRED
                    daysRemaining <= 15 -> ExpiryUrgency.CRITICAL
                    daysRemaining <= 30 -> ExpiryUrgency.EXPIRING_SOON
                    else -> ExpiryUrgency.NORMAL
                }

                if (urgency != ExpiryUrgency.NORMAL) {
                    val action = when (urgency) {
                        ExpiryUrgency.EXPIRED -> "Expired! Tap to fetch updated FY certificate from DigiLocker instantly."
                        ExpiryUrgency.CRITICAL -> "Expiring in $daysRemaining days! Renew now to prevent statutory DBT hold."
                        ExpiryUrgency.EXPIRING_SOON -> "Certificate expires within a month. Early renewal recommended."
                        else -> "Certificate valid."
                    }

                    alerts.add(
                        ExpiryAlert(
                            docId = doc.id,
                            documentTitle = doc.documentType,
                            certificateNumber = doc.certificateNumber,
                            expiryDate = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault()).format(parsedExpiry),
                            daysRemaining = daysRemaining,
                            urgency = urgency,
                            actionRequired = action,
                            autoRenewableViaDigiLocker = true
                        )
                    )
                }
            }
        }
        return alerts.sortedBy { it.daysRemaining }
    }

    private fun extractDateString(text: String): String {
        val match = Regex("\\d{4}-\\d{2}-\\d{2}|\\d{2}/\\d{2}/\\d{4}").find(text)
        return match?.value ?: text.trim()
    }
}
