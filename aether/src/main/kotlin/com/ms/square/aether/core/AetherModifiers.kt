package com.ms.square.aether.core

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ms.square.aether.core.internal.AetherOverlayElement
import com.ms.square.aether.core.internal.isAgslAvailable
import com.ms.square.aether.core.internal.isPowerSaveMode
import com.ms.square.aether.core.internal.isReducedMotionEnabled

/**
 * Applies an [AetherEffect] as an overlay rendered on top of content via drawWithContent.
 *
 * Use for weather effects that render on a transparent layer above the content.
 * No-op on devices below API 33 (Tiramisu) or when system animations are disabled.
 *
 * @param effect The effect to apply.
 * @param qualityPreset Quality level affecting rendering complexity.
 * @param fallback Modifier to apply on unsupported devices. Defaults to [Modifier].
 */
@Composable
public fun Modifier.aetherOverlay(
  effect: AetherEffect,
  qualityPreset: QualityPreset = QualityPreset.HIGH,
  fallback: Modifier = Modifier,
): Modifier {
  if (!isAgslAvailable()) return this.then(fallback)

  val (resolvedQuality, enabled) = resolveEnvironment(effect, qualityPreset)
  return this.then(AetherOverlayElement(effect, resolvedQuality, enabled))
}

/**
 * Resolves the effective quality preset and enabled state based on system power
 * and accessibility settings.
 * ie.enabled = false when the user has "Remove animations" on and the effect is animated.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
private fun resolveEnvironment(effect: AetherEffect, qualityPreset: QualityPreset): Pair<QualityPreset, Boolean> {
  val powerSave = isPowerSaveMode()
  val reducedMotion = isReducedMotionEnabled()
  val resolvedQuality = if (powerSave) QualityPreset.LOW else qualityPreset
  val enabled = !(reducedMotion && effect.isAnimated)
  return resolvedQuality to enabled
}
