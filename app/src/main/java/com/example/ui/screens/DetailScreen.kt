package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.StockSeedData
import com.example.ui.StockViewModel
import com.example.ui.components.InteractiveStockChart
import com.example.ui.components.NewsItemCard
import com.example.ui.components.TradeDialog
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun DetailScreen(
    viewModel: StockViewModel,
    modifier: Modifier = Modifier
) {
    val selectedTicker by viewModel.selectedTicker.collectAsState()
    val instruments by viewModel.repository.instruments.collectAsState()
    val signals by viewModel.repository.signals.collectAsState()
    val timeFrame by viewModel.selectedTimeFrame.collectAsState()
    val chartPoints by viewModel.chartPoints.collectAsState()
    val scrubbedPoint by viewModel.selectedChartPoint.collectAsState()
    val isChartLoading by viewModel.isChartLoading.collectAsState()
    val stockNews by viewModel.stockNews.collectAsState()
    val isNewsLoading by viewModel.isNewsLoading.collectAsState()
    val profile by viewModel.repository.profile.collectAsState()

    var showTradeDialog by remember { mutableStateOf(false) }

    val instrument = instruments.find { it.ticker == selectedTicker }
        ?: StockSeedData.INSTRUMENTS.first { it.ticker == "NVDA" }
    val signal = signals.find { it.ticker == selectedTicker }

    val activePrice = scrubbedPoint?.price ?: instrument.price
    val isPositive = instrument.changePct >= 0

    // Trade Dialog
    if (showTradeDialog) {
        TradeDialog(
            instrument = instrument,
            onDismiss = { showTradeDialog = false },
            onConfirm = { shares, price ->
                viewModel.addTradePosition(instrument.ticker, shares, price)
                showTradeDialog = false
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("detail_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back Button & Action Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier.testTag("back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.toggleFavorite(instrument.ticker) },
                        modifier = Modifier.testTag("favorite_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (instrument.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = "Favorite",
                            tint = if (instrument.isFavorite) HeatAmber else TextMuted
                        )
                    }

                    Button(
                        onClick = { showTradeDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("trade_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Position", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Stock Header & Live Price Banner
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = instrument.ticker,
                                    style = MaterialTheme.typography.displayMedium.copy(
                                        fontFamily = RobotoMonoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .background(BrandTeal.copy(alpha = 0.18f), RoundedCornerShape(6.dp))
                                        .border(1.dp, BrandTeal.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = instrument.sector.label,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = BrandTeal
                                    )
                                }
                            }
                            Text(
                                text = instrument.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextSecondary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .background(if (isPositive) GainGreenBg else LossRedBg, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = String.format(Locale.US, "%s%.2f%%", if (isPositive) "+" else "", instrument.changePct),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = RobotoMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPositive) GainGreen else LossRed
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Big Live Price Display
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = String.format(Locale.US, "$%.2f", activePrice),
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontFamily = RobotoMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    fontSize = 36.sp
                                )
                            )
                            scrubbedPoint?.let { point ->
                                Text(
                                    text = "Scrubbing: ${point.dateLabel} · Vol: ${point.volume}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = BrandTeal
                                )
                            }
                        }

                        Text(
                            text = "Live Real-Time Feed",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // TimeFrame Selector Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(SurfaceElevated, RoundedCornerShape(10.dp))
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TimeFrame.values().forEach { tf ->
                            val isSelected = timeFrame == tf
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) PrimaryNeon else Color.Transparent)
                                    .clickable { viewModel.setTimeFrame(tf) }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = tf.label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = RobotoMonoFontFamily,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.Black else TextSecondary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Interactive Chart
                    if (isChartLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(230.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = PrimaryNeon, modifier = Modifier.size(36.dp))
                        }
                    } else {
                        InteractiveStockChart(
                            points = chartPoints,
                            isPositive = isPositive,
                            onScrub = { point -> viewModel.setScrubbedPoint(point) },
                            modifier = Modifier.testTag("interactive_chart")
                        )
                    }
                }
            }
        }

        // Fundamental Metrics Grid
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Market Statistics",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MetricItem("Day Open", String.format(Locale.US, "$%.2f", instrument.open))
                        MetricItem("Day High", String.format(Locale.US, "$%.2f", instrument.high))
                        MetricItem("Day Low", String.format(Locale.US, "$%.2f", instrument.low))
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        MetricItem("Market Cap", instrument.marketCapStr)
                        MetricItem("P/E Ratio", String.format(Locale.US, "%.1f", instrument.peRatio))
                        MetricItem("Beta (5Y)", String.format(Locale.US, "%.2f", instrument.beta))
                    }
                }
            }
        }

        // Algorithmic Signal Analysis
        if (signal != null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Algorithmic Setup Analysis",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                            Box(
                                modifier = Modifier
                                    .background(PrimaryNeon.copy(alpha = 0.18f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = signal.kind.title,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = PrimaryNeon
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = signal.note,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Stats Row
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            MetricItem("Trend Direction", signal.trend.label)
                            MetricItem("Win Probability", "${signal.probability}%")
                            MetricItem("Risk Level", signal.riskLevel.name)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Commentary Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceElevated, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Quantitative Commentary",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = BrandTeal
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "“${signal.commentary}”",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Capital Sizing Suggestion
                        val suggestedAllocation = (profile.capital * profile.risk.multiplier)
                        val maxShares = if (instrument.price > 0) (suggestedAllocation / instrument.price).toInt() else 0
                        Text(
                            text = "Capital Mode (${profile.risk.label}): Suggested max position is $maxShares shares (~$${suggestedAllocation.toInt()}) based on active risk tolerance.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                }
            }
        }

        // Relevant Market News for this stock
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Market News for ${instrument.ticker}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                if (isNewsLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), color = BrandTeal, strokeWidth = 2.dp)
                }
            }
        }

        if (stockNews.isEmpty()) {
            item {
                Text(
                    text = "No recent headline news available for ${instrument.ticker}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
        } else {
            items(stockNews, key = { it.id }) { newsItem ->
                NewsItemCard(
                    news = newsItem,
                    onClick = { /* View news detail / link */ }
                )
            }
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = RobotoMonoFontFamily,
                fontWeight = FontWeight.SemiBold
            ),
            color = TextPrimary
        )
    }
}
