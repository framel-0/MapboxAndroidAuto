package com.tinydavid.car

import androidx.car.app.Screen
import androidx.car.app.ScreenManager
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.CarIcon
import androidx.core.graphics.drawable.IconCompat
import com.mapbox.search.MapboxSearchSdk
import com.tinydavid.android_auto.R
import com.tinydavid.car.feedback.core.CarFeedbackSender
import com.tinydavid.car.feedback.ui.CarFeedbackAction
import com.tinydavid.car.feedback.ui.buildFreeDriveFeedbackItemsProvider
import com.tinydavid.car.place_list_on_map.PlaceMarkerRenderer
import com.tinydavid.car.place_list_on_map.PlacesListItemMapper
import com.tinydavid.car.place_list_on_map.PlacesListOnMapScreen
import com.tinydavid.car.search.FavoritesApi
import com.tinydavid.car.search.SearchCarContext
import com.tinydavid.car.search.SearchScreen
import com.tinydavid.car.settings.CarSettingsScreen
import com.tinydavid.car.settings.SettingsCarContext

class MainActionStrip(
    private val screen: Screen,
    private val mainCarContext: MainCarContext
) {
    private val carContext = mainCarContext.carContext
    private val screenManager = carContext.getCarService(ScreenManager::class.java)

    /**
     * Build the action strip
     */
    fun builder() = ActionStrip.Builder()
        .addAction(buildSettingsAction())
        .addAction(buildFreeDriveFeedbackAction())
        .addAction(buildSearchAction())
        .addAction(buildFavoritesAction())

    /**
     * Build the settings action only
     */
    fun buildSettings() = ActionStrip.Builder()
        .addAction(buildSettingsAction())

    private fun buildFreeDriveFeedbackAction() =
        CarFeedbackAction(
            mainCarContext.mapboxCarMap,
            CarFeedbackSender(),
            buildFreeDriveFeedbackItemsProvider(screen.carContext)
        ).getAction(screen)

    private fun buildSettingsAction() = Action.Builder()
        .setIcon(
            CarIcon.Builder(
                IconCompat.createWithResource(
                    carContext, R.drawable.ic_settings
                )
            ).build()
        )
        .setOnClickListener {
            val settingsCarContext = SettingsCarContext(mainCarContext)
            carContext
                .getCarService(ScreenManager::class.java)
                .push(CarSettingsScreen(settingsCarContext))
        }
        .build()

    private fun buildSearchAction() = Action.Builder()
        .setIcon(
            CarIcon.Builder(
                IconCompat.createWithResource(
                    carContext,
                    R.drawable.ic_search_black36dp
                )
            ).build()
        )
        .setOnClickListener {
            screenManager.push(
                SearchScreen(SearchCarContext(mainCarContext))
            )
        }
        .build()

    private fun buildFavoritesAction() = Action.Builder()
        .setTitle(carContext.resources.getString(R.string.car_action_search_favorites))
        .setOnClickListener { screenManager.push(favoritesScreen()) }
        .build()

    private fun favoritesScreen(): PlacesListOnMapScreen {
        val placesProvider = FavoritesApi(
            mainCarContext.carContext,
            MapboxSearchSdk.serviceProvider.favoritesDataProvider()
        )
        return PlacesListOnMapScreen(
            mainCarContext,
            placesProvider,
            PlacesListItemMapper(
                PlaceMarkerRenderer(mainCarContext.carContext),
                mainCarContext
                    .mapboxNavigation
                    .navigationOptions
                    .distanceFormatterOptions
                    .unitType
            ),
            listOf(
                CarFeedbackAction(mainCarContext.mapboxCarMap, CarFeedbackSender(), placesProvider)
            )
        )
    }
}
