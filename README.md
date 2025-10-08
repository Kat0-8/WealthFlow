# WealthFlow
A **RESTful financial portfolio tracker** built with **Java Spring Boot**, **PostgreSQL**, and **jOOQ**.  
WealthFlow provides secure user authentication, real-time market data (via external APIs), asset tracking, favorite management, and email-based price notifications.

---

## 🚀 Tech Stack

| Layer | Technology                                 |
|-------|--------------------------------------------|
| **Language** | Java 17, TypeScript                        |
| **Framework** | Spring Boot, React                         |
| **Database** | PostgreSQL                                 |
| **ORM / SQL Builder** | jOOQ                                       |
| **Security** | Spring Security + JWT (Bearer tokens)      |
| **API Clients** | Alpha Vantage (stocks), CoinGecko (crypto) |
| **Email Service** | SendGrid                                   |
| **Build Tool** | Maven                                      |
| **Scheduling** | Spring Scheduler                           |

---

## 🧠 Core Features

### 👤 Authentication & Authorization
- User registration & login with JWT tokens.
- Passwords hashed using **BCrypt**.
- Stateless API security (no sessions).

### 📈 Asset Management
- Search and cache assets (stocks & crypto).
- Fetch real-time and historical prices from external APIs.
- Maintain a local database of tracked assets and price history.

### ⭐ Favorites
- Add/remove assets to a personal watchlist.
- Retrieve favorite assets with latest price updates.

### 🔔 Price Notifications
- Create alert rules for target price thresholds (above/below).
- Scheduled task checks all active rules periodically.
- Trigger email notifications via **SendGrid**.

### 🌐 External Integrations
- **Alpha Vantage API** → Stock market data.
- **CoinGecko API** → Cryptocurrency data.
- **SendGrid API** → Email delivery.

---