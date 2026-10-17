package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApiProvider
import com.example.data.model.RiskMode
import com.example.ui.StockViewModel
import com.example.ui.theme.*
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: StockViewModel,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.repository.profile.collectAsState()
    val apiStatus by viewModel.repository.apiStatusMessage.collectAsState()
    val testResult by viewModel.testApiResult.collectAsState()

    var finnhubKeyInput by remember(profile.customFinnhubKey) { mutableStateOf(profile.customFinnhubKey) }
    var alphaKeyInput by remember(profile.customAlphaVantageKey) { mutableStateOf(profile.customAlphaVantageKey) }
    var selectedProvider by remember(profile.activeProvider) { mutableStateOf(profile.activeProvider) }

    var capitalInput by remember(profile.capital) { mutableStateOf(profile.capital.toInt().toString()) }
    var selectedRisk by remember(profile.risk) { mutableStateOf(profile.risk) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Back Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.navigateBack() },
                    modifier = Modifier.testTag("settings_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Engine & API Settings",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            }
        }

        // Live Market Engine Status Banner
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
                            text = "Market Feed Status",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                        Box(
                            modifier = Modifier
                                .background(GainGreenBg, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "ACTIVE",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = GainGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = apiStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    testResult?.let { res ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceElevated, RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = res,
                                style = MaterialTheme.typography.labelSmall.copy(fontFamily = RobotoMonoFontFamily),
                                color = PrimaryNeon
                            )
                        }
                    }
                }
            }
        }

        // Real-Time Stock Data API Configuration Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VpnKey, contentDescription = null, tint = BrandTeal, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Real-Time Stock Data API",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Connect Finnhub or Alpha Vantage API keys for live institutional quotes, intraday candles, and financial news.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Provider Selector
                    Text(
                        text = "Active Data Provider",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        ApiProvider.values().forEach { provider ->
                            val isSelected = selectedProvider == provider
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (isSelected) SurfaceElevated else Color.Transparent,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .border(
                                        1.dp,
                                        if (isSelected) BrandTeal else SurfaceBorder,
                                        RoundedCornerShape(8.dp)
                                    )
                                    .clickable { selectedProvider = provider }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = provider.displayName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    color = if (isSelected) TextPrimary else TextSecondary
                                )
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = BrandTeal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Finnhub Key Field
                    OutlinedTextField(
                        value = finnhubKeyInput,
                        onValueChange = { finnhubKeyInput = it },
                        label = { Text("Finnhub API Key") },
                        placeholder = { Text("e.g. c123456... (free on finnhub.io)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("finnhub_key_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandTeal,
                            unfocusedBorderColor = SurfaceBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Alpha Vantage Key Field
                    OutlinedTextField(
                        value = alphaKeyInput,
                        onValueChange = { alphaKeyInput = it },
                        label = { Text("Alpha Vantage API Key") },
                        placeholder = { Text("e.g. ALPHAVANTAGE_KEY (alphavantage.co)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("alpha_key_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrandTeal,
                            unfocusedBorderColor = SurfaceBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.testApiConnection() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("test_api_button")
                        ) {
                            Text("Test Ping", color = BrandTeal)
                        }

                        Button(
                            onClick = {
                                viewModel.saveApiKeys(finnhubKeyInput, alphaKeyInput, selectedProvider)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeon),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("save_api_button")
                        ) {
                            Text("Save & Apply", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Trading Profile & Risk Setting
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(SurfaceBorder))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Trading Profile & Capital Mode",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = capitalInput,
                        onValueChange = { capitalInput = it },
                        label = { Text("Total Available Capital ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryNeon,
                            unfocusedBorderColor = SurfaceBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Algorithmic Risk Mode",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        RiskMode.values().forEach { mode ->
                            val isSelected = selectedRisk == mode
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        if (isSelected) PrimaryNeon else SurfaceElevated,
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedRisk = mode }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = mode.label,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.Black else TextPrimary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            val cap = capitalInput.toDoubleOrNull() ?: profile.capital
                            viewModel.saveProfileConfig(cap, selectedRisk)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Update Risk Profile", color = TextPrimary)
                    }
                }
            }
        }
    }
}
