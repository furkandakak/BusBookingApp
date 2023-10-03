package com.example.myapplication.mapsActivity

import com.example.myapplication.dataClasses.BookBus
import com.example.myapplication.dataClasses.Coordinate

interface MapsContract {
    interface View {
        fun showCoordinatesOnMap(coordinates: List<Coordinate>)
        fun showErrorDialog()
        fun bookSuccess()
    }

    interface Presenter {
        fun fetchCoordinates()
        fun attachView(view: View)
        fun detachView()
        fun sendPostRequest(stationId: Int, tripId: Int, bookBus: BookBus)
    }
}
