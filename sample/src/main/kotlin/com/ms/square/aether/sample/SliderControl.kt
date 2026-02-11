package com.ms.square.aether.sample

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SliderControl(
  label: String,
  value: Float,
  min: Float,
  max: Float,
  steps: Int = 0,
  onValueChange: (Float) -> Unit,
) {
  Column {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(label, style = MaterialTheme.typography.bodyMedium)
      Text("%.2f".format(value), style = MaterialTheme.typography.bodySmall)
    }
    Slider(
      value = value,
      onValueChange = onValueChange,
      valueRange = min..max,
      steps = steps
    )
  }
}
