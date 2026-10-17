package com.example.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// --- FINNHUB DTOs & SERVICE ---
@JsonClass(generateAdapter = true)
data class FinnhubQuoteDto(
    @Json(name = "c") val currentPrice: Double? = null,
    @Json(name = "d") val change: Double? = null,
    @Json(name = "dp") val percentChange: Double? = null,
    @Json(name = "h") val high: Double? = null,
    @Json(name = "l") val low: Double? = null,
    @Json(name = "o") val open: Double? = null,
    @Json(name = "pc") val previousClose: Double? = null,
    @Json(name = "t") val timestamp: Long? = null
)

@JsonClass(generateAdapter = true)
data class FinnhubCandleDto(
    @Json(name = "c") val closePrices: List<Double>? = null,
    @Json(name = "h") val highs: List<Double>? = null,
    @Json(name = "l") val lows: List<Double>? = null,
    @Json(name = "o") val opens: List<Double>? = null,
    @Json(name = "t") val timestamps: List<Long>? = null,
    @Json(name = "v") val volumes: List<Long>? = null,
    @Json(name = "s") val status: String? = null
)

@JsonClass(generateAdapter = true)
data class FinnhubNewsDto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "category") val category: String? = null,
    @Json(name = "datetime") val datetime: Long? = null,
    @Json(name = "headline") val headline: String? = null,
    @Json(name = "image") val image: String? = null,
    @Json(name = "related") val related: String? = null,
    @Json(name = "source") val source: String? = null,
    @Json(name = "summary") val summary: String? = null,
    @Json(name = "url") val url: String? = null
)

interface FinnhubService {
    @GET("api/v1/quote")
    suspend fun getQuote(
        @Query("symbol") symbol: String,
        @Query("token") token: String
    ): FinnhubQuoteDto

    @GET("api/v1/stock/candle")
    suspend fun getCandles(
        @Query("symbol") symbol: String,
        @Query("resolution") resolution: String,
        @Query("from") from: Long,
        @Query("to") to: Long,
        @Query("token") token: String
    ): FinnhubCandleDto

    @GET("api/v1/company-news")
    suspend fun getCompanyNews(
        @Query("symbol") symbol: String,
        @Query("from") from: String,
        @Query("to") to: String,
        @Query("token") token: String
    ): List<FinnhubNewsDto>

    @GET("api/v1/news")
    suspend fun getMarketNews(
        @Query("category") category: String = "general",
        @Query("token") token: String
    ): List<FinnhubNewsDto>
}

// --- ALPHA VANTAGE DTOs & SERVICE ---
@JsonClass(generateAdapter = true)
data class AlphaVantageQuoteItem(
    @Json(name = "01. symbol") val symbol: String? = null,
    @Json(name = "02. open") val open: String? = null,
    @Json(name = "03. high") val high: String? = null,
    @Json(name = "04. low") val low: String? = null,
    @Json(name = "05. price") val price: String? = null,
    @Json(name = "06. volume") val volume: String? = null,
    @Json(name = "09. change") val change: String? = null,
    @Json(name = "10. change percent") val changePercent: String? = null
)

@JsonClass(generateAdapter = true)
data class AlphaVantageGlobalQuoteResponse(
    @Json(name = "Global Quote") val globalQuote: AlphaVantageQuoteItem? = null
)

@JsonClass(generateAdapter = true)
data class AlphaVantageNewsItem(
    @Json(name = "title") val title: String? = null,
    @Json(name = "url") val url: String? = null,
    @Json(name = "time_published") val timePublished: String? = null,
    @Json(name = "summary") val summary: String? = null,
    @Json(name = "banner_image") val bannerImage: String? = null,
    @Json(name = "source") val source: String? = null,
    @Json(name = "overall_sentiment_label") val sentimentLabel: String? = null
)

@JsonClass(generateAdapter = true)
data class AlphaVantageNewsResponse(
    @Json(name = "items") val items: String? = null,
    @Json(name = "feed") val feed: List<AlphaVantageNewsItem>? = null
)

interface AlphaVantageService {
    @GET("query")
    suspend fun getGlobalQuote(
        @Query("function") function: String = "GLOBAL_QUOTE",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): AlphaVantageGlobalQuoteResponse

    @GET("query")
    suspend fun getNewsSentiment(
        @Query("function") function: String = "NEWS_SENTIMENT",
        @Query("tickers") tickers: String,
        @Query("limit") limit: Int = 10,
        @Query("apikey") apiKey: String
    ): AlphaVantageNewsResponse
}

// --- YAHOO LIVE FEED DTOs & SERVICE ---
@JsonClass(generateAdapter = true)
data class YahooSparkItem(
    @Json(name = "symbol") val symbol: String? = null,
    @Json(name = "fulldayPrice") val fulldayPrice: Double? = null,
    @Json(name = "fulldayChangePercent") val fulldayChangePercent: Double? = null,
    @Json(name = "previousClose") val previousClose: Double? = null,
    @Json(name = "close") val close: List<Double?>? = null,
    @Json(name = "end") val end: Long? = null
)

@JsonClass(generateAdapter = true)
data class YahooChartResult(
    @Json(name = "meta") val meta: YahooChartMeta? = null,
    @Json(name = "timestamp") val timestamp: List<Long>? = null,
    @Json(name = "indicators") val indicators: YahooChartIndicators? = null
)

@JsonClass(generateAdapter = true)
data class YahooChartMeta(
    @Json(name = "currency") val currency: String? = null,
    @Json(name = "symbol") val symbol: String? = null,
    @Json(name = "regularMarketPrice") val regularMarketPrice: Double? = null,
    @Json(name = "previousClose") val previousClose: Double? = null,
    @Json(name = "regularMarketDayHigh") val dayHigh: Double? = null,
    @Json(name = "regularMarketDayLow") val dayLow: Double? = null,
    @Json(name = "regularMarketVolume") val volume: Long? = null
)

@JsonClass(generateAdapter = true)
data class YahooChartIndicators(
    @Json(name = "quote") val quote: List<YahooChartQuoteValues>? = null
)

@JsonClass(generateAdapter = true)
data class YahooChartQuoteValues(
    @Json(name = "open") val open: List<Double?>? = null,
    @Json(name = "high") val high: List<Double?>? = null,
    @Json(name = "low") val low: List<Double?>? = null,
    @Json(name = "close") val close: List<Double?>? = null,
    @Json(name = "volume") val volume: List<Long?>? = null
)

@JsonClass(generateAdapter = true)
data class YahooChartEnvelope(
    @Json(name = "chart") val chart: YahooChartBody? = null
)

@JsonClass(generateAdapter = true)
data class YahooChartBody(
    @Json(name = "result") val result: List<YahooChartResult>? = null,
    @Json(name = "error") val error: Map<String, Any?>? = null
)

interface YahooFinanceService {
    @GET("v8/finance/spark")
    suspend fun getSpark(
        @Query("symbols") symbols: String,
        @Query("range") range: String = "1d",
        @Query("interval") interval: String = "5m"
    ): Map<String, YahooSparkItem>

    @GET("v8/finance/chart/{symbol}")
    suspend fun getChart(
        @Path("symbol") symbol: String,
        @Query("range") range: String = "1mo",
        @Query("interval") interval: String = "1d"
    ): YahooChartEnvelope
}
