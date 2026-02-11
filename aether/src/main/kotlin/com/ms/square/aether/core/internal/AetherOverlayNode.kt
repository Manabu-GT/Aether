package com.ms.square.aether.core.internal

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.ObserverModifierNode
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.node.observeReads
import androidx.compose.ui.platform.InspectorInfo
import com.ms.square.aether.core.AetherEffect
import com.ms.square.aether.core.QualityPreset
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * [ModifierNodeElement] that creates and updates an [AetherOverlayModifierNode].
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal data class AetherOverlayElement(
  val effect: AetherEffect,
  val qualityPreset: QualityPreset,
  val enabled: Boolean,
) : ModifierNodeElement<AetherOverlayModifierNode>() {
  override fun create() = AetherOverlayModifierNode(effect, qualityPreset, enabled)
  override fun update(node: AetherOverlayModifierNode) {
    node.update(effect, qualityPreset, enabled)
  }

  override fun InspectorInfo.inspectableProperties() {
    name = "aetherOverlay"
    properties["effect"] = effect
    properties["qualityPreset"] = qualityPreset
    properties["enabled"] = enabled
  }
}

/**
 * Modifier.Node that draws the effect shader as an overlay on top of content.
 * Uses drawWithContent to render content first, then the shader on a transparent layer.
 *
 * Animated effects use a VSYNC-aligned loop via [withFrameNanos].
 * Non-animated effects react to snapshot state changes via [ObserverModifierNode].
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal class AetherOverlayModifierNode(
  private var effect: AetherEffect,
  private var qualityPreset: QualityPreset,
  private var enabled: Boolean,
) : Modifier.Node(),
  DrawModifierNode,
  ObserverModifierNode {
  private var shader = ShaderCache.getOrCreate(effect.shaderSource)
  private var shaderBrush = shader?.let { ShaderBrush(it) }
  private var animationJob: Job? = null
  private var frameTimeNanos: Long = 0L

  override fun onAttach() {
    startAnimation()
  }

  override fun onDetach() {
    animationJob?.cancel()
  }

  fun update(newEffect: AetherEffect, newQuality: QualityPreset, newEnabled: Boolean) {
    val shaderChanged = newEffect.shaderSource != effect.shaderSource
    effect = newEffect
    qualityPreset = newQuality
    enabled = newEnabled
    if (shaderChanged) {
      shader = ShaderCache.getOrCreate(effect.shaderSource)
      shaderBrush = shader?.let { ShaderBrush(it) }
    }
    restartAnimation()
    invalidateDraw()
  }

  private fun startAnimation() {
    animationJob?.cancel()
    if (!enabled || !effect.isAnimated) return

    animationJob = coroutineScope.launch {
      val skipNanos = skipIntervalNanos(qualityPreset)
      var lastProcessedNanos = 0L

      while (isActive) {
        withFrameNanos { nanos ->
          if (nanos - lastProcessedNanos >= skipNanos) {
            frameTimeNanos = nanos
            lastProcessedNanos = nanos
            invalidateDraw()
          }
        }
      }
    }
  }

  private fun restartAnimation() {
    if (isAttached) {
      startAnimation()
    }
  }

  override fun onObservedReadsChanged() {
    invalidateDraw()
  }

  override fun ContentDrawScope.draw() {
    drawContent()
    if (!enabled) return
    val s = shader ?: return
    val brush = shaderBrush ?: return

    s.setFloatUniform("resolution", size.width, size.height)
    if (effect.isAnimated) {
      s.setFloatUniform("time", frameTimeNanos / NANOS_PER_SECOND)
    }
    observeReads {
      UniformBinder.bindAll(s, effect.uniforms())
    }
    drawRect(brush = brush)
  }
}
