package com.tinydavid.car.place_list_on_map

import android.location.Location
import android.text.SpannableString
import android.text.Spanned
import androidx.car.app.model.*
import com.mapbox.geojson.Point
import com.mapbox.navigation.base.formatter.UnitType
import com.mapbox.turf.TurfConstants
import com.mapbox.turf.TurfMeasurement
import com.tinydavid.car.search.PlaceRecord

class PlacesListItemMapper(
    private val placeMarkerRenderer: PlaceMarkerRenderer,
    private val unitType: UnitType
) {

    fun mapToItemList(
        anchorLocation: Location,
        places: List<PlaceRecord>,
        itemClickListener: PlacesListItemClickListener?
    ): ItemList {
        val listBuilder = ItemList.Builder()
        places.filter { it.coordinate != null }.forEach { placeRecord ->
            val distanceUnits = getDistanceUnits(unitType)
            val distance: Double = TurfMeasurement.distance(
                Point.fromLngLat(anchorLocation.longitude, anchorLocation.latitude),
                placeRecord.coordinate!!,
                distanceUnits.second
            )

            // fixme this was a copy/paste from a sample it should be improved for MB
            val description = SpannableString("   \u00b7 " + placeRecord.description)
            description.setSpan(
                DistanceSpan.create(Distance.create(distance, distanceUnits.first)),
                0,
                1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            description.setSpan(
                ForegroundCarColorSpan.create(CarColor.BLUE),
                0,
                1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            val isBrowsable = false

            listBuilder.addItem(
                Row.Builder()
                    .setTitle(placeRecord.name)
                    .addText(description)
                    .setMetadata(
                        Metadata.Builder()
                            .setPlace(
                                Place.Builder(
                                    CarLocation.create(
                                        Location("").also {
                                            it.latitude = placeRecord.coordinate.latitude()
                                            it.longitude = placeRecord.coordinate.longitude()
                                        }
                                    )
                                ).setMarker(
                                    PlaceMarker.Builder()
                                        .setIcon(getCarIcon(), PlaceMarker.TYPE_IMAGE)
                                        .build()
                                ).build()
                            )
                            .build()
                    )
                    .setOnClickListener { itemClickListener?.onItemClick(placeRecord) }
                    .setBrowsable(isBrowsable)
                    .build()
            )
        }

        return listBuilder.build()
    }

    private fun getCarIcon(): CarIcon {
        // fixme this is using a hardcoded icon and is taking for granted it won't be null
        return placeMarkerRenderer.renderMarker()
    }

    private fun getDistanceUnits(unitType: UnitType): Pair<Int, String> {
        return when (unitType) {
            UnitType.METRIC -> {
                Pair(Distance.UNIT_KILOMETERS, TurfConstants.UNIT_KILOMETERS)
            }
            UnitType.IMPERIAL -> {
                Pair(Distance.UNIT_MILES, TurfConstants.UNIT_MILES)
            }
        }
    }
}
