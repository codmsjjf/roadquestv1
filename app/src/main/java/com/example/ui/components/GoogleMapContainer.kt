package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig
import com.example.data.geo.GeoUtils
import com.example.data.model.GeoPoint
import com.example.data.model.RoadSegment
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.MintDiscovery
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

/**
 * Checks whether an API key matches standard Google Maps API key structure (starts with AIza, length >= 30).
 * Prevents invalid/dummy values like "676767" or "DEFAULT_MAPS_API_KEY" from crashing the app.
 */
fun isGoogleMapsApiKeyValid(key: String?): Boolean {
    if (key.isNullOrBlank()) return false
    val trimmed = key.trim()
    if (trimmed == "DEFAULT_MAPS_API_KEY" || trimmed == "676767" || trimmed.length < 25) {
        return false
    }
    return trimmed.startsWith("AIza")
}

@Composable
fun GoogleMapContainer(
    roadSegments: List<RoadSegment>,
    activeRoutePoints: List<GeoPoint> = emptyList(),
    userLocation: GeoPoint? = null,
    focusedStateCenter: GeoPoint? = null,
    onSegmentClick: ((RoadSegment) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Default center: Kuala Lumpur / Klang Valley (3.1390 N, 101.6869 E)
    val defaultCenter = remember { LatLng(3.1390, 101.6869) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultCenter, 10.5f)
    }

    // Inspect the configured API key
    val configuredKey = BuildConfig.MAPS_API_KEY
    val hasValidKey = remember(configuredKey) { isGoogleMapsApiKeyValid(configuredKey) }
    val isSalahKey = remember(configuredKey) {
        configuredKey.isNotBlank() && configuredKey != "DEFAULT_MAPS_API_KEY" && !isGoogleMapsApiKeyValid(configuredKey)
    }

    // Maps SDK availability and graceful initialization tracking
    var isMapsInitSuccess by remember { mutableStateOf<Boolean?>(null) }
    var mapsInitErrorMessage by remember { mutableStateOf<String?>(null) }
    var showNoticeCard by remember { mutableStateOf(true) }

    // If key is invalid ("Salah key") or not configured, default to the Tactical Canvas map
    var showTacticalMap by remember { mutableStateOf(!hasValidKey) }

    // Safely attempt MapsInitializer with try-catch to prevent ANY fatal crash
    LaunchedEffect(Unit) {
        try {
            MapsInitializer.initialize(context)
            isMapsInitSuccess = true
        } catch (t: Throwable) {
            isMapsInitSuccess = false
            mapsInitErrorMessage = t.localizedMessage ?: "Maps initialization failed"
            // Gracefully switch to tactical map so user always has a functional app
            showTacticalMap = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("google_map_container")
    ) {
        // Render either Google Maps SDK or Tactical Vector Canvas
        val canRenderGoogleMap = !showTacticalMap && (isMapsInitSuccess == true)

        if (canRenderGoogleMap) {
            // Google Maps SDK interactive map view
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("google_map_view"),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(
                    mapType = MapType.NORMAL,
                    isMyLocationEnabled = false
                ),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    compassEnabled = true,
                    myLocationButtonEnabled = false,
                    mapToolbarEnabled = false
                )
            ) {
                // Render Unexplored roads (Fog of War) in slate grey
                roadSegments.filter { !it.isExplored }.forEach { segment ->
                    val pts = GeoUtils.parsePoints(segment.pointsJson).map { LatLng(it.latitude, it.longitude) }
                    if (pts.size >= 2) {
                        Polyline(
                            points = pts,
                            color = Color(0xFF64748B),
                            width = 10f,
                            clickable = true,
                            onClick = { onSegmentClick?.invoke(segment) }
                        )
                    }
                }

                // Render Explored roads in vibrant neon cyan
                roadSegments.filter { it.isExplored }.forEach { segment ->
                    val pts = GeoUtils.parsePoints(segment.pointsJson).map { LatLng(it.latitude, it.longitude) }
                    if (pts.size >= 2) {
                        Polyline(
                            points = pts,
                            color = CyanAccent,
                            width = 14f,
                            clickable = true,
                            onClick = { onSegmentClick?.invoke(segment) }
                        )
                    }
                }

                // Render Active Driving Trail in bright orange
                if (activeRoutePoints.size >= 2) {
                    val activeLatLngs = activeRoutePoints.map { LatLng(it.latitude, it.longitude) }
                    Polyline(
                        points = activeLatLngs,
                        color = Color(0xFFFF6D00),
                        width = 16f
                    )
                }

                // User Location Marker
                userLocation?.let { loc ->
                    Marker(
                        state = MarkerState(position = LatLng(loc.latitude, loc.longitude)),
                        title = "Posisi Semasa",
                        snippet = "RoadQuest Explorer"
                    )
                }
            }
        } else {
            // Built-in Tactical Fog of War Canvas (Zero-dependency, offline, smooth 60fps)
            MalaysiaInteractiveMap(
                roadSegments = roadSegments,
                activeRoutePoints = activeRoutePoints,
                userLocation = userLocation,
                focusedStateCenter = focusedStateCenter,
                onSegmentClick = onSegmentClick,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Overlay Banner for "Salah Key" or Setup Notice
        AnimatedVisibility(
            visible = showNoticeCard && (!hasValidKey || isSalahKey),
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 110.dp, start = 14.dp, end = 14.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("api_key_status_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated.copy(alpha = 0.96f)),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isSalahKey) AmberGold else CardBorder
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isSalahKey) Icons.Default.WarningAmber else Icons.Default.Key,
                                contentDescription = null,
                                tint = if (isSalahKey) AmberGold else CyanAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = if (isSalahKey) "Kunci API Tidak Sah (Salah Key)" else "Google Maps Setup",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSalahKey) AmberGold else TextPrimary
                            )
                        }

                        IconButton(
                            onClick = { showNoticeCard = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Tutup",
                                tint = TextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isSalahKey) {
                            "Kunci semasa '$configuredKey' tidak sah untuk Google Maps Android SDK. Peta Taktikal Fog-of-War tempatan sedang digunakan secara automatik tanpa gangguan."
                        } else {
                            "RoadQuest sedia digunakan dengan Peta Taktikal Fog-of-War. Untuk memaparkan jubin satelit Google Maps, tambah MAPS_API_KEY dalam panel Secrets AI Studio."
                        },
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Format sah: Bermula dengan 'AIzaSy...' dari Google Cloud Console",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CyanAccent
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                showTacticalMap = !showTacticalMap
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showTacticalMap) CyanAccent else DarkSurface,
                                contentColor = if (showTacticalMap) DarkBackground else CyanAccent
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (showTacticalMap) "Guna Google Maps" else "Guna Peta Taktikal",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { showNoticeCard = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = DarkSurface,
                                contentColor = TextMuted
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "Faham", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Floating Action Buttons (Switch Mode & Recenter)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 100.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.End
            ) {
                // Switch between Google Maps and Tactical Canvas
                SmallFloatingActionButton(
                    onClick = { showTacticalMap = !showTacticalMap },
                    containerColor = DarkSurfaceElevated,
                    contentColor = if (showTacticalMap) MintDiscovery else CyanAccent,
                    modifier = Modifier.testTag("toggle_map_mode_button")
                ) {
                    Icon(
                        Icons.Default.Layers,
                        contentDescription = "Tukar Mod Peta (Taktikal / Google Maps)"
                    )
                }

                // Recenter Camera to Malaysia Center
                SmallFloatingActionButton(
                    onClick = {
                        val target = userLocation?.let { LatLng(it.latitude, it.longitude) } ?: defaultCenter
                        coroutineScope.launch {
                            if (canRenderGoogleMap) {
                                cameraPositionState.animate(
                                    CameraUpdateFactory.newLatLngZoom(target, 11f),
                                    800
                                )
                            }
                        }
                    },
                    containerColor = DarkSurfaceElevated,
                    contentColor = CyanAccent,
                    modifier = Modifier.testTag("recenter_google_map_button")
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Pusatkan Lokasi")
                }
            }
        }
    }
}
