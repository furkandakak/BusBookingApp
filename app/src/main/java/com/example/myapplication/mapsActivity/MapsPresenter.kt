package com.example.myapplication.mapsActivity

import com.example.myapplication.api.ApiClient
import com.example.myapplication.dataClasses.BookBus
import com.example.myapplication.dataClasses.Coordinate
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapsPresenter : MapsContract.Presenter {
    private var view: MapsContract.View? = null

    override fun attachView(view: MapsContract.View) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
    }

    //getting stations at near
    override fun fetchCoordinates() {
        val apiService = ApiClient.apiService
        val call = apiService.getCoordinates()

        call.enqueue(object : Callback<List<Coordinate>> {
            override fun onResponse(
                call: Call<List<Coordinate>>,
                response: Response<List<Coordinate>>
            ) {
                if (response.isSuccessful) {
                    val coordinates = response.body()
                    if (coordinates != null) {
                        view?.showCoordinatesOnMap(coordinates)
                    } else {
                        // empty response
                    }
                } else {
                    // handle API error
                }
            }

            override fun onFailure(call: Call<List<Coordinate>>, t: Throwable) {
                // Handle API failure
            }
        })
    }

    // Function to send the POST request
    override fun sendPostRequest(stationId: Int, tripId: Int, bookBus: BookBus) {
        val apiService = ApiClient.apiService
        val call = apiService.sendPostRequest(stationId, tripId, bookBus)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(
                call: Call<Void>,
                response: Response<Void>
            ) {
                if (response.isSuccessful) {
                    view?.bookSuccess()
                } else {
                    view?.showErrorDialog()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // Handle API failure
            }
        })
    }
}

