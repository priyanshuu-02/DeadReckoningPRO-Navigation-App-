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
import nisargpatel.deadreckoning.domain.model.RouteInfo
import nisargpatel.deadreckoning.domain.repository.NavigationRepository
import nisargpatel.deadreckoning.domain.state.AIState
import nisargpatel.deadreckoning.domain.state.AnalyticsState
import nisargpatel.deadreckoning.domain.state.CandidateRoad
import nisargpatel.deadreckoning.domain.state.GNSSState
import nisargpatel.deadreckoning.domain.state.MapMatchingState
import nisargpatel.deadreckoning.domain.state.MapState
import nisargpatel.deadreckoning.domain.state.NavigationEvent
import nisargpatel.deadreckoning.domain.state.NavigationState
import nisargpatel.deadreckoning.domain.state.SensorState
import nisargpatel.deadreckoning.domain.state.NavigationSession
import nisargpatel.deadreckoning.domain.state.SessionState
import nisargpatel.deadreckoning.ml.V8DeadReckoningEngine
import nisargpatel.deadreckoning.ml.V8Prediction
import nisargpatel.deadreckoning.fusion.VehicleAlignmentCalibrator
import nisargpatel.deadreckoning.fusion.VehicleFusionEkf
import nisargpatel.deadreckoning.matching.HiddenMarkovRoadMatcher
import nisargpatel.deadreckoning.util.RouteMapMatcher
import nisargpatel.deadreckoning.util.RouteMatch
import org.osmdroid.util.GeoPoint
import java.text.DateFormat
import java.util.Date
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
    private val fusion = VehicleFusionEkf()
    private val alignmentCalibrator = VehicleAlignmentCalibrator()
    private val historyStore = NavigationHistoryStore(context)
    private val calibrationStore = CalibrationStore(context)
    private val offlineRoadNetwork = OfflineRoadNetwork.get(context)
    private val roadMatcher = HiddenMarkovRoadMatcher()

    private val _navigationState = MutableStateFlow(NavigationState())
    override val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()
    private val _sensorState = MutableStateFlow(SensorState())
    override val sensorState: StateFlow<SensorState> = _sensorState.asStateFlow()
    private val _gnssState = MutableStateFlow(GNSSState())
    override val gnssState: StateFlow<GNSSState> = _gnssState.asStateFlow()
    private val _aiState = MutableStateFlow(AIState(isModelLoaded = model != null, modelVersion = model?.manifest?.deployment_status ?: "Unavailable"))
    override val aiState: StateFlow<AIState> = _aiState.asStateFlow()
    private val _mapState = MutableStateFlow(MapState())
    override val mapState: StateFlow<MapState> = _mapState.asStateFlow()
    private val _mapMatchingState = MutableStateFlow(MapMatchingState())
    override val mapMatchingState: StateFlow<MapMatchingState> = _mapMatchingState.asStateFlow()
    private val _analyticsState = MutableStateFlow(historyStore.aggregate(historyStore.load()))
    override val analyticsState: StateFlow<AnalyticsState> = _analyticsState.asStateFlow()
    private val _sessionState = MutableStateFlow(SessionState(sessions = historyStore.load()))
    override val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>(extraBufferCapacity = 1)
    override val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

    private var lastGnssUpdateMs = 0L
    private var lastPotholeAtMs = 0L
    private var activeRoute = emptyList<GeoPoint>()
    private var sessionStartedAtMs = 0L
    private var outageStartedAtMs = 0L
    private var outageCount = 0
    private var totalOutageMs = 0L
    private var accumulatedDriftMeters = 0.0
    private var driftSamples = 0
    private var maxDriftMeters = 0.0
    private var squaredSpeedError = 0.0
    private var speedErrorSamples = 0
    private var lastRecoveryDurationSeconds = 0.0
    private var lastFusionImuNs = 0L

    init {
        calibrationStore.load()?.let(alignmentCalibrator::restore)
        sensorAdapter.startListening()
        scope.launch {
            sensorAdapter.sensorState.collect { state ->
                val alignment = alignmentCalibrator.alignment()
                val alignedState = state.copy(
                    vehicleHeadingDegrees = alignmentCalibrator.adjustedHeading(state.yawDegrees).toFloat(),
                    yawAlignmentOffsetDegrees = alignment.yawOffsetDegrees.toFloat(),
                    alignmentConfidencePercentage = alignment.confidencePercentage
                )
                _sensorState.value = alignedState
                processPothole(alignedState)
                if (_navigationState.value.isNavigating) processNavigationSensor(alignedState)
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
        startGnssMonitoring()
        if (!_navigationState.value.isNavigating) {
            sessionStartedAtMs = System.currentTimeMillis()
            outageStartedAtMs = 0L
            outageCount = 0
            totalOutageMs = 0L
            accumulatedDriftMeters = 0.0
            driftSamples = 0
            maxDriftMeters = 0.0
            squaredSpeedError = 0.0
            speedErrorSamples = 0
            lastRecoveryDurationSeconds = 0.0
            _analyticsState.value = AnalyticsState()
        }
        _navigationState.value = _navigationState.value.copy(isNavigating = true)
        _navigationEvents.tryEmit(NavigationEvent.NavigationStarted)
    }

    override fun stopNavigation() {
        locationAdapter.stopLocationUpdates()
        _navigationState.value = _navigationState.value.copy(isNavigating = false)
        finishSession()
        _navigationEvents.tryEmit(NavigationEvent.NavigationStopped)
    }

    override fun startGnssMonitoring() {
        locationAdapter.startLocationUpdates()
    }

    override fun setActiveRoute(route: RouteInfo) {
        activeRoute = route.routePoints
        _mapState.value = _mapState.value.copy(routePoints = route.routePoints)
    }

    override suspend fun findOfflineRoute(start: GeoPoint, end: GeoPoint, destinationName: String): RouteInfo? {
        val points = offlineRoadNetwork.route(start, end) ?: return null
        val distanceMeters = points.zipWithNext().sumOf { (first, second) -> first.distanceToAsDouble(second) }
        return RouteInfo(
            sourceName = "Current Location",
            destinationName = destinationName,
            sourcePoint = start,
            destinationPoint = end,
            routePoints = points,
            totalDistanceKm = distanceMeters / 1_000.0,
            estimatedTimeMinutes = (distanceMeters / 500.0).toInt().coerceAtLeast(1),
            nextManeuver = "Offline route from downloaded road network"
        )
    }

    private fun applyGnss(state: GNSSState) {
        if (!_navigationState.value.isNavigating) return
        val position = GeoPoint(state.latitude, state.longitude)
        val alignment = alignmentCalibrator.addObservation(
            phoneYawDegrees = _sensorState.value.yawDegrees,
            phonePitchDegrees = _sensorState.value.pitchDegrees,
            phoneRollDegrees = _sensorState.value.rollDegrees,
            gnssBearingDegrees = state.bearingDegrees,
            speedKmh = state.speedKmh,
            accuracyMeters = state.accuracyMeters
        )
        calibrationStore.save(alignment)
        _sensorState.value = _sensorState.value.copy(
            vehicleHeadingDegrees = alignmentCalibrator.adjustedHeading(_sensorState.value.yawDegrees).toFloat(),
            yawAlignmentOffsetDegrees = alignment.yawOffsetDegrees.toFloat(),
            alignmentConfidencePercentage = alignment.confidencePercentage
        )
        val wasOutage = outageStartedAtMs != 0L
        if (wasOutage) {
            val outageDurationMs = System.currentTimeMillis() - outageStartedAtMs
            totalOutageMs += outageDurationMs
            lastRecoveryDurationSeconds = outageDurationMs / 1_000.0
            outageStartedAtMs = 0L
        }
        val fused = fusion.updateGnss(position, state.speedKmh / 3.6, state.bearingDegrees, state.accuracyMeters)
        applyRouteMatch(fused.position, isDeadReckoning = false)
        _navigationState.value = _navigationState.value.copy(
            mode = NavigationMode.GNSS_INS,
            speedKmh = fused.speedMps * 3.6,
            headingDegrees = fused.headingDegrees,
            accuracyMeters = fused.horizontalUncertaintyMeters,
            latitude = fused.position.latitude,
            longitude = fused.position.longitude,
            confidencePercentage = state.signalQualityPercentage,
            outageDurationSeconds = 0
        )
        _mapState.value = _mapState.value.copy(
            currentPosition = fused.position,
            gnssTrajectory = (_mapState.value.gnssTrajectory + fused.position).takeLast(200)
        )
        updateAnalytics()
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
        if (!hasFreshGnss() && fusion.isInitialized()) {
            val now = System.nanoTime()
            if (lastFusionImuNs != 0L) {
                fusion.predictGyro(state.gyroZ.toDouble(), (now - lastFusionImuNs) / 1_000_000_000.0)?.let { fused ->
                    _navigationState.value = _navigationState.value.copy(
                        headingDegrees = fused.headingDegrees,
                        accuracyMeters = fused.horizontalUncertaintyMeters
                    )
                }
            }
            lastFusionImuNs = now
        } else {
            lastFusionImuNs = 0L
        }
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
        if (hasFreshGnss()) {
            val speedError = prediction.speedMps * 3.6 - _gnssState.value.speedKmh
            squaredSpeedError += speedError * speedError
            speedErrorSamples++
            updateAnalytics()
            return
        }

        val previous = _navigationState.value
        if (!fusion.isInitialized() && (previous.latitude != 0.0 || previous.longitude != 0.0)) {
            fusion.reset(GeoPoint(previous.latitude, previous.longitude), previous.speedKmh / 3.6, previous.headingDegrees, previous.accuracyMeters)
        }
        val fused = fusion.predict(
            forwardMeters = prediction.forwardMeters.toDouble(),
            lateralMeters = prediction.lateralMeters.toDouble(),
            headingDeltaRadians = prediction.headingDeltaRadians.toDouble(),
            intervalSeconds = 2.0
        ) ?: return
        if (outageStartedAtMs == 0L) {
            outageStartedAtMs = System.currentTimeMillis()
            outageCount++
        }
        val nextOutage = previous.outageDurationSeconds + 2
        _navigationState.value = previous.copy(
            mode = NavigationMode.AI_DEAD_RECKONING,
            speedKmh = prediction.speedMps * 3.6,
            headingDegrees = fused.headingDegrees,
            latitude = fused.position.latitude,
            longitude = fused.position.longitude,
            accuracyMeters = fused.horizontalUncertaintyMeters,
            confidencePercentage = prediction.confidencePercentage,
            outageDurationSeconds = nextOutage,
            totalDistanceKm = previous.totalDistanceKm + prediction.forwardMeters.coerceAtLeast(0f) / 1000.0
        )
        _mapState.value = _mapState.value.copy(
            currentPosition = fused.position,
            rawDRPosition = fused.position,
            drTrajectory = (_mapState.value.drTrajectory + fused.position).takeLast(200)
        )
        val match = applyRouteMatch(fused.position, isDeadReckoning = true)
        if (match != null) {
            _navigationState.value = _navigationState.value.copy(
                latitude = match.point.latitude,
                longitude = match.point.longitude,
                accuracyMeters = maxOf(_navigationState.value.accuracyMeters, match.distanceMeters)
            )
            _mapState.value = _mapState.value.copy(currentPosition = match.point)
        }
        updateAnalytics()
    }

    private fun applyRouteMatch(position: GeoPoint, isDeadReckoning: Boolean): RouteMatch? {
        val routeMatch = RouteMapMatcher.match(position, activeRoute)
        val roadCandidates = offlineRoadNetwork.match(position)
        val hmmMatch = roadMatcher.update(position, roadCandidates)
        val hmmRouteMatch = hmmMatch?.let { RouteMatch(it.candidate.point, it.candidate.distanceMeters, it.confidence) }
        val match = routeMatch ?: hmmRouteMatch ?: roadCandidates.firstOrNull()?.let {
            RouteMatch(it.point, it.distanceMeters, (100.0 - it.distanceMeters * 4.0).toInt().coerceIn(0, 100))
        } ?: return null
        _mapMatchingState.value = MapMatchingState(
            rawPositionLat = position.latitude,
            rawPositionLon = position.longitude,
            matchedPositionLat = match.point.latitude,
            matchedPositionLon = match.point.longitude,
            selectedRoadName = hmmMatch?.candidate?.roadName ?: roadCandidates.firstOrNull()?.roadName ?: "Active navigation route",
            candidateRoads = if (roadCandidates.isEmpty()) listOf(CandidateRoad("Active navigation route", match.confidence)) else roadCandidates.map {
                CandidateRoad(it.roadName, (100.0 - it.distanceMeters * 4.0).toInt().coerceIn(0, 100))
            },
            matchConfidencePercentage = match.confidence,
            distanceFromRoadMeters = match.distanceMeters,
            candidateCount = if (roadCandidates.isEmpty()) 1 else roadCandidates.size
        )
        _mapState.value = _mapState.value.copy(
            matchedPosition = match.point,
            matchedTrajectory = (_mapState.value.matchedTrajectory + match.point).takeLast(200)
        )
        if (isDeadReckoning) {
            accumulatedDriftMeters += match.distanceMeters
            driftSamples++
            maxDriftMeters = maxOf(maxDriftMeters, match.distanceMeters)
        }
        return match
    }

    private fun updateAnalytics() {
        if (sessionStartedAtMs == 0L) return
        val now = System.currentTimeMillis()
        val activeOutageMs = if (outageStartedAtMs == 0L) 0L else now - outageStartedAtMs
        _analyticsState.value = AnalyticsState(
            totalDistanceKm = _navigationState.value.totalDistanceKm,
            totalDurationSeconds = (now - sessionStartedAtMs) / 1_000L,
            outageCount = outageCount,
            totalOutageDurationSeconds = (totalOutageMs + activeOutageMs) / 1_000L,
            averageDriftMeters = if (driftSamples == 0) 0.0 else accumulatedDriftMeters / driftSamples,
            maxDriftMeters = maxDriftMeters,
            positionErrorMeters = if (driftSamples == 0) 0.0 else accumulatedDriftMeters / driftSamples,
            speedErrorKmh = if (speedErrorSamples == 0) 0.0 else kotlin.math.sqrt(squaredSpeedError / speedErrorSamples),
            aiSpeedRmseKmh = if (speedErrorSamples == 0) 0.0 else kotlin.math.sqrt(squaredSpeedError / speedErrorSamples),
            mapMatchingAccuracyPercentage = _mapMatchingState.value.matchConfidencePercentage,
            gnssRecoveryTimeSeconds = lastRecoveryDurationSeconds
        )
    }

    private fun finishSession() {
        if (sessionStartedAtMs == 0L) return
        val now = System.currentTimeMillis()
        if (outageStartedAtMs != 0L) totalOutageMs += now - outageStartedAtMs
        val durationSeconds = (now - sessionStartedAtMs) / 1_000L
        val session = NavigationSession(
            id = sessionStartedAtMs.toString(),
            dateString = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(sessionStartedAtMs)),
            durationString = "%d:%02d".format(durationSeconds / 60, durationSeconds % 60),
            distanceKm = _navigationState.value.totalDistanceKm,
            outageCount = outageCount,
            drDurationSeconds = totalOutageMs / 1_000L,
            maxErrorMeters = maxDriftMeters,
            avgErrorMeters = if (driftSamples == 0) 0.0 else accumulatedDriftMeters / driftSamples,
            status = "Completed"
        )
        _sessionState.value = SessionState(sessions = (listOf(session) + _sessionState.value.sessions).take(25))
        historyStore.save(_sessionState.value.sessions)
        sessionStartedAtMs = 0L
        outageStartedAtMs = 0L
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

}
