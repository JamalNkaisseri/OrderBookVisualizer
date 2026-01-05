# Order Book Visualizer

A real-time cryptocurrency order book visualization application built with JavaFX. Connect to Binance's WebSocket API to monitor live market depth, visualize bid/ask spreads, and track market volatility across multiple trading pairs.



## Features

- Live Order Book Updates - Real-time bid and ask orders via Binance WebSocket
- Multiple Trading Pairs - Switch between BTC/USDT, ETH/USDT, BNB/USDT, SOL/USDT, and more
- Depth Chart Visualization - Visual representation of market depth with interactive charts
- Market Info Widget - Key metrics including spread, bid/ask volumes, and mid-price
- Volatility Tracking - Monitor price volatility in real-time
- Dark Mode UI - Professional trading terminal aesthetic
- Responsive Layout - Organized display with order book tables and visualization widgets

## Quick Start

### Prerequisites

- Java 11 or higher ([Download JDK](https://adoptium.net/))
- Maven 3.6 ([Download Maven](https://maven.apache.org/download.cgi))
- Internet connection (for Binance WebSocket data)

### Installation & Running

1. Clone the repository
   ```bash
   git clone https://github.com/JamalNkaisseri/OrderBookVisualizer.git
   cd OrderBookVisualizer
   ```

2. Compile the project
   ```bash
   mvn clean compile
   ```

3. Run the application
   ```bash
   mvn javafx:run
   ```

The application will launch and automatically connect to Binance's WebSocket to stream live order book data.

## 🎯 Usage

### Switching Trading Pairs

Use the dropdown menu at the top of the application to switch between different trading pairs:
- BTC/USDT
- ETH/USDT
- BNB/USDT
- ADA/USDT
- SOL/USDT
- XRP/USDT
- DOT/USDT
- AVAX/USDT
- MATIC/USDT
- LINK/USDT

### Understanding the Interface

Order Book Tables
- BIDS (Green) - Buy orders sorted by highest price first
- ASKS (Red) - Sell orders sorted by lowest price first
- Columns show: Price, Amount, and Total volume

Depth Chart
- Visual representation of cumulative bid and ask volumes
- X-axis: Price levels
- Y-axis: Cumulative volume

Market Info Widget
- Current spread between best bid and ask
- Total bid and ask volumes
- Mid-market price

Volatility Widget
- Real-time price volatility metrics
- Helps gauge market activity

##  Technology Stack

- Java 11 - Core programming language
- JavaFX - Modern GUI framework for rich desktop applications
- Maven - Build automation and dependency management
- WebSocket - Real-time data streaming from Binance API
- Binance API - Market data source

## 📁 Project Structure

```
OrderBookVisualizer/
├── src/main/
│   ├── java/com/visualizer/
│   │   ├── OrderBookVisualizer.java    # Main application entry
│   │   ├── OrderBookView.java          # Main UI layout
│   │   ├── OrderBookTableView.java     # Order book table component
│   │   ├── DepthChartView.java         # Depth chart visualization
│   │   ├── MarketInfoWidget.java       # Market metrics display
│   │   ├── VolatilityWidget.java       # Volatility tracking
│   │   ├── WebSocketClient.java        # Binance WebSocket handler
│   │   ├── Order.java                  # Order data model
│   │   └── TradingPair.java            # Trading pair model
│   └── resources/
│       └── styles.css                  # Application styling
├── screenshots/                        # Application screenshots
├── pom.xml                            # Maven configuration
└── README.md                          # This file
```

##  Building from Source

### Compile Only
```bash
mvn clean compile
```

### Package as JAR
```bash
mvn clean package
```

The packaged JAR will be in the `target/` directory.

### Run Tests (if applicable)
```bash
mvn test
```

## Data Source

This application uses [Binance's WebSocket API](https://binance-docs.github.io/apidocs/spot/en/#websocket-market-streams) for real-time market data. No API key is required for public market data streams.

WebSocket Endpoint `wss://stream.binance.com:9443/ws/{symbol}@depth`

## Contributing

Contributions are welcome! Here's how you can help:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

### Ideas for Contribution
- Add more exchanges (Coinbase, Kraken, etc.)
- Implement trade history visualization
- Add order book heatmap
- Export data to CSV
- Add sound alerts for price movements
- Implement custom trading pair input

##  License

This project is open source and available under the [MIT License](LICENSE).

##  Known Issues

- WebSocket reconnection on connection loss needs improvement
- High-frequency updates may cause UI lag on slower systems

## 📧 Contact

Jamal Nkaisseri - [@JamalNkaisseri](https://github.com/JamalNkaisseri)

x - @Jamxl98

Project Link: [https://github.com/JamalNkaisseri/OrderBookVisualizer](https://github.com/JamalNkaisseri/OrderBookVisualizer)

##  Acknowledgments

- [Binance API Documentation](https://binance-docs.github.io/apidocs/)
- [JavaFX Documentation](https://openjfx.io/)
- [Maven](https://maven.apache.org/)

---

⭐ **Star this repo** if you find it useful!

💡 **Have suggestions?** Open an issue or submit a pull request!
