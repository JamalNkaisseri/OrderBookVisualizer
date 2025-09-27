package com.visualizer;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;


public class VolatilityIndicators {

    // Price data storage
    private final Queue<PriceData> priceHistory = new ConcurrentLinkedQueue<>();
    private final int maxHistorySize;
    private final MathContext mathContext = new MathContext(8, RoundingMode.HALF_UP);

    public VolatilityIndicators(int maxHistorySize) {
        this.maxHistorySize = maxHistorySize;
    }

    /**
     * Add new price data point
     */
    public void addPriceData(BigDecimal price, long timestamp, BigDecimal volume) {
        priceHistory.offer(new PriceData(price, timestamp, volume));

        // Keep history within bounds
        while (priceHistory.size() > maxHistorySize) {
            priceHistory.poll();
        }
    }

    /**
     * 1. ATR (Average True Range) - Most popular volatility indicator
     * Measures average price movement over N periods
     */
    public BigDecimal calculateATR(int periods) {
        if (priceHistory.size() < periods + 1) return BigDecimal.ZERO;

        List<PriceData> data = new ArrayList<>(priceHistory);
        BigDecimal atrSum = BigDecimal.ZERO;

        for (int i = data.size() - periods; i < data.size(); i++) {
            if (i > 0) {
                PriceData current = data.get(i);
                PriceData previous = data.get(i - 1);

                // True Range = max(high-low, |high-prevClose|, |low-prevClose|)
                // For crypto, we'll use current price as both high/low/close
                BigDecimal tr1 = BigDecimal.ZERO; // high - low (0 for single price)
                BigDecimal tr2 = current.price.subtract(previous.price).abs();
                BigDecimal tr3 = tr2; // Same as tr2 for single price point

                BigDecimal trueRange = tr2; // Simplified for single price data
                atrSum = atrSum.add(trueRange);
            }
        }

        return atrSum.divide(BigDecimal.valueOf(periods), mathContext);
    }

    /**
     * 2. Bollinger Band Width - Measures volatility expansion/contraction
     * Width = (Upper Band - Lower Band) / Middle Band
     */
    public BigDecimal calculateBollingerBandWidth(int periods, double stdDevMultiplier) {
        if (priceHistory.size() < periods) return BigDecimal.ZERO;

        List<PriceData> data = new ArrayList<>(priceHistory);
        List<BigDecimal> recentPrices = data.subList(Math.max(0, data.size() - periods), data.size())
                .stream()
                .map(pd -> pd.price)
                .toList();

        BigDecimal sma = calculateSMA(recentPrices);
        BigDecimal stdDev = calculateStandardDeviation(recentPrices, sma);

        BigDecimal upperBand = sma.add(stdDev.multiply(BigDecimal.valueOf(stdDevMultiplier)));
        BigDecimal lowerBand = sma.subtract(stdDev.multiply(BigDecimal.valueOf(stdDevMultiplier)));

        if (sma.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        return upperBand.subtract(lowerBand).divide(sma, mathContext);
    }

    /**
     * 3. Price Velocity - Rate of price change
     */
    public BigDecimal calculatePriceVelocity(int periods) {
        if (priceHistory.size() < periods + 1) return BigDecimal.ZERO;

        List<PriceData> data = new ArrayList<>(priceHistory);
        PriceData current = data.get(data.size() - 1);
        PriceData past = data.get(data.size() - 1 - periods);

        BigDecimal priceChange = current.price.subtract(past.price);
        long timeChange = current.timestamp - past.timestamp;

        if (timeChange == 0) return BigDecimal.ZERO;

        // Return price change per second
        return priceChange.divide(BigDecimal.valueOf(timeChange / 1000.0), mathContext);
    }

    /**
     * 4. Historical Volatility - Annualized volatility based on returns
     */
    public BigDecimal calculateHistoricalVolatility(int periods) {
        if (priceHistory.size() < periods + 1) return BigDecimal.ZERO;

        List<PriceData> data = new ArrayList<>(priceHistory);
        List<BigDecimal> returns = new ArrayList<>();

        // Calculate returns
        for (int i = data.size() - periods; i < data.size(); i++) {
            if (i > 0) {
                BigDecimal currentPrice = data.get(i).price;
                BigDecimal previousPrice = data.get(i - 1).price;

                if (previousPrice.compareTo(BigDecimal.ZERO) != 0) {
                    BigDecimal returnRate = currentPrice.divide(previousPrice, mathContext)
                            .subtract(BigDecimal.ONE);
                    returns.add(returnRate);
                }
            }
        }

        if (returns.isEmpty()) return BigDecimal.ZERO;

        BigDecimal meanReturn = returns.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns.size()), mathContext);

