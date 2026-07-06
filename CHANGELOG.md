# Changelog

All notable changes to AllyPhone will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [0.8a] - Unreleased

### Added
- ATM registry (`/atm add|remove|list`) and a "Find Nearest ATM" screen in Wallet, sorted by distance with coordinates — physical cash withdrawal itself happens in AlsBanker.
- Persistent per-player alert inbox (`AlertService`/`AlertSQLService`) with a console-only `/phonealert <player> <source> <message>` bridge other plugins (e.g. AlsBanker) use to push notifications; shown with an unread badge in AlertsApp.
- One-click "Pay Bill Now" button in Wallet, and a generic `command:<cmd>` GUI action so any app icon can dispatch a real command as the clicking player.
- Click-to-SMS: clicking a friend in FriendsApp now prompts an in-chat compose flow instead of requiring `/sms` from memory.
- Wallet now shows Savings and Stocks sections (via new `BankingService.SavingsSummary`/`StockHolding`), ready to display real data once AlsBanker's API exposes it.
- `CompositeBankingService`: Vault is now the balance of record (matching `%vault_eco_balance%`), with AlsBanker layered in only for loan summary/transaction history when both are installed.
- Real BlueMap integration (`TowerMapIntegration`) — cell towers now render as live POI markers instead of a placeholder no-op.
- TownyApp: live town/nation info (via PlaceholderAPI) plus buttons that run real Towny commands as the player.
- PlotsApp: shows Towny plot/claim ownership info and claim/forsale/unclaim buttons when Towny is present, instead of a generic message.
- New apps: `JobsApp` (EcoJobs), `PetsApp` (GPet), `QuestsApp` (Quests), `MusicApp` (GMusic) — each opens the target plugin's own GUI via command dispatch.
- Phone item switched from a player-head texture to a plain item with `CustomModelData(1)`, with a starter resource pack (`resourcepack/`) providing the model override and a placeholder icon.
- Phone item now shows a live unread-message notification badge (lore + enchant glow), refreshed every few seconds.
- Default installed apps expanded to Alerts, Wallet, Friends, Messages, News, Weather, Servers, Towny, Plots, App Store, Help.
- `PhoneHomeGUI` and every other app screen restyled with a dark bezel border, status bar, and bottom dock so they read as a phone display rather than a chest inventory.

### Fixed
- In-memory caching for cell towers so signal checks and repeating tasks stop re-querying SQLite every cycle.
- `InventoryDragEvent` is now cancelled in phone GUIs, preventing item loss from dragging items into an open phone menu.
- Cell tower removal now actually requires a shift-click, matching its own lore.

## [0.6] - Unreleased

### Added
- Phone item with home screen GUI and app grid (`PhoneHomeGUI`).
- Cell tower / signal system gating phone usage by proximity, configurable via `signal-radius` and `towers-enabled`.
- Apps: Messages, News, Alerts, Wallet, Weather, Friends, Plots, Towny, Servers, Tower Manager, App Store, Extras, Help.
- SMS messaging (`/sms`) and city news posting (`/phonenews`) with SQL-backed history.
- Cell tower management command (`/celltower`) and BlueMap integration for visualizing towers.
- Banking integration with AlsBanker as the primary backend and Vault as a fallback.
- Recurring monthly billing for phone service (`BillingService`).
- PlaceholderAPI expansion (`AllyPhoneExpansion`).
- Discord webhook notifications (`DiscordWebhook`).
- Startup error handling that logs failures to `logs/error-log.log` and safely disables the plugin on enable failure.
