package com.ms.square.aether.core.internal

import android.graphics.RuntimeShader
import android.os.Build
import androidx.annotation.RequiresApi
import com.ms.square.aether.core.UniformValue

/**
 * Binds [UniformValue] instances to a [RuntimeShader] using the appropriate
 * `setUniform*()` overload for each sealed type.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
internal object UniformBinder {

  fun bind(shader: RuntimeShader, name: String, value: UniformValue) {
    when (value) {
      is UniformValue.Float1 -> shader.setFloatUniform(name, value.v)
      is UniformValue.Float2 -> shader.setFloatUniform(name, value.x, value.y)
      is UniformValue.Float3 -> shader.setFloatUniform(name, value.x, value.y, value.z)
      is UniformValue.Float4 -> shader.setFloatUniform(name, value.x, value.y, value.z, value.w)
      is UniformValue.ColorValue -> {
        val color = value.color
        shader.setFloatUniform(name, color.red, color.green, color.blue, color.alpha)
      }
      is UniformValue.Int1 -> shader.setIntUniform(name, value.v)
    }
  }

  fun bindAll(shader: RuntimeShader, uniforms: Map<String, UniformValue>) {
    uniforms.forEach { (name, value) -> bind(shader, name, value) }
  }
}
