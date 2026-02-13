package com.ms.square.aether.weather

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.ms.square.aether.core.AetherEffect
import com.ms.square.aether.core.UniformValue
import org.intellij.lang.annotations.Language

/**
 * Animated cloud overlay effect using layered noise.
 *
 * Renders drifting clouds that slowly swirl over the content.
 *
 * @param coverage Cloud coverage. 0.0 = wispy, 1.0 = overcast.
 * @param speed Drift speed multiplier.
 * @param color Cloud tint color.
 */
public class Clouds(coverage: Float = DEFAULT_COVERAGE, speed: Float = 1.0f, color: Color = DefaultCloudColor) :
  AetherEffect {

  public var coverage: Float by mutableFloatStateOf(coverage)
  public var speed: Float by mutableFloatStateOf(speed)
  public var color: Color by mutableStateOf(color)

  override val isAnimated: Boolean get() = true

  override val shaderSource: String = SHADER_SOURCE

  override fun uniforms(): Map<String, UniformValue> = mapOf(
    "coverage" to UniformValue.Float1(coverage.coerceIn(0f, 1f)),
    "speed" to UniformValue.Float1(speed),
    "cloudColor" to UniformValue.ColorValue(color)
  )

  public companion object {
    private const val DEFAULT_COVERAGE = 0.30f

    /** Default blue-gray cloud color — visible on both light and dark backgrounds. */
    public val DefaultCloudColor: Color = Color(0xD8707888.toInt())

    /** Wispy high-altitude clouds. */
    public fun wispy(): Clouds = Clouds(coverage = 0.15f, speed = 0.6f)

    /** Partly cloudy sky. */
    public fun partlyCloudy(): Clouds = Clouds(coverage = 0.35f)

    /** Overcast sky with full cloud cover. */
    public fun overcast(): Clouds = Clouds(coverage = 0.85f, speed = 0.4f)

    @Language("AGSL")
    private const val SHADER_SOURCE: String = """
      uniform float2 resolution;
      uniform float  time;
      uniform float  coverage;
      uniform float  speed;
      layout(color) uniform half4 cloudColor;

      // Hash function by Dave Hoskins, MIT License
      // https://www.shadertoy.com/view/4djSRW
      float hash2D(float2 p) {
        float3 p3 = fract(float3(p.xyx) * float3(0.1031, 0.1030, 0.0973));
        p3 += dot(p3, p3.yzx + 33.33);
        p3 = fract((p3.xxy + p3.yzz) * p3.zyx);
        return p3.x;
      }

      // Gradient hash — returns 2D gradient vector in [-1, 1]
      float2 gradHash(float2 p) {
        float h = hash2D(p);
        float angle = h * 6.28318530718;
        return float2(cos(angle), sin(angle));
      }

      // Gradient noise (Perlin-like) with quintic interpolation
      float noise(float2 p) {
        float2 i = floor(p);
        float2 f = fract(p);
        float2 u = f * f * f * (f * (f * 6.0 - 15.0) + 10.0);

        float a = dot(gradHash(i + float2(0.0, 0.0)), f - float2(0.0, 0.0));
        float b = dot(gradHash(i + float2(1.0, 0.0)), f - float2(1.0, 0.0));
        float c = dot(gradHash(i + float2(0.0, 1.0)), f - float2(0.0, 1.0));
        float d = dot(gradHash(i + float2(1.0, 1.0)), f - float2(1.0, 1.0));

        return mix(mix(a, b, u.x), mix(c, d, u.x), u.y);
      }

      float fbm(float2 p) {
        float value = 0.0;
        float amplitude = 0.5;
        for (int i = 0; i < 5; i++) {
          value += amplitude * noise(p);
          p *= 2.0;
          amplitude *= 0.5;
        }
        return value * 0.5 + 0.5;
      }

      half4 main(float2 fragCoord) {
        if (resolution.x < 1.0 || resolution.y < 1.0) return half4(0.0);
        float2 uv = fragCoord / resolution;

        float t = mod(time, 1800.0) * speed * 0.05;

        // Coherent wind direction for natural drift
        float2 wind = float2(1.0, 0.3);

        // --- Coverage controls the smoothstep threshold, not a final multiplier ---
        // Map coverage [0,1] to threshold: high coverage = low threshold = more cloud area
        // At coverage=0.2: threshold ~0.58 (light haze, only peaks show)
        // At coverage=0.5: threshold ~0.42 (partly cloudy)
        // At coverage=0.8: threshold ~0.22 (overcast, nearly full coverage)
        float threshold = mix(0.65, 0.15, coverage);

        // Organic drift: sinusoidal speed variation simulates wind gusts
        float gustT = t + sin(t * 0.12) * 2.5;
        // Slow perpendicular drift for internal churning
        float2 drift = float2(-wind.y, wind.x) * sin(t * 0.7) * 0.08;

        // Near cloud layer: lower frequency, faster drift, domain-warped for natural shapes
        float n1 = fbm(uv * 5.0 + wind * gustT + drift);
        float2 warp = float2(n1, n1 * 0.8) * 0.45;
        float n2 = fbm(uv * 3.5 + warp + wind * gustT * 0.6 - drift * 0.5 + 50.0);
        float nearRaw = (n1 + n2) * 0.5;

        // Shape near cloud: threshold sets where clouds begin, fixed-width soft edge
        float edgeSoft = 0.12; // controls how soft cloud edges are
        float nearCloud = smoothstep(threshold, threshold + edgeSoft, nearRaw);

        // Far cloud layer: higher frequency, slower drift, adds depth/detail
        float n3 = fbm(uv * 8.0 + wind * gustT * 0.35 + drift * 1.5 + 100.0);
        float farThreshold = threshold + 0.05; // far clouds slightly sparser
        float farCloud = smoothstep(farThreshold, farThreshold + edgeSoft, n3);

        // Multi-layer parallax blend
        float cloud = nearCloud * 0.7 + farCloud * 0.3;

        // --- Volumetric feel: brighter centers, softer edges ---
        // Use how far above threshold the noise is to create interior density
        float nearDepth = smoothstep(threshold, threshold + 0.30, nearRaw);
        float interior = nearDepth * 0.4; // extra brightness in thick cloud centers

        // --- Vertical gradient: denser toward top (sky region) ---
        // In AGSL overlay: uv.y=0 is top of screen, uv.y=1 is bottom
        // Clouds visible everywhere but denser at top, fading gently at bottom
        float verticalFade = 1.0 - smoothstep(0.5, 1.0, uv.y);
        // Slight boost at the very top for sky-like density
        verticalFade = mix(verticalFade, 1.0, (1.0 - smoothstep(0.0, 0.3, uv.y)) * 0.3);
        cloud *= verticalFade;
        cloud = clamp(cloud, 0.0, 1.0);

        // --- Color: darker edges for contrast on light backgrounds, subtle warm highlights in cores ---
        float3 edgeTint = cloudColor.rgb * float3(0.82, 0.85, 0.92); // darker cool edge for visibility
        float3 coreTint = cloudColor.rgb * float3(1.02, 1.01, 0.98); // subtle warm core, no brightening
        float3 finalColor = mix(edgeTint, coreTint, clamp(cloud + interior, 0.0, 1.0));

        // Alpha: cloud density drives opacity, cloudColor.a is the max opacity cap
        // Boost alpha contrast for better edge definition on light backgrounds
        float alphaBoost = cloud * (1.0 + cloud * 0.3); // denser clouds get up to 30% more opacity
        float alpha = cloudColor.a * clamp(alphaBoost, 0.0, 1.0);

        return half4(finalColor * alpha, alpha);
      }
    """
  }
}
