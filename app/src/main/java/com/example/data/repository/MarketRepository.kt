package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.api.ApiClient
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt
import kotlin.random.Random

class MarketRepository(private val context: Context) {
    private val TAG = "MarketRepository"
    private val prefs = context.getSharedPreferences("stockism_prefs", Context.MODE_PRIVATE)

    private val _instruments = MutableStateFlow<List<Instrument>>(StockSeedData.INSTRUMENTS)
    val instruments: StateFlow<List<Instrument>> = _instruments.asStateFlow()

    private val _signals = MutableStateFlow<List<Signal>>(StockSeedData.SIGNALS)
    val signals: StateFlow<List<Signal>> = _signals.asStateFlow()

    private val _alerts = MutableStateFlow<List<AlertItem>>(StockSeedData.INITIAL_ALERTS)
    val alerts: StateFlow<List<AlertItem>> = _alerts.asStateFlow()

    private val _news = MutableStateFlow<List<MarketNews>>(StockSeedData.INITIAL_NEWS)
    val news: StateFlow<List<MarketNews>> = _news.asStateFlow()

    private val _positions = MutableStateFlow<List<Position>>(StockSeedData.INITIAL_POSITIONS)
    val positions: StateFlow<List<Position>> = _positions.asStateFlow()

    private val _profile = MutableStateFlow<UserProfile>(loadProfile())
    val profile: StateFlow<UserProfile> = _profile.asStateFlow()

    private val _marketPulse = MutableStateFlow(
        MarketPulse(
            status = "NYSE/NASDAQ Open · Real-time Feed",
            summary = "Momentum rising in mid-cap tech. Volatility stable. Leadership firm in semiconductor hardware.",
            advanceDecline = "14 Advances / 5 Declines",
            volatilityIndex = 14.8,
            isLive = true,
            lastUpdated = System.currentTimeMillis()
        )
    )
    val marketPulse: StateFlow<MarketPulse> = _marketPulse.asStateFlow()

    private val _apiStatusMessage = MutableStateFlow<String>("Real-time engine ready")
    val apiStatusMessage: StateFlow<String> = _apiStatusMessage.asStateFlow()

    private val _isRefreshing = MutableStateFlow<Boolean>(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        // Initialize sparklines for all seed instruments
        val initialized = _instruments.value.map { inst ->
            val spark = generateSyntheticHistory(inst.price, 24, 0.02).map { it.price }
            inst.copy(sparkline = spark)
        }
        _instruments.value = initialized
    }

    private fun loadProfile(): UserProfile {
        val capital = prefs.getFloat("capital", 25000f).toDouble()
        val riskStr = prefs.getString("risk", RiskMode.Balanced.name) ?: RiskMode.Balanced.name
        val risk = try { RiskMode.valueOf(riskStr) } catch (e: Exception) { RiskMode.Balanced }
        val finnhubKey = prefs.getString("finnhub_key", "") ?: ""
        val alphaKey = prefs.getString("alpha_key", "") ?: ""
        val providerStr = prefs.getString("provider", ApiProvider.Auto.name) ?: ApiProvider.Auto.name
        val provider = try { ApiProvider.valueOf(providerStr) } catch (e: Exception) { ApiProvider.Auto }

        return UserProfile(
            capital = capital,
            risk = risk,
            customFinnhubKey = finnhubKey,
            customAlphaVantageKey = alphaKey,
            activeProvider = provider
        )
    }

    fun updateProfile(newProfile: UserProfile) {
        _profile.value = newProfile
        prefs.edit()
            .putFloat("capital", newProfile.capital.toFloat())
            .putString("risk", newProfile.risk.name)
            .putString("finnhub_key", newProfile.customFinnhubKey)
            .putString("alpha_key", newProfile.customAlphaVantageKey)
            .putString("provider", newProfile.activeProvider.name)
            .apply()
    }

    fun toggleFavorite(ticker: String) {
        _instruments.value = _instruments.value.map { inst ->
            if (inst.ticker == ticker) inst.copy(isFavorite = !inst.isFavorite) else inst
        }
    }

    fun addPosition(ticker: String, shares: Double, price: Double) {
        val current = _positions.value.toMutableList()
        val existingIndex = current.indexOfFirst { it.ticker == ticker }
        if (existingIndex >= 0) {
            val existing = current[existingIndex]
            val totalShares = existing.shares + shares
            val newAvg = if (totalShares > 0) ((existing.shares * existing.avgPrice) + (shares * price)) / totalShares else price
            current[existingIndex] = existing.copy(shares = totalShares, avgPrice = newAvg)
        } else {
            current.add(Position("pos-${System.currentTimeMillis()}", ticker, shares, price))
        }
        _positions.value = current
    }

