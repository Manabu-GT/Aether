package com.ms.square.aether.core

/**
 * Unified wind model for weather effects.
 *
 * @param angle Wind tilt angle. 0 = vertical, positive = tilted right.
 * @param strength Wind strength multiplier. 0 = no wind, 1 = strong wind.
 */
public data class Wind(val angle: Float = 0f, val strength: Float = 0.3f) {
  public companion object {
    /** No wind — particles fall straight down. */
    public val Calm: Wind = Wind(angle = 0f, strength = 0f)

    /** Light breeze — slight tilt and drift. */
    public val LightBreeze: Wind = Wind(angle = 0.1f, strength = 0.3f)

    /** Strong wind — significant tilt and drift. */
    public val StrongWind: Wind = Wind(angle = 0.3f, strength = 0.8f)
  }
}
