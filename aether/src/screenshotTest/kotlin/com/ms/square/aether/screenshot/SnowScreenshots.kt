package com.ms.square.aether.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.ms.square.aether.weather.Snow

@PreviewTest
@Preview
@Composable
internal fun SnowDefault() {
  EffectCard(Snow())
}

@PreviewTest
@Preview
@Composable
internal fun SnowLight() {
  EffectCard(Snow.light())
}

@PreviewTest
@Preview
@Composable
internal fun SnowHeavy() {
  EffectCard(Snow.heavy())
}

@PreviewTest
@Preview
@Composable
internal fun SnowBlizzard() {
  EffectCard(Snow.blizzard())
}
