package nisargpatel.deadreckoning.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import nisargpatel.deadreckoning.adapter.LocationAdapter
import nisargpatel.deadreckoning.adapter.PotholeDetector
import nisargpatel.deadreckoning.adapter.SensorAdapter
import nisargpatel.deadreckoning.domain.model.NavigationMode
import nisargpatel.deadreckoning.domain.repository.NavigationRepository
import nisargpatel.deadreckoning.domain.state.AIState
import nisargpatel.deadreckoning.domain.state.AnalyticsState
import nisargpatel.deadreckoning.domain.state.GNSSState
import nisargpatel.deadreckoning.domain.state.MapMatchingState
import nisargpatel.deadreckoning.domain.state.MapState
import nisargpatel.deadreckoning.domain.state.NavigationEvent
import nisargpatel.deadreckoning.domain.state.NavigationState
import nisargpatel.deadreckoning.domain.state.SensorState
import nisargpatel.deadreckoning.domain.state.SessionState
import nisargpatel.deadreckoning.ml.V8DeadReckoningEngine
import nisargpatel.deadreckoning.ml.V8Prediction
import org.osmdroid.util.GeoPoint
import kotlin.math.cos
import kotlin.math.sin

/** The live data path: Android sensors + fused location + V8 on-device inference. */
class LiveNavigationRepository(
    context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) : NavigationRepository {
    private val sensorAdapter = SensorAdapter(context)
    private val locationAdapter = LocationAdapter(context)
    private val potholeDetector = PotholeDetector()
    private val model = runCatching { V8DeadReckoningEngine(context) }.getOrNull()

    private val _navigationState = MutableStateFlow(NavigationState())
    override val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()
    private val _sensorState = MutableStateFlow(SensorState())
    override val sensorState: StateFlow<SensorState> = _sensorState.asStateFlow()
    private val _gnssState = MutableStateFlow(GNSSState())
    override val gnssState: StateFlow<GNSSState> = _gnssState.asStateFlow()
    private val _aiState = MutableStateFlow(AIState(isModelLoaded = model != null, modelVersion = if (model == null) "Unavailable" else "V8 heading-delta"))
    override val aiState: StateFlow<AIState> = _aiState.asStateFlow()
    private val _mapState = MutableStateFlow(MapState())
    override val mapState: StateFlow<MapState> = _mapState.asStateFlow()
    private val _mapMatchingState = MutableStateFlow(MapMatchingState())
    override val mapMatchingState: StateFlow<MapMatchingState> = _mapMatchingState.asStateFlow()
    private val _analyticsState = MutableStateFlow(AnalyticsState())
    override val analyticsState: StateFlow<AnalyticsState> = _analyticsState.asStateFlow()
    private val _sessionState = MutableStateFlow(SessionState())
    override val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>(extraBufferCapacity = 1)
    override val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

    private var lastGnssUpdateMs = 0L
    private var lastPotholeAtMs = 0L

    init {
        sensorAdapter.startListening()
        scope.launch {
            sensorAdapter.sensorState.collect { state ->
                _sensorState.value = state
                processPothole(state)
                if (_navigationState.value.isNavigating) processNavigationSensor(state)
            }
        }
        scope.launch {
            locationAdapter.gnssState.collect { state ->
                _gnssState.value = state
                if (state.isAvailable) {
                    lastGnssUpdateMs = System.currentTimeMillis()
                    applyGnss(state)
                }
            }
        }
    }

    override fun startNavigation() {
        locationAdapter.startLocationUpdates()
        _navigationState.value = _navigationState.value.copy(isNavigating = true)
        _navigationEvents.tryEmit(NavigationEvent.NavigationStarted)
    }

    override fun stopNavigation() {
        locationAdapter.stopLocationUpdates()
        _navigationState.value = _navigationState.value.copy(isNavigating = false)
        _navigationEvents.tryEmit(NavigationEvent.NavigationStopped)
    }

    private fun applyGnss(state: GNSSState) {
        if (!_navigationState.value.isNavigating) return
        val position = GeoPoint(state.latitude, state.longitude)
        _navigationState.value = _navigationState.value.copy(
            mode = NavigationMode.GNSS_INS,
            speedKmh = state.speedKmh.takeIf { it >= 2.0 } ?: 0.0,
            headingDegrees = state.bearingDegrees,
            accuracyMeters = state.accuracyMeters,
            latitude = state.latitude,
            longitude = state.longitude,
            confidencePercentage = state.signalQualityPercentage,
            outageDurationSeconds = 0
        )
        _mapState.value = _mapState.value.copy(
            currentPosition = position,
            routePoints = (_mapState.value.routePoints + position).takeLast(200),
            gnssTrajectory = (_mapState.value.gnssTrajectory + position).takeLast(200)
        )
    }

    private fun processPothole(state: SensorState) {
        val pothole = potholeDetector.update(state.accelX, state.accelY, state.accelZ, state.gyroX, state.gyroY, state.gyroZ)
        if (pothole.detected && System.currentTimeMillis() - lastPotholeAtMs > 2500L) {
            lastPotholeAtMs = System.currentTimeMillis()
            val alert = "${pothole.severity} pothole (${pothole.confidence}% confidence)"
            _aiState.value = _aiState.value.copy(anomalyDetected = alert)
            Log.i("LiveNavigation", "Pothole event emitted: $alert")
            _navigationEvents.tryEmit(NavigationEvent.PotholeDetected(alert))
        }
    }

    private fun processNavigationSensor(state: SensorState) {
        val seedSpeed = if (hasFreshGnss()) _gnssState.value.speedKmh / 3.6 else _aiState.value.predictedSpeedKmh / 3.6
        model?.addSample(
            System.nanoTime(), state.accelX, state.accelY, state.accelZ,
            state.gyroX, state.gyroY, state.gyroZ, seedSpeed.toFloat()
        )?.let(::applyPrediction)
    }

    private fun applyPrediction(prediction: V8Prediction) {
        _aiState.value = _aiState.value.copy(
            isActive = !hasFreshGnss(),
            predictedSpeedKmh = prediction.speedMps * 3.6,
            speedConfidencePercentage = prediction.confidencePercentage,
            motionClassification = prediction.motionClass.label,
            motionConfidencePercentage = prediction.confidencePercentage,
            inferenceTimeMs = prediction.inferenceTimeMs
        )
        if (hasFreshGnss()) return

        val previous = _navigationState.value
        val heading = previous.headingDegrees + Math.toDegrees(prediction.headingDeltaRadians.toDouble())
        val point = advance(previous.latitude, previous.longitude, heading, prediction.forwardMeters, prediction.lateralMeters)
        val nextOutage = previous.outageDurationSeconds + 2
        _navigationState.value = previous.copy(
            mode = NavigationMode.AI_DEAD_RECKONING,
            speedKmh = prediction.speedMps * 3.6,
            headingDegrees = heading,
            latitude = point.latitude,
            longitude = point.longitude,
            accuracyMeters = previous.accuracyMeters + 1.5,
            confidencePercentage = prediction.confidencePercentage,
            outageDurationSeconds = nextOutage,
            totalDistanceKm = previous.totalDistanceKm + prediction.forwardMeters.coerceAtLeast(0f) / 1000.0
        )
        _mapState.value = _mapState.value.copy(
            currentPosition = point,
            rawDRPosition = point,
            routePoints = (_mapState.value.routePoints + point).takeLast(200),
            drTrajectory = (_mapState.value.drTrajectory + point).takeLast(200)
        )
    }

    private fun hasFreshGnss() = _gnssState.value.isAvailable && System.currentTimeMillis() - lastGnssUpdateMs < 4_000L

    private fun advance(latitude: Double, longitude: Double, headingDegrees: Double, forwardMeters: Float, lateralMeters: Float): GeoPoint {
        if (latitude == 0.0 && longitude == 0.0) return GeoPoint(0.0, 0.0)
        val heading = Math.toRadians(headingDegrees)
        val north = forwardMeters * cos(heading).toFloat() - lateralMeters * sin(heading).toFloat()
        val east = forwardMeters * sin(heading).toFloat() + lateralMeters * cos(heading).toFloat()
        val latitudeDelta = north / 111_111.0
        val longitudeDelta = east / (111_111.0 * cos(Math.toRadians(latitude)))
        return GeoPoint(latitude + latitudeDelta, longitude + longitudeDelta)
    }

    // Kept for the older debug surface; production navigation never manufactures telemetry.
    override fun simulateModeGNSSActive() = Unit
    override fun simulateOutage() = Unit
    override fun simulatePothole() = Unit
    override fun simulateRecovery() = Unit
    override fun simulateOffline() = Unit
    override fun simulateError() = Unit
    override fun resetDemo() = Unit
    override fun startAutoPlay() = Unit
    override fun stopAutoPlay() = Unit
}
