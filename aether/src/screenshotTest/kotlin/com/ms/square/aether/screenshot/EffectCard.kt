@file:Suppress("MagicNumber")

package com.ms.square.aether.screenshot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.ms.square.aether.core.AetherEffect
import com.ms.square.aether.core.aetherOverlay

/**
 * Shared composable for screenshot tests: dark gradient background + effect overlay.
 */
@Composable
internal fun EffectCard(effect: AetherEffect) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Brush.verticalGradient(
          colors = listOf(Color(0xFF1a1a2e), Color(0xFF16213e))
        )
      )
      .aetherOverlay(effect)
  )
}
