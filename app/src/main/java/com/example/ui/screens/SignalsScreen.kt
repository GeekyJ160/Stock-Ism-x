package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.SignalKind
import com.example.ui.StockViewModel
import com.example.ui.components.SignalCard
import com.example.ui.theme.*

@Composable
fun SignalsScreen(
    viewModel: StockViewModel,
    modifier: Modifier = Modifier
) {
    val signals by viewModel.repository.signals.collectAsState()
    val instruments by viewModel.repository.instruments.collectAsState()
    val selectedFilter by viewModel.selectedSignalKind.collectAsState()

    val filtered = if (selectedFilter == null) {
        signals
    } else {
        signals.filter { it.kind == selectedFilter }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("signals_screen")
    ) {
        // Filter Chips Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { viewModel.setSignalKindFilter(null) },
                    label = { Text("All Setups (${signals.size})") },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryNeon,
                        selectedLabelColor = androidx.compose.ui.graphics.Color.Black,
                        containerColor = SurfaceDark,
                        labelColor = TextSecondary
                    )
                )
            }

            items(SignalKind.values()) { kind ->
                val count = signals.count { it.kind == kind }
                FilterChip(
                    selected = selectedFilter == kind,
                    onClick = {
                        viewModel.setSignalKindFilter(if (selectedFilter == kind) null else kind)
                    },
                    label = { Text("${kind.title} ($count)") },
                    shape = RoundedCornerShape(20.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandTeal,
                        selectedLabelColor = androidx.compose.ui.graphics.Color.Black,
                        containerColor = SurfaceDark,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        // Signals List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "High-Probability Algorithmic Setups",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = "System scans price action, volume anomalies, and pattern matches across monitored stocks.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(filtered, key = { it.id }) { sig ->
                val inst = instruments.find { it.ticker == sig.ticker }
                SignalCard(
                    signal = sig,
                    instrument = inst,
                    onClick = { viewModel.selectStock(sig.ticker) }
                )
            }
        }
    }
}