        BigDecimal variance = returns.stream()
                .map(r -> r.subtract(meanReturn).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(returns.size()), mathContext);

        // Annualized volatility (assuming 1-minute periods, 525600 minutes per year)
        return sqrt(variance.multiply(BigDecimal.valueOf(525600)));
    }

    /**
     * 5. Volatility Spike Detector - Identifies sudden volatility increases
     */
    public VolatilitySpike detectVolatilitySpike(int shortPeriod, int longPeriod, double threshold) {
        if (priceHistory.size() < longPeriod) {
            return new VolatilitySpike(false, BigDecimal.ZERO, BigDecimal.ZERO);
        }

        BigDecimal shortTermATR = calculateATR(shortPeriod);
        BigDecimal longTermATR = calculateATR(longPeriod);

        if (longTermATR.compareTo(BigDecimal.ZERO) == 0) {
            return new VolatilitySpike(false, shortTermATR, longTermATR);
        }

        BigDecimal ratio = shortTermATR.divide(longTermATR, mathContext);
        boolean isSpike = ratio.compareTo(BigDecimal.valueOf(threshold)) > 0;

        return new VolatilitySpike(isSpike, shortTermATR, longTermATR);
    }

    /**
     * 6. Volatility Percentile - Where current volatility ranks historically
     */
    public BigDecimal calculateVolatilityPercentile(int lookbackPeriods, int currentPeriods) {
        if (priceHistory.size() < lookbackPeriods) return BigDecimal.ZERO;

        List<BigDecimal> historicalATRs = new ArrayList<>();
        List<PriceData> data = new ArrayList<>(priceHistory);

        // Calculate ATR for each possible period in lookback window
        for (int i = currentPeriods; i < Math.min(data.size(), lookbackPeriods); i++) {
            List<PriceData> subset = data.subList(i - currentPeriods, i);
            BigDecimal atr = calculateATRForSubset(subset);
            if (atr.compareTo(BigDecimal.ZERO) > 0) {
                historicalATRs.add(atr);
            }
        }

        if (historicalATRs.isEmpty()) return BigDecimal.ZERO;

        BigDecimal currentATR = calculateATR(currentPeriods);
        Collections.sort(historicalATRs);

        long countBelow = historicalATRs.stream()
                .mapToLong(atr -> atr.compareTo(currentATR) < 0 ? 1 : 0)
                .sum();

        return BigDecimal.valueOf(countBelow)
                .divide(BigDecimal.valueOf(historicalATRs.size()), mathContext)
                .multiply(BigDecimal.valueOf(100));
    }

    // Helper methods
    private BigDecimal calculateSMA(List<BigDecimal> prices) {
        if (prices.isEmpty()) return BigDecimal.ZERO;
        return prices.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(prices.size()), mathContext);
    }

    private BigDecimal calculateStandardDeviation(List<BigDecimal> prices, BigDecimal mean) {
        if (prices.size() < 2) return BigDecimal.ZERO;

        BigDecimal variance = prices.stream()
                .map(price -> price.subtract(mean).pow(2))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(prices.size()), mathContext);

        return sqrt(variance);
    }

    private BigDecimal sqrt(BigDecimal value) {
        // Simple Newton's method for square root
        if (value.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;

        BigDecimal x = value;
        BigDecimal prev;
        do {
            prev = x;
            x = x.add(value.divide(x, mathContext)).divide(BigDecimal.valueOf(2), mathContext);
        } while (x.subtract(prev).abs().compareTo(BigDecimal.valueOf(0.0001)) > 0);

        return x;
    }

    private BigDecimal calculateATRForSubset(List<PriceData> subset) {
        if (subset.size() < 2) return BigDecimal.ZERO;

        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 1; i < subset.size(); i++) {
            BigDecimal trueRange = subset.get(i).price.subtract(subset.get(i-1).price).abs();
            sum = sum.add(trueRange);
        }

        return sum.divide(BigDecimal.valueOf(subset.size() - 1), mathContext);
    }

    // Data classes
    public static class PriceData {
        public final BigDecimal price;
        public final long timestamp;
        public final BigDecimal volume;

        public PriceData(BigDecimal price, long timestamp, BigDecimal volume) {
            this.price = price;
            this.timestamp = timestamp;
            this.volume = volume;
        }
    }

    public static class VolatilitySpike {
        public final boolean isSpike;
        public final BigDecimal shortTermVolatility;
        public final BigDecimal longTermVolatility;

        public VolatilitySpike(boolean isSpike, BigDecimal shortTerm, BigDecimal longTerm) {
            this.isSpike = isSpike;
            this.shortTermVolatility = shortTerm;
            this.longTermVolatility = longTerm;
        }
    }
}