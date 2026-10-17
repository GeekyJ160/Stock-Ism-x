package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.MarketRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

enum class MainScreen(val title: String) {
    Home("Home"),
    Signals("Signals"),
    Scanner("Scanner"),
    Portfolio("Portfolio"),
    Alerts("Alerts"),
    Detail("Stock Detail"),
    Settings("Settings")
}

data class PortfolioSummary(
    val totalBalance: Double,
    val totalInvested: Double,
    val totalProfitLoss: Double,
    val totalProfitLossPct: Double,
    val availableCash: Double
)

class StockViewModel(application: Application) : AndroidViewModel(application) {
    val repository = MarketRepository(application)

    // Screen navigation
    private val _currentScreen = MutableStateFlow(MainScreen.Home)
    val currentScreen: StateFlow<MainScreen> = _currentScreen.asStateFlow()

    private val _previousScreen = MutableStateFlow(MainScreen.Home)

    // Stock Selection for Detail view
    private val _selectedTicker = MutableStateFlow<String>("NVDA")
    val selectedTicker: StateFlow<String> = _selectedTicker.asStateFlow()

    // Chart Timeframe & Points for Detail Screen
    private val _selectedTimeFrame = MutableStateFlow(TimeFrame.Day)
    val selectedTimeFrame: StateFlow<TimeFrame> = _selectedTimeFrame.asStateFlow()

    private val _chartPoints = MutableStateFlow<List<HistoricalPoint>>(emptyList())
    val chartPoints: StateFlow<List<HistoricalPoint>> = _chartPoints.asStateFlow()

    private val _selectedChartPoint = MutableStateFlow<HistoricalPoint?>(null)
    val selectedChartPoint: StateFlow<HistoricalPoint?> = _selectedChartPoint.asStateFlow()

    private val _isChartLoading = MutableStateFlow(false)
    val isChartLoading: StateFlow<Boolean> = _isChartLoading.asStateFlow()

    // Stock-specific news
    private val _stockNews = MutableStateFlow<List<MarketNews>>(emptyList())
    val stockNews: StateFlow<List<MarketNews>> = _stockNews.asStateFlow()

    private val _isNewsLoading = MutableStateFlow(false)
    val isNewsLoading: StateFlow<Boolean> = _isNewsLoading.asStateFlow()

    // Scanner state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedSector = MutableStateFlow<Sector?>(null)
    val selectedSector: StateFlow<Sector?> = _selectedSector.asStateFlow()

    private val _selectedCap = MutableStateFlow<MarketCap?>(null)
    val selectedCap: StateFlow<MarketCap?> = _selectedCap.asStateFlow()

    // Signals state
    private val _selectedSignalKind = MutableStateFlow<SignalKind?>(null)
    val selectedSignalKind: StateFlow<SignalKind?> = _selectedSignalKind.asStateFlow()

    // Key Validation / Test feedback
    private val _testApiResult = MutableStateFlow<String?>(null)
    val testApiResult: StateFlow<String?> = _testApiResult.asStateFlow()

    private var pollJob: Job? = null

    init {
        // Initial quote fetch and news load
        viewModelScope.launch {
            repository.refreshQuotes()
            loadDetailData(_selectedTicker.value, _selectedTimeFrame.value)
        }
        startPolling()
    }

    private fun startPolling() {
        pollJob?.cancel()
        pollJob = viewModelScope.launch {
            while (isActive) {
                delay(12000L) // 12-second live refresh
                repository.refreshQuotes()
            }
        }
    }

    fun navigateTo(screen: MainScreen) {
        if (_currentScreen.value != screen) {
            _previousScreen.value = _currentScreen.value
            _currentScreen.value = screen
        }
    }

    fun navigateBack() {
        if (_currentScreen.value == MainScreen.Detail || _currentScreen.value == MainScreen.Settings) {
            _currentScreen.value = _previousScreen.value
        } else {
            _currentScreen.value = MainScreen.Home
        }
    }

    fun selectStock(ticker: String) {
        _selectedTicker.value = ticker
        _previousScreen.value = _currentScreen.value
        _currentScreen.value = MainScreen.Detail
        loadDetailData(ticker, _selectedTimeFrame.value)
    }

