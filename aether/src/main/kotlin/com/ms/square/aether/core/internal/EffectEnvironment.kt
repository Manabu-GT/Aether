package com.ms.square.aether.core.internal

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext

/** Queries system power and accessibility state. */
internal object EffectEnvironment {

  /**
   * Returns true if the system "Remove animations" setting is active,
   * meaning users prefer reduced motion.
   */
  fun isReducedMotionEnabled(context: Context): Boolean {
    val scale = Settings.Global.getFloat(
      context.contentResolver,
      Settings.Global.ANIMATOR_DURATION_SCALE,
      1.0f
    )
    return scale == 0f
  }

  /** Returns true if Battery Saver mode is active. */
  fun isPowerSaveMode(context: Context): Boolean {
    val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    return pm?.isPowerSaveMode == true
  }
}

/** Reactively observes Battery Saver mode via BroadcastReceiver. */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
internal fun isPowerSaveMode(): Boolean {
  val context = LocalContext.current
  return produceState(initialValue = EffectEnvironment.isPowerSaveMode(context)) {
    val receiver = object : BroadcastReceiver() {
      override fun onReceive(ctx: Context, intent: Intent) {
        value = EffectEnvironment.isPowerSaveMode(ctx)
      }
    }
    val filter = IntentFilter(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
    context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
    awaitDispose { context.unregisterReceiver(receiver) }
  }.value
}

/** Reactively observes the "Remove animations" accessibility setting via ContentObserver. */
@Composable
internal fun isReducedMotionEnabled(): Boolean {
  val context = LocalContext.current
  return produceState(initialValue = EffectEnvironment.isReducedMotionEnabled(context)) {
    val uri = Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE)
    val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
      override fun onChange(selfChange: Boolean) {
        value = EffectEnvironment.isReducedMotionEnabled(context)
      }
    }
    context.contentResolver.registerContentObserver(uri, false, observer)
    awaitDispose { context.contentResolver.unregisterContentObserver(observer) }
  }.value
}
