package com.ms.square.aether.core.internal

import com.ms.square.aether.core.QualityPreset

internal const val NANOS_PER_SECOND = 1_000_000_000f

/** VSYNC skip interval for ~30fps. */
private const val SKIP_NANOS_30FPS = 33_333_333L

/** VSYNC skip interval for ~60fps. */
private const val SKIP_NANOS_60FPS = 16_666_667L

/**
 * Returns the VSYNC frame skip interval in nanoseconds for the given [qualityPreset].
 * Frames arriving sooner than this interval after the last processed frame are skipped.
 */
internal fun skipIntervalNanos(qualityPreset: QualityPreset): Long =
  if (qualityPreset == QualityPreset.LOW) SKIP_NANOS_30FPS else SKIP_NANOS_60FPS
