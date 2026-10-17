package com.example.data.repository

import com.example.data.model.*

object StockSeedData {
    val INSTRUMENTS = listOf(
        Instrument("NVDA", "NVIDIA Corporation", Sector.Technology, MarketCap.Large, 138.40, 2.74, 134.80, 139.60, 134.20, 48200000L, "$3.4T", 54.2, 1.68),
        Instrument("PLTR", "Palantir Technologies", Sector.Technology, MarketCap.Large, 42.15, 3.84, 40.50, 42.80, 40.30, 29400000L, "$94.2B", 82.1, 1.45),
        Instrument("AVGO", "Broadcom Inc.", Sector.Technology, MarketCap.Large, 184.20, 1.62, 181.50, 185.30, 180.90, 8400000L, "$860B", 38.4, 1.32),
        Instrument("AMD", "Advanced Micro Devices", Sector.Technology, MarketCap.Large, 168.90, 2.18, 165.20, 170.10, 164.80, 22100000L, "$273B", 46.5, 1.54),
        Instrument("IOT", "Samsara Inc.", Sector.Technology, MarketCap.Mid, 38.40, 2.35, 37.40, 39.10, 37.20, 6800000L, "$21.1B", 42.0, 1.25),
        Instrument("SMCI", "Super Micro Computer", Sector.Technology, MarketCap.Mid, 48.60, 4.42, 46.20, 49.80, 45.90, 19500000L, "$28.5B", 22.8, 1.85),
        Instrument("CRWD", "CrowdStrike Holdings", Sector.Technology, MarketCap.Large, 312.40, 1.05, 309.00, 314.50, 307.80, 4200000L, "$76.3B", 78.4, 1.18),
        Instrument("AAPL", "Apple Inc.", Sector.Technology, MarketCap.Large, 234.80, 0.92, 232.50, 235.60, 231.90, 38500000L, "$3.5T", 33.1, 0.98),
        Instrument("MSFT", "Microsoft Corp.", Sector.Technology, MarketCap.Large, 442.50, 1.14, 437.20, 444.10, 436.80, 18900000L, "$3.3T", 35.8, 0.92),
        Instrument("TSLA", "Tesla Inc.", Sector.Consumer, MarketCap.Large, 220.70, 3.12, 213.50, 222.40, 212.80, 61200000L, "$705B", 68.2, 2.05),
        Instrument("LLY", "Eli Lilly & Co.", Sector.Healthcare, MarketCap.Large, 892.30, 1.28, 881.00, 895.50, 879.20, 3100000L, "$848B", 61.4, 0.72),
        Instrument("UNH", "UnitedHealth Group", Sector.Healthcare, MarketCap.Large, 548.20, -0.64, 552.00, 553.40, 546.50, 2800000L, "$505B", 24.3, 0.65),
        Instrument("JPM", "JPMorgan Chase & Co.", Sector.Financials, MarketCap.Large, 228.50, 0.72, 226.80, 229.40, 226.10, 8900000L, "$650B", 12.8, 1.08),
        Instrument("GS", "Goldman Sachs Group", Sector.Financials, MarketCap.Large, 524.00, 0.94, 519.00, 526.50, 517.50, 1900000L, "$172B", 15.6, 1.24),
        Instrument("XOM", "Exxon Mobil Corp.", Sector.Energy, MarketCap.Large, 118.40, -1.12, 119.80, 120.20, 117.90, 11400000L, "$470B", 14.1, 0.88),
        Instrument("CVX", "Chevron Corporation", Sector.Energy, MarketCap.Large, 152.10, -0.58, 153.20, 153.80, 151.40, 6400000L, "$279B", 13.9, 0.82),
        Instrument("COP", "ConocoPhillips", Sector.Energy, MarketCap.Large, 109.80, -1.46, 111.50, 111.90, 109.20, 4800000L, "$129B", 12.4, 0.95),
        Instrument("CAT", "Caterpillar Inc.", Sector.Industrials, MarketCap.Large, 396.70, 0.61, 394.00, 398.50, 393.20, 2400000L, "$192B", 17.8, 1.12),
        Instrument("BA", "Boeing Company", Sector.Industrials, MarketCap.Large, 178.20, -0.38, 179.00, 180.20, 177.50, 5100000L, "$109B", 34.0, 1.42),
        Instrument("SPY", "SPDR S&P 500 ETF", Sector.Financials, MarketCap.Large, 584.50, 0.65, 581.20, 585.80, 580.90, 52000000L, "$560B", 26.2, 1.00)
    )