    fun setTimeFrame(tf: TimeFrame) {
        _selectedTimeFrame.value = tf
        loadChartData(_selectedTicker.value, tf)
    }

    fun setScrubbedPoint(point: HistoricalPoint?) {
        _selectedChartPoint.value = point
    }

    private fun loadDetailData(ticker: String, timeFrame: TimeFrame) {
        loadChartData(ticker, timeFrame)
        loadStockNews(ticker)
    }

    private fun loadChartData(ticker: String, timeFrame: TimeFrame) {
        viewModelScope.launch {
            _isChartLoading.value = true
            val points = repository.fetchHistoricalData(ticker, timeFrame)
            _chartPoints.value = points
            _selectedChartPoint.value = null
            _isChartLoading.value = false
        }
    }

    fun loadStockNews(ticker: String) {
        viewModelScope.launch {
            _isNewsLoading.value = true
            val news = repository.fetchStockNews(ticker)
            _stockNews.value = news
            _isNewsLoading.value = false
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSectorFilter(sector: Sector?) {
        _selectedSector.value = sector
    }

    fun setCapFilter(cap: MarketCap?) {
        _selectedCap.value = cap
    }

    fun setSignalKindFilter(kind: SignalKind?) {
        _selectedSignalKind.value = kind
    }

    fun refreshAll() {
        viewModelScope.launch {
            repository.refreshQuotes()
            loadDetailData(_selectedTicker.value, _selectedTimeFrame.value)
        }
    }

    fun toggleFavorite(ticker: String) {
        repository.toggleFavorite(ticker)
    }

    fun addTradePosition(ticker: String, shares: Double, price: Double) {
        repository.addPosition(ticker, shares, price)
    }

    fun removePosition(id: String) {
        repository.removePosition(id)
    }

    fun markAlertRead(id: String) {
        repository.markAlertRead(id)
    }

    fun markAllAlertsRead() {
        repository.markAllAlertsRead()
    }

    fun saveApiKeys(finnhubKey: String, alphaKey: String, provider: ApiProvider) {
        val current = repository.profile.value
        repository.updateProfile(
            current.copy(
                customFinnhubKey = finnhubKey,
                customAlphaVantageKey = alphaKey,
                activeProvider = provider
            )
        )
        refreshAll()
    }

    fun saveProfileConfig(capital: Double, risk: RiskMode) {
        val current = repository.profile.value
        repository.updateProfile(current.copy(capital = capital, risk = risk))
    }

    fun testApiConnection() {
        viewModelScope.launch {
            _testApiResult.value = "Testing API connection..."
            delay(600)
            repository.refreshQuotes()
            val msg = repository.apiStatusMessage.value
            _testApiResult.value = "Success: $msg"
        }
    }

    fun clearTestApiResult() {
        _testApiResult.value = null
    }

    // Calculations
    fun getPortfolioSummary(instruments: List<Instrument>, positions: List<Position>, profile: UserProfile): PortfolioSummary {
        var totalInvested = 0.0
        var totalCurrent = 0.0

        for (pos in positions) {
            val inst = instruments.find { it.ticker == pos.ticker }
            val currentPrice = inst?.price ?: pos.avgPrice
            val invested = pos.shares * pos.avgPrice
            val currentVal = pos.shares * currentPrice
            totalInvested += invested
            totalCurrent += currentVal
        }

        val profitLoss = totalCurrent - totalInvested
        val profitLossPct = if (totalInvested > 0) (profitLoss / totalInvested) * 100.0 else 0.0
        val availableCash = maxOf(0.0, profile.capital - totalInvested)
        val totalBalance = availableCash + totalCurrent

        return PortfolioSummary(
            totalBalance = totalBalance,
            totalInvested = totalInvested,
            totalProfitLoss = profitLoss,
            totalProfitLossPct = profitLossPct,
            availableCash = availableCash
        )
    }

    override fun onCleared() {
        super.onCleared()
        pollJob?.cancel()
    }
}
