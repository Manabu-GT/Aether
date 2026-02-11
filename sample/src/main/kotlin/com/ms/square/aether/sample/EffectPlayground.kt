@file:Suppress("MagicNumber")

package com.ms.square.aether.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ms.square.aether.core.Wind
import com.ms.square.aether.core.aetherOverlay
import com.ms.square.aether.weather.Clouds
import com.ms.square.aether.weather.LightningFlash
import com.ms.square.aether.weather.Rain
import com.ms.square.aether.weather.Snow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val previewHeight = 480.dp
private const val MAX_INTENSITY = 5

private enum class RainPreset(val label: String) {
  LIGHT("Light"),
  MODERATE("Moderate"),
  HEAVY("Heavy"),
  STORM("Storm"),
  CUSTOM("Custom"),
}

private enum class SnowPreset(val label: String) {
  LIGHT("Light"),
  MODERATE("Moderate"),
  HEAVY("Heavy"),
  BLIZZARD("Blizzard"),
  CUSTOM("Custom"),
}

private enum class CloudsPreset(val label: String) {
  WISPY("Wispy"),
  PARTLY_CLOUDY("Partly Cloudy"),
  OVERCAST("Overcast"),
  CUSTOM("Custom"),
}

@Composable
fun EffectPlayground(demo: EffectDemo, contentPadding: PaddingValues = PaddingValues()) {
  Column(
    modifier = Modifier
      .fillMaxSize()
      .padding(contentPadding)
      .verticalScroll(rememberScrollState())
  ) {
    // Live preview
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .height(previewHeight)
        .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
        .background(
          Brush.verticalGradient(
            colors = listOf(Color(0xFF1a1a2e), Color(0xFF16213e))
          )
        )
        .aetherOverlay(demo.effect)
    )

    // Controls
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Text(
        text = demo.name,
        style = MaterialTheme.typography.headlineSmall
      )
      Text(
        text = demo.description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      when (val effect = demo.effect) {
        is Rain -> RainControls(effect)
        is Snow -> SnowControls(effect)
        is Clouds -> CloudsControls(effect)
        is LightningFlash -> LightningControls(effect)
        else -> Text("No controls available")
      }
    }
  }
}

@Composable
private fun <T> PresetChips(options: List<T>, selected: T, label: (T) -> String, onSelect: (T) -> Unit) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier.horizontalScroll(rememberScrollState())
  ) {
    options.forEach { option ->
      FilterChip(
        selected = selected == option,
        onClick = { onSelect(option) },
        label = { Text(label(option)) }
      )
    }
  }
}

@Composable
private fun RainControls(effect: Rain) {
  var selected by remember { mutableStateOf(RainPreset.MODERATE) }

  PresetChips(RainPreset.entries, selected, { it.label }) { preset ->
    selected = preset
    val source = when (preset) {
      RainPreset.LIGHT -> Rain.light()
      RainPreset.MODERATE -> Rain.moderate()
      RainPreset.HEAVY -> Rain.heavy()
      RainPreset.STORM -> Rain.storm()
      RainPreset.CUSTOM -> null
    }
    if (source != null) {
      effect.intensity = source.intensity
      effect.speed = source.speed
      effect.dropLength = source.dropLength
      effect.wind = source.wind
    }
  }

  if (selected == RainPreset.CUSTOM) {
    SliderControl("Intensity", effect.intensity.toFloat(), 1f, MAX_INTENSITY.toFloat(), 4) { v ->
      effect.intensity = v.roundToInt()
    }
    SliderControl("Speed", effect.speed, 0.2f, 3f) { v ->
      effect.speed = v
    }
    SliderControl("Drop Length", effect.dropLength, 0.3f, 3f) { v ->
      effect.dropLength = v
    }
    SliderControl("Wind", effect.wind.angle, -0.5f, 0.5f) { v ->
      effect.wind = Wind(angle = v, strength = effect.wind.strength)
    }
  }
}

@Composable
private fun SnowControls(effect: Snow) {
  var selected by remember { mutableStateOf(SnowPreset.MODERATE) }

  PresetChips(SnowPreset.entries, selected, { it.label }) { preset ->
    selected = preset
    val source = when (preset) {
      SnowPreset.LIGHT -> Snow.light()
      SnowPreset.MODERATE -> Snow.moderate()
      SnowPreset.HEAVY -> Snow.heavy()
      SnowPreset.BLIZZARD -> Snow.blizzard()
      SnowPreset.CUSTOM -> null
    }
    if (source != null) {
      effect.density = source.density
      effect.speed = source.speed
      effect.flakeSize = source.flakeSize
      effect.wind = source.wind
    }
  }

  if (selected == SnowPreset.CUSTOM) {
    SliderControl("Density", effect.density.toFloat(), 1f, MAX_INTENSITY.toFloat(), 4) { v ->
      effect.density = v.roundToInt()
    }
    SliderControl("Speed", effect.speed, 0.2f, 3f) { v ->
      effect.speed = v
    }
    SliderControl("Flake Size", effect.flakeSize, 0.3f, 3f) { v ->
      effect.flakeSize = v
    }
    SliderControl("Wind", effect.wind.strength, -1f, 1f) { v ->
      effect.wind = Wind(angle = effect.wind.angle, strength = v)
    }
  }
}

@Composable
private fun CloudsControls(effect: Clouds) {
  var selected by remember { mutableStateOf(CloudsPreset.PARTLY_CLOUDY) }

  PresetChips(CloudsPreset.entries, selected, { it.label }) { preset ->
    selected = preset
    val source = when (preset) {
      CloudsPreset.WISPY -> Clouds.wispy()
      CloudsPreset.PARTLY_CLOUDY -> Clouds.partlyCloudy()
      CloudsPreset.OVERCAST -> Clouds.overcast()
      CloudsPreset.CUSTOM -> null
    }
    if (source != null) {
      effect.coverage = source.coverage
      effect.speed = source.speed
    }
  }

  if (selected == CloudsPreset.CUSTOM) {
    SliderControl("Coverage", effect.coverage, 0f, 1f) { v ->
      effect.coverage = v
    }
    SliderControl("Speed", effect.speed, 0.2f, 3f) { v ->
      effect.speed = v
    }
  }
}

@Composable
private fun LightningControls(effect: LightningFlash) {
  val scope = rememberCoroutineScope()

  SliderControl("Brightness", effect.brightness, 0.5f, 3f) { v ->
    effect.brightness = v
  }
  SliderControl("Bolts", effect.boltCount.toFloat(), 1f, 5f, steps = 3) { v ->
    effect.boltCount = v.roundToInt()
  }
  SliderControl("Fork Intensity", effect.forkIntensity, 0f, 1f) { v ->
    effect.forkIntensity = v
  }
  Button(
    onClick = { scope.launch { effect.flash() } },
    modifier = Modifier.fillMaxWidth()
  ) {
    Text("Trigger Flash")
  }
}
