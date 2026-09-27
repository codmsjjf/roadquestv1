package com.example.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import com.example.data.geo.GeoUtils
import com.example.data.model.GeoPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

data class LiveDriveTelemetry(
    val isDriving: Boolean = false,
    val isPaused: Boolean = false,
    val isSimulated: Boolean = false,
    val currentPoint: GeoPoint = GeoPoint(3.082, 101.583), // Default Klang Valley
    val currentSpeedKmh: Double = 0.0,
    val averageSpeedKmh: Double = 0.0,
    val topSpeedKmh: Double = 0.0,
    val distanceKm: Double = 0.0,
    val durationSeconds: Long = 0,
    val routePoints: List<GeoPoint> = emptyList(),
    val newlyDiscoveredRoads: List<String> = emptyList(),
    val explorationPercentGained: Double = 0.0,
    val routeName: String = "Federal Highway Sprint"
)

class LocationTracker(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var simulationJob: Job? = null
    private var tickerJob: Job? = null
    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null

    private val _telemetry = MutableStateFlow(LiveDriveTelemetry())
    val telemetry = _telemetry.asStateFlow()

    private var speedSamples = mutableListOf<Double>()

    val simulationPresets = listOf(
        "Karak Mountain Run (Gombak -> Genting)" to listOf(
            GeoPoint(3.238, 101.735),
            GeoPoint(3.260, 101.750),
            GeoPoint(3.285, 101.765),
            GeoPoint(3.305, 101.775),
            GeoPoint(3.330, 101.780),
            GeoPoint(3.345, 101.788),
            GeoPoint(3.360, 101.792),
            GeoPoint(3.390, 101.815),
            GeoPoint(3.420, 101.840)
        ),
        "Federal Highway Sprint (Klang -> KL)" to listOf(
            GeoPoint(3.045, 101.448),
            GeoPoint(3.055, 101.485),
            GeoPoint(3.068, 101.520),
            GeoPoint(3.076, 101.555),
            GeoPoint(3.082, 101.583),
            GeoPoint(3.090, 101.605),
            GeoPoint(3.102, 101.628),
            GeoPoint(3.111, 101.649),
            GeoPoint(3.120, 101.668),
            GeoPoint(3.134, 101.696)
        ),
        "MEX Highway Cruise (KL -> Putrajaya)" to listOf(
            GeoPoint(3.142, 101.718),
            GeoPoint(3.105, 101.705),
            GeoPoint(3.060, 101.692),
            GeoPoint(3.010, 101.685),
            GeoPoint(2.970, 101.680),
            GeoPoint(2.930, 101.682),
            GeoPoint(2.915, 101.650)
        ),
        "DASH Highway Express (Shah Alam -> Damansara)" to listOf(
            GeoPoint(3.135, 101.492),
            GeoPoint(3.140, 101.510),
            GeoPoint(3.145, 101.530),
            GeoPoint(3.150, 101.555),
            GeoPoint(3.155, 101.580),
            GeoPoint(3.160, 101.610),
            GeoPoint(3.165, 101.632)
        )
    )

    fun startDrive(useSimulation: Boolean = false, presetIndex: Int = 0) {
        speedSamples.clear()
        val initialPoints = if (useSimulation) {
            val preset = simulationPresets.getOrElse(presetIndex) { simulationPresets.first() }
            preset.second
        } else emptyList()

        val startLoc = initialPoints.firstOrNull() ?: GeoPoint(3.082, 101.583)
        val routeTitle = if (useSimulation) simulationPresets.getOrElse(presetIndex) { simulationPresets.first() }.first else "Live GPS Drive"

        _telemetry.value = LiveDriveTelemetry(
            isDriving = true,
            isPaused = false,
            isSimulated = useSimulation,
            currentPoint = startLoc,
            currentSpeedKmh = 0.0,
            averageSpeedKmh = 0.0,
            topSpeedKmh = 0.0,
            distanceKm = 0.0,
            durationSeconds = 0,
            routePoints = listOf(startLoc),
            newlyDiscoveredRoads = emptyList(),
            explorationPercentGained = 0.0,
            routeName = routeTitle
        )

        startTimer()

        if (useSimulation) {
            startSimulation(initialPoints)
        } else {
            startRealGps()
        }
    }

    private fun startTimer() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive && _telemetry.value.isDriving) {
                delay(1000)
                if (!_telemetry.value.isPaused) {
                    val current = _telemetry.value
                    _telemetry.value = current.copy(
                        durationSeconds = current.durationSeconds + 1
                    )
                }
            }
        }
    }

    private fun startSimulation(points: List<GeoPoint>) {
        simulationJob?.cancel()
        simulationJob = scope.launch {
            if (points.size < 2) return@launch
            var segmentIdx = 0
            var subStep = 0
            val subStepsTotal = 8 // Steps per segment for smooth motion

            while (isActive && _telemetry.value.isDriving && segmentIdx < points.size - 1) {
                delay(1200) // update interval
                if (_telemetry.value.isPaused) continue

                val pA = points[segmentIdx]
                val pB = points[segmentIdx + 1]
                val fraction = (subStep + 1).toDouble() / subStepsTotal

                val nextLat = pA.latitude + (pB.latitude - pA.latitude) * fraction
                val nextLng = pA.longitude + (pB.longitude - pA.longitude) * fraction
                val newPoint = GeoPoint(nextLat, nextLng)

                val speed = Random.nextDouble(62.0, 96.0)
                processNewLocation(newPoint, speed)

                subStep++
                if (subStep >= subStepsTotal) {
                    subStep = 0
                    segmentIdx++
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startRealGps() {
        try {
            locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            locationListener = object : LocationListener {
                override fun onLocationChanged(loc: Location) {
                    if (_telemetry.value.isDriving && !_telemetry.value.isPaused) {
                        val pt = GeoPoint(loc.latitude, loc.longitude)
                        val speedKmh = if (loc.hasSpeed()) (loc.speed * 3.6).toDouble() else 45.0
                        processNewLocation(pt, speedKmh)
                    }
                }
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            locationListener?.let {
                locationManager?.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    2000L,
                    3f,
                    it,
                    Looper.getMainLooper()
                )
            }
        } catch (_: Exception) {
            // Fallback gracefully
        }
    }

    private fun processNewLocation(newPoint: GeoPoint, speedKmh: Double) {
        val current = _telemetry.value
        val lastPoint = current.routePoints.lastOrNull()
        val distDeltaKm = if (lastPoint != null) GeoUtils.distanceKm(lastPoint, newPoint) else 0.0

        speedSamples.add(speedKmh)
        val avgSpeed = if (speedSamples.isNotEmpty()) speedSamples.average() else speedKmh
        val topSpeed = maxOf(current.topSpeedKmh, speedKmh)

        val updatedPoints = current.routePoints + newPoint

        _telemetry.value = current.copy(
            currentPoint = newPoint,
            currentSpeedKmh = speedKmh,
            averageSpeedKmh = avgSpeed,
            topSpeedKmh = topSpeed,
            distanceKm = current.distanceKm + distDeltaKm,
            routePoints = updatedPoints
        )
    }

    fun registerDiscoveredRoad(roadName: String) {
        val current = _telemetry.value
        if (!current.newlyDiscoveredRoads.contains(roadName)) {
            val updated = current.newlyDiscoveredRoads + roadName
            _telemetry.value = current.copy(
                newlyDiscoveredRoads = updated,
                explorationPercentGained = current.explorationPercentGained + 0.08
            )
        }
    }

    fun togglePause() {
        val current = _telemetry.value
        _telemetry.value = current.copy(isPaused = !current.isPaused)
    }

    fun stopDrive(): LiveDriveTelemetry {
        val finalState = _telemetry.value
        simulationJob?.cancel()
        tickerJob?.cancel()
        locationListener?.let { locationManager?.removeUpdates(it) }

        _telemetry.value = _telemetry.value.copy(
            isDriving = false,
            isPaused = false
        )
        return finalState
    }
}
