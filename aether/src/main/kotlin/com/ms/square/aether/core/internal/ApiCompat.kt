package com.ms.square.aether.core.internal

import android.os.Build

/** Returns true if the device supports AGSL (API 33+ / Tiramisu). */
internal fun isAgslAvailable(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
