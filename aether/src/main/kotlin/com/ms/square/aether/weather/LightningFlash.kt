package com.ms.square.aether.weather

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.ms.square.aether.core.AetherEffect
import com.ms.square.aether.core.UniformValue
import org.intellij.lang.annotations.Language

/**
 * Lightning flash overlay effect that renders a bright screen flash.
 *
 * This is a **triggered** effect: call [flash] to fire a flash animation,
 * or manually animate [progress] from 1.0 to 0.0.
 *
 * @param progress Flash intensity from 0.0 (invisible) to 1.0 (full brightness).
 * @param brightness Peak brightness multiplier.
 * @param boltCount Number of root lightning bolts (1-5). Each bolt gets its own branches.
 * @param forkIntensity Intensity of lightning fork pattern. 0 = pure flash, 1 = prominent forks.
 * @param color Flash tint color.
 */
public class LightningFlash(
  progress: Float = 0f,
  brightness: Float = DEFAULT_BRIGHTNESS,
  boltCount: Int = DEFAULT_BOLT_COUNT,
  forkIntensity: Float = DEFAULT_FORK_INTENSITY,
  color: Color = DefaultFlashColor,
) : AetherEffect {

  public var progress: Float by mutableFloatStateOf(progress)
  public var brightness: Float by mutableFloatStateOf(brightness)
  public var boltCount: Int by mutableIntStateOf(boltCount)
  public var forkIntensity: Float by mutableFloatStateOf(forkIntensity)
  public var color: Color by mutableStateOf(color)

  override val isAnimated: Boolean get() = false

  override val shaderSource: String = SHADER_SOURCE

  override fun uniforms(): Map<String, UniformValue> = mapOf(
    "progress" to UniformValue.Float1(progress.coerceIn(0f, 1f)),
    "brightness" to UniformValue.Float1(brightness),
    "boltCount" to UniformValue.Int1(boltCount.coerceAtLeast(1)),
    "forkIntensity" to UniformValue.Float1(forkIntensity.coerceIn(0f, 1f)),
    "flashColor" to UniformValue.ColorValue(color)
  )

  /**
   * Triggers a flash animation, animating [progress] from 1.0 to 0.0.
   *
   * This is a suspend function — call from a coroutine scope (e.g., `LaunchedEffect` or
   * `rememberCoroutineScope().launch`).
   *
   * @param durationMs Duration of the flash fade-out in milliseconds.
   */
  public suspend fun flash(durationMs: Int = DEFAULT_FLASH_DURATION_MS) {
    try {
      animate(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = tween(durationMs)
      ) { value, _ -> progress = value }
    } finally {
      progress = 0f
    }
  }

  public companion object {
    private const val DEFAULT_BRIGHTNESS = 1.5f
    private const val DEFAULT_BOLT_COUNT = 3
    private const val DEFAULT_FORK_INTENSITY = 0.4f
    private const val DEFAULT_FLASH_DURATION_MS = 300

    /** Default lightning flash color: bright white with slight blue tint. */
    public val DefaultFlashColor: Color = Color(0xFFE8EEFF.toInt())

    @Language("AGSL")
    private const val SHADER_SOURCE: String = """
      uniform float2 resolution;
      uniform float  progress;
      uniform float  brightness;
      uniform int    boltCount;
      uniform float  forkIntensity;
      layout(color) uniform half4 flashColor;

      // Hash function by Dave Hoskins, MIT License
      // https://www.shadertoy.com/view/4djSRW
      float hash(float2 p) {
        float3 p3 = fract(float3(p.xyx) * float3(0.1031, 0.1030, 0.0973));
        p3 += dot(p3, p3.yzx + 33.33);
        p3 = fract((p3.xxy + p3.yzz) * p3.zyx);
        return p3.x;
      }

      // Piecewise-linear zigzag — sharp V-shaped kinks, not smooth curves.
      // Raw linear interp (no smoothstep) produces electric jagged segments.
      float zigzag(float y, float freq, float seed) {
        float sy = y * freq;
        float i = floor(sy);
        float f = fract(sy);
        return mix(hash(float2(i, seed)), hash(float2(i + 1.0, seed)), f) - 0.5;
      }

      // Multi-octave zigzag: coarse wander + medium jags + fine crackle
      float boltPath(float y, float seed) {
        return zigzag(y, 3.5, seed) * 0.16
             + zigzag(y, 9.0, seed + 10.0) * 0.06
             + zigzag(y, 22.0, seed + 20.0) * 0.02;
      }

      // Three-layer glow per bolt segment:
      //   .x = hard hairline core (~1.5px)
      //   .y = hyperbolic inner glow (~8px, sharper than Gaussian)
      //   .z = wide Gaussian atmospheric glow (~55px)
      float3 boltGlow(float dist, float intensity, float pw) {
        float coreW = pw * 1.5;
        float core = (1.0 - smoothstep(0.0, coreW, dist)) * 2.0;

        float innerW = pw * 8.0;
        float inner = clamp(innerW / (dist + innerW * 0.5), 0.0, 1.0) * 0.6;

        float outerSigma = pw * 55.0;
        float outer = exp(-dist * dist / (2.0 * outerSigma * outerSigma)) * 0.15;

        return float3(core, inner, outer) * intensity;
      }

      half4 main(float2 fragCoord) {
        if (progress <= 0.001) return half4(0.0);
        if (resolution.x < 1.0 || resolution.y < 1.0) return half4(0.0);

        float2 uv = fragCoord / resolution;
        float pw = 1.0 / resolution.x;

        float totalCore = 0.0;
        float totalInner = 0.0;
        float totalOuter = 0.0;
        float minBoltDist = 1.0;
        float nBolts = float(boltCount);

        // --- Loop over root bolts ---
        for (int b = 0; b < 5; b++) {
          // AGSL requires loop limits to be determinable at compile time, so can't write b < boltCount directly
          if (b >= boltCount) break;
          float fb = float(b);
          float boltSeed = fb * 1000.0;

          // Spread bolts evenly across screen with small random jitter
          float baseX = 0.5 + (fb - (nBolts - 1.0) * 0.5) * 0.2
                      + (hash(float2(fb, 700.0)) - 0.5) * 0.08;
          float tilt = (hash(float2(fb, 42.0)) - 0.5) * 0.25;

          // Primary bolt (b=0) full brightness, others slightly dimmer
          float boltIntensity = 1.0 - fb * 0.15;

          // Per-bolt random vertical extent
          float startY = hash(float2(fb, 800.0)) * 0.08;
          float endY = 0.55 + hash(float2(fb, 900.0)) * 0.40;

          // --- Root bolt path ---
          float boltX = baseX + tilt * uv.y + boltPath(uv.y, boltSeed);
          float boltDist = abs(uv.x - boltX);
          minBoltDist = min(minBoltDist, boltDist);

          float3 glow = boltGlow(boltDist, boltIntensity, pw);
          float taper = smoothstep(startY, startY + 0.10, uv.y)
                      * (1.0 - smoothstep(endY - 0.15, endY, uv.y));
          glow *= taper;

          totalCore += glow.x;
          totalInner += glow.y;
          totalOuter += glow.z;

          // --- Branches for this bolt: 3 tiers x 4 = 12 ---
          if (forkIntensity > 0.0) {
            for (int i = 0; i < 12; i++) {
              float fi = float(i);
              float tier = floor(fi / 4.0);
              float idx = mod(fi, 4.0);

              float tierScale = 1.0 / (tier + 1.5);
              float tierAlpha = boltIntensity / (tier + 2.0);

              float spawnY = startY + 0.05
                           + hash(float2(fi + boltSeed, 100.0))
                             * (endY - startY - 0.15);

              // Push branch angles toward 30-60 degrees off vertical
              float angle = (hash(float2(fi + boltSeed, 200.0)) - 0.5);
              angle = sign(angle) * (0.4 + abs(angle) * 0.5);

              float branchLen = (0.1 + hash(float2(fi + boltSeed, 300.0)) * 0.18)
                              * tierScale;
              float yEnd = spawnY + branchLen;

              if (uv.y < spawnY || uv.y > yEnd) continue;

              float t = (uv.y - spawnY) / branchLen;

              float parentX;
              if (tier < 0.5) {
                parentX = baseX + tilt * spawnY + boltPath(spawnY, boltSeed);
              } else {
                float pi = mod(idx, 4.0);
                float pSpawnY = 0.12
                              + hash(float2(pi + boltSeed, 100.0)) * 0.6;
                float pAngle = (hash(float2(pi + boltSeed, 200.0)) - 0.5);
                pAngle = sign(pAngle) * (0.4 + abs(pAngle) * 0.5);
                float pMainX = baseX + tilt * pSpawnY
                             + boltPath(pSpawnY, boltSeed);
                parentX = pMainX
                        + (spawnY - pSpawnY) * pAngle * 0.25 / 1.5
                        + boltPath(spawnY, 50.0 + pi * 7.0 + boltSeed)
                          * 0.06 / 1.5;
              }

              float branchX = parentX
                            + t * angle * 0.25 * tierScale
                            + boltPath(uv.y, 50.0 + fi * 7.0 + boltSeed)
                              * 0.05 * tierScale;

              float branchDist = abs(uv.x - branchX);
              float3 branchGlow = boltGlow(branchDist, tierAlpha, pw)
                                * (1.0 - t * 0.85)
                                * forkIntensity;

              totalCore += branchGlow.x;
              totalInner += branchGlow.y;
              totalOuter += branchGlow.z;
            }
          }
        }

        // --- Flicker (WCAG 2.3.1 safe: <=3 flashes/sec) ---
        float flickerPhase = progress * 2.5;
        float flicker = smoothstep(0.0, 0.1, fract(flickerPhase))
                      * (1.0 - smoothstep(0.1, 0.4, fract(flickerPhase)));
        flicker = 0.6 + flicker * 0.4;

        // --- Apply progress fade, flicker, brightness ---
        float fade = progress * flicker * brightness;
        totalCore *= fade;
        totalInner *= fade;
        totalOuter *= fade;

        // --- Color separation: white core / blue-white inner / purple-blue outer ---
        float3 coreColor = float3(1.0, 1.0, 1.0);
        float3 innerColor = flashColor.rgb;
        float3 outerColor = flashColor.rgb * float3(0.7, 0.6, 1.0);

        float3 finalColor = coreColor * totalCore
                          + innerColor * totalInner
                          + outerColor * totalOuter;

        // --- Wide atmospheric sky illumination ---
        float atmSigma = pw * 200.0;
        float atm = exp(-minBoltDist * minBoltDist / (2.0 * atmSigma * atmSigma))
                  * 0.08 * progress * brightness;
        atm *= smoothstep(0.0, 0.3, uv.y) * (1.0 - smoothstep(0.2, 1.0, uv.y));
        finalColor += outerColor * atm;

        // --- Screen flash (concentrated near nearest bolt) ---
        float flashSigma = pw * 300.0;
        float flashFalloff = exp(-minBoltDist * minBoltDist
                               / (2.0 * flashSigma * flashSigma));
        float flash = pow(progress, 4.0) * 0.1 * brightness
                    * mix(0.2, 1.0, flashFalloff);
        finalColor += flashColor.rgb * flash * flicker;

        float totalAlpha = clamp(totalCore + totalInner + totalOuter
                               + atm + flash, 0.0, 1.0);
        return half4(finalColor * totalAlpha, flashColor.a * totalAlpha);
      }
    """
  }
}
