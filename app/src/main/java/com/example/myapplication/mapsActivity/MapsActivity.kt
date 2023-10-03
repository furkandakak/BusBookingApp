package com.example.myapplication.mapsActivity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.R.*
import com.example.myapplication.adapter.BusListAdapter
import com.example.myapplication.dataClasses.BookBus
import com.example.myapplication.dataClasses.Coordinate
import com.example.myapplication.dataClasses.TripsDetail
import com.example.myapplication.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, MapsContract.View,
    GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private lateinit var presenter: MapsContract.Presenter
    private lateinit var binding: ActivityMapsBinding

    private lateinit var list: List<Coordinate>
    private lateinit var adapter: BusListAdapter

    private lateinit var bottomSheetDialog: BottomSheetDialog

    var clickedMarker: Marker? = null

    private val completedMarker = mutableListOf<Marker>()
    private val allMarker = mutableListOf<Marker>()
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        presenter = MapsPresenter()
        presenter.attachView(this)
        presenter.fetchCoordinates()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Initialize the FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }

    override fun showCoordinatesOnMap(coordinates: List<Coordinate>) {

        // Creating a custom marker icon from the drawable resource
        val customMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.point)

        // Add markers to the map based on the fetched coordinates
        for (coordinate in coordinates) {
            val latLng = parseCoordinates(coordinate.center_coordinates)
            val markerTitle = "${coordinate.trips_count} Trips"

            // Create a marker with the custom icon
            val marker = mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(markerTitle)
                    .icon(customMarkerIcon)
            )

            // Set the marker id as coordinate.id
            if (marker != null) {
                marker.tag = coordinate.id
            }
            allMarker.add(marker!!)
        }

        list = coordinates
    }


    override fun showErrorDialog() {
        showBookErrorDialog(this)
    }

    override fun bookSuccess() {
        val successIcon = BitmapDescriptorFactory.fromResource(R.drawable.completed)
        bottomSheetDialog.dismiss()
        clickedMarker?.setIcon(successIcon)
        completedMarker.add(clickedMarker!!)
    }

    private fun parseCoordinates(coordinates: String): LatLng {
        val parts = coordinates.split(",")
        val lat = parts[0].toDouble()
        val lng = parts[1].toDouble()
        return LatLng(lat, lng)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        val mapStyleOptions = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style)
        mMap.setMapStyle(mapStyleOptions)

        mMap.setOnMapClickListener(this)

        mMap.setOnMarkerClickListener(this)

        // Check for location permission
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is granted, get the current location
            fusedLocationClient.lastLocation
                .addOnSuccessListener(this) { location: Location? ->
                    // Got last known location.
                    if (location != null) {
                        val currentLatLng = LatLng(location.latitude, location.longitude)

                        val myLocation =
                            BitmapDescriptorFactory.fromResource(R.drawable.my_location)
                        // Add a marker to current location
                        mMap.addMarker(
                            MarkerOptions().position(currentLatLng).title("My Location")
                                .icon(myLocation)
                        )

                        // Move the camera to center on your current location
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 10f))
                    }
                }
        } else {
            // Permission is not granted, request the permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                restartActivity()
            } else {
                // Permission denied
            }
        }
    }


    private fun restartActivity() {
        val intent = Intent(this, MapsActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }


    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onMarkerClick(p0: Marker): Boolean {

        val point = BitmapDescriptorFactory.fromResource(R.drawable.point)
        val success = BitmapDescriptorFactory.fromResource(R.drawable.completed)

        allMarker.map { it.setIcon(point) }
        completedMarker.map { it.setIcon(success) }


        if (p0.title != "My Location") {
            val selected = BitmapDescriptorFactory.fromResource(R.drawable.selected_point)
            p0.setIcon(selected)
            clickedMarker = p0

            binding.busListButton.visibility = View.VISIBLE

            adapter = BusListAdapter(list, p0.tag.toString().toInt())
            adapter.notifyDataSetChanged()

            binding.busListButton.setOnClickListener {
                showBusListDialog(this)
                clickedMarker = p0
            }
        } else {
            binding.busListButton.visibility = View.GONE
        }
        return false
    }

    override fun onMapClick(p0: LatLng) {
        val point = BitmapDescriptorFactory.fromResource(R.drawable.point)
        val success = BitmapDescriptorFactory.fromResource(R.drawable.completed)
        binding.busListButton.visibility = View.GONE

        val clickedMarkerId = clickedMarker?.id
        val completedMarkerIds = completedMarker.map { it.id }

        if (!completedMarkerIds.contains(clickedMarkerId)) {
            clickedMarker?.setIcon(point)
        } else {
            clickedMarker?.setIcon(success)
        }
    }

    private fun showBusListDialog(context: Context) {
        bottomSheetDialog = BottomSheetDialog(context)

        val view = layoutInflater.inflate(R.layout.bus_dialog, null)

        //RecyclerView and set its adapter
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewBus)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
        adapter.notifyDataSetChanged()

        adapter.setOnItemClickListener(object : BusListAdapter.OnItemClickListener {
            override fun onButtonClick(position: Int, item: TripsDetail, id: Int) {
                val stationId = id
                val tripId = item.id
                val bookBus = BookBus(stationId, tripId)

                presenter.sendPostRequest(stationId, tripId, bookBus)
            }
        })

        bottomSheetDialog.setContentView(view)

        val bottomSheet =
            bottomSheetDialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT

        val bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet!!)
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        bottomSheetDialog.show()
    }

    @SuppressLint("MissingInflatedId")
    private fun showBookErrorDialog(context: Context) {
        val inflater = LayoutInflater.from(context)
        val dialogView: View = inflater.inflate(layout.book_error_dialog, null)

        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setView(dialogView)

        val cancelButton = dialogView.findViewById<CardView>(id.selectTrip)

        val alertDialog = alertDialogBuilder.create()

        cancelButton.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.setCancelable(false)
        alertDialog.show()
    }
}
