package com.tinydavid.car.place_list_on_map

import com.mapbox.bindgen.Expected
import com.tinydavid.car.search.GetPlacesError
import com.tinydavid.car.search.PlaceRecord

interface PlacesListOnMapProvider {
    suspend fun getPlaces(): Expected<GetPlacesError, List<PlaceRecord>>
    fun cancel()
}
