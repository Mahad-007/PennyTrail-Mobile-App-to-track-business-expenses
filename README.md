# PennyTrail

A modern Android app for tracking business finances — expenses, sales, inventory, and customer credits — built with Kotlin and Jetpack Compose.

## Features

### Dashboard
- Today's and monthly sales, expenses, and profit/loss at a glance
- Outstanding credit summary
- Quick access to recent sales and expenses

### Sales Management
- Record sales with product selection, quantity, and unit price
- Date navigation with previous/next day and calendar picker
- Sales grouped by product with subtotals and daily totals

### Expense Tracking
- Log expenses with categories (Rent, Supplies, Utilities, Transport, Salary, Food, Other)
- View and filter expenses by date
- Track spending patterns over time

### Inventory
- Dedicated inventory tab showing all products with stock levels
- **Purchase rate** and **selling rate** displayed side-by-side
- Color-coded stock availability (green/amber/red)
- Add stock with purchase price tracking per batch
- Total inventory value calculation
- Full stock history with purchase prices

### Product Management
- Add, edit, and soft-delete products
- Default selling price per product
- Stock summary with total, sold, and available quantities
- Stock entry history with dates and notes

### Credit Tracking
- Record credit given to customers
- **Partial payment support** — track how much is paid and remaining
- Payment history per credit with dates and notes
- Progress bar showing payment status
- Auto-marks as fully paid when balance reaches zero
- Filter between all credits and unpaid only
- Outstanding balance displayed in app bar

### Data Export
- Export all data to CSV files (expenses, sales, products, stock entries, credits, credit payments)
- Share via WhatsApp, Gmail, Google Drive, or any app using Android's share sheet

### Dark Mode
- Full dark mode support with theme-aware colors
- Adapted card backgrounds and semantic colors for readability
- Splash screen with dark variant

## Screenshots

| Dashboard | Sales | Inventory | Credits |
|-----------|-------|-----------|---------|
| Summary cards with profit/loss | Date navigation, product grouping | Purchase & selling rates | Partial payments with progress |

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin 2.0.21 |
| UI | Jetpack Compose (BOM 2024.12.01) |
| Design System | Material Design 3 |
| Navigation | Navigation Compose 2.8.5 |
| Database | Room 2.6.1 (SQLite) |
| Async | Kotlin Coroutines 1.9.0 + Flow |
| Build | Gradle 8.11.1, AGP 8.7.3, KSP |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 (Android 15) |

## Architecture

```
MVVM + Repository Pattern

UI (Compose Screens)
  └── ViewModels (StateFlow)
        └── Repositories
              └── DAOs (Room)
                    └── SQLite Database
```

- **Data Layer**: Room entities, DAOs, and repository wrappers
- **UI Layer**: Jetpack Compose screens with Material 3 components
- **State Management**: ViewModels with `StateFlow`, `combine()`, and `flatMapLatest()`
- **Dependency Injection**: Manual service locator via `PennyTrailApp` (Application class)
- **Navigation**: Compose Navigation with sealed class routes and bottom navigation bar

## Project Structure

```
app/src/main/kotlin/com/expense/tracker/
├── PennyTrailApp.kt              # Application class (DI container)
├── MainActivity.kt                # Entry point with splash screen
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt        # Room database (v3) with migrations
│   │   ├── Converters.kt         # LocalDate type converters
│   │   ├── dao/                   # 6 DAOs (Expense, Sale, Product, Credit, StockEntry, CreditPayment)
│   │   └── entity/                # 7 entities + 2 query result classes
│   └── repository/                # 5 repositories wrapping DAOs
├── ui/
│   ├── components/                # Reusable composables (DatePicker, CurrencyTextField, Dialogs)
│   ├── navigation/                # Screen routes + AppNavigation with bottom bar
│   ├── screens/
│   │   ├── dashboard/             # Dashboard with summary cards
│   │   ├── expense/               # Expense list + add/edit
│   │   ├── sale/                  # Sales with date navigation
│   │   ├── inventory/             # Inventory overview with rates
│   │   ├── product/               # Product management + stock
│   │   └── credit/                # Credits with partial payments
│   └── theme/                     # Colors, Typography, Theme with dark mode support
└── util/
    ├── CurrencyUtils.kt          # PKR formatting (Rs. 1,234.56)
    ├── DateUtils.kt              # Date formatting and epoch conversion
    └── CsvExporter.kt            # Export all tables to CSV
```

## Database Schema

| Table | Key Fields |
|-------|-----------|
| `expenses` | date, amount, category, description |
| `sales` | date, productId, productName, quantity, unitPrice, totalAmount |
| `products` | name, defaultPrice, isActive, stockQuantity |
| `stock_entries` | productId, quantity, purchasePrice, note, date |
| `credits` | personName, amount, amountPaid, isPaid, date |
| `credit_payments` | creditId, amount, note, date |

Database version 3 with migrations:
- **v1 → v2**: Added stock management (stockQuantity + stock_entries table)
- **v2 → v3**: Added purchase price tracking + credit partial payments (amountPaid + credit_payments table)

## Getting Started

### Prerequisites
- Android Studio 2024.2+ (Ladybug or newer)
- JDK 17
- Android SDK 35

### Build & Run
1. Clone the repository
   ```bash
   git clone https://github.com/Mahad-007/PennyTrail-Mobile-App-to-track-business-expenses.git
   ```
2. Open in Android Studio
3. Sync Gradle and run on device/emulator (API 26+)

### Build from Command Line
```bash
./gradlew assembleDebug
```
APK will be at `app/build/outputs/apk/debug/app-debug.apk`

## Currency

The app uses **Pakistani Rupee (PKR)** with `Rs.` prefix formatting. To change currency, modify `util/CurrencyUtils.kt`.

## License

This project is for educational and personal use.
