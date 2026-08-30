# Dead Reckoning Pro Navigation

An Android vehicle-navigation research app that combines fused GNSS, phone IMU data, and an on-device V8 inertial model. It is designed to keep producing a short-horizon position estimate during a GNSS outage, while showing live navigation and road-impact telemetry.

> This is a research prototype, not a safety-critical navigation system. GNSS remains the trusted source when it is available; inertial estimates accumulate error during longer outages.

## What It Does

- Reads live accelerometer, gyroscope, magnetometer, and fused-location data from the phone.
- Shows the current location on an OpenStreetMap/OSMDroid map with a heading-aware position marker.
- Uses GNSS as the primary navigation source and calibration anchor.
- Runs the bundled V8 dead-reckoning model locally during GNSS loss.
- Detects strong road impacts with a conservative multi-sample pothole detector.
- Shows live IMU, GNSS, AI, navigation, diagnostics, and session screens through a Jetpack Compose UI.

## Navigation Pipeline

```text
Phone sensors -> SensorAdapter -> PotholeDetector -> alert/event stream
                   |
                   +-------------> V8 ONNX model -> speed, displacement,
                                                   heading delta, motion class

Fused location -> LocationAdapter -> GNSS navigation state and map position
                                          |
GNSS unavailable + V8 prediction --------+
                  +--------------> short-horizon dead-reckoning position
```

`LiveNavigationRepository` is the active runtime data source. It owns the live sensor, GNSS, V8 inference, map, and pothole event flow.

## V8 On-Device Model

The app ships a reviewed V8 ONNX model and its training normalization data in `app/src/main/assets/ml/`.

### Current V8 Evidence Gate

The bundled V8 candidate was selected on validation only and evaluated on a held-out IO-VNBD split. Its held-out report records 0.748 m/s speed MAE, 1.851 m position RMSE per window, and 81.1% motion accuracy. It does **not** currently meet the SIH trajectory-drift objective: mean final position error is 24.8 m at 10 seconds and 373.0 m at 60 seconds. The runtime therefore labels it experimental and verifies its SHA-256 and I/O contract before loading. A retrained candidate must update `v8_manifest.json` from the generated held-out report before deployment.

| Item | Value |
| --- | --- |
| Runtime | ONNX Runtime for Android |
| Input | 20 samples x 6 IMU channels |
| Sample rate | 10 Hz |
| Window duration | 2 seconds |
| Outputs | speed, forward/lateral displacement, heading delta, motion logits |
| Motion classes | stationary, driving straight, turning |
| Runtime behavior | Uses GNSS speed as the trusted seed; uses V8 predictions when GNSS is stale |

The model is intentionally not used as the sole long-distance position source. Its held-out evaluation shows drift that rises during long GNSS outages, so the app returns to GNSS as soon as a fresh fix is available.

### Exporting the Model

The included model assets were generated with:

```powershell
python tools/export_v8_model.py
```

The exporter expects the V8 training repository at `.codex-ml-codes-review/`. This directory is a local development dependency and should not be committed to this repository.

It writes:

```text
app/src/main/assets/ml/
|- v8_dead_reckoning.onnx
|- v8_manifest.json
`- v8_normalization.json
```

## Pothole Detection

`PotholeDetector` uses a rolling multi-sample impact check rather than a single gyroscope spike. A detection combines acceleration shock, rotational vibration, and cluster persistence. It reports `Minor`, `Moderate`, or `Severe` with a signal-derived confidence score.

Pothole monitoring starts with the IMU listener and is independent of an active navigation session. Alerts are rate-limited before they reach the UI.

## Requirements

- Android Studio with Android SDK Platform 34
- JDK 17
- Android device running Android 6.0 (API 23) or later
- A device with an accelerometer and gyroscope for full inertial features
- Location enabled on the device

## Build and Install

```powershell
.\gradlew assembleDebug
.\gradlew installDebug
```

The debug APK is written to:

```text
app/build/outputs/apk/debug/app-debug.apk
```

To run the focused detector tests:

```powershell
.\gradlew testDebugUnitTest --tests nisargpatel.deadreckoning.PotholeDetectorTest
```

## Permissions

The manifest declares location, sensor, notification, foreground-service, network, and optional storage/activity-recognition permissions. The runtime app needs location permission and enabled device location services before it can receive a GNSS fix and show a position marker.

## Live Debugging

With USB debugging enabled:

```powershell
$adb = "$env:LOCALAPPDATA\Android\Sdk\platform-tools\adb.exe"
& $adb logcat -v time V8DeadReckoning:D LiveNavigation:D PotholeDetector:I AndroidRuntime:E '*:S'
```

Useful log tags:

- `V8DeadReckoning`: model load and inference timing.
- `LiveNavigation`: emitted pothole events and live navigation state work.
- `PotholeDetector`: detected severity, confidence, shock, and vibration values.
- `AndroidRuntime`: crashes.

## Project Layout

```text
app/src/main/java/nisargpatel/deadreckoning/
|- adapter/
|  |- LocationAdapter.kt          # Fused location -> GNSS state
|  |- SensorAdapter.kt            # Android sensors -> IMU state
|  `- PotholeDetector.kt          # Multi-sample impact classification
|- data/
|  `- LiveNavigationRepository.kt # Active live application data flow
|- ml/
|  `- V8DeadReckoningEngine.kt    # ONNX Runtime inference and stationary gate
|- ui/
|  |- navigation/                 # Compose navigation shell
|  |- screens/                    # Live navigation and diagnostics screens
|  `- viewmodel/                  # Screen state holders
|- domain/                         # State models and repository contract
`- sensor/                         # Existing heading/DR support code

app/src/main/assets/ml/             # Bundled V8 ONNX model and metadata
tools/export_v8_model.py            # Repeatable ONNX export script
```

## Current Limitations

- The V8 model is intended for short GNSS outages; it is not a replacement for route-grade GNSS navigation.
- Location permission must be granted by Android before the map can show the device position.
- Motion and speed estimates can need per-device calibration because phone mounting, sensor bias, and driving surface differ from the model training data.
- Map matching, offline map downloads, long-term session analytics, and several legacy diagnostic screens are still under active development.
- Some legacy Java/XML screens remain in the codebase; the Compose `IDRAppShell` uses the live repository for the current app experience.

## Technology

- Kotlin and Java
- Jetpack Compose and Material 3
- Google Play Services Fused Location Provider
- OSMDroid / OpenStreetMap tiles
- ONNX Runtime Android
- EJML

## License

This project is provided for educational and research use. Check the licenses of bundled models, data, and third-party dependencies before redistributing it.
