<div align="center">

# 🥬 FreshTrack AI

**A local-first Android companion for tracking groceries, catching expiry risks, planning recipes, and turning missing ingredients into a shopping list.**

![Android](https://img.shields.io/badge/Android-API%2026%2B-3DDC84?style=flat-square&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?style=flat-square&logo=kotlin&logoColor=white)
![Room](https://img.shields.io/badge/Data-Room-2563EB?style=flat-square)
![AI](https://img.shields.io/badge/AI-Gemini%20%2B%20Gemma-8E75B2?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-111827?style=flat-square)

</div>

## 🎯 Why FreshTrack?

Groceries are easy to forget once they reach the fridge or pantry. FreshTrack keeps the inventory visible, highlights items that need attention, and helps reuse available ingredients before buying more.

## ✨ What It Does

- Maintains an offline inventory with quantities, categories, dates, expiry status, and notes.
- Supports manual entry plus editable review flows for food images, receipts, and nutrition labels.
- Sorts inventory by expiry or name and separates fresh, watch, critical, and expired items.
- Runs a daily `WorkManager` check and posts expiry notifications.
- Generates recipes from the current inventory through Gemini, with local Gemma fallback.
- Adds missing recipe ingredients to a persistent shopping list and merges duplicates.
- Stores inventory, shopping items, and cached recipe results locally.
- Lets users configure a Gemini API key and import or download a compatible local Gemma model at runtime.

## 📱 Verified App Views

The screens below are captured from the Android app with synthetic grocery data.

| Dashboard | Inventory |
|---|---|
| ![FreshTrack dashboard with synthetic grocery data](docs/screenshots/freshtrack-dashboard.png) | ![FreshTrack inventory with synthetic grocery data](docs/screenshots/freshtrack-inventory.png) |

## 🧭 Architecture

```mermaid
flowchart LR
    UI["Jetpack Compose screens"] --> VM["ViewModels + StateFlow"]
    VM --> REPO["InventoryRepository"]
    REPO --> ROOM[("Room database")]

    WORK["WorkManager expiry check"] --> ROOM
    WORK --> NOTIFY["Android notifications"]

    UI --> REVIEW["Editable scan review"]
    REVIEW --> GEMMA["Local Gemma via LiteRT-LM"]
    GEMMA --> VM

    VM --> RECIPE["Recipe generation"]
    RECIPE --> GEMINI["Gemini API"]
    GEMINI -. "missing key or service failure" .-> GEMMA

    VM --> RULES["Shelf-life and category rules"]
    RULES --> ROOM
```

The UI observes `StateFlow` data exposed by ViewModels. Repositories isolate Room access, while AI providers keep cloud and on-device inference paths separate from inventory persistence.

## Feature and Data Flow

1. Compose screens send user actions to `InventoryViewModel` or the recipe
   ViewModel instead of writing to Room directly.
2. `InventoryRepository` coordinates the inventory and shopping DAOs. DAO
   `Flow` results become `StateFlow`, so visible lists update when Room changes.
3. Manual or AI-assisted scans remain editable before mapped items are inserted
   into inventory.
4. `ExpiryCalculator` parses known date formats, calculates remaining days, and
   maps the result to fresh, watch, critical, or expired status. Rule-based shelf
   life provides a fallback when no reliable date is available.
5. WorkManager performs a periodic database check and creates local notifications
   for items that need attention without requiring the app to remain open.
6. Recipe generation starts with configured Gemini. Missing keys, quota or
   service failures can route to local Gemma when a compatible model is installed.
7. Missing recipe ingredients enter `addOrMergeShoppingItem`, which normalizes
   names, avoids duplicate rows, combines quantities, and retains recipe source.

### Why this architecture

- Room is the offline source of truth, while repository and ViewModel layers
  keep persistence out of UI composables.
- `StateFlow` represents observable screen state and survives asynchronous Room
  and AI operations more clearly than manually refreshing mutable lists.
- WorkManager fits deferrable daily expiry checks and respects Android background
  execution rules better than a permanently running service.
- Cloud Gemini improves convenience; local Gemma provides a privacy-conscious
  fallback, but both paths return reviewable suggestions rather than authoritative
  food-safety or nutrition decisions.

## Technical Checkpoints

| Topic | Source checkpoint |
|---|---|
| Room access and shopping-item merging | `app/src/main/java/my/edu/utar/freshtrackai/data/repository/InventoryRepository.kt` |
| Observable inventory and user actions | `app/src/main/java/my/edu/utar/freshtrackai/ui/inventory/InventoryViewModel.kt` |
| Expiry parsing and status rules | `app/src/main/java/my/edu/utar/freshtrackai/logic/ExpiryCalculator.kt` |
| Gemini failure classification and fallback | `app/src/main/java/my/edu/utar/freshtrackai/ai/GeminiRuntime.kt` |
| Recipe-generation state | `app/src/main/java/my/edu/utar/freshtrackai/ai/RecipeGenerationViewModel.kt` |
| Screen integration | `app/src/main/java/my/edu/utar/freshtrackai/ui/dashboard/DashboardRoute.kt` |

## Project Context

FreshTrack AI is a mobile-development group project. This README describes the
whole product and the responsibilities of its shared architecture. Ian Hong's
verified contribution focused on Compose UI components, user interactions,
screen-level integration, and connecting inventory, scan, recipe, shopping-list,
and expiry-aware workflows. It does not attribute every AI, Room, or background
worker component to one team member.

## 🧰 Technology

| Area | Implementation |
|---|---|
| UI | Kotlin, Jetpack Compose, Material 3 |
| State | Android ViewModel, Kotlin Coroutines, `StateFlow` |
| Persistence | Room, DAO interfaces, repository layer |
| Background work | WorkManager, Android notifications |
| Cloud AI | Google Gemini Android SDK |
| On-device AI | Gemma through Google AI Edge LiteRT-LM |
| Media | Camera/gallery document access, Coil |
| Testing | JUnit, coroutine test utilities, AndroidX test libraries |

## 🚀 Run Locally

### Requirements

- Android Studio with JDK 17+
- Android SDK 36
- Android device or emulator running Android 8.0+

```powershell
# Build the debug APK
.\gradlew.bat :app:assembleDebug

# Run local unit tests
.\gradlew.bat :app:testDebugUnitTest
```

Open the project in Android Studio, select the `app` configuration, and run it on a device or emulator. Core inventory and shopping-list features work without an API key.

For AI features:

1. Import or download a compatible `.litertlm` Gemma model from Profile Settings.
2. Add and validate a Gemini API key only when cloud recipe or estimation support is needed.
3. Keep API keys and model binaries outside version control.

## ✅ Validation and Boundaries

- Unit coverage includes repository behavior, sorting, dashboard data, expiry calculations, scan mapping, Gemini runtime handling, model downloads, and recipe generation.
- Food-image extraction and receipt parsing use the configured local Gemma model; they are unavailable until a compatible model is installed.
- Gemini calls require network access and a user-supplied key. Recipe generation can fall back to Gemma when the cloud path is unavailable.
- Room is the source of truth for app data. There is no account system, cloud synchronization, or cross-device backup.
- Database migrations currently use destructive fallback, so schema upgrades can clear local data.
- AI output remains reviewable and should not be treated as authoritative nutrition or food-safety advice.

## 📄 License

Released under the [MIT License](LICENSE).
