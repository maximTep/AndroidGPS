package com.example.androidgps

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.JsonReader
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.androidgps.databinding.ActivityMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketTimeoutException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


fun hideKeyboardFrom(context: Context, view: View?) {
    val imm =
        context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view?.windowToken, 0)
}


fun connection_test()
{
    val sock = Socket()

    sock.connect(InetSocketAddress("87.119.229.40", 9090))


    sock.outputStream.write("Mobile string sent :)".toByteArray())
    sock.shutdownOutput()


    var data = ""
    val len = sock.getInputStream().available()
    for(i in 1..len)
    {
        data += sock.getInputStream().read().toChar()
    }

    Log.d("INFO", "Connection state: " + sock.isConnected.toString())

    sock.close()
}


fun deprecatedSendDataToServer(activity: Activity ,apiResponse: String, city: String): Boolean
{
    val sock = Socket()
    try {
        sock.connect(InetSocketAddress("87.119.229.40", 9090), 2000)
    }
    catch(E: SocketTimeoutException)
    {
        Toast.makeText(activity, "Сервер хранения не доступен", Toast.LENGTH_LONG).show()
        Log.d("INFO", "Error: $E")
        return false
    }
    if(!sock.isConnected)
    {
        Toast.makeText(activity, "Сервер хранения не доступен", Toast.LENGTH_LONG).show()
        Log.d("INFO", "Server is not available")
        return false
    }

    Log.d("INFO", "Server is available")


    val weather = JSONObject(apiResponse).getJSONArray("weather")
    val desc = weather.getJSONObject(0).getString("description")
    val temp = JSONObject(apiResponse).getJSONObject("main").getDouble("temp")


    val currentDate = Date();
    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    val date = dateFormat.format(currentDate);
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val time = timeFormat.format(currentDate)

    val result = JSONObject()
    result.put("city", city)
    result.put("date", date)
    result.put("time", time)
    result.put("desc", desc)
    result.put("temp", temp)

    sock.outputStream.write(result.toString().toByteArray())


    sock.close()
    return true
}


fun sendDataToServer(activity: Activity, data: String): Boolean {
    val ip = "87.119.224.6"


    val sock = Socket()
    try {
        sock.connect(InetSocketAddress(ip, 9090), 5000)
    }
    catch(E: SocketTimeoutException)
    {
        //Toast.makeText(activity, "Сервер хранения недоступен", Toast.LENGTH_LONG).show()
        Log.d("INFO", "Error: $E")
        return false
    }
    if(!sock.isConnected)
    {
        //Toast.makeText(activity, "Сервер хранения недоступен", Toast.LENGTH_LONG).show()
        Log.d("INFO", "Server is not available")
        return false
    }

    Log.d("INFO", "Server is available")




    sock.outputStream.write(data.toByteArray())


    sock.close()
    return true
}


fun receiveDataFromServer(activity: Activity): String{
    val ip = "87.119.224.6"

    val sock = Socket()
    try {
        sock.connect(InetSocketAddress(ip, 9090), 5000)
    }
    catch(E: SocketTimeoutException)
    {
        //Toast.makeText(activity, "Сервер хранения недоступен", Toast.LENGTH_LONG).show()
        Log.d("INFO", "Error: $E")
        return "error"
    }
    if(!sock.isConnected)
    {
        Toast.makeText(activity, "Сервер хранения недоступен", Toast.LENGTH_LONG).show()
        Log.d("INFO", "Server is not available")
        return "error"
    }

    Thread.sleep(1200)

    var data = ""
    val len = sock.getInputStream().available()
    for(i in 1..len)
    {
        data += sock.getInputStream().read().toChar()
    }

    sock.close()
    return data
}





class MapsActivity : AppCompatActivity(),
    OnMapReadyCallback,
    GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    fun updateLocations(view: View?){
        val TAG_CODE_PERMISSION_LOCATION = 100
        if (ActivityCompat.checkSelfPermission(this@MapsActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this@MapsActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("INFO", "Asking for permission")
            ActivityCompat.requestPermissions(
                this@MapsActivity, arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                TAG_CODE_PERMISSION_LOCATION
            )
        }
        if (ActivityCompat.checkSelfPermission(this@MapsActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this@MapsActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this@MapsActivity, "Permissions declined", Toast.LENGTH_SHORT).show()
            return
        }
        mMap.isMyLocationEnabled = true



            fusedLocationClient.lastLocation.addOnSuccessListener { location : Location? ->
                if (location != null) {
                    val selfMark = LatLng(location.latitude, location.longitude)
                    //mMap.addMarker(MarkerOptions().position(selfMark).title("Me"))
                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(selfMark))

                    var locations:ArrayList<String> = ArrayList<String>()

                    GlobalScope.launch {
                        val id: String = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
                        val locJson = JSONObject()
                        locJson.put("id", id)
                        locJson.put("latitude", location.latitude)
                        locJson.put("longitude", location.longitude)

                        sendDataToServer(this@MapsActivity, "setLoc $locJson")

                        sendDataToServer(this@MapsActivity, "getLoc")

                        val servData = receiveDataFromServer(this@MapsActivity)
                        locations = ArrayList(servData.split("\n"))
                        locations.remove("")
                        for (loc in locations){
                            val lat = JSONObject(loc).getDouble("latitude")
                            val long = JSONObject(loc).getDouble("longitude")

                            val mark = LatLng(lat, long)
                            this@MapsActivity.runOnUiThread {
                                mMap.addMarker(MarkerOptions().position(mark))

                            }
                        }
                    }





                }

        }




        Toast.makeText(this, "Locations updated", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)




    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
//        val sydney = LatLng(-34.0, 151.0)
//        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))



        mMap.setOnMyLocationButtonClickListener(this)
        mMap.setOnMyLocationClickListener(this)

        GlobalScope.launch {




        }



    }


    override fun onMyLocationClick(location: Location) {
        Toast.makeText(this, "Current location:\n$location", Toast.LENGTH_LONG)
            .show()
    }

    override fun onMyLocationButtonClick(): Boolean {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT)
            .show()
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false
    }







}