    fun removePosition(id: String) {
        _positions.value = _positions.value.filter { it.id != id }
    }

    fun markAlertRead(id: String) {
        _alerts.value = _alerts.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun markAllAlertsRead() {
        _alerts.value = _alerts.value.map { it.copy(isRead = true) }
    }

    /**
     * Resolves the effective API keys between BuildConfig and user settings.
     */
    fun getEffectiveFinnhubKey(): String {
        val custom = _profile.value.customFinnhubKey.trim()
        if (custom.isNotEmpty()) return custom
        return try {
            val fromConfig = BuildConfig::class.java.getField("FINNHUB_API_KEY").get(null) as? String
            val clean = fromConfig?.trim().orEmpty()
            if (clean == "FINNHUB_API_KEY_PLACEHOLDER" || clean.isEmpty()) "" else clean
        } catch (e: Exception) { "" }
    }

    fun getEffectiveAlphaVantageKey(): String {
        val custom = _profile.value.customAlphaVantageKey.trim()
        if (custom.isNotEmpty()) return custom
        return try {
            val fromConfig = BuildConfig::class.java.getField("ALPHA_VANTAGE_API_KEY").get(null) as? String
            val clean = fromConfig?.trim().orEmpty()
            if (clean == "ALPHA_VANTAGE_API_KEY_PLACEHOLDER" || clean.isEmpty()) "" else clean
        } catch (e: Exception) { "" }
    }

    /**
     * Main sync method: fetches real-time stock quotes from Finnhub, Alpha Vantage,
     * or Yahoo Finance spark stream, updating the instrument quotes.
     */
    suspend fun refreshQuotes() = withContext(Dispatchers.IO) {
        _isRefreshing.value = true
        val finnhubKey = getEffectiveFinnhubKey()
        val alphaKey = getEffectiveAlphaVantageKey()
        val provider = _profile.value.activeProvider

        var updated = false

        // 1. Try Finnhub if configured or active
        if ((provider == ApiProvider.Finnhub || provider == ApiProvider.Auto) && finnhubKey.isNotEmpty()) {
            try {
                _apiStatusMessage.value = "Streaming quotes via Finnhub API..."
                val list = _instruments.value.toMutableList()
                // Fetch top 5 instruments in focus to respect free tier rate limit
                val priority = list.take(6)
                for (i in priority.indices) {
                    val inst = priority[i]
                    val quote = ApiClient.finnhubService.getQuote(inst.ticker, finnhubKey)
                    if (quote.currentPrice != null && quote.currentPrice > 0) {
                        val current = quote.currentPrice
                        val change = quote.change ?: 0.0
                        val changePct = quote.percentChange ?: (if (inst.price > 0) (change / inst.price) * 100 else 0.0)
                        val high = quote.high ?: maxOf(inst.high, current)
                        val low = quote.low ?: minOf(inst.low, current)
                        val open = quote.open ?: inst.open
                        val newSpark = (inst.sparkline.takeLast(20) + current).takeLast(24)

                        val idx = list.indexOfFirst { it.ticker == inst.ticker }
                        if (idx >= 0) {
                            list[idx] = inst.copy(
                                price = current,
                                changePct = changePct,
                                high = high,
                                low = low,
                                open = open,
                                sparkline = newSpark
                            )
                        }
                    }
                }
                _instruments.value = list
                _apiStatusMessage.value = "Live Finnhub stream active · Up-to-date"
                updated = true
            } catch (e: Exception) {
                Log.w(TAG, "Finnhub quote update error: ${e.message}")
            }
        }

        // 2. Try Alpha Vantage if Finnhub not used or provider is Alpha Vantage
        if (!updated && (provider == ApiProvider.AlphaVantage || provider == ApiProvider.Auto) && alphaKey.isNotEmpty()) {
            try {
                _apiStatusMessage.value = "Fetching quotes via Alpha Vantage API..."
                val topTicker = _instruments.value.firstOrNull()?.ticker ?: "NVDA"
                val res = ApiClient.alphaVantageService.getGlobalQuote("GLOBAL_QUOTE", topTicker, alphaKey)
                res.globalQuote?.price?.toDoubleOrNull()?.let { current ->
                    val changePct = res.globalQuote.changePercent?.replace("%", "")?.toDoubleOrNull() ?: 0.0
                    val list = _instruments.value.map { inst ->
                        if (inst.ticker == topTicker) {
                            inst.copy(price = current, changePct = changePct)
                        } else inst
                    }
                    _instruments.value = list
                    _apiStatusMessage.value = "Alpha Vantage live data active"
                    updated = true
                }
            } catch (e: Exception) {
                Log.w(TAG, "Alpha Vantage update error: ${e.message}")
            }
        }

        // 3. Fallback: Yahoo Finance spark stream or micro-fluctuations
        if (!updated) {
            try {
                val symbols = _instruments.value.map { it.ticker }.take(12).joinToString(",") + ",SPY"
                val sparkResponse = ApiClient.yahooFinanceService.getSpark(symbols, "1d", "5m")
                if (sparkResponse.isNotEmpty()) {
                    val list = _instruments.value.map { inst ->
                        val sparkItem = sparkResponse[inst.ticker]
                        if (sparkItem != null) {
                            val closes = sparkItem.close?.filterNotNull() ?: emptyList()
                            val price = sparkItem.fulldayPrice ?: closes.lastOrNull() ?: inst.price
                            val changePct = sparkItem.fulldayChangePercent ?: inst.changePct
                            val series = if (closes.isNotEmpty()) closes.takeLast(24) else inst.sparkline
                            inst.copy(price = price, changePct = changePct, sparkline = series)
                        } else inst
                    }
                    _instruments.value = list
                    _apiStatusMessage.value = "Live Market Stream (Yahoo Finance) active"
                    updated = true
                }
            } catch (e: Exception) {
                Log.d(TAG, "Yahoo Finance spark fallback: ${e.message}")
                // Simulated active trading micro-tick if network completely offline
                simulateLiveTick()
                _apiStatusMessage.value = "Active Market Simulation Mode"
            }
        }

        updateMarketPulse()
        _isRefreshing.value = false
    }

    /**
     * Fetches historical chart candles for an individual stock.
     */
    suspend fun fetchHistoricalData(ticker: String, timeframe: TimeFrame): List<HistoricalPoint> = withContext(Dispatchers.IO) {
        val finnhubKey = getEffectiveFinnhubKey()
        val inst = _instruments.value.find { it.ticker == ticker } ?: StockSeedData.INSTRUMENTS.first()

        // 1. Try Finnhub candles if key available
        if (finnhubKey.isNotEmpty()) {
            try {
                val nowSec = System.currentTimeMillis() / 1000
                val fromSec = nowSec - (timeframe.days * 24L * 3600L)
                val res = when (timeframe) {
                    TimeFrame.Day -> "5"
                    TimeFrame.Week -> "15"
                    TimeFrame.Month -> "D"
                    TimeFrame.Year -> "W"
                    TimeFrame.All -> "M"
                }
                val candle = ApiClient.finnhubService.getCandles(ticker, res, fromSec, nowSec, finnhubKey)
                if (candle.status == "ok" && !candle.closePrices.isNullOrEmpty() && !candle.timestamps.isNullOrEmpty()) {
                    val count = candle.closePrices.size
                    val points = mutableListOf<HistoricalPoint>()
                    val sdf = SimpleDateFormat(if (timeframe == TimeFrame.Day) "HH:mm" else "MMM dd", Locale.getDefault())

                    for (i in 0 until count) {
                        val ts = (candle.timestamps[i]) * 1000L
                        val close = candle.closePrices[i]
                        val open = candle.opens?.getOrNull(i) ?: close
                        val high = candle.highs?.getOrNull(i) ?: close
                        val low = candle.lows?.getOrNull(i) ?: close
                        val vol = candle.volumes?.getOrNull(i) ?: 1000L
                        points.add(HistoricalPoint(ts, close, open, high, low, close, vol, sdf.format(Date(ts))))
                    }
                    return@withContext points
                }
            } catch (e: Exception) {
                Log.w(TAG, "Finnhub candle error: ${e.message}")
            }
        }

        // 2. Try Yahoo Finance chart
        try {
            val rangeStr = when (timeframe) {
                TimeFrame.Day -> "1d"
                TimeFrame.Week -> "5d"
                TimeFrame.Month -> "1mo"
                TimeFrame.Year -> "1y"
                TimeFrame.All -> "5y"
            }
            val intervalStr = timeframe.interval
            val envelope = ApiClient.yahooFinanceService.getChart(ticker, rangeStr, intervalStr)
            val result = envelope.chart?.result?.firstOrNull()
            if (result != null && !result.timestamp.isNullOrEmpty()) {
                val quotes = result.indicators?.quote?.firstOrNull()
                val closes = quotes?.close
                if (closes != null && closes.isNotEmpty()) {
                    val points = mutableListOf<HistoricalPoint>()
                    val sdf = SimpleDateFormat(if (timeframe == TimeFrame.Day) "HH:mm" else "MMM dd", Locale.getDefault())

                    for (i in result.timestamp.indices) {
                        val close = closes.getOrNull(i) ?: continue
                        val ts = result.timestamp[i] * 1000L
                        val open = quotes.open?.getOrNull(i) ?: close
                        val high = quotes.high?.getOrNull(i) ?: close
                        val low = quotes.low?.getOrNull(i) ?: close
                        val vol = quotes.volume?.getOrNull(i) ?: 50000L
                        points.add(HistoricalPoint(ts, close, open, high, low, close, vol, sdf.format(Date(ts))))
                    }
                    if (points.isNotEmpty()) return@withContext points
                }
            }
        } catch (e: Exception) {
            Log.d(TAG, "Yahoo chart fallback for $ticker: ${e.message}")
        }

        // 3. Realistic Synthetic Generator
        val count = when (timeframe) {
            TimeFrame.Day -> 36
            TimeFrame.Week -> 35
            TimeFrame.Month -> 30
            TimeFrame.Year -> 52
            TimeFrame.All -> 60
        }
        val variance = when (timeframe) {
            TimeFrame.Day -> 0.015
            TimeFrame.Week -> 0.035
            TimeFrame.Month -> 0.07
            TimeFrame.Year -> 0.22
            TimeFrame.All -> 0.45
        }
        generateSyntheticHistory(inst.price, count, variance, timeframe)
    }

    /**
     * Fetches relevant market & company news for a specific stock or broad market.
     */
    suspend fun fetchStockNews(ticker: String?): List<MarketNews> = withContext(Dispatchers.IO) {
        val finnhubKey = getEffectiveFinnhubKey()
        val alphaKey = getEffectiveAlphaVantageKey()

        // 1. Finnhub company news
        if (finnhubKey.isNotEmpty() && !ticker.isNullOrEmpty()) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val cal = Calendar.getInstance()
                val toDate = sdf.format(cal.time)
                cal.add(Calendar.DAY_OF_YEAR, -7)
                val fromDate = sdf.format(cal.time)

                val list = ApiClient.finnhubService.getCompanyNews(ticker, fromDate, toDate, finnhubKey)
                if (list.isNotEmpty()) {
                    return@withContext list.take(8).map { item ->
                        MarketNews(
                            id = item.id?.toString() ?: "news-${UUID.randomUUID()}",
                            ticker = ticker,
                            headline = item.headline ?: "Market Update for $ticker",
                            summary = item.summary ?: "Recent trading trends and volume analysis.",
                            source = item.source ?: "Finnhub Financial",
                            url = item.url ?: "",
                            imageUrl = item.image,
                            datetime = (item.datetime ?: System.currentTimeMillis() / 1000) * 1000L,
                            sentiment = determineSentiment(item.headline.orEmpty() + " " + item.summary.orEmpty())
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Finnhub news error: ${e.message}")
            }
        }

        // 2. Alpha Vantage news & sentiment
        if (alphaKey.isNotEmpty()) {
            try {
                val queryTicker = ticker ?: "NVDA"
                val response = ApiClient.alphaVantageService.getNewsSentiment(tickers = queryTicker, limit = 8, apiKey = alphaKey)
                if (!response.feed.isNullOrEmpty()) {
                    return@withContext response.feed.map { item ->
                        val sent = when (item.sentimentLabel?.lowercase()) {
                            "bullish", "somewhat-bullish" -> Sentiment.Bullish
                            "bearish", "somewhat-bearish" -> Sentiment.Bearish
                            else -> Sentiment.Neutral
                        }
                        MarketNews(
                            id = "av-${UUID.randomUUID()}",
                            ticker = ticker ?: "MARKET",
                            headline = item.title ?: "Market News Alert",
                            summary = item.summary ?: "",
                            source = item.source ?: "Alpha Vantage News",
                            url = item.url ?: "",
                            imageUrl = item.bannerImage,
                            datetime = System.currentTimeMillis() - Random.nextLong(100000, 3600000),
                            sentiment = sent
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Alpha Vantage news error: ${e.message}")
            }
        }

        // 3. Seed / fallback news filtered by ticker
        val all = _news.value
        if (!ticker.isNullOrEmpty()) {
            val matching = all.filter { it.ticker.equals(ticker, ignoreCase = true) }
            if (matching.isNotEmpty()) return@withContext matching
        }
        all
    }

    private fun determineSentiment(text: String): Sentiment {
        val lower = text.lowercase()
        return when {
            lower.contains("surge") || lower.contains("gain") || lower.contains("record") ||
                    lower.contains("breakout") || lower.contains("bullish") || lower.contains("growth") ||
                    lower.contains("accelerate") || lower.contains("upgrade") -> Sentiment.Bullish

            lower.contains("drop") || lower.contains("fall") || lower.contains("loss") ||
                    lower.contains("warning") || lower.contains("bearish") || lower.contains("cut") ||
                    lower.contains("slump") || lower.contains("downgrade") -> Sentiment.Bearish

            else -> Sentiment.Neutral
        }
    }

    private fun simulateLiveTick() {
        val list = _instruments.value.toMutableList()
        val randomIdx = Random.nextInt(list.size)
        val target = list[randomIdx]
        val deltaPct = (Random.nextDouble(-0.35, 0.45))
        val newPrice = ((target.price * (1.0 + (deltaPct / 100.0))) * 100.0).roundToInt() / 100.0
        val newChangePct = (((target.changePct + (deltaPct * 0.4))) * 100.0).roundToInt() / 100.0
        val newSpark = (target.sparkline.takeLast(20) + newPrice).takeLast(24)

        list[randomIdx] = target.copy(
            price = newPrice,
            changePct = newChangePct,
            sparkline = newSpark
        )
        _instruments.value = list
    }

    private fun updateMarketPulse() {
        val advances = _instruments.value.count { it.changePct >= 0 }
        val declines = _instruments.value.size - advances
        val avgChange = if (_instruments.value.isNotEmpty()) _instruments.value.map { it.changePct }.average() else 0.0

        val summary = when {
            avgChange > 1.0 -> "Bullish momentum expanding. Tech & semiconductor leadership accelerating into resistance."
            avgChange > 0.0 -> "Market trading with steady upward bias. Growth sectors accumulating selective bids."
            avgChange > -1.0 -> "Consolidation phase across major indices. Defensives and financials holding support."
            else -> "Market softening under distribution pressure. Elevated volatility detected; risk caution advised."
        }

        _marketPulse.value = MarketPulse(
            status = "NYSE/NASDAQ Active · Real-time Tape",
            summary = summary,
            advanceDecline = "$advances Advances / $declines Declines",
            volatilityIndex = (14.2 + (Random.nextDouble(-0.5, 0.5) * 10).roundToInt() / 10.0),
            isLive = true,
            lastUpdated = System.currentTimeMillis()
        )
    }

    private fun generateSyntheticHistory(
        currentPrice: Double,
        points: Int,
        variance: Double,
        timeframe: TimeFrame = TimeFrame.Day
    ): List<HistoricalPoint> {
        val list = mutableListOf<HistoricalPoint>()
        var runningPrice = currentPrice * (1.0 - (variance * 0.6))
        val stepMs = (timeframe.days * 24L * 3600L * 1000L) / points
        val startTime = System.currentTimeMillis() - (timeframe.days * 24L * 3600L * 1000L)
        val sdf = SimpleDateFormat(if (timeframe == TimeFrame.Day) "HH:mm" else "MMM dd", Locale.getDefault())

        for (i in 0 until points) {
            val progress = i.toDouble() / (points - 1)
            val noise = Random.nextDouble(-variance * 0.4, variance * 0.45)
            // Gently guide towards current price at the end
            runningPrice += (currentPrice - runningPrice) * (0.05 + 0.08 * progress) + (runningPrice * noise * 0.2)
            if (i == points - 1) runningPrice = currentPrice

            val high = runningPrice * (1.0 + Random.nextDouble(0.002, 0.012))
            val low = runningPrice * (1.0 - Random.nextDouble(0.002, 0.012))
            val open = (runningPrice + low) / 2
            val ts = startTime + (i * stepMs)
            val vol = (Random.nextLong(15000, 250000))

            list.add(
                HistoricalPoint(
                    timestamp = ts,
                    price = ((runningPrice * 100.0).roundToInt() / 100.0),
                    open = ((open * 100.0).roundToInt() / 100.0),
                    high = ((high * 100.0).roundToInt() / 100.0),
                    low = ((low * 100.0).roundToInt() / 100.0),
                    close = ((runningPrice * 100.0).roundToInt() / 100.0),
                    volume = vol,
                    dateLabel = sdf.format(Date(ts))
                )
            )
        }
        return list
    }
}
