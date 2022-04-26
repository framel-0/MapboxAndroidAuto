package com.tinydavid.car.place_list_on_map

import com.tinydavid.car.search.PlaceRecord

interface PlacesListItemClickListener {
    fun onItemClick(placeRecord: PlaceRecord)
}
