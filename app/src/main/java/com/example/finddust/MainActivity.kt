package com.example.finddust

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import com.example.finddust.data.Repository
import com.example.finddust.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var cancellationTokenSource: CancellationTokenSource? = null
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initVariables()
        requestLocationPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancellationTokenSource?.cancel()
        scope.cancel()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val locationPermissionGranted =
            requestCode == REQUEST_ACCESS_LOCATION_PERMISSION && grantResults[0] == PackageManager.PERMISSION_GRANTED

        if (!locationPermissionGranted) {
            finish()
        } else {
            fetchAirQualityData()
        }
    }

    private fun initVariables() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            REQUEST_ACCESS_LOCATION_PERMISSION
        )
    }

    @SuppressLint("MissingPermission")
    private fun fetchAirQualityData() {
        cancellationTokenSource = CancellationTokenSource()
        cancellationTokenSource?.let {
            fusedLocationProviderClient
                .getCurrentLocation(
                    LocationRequest.PRIORITY_HIGH_ACCURACY,
                    it.token
                ).addOnSuccessListener { location ->
                    scope.launch {
                        val monitoringStation = Repository.getNearbyMonitoringStation(location.latitude, location.longitude)
                        monitoringStation?.let { station ->
                            station.stationName?.let { stationName ->
                                val measuredValue = Repository.getLatestAirQualityData(stationName)

                                binding.textView.text = measuredValue.toString()
                            }
                        }
                    }
                }
        }
    }

    companion object {
        private const val REQUEST_ACCESS_LOCATION_PERMISSION = 100
    }
}