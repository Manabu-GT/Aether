package com.ms.square.aether.weather

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import com.ms.square.aether.core.AetherEffect
import com.ms.square.aether.core.UniformValue
import com.ms.square.aether.core.Wind
import org.intellij.lang.annotations.Language

/**
 * Animated snow overlay effect with falling snowflakes, wind drift, and configurable density.
 *
 * Each snowflake has a bright core surrounded by a dark halo for contrast
 * on any background (light maps, dark UIs, satellite imagery).
 *
 * @param density Number of snowflake layers (1-5). Higher values produce denser snowfall.
 * @param speed Fall speed multiplier. 1.0 is a gentle drift.
 * @param flakeSize Base size of snowflakes. 1.0 is normal.
 * @param color Snowflake tint color.
 * @param haloColor Dark outline color for contrast on light backgrounds.
 * @param wind Wind configuration controlling horizontal drift.
 */
public class Snow(
  density: Int = DEFAULT_DENSITY,
  speed: Float = 1.0f,
  flakeSize: Float = 1.0f,
  color: Color = DefaultSnowColor,
  haloColor: Color = DefaultHaloColor,
  wind: Wind = Wind(angle = 0f, strength = DEFAULT_WIND),
) : AetherEffect {

  public var density: Int by mutableIntStateOf(density)
  public var speed: Float by mutableFloatStateOf(speed)
  public var flakeSize: Float by mutableFloatStateOf(flakeSize)
  public var color: Color by mutableStateOf(color)
  public var haloColor: Color by mutableStateOf(haloColor)
  public var wind: Wind by mutableStateOf(wind)

  override val isAnimated: Boolean get() = true

  override val shaderSource: String = SHADER_SOURCE

  override fun uniforms(): Map<String, UniformValue> = mapOf(
    "density" to UniformValue.Int1(density.coerceIn(1, MAX_DENSITY)),
    "speed" to UniformValue.Float1(speed),
    "flakeSize" to UniformValue.Float1(flakeSize),
    "snowColor" to UniformValue.ColorValue(color),
    "haloColor" to UniformValue.ColorValue(haloColor),
    "windStrength" to UniformValue.Float1(wind.strength)
  )

  public companion object {
    private const val DEFAULT_DENSITY = 3
    private const val DEFAULT_WIND = 0.3f
    private const val MAX_DENSITY = 5

    /** Default icy blue-white snowflake color. */
    public val DefaultSnowColor: Color = Color(0xFFE0F0FF.toInt())

    /** Default cool blue halo for icy contrast on any background. */
    public val DefaultHaloColor: Color = Color(0xCC1A3A5C.toInt())

    /** Light dusting of snow. */
    public fun light(): Snow = Snow(density = 1, speed = 0.6f, wind = Wind.Calm)

    /** Moderate snowfall. */
    public fun moderate(): Snow = Snow(density = 3, speed = 1.0f)

    /** Heavy snowfall. */
    public fun heavy(): Snow = Snow(density = 4, speed = 1.2f, flakeSize = 1.2f)

    /** Blizzard conditions with strong wind. */
    public fun blizzard(): Snow = Snow(
      density = 5,
      speed = 1.5f,
      flakeSize = 1.3f,
      wind = Wind.StrongWind
    )

    @Language("AGSL")
    private const val SHADER_SOURCE: String = """
      uniform float2 resolution;
      uniform float  time;
      uniform int    density;
      uniform float  speed;
      uniform float  flakeSize;
      uniform float4 snowColor;
      uniform float4 haloColor;
      uniform float  windStrength;

      // Hash function by Dave Hoskins, MIT License
      // https://www.shadertoy.com/view/4djSRW
      float hash(float2 p) {
        float3 p3 = fract(float3(p.xyx) * float3(0.1031, 0.1030, 0.0973));
        p3 += dot(p3, p3.yzx + 33.33);
        return fract((p3.x + p3.y) * p3.z);
      }

      // Returns float2(coreIntensity, haloIntensity) for dual-layer compositing
      float2 snowLayer(float2 uv, float layerSeed, float layerDepth) {
        float t = mod(time, 600.0) * speed;

        // Grid density: fewer, larger cells for visible snowflakes
        float gridScale = 4.5 + layerDepth * 9.0;

        // Fall speed: near layers fall faster for parallax
        float fallRate = mix(0.5, 0.12, layerDepth);
        uv.y -= t * fallRate;
        uv.x += sin(uv.y * 1.8 + t * 0.6 + layerSeed) * windStrength * 0.12;
        uv.x -= t * 0.04;

        // Grid cell
        float2 cellUv = uv * gridScale;
        float2 cellId = floor(cellUv);
        float2 cellFrac = fract(cellUv);

        // Skip ~30% of cells for natural sparsity
        float skip = hash(cellId + layerSeed + 200.0);
        if (skip < 0.30) return float2(0.0, 0.0);

        // Random flake center within inner cell area
        float rx = hash(cellId + layerSeed);
        float ry = hash(cellId + layerSeed + 100.0);
        float2 center = float2(0.2 + rx * 0.6, 0.2 + ry * 0.6);

        // Distance and angle to flake center
        float2 diff = cellFrac - center;
        float dist = length(diff);

        // Per-flake size variation — large flakes (~20px near, ~10px far at 1080p)
        float sizeVar = 0.6 + hash(cellId + layerSeed + 300.0) * 0.4;
        float radius = 0.144 * flakeSize * sizeVar * mix(1.0, 0.5, layerDepth);

        // Early exit: max possible radius (arm tip + halo)
        float maxR = radius * 2.0;
        if (dist > maxR) return float2(0.0, 0.0);

        // --- Dendritic snowflake shape (thin arms + side branches) ---
        float rotation = hash(cellId + layerSeed + 500.0) * 6.28;
        float angle = atan(diff.y, diff.x) + rotation;

        // Fold into 60-degree sector centered on arm axis
        float sector = mod(angle + 0.5236, 1.0472) - 0.5236;
        float absSector = abs(sector);

        // Arm-local Cartesian: ax = along arm, ay = perpendicular
        float cosS = cos(absSector);
        float sinS = sin(absSector);
        float ax = dist * cosS;
        float ay = dist * sinS;

        // --- Hub: small solid center ---
        float hubR = radius * 0.14;
        float hubDist = max(dist - hubR, 0.0);

        // --- Main arm: thin line from hub to near-tip ---
        float armEnd = radius * 0.97;
        float armAx = clamp(ax, hubR, armEnd);
        float armDist = length(float2(ax - armAx, ay));

        // --- Side branches: 3 per arm-half, perpendicular with slight forward lean ---
        // Branches lean slightly toward arm tip (natural crystal growth direction)
        float lean = 0.20;
        float invE = 1.0 / (lean * lean + 1.0);
        float bVar = hash(cellId + layerSeed + 600.0);

        // Branch 1: near base, longest (like reference: prominent lower branches)
        float bP1 = (0.28 + bVar * 0.08) * radius;
        float bL1 = (0.42 + bVar * 0.08) * radius;
        float t1 = clamp(((ax - bP1) * lean + ay) * invE / bL1, 0.0, 1.0);
        float d1 = length(float2(ax - bP1 - t1 * lean * bL1, ay - t1 * bL1));

        // Branch 2: mid-arm, medium
        float bP2 = (0.50 + bVar * 0.10) * radius;
        float bL2 = (0.30 + bVar * 0.06) * radius;
        float t2 = clamp(((ax - bP2) * lean + ay) * invE / bL2, 0.0, 1.0);
        float d2 = length(float2(ax - bP2 - t2 * lean * bL2, ay - t2 * bL2));

        // Branch 3: near tip, shortest
        float bP3 = (0.72 + bVar * 0.08) * radius;
        float bL3 = (0.16 + bVar * 0.04) * radius;
        float t3 = clamp(((ax - bP3) * lean + ay) * invE / bL3, 0.0, 1.0);
        float d3 = length(float2(ax - bP3 - t3 * lean * bL3, ay - t3 * bL3));

        float branchDist = min(d1, min(d2, d3));

        // --- Distance to nearest snowflake feature ---
        float featureDist = min(hubDist, min(armDist, branchDist));

        // Line width: scales with radius for consistent visual weight
        float lineW = radius * 0.08;
        float haloW = lineW * 3.5;

        // Core: bright crystalline structure
        float core = 1.0 - smoothstep(lineW * 0.15, lineW, featureDist);
        core *= core;

        // Halo: soft glow around structure
        float halo = 1.0 - smoothstep(lineW * 0.5, haloW, featureDist);

        // Subtle twinkle
        float phase = hash(cellId + layerSeed + 400.0) * 6.28;
        float twinkle = 0.8 + 0.2 * sin(t * 2.0 + phase);

        // Depth dimming — keep far layers more visible
        float depthDim = mix(1.0, 0.5, layerDepth);

        return float2(core * twinkle * depthDim, halo * twinkle * depthDim);
      }

      half4 main(float2 fragCoord) {
        if (resolution.x < 1.0 || resolution.y < 1.0) return half4(0.0);
        float2 uv = (fragCoord * 2.0 - resolution) / min(resolution.x, resolution.y);

        // Vertical fade — snow dense at top, thinning at bottom
        // AGSL Y=0 is top, so uv.y < 0 at top, > 0 at bottom
        float vertFade = 1.0 - smoothstep(0.0, 1.5, uv.y);

        float core = 0.0;
        float halo = 0.0;
        int layers = density + 2;

        for (int i = 0; i < 7; i++) {
          if (i >= layers) break;
          float depth = float(i) / 6.0;
          float2 layer = snowLayer(uv, float(i) * 17.3, depth);
          core += layer.x;
          halo += layer.y;
        }

        core *= vertFade;
        halo *= vertFade;
        core = clamp(core, 0.0, 1.0);
        halo = clamp(halo, 0.0, 1.0);

        // Over-composite: bright core in front of dark halo shadow
        float coreA = clamp(snowColor.a * core, 0.0, 1.0);
        float haloA = clamp(haloColor.a * halo, 0.0, 1.0);
        // Standard "A over B" blend
        float3 col = snowColor.rgb * coreA + haloColor.rgb * haloA * (1.0 - coreA);
        float alpha = coreA + haloA * (1.0 - coreA);
        if (alpha < 0.001) return half4(0.0);
        return half4(col, alpha);
      }
    """
  }
}
