package nisargpatel.deadreckoning.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import nisargpatel.deadreckoning.domain.repository.NavigationRepository
import nisargpatel.deadreckoning.domain.state.*

class NavigationViewModel(
    private val repository: NavigationRepository
) : ViewModel() {

    val navigationState: StateFlow<NavigationState> = repository.navigationState
    val gnssState: StateFlow<GNSSState> = repository.gnssState
    val aiState: StateFlow<AIState> = repository.aiState
    val sensorState: StateFlow<SensorState> = repository.sensorState
    val mapState: StateFlow<MapState> = repository.mapState
    val mapMatchingState: StateFlow<MapMatchingState> = repository.mapMatchingState
    val events: SharedFlow<NavigationEvent> = repository.navigationEvents

    fun startNavigation() = repository.startNavigation()
    fun stopNavigation() = repository.stopNavigation()

    // Simulation / Demo Actions
    fun simulateGNSSActive() = repository.simulateModeGNSSActive()
    fun simulateOutage() = repository.simulateOutage()
    fun simulateRecovery() = repository.simulateRecovery()
    fun simulateOffline() = repository.simulateOffline()
    fun simulateError() = repository.simulateError()
    fun resetDemo() = repository.resetDemo()
    fun startAutoPlay() = repository.startAutoPlay()
    fun stopAutoPlay() = repository.stopAutoPlay()
}
