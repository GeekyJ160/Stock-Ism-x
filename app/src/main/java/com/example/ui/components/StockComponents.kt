package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.*
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StockRowCard(
    instrument: Instrument,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPositive = instrument.changePct >= 0
    val changeColor = if (isPositive) GainGreen else LossRed
    val changeBg = if (isPositive) GainGreenBg else LossRedBg

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stock_card_${instrument.ticker}")
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ticker & Company Name
            Column(modifier = Modifier.weight(1.1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = instrument.ticker,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = RobotoMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .background(SurfaceElevated, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = instrument.sector.name.take(4),
                            style = MaterialTheme.typography.labelSmall,
                            color = BrandTeal
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = instrument.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Mini Sparkline
            Box(
                modifier = Modifier
                    .width(64.dp)
                    .height(28.dp)
                    .padding(horizontal = 4.dp)
            ) {
                if (instrument.sparkline.isNotEmpty()) {
                    Sparkline(
                        values = instrument.sparkline,
                        modifier = Modifier.fillMaxSize(),
                        isPositive = isPositive
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Live Price & % Change
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.weight(0.9f)
            ) {
                Text(
                    text = String.format(Locale.US, "$%.2f", instrument.price),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = RobotoMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(3.dp))
                Box(
                    modifier = Modifier
                        .background(changeBg, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%s%.2f%%", if (isPositive) "+" else "", instrument.changePct),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = RobotoMonoFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = changeColor
                        )
                    )
                }
            }

            // Favorite star
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("favorite_button_${instrument.ticker}")
            ) {
                Icon(
                    imageVector = if (instrument.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (instrument.isFavorite) HeatAmber else TextFaint,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SignalCard(
    signal: Signal,
    instrument: Instrument?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val badgeColor = when (signal.kind) {
        SignalKind.MomentumSetup -> SignalMomentum
        SignalKind.VolatilitySpike -> SignalVolatility
        SignalKind.SectorRotation -> SignalSector
        SignalKind.PatternMatch -> SignalPattern
        SignalKind.RiskWarning -> SignalRisk
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("signal_card_${signal.id}")
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Signal Kind Badge
                Box(
                    modifier = Modifier
                        .background(badgeColor.copy(alpha = 0.18f), RoundedCornerShape(6.dp))
                        .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = signal.kind.title,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = badgeColor
                    )
                }

                // Probability Score Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Confidence: ",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Text(
                        text = "${signal.probability}%",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (signal.probability >= 75) GainGreen else HeatAmber
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Ticker & Setup Headline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${signal.ticker} · ${instrument?.name ?: "Stock"}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = signal.volumeStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = BrandTeal
                    )
                }

                instrument?.let {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = String.format(Locale.US, "$%.2f", it.price),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = RobotoMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = String.format(Locale.US, "%s%.2f%%", if (it.changePct >= 0) "+" else "", it.changePct),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = RobotoMonoFontFamily,
                                color = if (it.changePct >= 0) GainGreen else LossRed
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Note snippet
            Text(
                text = signal.note,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Meter
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pattern Match ${signal.pattern}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    modifier = Modifier.width(110.dp)
                )
                LinearProgressIndicator(
                    progress = { signal.pattern / 100f },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = badgeColor,
                    trackColor = SurfaceElevated
                )
            }
        }
    }
}

@Composable
fun NewsItemCard(
    news: MarketNews,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sentimentColor = when (news.sentiment) {
        Sentiment.Bullish -> GainGreen
        Sentiment.Bearish -> LossRed
        Sentiment.Neutral -> BrandTeal
    }
    val sentimentBg = when (news.sentiment) {
        Sentiment.Bullish -> GainGreenBg
        Sentiment.Bearish -> LossRedBg
        Sentiment.Neutral -> Color(0x1F06B6D4)
    }

    val timeAgo = remember(news.datetime) {
        val diffMin = ((System.currentTimeMillis() - news.datetime) / 60000L).coerceAtLeast(1)
        if (diffMin < 60) "${diffMin}m ago" else "${diffMin / 60}h ago"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("news_card_${news.id}")
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(SurfaceElevated, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = news.ticker,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = RobotoMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = BrandTeal
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = news.source,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }

                Box(
                    modifier = Modifier
                        .background(sentimentBg, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = news.sentiment.name,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = sentimentColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = news.headline,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (news.summary.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = news.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = timeAgo,
                style = MaterialTheme.typography.labelSmall,
                color = TextFaint
            )
        }
    }
}

@Composable
fun TradeDialog(
    instrument: Instrument,
    onDismiss: () -> Unit,
    onConfirm: (shares: Double, price: Double) -> Unit
) {
    var sharesText by remember { mutableStateOf("10") }
    var priceText by remember { mutableStateOf(String.format(Locale.US, "%.2f", instrument.price)) }

    val shares = sharesText.toDoubleOrNull() ?: 0.0
    val price = priceText.toDoubleOrNull() ?: instrument.price
    val totalCost = shares * price

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceElevated),
            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorderLight))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Add Position · ${instrument.ticker}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = instrument.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = sharesText,
                    onValueChange = { sharesText = it },
                    label = { Text("Number of Shares") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("shares_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryNeon,
                        unfocusedBorderColor = SurfaceBorder
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Average Buy Price ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("price_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryNeon,
                        unfocusedBorderColor = SurfaceBorder
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Outlay:", style = MaterialTheme.typography.bodyMedium, color = TextMuted)
                    Text(
                        text = String.format(Locale.US, "$%.2f", totalCost),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = RobotoMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = PrimaryNeon
                        )
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (shares > 0 && price > 0) {
                                onConfirm(shares, price)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                        modifier = Modifier.testTag("confirm_position_button")
                    ) {
                        Text("Add Position", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
