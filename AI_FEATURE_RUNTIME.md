# AI Feature Runtime Notes

This file documents which features currently rely on Gemini cloud calls, which rely on local Gemma, and which work across both paths.

## Feature Matrix

### Recipe generation
- Primary path: Gemini cloud
- Fallback path: local Gemma

Reason:
- Recipe generation builds one shared inventory prompt in the app layer and sends that same prompt into the Gemini path or the local Gemma fallback path.
- The Gemini key is read from the user-entered Settings value at runtime.
- Missing key, invalid key, or depleted quota skip straight to local Gemma.
- Only temporary busy/unavailable responses retry before local fallback.
- Generated recipes are cached locally and restored after app relaunch.
- Existing cached recipes remain visible while a new generation request is loading.
- Cached recipes are replaced only after a new generation succeeds.

### Food categorization / expiry estimation
- Primary path: Gemini cloud
- Fallback path: offline category/default shelf-life rules

Reason:
- `AiCategorizer` now reads the same Settings-saved Gemini key used by recipe generation.
- If no key is saved or Gemini fails, the app falls back to local shelf-life/category rules instead of Gemma.

### Food image detection
- Path: local Gemma
- Quantity/unit extraction: best effort only
- Frontend should allow user correction

### Receipt parsing
- Path: local Gemma
- Quantity/unit extraction: best effort only
- Frontend should allow user correction

### Nutrition label scan
- Primary path: local Gemma
- Requires a configured local Gemma model with image support
- UI supports both:
  - camera capture
  - gallery image selection

### Nutrition AI fill by item name
- Primary path: local Gemma
- Requires a configured local Gemma model

## Gemini key behavior
- The app no longer depends on `local.properties`/`BuildConfig` Gemini keys for normal runtime behavior.
- Users enter the Gemini key in Settings.
- Settings now support:
  - save key
  - clear key
  - test key
- Validation distinguishes:
  - missing key
  - valid key
  - invalid key
  - quota/credits exhausted
  - generic request failure
- Depleted-credit keys reach Gemini successfully but should be reported as quota exhausted, not as invalid keys.
- If the saved key is missing, recipe generation skips to local Gemma instead of pretending cloud generation works.

## Startup model recovery
- App startup now checks real Gemma model status, not just whether a path string exists.
- `Configured` continues normally.
- `Not set` reopens model selection.
- `Missing file` reopens model selection and surfaces that the previous model file is gone.

## Gemma model setup flow
- Settings now expose:
  - `Download Gemma 4`
  - `Choose Model`
- `Download Gemma 4` uses Android `DownloadManager` to place the verified `.litertlm` file in the device `Downloads` folder.
- The app then relies on `Choose Model` to import that downloaded file into app-private storage.
- Settings surface download state such as:
  - queued
  - running with percentage when available
  - complete and ready to import
  - failed
- The download-start toast is intentionally short; ongoing state is shown in the Settings card instead of long popup text.

## Recipe list behavior
- The main recipe page owns the full generated list directly.
- There is no separate `Recipe View All` screen.
- The recipe page keeps showing the previous generated list while a new generation request is loading.

## Settings quick actions
- Notifications now expose only `Open Settings` in the quick-settings sheet.
- The previous in-sheet `Send Test` notification button has been removed.
