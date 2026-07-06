# AllyPhone

A Paper/Bukkit plugin that adds an in-game smartphone item to Minecraft, complete with a signal/cell-tower system, a suite of "apps" (GUIs) styled like a real phone home screen, SMS messaging, city news, and integrations with economy, land-claim, jobs/pets/quests, music, and map plugins.

- **Version:** 0.8a
- **API version:** 1.20 (Paper `1.20.4-R0.1-SNAPSHOT`)
- **Main class:** `com.allyphone.AllyPhonePlugin`
- **Author:** SkellyHunter22

## Features

- **Phone item & GUI** — right-click the phone item to open a home screen (`PhoneHomeGUI`) styled like a real phone (dark bezel border, status bar, bottom dock), with an installable app grid. The phone item itself uses `CustomModelData` so a resource pack can skin it (see `resourcepack/`).
- **Notification badge** — the phone item's lore/glow updates live to show unread SMS count.
- **Cell tower / signal system** — phones only work within range of a placed cell tower (`signal-radius` in `config.yml`); towers are managed via `/celltower`, registered in-world by sneak-right-clicking a beacon while holding the phone, and rendered live on the map through a real BlueMap API integration.
- **Apps** (installable per-player via the App Store; a sensible default set is granted on first join):
  - `MessagesApp` — SMS-style messaging (`/sms`, or click a friend in Friends to compose in chat), SQL-backed history, notification badge.
  - `NewsApp` — city/server news feed (`/phonenews` to post), SQL-backed, broadcasts + optional Discord webhook.
  - `AlertsApp` — persistent per-player alert inbox; other plugins (e.g. AlsBanker) push into it via the console-only `/phonealert` bridge.
  - `WalletApp` — balance, loans, transaction history, savings/stocks (via AlsBanker), a one-click "Pay Bill Now" button, and a "Find Nearest ATM" locator.
  - `CustomizeApp` — bezel theme, phone nickname, and home-screen app ordering.
  - `AdminApp` (`allyphone.admin` only) — one-click buttons for this plugin's admin commands and common server-admin actions.
  - `WeatherApp` — in-world weather/forecast display.
  - `FriendsApp` — online players list; click one to compose an SMS.
  - `PlotsApp` — shows Towny plot/claim info (via PlaceholderAPI) when Towny is present, with claim/forsale/unclaim buttons; falls back to a generic PlotSquared notice otherwise.
  - `TownyApp` — live town/nation info plus buttons that run real Towny commands (`/town`, `/nation`, `/resident`, etc.) as the player.
  - `ServersApp` — BungeeCord/Velocity server switcher (reads a `servers:` list from `config.yml`).
  - `TowerManagerApp` — lists/removes registered cell towers with distance info.
  - `JobsApp` — opens EcoJobs' own GUI.
  - `PetsApp` — opens GPet's buy/manage GUI, plus a "My Pets" screen (backed by GPet's own `PetService`) listing your currently-spawned pets by pet ID with a one-click hide/despawn action that keeps ownership, name, and data intact.
  - `QuestsApp` — opens the Quests plugin's GUI.
  - `MusicApp` — play/pause/skip/stop controls for GMusic.
  - `AppStoreApp` — install/uninstall optional apps (`InstalledAppsStore`).
  - `ExtrasApp`, `HelpApp` — misc utilities and in-game help.
  - Any app icon can be tagged to run a real command as the clicking player via a generic `command:<cmd>` action.
- **Billing** — recurring monthly phone service cost (`monthly-cost` in `config.yml`) handled by `BillingService`, payable early via Wallet's Pay Bill button.
- **Banking** — Vault is the balance of record (so it matches economy placeholders like `%vault_eco_balance%`); AlsBanker, when also present, layers in loan summary and transaction history on top via `CompositeBankingService`.
- **PlaceholderAPI expansion** — exposes AllyPhone data as placeholders via `AllyPhoneExpansion`.
- **Discord webhook support** — outbound news notifications via `DiscordWebhook`.
- **Persistent storage** — SQLite-backed messages, news, alerts, cell towers, ATMs, phone accounts, and installed-app state.

## Commands

| Command | Description |
|---|---|
| `/phone` | Open your AllyPhone |
| `/sms` | Send an SMS |
| `/phonenews` | Post city news |
| `/celltower` | Manage cell towers |
| `/atm` | Manage ATM locations shown in the Wallet app |
| `/phonealert` | Console-only bridge other plugins use to push a phone notification |

## Dependencies

| Plugin | Requirement |
|---|---|
| Paper API 1.20.4 | Required (compile/runtime) |
| Vault | Optional — preferred economy backend (balance of record) |
| AlsBanker | Optional — adds loan summary, transaction history, and the alert bridge on top of Vault (or stands alone if Vault isn't present) |
| BlueMap | Optional — renders cell towers as live map markers |
| PlaceholderAPI | Optional — enables `%allyphone_*%` placeholders and Towny plot/town info |
| Towny | Optional — powers TownyApp and PlotsApp's live town/plot info and commands |
| EcoJobs | Optional — powers JobsApp |
| GPet | Optional — powers PetsApp, including a direct `PetService` API integration for "My Pets" (compiled against GPet 1.15.2, `provided` scope) |
| GMusic | Optional — powers MusicApp |
| Quests | Optional — powers QuestsApp |

All integrations are soft dependencies — the plugin enables fine without any of them, with each affected app showing a "not installed" notice instead. If neither AlsBanker nor Vault is installed, wallet/billing features are simulated only.

## Resource Pack

The phone item uses `CustomModelData(1)` on a Paper item so a resource pack can give it a custom icon without affecting normal paper. A starter pack (model override + a placeholder icon) lives in `resourcepack/`; swap the texture at `resourcepack/assets/minecraft/textures/item/allyphone_phone.png` for your own art, then re-zip and host it (see `server.properties`'s `resource-pack`/`resource-pack-sha1`/`require-resource-pack` options to serve it to all players automatically).

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
