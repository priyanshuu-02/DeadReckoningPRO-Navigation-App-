package nisargpatel.deadreckoning.domain.model

import org.osmdroid.util.GeoPoint

data class RouteInfo(
    val sourceName: String = "Current Location",
    val destinationName: String = "Vijayawada City Center",
    val sourcePoint: GeoPoint = GeoPoint(16.5062, 80.6480),
    val destinationPoint: GeoPoint = GeoPoint(16.5180, 80.6650),
    val routePoints: List<GeoPoint> = listOf(
        GeoPoint(16.5062, 80.6480),
        GeoPoint(16.5090, 80.6520),
        GeoPoint(16.5120, 80.6570),
        GeoPoint(16.5150, 80.6610),
        GeoPoint(16.5180, 80.6650)
    ),
    val totalDistanceKm: Double = 3.4,
    val estimatedTimeMinutes: Int = 8,
    val nextManeuver: String = "Turn Right onto MG Road in 250m",
    val distanceToNextManeuverMeters: Int = 250,
    val maneuverIconType: ManeuverIconType = ManeuverIconType.RIGHT
)

enum class ManeuverIconType {
    STRAIGHT,
    LEFT,
    RIGHT,
    SLIGHT_LEFT,
    SLIGHT_RIGHT,
    UTURN,
    ARRIVED
}