    val SIGNALS = listOf(
        Signal(
            id = "sig-nvda",
            ticker = "NVDA",
            kind = SignalKind.MomentumSetup,
            trend = Trend.Up,
            trendStrength = Level.High,
            volatility = Level.Medium,
            probability = 85,
            volumeStatus = "Heavy Institutional Flow",
            volumeCopy = "Buy volume surge across 5-minute candles surpassing 20-day average by 180%.",
            pattern = 88,
            riskLevel = Level.Low,
            note = "Breakout through consolidation resistance at $136.50 with accelerating relative strength.",
            commentary = "NVDA demonstrates strong institutional accumulation on Blackwell demand guidance. Trend momentum is verified across short and medium moving averages."
        ),
        Signal(
            id = "sig-iot",
            ticker = "IOT",
            kind = SignalKind.MomentumSetup,
            trend = Trend.Up,
            trendStrength = Level.High,
            volatility = Level.Low,
            probability = 72,
            volumeStatus = "Elevated Inflow",
            volumeCopy = "Unusual multi-block accumulation detected on afternoon session.",
            pattern = 78,
            riskLevel = Level.Low,
            note = "Cup-and-handle consolidation pattern nearing breakout threshold.",
            commentary = "Mid-cap enterprise IoT software continues to attract sector rotation inflows as margins expand."
        ),
        Signal(
            id = "sig-pltr",
            ticker = "PLTR",
            kind = SignalKind.VolatilitySpike,
            trend = Trend.Up,
            trendStrength = Level.High,
            volatility = Level.High,
            probability = 76,
            volumeStatus = "High Volatility Surge",
            volumeCopy = "Rapid ATR expansion with aggressive options sweep flow.",
            pattern = 82,
            riskLevel = Level.Medium,
            note = "Government and commercial AIP bootcamps driving sustained pipeline conversions.",
            commentary = "Volatility spike indicates directional continuation. High beta environment necessitates disciplined position sizing."
        ),
        Signal(
            id = "sig-avgo",
            ticker = "AVGO",
            kind = SignalKind.MomentumSetup,
            trend = Trend.Up,
            trendStrength = Level.High,
            volatility = Level.Low,
            probability = 81,
            volumeStatus = "Steady Accumulation",
            volumeCopy = "Continuous buy pressure with narrowing spread during morning hours.",
            pattern = 84,
            riskLevel = Level.Low,
            note = "Custom silicon and Ethernet networking leadership cementing multi-quarter earnings beat potential.",
            commentary = "Broadcom maintains a textbook ascending triangle structure with positive MACD divergence."
        ),
        Signal(
            id = "sig-amd",
            ticker = "AMD",
            kind = SignalKind.SectorRotation,
            trend = Trend.Up,
            trendStrength = Level.Medium,
            volatility = Level.Medium,
            probability = 79,
            volumeStatus = "Rotational Inflow",
            volumeCopy = "Capital rotating out of lagging cyclicals directly into semiconductor leaders.",
            pattern = 75,
            riskLevel = Level.Low,
            note = "Data center MI300 series GPU adoption expanding with cloud hyperscalers.",
            commentary = "Relative strength against semiconductor ETF (SMH) breaking above 50-day moving average."
        ),
        Signal(
            id = "sig-smci",
            ticker = "SMCI",
            kind = SignalKind.PatternMatch,
            trend = Trend.Up,
            trendStrength = Level.Medium,
            volatility = Level.High,
            probability = 68,
            volumeStatus = "Volatile Rebound",
            volumeCopy = "Short-covering rally confirmed by elevated retail and options volume.",
            pattern = 80,
            riskLevel = Level.High,
            note = "Bullish engulfing candle pattern printed at key horizontal support.",
            commentary = "Technical bounce in progress. Maintain tight stop-loss due to elevated historic volatility."
        ),
        Signal(
            id = "sig-lly",
            ticker = "LLY",
            kind = SignalKind.MomentumSetup,
            trend = Trend.Up,
            trendStrength = Level.High,
            volatility = Level.Low,
            probability = 77,
            volumeStatus = "Defensive Quality Flow",
            volumeCopy = "Steady index fund and healthcare sector ETF allocations.",
            pattern = 76,
            riskLevel = Level.Low,
            note = "Incretin metabolic pipeline expansion sustaining multi-year revenue growth.",
            commentary = "Lilly continues to serve as premier large-cap growth stalwart with minimal macroeconomic sensitivity."
        ),
        Signal(
            id = "sig-xom",
            ticker = "XOM",
            kind = SignalKind.RiskWarning,
            trend = Trend.Down,
            trendStrength = Level.Medium,
            volatility = Level.Low,
            probability = 63,
            volumeStatus = "Distribution Detected",
            volumeCopy = "Institutional outflows correlating with crude oil inventory build.",
            pattern = 71,
            riskLevel = Level.Medium,
            note = "Double top breakdown below $120 neckline with declining momentum.",
            commentary = "Crude futures softness creating multi-week headwind. Hedging or reducing exposure recommended."
        ),
        Signal(
            id = "sig-jpm",
            ticker = "JPM",
            kind = SignalKind.SectorRotation,
            trend = Trend.Up,
            trendStrength = Level.Medium,
            volatility = Level.Low,
            probability = 73,
            volumeStatus = "Balanced Bids",
            volumeCopy = "Broad institutional positioning ahead of quarterly net interest margin data.",
            pattern = 74,
            riskLevel = Level.Low,
            note = "Net interest income resilience and investment banking fee rebound.",
            commentary = "Financials providing reliable low-beta stability in the current yield curve regime."
        )
    )

