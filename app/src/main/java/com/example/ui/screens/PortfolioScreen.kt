package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import com.example.data.model.Position
import com.example.ui.StockViewModel
import com.example.ui.components.StockRowCard
import com.example.ui.components.TradeDialog
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun PortfolioScreen(
    viewModel: StockViewModel,
    modifier: Modifier = Modifier
) {
    val instruments by viewModel.repository.instruments.collectAsState()
    val positions by viewModel.repository.positions.collectAsState()
    val profile by viewModel.repository.profile.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Holdings, 1: Watchlist
    var showTradeDialog by remember { mutableStateOf(false) }
    var selectedStockForTrade by remember { mutableStateOf(instruments.firstOrNull()) }

    val summary = remember(instruments, positions, profile) {
        viewModel.getPortfolioSummary(instruments, positions, profile)
    }

    val favorites = instruments.filter { it.isFavorite }

    if (showTradeDialog && selectedStockForTrade != null) {
        TradeDialog(
            instrument = selectedStockForTrade!!,
            onDismiss = { showTradeDialog = false },
            onConfirm = { shares, price ->
                viewModel.addTradePosition(selectedStockForTrade!!.ticker, shares, price)
                showTradeDialog = false
            }
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("portfolio_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Portfolio Summary Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Total Portfolio Balance",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = String.format(Locale.US, "$%,.2f", summary.totalBalance),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontFamily = RobotoMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val isProfit = summary.totalProfitLoss >= 0
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .background(if (isProfit) GainGreenBg else LossRedBg, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = String.format(
                                    Locale.US,
                                    "%s$%,.2f (%s%.2f%%)",
                                    if (isProfit) "+" else "",
                                    summary.totalProfitLoss,
                                    if (isProfit) "+" else "",
                                    summary.totalProfitLossPct
                                ),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = RobotoMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isProfit) GainGreen else LossRed
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Unrealized P&L",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Invested Capital", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(
                                text = String.format(Locale.US, "$%,.2f", summary.totalInvested),
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = RobotoMonoFontFamily, fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Available Cash", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text(
                                text = String.format(Locale.US, "$%,.2f", summary.availableCash),
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = RobotoMonoFontFamily, fontWeight = FontWeight.SemiBold),
                                color = PrimaryNeon
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Capital Allocation Bar
                    val allocPct = if (profile.capital > 0) (summary.totalInvested / profile.capital).toFloat().coerceIn(0f, 1f) else 0f
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Capital Allocation", style = MaterialTheme.typography.labelSmall, color = TextMuted)
                            Text("${(allocPct * 100).toInt()}% deployed", style = MaterialTheme.typography.labelSmall, color = BrandTeal)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { allocPct },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = BrandTeal,
                            trackColor = SurfaceElevated
                        )
                    }
                }
            }
        }

        // Segmented Tabs: Holdings vs Watchlist
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark, RoundedCornerShape(12.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 0) SurfaceElevated else Color.Transparent)
                        .clickable { selectedTab = 0 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Holdings (${positions.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) TextPrimary else TextMuted
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (selectedTab == 1) SurfaceElevated else Color.Transparent)
                        .clickable { selectedTab = 1 }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Watchlist (${favorites.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) TextPrimary else TextMuted
                        )
                    )
                }
            }
        }

        if (selectedTab == 0) {
            // Holdings List
            if (positions.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No open positions. Tap '+ Add Position' to log an investment.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                }
            } else {
                items(positions, key = { it.id }) { pos ->
                    val inst = instruments.find { it.ticker == pos.ticker }
                    val currentPrice = inst?.price ?: pos.avgPrice
                    val marketValue = pos.shares * currentPrice
                    val costBasis = pos.shares * pos.avgPrice
                    val pl = marketValue - costBasis
                    val plPct = if (costBasis > 0) (pl / costBasis) * 100.0 else 0.0
                    val isGain = pl >= 0

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.selectStock(pos.ticker) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = pos.ticker,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = RobotoMonoFontFamily,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = TextPrimary
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${pos.shares.toInt()} shs @ $${String.format(Locale.US, "%.2f", pos.avgPrice)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = inst?.name ?: "Stock",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = String.format(Locale.US, "$%,.2f", marketValue),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = RobotoMonoFontFamily,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = String.format(Locale.US, "%s$%.2f (%s%.1f%%)", if (isGain) "+" else "", pl, if (isGain) "+" else "", plPct),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontFamily = RobotoMonoFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isGain) GainGreen else LossRed
                                    )
                                )
                            }

                            IconButton(
                                onClick = { viewModel.removePosition(pos.id) },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Remove",
                                    tint = TextFaint,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Watchlist Tab
            if (favorites.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No stocks favorited. Tap the star icon on any stock card to add it to your watchlist.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted
                        )
                    }
                }
            } else {
                items(favorites, key = { it.ticker }) { inst ->
                    StockRowCard(
                        instrument = inst,
                        onClick = { viewModel.selectStock(inst.ticker) },
                        onToggleFavorite = { viewModel.toggleFavorite(inst.ticker) }
                    )
                }
            }
        }
    }
}
