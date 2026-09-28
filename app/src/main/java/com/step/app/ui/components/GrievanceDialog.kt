package com.step.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.step.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrievanceBottomSheet(
    onDismiss: () -> Unit,
    onSubmit: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = BackgroundWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = BorderMedium,
                height = 4.dp,
                width = 36.dp
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 6.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Surface(
                color = StatusPendingBg,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = "DIRECT BENEFIT TRANSFER ESCALATION",
                    color = StatusPending,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Lodge DBT Payment Grievance",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                color = SurfaceCard,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight)
            ) {
                Text(
                    text = "Under the MoTA Citizen Charter, all DBT non-credit grievances must be resolved within 30 calendar days with automated bank reconciliation.",
                    fontSize = 11.sp,
                    color = TextBody,
                    lineHeight = 16.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text("Application ID", fontSize = 10.sp, color = TextSubtle, fontWeight = FontWeight.SemiBold)
            Text("NSP-2025-PMS-74921", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = TextDark)

            Spacer(modifier = Modifier.height(8.dp))

            Text("PFMS / Bank UTR Reference", fontSize = 10.sp, color = TextSubtle, fontWeight = FontWeight.SemiBold)
            Text("RBI492810488219", fontSize = 12.sp, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace, color = PrimaryDeepOrange)

            Spacer(modifier = Modifier.height(14.dp))

            // SLA Clock Card
            Card(
                colors = CardDefaults.cardColors(containerColor = PrimarySurfaceLight),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = FontAwesomeIcons.Solid.Clock,
                            contentDescription = null,
                            tint = PrimaryDeepOrange,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ministry SLA Resolution Window", fontSize = 10.sp, color = PrimaryDeepOrangeDark, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("30 Days Maximum Statutory SLA", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextDark)
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = PrimaryDeepOrange,
                        trackColor = BorderLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderMedium)
                ) {
                    Text("Cancel", color = TextBody, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = onSubmit,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryDeepOrange),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp)
                ) {
                    Text("Submit SLA Ticket", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Backward-compatible alias for existing references
@Composable
fun GrievanceDialog(onDismiss: () -> Unit, onSubmit: () -> Unit) =
    GrievanceBottomSheet(onDismiss = onDismiss, onSubmit = onSubmit)
