# Change Log

## Version 0.1.0 *(2026-02-11)*

Initial release.

### Effects

* **Rain** – Multi-layer parallax raindrops with halo glow. Presets: `light()`, `moderate()`, `heavy()`, `storm()`
* **Snow** – Crystalline dendritic snowflakes with gentle drift. Presets: `light()`, `moderate()`, `heavy()`, `blizzard()`
* **Clouds** – Volumetric cloud cover using layered Perlin noise. Presets: `wispy()`, `partlyCloudy()`, `overcast()`
* **Lightning Flash** – Forking bolt geometry with atmospheric glow and `suspend fun flash()` trigger

### Core

* `Modifier.aetherOverlay()` – Transparent overlay modifier powered by AGSL shaders (API 33+)
* `AetherEffect` interface – Implement custom effects with `shaderSource` and `uniforms()`
* `QualityPreset` – `LOW`, `MEDIUM`, `HIGH` rendering quality levels
* `Wind` – Unified wind model with `Calm`, `LightBreeze`, `StrongWind` presets
* Shader caching by source string – no recompilation on parameter changes
* `Modifier.Node` implementation (not `composed {}`)
* Reactive Battery Saver and reduced motion detection
* No-op fallback on devices below API 33
