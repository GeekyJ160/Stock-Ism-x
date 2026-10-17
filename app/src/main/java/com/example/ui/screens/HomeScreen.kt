package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.ui.unit.sp
import com.example.data.model.Instrument
import com.example.ui.MainScreen
import com.example.ui.StockViewModel
import com.example.ui.components.NewsItemCard
import com.example.ui.components.SignalCard
import com.example.ui.components.Sparkline
import com.example.ui.components.StockRowCard
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: StockViewModel,
    modifier: Modifier = Modifier
) {
    val instruments by viewModel.repository.instruments.collectAsState()
    val signals by viewModel.repository.signals.collectAsState()
    val news by viewModel.repository.news.collectAsState()
    val pulse by viewModel.repository.marketPulse.collectAsState()
    val apiStatus by viewModel.repository.apiStatusMessage.collectAsState()
    val isRefreshing by viewModel.repository.isRefreshing.collectAsState()

    val spyInstrument = instruments.find { it.ticker == "SPY" } ?: instruments.firstOrNull()
    val topSignals = signals.take(3)
    val movers = instruments.sortedByDescending { kotlin.math.abs(it.changePct) }.take(6)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_column"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Market Tape Status Header
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(GainGreen, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "SYSTEM ONLINE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = GainGreen
                            )
                        }

                        IconButton(
                            onClick = { viewModel.refreshAll() },
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = if (isRefreshing) PrimaryNeon else TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = pulse.summary,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = pulse.advanceDecline,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )

                        Text(
                            text = apiStatus,
                            style = MaterialTheme.typography.labelSmall,
                            color = BrandTeal
                        )
                    }

                    // Benchmark SPY Sparkline
                    spyInstrument?.let { spy ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceElevated, RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "S&P 500 (SPY)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = String.format(Locale.US, "$%.2f (%s%.2f%%)", spy.price, if (spy.changePct >= 0) "+" else "", spy.changePct),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = RobotoMonoFontFamily,
                                        color = if (spy.changePct >= 0) GainGreen else LossRed
                                    )
                                )
                            }

                            Box(modifier = Modifier.width(90.dp).height(24.dp)) {
                                if (spy.sparkline.isNotEmpty()) {
                                    Sparkline(
                                        values = spy.sparkline,
                                        modifier = Modifier.fillMaxSize(),
                                        isPositive = spy.changePct >= 0
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active Movers Carousel
        item {
            Column {
                Text(
                    text = "Active Market Movers",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(movers, key = { it.ticker }) { inst ->
                        Card(
                            modifier = Modifier
                                .width(140.dp)
                                .clickable { viewModel.selectStock(inst.ticker) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = inst.ticker,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontFamily = RobotoMonoFontFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = String.format(Locale.US, "%s%.1f%%", if (inst.changePct >= 0) "+" else "", inst.changePct),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = RobotoMonoFontFamily,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (inst.changePct >= 0) GainGreen else LossRed
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = String.format(Locale.US, "$%.2f", inst.price),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = RobotoMonoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(modifier = Modifier.fillMaxWidth().height(20.dp)) {
                                    Sparkline(
                                        values = inst.sparkline,
                                        modifier = Modifier.fillMaxSize(),
                                        isPositive = inst.changePct >= 0
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Today's Key Signals Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Key Signals",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                TextButton(onClick = { viewModel.navigateTo(MainScreen.Signals) }) {
                    Text("View All", color = BrandTeal)
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = BrandTeal,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        items(topSignals, key = { it.id }) { sig ->
            val inst = instruments.find { it.ticker == sig.ticker }
            SignalCard(
                signal = sig,
                instrument = inst,
                onClick = { viewModel.selectStock(sig.ticker) }
            )
        }

        // Live Market News Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Market News & Sentiment",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
        }

        items(news.take(4), key = { it.id }) { item ->
            NewsItemCard(
                news = item,
                onClick = { viewModel.selectStock(item.ticker) }
            )
        }
    }
}
