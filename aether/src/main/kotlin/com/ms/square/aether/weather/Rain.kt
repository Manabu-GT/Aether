package com.ms.square.aether.weather

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ms.square.aether.core.AetherEffect
import com.ms.square.aether.core.UniformValue
import com.ms.square.aether.core.Wind
import org.intellij.lang.annotations.Language

/**
 * Animated rain overlay effect with configurable intensity, speed, and color.
 *
 * Renders falling rain streaks over content using an AGSL shader.
 * Each raindrop has a bright core surrounded by a dark halo for contrast
 * on any background (light maps, dark UIs, satellite imagery).
 *
 * @param intensity Number of rain layers (1-5). Higher values produce denser rain.
 * @param speed Multiplier for fall speed. 1.0 is normal, 2.0 is double speed.
 * @param dropLength Length of rain streaks. 1.0 is normal, larger values produce longer streaks.
 * @param colors Color configuration for core, halo, and atmospheric tint.
 * @param wind Wind configuration controlling tilt direction.
 */
public class Rain(
  intensity: Int = DEFAULT_INTENSITY,
  speed: Float = 1.0f,
  dropLength: Float = 1.0f,
  colors: RainColors = RainColors(),
  wind: Wind = Wind.LightBreeze,
) : AetherEffect {

  public var intensity: Int by mutableIntStateOf(intensity)
  public var speed: Float by mutableFloatStateOf(speed)
  public var dropLength: Float by mutableFloatStateOf(dropLength)
  public var colors: RainColors by mutableStateOf(colors)
  public var wind: Wind by mutableStateOf(wind)

  override val isAnimated: Boolean get() = true

  override val shaderSource: String = SHADER_SOURCE

  override fun uniforms(): Map<String, UniformValue> = mapOf(
    "intensity" to UniformValue.Int1(intensity.coerceIn(1, MAX_INTENSITY)),
    "speed" to UniformValue.Float1(speed),
    "dropLength" to UniformValue.Float1(dropLength),
    "rainColor" to UniformValue.ColorValue(colors.core),
    "haloColor" to UniformValue.ColorValue(colors.halo),
    "tintColor" to UniformValue.ColorValue(colors.tint),
    "windAngle" to UniformValue.Float1(wind.angle)
  )

  public companion object {
    private const val DEFAULT_INTENSITY = 3
    private const val MAX_INTENSITY = 5

    /** Light drizzle. */
    public fun light(): Rain =
      Rain(intensity = 1, speed = 0.7f, dropLength = 0.6f, wind = Wind(angle = 0.05f, strength = 0.2f))

    /** Moderate rainfall. */
    public fun moderate(): Rain = Rain(intensity = 3, speed = 1.0f)

    /** Heavy rain. */
    public fun heavy(): Rain = Rain(intensity = 4, speed = 1.3f, dropLength = 1.3f)

    /** Intense storm with strong wind. */
    public fun storm(): Rain = Rain(
      intensity = 5,
      speed = 1.8f,
      dropLength = 1.5f,
      wind = Wind.StrongWind
    )

    @Language("AGSL")
    private const val SHADER_SOURCE: String = """
      uniform float2 resolution;
      uniform float  time;
      uniform int    intensity;
      uniform float  speed;
      uniform float  dropLength;
      layout(color) uniform half4 rainColor;
      layout(color) uniform half4 haloColor;
      layout(color) uniform half4 tintColor;
      uniform float  windAngle;

      // Hash function by Dave Hoskins, MIT License
      // https://www.shadertoy.com/view/4djSRW
      float hash(float2 p) {
        float3 p3 = fract(float3(p.xyx) * float3(0.1031, 0.1030, 0.0973));
        p3 += dot(p3, p3.yzx + 33.33);
        p3 = fract((p3.xxy + p3.yzz) * p3.zyx);
        return p3.x;
      }

      float hash13(float3 p3) {
        p3 = fract(p3 * 0.1031);
        p3 += dot(p3, p3.zyx + 31.32);
        return fract((p3.x + p3.y) * p3.z);
      }

      float safeTime() { return mod(time, 600.0); }

      // Returns float2(coreIntensity, haloIntensity) for dual-layer compositing
      float2 rainLayer(float2 uv, float layerSeed, float layerDepth) {
        // Far layers denser (more columns), near layers sparser
        float numCols = 30.0 + layerDepth * 55.0;
        float colId = floor(uv.x * numCols);
        float localX = fract(uv.x * numCols) - 0.5;

        // Long streaks — far layers slightly shorter
        float baseDropLen = 0.35 * dropLength * (1.0 - layerDepth * 0.5);

        float spawnInterval = 0.75 / (0.6 + layerDepth);

        // Depth-based speed parallax: near = fast, far = slow
        float depthSpeed = 1.0 - layerDepth * 0.75;
        float t = safeTime() * speed * depthSpeed
                  * (0.8 + hash(float2(layerSeed, colId)) * 0.5);

        float core = 0.0;
        float halo = 0.0;
        for (int slot = -2; slot <= 3; slot++) {
          float spawnTime = floor(t / spawnInterval + float(slot)) * spawnInterval;

          float dropRnd = hash13(float3(colId, spawnTime, layerSeed));
          if (dropRnd < 0.12) continue; // 88% spawn rate

          float age = t - spawnTime;
          if (age < 0.0) continue;

          float dropSpeed = 0.5 + hash13(float3(colId, spawnTime, layerSeed + 200.0)) * 0.2;
          float yPos = age * dropSpeed;

          // Per-drop length variation (65-135%)
          float lenVar = 0.65 + hash13(float3(colId, spawnTime, layerSeed + 150.0)) * 0.7;
          float thisDropLen = baseDropLen * lenVar;

          float dy = uv.y - yPos;
          if (dy > 0.0 || dy < -thisDropLen) continue;

          // 0 = tail (top), 1 = head (bottom)
          float dropT = clamp(1.0 + dy / thisDropLen, 0.0, 1.0);

          float dropJitter = (hash13(float3(colId, spawnTime, layerSeed + 50.0)) - 0.5) * 0.25;
          float dx = localX - dropJitter;

          // Convert horizontal distance to UV space for resolution-independent width
          float dxUV = dx / numCols;
          float absDxUV = abs(dxUV);

          // Width in UV space (fraction of screen width) — near ~2.5px, far ~1.5px at 1080p
          float widthVar = 0.8 + hash13(float3(colId, spawnTime, layerSeed + 100.0)) * 0.4;
          float coreW = mix(0.0033, 0.0014, layerDepth) * widthVar;
          float tailTaper = mix(0.5, 1.0, smoothstep(0.0, 0.20, dropT));
          float headBulge = 1.0 + 0.15 * smoothstep(0.88, 0.97, dropT);
          float w = coreW * tailTaper * headBulge;

          // Halo extends ~3px beyond core edge
          float haloW = w + 0.003;
          if (absDxUV > haloW) continue;

          // Pixel-aware anti-aliased edge — near layers get softer edges for cinematic feel
          float pixelW = 1.0 / resolution.x;
          float softness = mix(1.6, 1.0, layerDepth);
          float aaZone = max(w * 0.4 * softness, pixelW);
          float coreShape = 1.0 - smoothstep(w - aaZone, w, absDxUV);

          // Dark halo ring: visible from core edge to haloW
          float haloShape = (1.0 - smoothstep(w, haloW, absDxUV)) * (1.0 - coreShape);

          // Micro-splash: radial bloom at drop terminus simulating surface impact
          float splashR = coreW * 3.5;
          float splashDist = length(float2(dxUV, dy * resolution.y / resolution.x));
          float splashShape = (1.0 - smoothstep(0.0, splashR, splashDist))
                            * smoothstep(0.85, 0.98, dropT) * 0.5;
          coreShape = max(coreShape, splashShape);

          // Brightness: smooth tail ramp, gentle head round-off
          float tailFade = smoothstep(0.0, 0.18, dropT);
          float headFade = 1.0 - smoothstep(0.92, 1.0, dropT);
          float brightness = pow(dropT, 1.8) * tailFade * headFade;

          // Depth: gentle power-curve falloff (near=1.0, far~0.45)
          float depthFade = mix(1.0, 0.45, layerDepth * layerDepth);
          float dropIntensity = 0.6 + dropRnd * 0.4;

          float contrib = brightness * dropIntensity * depthFade;
          core += coreShape * contrib;
          halo += haloShape * contrib;
        }

        return float2(core, halo);
      }

      half4 main(float2 fragCoord) {
        if (resolution.x < 1.0 || resolution.y < 1.0) return half4(0.0);
        float2 uv = fragCoord / resolution;
        uv.x += uv.y * windAngle;

        float edgeFade = smoothstep(0.0, 0.06, uv.y) * (1.0 - smoothstep(0.94, 1.0, uv.y));

        // Atmospheric tint base layer (scales with intensity, vignette at edges)
        float2 center = uv - 0.5;
        float vignette = 1.0 + 0.3 * dot(center, center);
        float tintAlpha = float(intensity) / 5.0 * 0.22 * edgeFade * vignette;
        float3 tintRgb = tintColor.rgb;

        // Start with tint as base layer (premultiplied)
        float3 pm = tintRgb * tintAlpha;
        float alpha = tintAlpha;

        // Composite rain layers far-to-near with atmospheric perspective
        for (int i = 4; i >= 0; i--) {
          if (i >= intensity) continue;
          float layerDepth = float(i) / 5.0;
          float2 layer = rainLayer(uv, float(i) * 7.23, layerDepth);
          float coreVal = clamp(layer.x * edgeFade, 0.0, 1.0);
          float haloVal = clamp(layer.y * edgeFade, 0.0, 1.0);

          // Atmospheric perspective: far layers shift toward cool blue-gray
          float3 atmColor = mix(rainColor.rgb, float3(0.55, 0.62, 0.72), layerDepth * 0.45);

          // Per-layer A-over-B: core over halo, then over accumulator
          float coreA = clamp(rainColor.a * coreVal, 0.0, 1.0);
          float haloA = clamp(haloColor.a * haloVal, 0.0, 1.0);
          float3 layerPm = atmColor * coreA + haloColor.rgb * haloA * (1.0 - coreA);
          float layerA = coreA + haloA * (1.0 - coreA);

          pm = layerPm + pm * (1.0 - layerA);
          alpha = layerA + alpha * (1.0 - layerA);
        }

        if (alpha < 0.001) return half4(0.0);
        return half4(pm, alpha);
      }
    """
  }
}
