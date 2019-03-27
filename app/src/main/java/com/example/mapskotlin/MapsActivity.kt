package com.example.mapskotlin

import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.view.Menu
import android.view.MenuItem
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import android.content.DialogInterface
import android.graphics.Color
import android.os.AsyncTask
import android.support.v7.app.AlertDialog
import android.util.Log
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Exception


class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMarkerClickListener{

    private lateinit var usedLocationProviderClient: FusedLocationProviderClient

    private lateinit var lastLocation: Location

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onMarkerClick(p0: Marker?) = false

    private lateinit var mMap: GoogleMap

    private val MAP_TYPE_ITEMS = arrayOf<CharSequence>("Road Map", "Terrain", "Hybrid", "Satellite")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

      //  showMapTypeSelectorDialog()

        usedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater = menuInflater
        inflater.inflate(R.menu.menu_items,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.item1 -> return super.onOptionsItemSelected(item)
            R.id.item2 -> {
                showMapTypeSelectorDialog()
                return true
            }
            //do what you like
            else -> return super.onOptionsItemSelected(item)
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        /*// Add a marker in Sydney and move the camera
        val sydney = LatLng(-6.2970078, 106.6984299)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Udacoding"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))*/

        mMap.setOnMarkerClickListener(this)
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.uiSettings.isCompassEnabled = true

        setUpMap()

        /*val location1 = LatLng(-6.2970078,106.6984299)
        placeMarker(location1)
        val location2 = LatLng(-6.3047146,106.6418088)
        placeMarker(location2)*/

        val intent = intent
        val location1 = intent.getStringExtra("origin")
       // val convertLocation1 = getLatLng(location1)
        val location2 = intent.getStringExtra("destination")
       // val convertLocation2 = getLatLng(location2)

        val URL = getDirectionURL(location1,location2)
        //val URL = getDirectionURL(location1,location2)
        GetDirection(URL).execute()
    }

    private fun placeMarker(location: LatLng){

        val markerOption = MarkerOptions().position(location)
        markerOption.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
        mMap.addMarker(markerOption)
    }

    private fun setUpMap(){
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        mMap.isMyLocationEnabled = true
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        usedLocationProviderClient.lastLocation.addOnSuccessListener(this) {location ->

            if (location != null){
                lastLocation = location
                val currentLatLong = LatLng(location.latitude,location.longitude)
                placeMarker(currentLatLong)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong,13f))

            }
        }
    }

    private fun showMapTypeSelectorDialog() {
        // Prepare the dialog by setting up a Builder.
        val fDialogTitle = "Select Map Type"
        val builder = AlertDialog.Builder(this)
        builder.setTitle(fDialogTitle)

        // Find the current map type to pre-check the item representing the current state.
        val checkItem = mMap.mapType - 1

        // Add an OnClickListener to the dialog, so that the selection will be handled.
        builder.setSingleChoiceItems(
            MAP_TYPE_ITEMS,
            checkItem,
            DialogInterface.OnClickListener { dialog, item ->
                // Locally create a finalised object.

                // Perform an action depending on which item was selected.
                //"Road Map", "Terrain", "Hybrid", "Satellite"
                when (item) {
                    1 -> mMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
                    2 -> mMap.mapType = GoogleMap.MAP_TYPE_TERRAIN
                    3 -> mMap.mapType = GoogleMap.MAP_TYPE_HYBRID
                    else -> mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
                }
                dialog.dismiss()
            }
        )

        // Build the dialog and show it.
        val fMapTypeDialog = builder.create()
        fMapTypeDialog.setCanceledOnTouchOutside(true)
        fMapTypeDialog.show()
    }

    fun getDirectionURL(origin: String, dest: String): String{
        return "https://maps.googleapis.com/maps/api/directions/json?origin=$origin&destination=$dest&sensor=false&mode=driving&key=AIzaSyADqqeKcwI3cXhRznauSIPVYcKQidxa504"
    }

    /*fun getDirectionURL(origin: LatLng, dest: LatLng): String{
            return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=driving&key=AIzaSyADqqeKcwI3cXhRznauSIPVYcKQidxa504"
    }*/

    private inner class GetDirection(val url: String) : AsyncTask<Void,Void,List<List<LatLng>>>(){
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body()?.string()
            Log.d("GoogleMap" , " data : $data")
            val result = ArrayList<List<LatLng>>()
            try {
                val respObj = Gson().fromJson(data,GoogleMapDTO::class.java)

            //    val latlngLocation1 =

                val path = ArrayList<LatLng>()

                for (i in 0..(respObj.routes[0].legs[0].steps.size-1)){
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            }catch (e: Exception){
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()
            for (i in result.indices){
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.BLUE)
                lineoption.geodesic(true)
            }
            mMap.addPolyline(lineoption)
        }

    }

    private inner class GetLatLng(val url: String) : AsyncTask<Void,Void,List<List<LatLng>>>(){
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body()?.string()
            Log.d("GoogleMap" , " data : $data")
            val result = ArrayList<List<LatLng>>()
            try {
                val respObj = Gson().fromJson(data,GoogleMapDTO::class.java)

                val path = ArrayList<LatLng>()

                for (i in 0..(respObj.routes[0].legs[0].steps.size-1)){
                    /*val startLatLng = LatLng(respObj.routes[0].legs[0].steps[i].start_location.lat.toDouble(),
                        respObj.routes[0].legs[0].steps[i].start_location.lng.toDouble())
                    path.add(startLatLng)
                    val endLatLng = LatLng(respObj.routes[0].legs[0].steps[i].end_location.lat.toDouble(),
                        respObj.routes[0].legs[0].steps[i].end_location.lng.toDouble())*/
                    path.addAll(decodePolyline(respObj.routes[0].legs[0].steps[i].polyline.points))
                }
                result.add(path)
            }catch (e: Exception){
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()
            for (i in result.indices){
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.BLUE)
                lineoption.geodesic(true)
            }
            mMap.addPolyline(lineoption)
        }

    }

    fun decodePolyline(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val latLng = LatLng((lat.toDouble() / 1E5),(lng.toDouble() / 1E5))
            poly.add(latLng)
        }

        return poly
    }

    /*fun getLatLng(origin: String): LatLng{
        val client = OkHttpClient()
        val request = Request.Builder().url("https://maps.googleapis.com/maps/api/directions/json?origin=$origin&destination=$origin&sensor=false&mode=driving&key=AIzaSyADqqeKcwI3cXhRznauSIPVYcKQidxa504").build()
        val response = client.newCall(request).execute()
        val data = response.body()?.string()

        val respObj = Gson().fromJson(data,GoogleMapDTO::class.java)
        val lat = respObj.routes[0].legs[0].start_location.lat.toDouble()
        val lng = respObj.routes[0].legs[0].start_location.lng.toDouble()

        return LatLng(lat,lng)
    }*/
}
