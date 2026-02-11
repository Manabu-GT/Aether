@file:Suppress("MagicNumber")

package com.ms.square.aether.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ms.square.aether.core.aetherOverlay
import com.ms.square.aether.weather.LightningFlash
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val previewHeight = 200.dp
private val cardCornerRadius = 16.dp

@Composable
fun EffectGallery(
  demos: List<EffectDemo>,
  onEffectSelected: (Int) -> Unit,
  contentPadding: PaddingValues = PaddingValues(),
) {
  LazyColumn(
    contentPadding = contentPadding,
    verticalArrangement = Arrangement.spacedBy(16.dp),
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    itemsIndexed(demos) { index, demo ->
      EffectCard(
        demo = demo,
        onClick = { onEffectSelected(index) }
      )
    }
  }
}

private const val FLASH_INTERVAL_MS = 2000L

@Composable
private fun EffectCard(demo: EffectDemo, onClick: () -> Unit) {
  // Auto-flash lightning on the gallery card so the preview is visible
  if (demo.effect is LightningFlash) {
    val lightning = demo.effect
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
      while (true) {
        delay(FLASH_INTERVAL_MS)
        scope.launch { lightning.flash() }
      }
    }
  }

  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(cardCornerRadius),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
  ) {
    Box {
      // Dark background for weather effects to show up nicely
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(previewHeight)
          .clip(RoundedCornerShape(cardCornerRadius))
          .background(
            Brush.verticalGradient(
              colors = listOf(
                Color(0xFF1a1a2e),
                Color(0xFF16213e)
              )
            )
          )
          .aetherOverlay(demo.effect)
      )

      // Label overlay at bottom
      Column(
        modifier = Modifier
          .align(Alignment.BottomStart)
          .fillMaxWidth()
          .background(Color.Black.copy(alpha = 0.5f))
          .padding(12.dp)
      ) {
        Text(
          text = demo.name,
          style = MaterialTheme.typography.titleMedium,
          color = Color.White
        )
        Text(
          text = demo.description,
          style = MaterialTheme.typography.bodySmall,
          color = Color.White.copy(alpha = 0.7f)
        )
      }
    }
  }
}
