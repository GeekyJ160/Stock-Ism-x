package com.example.data.model

enum class Sector(val label: String) {
    Technology("Technology"),
    Energy("Energy"),
    Financials("Financials"),
    Healthcare("Healthcare"),
    Industrials("Industrials"),
    Consumer("Consumer"),
    Communication("Communication")
}

enum class MarketCap(val label: String) {
    Large("Large Cap"),
    Mid("Mid Cap"),
    Small("Small Cap")
}

enum class Trend(val label: String) {
    Up("Bullish"),
    Down("Bearish"),
    Sideways("Consolidation")
}

enum class Level {
    High, Medium, Low
}

enum class RiskMode(val label: String, val multiplier: Float) {
    Conservative("Conservative", 0.05f),
    Balanced("Balanced", 0.12f),
    Aggressive("Aggressive", 0.25f)
}

enum class SignalKind(val title: String, val category: String) {
    MomentumSetup("Momentum Setup", "momentum"),
    VolatilitySpike("Volatility Spike", "volatility"),
    SectorRotation("Sector Rotation", "sector"),
    PatternMatch("Pattern Match", "pattern"),
    RiskWarning("Risk Warning", "risk")
}

enum class TimeFrame(val label: String, val days: Int, val interval: String) {
    Day("1D", 1, "5m"),
    Week("1W", 7, "15m"),
    Month("1M", 30, "1d"),
    Year("1Y", 365, "1wk"),
    All("ALL", 1825, "1mo")
}

enum class Sentiment {
    Bullish, Bearish, Neutral
}

enum class ApiProvider(val displayName: String) {
    Auto("Auto (Multi-Source Live)"),
    Finnhub("Finnhub API"),
    AlphaVantage("Alpha Vantage API"),
    LiveFeed("Yahoo Live Stream")
}

data class Instrument(
    val ticker: String = "",
    val name: String,
    val sector: Sector,
    val cap: MarketCap,
    val price: Double,
    val changePct: Double,
    val open: Double = price * 0.99,
    val high: Double = price * 1.02,
    val low: Double = price * 0.98,
    val volume: Long = 14500000L,
    val marketCapStr: String = "$1.2T",
    val peRatio: Double = 28.5,
    val beta: Double = 1.15,
    val sparkline: List<Double> = emptyList(),
    val isFavorite: Boolean = false
)

data class HistoricalPoint(
    val timestamp: Long,
    val price: Double,
    val open: Double = price,
    val high: Double = price,
    val low: Double = price,
    val close: Double = price,
    val volume: Long = 0L,
    val dateLabel: String = ""
)

data class MarketNews(
    val id: String,
    val ticker: String,
    val headline: String,
    val summary: String,
    val source: String,
    val url: String = "",
    val imageUrl: String? = null,
    val datetime: Long = System.currentTimeMillis(),
    val category: String = "general",
    val sentiment: Sentiment = Sentiment.Neutral
)

data class Signal(
    val id: String,
    val ticker: String,
    val kind: SignalKind,
    val trend: Trend,
    val trendStrength: Level,
    val volatility: Level,
    val probability: Int, // 0 - 100
    val volumeStatus: String,
    val volumeCopy: String,
    val pattern: Int, // 0 - 100
    val riskLevel: Level,
    val note: String,
    val commentary: String
)

data class AlertItem(
    val id: String,
    val ticker: String,
    val category: String,
    val title: String,
    val minutesAgo: Int,
    val probability: Int,
    val body: String,
    val signalId: String? = null,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

data class Position(
    val id: String,
    val ticker: String,
    val shares: Double,
    val avgPrice: Double
)

data class UserProfile(
    val onboarded: Boolean = true,
    val capital: Double = 25000.0,
    val risk: RiskMode = RiskMode.Balanced,
    val categories: List<String> = listOf("momentum", "volatility", "sector", "pattern", "risk"),
    val intelEnabled: Boolean = true,
    val activeProvider: ApiProvider = ApiProvider.Auto,
    val customFinnhubKey: String = "",
    val customAlphaVantageKey: String = ""
)

data class MarketPulse(
    val status: String,
    val summary: String,
    val advanceDecline: String,
    val volatilityIndex: Double,
    val isLive: Boolean,
    val lastUpdated: Long
)
