@file:Suppress("MagicNumber")

package com.ms.square.aether.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.rememberCameraPositionState
import com.ms.square.aether.core.AetherEffect
import com.ms.square.aether.core.aetherOverlay
import com.ms.square.aether.weather.Clouds
import com.ms.square.aether.weather.LightningFlash
import com.ms.square.aether.weather.Rain
import com.ms.square.aether.weather.Snow
import kotlinx.coroutines.launch

private val tokyo = LatLng(35.6812, 139.7671)
private const val DEFAULT_ZOOM = 12f

// Google Maps standard dark/night style
private const val DARK_MAP_STYLE = """[
  {"elementType":"geometry","stylers":[{"color":"#212121"}]},
  {"elementType":"labels.text.fill","stylers":[{"color":"#757575"}]},
  {"elementType":"labels.text.stroke","stylers":[{"color":"#212121"}]},
  {"featureType":"administrative","elementType":"geometry","stylers":[{"color":"#757575"}]},
  {"featureType":"poi","elementType":"geometry","stylers":[{"color":"#181818"}]},
  {"featureType":"road","elementType":"geometry.fill","stylers":[{"color":"#2c2c2c"}]},
  {"featureType":"road","elementType":"geometry.stroke","stylers":[{"color":"#212121"}]},
  {"featureType":"road.highway","elementType":"geometry.fill","stylers":[{"color":"#3c3c3c"}]},
  {"featureType":"transit","elementType":"geometry","stylers":[{"color":"#2f3948"}]},
  {"featureType":"water","elementType":"geometry","stylers":[{"color":"#000000"}]},
  {"featureType":"water","elementType":"labels.text.fill","stylers":[{"color":"#3d3d3d"}]}
]"""

private enum class MapStyle(val label: String) {
  LIGHT("Light"),
  DARK("Dark"),
  SATELLITE("Satellite"),
}

private enum class WeatherType(val label: String) {
  RAIN("Rain"),
  SNOW("Snow"),
  CLOUDS("Clouds"),
}

@Suppress("LongMethod")
@Composable
fun MapShowcase(contentPadding: PaddingValues = PaddingValues()) {
  var selectedWeather by remember { mutableStateOf(WeatherType.RAIN) }
  var mapStyle by remember { mutableStateOf(MapStyle.DARK) }
  var lightningEnabled by remember { mutableStateOf(false) }

  // Default effects — halos provide contrast on any map style, no custom colors needed
  val rain = remember { Rain(intensity = 2) }
  val snow = remember { Snow(density = 3) }
  val clouds = remember { Clouds(coverage = 0.45f) }
  val lightning = remember { LightningFlash(progress = 0f) }

  val currentEffect: AetherEffect = when (selectedWeather) {
    WeatherType.RAIN -> rain
    WeatherType.SNOW -> snow
    WeatherType.CLOUDS -> clouds
  }

  val cameraPositionState = rememberCameraPositionState {
    position = CameraPosition.fromLatLngZoom(tokyo, DEFAULT_ZOOM)
  }

  Box(modifier = Modifier.fillMaxSize()) {
    // Full-screen map with configurable style
    val mapProperties = remember(mapStyle) {
      when (mapStyle) {
        MapStyle.LIGHT -> MapProperties()
        MapStyle.DARK -> MapProperties(mapStyleOptions = MapStyleOptions(DARK_MAP_STYLE))
        MapStyle.SATELLITE -> MapProperties(mapType = MapType.SATELLITE)
      }
    }
    GoogleMap(
      modifier = Modifier.fillMaxSize(),
      cameraPositionState = cameraPositionState,
      properties = mapProperties
    )

    // Weather overlay — top half only, fades out toward center
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .fillMaxHeight(0.55f)
        .align(Alignment.TopCenter)
        .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        .drawWithContent {
          drawContent()
          // Gradient mask: fully visible at top, fades to transparent at bottom
          drawRect(
            brush = Brush.verticalGradient(
              colors = listOf(Color.Black, Color.Transparent),
              startY = size.height * 0.4f,
              endY = size.height
            ),
            blendMode = BlendMode.DstIn
          )
        }
        .aetherOverlay(currentEffect)
        .then(if (lightningEnabled) Modifier.aetherOverlay(lightning) else Modifier)
    )

    // Floating control panel
    Card(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth()
        .padding(bottom = contentPadding.calculateBottomPadding() + 16.dp)
        .padding(horizontal = 16.dp),
      shape = RoundedCornerShape(16.dp),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
      )
    ) {
      Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        // Map style selector
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Map Style", style = MaterialTheme.typography.labelMedium)
          MapStyle.entries.forEach { style ->
            FilterChip(
              selected = mapStyle == style,
              onClick = { mapStyle = style },
              label = { Text(style.label) }
            )
          }
        }

        // Effect selector — weather + lightning toggle
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          WeatherType.entries.forEach { type ->
            FilterChip(
              selected = selectedWeather == type,
              onClick = { selectedWeather = type },
              label = { Text(type.label) }
            )
          }
          FilterChip(
            selected = lightningEnabled,
            onClick = { lightningEnabled = !lightningEnabled },
            label = { Text("Lightning") }
          )
        }

        // Per-effect controls
        when (selectedWeather) {
          WeatherType.RAIN -> MapRainControls(rain)
          WeatherType.SNOW -> MapSnowControls(snow)
          WeatherType.CLOUDS -> MapCloudsControls(clouds)
        }

        if (lightningEnabled) {
          val scope = rememberCoroutineScope()
          Button(
            onClick = { scope.launch { lightning.flash() } },
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Trigger Flash")
          }
        }
      }
    }
  }
}

@Composable
private fun MapRainControls(effect: Rain) {
  SliderControl("Intensity", effect.intensity.toFloat(), 1f, 5f, steps = 4) { v ->
    effect.intensity = v.toInt()
  }
}

@Composable
private fun MapSnowControls(effect: Snow) {
  SliderControl("Density", effect.density.toFloat(), 1f, 5f, steps = 4) { v ->
    effect.density = v.toInt()
  }
}

@Composable
private fun MapCloudsControls(effect: Clouds) {
  SliderControl("Coverage", effect.coverage, 0f, 1f) { v ->
    effect.coverage = v
  }
}
