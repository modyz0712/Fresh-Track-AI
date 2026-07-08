<div align="center">

# FreshTrack AI

AI-assisted Android grocery inventory, expiry reminder, recipe suggestion, and shopping-list app for smarter household food management.

![Android](https://img.shields.io/badge/Android-Kotlin%20%2B%20Compose-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Room](https://img.shields.io/badge/Storage-Room%20Database-2563EB?style=for-the-badge)
![Gemini](https://img.shields.io/badge/AI-Gemini%20API-7C3AED?style=for-the-badge)
![Gemma](https://img.shields.io/badge/Local%20AI-Gemma%20LiteRT--LM-111827?style=for-the-badge)
![License](https://img.shields.io/badge/License-MIT-111827?style=for-the-badge)

</div>

---

## Table of Contents

- [About](#about)
- [Problem Scope](#problem-scope)
- [Key Features](#key-features)
- [AI Runtime Design](#ai-runtime-design)
- [Architecture](#architecture)
- [Repository Structure](#repository-structure)
- [Getting Started](#getting-started)
- [Proposal Deviations](#proposal-deviations)
- [License](#license)

## About

FreshTrack AI is a mobile application developed for household grocery management. It helps users record food items, monitor expiry status, reuse existing ingredients through recipe suggestions, and prepare shopping lists for missing items.

The app is designed for students, working adults, families, and small households that want a low-friction alternative to manual pantry tracking or expensive smart-fridge hardware. It aligns with SDG 12, Responsible Consumption and Production, by encouraging better food visibility and reducing avoidable food waste.

## Problem Scope

Many households buy groceries without a clear way to track what was purchased, when it was added, and when it may expire. This can lead to forgotten ingredients, duplicate purchases, poor meal planning, and unnecessary food waste.

FreshTrack AI addresses this with:

- AI-assisted food image scan and receipt parsing.
- Editable review before saving detected items.
- Local inventory storage on the device.
- Expiry estimation and daily reminder notifications.
- Recipe suggestions based on current inventory.
- Shopping list generation from missing recipe ingredients.

## Key Features

### Smart Food Scan and Receipt Parsing

- Supports food image scan and receipt image parsing.
- Uses the local Gemma LiteRT-LM path for image-based extraction.
- Converts scan results into editable item entries before saving.
- Allows users to correct item names, categories, quantities, expiry data, and nutrition notes.

### Inventory Management

- Stores grocery and pantry items locally using Room Database.
- Supports add, edit, delete, retrieve, and sorted display workflows.
- Uses ViewModel and Kotlin Flow so UI state follows the latest stored data.
- Keeps inventory available offline after the app is closed.

### Expiry Estimation and Notification

- Uses a three-tier expiry approach:
  - user-entered or scanned expiry date
  - offline category and shelf-life rules
  - Gemini fallback for more specific shelf-life estimation
- Classifies items as Fresh, Watch, Critical, or Expired.
- Uses WorkManager for daily background expiry checks and notifications.

### AI Recipe Suggestion

- Generates recipes from the user's current inventory summary.
- Uses Gemini as the primary recipe generation path.
- Falls back to local Gemma when Gemini is unavailable, invalid, or quota-limited.
- Caches generated recipes locally so previous results remain visible after relaunch.

### Shopping List Generation

- Adds missing ingredients from recipe results into a separate shopping list.
- Normalizes duplicate ingredient names.
- Aggregates quantity counts to reduce repeated list entries.

### Runtime Settings

- Lets users enter, save, clear, and test a Gemini API key at runtime.
- Lets users download or choose a Gemma model for local AI features.
- Surfaces model and API-key status inside the app instead of requiring hardcoded secrets.

## AI Runtime Design

| Feature | Primary Path | Fallback / Notes |
|---|---|---|
| Food image detection | Local Gemma | User can edit results before saving |
| Receipt parsing | Local Gemma | Receipt OCR endpoint from proposal was not used |
| Nutrition label scan | Local Gemma | Supports camera and gallery image selection |
| Nutrition AI fill | Local Gemma | Stores output in editable notes |
| Recipe generation | Gemini | Falls back to local Gemma |
| Category / expiry assistance | Gemini | Falls back to offline shelf-life rules |

This hybrid design keeps the core inventory workflow usable offline while still using Gemini where cloud AI provides stronger structured reasoning.

## Architecture

```text
Jetpack Compose UI
        |
        v
ViewModels + Dashboard / Scan / Recipe / Inventory Screens
        |
        v
InventoryRepository
        |
        v
Room Database

AI support:
Gemma LiteRT-LM  -> food scan, receipt parsing, nutrition, recipe fallback
Gemini API      -> recipe generation, shelf-life/category assistance
WorkManager     -> daily expiry checks and notifications
```

## Repository Structure

```text
Fresh-Track-AI/
|-- app/
|   |-- src/main/java/my/edu/utar/freshtrackai/
|   |   |-- ai/               Gemini, Gemma, recipe, OCR, scan mapping
|   |   |-- data/             Room database, DAO, entities, repository
|   |   |-- logic/            expiry rules, notifications, nutrition support
|   |   |-- ui/               Jetpack Compose screens and dashboard flow
|   |   `-- MainActivity.kt
|   |-- src/main/res/         launcher icon, themes, XML config
|   `-- build.gradle.kts
|-- gradle/libs.versions.toml
|-- AI_FEATURE_RUNTIME.md
|-- BUILD_TOOLCHAIN.md
|-- README_Handoff.md
|-- LICENSE
`-- README.md
```

## Getting Started

### Prerequisites

- Android Studio
- JDK 17 or newer
- Android SDK 36
- Android device or emulator, preferably a real device for Gemma testing

### Build

On Windows PowerShell:

```powershell
$env:JAVA_HOME='C:\Program Files\Android\Android Studio\jbr'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :app:assembleDebug
```

### Run Unit Tests

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

### Runtime Setup

1. Open the app.
2. Use Settings to enter and test a Gemini API key if cloud AI features are needed.
3. Use Settings to download or choose a Gemma `.litertlm` model for local AI features.
4. Use Smart Scan, inventory, recipe, and shopping-list flows from the dashboard.

## Proposal Deviations

The final implementation differs from the original proposal in a few intentional ways:

- Food scan and receipt parsing use local Gemma instead of separate external AI/OCR REST endpoints.
- Firebase cloud backup and cross-device sync are not implemented; inventory is stored locally through Room.
- A nutrition information card was added even though it was not part of the original core proposal.
- Expiry estimation was extended beyond rule-based logic with Gemini fallback support.

These changes make the app more offline-capable and reduce dependence on backend services while still satisfying the assignment's external-service requirement through Gemini integration.

## License

This repository is released under the MIT License. See [LICENSE](LICENSE) for details.
