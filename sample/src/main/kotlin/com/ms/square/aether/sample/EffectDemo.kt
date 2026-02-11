package com.ms.square.aether.sample

import com.ms.square.aether.core.AetherEffect
import com.ms.square.aether.weather.Clouds
import com.ms.square.aether.weather.LightningFlash
import com.ms.square.aether.weather.Rain
import com.ms.square.aether.weather.Snow

/** Wraps an [AetherEffect] with display metadata for the gallery. */
data class EffectDemo(val name: String, val description: String, val effect: AetherEffect)

/** All available weather effect demos. */
val weatherEffects: List<EffectDemo> = listOf(
  EffectDemo(
    name = "Rain",
    description = "Animated rain streaks with configurable intensity and wind.",
    effect = Rain()
  ),
  EffectDemo(
    name = "Snow",
    description = "Gently falling snowflakes with wind drift and twinkle.",
    effect = Snow()
  ),
  EffectDemo(
    name = "Clouds",
    description = "Drifting cloud overlay using layered noise.",
    effect = Clouds.partlyCloudy()
  ),
  EffectDemo(
    name = "Lightning",
    description = "Triggered brightness flash with optional fork pattern.",
    effect = LightningFlash(progress = 0f)
  )
)
