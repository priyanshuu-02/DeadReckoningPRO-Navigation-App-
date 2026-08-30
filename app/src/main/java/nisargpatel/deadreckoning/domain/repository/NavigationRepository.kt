package nisargpatel.deadreckoning.domain.repository

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import nisargpatel.deadreckoning.domain.state.*

interface NavigationRepository {
    val navigationState: StateFlow<NavigationState>
    val sensorState: StateFlow<SensorState>
    val gnssState: StateFlow<GNSSState>
    val aiState: StateFlow<AIState>
    val mapState: StateFlow<MapState>
    val mapMatchingState: StateFlow<MapMatchingState>
    val analyticsState: StateFlow<AnalyticsState>
    val sessionState: StateFlow<SessionState>
    val navigationEvents: SharedFlow<NavigationEvent>

    fun startNavigation()
    fun stopNavigation()

    // Simulation & Demo Controls
    fun simulateModeGNSSActive()
    fun simulateOutage()
    fun simulatePothole()
    fun simulateRecovery()
    fun simulateOffline()
    fun simulateError()
    fun resetDemo()
    fun startAutoPlay()
    fun stopAutoPlay()
}