    val INITIAL_POSITIONS = listOf(
        Position("pos-1", "NVDA", 25.0, 122.50),
        Position("pos-2", "PLTR", 120.0, 36.40),
        Position("pos-3", "AVGO", 15.0, 172.00),
        Position("pos-4", "IOT", 80.0, 34.20)
    )

    val INITIAL_ALERTS = listOf(
        AlertItem(
            id = "alt-1",
            ticker = "NVDA",
            category = "momentum",
            title = "Momentum Breakout Triggered",
            minutesAgo = 6,
            probability = 85,
            body = "NVDA crossed above $137.50 with +180% relative volume surge.",
            signalId = "sig-nvda"
        ),
        AlertItem(
            id = "alt-2",
            ticker = "PLTR",
            category = "volatility",
            title = "Volatility Spike Alert",
            minutesAgo = 18,
            probability = 76,
            body = "PLTR intraday range expanded 4.2% on aggressive commercial demand flow.",
            signalId = "sig-pltr"
        ),
        AlertItem(
            id = "alt-3",
            ticker = "XOM",
            category = "risk",
            title = "Risk Warning: Trend Softening",
            minutesAgo = 34,
            probability = 63,
            body = "XOM slipped below critical 20-day moving average at $119.20.",
            signalId = "sig-xom"
        ),
        AlertItem(
            id = "alt-4",
            ticker = "AMD",
            category = "sector",
            title = "Sector Rotation Detected",
            minutesAgo = 52,
            probability = 79,
            body = "Semiconductor rotation accelerating into AMD as data center revenue scales.",
            signalId = "sig-amd"
        )
    )

    val INITIAL_NEWS = listOf(
        MarketNews(
            id = "news-1",
            ticker = "NVDA",
            headline = "NVIDIA Blackwell Ultra Production Ramp Accelerates Ahead of Hyperscaler Deployments",
            summary = "CEO Jensen Huang highlights unprecedented generative AI compute cluster demand across cloud service providers, driving gross margin expansion.",
            source = "Bloomberg Markets",
            datetime = System.currentTimeMillis() - 14 * 60 * 1000,
            sentiment = Sentiment.Bullish
        ),
        MarketNews(
            id = "news-2",
            ticker = "PLTR",
            headline = "Palantir Expands AIP Platform Contracts Across Federal and Commercial Defense Sectors",
            summary = "New enterprise multi-year deals underscore accelerated adoption of Palantir's Artificial Intelligence Platform with demonstrable ROI within weeks.",
            source = "Reuters Financial",
            datetime = System.currentTimeMillis() - 45 * 60 * 1000,
            sentiment = Sentiment.Bullish
        ),
        MarketNews(
            id = "news-3",
            ticker = "AVGO",
            headline = "Broadcom Secures Next-Generation Custom ASIC Silicon Designs for Tier-1 Cloud Customers",
            summary = "Networking and high-speed optical interconnect demand positions Broadcom as central hardware backbone for distributed large model training.",
            source = "Wall Street Journal",
            datetime = System.currentTimeMillis() - 120 * 60 * 1000,
            sentiment = Sentiment.Bullish
        ),
        MarketNews(
            id = "news-4",
            ticker = "AMD",
            headline = "AMD Instinct MI325X Software Ecosystem Matures With Major PyTorch Optimizations",
            summary = "Open-source ROCm 6.2 performance benchmarks demonstrate competitive parity in inference latency across open-weights frontier LLMs.",
            source = "TechCrunch Markets",
            datetime = System.currentTimeMillis() - 180 * 60 * 1000,
            sentiment = Sentiment.Bullish
        ),
        MarketNews(
            id = "news-5",
            ticker = "XOM",
            headline = "Crude Oil Consolidates Below $75 as Global Refinery Inventories Signal Muted Demand",
            summary = "OPEC+ output policy and elevated commercial reserves weigh on near-term upstream exploration margins for integrated energy majors.",
            source = "Financial Times",
            datetime = System.currentTimeMillis() - 240 * 60 * 1000,
            sentiment = Sentiment.Bearish
        ),
        MarketNews(
            id = "news-6",
            ticker = "JPM",
            headline = "JPMorgan Signals Capital Markets Resurgence Following Uptick in Debt and Equity Underwriting",
            summary = "Jamie Dimon reports corporate advisory pipelines remain strong as rate clarity improves corporate transaction volume.",
            source = "CNBC",
            datetime = System.currentTimeMillis() - 310 * 60 * 1000,
            sentiment = Sentiment.Neutral
        )
    )
}
