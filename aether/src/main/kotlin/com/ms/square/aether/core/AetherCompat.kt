package com.ms.square.aether.core

import com.ms.square.aether.core.internal.isAgslAvailable

/** Returns true if the device supports Aether shader effects (Android 13+). */
public fun isAetherAvailable(): Boolean = isAgslAvailable()
