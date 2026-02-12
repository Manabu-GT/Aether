package com.ms.square.aether.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.ms.square.aether.weather.Rain

@PreviewTest
@Preview
@Composable
internal fun RainDefault() {
  EffectCard(Rain())
}

@PreviewTest
@Preview
@Composable
internal fun RainLight() {
  EffectCard(Rain.light())
}

@PreviewTest
@Preview
@Composable
internal fun RainHeavy() {
  EffectCard(Rain.heavy())
}

@PreviewTest
@Preview
@Composable
internal fun RainStorm() {
  EffectCard(Rain.storm())
}
