package com.example.myapplication.dataClasses

data class Coordinate(
    val center_coordinates: String,
    val name: String,
    val trips_count: Int,
    val id: Int,
    val trips: List<TripsDetail>? = null
)

data class TripsDetail(val bus_name: String, val id: Int, val time: String)