package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.MarketCap
import com.example.data.model.Sector
import com.example.ui.StockViewModel
import com.example.ui.components.StockRowCard
import com.example.ui.theme.*

@Composable
fun ScannerScreen(
    viewModel: StockViewModel,
    modifier: Modifier = Modifier
) {
    val instruments by viewModel.repository.instruments.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedSector by viewModel.selectedSector.collectAsState()
    val selectedCap by viewModel.selectedCap.collectAsState()

    val filtered = instruments.filter { inst ->
        val matchesSearch = searchQuery.isBlank() ||
                inst.ticker.contains(searchQuery, ignoreCase = true) ||
                inst.name.contains(searchQuery, ignoreCase = true)
        val matchesSector = selectedSector == null || inst.sector == selectedSector
        val matchesCap = selectedCap == null || inst.cap == selectedCap
        matchesSearch && matchesSector && matchesCap
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("scanner_screen")
    ) {
        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            placeholder = { Text("Search by symbol or company name...", color = TextMuted) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = TextMuted)
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.setSearchQuery("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextMuted)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("scanner_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark,
                focusedBorderColor = PrimaryNeon,
                unfocusedBorderColor = SurfaceBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            )
        )

        // Sector Filter Chips Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedSector == null,
                    onClick = { viewModel.setSectorFilter(null) },
                    label = { Text("All Sectors") },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandTeal,
                        selectedLabelColor = androidx.compose.ui.graphics.Color.Black,
                        containerColor = SurfaceDark,
                        labelColor = TextSecondary
                    )
                )
            }
            items(Sector.values()) { sector ->
                FilterChip(
                    selected = selectedSector == sector,
                    onClick = {
                        viewModel.setSectorFilter(if (selectedSector == sector) null else sector)
                    },
                    label = { Text(sector.label) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = BrandTeal,
                        selectedLabelColor = androidx.compose.ui.graphics.Color.Black,
                        containerColor = SurfaceDark,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        // Market Cap Filter Chips Row
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedCap == null,
                    onClick = { viewModel.setCapFilter(null) },
                    label = { Text("All Caps") },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryNeon,
                        selectedLabelColor = androidx.compose.ui.graphics.Color.Black,
                        containerColor = SurfaceDark,
                        labelColor = TextSecondary
                    )
                )
            }
            items(MarketCap.values()) { cap ->
                FilterChip(
                    selected = selectedCap == cap,
                    onClick = {
                        viewModel.setCapFilter(if (selectedCap == cap) null else cap)
                    },
                    label = { Text(cap.label) },
                    shape = RoundedCornerShape(16.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = PrimaryNeon,
                        selectedLabelColor = androidx.compose.ui.graphics.Color.Black,
                        containerColor = SurfaceDark,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        // Result count header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${filtered.size} Instruments Monitored",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = TextSecondary
            )
            Text(
                text = "Live Ticks",
                style = MaterialTheme.typography.labelSmall,
                color = GainGreen
            )
        }

        // Scanner Instruments List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filtered, key = { it.ticker }) { inst ->
                StockRowCard(
                    instrument = inst,
                    onClick = { viewModel.selectStock(inst.ticker) },
                    onToggleFavorite = { viewModel.toggleFavorite(inst.ticker) }
                )
            }
        }
    }
}
