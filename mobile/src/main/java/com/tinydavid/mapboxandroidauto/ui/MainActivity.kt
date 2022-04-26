package com.tinydavid.mapboxandroidauto.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils.replace
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import androidx.appcompat.app.AppCompatActivity
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.navigation.base.ExperimentalPreviewMapboxNavigationAPI
import com.mapbox.navigation.core.MapboxNavigationProvider
import com.mapbox.navigation.core.trip.session.TripSessionState
import com.mapbox.navigation.examples.androidauto.app.routerequest.MapLongClickRouteRequest
import com.mapbox.navigation.ui.maps.NavigationStyles
import com.tinydavid.android_auto.*
import com.tinydavid.car.location.CarLocationPuck
import com.tinydavid.mapboxandroidauto.R
import com.tinydavid.mapboxandroidauto.ReplayNavigationObserver
import com.tinydavid.mapboxandroidauto.databinding.ActivityMainBinding
import com.tinydavid.mapboxandroidauto.ui.navigation.ActiveGuidanceFragment
import com.tinydavid.mapboxandroidauto.ui.navigation.AppNavigationCamera
import com.tinydavid.mapboxandroidauto.ui.navigation.AppRouteLine
import com.tinydavid.mapboxandroidauto.ui.search.SearchFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), PermissionsListener {
    private val permissionsManager = PermissionsManager(this)
    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)

        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        if (!PermissionsManager.areLocationPermissionsGranted(this)) {
            permissionsManager.requestLocationPermissions(this)
        } else {
            startTripSession()

            MapLongClickRouteRequest().observeClicks(binding.mapView, lifecycle)
        }

        binding.mapView.getMapboxMap()
            .loadStyleUri(NavigationStyles.NAVIGATION_DAY_STYLE) { style ->
                lifecycle.addObserver(
                    AppRouteLine(
                        context = this,
                        style = style,
                        mapboxNavigation = MapboxNavigationProvider.retrieve(),
                        mapView = binding.mapView
                    )
                )
            }

        lifecycle.addObserver(
            AppNavigationCamera(binding.mapView, AppNavigationCamera.CameraMode.FOLLOWING)
        )

        binding.mapView.location.apply {
            locationPuck = CarLocationPuck.navigationPuck2D(this@MainActivity)
            enabled = true
            pulsingEnabled = true
            setLocationProvider(MapboxCarApp.carAppServices.location().navigationLocationProvider)
        }

        CoroutineScope(Dispatchers.Main).launch {
            MapboxCarApp.carAppState.collect { carAppState ->
                onCarAppStateChanged(carAppState)
            }
        }
    }

    private fun onCarAppStateChanged(carAppState: CarAppState) {
        when (carAppState) {
            FreeDriveState, RoutePreviewState -> {
                if (carAppStateFragment() !is SearchFragment) {
                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace<SearchFragment>(R.id.carAppStateFragment)
                    }
                }
            }
            ActiveGuidanceState, ArrivalState -> {
                if (carAppStateFragment() !is ActiveGuidanceFragment) {
                    supportFragmentManager.commit {
                        setReorderingAllowed(true)
                        replace<ActiveGuidanceFragment>(R.id.carAppStateFragment)
                    }
                }
            }
        }
    }

    private fun carAppStateFragment() =
        supportFragmentManager.findFragmentById(R.id.carAppStateFragment)

    override fun onBackPressed() {
        val backPressedHandled = when (val currentFragment = carAppStateFragment()) {
            is SearchFragment -> currentFragment.handleOnBackPressed()
            else -> false
        }
        if (!backPressedHandled) {
            super.onBackPressed()
        }
    }

    @SuppressLint("MissingPermission")
    @OptIn(ExperimentalPreviewMapboxNavigationAPI::class)
    private fun startTripSession() {
        val mapboxNavigation = MapboxNavigationProvider.retrieve()
        if (mapboxNavigation.getTripSessionState() != TripSessionState.STARTED) {
            if (ReplayNavigationObserver.ENABLE_REPLAY) {
                val mapboxReplayer = MapboxNavigationProvider.retrieve().mapboxReplayer
                mapboxReplayer.pushRealLocation(this, 0.0)
                mapboxNavigation.startReplayTripSession()
                mapboxReplayer.play()
            } else {
                mapboxNavigation.startTripSession()
            }
        }
    }

    override fun onExplanationNeeded(permissionsToExplain: MutableList<String>?) {
        Toast.makeText(
            this,
            "This app needs location and storage permissions in order to show its functionality.",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            startTripSession()
            Toast.makeText(
                this,
                "You didn't grant the permissions required to use the app",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}