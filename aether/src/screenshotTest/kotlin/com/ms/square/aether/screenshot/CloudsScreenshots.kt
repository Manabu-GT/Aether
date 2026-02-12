package com.ms.square.aether.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.ms.square.aether.weather.Clouds

@PreviewTest
@Preview
@Composable
internal fun CloudsDefault() {
  EffectCard(Clouds())
}

@PreviewTest
@Preview
@Composable
internal fun CloudsWispy() {
  EffectCard(Clouds.wispy())
}

@PreviewTest
@Preview
@Composable
internal fun CloudsOvercast() {
  EffectCard(Clouds.overcast())
}
