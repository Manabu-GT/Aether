package com.ms.square.aether.core

/**
 * Quality presets that control effect rendering complexity.
 *
 * - [LOW]: Reduced density/complexity, ~30fps. Suitable for Battery Saver mode.
 * - [MEDIUM]: Balanced quality and performance, ~60fps.
 * - [HIGH]: Full quality with maximum density/complexity, capped at 60fps even on 120Hz.
 */
public enum class QualityPreset {
  LOW,
  MEDIUM,
  HIGH,
}
