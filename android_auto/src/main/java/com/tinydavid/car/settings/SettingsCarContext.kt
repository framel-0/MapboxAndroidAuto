package com.tinydavid.car.settings

import com.tinydavid.car.MainCarContext

/**
 * Contains the dependencies for the settings screen.
 */
class SettingsCarContext(
    val mainCarContext: MainCarContext
) {
    val carContext = mainCarContext.carContext
    val carSettingsStorage = mainCarContext.carSettingsStorage
}
