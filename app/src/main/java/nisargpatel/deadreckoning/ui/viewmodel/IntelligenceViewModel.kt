package nisargpatel.deadreckoning.ui.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.StateFlow
import nisargpatel.deadreckoning.domain.repository.NavigationRepository
import nisargpatel.deadreckoning.domain.state.AIState
import nisargpatel.deadreckoning.domain.state.NavigationState

class IntelligenceViewModel(
    private val repository: NavigationRepository
) : ViewModel() {

    val aiState: StateFlow<AIState> = repository.aiState
    val navigationState: StateFlow<NavigationState> = repository.navigationState

    fun simulatePothole() = repository.simulatePothole()
}
