# Google AdMob integration

## Placement (policy-safe)

| Screen | Banner | Interstitial | Rationale |
|--------|--------|--------------|-----------|
| Home | Yes (above bottom nav) | No | Stable browsing; 16dp gap from FAB/nav |
| Settings | Yes (above bottom nav) | No | Non-critical screen |
| Scanner (camera/crop/filter) | **No** | **No** | Primary task — policy violation risk |
| Save success | **No** | **No** | Do not block export actions |
| PDF viewer | **No** | **No** | Reading experience |
| Selection mode (Home) | **Hidden** | No | Avoid ads near bulk actions |

**Interstitial (optional):** Only when user taps **Back to home** after a successful save, with Remote Config `enable_interstitial_ads` (default `false`) and `interstitial_cooldown_seconds` (default 300).

## Configuration

- **Firebase project:** `lzyscan` (same as analytics)
- **AdMob:** Create apps in [AdMob](https://admob.google.com) for each package (`dev`, `staging`, `prod`) and replace unit IDs in `app/build.gradle.kts` prod flavor.
- **Test ads:** Google test unit IDs are used in `dev`/`staging` and until prod IDs are set (`USE_TEST_AD_UNITS`).

## Remote Config keys

- `enable_ads` — master switch
- `enable_banner_ads` — banner on Home/Settings
- `enable_interstitial_ads` — post-save interstitial (default off)
- `interstitial_cooldown_seconds` — min seconds between interstitials (≥ 60)

## Consent (UMP)

`AdConsentManager` runs on first `MainActivity` launch when ads are enabled (EEA/UK).

## Files

- `core/ads/` — SDK init, consent, policy, interstitial
- `presentation/ads/BannerAdSlot.kt` — Compose adaptive banner
- `presentation/features/main/MainScreen.kt` — banner in `bottomBar`
