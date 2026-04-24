# Fresh Track AI Handoff

## Current Local State
- Branch baseline: `origin/dev` at `5bb13db`
- Current workspace: local `repo/` contains substantial uncommitted progress beyond GitHub `dev`
- Build verification on the current local tree:
  - `:app:assembleDebug` passes
  - `:app:testDebugUnitTest` passes

## Architecture Summary
- UI:
  - Kotlin + Jetpack Compose
  - Root route: `app/src/main/java/my/edu/utar/freshtrackai/ui/dashboard/DashboardRoute.kt`
- Data:
  - Room under `data/local`
  - `InventoryRepository`
  - `InventoryViewModel`
- AI runtime split:
  - Gemma local:
    - food image detection
    - receipt OCR/parsing
    - recipe fallback generation
    - nutrition label scan
    - nutrition estimate by item name
  - Gemini cloud:
    - primary recipe generation
    - categorization / shelf-life estimation

## Major Local Progress Beyond `origin/dev`
- Scan flow simplified into a single-image confirm flow with clearer preview and action states
- Recipe flow unified from mixed mock/live state
- Recipe flow is now a single-screen generated list:
  - no separate `Recipe View All` route
  - no recipe customization controls
  - generated recipes are cached locally and restored after app relaunch
  - previously cached recipes stay visible while a new generation request is loading
  - cached recipes are replaced only after a new generation succeeds
- Gemini-backed external API use now reads a user-entered Settings key at runtime
- Settings API-key controls now include:
  - masked key entry
  - save
  - clear
  - test key
- Settings API-key validation now distinguishes:
  - invalid key
  - quota/credits exhausted
  - generic request failure
- Recipe runtime now classifies Gemini failures and falls back to local Gemma more truthfully
- AI categorization / expiry estimation still uses Gemini first, but falls back to offline category/default shelf-life rules when Gemini is unavailable
- Local Gemma fallback recipe parsing is hardened against noisy model output
- Nutrition moved from Gemini-first to local Gemma-first
- Nutrition label scan now supports both camera and gallery
- Food/receipt scan review now enriches placeholder nutrition and expiry before showing review items:
  - nutrition placeholder text triggers local Gemma nutrition estimation
  - placeholder estimated expiry is re-resolved through Gemini-first shelf-life logic with offline fallback
- Startup Gemma recovery now uses real model status and reprompts when the saved model file is missing
- Shopping list duplicate merging and quantity aggregation implemented
- Android 13+ notification permission flow added
- Notification settings now use the system settings link only from the quick-settings sheet
- Gemma settings now support a download-first flow:
  - `Download Gemma 4` starts an Android `DownloadManager` download to `Downloads`
  - status/progress is shown inside Settings
  - `Choose Model` still imports the downloaded file into app storage
- Inventory/home/review formatting improved:
  - exact added dates
  - quantity normalization
  - inventory sorting with persistence
- Generated recipe placeholders improved and remote stock images remain removed
- Launcher icon adaptive composition cleaned up
- Legacy duplicate Room package removed

## Important Runtime Notes
- Prefer a real Android phone for Gemma testing
- Gemma model is chosen through Android document picker and copied into app storage
- Current Gemma runtime path is CPU-backed in app code, so performance depends mainly on the phone hardware
- A temporary Gemini key was checked during validation and returned `429 RESOURCE_EXHAUSTED`; that key was not written into project files
- Later Settings API-key validation work confirmed that depleted-credit keys must be reported as quota exhausted rather than invalid
- Recipe persistence now uses local app preferences storage rather than a process-only ViewModel cache
- Scan review enrichment now runs after food image detection / receipt OCR and before the review screen is shown

## Docs To Read First
- `BUILD_TOOLCHAIN.md`
- `AI_FEATURE_RUNTIME.md`
- `../context/PROJECT_CONTEXT.md`
- `../context/NEW_THREAD_BOOTSTRAP.md`
- `../context/project_context.jsonl`

## Push / Review Reminder
- Treat `origin/dev` as the pushed team baseline
- Treat the local working tree as the latest continuation state
- Before pushing, compare with:
  - `git diff origin/dev -- .`
