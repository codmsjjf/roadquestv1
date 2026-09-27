package com.example.data.geo

import com.example.data.model.GeoPoint
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GeoUtils {
    private const val EARTH_RADIUS_METERS = 6371000.0

    fun distanceMeters(p1: GeoPoint, p2: GeoPoint): Double {
        val lat1Rad = Math.toRadians(p1.latitude)
        val lat2Rad = Math.toRadians(p2.latitude)
        val deltaLat = Math.toRadians(p2.latitude - p1.latitude)
        val deltaLng = Math.toRadians(p2.longitude - p1.longitude)

        val a = sin(deltaLat / 2) * sin(deltaLat / 2) +
                cos(lat1Rad) * cos(lat2Rad) *
                sin(deltaLng / 2) * sin(deltaLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    fun distanceKm(p1: GeoPoint, p2: GeoPoint): Double {
        return distanceMeters(p1, p2) / 1000.0
    }

    /**
     * Calculates distance in meters from point P to line segment AB.
     */
    fun distanceToSegmentMeters(p: GeoPoint, a: GeoPoint, b: GeoPoint): Double {
        val dx = b.longitude - a.longitude
        val dy = b.latitude - a.latitude

        if (dx == 0.0 && dy == 0.0) {
            return distanceMeters(p, a)
        }

        // Project point onto line segment
        val t = ((p.longitude - a.longitude) * dx + (p.latitude - a.latitude) * dy) / (dx * dx + dy * dy)
        val clampedT = t.coerceIn(0.0, 1.0)

        val nearestPoint = GeoPoint(
            latitude = a.latitude + clampedT * dy,
            longitude = a.longitude + clampedT * dx
        )

        return distanceMeters(p, nearestPoint)
    }

    /**
     * Checks if point is within threshold distance (meters) of any segment in polyline.
     */
    fun isPointNearPolyline(point: GeoPoint, polyline: List<GeoPoint>, thresholdMeters: Double = 120.0): Boolean {
        if (polyline.size < 2) {
            return polyline.any { distanceMeters(point, it) <= thresholdMeters }
        }
        for (i in 0 until polyline.size - 1) {
            val d = distanceToSegmentMeters(point, polyline[i], polyline[i + 1])
            if (d <= thresholdMeters) return true
        }
        return false
    }

    fun parsePoints(pointsJson: String): List<GeoPoint> {
        if (pointsJson.isBlank()) return emptyList()
        return pointsJson.split(";").mapNotNull { pair ->
            val parts = pair.split(",")
            if (parts.size == 2) {
                val lat = parts[0].toDoubleOrNull()
                val lng = parts[1].toDoubleOrNull()
                if (lat != null && lng != null) GeoPoint(lat, lng) else null
            } else null
        }
    }

    fun pointsToString(points: List<GeoPoint>): String {
        return points.joinToString(";") { "${it.latitude},${it.longitude}" }
    }
}
