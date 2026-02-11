package com.ms.square.aether.core

/**
 * Core interface for Aether shader effects.
 *
 * Implementations provide an AGSL shader source string and a set of typed uniforms.
 * The [shaderSource] serves as the cache key -- RuntimeShader is compiled once per
 * unique source string and uniforms are updated per-frame without recompilation.
 */
public interface AetherEffect {
  /** AGSL shader source code. Used as the RuntimeShader cache key. */
  public val shaderSource: String

  /** Current uniform values to bind to the shader each frame. */
  public fun uniforms(): Map<String, UniformValue>

  /** Whether this effect requires continuous animation (time-based uniforms). */
  public val isAnimated: Boolean get() = false
}
