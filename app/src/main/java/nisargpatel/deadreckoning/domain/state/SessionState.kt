package nisargpatel.deadreckoning.domain.state

/** A persistence-safe geographic point used by archived trip trajectories. */
data class TrajectoryPoint(
    val latitude: Double,
    val longitude: Double
)

data class NavigationSession(
    val id: String,
    val dateString: String,
    val durationString: String,
    val distanceKm: Double,
    val outageCount: Int,
    val drDurationSeconds: Long,
    val maxErrorMeters: Double,
    val avgErrorMeters: Double,
    val status: String,
    /** Planned navigation route shown in blue. */
    val plannedRoute: List<TrajectoryPoint> = emptyList(),
    /** Positions received while GNSS was available, shown in green. */
    val gnssPath: List<TrajectoryPoint> = emptyList(),
    /** Continuous inertial positions during GNSS outages, shown in red. */
    val deadReckoningPath: List<TrajectoryPoint> = emptyList()
)

data class SessionState(
    val sessions: List<NavigationSession> = emptyList(),
    val selectedSession: NavigationSession? = null,
    val isLoading: Boolean = false
)
