package nisargpatel.deadreckoning.util

import kotlin.math.cos
import org.osmdroid.util.GeoPoint

data class RouteMatch(val point: GeoPoint, val distanceMeters: Double, val confidence: Int)

/** Projects a dead-reckoned point onto the active, locally retained route geometry. */
object RouteMapMatcher {
    fun match(point: GeoPoint, route: List<GeoPoint>): RouteMatch? {
        if (route.size < 2) return null
        var nearest: GeoPoint? = null
        var nearestDistance = Double.MAX_VALUE
        route.zipWithNext().forEach { (start, end) ->
            val candidate = project(point, start, end)
            val distance = point.distanceToAsDouble(candidate)
            if (distance < nearestDistance) {
                nearestDistance = distance
                nearest = candidate
            }
        }
        val matched = nearest ?: return null
        return RouteMatch(
            point = matched,
            distanceMeters = nearestDistance,
            confidence = (100.0 - nearestDistance * 4.0).toInt().coerceIn(0, 100)
        )
    }

    private fun project(point: GeoPoint, start: GeoPoint, end: GeoPoint): GeoPoint {
        val latitudeScale = 111_111.0
        val longitudeScale = latitudeScale * cos(Math.toRadians(point.latitude))
        val bx = (end.longitude - start.longitude) * longitudeScale
        val by = (end.latitude - start.latitude) * latitudeScale
        val px = (point.longitude - start.longitude) * longitudeScale
        val py = (point.latitude - start.latitude) * latitudeScale
        val lengthSquared = bx * bx + by * by
        if (lengthSquared == 0.0) return start
        val fraction = ((px * bx + py * by) / lengthSquared).coerceIn(0.0, 1.0)
        return GeoPoint(start.latitude + by * fraction / latitudeScale, start.longitude + bx * fraction / longitudeScale)
    }
}
