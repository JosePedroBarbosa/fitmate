package com.example.fitmate.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.*
import com.google.maps.android.compose.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun FindGymScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var hasPermission by remember { mutableStateOf(false) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    userLocation = LatLng(it.latitude, it.longitude)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        val granted = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) {
            hasPermission = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    userLocation = LatLng(it.latitude, it.longitude)
                }
            }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(

    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (userLocation != null) {
                var gyms by remember { mutableStateOf<List<Pair<String, LatLng>>>(emptyList()) }

                LaunchedEffect(userLocation) {
                    scope.launch {
                        val lat = userLocation!!.latitude
                        val lng = userLocation!!.longitude
                        gyms = fetchNearbyGyms(lat, lng, "AIzaSyCx45MAL2sghGilIyjnfon7QpQ7c8unnyA")
                    }
                }

                val cameraPositionState = rememberCameraPositionState {
                    position = CameraPosition.fromLatLngZoom(userLocation!!, 14f)
                }

                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    properties = MapProperties(isMyLocationEnabled = hasPermission)
                ) {
                    Marker(
                        state = MarkerState(position = userLocation!!),
                        title = "You are here",
                        icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)
                    )

                    gyms.forEach { (name, position) ->
                        Marker(
                            state = MarkerState(position = position),
                            title = name
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(16.dp))
                    Text("Getting your location...")
                }
            }
        }
    }
}

suspend fun fetchNearbyGyms(
    lat: Double,
    lng: Double,
    apiKey: String
): List<Pair<String, LatLng>> {
    return withContext(Dispatchers.IO) {
        val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                "?location=$lat,$lng" +
                "&radius=3000" + // raio de 3 km
                "&type=gym" +
                "&key=$apiKey"

        try {
            val response = URL(url).readText()
            val json = JSONObject(response)
            val results = json.getJSONArray("results")

            List(results.length()) { i ->
                val obj = results.getJSONObject(i)
                val name = obj.getString("name")
                val location = obj.getJSONObject("geometry").getJSONObject("location")
                val gymLat = location.getDouble("lat")
                val gymLng = location.getDouble("lng")
                name to LatLng(gymLat, gymLng)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
