# Changelog

All notable changes to AllyPhone will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

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
