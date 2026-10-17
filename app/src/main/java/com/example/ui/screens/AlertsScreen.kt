package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.StockViewModel
import com.example.ui.theme.*

@Composable
fun AlertsScreen(
    viewModel: StockViewModel,
    modifier: Modifier = Modifier
) {
    val alerts by viewModel.repository.alerts.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("alerts_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "System Alerts & Intel",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Text(
                        text = "Real-time volatility and momentum alerts",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                TextButton(
                    onClick = { viewModel.markAllAlertsRead() },
                    modifier = Modifier.testTag("mark_all_read_button")
                ) {
                    Icon(Icons.Default.DoneAll, contentDescription = null, tint = BrandTeal, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Mark Read", color = BrandTeal)
                }
            }
        }

        if (alerts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No active alerts. The algorithmic engine is monitoring the tape.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }
        } else {
            items(alerts, key = { it.id }) { alert ->
                val badgeColor = when (alert.category) {
                    "momentum" -> SignalMomentum
                    "volatility" -> SignalVolatility
                    "sector" -> SignalSector
                    "risk" -> SignalRisk
                    else -> SignalPattern
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            viewModel.markAlertRead(alert.id)
                            viewModel.selectStock(alert.ticker)
                        },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (alert.isRead) SurfaceDark.copy(alpha = 0.6f) else SurfaceDark
                    ),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (alert.isRead) SurfaceBorder else badgeColor.copy(alpha = 0.5f)
                        )
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        // Unread Dot
                        Box(
                            modifier = Modifier
                                .padding(top = 5.dp, end = 10.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (alert.isRead) Color.Transparent else badgeColor)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = alert.ticker,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = RobotoMonoFontFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(badgeColor.copy(alpha = 0.18f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = alert.category.uppercase(),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = badgeColor
                                        )
                                    }
                                }

                                Text(
                                    text = "${alert.minutesAgo}m ago",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextFaint
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = alert.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            )

                            Spacer(modifier = Modifier.height(3.dp))

                            Text(
                                text = alert.body,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
