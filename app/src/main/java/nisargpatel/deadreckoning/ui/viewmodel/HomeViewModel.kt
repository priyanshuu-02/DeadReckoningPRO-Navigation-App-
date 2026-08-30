package nisargpatel.deadreckoning.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import nisargpatel.deadreckoning.domain.repository.NavigationRepository
import nisargpatel.deadreckoning.domain.state.NavigationState
import nisargpatel.deadreckoning.domain.state.SessionState

class HomeViewModel(
    private val repository: NavigationRepository
) : ViewModel() {

    val navigationState: StateFlow<NavigationState> = repository.navigationState
    val sessionState: StateFlow<SessionState> = repository.sessionState

    fun startNavigation() {
        repository.startNavigation()
    }

    fun stopNavigation() {
        repository.stopNavigation()
    }
}
