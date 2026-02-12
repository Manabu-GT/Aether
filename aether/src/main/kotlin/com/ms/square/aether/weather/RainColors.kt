package com.ms.square.aether.weather

import androidx.compose.ui.graphics.Color

/**
 * Color configuration for the [Rain] effect.
 *
 * @param core Tint color for rain drop cores.
 * @param halo Dark outline color for contrast on light backgrounds.
 * @param tint Atmospheric tint color applied behind rain (RGB only; alpha scales with intensity).
 */
public data class RainColors(
  val core: Color = DefaultCore,
  val halo: Color = DefaultHalo,
  val tint: Color = DefaultTint,
) {
  public companion object {
    /** Default semi-transparent light blue rain color. */
    public val DefaultCore: Color = Color(0xAAC0D8F0.toInt())

    /** Default dark navy halo for contrast on light backgrounds. */
    public val DefaultHalo: Color = Color(0x80203040)

    /** Default subtle blue-gray atmospheric tint. */
    public val DefaultTint: Color = Color(0x18203040)
  }
}
