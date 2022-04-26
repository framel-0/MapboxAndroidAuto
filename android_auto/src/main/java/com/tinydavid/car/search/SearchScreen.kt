package com.tinydavid.car.search

import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.car.app.Screen
import androidx.car.app.model.*
import com.mapbox.navigation.base.route.NavigationRoute
import com.mapbox.search.result.SearchSuggestion
import com.tinydavid.android_auto.R
import com.tinydavid.android_auto.logAndroidAuto
import com.tinydavid.android_auto.logAndroidAutoFailure
import com.tinydavid.car.feedback.core.CarFeedbackSender
import com.tinydavid.car.feedback.ui.CarFeedbackAction
import com.tinydavid.car.feedback.ui.buildSearchPlacesCarFeedbackProvider
import com.tinydavid.car.preview.CarRoutePreviewScreen
import com.tinydavid.car.preview.CarRouteRequestCallback
import com.tinydavid.car.preview.RoutePreviewCarContext

/**
 * This screen allows the user to search for a destination.
 */
class SearchScreen(
    private val searchCarContext: SearchCarContext,
) : Screen(searchCarContext.carContext) {

    @VisibleForTesting
    var itemList = buildErrorItemList(R.string.car_search_no_results)

    // Cached to send to feedback.
    private var searchSuggestions: List<SearchSuggestion> = emptyList()

    private val carRouteRequestCallback = object : CarRouteRequestCallback {

        override fun onRoutesReady(placeRecord: PlaceRecord, routes: List<NavigationRoute>) {
            val routePreviewCarContext = RoutePreviewCarContext(searchCarContext.mainCarContext)

            screenManager.push(CarRoutePreviewScreen(routePreviewCarContext, placeRecord, routes))
        }

        override fun onUnknownCurrentLocation() {
            onErrorItemList(R.string.car_search_unknown_current_location)
        }

        override fun onDestinationLocationUnknown() {
            onErrorItemList(R.string.car_search_unknown_search_location)
        }

        override fun onNoRoutesFound() {
            onErrorItemList(R.string.car_search_no_results)
        }
    }

    override fun onGetTemplate(): Template {
        return SearchTemplate.Builder(
            object : SearchTemplate.SearchCallback {
                override fun onSearchTextChanged(searchText: String) {
                    doSearch(searchText)
                }

                override fun onSearchSubmitted(searchTerm: String) {
                    logAndroidAutoFailure("onSearchSubmitted not implemented $searchTerm")
                }
            })
            .setHeaderAction(Action.BACK)
            .setActionStrip(
                ActionStrip.Builder()
                    .addAction(
                        CarFeedbackAction(
                            searchCarContext.mainCarContext.mapboxCarMap,
                            CarFeedbackSender(),
                            buildSearchPlacesCarFeedbackProvider(
                                carContext = carContext,
                                searchSuggestions = searchSuggestions
                            )
                        ).getAction(this@SearchScreen)
                    )
                    .build()
            )
            .setShowKeyboardByDefault(false)
            .setItemList(itemList)
            .build()
    }

    fun doSearch(searchText: String) {
        searchCarContext.carSearchEngine.search(searchText) { suggestions ->
            searchSuggestions = suggestions
            if (suggestions.isEmpty()) {
                onErrorItemList(R.string.car_search_no_results)
            } else {
                val builder = ItemList.Builder()
                suggestions.forEach { suggestion ->
                    builder.addItem(searchItemRow(suggestion))
                }
                itemList = builder.build()
                invalidate()
            }
        }
    }

    private fun searchItemRow(suggestion: SearchSuggestion) = Row.Builder()
        .setTitle(suggestion.name)
        .addText(formatDistance(suggestion))
        .setOnClickListener { onClickSearch(suggestion) }
        .build()

    private fun formatDistance(searchSuggestion: SearchSuggestion): CharSequence {
        val distanceMeters = searchSuggestion.distanceMeters ?: return ""
        return searchCarContext.distanceFormatter.formatDistance(distanceMeters)
    }

    private fun onClickSearch(searchSuggestion: SearchSuggestion) {
        logAndroidAuto("onClickSearch $searchSuggestion")
        searchCarContext.carSearchEngine.select(searchSuggestion) { searchResults ->
            logAndroidAuto("onClickSearch select ${searchResults.joinToString()}")
            if (searchResults.isNotEmpty()) {
                searchCarContext.carRouteRequest.request(
                    PlaceRecordMapper.fromSearchResult(searchResults.first()),
                    carRouteRequestCallback
                )
            }
        }
    }

    private fun onErrorItemList(@StringRes stringRes: Int) {
        itemList = buildErrorItemList(stringRes)
        invalidate()
    }

    private fun buildErrorItemList(@StringRes stringRes: Int) = ItemList.Builder()
        .setNoItemsMessage(carContext.getString(stringRes))
        .build()

    companion object {
        // TODO turn this into something typesafe
        fun parseResult(results: Any?): Any? {
            return results
        }
    }
}
