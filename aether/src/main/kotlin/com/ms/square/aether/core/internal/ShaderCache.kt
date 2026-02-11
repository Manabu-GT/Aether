package com.ms.square.aether.core.internal

import android.graphics.RuntimeShader
import android.os.Build
import android.util.Log
import android.util.LruCache
import androidx.annotation.RequiresApi

private const val LOG_TAG = "[Aether]"

private const val MAX_CACHE_SIZE = 8

/**
 * Caches [RuntimeShader] instances by their source string to avoid expensive recompilation
 * when effect parameters change (only uniforms need updating).
 *
 * Returns null if shader compilation fails, allowing callers to degrade gracefully.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal object ShaderCache {

  private val cache = object : LruCache<String, RuntimeShader>(MAX_CACHE_SIZE) {
    override fun sizeOf(key: String, value: RuntimeShader): Int = 1
  }

  fun getOrCreate(shaderSource: String): RuntimeShader? {
    cache[shaderSource]?.let { return it }

    return try {
      // this should be fast enough as it just parses and validates the shader source, its compilation
      // happens later during the rendering 1st time.
      RuntimeShader(shaderSource).also { cache.put(shaderSource, it) }
    } catch (e: IllegalArgumentException) {
      Log.e(LOG_TAG, "Failed to create a shader", e)
      null
    }
  }
}
