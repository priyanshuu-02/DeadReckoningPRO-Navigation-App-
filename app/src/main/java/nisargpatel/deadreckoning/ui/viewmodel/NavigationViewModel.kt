package nisargpatel.deadreckoning.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nisargpatel.deadreckoning.domain.model.RouteInfo
import nisargpatel.deadreckoning.domain.repository.NavigationRepository
import nisargpatel.deadreckoning.domain.state.*
import nisargpatel.deadreckoning.util.OSRMRouteFetcher
import org.osmdroid.util.GeoPoint

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

    private val _selectedRoute = MutableStateFlow(RouteInfo())
    val selectedRoute: StateFlow<RouteInfo> = _selectedRoute.asStateFlow()

    init {
        // Fetch initial street route to default city center
        selectDestination("Vijayawada City Center", GeoPoint(16.5062, 80.6480))
    }

    fun startNavigation() = repository.startNavigation()
    fun stopNavigation() = repository.stopNavigation()

    fun selectDestination(name: String, destinationPoint: GeoPoint) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentNav = navigationState.value
            val sourcePoint = if (currentNav.latitude != 0.0 && currentNav.longitude != 0.0) {
                GeoPoint(currentNav.latitude, currentNav.longitude)
            } else {
                GeoPoint(16.5215, 80.5216)
            }

            val route = OSRMRouteFetcher.fetchRoute(sourcePoint, destinationPoint, name)
            _selectedRoute.value = route
        }
    }

    // Simulation / Engine Actions
    fun simulateGNSSActive() = repository.simulateModeGNSSActive()
    fun simulateOutage() = repository.simulateOutage()
    fun simulatePothole() = repository.simulatePothole()
    fun simulateRecovery() = repository.simulateRecovery()
    fun simulateOffline() = repository.simulateOffline()
    fun simulateError() = repository.simulateError()
    fun resetDemo() = repository.resetDemo()
    fun startAutoPlay() = repository.startAutoPlay()
    fun stopAutoPlay() = repository.stopAutoPlay()
}
