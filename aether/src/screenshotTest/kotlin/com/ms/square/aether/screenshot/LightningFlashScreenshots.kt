@file:Suppress("MagicNumber")

package com.ms.square.aether.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.ms.square.aether.weather.LightningFlash

@PreviewTest
@Preview
@Composable
internal fun LightningFlashVisible() {
  // Use 1 bolt, no forks to keep CPU shader render within Layoutlib's 10s timeout
  EffectCard(LightningFlash(progress = 0.8f, boltCount = 1, forkIntensity = 0f))
}

@PreviewTest
@Preview
@Composable
internal fun LightningFlashFull() {
  EffectCard(LightningFlash(progress = 1.0f, boltCount = 1, forkIntensity = 0f))
}
