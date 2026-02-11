package com.ms.square.aether.core

import androidx.compose.ui.graphics.Color

/**
 * Type-safe representation of AGSL shader uniform values.
 *
 * Each sealed subtype maps to a specific `RuntimeShader.setUniform*()` overload,
 * preventing runtime type mismatches.
 */
public sealed interface UniformValue {
  @JvmInline
  public value class Float1(public val v: Float) : UniformValue

  public data class Float2(public val x: Float, public val y: Float) : UniformValue

  public data class Float3(public val x: Float, public val y: Float, public val z: Float) : UniformValue

  public data class Float4(public val x: Float, public val y: Float, public val z: Float, public val w: Float) :
    UniformValue

  @JvmInline
  public value class ColorValue(public val color: Color) : UniformValue

  @JvmInline
  public value class Int1(public val v: Int) : UniformValue
}
