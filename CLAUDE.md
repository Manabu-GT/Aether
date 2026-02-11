# Aether

GPU-accelerated weather effects for Android Compose, powered by AGSL.

## Build

- `./gradlew build` — Build all modules
- `./gradlew :sample:assembleDebug` — Build sample app
- `./gradlew check` — Run Detekt + Spotless + tests

## Structure

- `aether/` — Single library module (`com.ms-square:aether`)
- `sample/` — Showcase app

## Architecture

- Effects activate on API 33+ (AGSL), no-op on older devices
- Shader caching by source string — never recompile on parameter changes
- Effect properties backed by Compose Snapshot state
- `Modifier.Node` (not `composed {}`) for modifier implementation
- Reactive Battery Saver / reduced motion detection via `produceState`
