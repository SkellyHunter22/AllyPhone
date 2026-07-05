# AllyPhone

A Paper/Bukkit plugin that adds an in-game smartphone item to Minecraft, complete with a signal/cell-tower system, a suite of "apps" (GUIs), SMS messaging, city news, and integrations with economy, land-claim, and map plugins.

- **API version:** 1.20 (Paper `1.20.4-R0.1-SNAPSHOT`)
- **Main class:** `com.allyphone.AllyPhonePlugin`
- **Author:** SkellyHunter22

## Features

- **Phone item & GUI** — right-click the phone item to open a home screen (`PhoneHomeGUI`) with an app grid, similar to a real smartphone.
- **Cell tower / signal system** — phones only work within range of a placed cell tower (`signal-radius` in `config.yml`); towers are managed via `/celltower` and rendered on the map through BlueMap integration.
- **Apps:**
  - `MessagesApp` — SMS-style messaging between players (`/sms`), backed by a SQL-stored message history.
  - `NewsApp` — city/server news feed (`/phonenews` to post), stored in SQL.
  - `AlertsApp` — server alerts/notifications.
  - `WalletApp` — balance and transactions, backed by AlsBanker (preferred) or Vault as a fallback economy provider.
  - `WeatherApp` — in-world weather/forecast display.
  - `FriendsApp` — friends list management.
  - `PlotsApp` / `TownyApp` — land-claim integrations.
  - `ServersApp` — server/network info.
  - `TowerManagerApp` — in-phone cell tower management.
  - `AppStoreApp` — enable/install additional apps (`InstalledAppsStore`).
  - `ExtrasApp`, `HelpApp` — misc utilities and in-game help.
- **Billing** — recurring monthly phone service cost (`monthly-cost` in `config.yml`) handled by `BillingService`.
- **PlaceholderAPI expansion** — exposes AllyPhone data as placeholders via `AllyPhoneExpansion`.
- **Discord webhook support** — outbound notifications via `DiscordWebhook`.
- **Persistent storage** — SQL-backed message and news history (`Database`, `MessageSQLService`, `NewsSQLService`).

## Commands

| Command | Description |
|---|---|
| `/phone` | Open your AllyPhone |
| `/sms` | Send an SMS |
| `/phonenews` | Post city news |
| `/celltower` | Manage cell towers |

## Dependencies

| Plugin | Requirement |
|---|---|
| Paper API 1.20.4 | Required (compile/runtime) |
| AlsBanker | Optional — preferred economy backend |
| Vault | Optional — fallback economy backend if AlsBanker isn't present |
| BlueMap | Optional — renders cell towers on the web map |
| PlaceholderAPI | Optional — enables `%allyphone_*%` placeholders |

If neither AlsBanker nor Vault is installed, wallet/billing features are disabled but the plugin still enables.

## Configuration

`config.yml`:

```yaml
phone:
  monthly-cost: 500
  signal-radius: 500
  towers-enabled: true
```

- `monthly-cost` — recurring charge for phone service.
- `signal-radius` — block radius around a cell tower where phones have signal.
- `towers-enabled` — toggles the cell tower/signal requirement entirely.

## Building

```bash
mvn clean package
```

Requires Java 21. The resulting jar is placed in `target/` and can be dropped into a Paper server's `plugins/` folder.

## Errors & Logging

Startup failures are caught, logged to `logs/error-log.log`, and the plugin safely disables itself rather than leaving the server in a bad state (`PluginLogService`).
