package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.geo.GeoUtils
import com.example.data.model.GeoPoint
import com.example.data.model.RoadSegment
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricOrange
import com.example.ui.theme.MintDiscovery
import com.example.ui.theme.RoadExplored
import com.example.ui.theme.RoadUnexplored
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MalaysiaInteractiveMap(
    roadSegments: List<RoadSegment>,
    activeRoutePoints: List<GeoPoint> = emptyList(),
    userLocation: GeoPoint? = null,
    focusedStateCenter: GeoPoint? = null,
    onSegmentClick: ((RoadSegment) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // Malaysia geographic bounds:
    // Peninsular: Lat 1.2 to 6.8 N, Lng 99.6 to 104.5 E
    // Default center at Klang Valley (Lat 3.14, Lng 101.69)
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        zoomScale = (zoomScale * zoomChange).coerceIn(0.5f, 6.0f)
        panOffsetX += panChange.x
        panOffsetY += panChange.y
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseGlow"
    )

    // Key major city coordinates to draw as subtle tactical hubs
    val cityHubs = remember {
        listOf(
            "Kuala Lumpur" to GeoPoint(3.139, 101.686),
            "Petaling Jaya" to GeoPoint(3.107, 101.606),
            "Georgetown" to GeoPoint(5.414, 100.328),
            "Ipoh" to GeoPoint(4.592, 101.090),
            "Johor Bahru" to GeoPoint(1.485, 103.761),
            "Melaka" to GeoPoint(2.189, 102.250),
            "Kuantan" to GeoPoint(3.812, 103.325),
            "Seremban" to GeoPoint(2.725, 101.942),
            "Alor Setar" to GeoPoint(6.124, 100.367),
            "Kota Bharu" to GeoPoint(6.125, 102.238),
            "K. Terengganu" to GeoPoint(5.311, 103.132),
            "Kuching" to GeoPoint(1.553, 110.359),
            "Kota Kinabalu" to GeoPoint(5.980, 116.073)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .testTag("interactive_map_container")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = transformState)
                .pointerInput(roadSegments) {
                    detectTapGestures { tapOffset ->
                        val w = size.width.toFloat()
                        val h = size.height.toFloat()

                        // Check which segment is closest to tap
                        for (segment in roadSegments) {
                            val pts = GeoUtils.parsePoints(segment.pointsJson)
                            for (i in 0 until pts.size - 1) {
                                val p1 = projectGeoToCanvas(pts[i], w, h, zoomScale, panOffsetX, panOffsetY)
                                val p2 = projectGeoToCanvas(pts[i + 1], w, h, zoomScale, panOffsetX, panOffsetY)
                                val dist = distanceToLineSegment(tapOffset, p1, p2)
                                if (dist < 40f) {
                                    onSegmentClick?.invoke(segment)
                                    return@detectTapGestures
                                }
                            }
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // 1. Draw Tactical Grid Background
            val gridSpacing = 60f * zoomScale
            val startX = (panOffsetX % gridSpacing) - gridSpacing
            val startY = (panOffsetY % gridSpacing) - gridSpacing

            var curX = startX
            while (curX < width + gridSpacing) {
                drawLine(
                    color = Color(0xFF141C2A),
                    start = Offset(curX, 0f),
                    end = Offset(curX, height),
                    strokeWidth = 1f
                )
                curX += gridSpacing
            }

            var curY = startY
            while (curY < height + gridSpacing) {
                drawLine(
                    color = Color(0xFF141C2A),
                    start = Offset(0f, curY),
                    end = Offset(width, curY),
                    strokeWidth = 1f
                )
                curY += gridSpacing
            }

            // 2. Draw Malaysia Continental Outline / Coastline guides
            drawMalaysiaCoastline(width, height, zoomScale, panOffsetX, panOffsetY)

            // 3. Draw Unexplored Road Segments (Fog of War)
            // Layer 1: Dark Slate Grey casing
            for (segment in roadSegments) {
                if (!segment.isExplored) {
                    val pts = GeoUtils.parsePoints(segment.pointsJson)
                    if (pts.size >= 2) {
                        val path = Path()
                        val p0 = projectGeoToCanvas(pts[0], width, height, zoomScale, panOffsetX, panOffsetY)
                        path.moveTo(p0.x, p0.y)
                        for (i in 1 until pts.size) {
                            val pi = projectGeoToCanvas(pts[i], width, height, zoomScale, panOffsetX, panOffsetY)
                            path.lineTo(pi.x, pi.y)
                        }

                        // Outer dimmed casing
                        drawPath(
                            path = path,
                            color = RoadUnexplored.copy(alpha = 0.7f),
                            style = Stroke(
                                width = 3.5f * zoomScale.coerceIn(0.8f, 2.5f),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }

            // 4. Draw Explored Roads (Radiant Neon Cyan with bloom & glow)
            for (segment in roadSegments) {
                if (segment.isExplored) {
                    val pts = GeoUtils.parsePoints(segment.pointsJson)
                    if (pts.size >= 2) {
                        val path = Path()
                        val p0 = projectGeoToCanvas(pts[0], width, height, zoomScale, panOffsetX, panOffsetY)
                        path.moveTo(p0.x, p0.y)
                        for (i in 1 until pts.size) {
                            val pi = projectGeoToCanvas(pts[i], width, height, zoomScale, panOffsetX, panOffsetY)
                            path.lineTo(pi.x, pi.y)
                        }

                        // Outer bloom glow
                        drawPath(
                            path = path,
                            color = CyanAccent.copy(alpha = 0.25f * pulseAnim),
                            style = Stroke(
                                width = 12f * zoomScale.coerceIn(0.8f, 2.5f),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // Mid glow
                        drawPath(
                            path = path,
                            color = CyanAccent.copy(alpha = 0.6f),
                            style = Stroke(
                                width = 6f * zoomScale.coerceIn(0.8f, 2.5f),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )

                        // Core bright road beam
                        drawPath(
                            path = path,
                            color = Color(0xFFE0F7FA),
                            style = Stroke(
                                width = 2.5f * zoomScale.coerceIn(0.8f, 2.5f),
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }
            }

            // 5. Draw Active Route Points (Live Drive Path)
            if (activeRoutePoints.size >= 2) {
                val routePath = Path()
                val startP = projectGeoToCanvas(activeRoutePoints[0], width, height, zoomScale, panOffsetX, panOffsetY)
                routePath.moveTo(startP.x, startP.y)
                for (i in 1 until activeRoutePoints.size) {
                    val pi = projectGeoToCanvas(activeRoutePoints[i], width, height, zoomScale, panOffsetX, panOffsetY)
                    routePath.lineTo(pi.x, pi.y)
                }

                // Vibrant Orange Glow
                drawPath(
                    path = routePath,
                    color = ElectricOrange.copy(alpha = 0.4f),
                    style = Stroke(
                        width = 14f * zoomScale.coerceIn(0.8f, 2.5f),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                drawPath(
                    path = routePath,
                    color = ElectricOrange,
                    style = Stroke(
                        width = 5.5f * zoomScale.coerceIn(0.8f, 2.5f),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }

            // 6. Draw City Hubs & Labels
            for ((cityName, pt) in cityHubs) {
                val canvasPt = projectGeoToCanvas(pt, width, height, zoomScale, panOffsetX, panOffsetY)
                if (canvasPt.x in -50f..(width + 50f) && canvasPt.y in -50f..(height + 50f)) {
                    // Center dot
                    drawCircle(
                        color = Color(0xFF64748B),
                        radius = 3.5f * zoomScale.coerceIn(0.8f, 1.8f),
                        center = canvasPt
                    )
                    drawCircle(
                        color = Color(0xFF0F172A),
                        radius = 1.5f * zoomScale.coerceIn(0.8f, 1.8f),
                        center = canvasPt
                    )

                    // Draw text label on canvas
                    drawIntoCanvas { canvas ->
                        val paint = android.graphics.Paint().apply {
                            color = android.graphics.Color.argb(160, 180, 200, 225)
                            textSize = 24f * zoomScale.coerceIn(0.8f, 1.4f)
                            isAntiAlias = true
                            typeface = android.graphics.Typeface.create(
                                android.graphics.Typeface.SANS_SERIF,
                                android.graphics.Typeface.BOLD
                            )
                        }
                        canvas.nativeCanvas.drawText(
                            cityName,
                            canvasPt.x + 8f,
                            canvasPt.y - 6f,
                            paint
                        )
                    }
                }
            }

            // 7. Draw User / Vehicle Position
            userLocation?.let { loc ->
                val vehiclePos = projectGeoToCanvas(loc, width, height, zoomScale, panOffsetX, panOffsetY)

                // Outer sonar pulse
                drawCircle(
                    color = MintDiscovery.copy(alpha = 0.25f * (1f - pulseAnim)),
                    radius = 36f * pulseAnim * zoomScale.coerceIn(0.8f, 2.0f),
                    center = vehiclePos
                )

                // Vehicle aura
                drawCircle(
                    color = MintDiscovery.copy(alpha = 0.4f),
                    radius = 16f * zoomScale.coerceIn(0.8f, 2.0f),
                    center = vehiclePos
                )

                // Vehicle inner crest
                drawCircle(
                    color = Color.White,
                    radius = 7f * zoomScale.coerceIn(0.8f, 2.0f),
                    center = vehiclePos
                )
            }
        }

        // Map Control Buttons (Zoom in, Zoom out, Center location)
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 100.dp)
        ) {
            androidx.compose.foundation.layout.Column(
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { zoomScale = (zoomScale * 1.3f).coerceAtMost(6.0f) },
                    containerColor = DarkSurface,
                    contentColor = CyanAccent,
                    modifier = Modifier.testTag("zoom_in_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Zoom In")
                }

                SmallFloatingActionButton(
                    onClick = { zoomScale = (zoomScale / 1.3f).coerceAtLeast(0.5f) },
                    containerColor = DarkSurface,
                    contentColor = CyanAccent,
                    modifier = Modifier.testTag("zoom_out_button")
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Zoom Out")
                }

                SmallFloatingActionButton(
                    onClick = {
                        zoomScale = 1.0f
                        panOffsetX = 0f
                        panOffsetY = 0f
                    },
                    containerColor = DarkSurface,
                    contentColor = MintDiscovery,
                    modifier = Modifier.testTag("recenter_button")
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Center Location")
                }
            }
        }
    }
}

private fun projectGeoToCanvas(
    geo: GeoPoint,
    canvasWidth: Float,
    canvasHeight: Float,
    zoomScale: Float,
    panX: Float,
    panY: Float
): Offset {
    // Projection tuned for Malaysia:
    // Peninsular Malaysia spans roughly Lat 1.2 to 6.8 N, Lng 99.8 to 104.5 E
    // Center reference: Lat 3.5, Lng 102.0
    val centerLat = 3.5
    val centerLng = 102.0

    // Mercator-like local planar scaling:
    val scaleFactor = (canvasWidth.coerceAtLeast(400f) / 5.2f) * zoomScale

    val x = canvasWidth / 2f + ((geo.longitude - centerLng) * scaleFactor).toFloat() + panX
    // Latitude increases upwards, so inverted Y
    val y = canvasHeight / 2f - ((geo.latitude - centerLat) * scaleFactor * 1.05f).toFloat() + panY

    return Offset(x, y)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMalaysiaCoastline(
    width: Float,
    height: Float,
    zoomScale: Float,
    panX: Float,
    panY: Float
) {
    // Stylized peninsular coastlines & borders for tactical map immersion
    val peninsularCoastline = listOf(
        GeoPoint(6.45, 100.15), // Perlis
        GeoPoint(6.15, 100.35), // Kedah
        GeoPoint(5.42, 100.30), // Penang
        GeoPoint(4.85, 100.70), // Perak Taiping
        GeoPoint(4.20, 100.60), // Lumut
        GeoPoint(3.05, 101.40), // Klang / Selangor coast
        GeoPoint(2.50, 101.80), // Port Dickson
        GeoPoint(2.20, 102.25), // Melaka
        GeoPoint(1.35, 103.50), // Tanjung Piai (Southernmost)
        GeoPoint(1.48, 103.85), // Johor Bahru / Pasir Gudang
        GeoPoint(2.00, 104.05), // Mersing
        GeoPoint(3.80, 103.35), // Kuantan
        GeoPoint(4.75, 103.45), // Dungun
        GeoPoint(5.35, 103.15), // Terengganu
        GeoPoint(6.20, 102.25), // Tumpat / Kota Bharu
        GeoPoint(5.80, 101.80), // Kelantan border
        GeoPoint(5.75, 101.00), // Perak inland border
        GeoPoint(6.45, 100.15)  // Back to Perlis
    )

    val path = Path()
    val p0 = projectGeoToCanvas(peninsularCoastline[0], width, height, zoomScale, panX, panY)
    path.moveTo(p0.x, p0.y)
    for (i in 1 until peninsularCoastline.size) {
        val p = projectGeoToCanvas(peninsularCoastline[i], width, height, zoomScale, panX, panY)
        path.lineTo(p.x, p.y)
    }

    // Draw subtle glowing territory fill and boundary line
    drawPath(
        path = path,
        color = Color(0xFF0F1522),
        style = androidx.compose.ui.graphics.drawscope.Fill
    )

    drawPath(
        path = path,
        color = Color(0xFF1E293B),
        style = Stroke(
            width = 1.5f * zoomScale.coerceIn(0.8f, 2.0f),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
        )
    )
}

private fun distanceToLineSegment(p: Offset, a: Offset, b: Offset): Float {
    val dx = b.x - a.x
    val dy = b.y - a.y
    if (dx == 0f && dy == 0f) {
        val px = p.x - a.x
        val py = p.y - a.y
        return kotlin.math.sqrt(px * px + py * py)
    }
    val t = ((p.x - a.x) * dx + (p.y - a.y) * dy) / (dx * dx + dy * dy)
    val clampedT = t.coerceIn(0f, 1f)
    val nearestX = a.x + clampedT * dx
    val nearestY = a.y + clampedT * dy
    val distSquare = (p.x - nearestX) * (p.x - nearestX) + (p.y - nearestY) * (p.y - nearestY)
    return kotlin.math.sqrt(distSquare)
}
