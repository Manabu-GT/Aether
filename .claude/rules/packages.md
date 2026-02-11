# Package Structure

- `com.ms.square.aether.core` — Public API: `AetherEffect`, `AetherModifiers` (`aetherOverlay`), `QualityPreset`, `UniformValue`, `Wind`, `AetherCompat`
- `com.ms.square.aether.core.internal` — `ShaderCache`, `UniformBinder`, `EffectEnvironment`, `AetherOverlayNode`, `ApiCompat`
- `com.ms.square.aether.weather` — `Rain`, `Snow`, `Clouds`, `LightningFlash`

# Key Patterns

- Effect properties backed by Compose Snapshot state (`mutableStateOf` / `mutableFloatStateOf` / `mutableIntStateOf`) — mutating them triggers recomposition/redraw
- Preset factories: `Rain.light()`, `Snow.blizzard()`, `Clouds.overcast()`
- Reactive environment: `BroadcastReceiver` (Battery Saver) + `ContentObserver` (reduced motion) via `produceState`
- Type-safe uniforms via sealed interface `UniformValue`
