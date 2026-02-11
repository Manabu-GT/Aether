---
paths:
  - "aether/src/main/kotlin/com/ms/square/aether/weather/**"
---

# AGSL Shader Guidelines

## Critical: Float Precision with Time

`time` uniform (seconds since boot) can reach 10,000+. Using it to offset UVs before `floor()`/`fract()` causes float32 precision degradation — cell-based particle rendering produces rectangular artifacts instead of circles.

**Fix**: Always wrap with `mod(time, 600.0)` before UV calculations. 600s period avoids visible repetition.

## AGSL/SkSL Gotchas

- `dFdx()` / `dFdy()` are NOT available in AGSL — causes silent shader compilation failure
- `saturate()` is HLSL-only — use `clamp(x, 0.0, 1.0)` instead
- `smoothstep(edge0, edge1, x)` with edge0 > edge1 is undefined behavior — reverse args and use `1.0 - smoothstep()`

## Design Patterns

- **Cloud coverage**: Use as a noise threshold (`mix(0.65, 0.15, coverage)`) NOT as a final multiplier (`cloud *= coverage` crushes opacity)
- **Vertical gradients**: In AGSL overlay mode, `uv.y=0` is top. For sky effects use `1.0 - smoothstep(0.5, 1.0, uv.y)` for top-heavy effects
- **Volumetric clouds**: Measure noise depth above threshold for interior brightness; use edge/core color tinting for subsurface scattering feel
- **Snowflake geometry**: Use hexagonal hub (55% radius) + tapered arms + sinusoidal bumps + side spurs for crystalline look
