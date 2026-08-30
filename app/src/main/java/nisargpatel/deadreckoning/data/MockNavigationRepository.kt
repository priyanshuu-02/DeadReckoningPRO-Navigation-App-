package nisargpatel.deadreckoning.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import nisargpatel.deadreckoning.adapter.SensorAdapter
import nisargpatel.deadreckoning.adapter.PotholeDetector
import nisargpatel.deadreckoning.adapter.TripDataAdapter
import nisargpatel.deadreckoning.domain.model.NavigationMode
import nisargpatel.deadreckoning.domain.repository.NavigationRepository
import nisargpatel.deadreckoning.domain.state.*
import org.osmdroid.util.GeoPoint

/**
 * Mock state machine repository for SIH demonstration.
 * Owns presentation state transitions (GNSS_INS -> Outage -> AI DR -> Pothole -> Recovery -> GNSS_INS).
 * Provides mock/demo metrics and future integration TODOs.
 */
class MockNavigationRepository(
    private val context: Context,
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
) : NavigationRepository {
    companion object {
        private const val TAG = "MockNavigationRepository"
    }

    private val tripAdapter = TripDataAdapter(context)

    // StateFlow streams
    private val _navigationState = MutableStateFlow(createInitialNavigationState())
    override val navigationState: StateFlow<NavigationState> = _navigationState.asStateFlow()

    private val _sensorState = MutableStateFlow(createInitialSensorState())
    override val sensorState: StateFlow<SensorState> = _sensorState.asStateFlow()

    private val _gnssState = MutableStateFlow(createInitialGNSSState())
    override val gnssState: StateFlow<GNSSState> = _gnssState.asStateFlow()

    private val _aiState = MutableStateFlow(createInitialAIState())
    override val aiState: StateFlow<AIState> = _aiState.asStateFlow()

    private val _mapState = MutableStateFlow(createInitialMapState())
    override val mapState: StateFlow<MapState> = _mapState.asStateFlow()

    private val _mapMatchingState = MutableStateFlow(createInitialMapMatchingState())
    override val mapMatchingState: StateFlow<MapMatchingState> = _mapMatchingState.asStateFlow()

    private val _analyticsState = MutableStateFlow(createInitialAnalyticsState())
    override val analyticsState: StateFlow<AnalyticsState> = _analyticsState.asStateFlow()

    private val _sessionState = MutableStateFlow(SessionState(sessions = tripAdapter.getSessions()))
    override val sessionState: StateFlow<SessionState> = _sessionState.asStateFlow()

    // SharedFlow for transient events (replay=1 keeps the last event so subscribers don't miss pothole alerts)
    private val _navigationEvents = MutableSharedFlow<NavigationEvent>(replay = 1)
    override val navigationEvents: SharedFlow<NavigationEvent> = _navigationEvents.asSharedFlow()

    private var autoPlayJob: Job? = null
    private var outageTimerJob: Job? = null
    private var sensorListenerJob: Job? = null
    private var outageSeconds: Long = 0L
    private val potholeDetector = PotholeDetector()
    private var lastPotholeAt: Long = 0L
    private val sensorAdapter = SensorAdapter(context)

    init {
        // Wire up sensor data to pothole detector
        sensorListenerJob = externalScope.launch {
            sensorAdapter.sensorState.collect { sensorState ->
                processSensorSample(
                    sensorState.accelX,
                    sensorState.accelY,
                    sensorState.accelZ,
                    sensorState.gyroX,
                    sensorState.gyroY,
                    sensorState.gyroZ
                )
            }
        }
        // Start listening to sensor events
        sensorAdapter.startListening()
        Log.i(TAG, "SensorAdapter initialized and listening to real sensor data")
    }

    override fun startNavigation() {
        _navigationState.value = _navigationState.value.copy(
            isNavigating = true,
            mode = NavigationMode.GNSS_INS,
            confidencePercentage = 98
        )
        emitEvent(NavigationEvent.NavigationStarted)
    }

    override fun stopNavigation() {
        stopAutoPlay()
        stopOutageTimer()
        _navigationState.value = _navigationState.value.copy(
            isNavigating = false,
            mode = NavigationMode.GNSS_INS
        )
        emitEvent(NavigationEvent.NavigationStopped)
    }

    override fun simulateModeGNSSActive() {
        stopOutageTimer()
        _navigationState.value = _navigationState.value.copy(
            mode = NavigationMode.GNSS_INS,
            accuracyMeters = 3.2,
            confidencePercentage = 98,
            isDemoMode = true
        )
        _gnssState.value = _gnssState.value.copy(
            isAvailable = true,
            satelliteCount = 18,
            accuracyMeters = 3.2,
            fixStatus = "3D FIX (GPS + GLONASS)",
            signalQualityPercentage = 95
        )
        _aiState.value = _aiState.value.copy(
            isActive = false,
            motionClassification = "NORMAL DRIVING"
        )
        emitEvent(NavigationEvent.GNSSRestored)
    }

    override fun simulateOutage() {
        stopOutageTimer()
        _navigationState.value = _navigationState.value.copy(
            mode = NavigationMode.AI_DEAD_RECKONING,
            accuracyMeters = 7.4,
            confidencePercentage = 88,
            isDemoMode = true
        )
        _gnssState.value = _gnssState.value.copy(
            isAvailable = false,
            satelliteCount = 0,
            fixStatus = "NO FIX (OUTAGE)",
            signalQualityPercentage = 0
        )

        // TODO Phase 3: Connect real MLRepository for AI speed estimation & motion classification
        _aiState.value = _aiState.value.copy(
            isActive = true,
            predictedSpeedKmh = 48.3,
            speedConfidencePercentage = 94,
            motionClassification = "NORMAL DRIVING (AI DR)",
            inferenceTimeMs = 8L
        )

        emitEvent(NavigationEvent.GNSSLost)
        startOutageTimer()
    }

    override fun simulatePothole() {
        // Pothole simulation is now only triggered by actual sensor detection flow
        Log.d(TAG, "Manual pothole simulation called - no-op (use sensor-based detection instead)")
    }

    fun processSensorSample(
        accelX: Float,
        accelY: Float,
        accelZ: Float,
        gyroX: Float,
        gyroY: Float,
        gyroZ: Float
    ) {
        val detection = potholeDetector.update(accelX, accelY, accelZ, gyroX, gyroY, gyroZ)
        if (detection.detected && System.currentTimeMillis() - lastPotholeAt > 2500L) {
            lastPotholeAt = System.currentTimeMillis()
            Log.i(TAG, "PotholeDetected event emitted | severity=${detection.severity} | confidence=${detection.confidence}%")
            triggerPotholeEvent("${detection.severity} Pothole (${detection.confidence}% confidence)", detection.confidence)
        }
    }

    private fun triggerPotholeEvent(severity: String, confidence: Int) {
        Log.d(TAG, "triggerPotholeEvent called | severity=$severity | confidence=$confidence")
        // Only emit the transient event, do NOT permanently modify anomalyDetected state
        // The Intelligence screen will handle the transient display via LaunchedEffect
        emitEvent(NavigationEvent.PotholeDetected(severity = severity))
    }

    override fun simulateRecovery() {
        _navigationState.value = _navigationState.value.copy(
            mode = NavigationMode.GNSS_RECOVERY,
            accuracyMeters = 4.1,
            confidencePercentage = 92
        )
        _gnssState.value = _gnssState.value.copy(
            isAvailable = true,
            satelliteCount = 14,
            fixStatus = "RECOVERY FIX",
            signalQualityPercentage = 85
        )
        // TODO Phase 4: Connect real EKF/INS engine for position drift reconciliation
        _analyticsState.value = _analyticsState.value.copy(
            gnssRecoveryTimeSeconds = 2.4
        )
        emitEvent(NavigationEvent.GNSSRestored)

        externalScope.launch {
            delay(3000L)
            simulateModeGNSSActive()
        }
    }

    override fun simulateOffline() {
        stopOutageTimer()
        _navigationState.value = _navigationState.value.copy(
            mode = NavigationMode.OFFLINE,
            confidencePercentage = 85,
            isDemoMode = true
        )
        // TODO Phase 5: Connect offline MapRepository
        emitEvent(NavigationEvent.MapUnavailable)
    }

    override fun simulateError() {
        stopOutageTimer()
        _navigationState.value = _navigationState.value.copy(
            mode = NavigationMode.ERROR,
            confidencePercentage = 30,
            isDemoMode = true
        )
        emitEvent(NavigationEvent.NavigationError("Simulated Sensor Calibration Error"))
    }

    override fun resetDemo() {
        stopAutoPlay()
        stopOutageTimer()
        outageSeconds = 0L
        _navigationState.value = createInitialNavigationState()
        _sensorState.value = createInitialSensorState()
        _gnssState.value = createInitialGNSSState()
        _aiState.value = createInitialAIState()
        _mapState.value = createInitialMapState()
        _mapMatchingState.value = createInitialMapMatchingState()
        _analyticsState.value = createInitialAnalyticsState()
        emitEvent(NavigationEvent.GNSSRestored)
    }

    override fun startAutoPlay() {
        stopAutoPlay()
        _navigationState.value = _navigationState.value.copy(isDemoMode = true, isNavigating = true)
        autoPlayJob = externalScope.launch {
            while (isActive) {
                // 1. GNSS ACTIVE (0-6s)
                simulateModeGNSSActive()
                delay(6000L)

                // 2. GNSS OUTAGE (6-14s)
                simulateOutage()
                delay(8000L)

                // 3. RECOVERY (14-18s) - pothole detection is now sensor-based only
                simulateRecovery()
                delay(4000L)
            }
        }
    }

    override fun stopAutoPlay() {
        autoPlayJob?.cancel()
        autoPlayJob = null
    }

    private fun startOutageTimer() {
        stopOutageTimer()
        outageSeconds = 0L
        outageTimerJob = externalScope.launch {
            while (isActive) {
                delay(1000L)
                outageSeconds++
                _navigationState.value = _navigationState.value.copy(outageDurationSeconds = outageSeconds)
                _gnssState.value = _gnssState.value.copy(outageDurationSeconds = outageSeconds)
            }
        }
    }

    private fun stopOutageTimer() {
        outageTimerJob?.cancel()
        outageTimerJob = null
    }

    private fun emitEvent(event: NavigationEvent) {
        externalScope.launch {
            _navigationEvents.emit(event)
        }
    }

    // Initial Mock Values supplied by Repository (not hard-coded in State classes)
    private fun createInitialNavigationState() = NavigationState(
        mode = NavigationMode.GNSS_INS,
        speedKmh = 48.2,
        headingDegrees = 87.0,
        accuracyMeters = 3.2,
        latitude = 16.5062,
        longitude = 80.6480,
        totalDistanceKm = 12.7,
        outageDurationSeconds = 0L,
        confidencePercentage = 98,
        isNavigating = true,
        isDemoMode = true
    )

    private fun createInitialSensorState() = SensorState(
        accelX = 0.12f, accelY = -0.04f, accelZ = 9.81f, accelMagnitude = 9.81f,
        gyroX = 0.01f, gyroY = 0.02f, gyroZ = -0.01f,
        magX = 22.4f, magY = -12.1f, magZ = 41.5f,
        rollDegrees = 2.3f, pitchDegrees = -1.2f, yawDegrees = 86.4f,
        mountStabilityPercentage = 96, alignmentConfidencePercentage = 94,
        imuSamplingHz = 100, overallHealthPercentage = 92
    )

    private fun createInitialGNSSState() = GNSSState(
        isAvailable = true, satelliteCount = 18, accuracyMeters = 3.2,
        latitude = 16.5062, longitude = 80.6480, speedKmh = 48.2, bearingDegrees = 87.0,
        signalQualityPercentage = 95, fixStatus = "3D FIX (GPS + GLONASS)",
        hdop = 0.9f, vdop = 1.1f
    )

    private fun createInitialAIState() = AIState(
        isModelLoaded = true, isActive = false, predictedSpeedKmh = 48.3,
        speedConfidencePercentage = 94, motionClassification = "NORMAL DRIVING",
        motionConfidencePercentage = 96, anomalyDetected = "None",
        inferenceTimeMs = 8L, modelVersion = "v1.2-SIH-VehPDR"
    )

    private fun createInitialMapState() = MapState(
        currentPosition = GeoPoint(16.5062, 80.6480),
        rawDRPosition = GeoPoint(16.5065, 80.6483),
        matchedPosition = GeoPoint(16.5062, 80.6480),
        routePoints = listOf(
            GeoPoint(16.5062, 80.6480),
            GeoPoint(16.5070, 80.6490),
            GeoPoint(16.5080, 80.6500)
        ),
        isMapAvailable = true,
        currentMapName = "ISRO Bhuvan Standard 2D (SOI Boundary)"
    )

    private fun createInitialMapMatchingState() = MapMatchingState(
        rawPositionLat = 16.5065, rawPositionLon = 80.6483,
        matchedPositionLat = 16.5062, matchedPositionLon = 80.6480,
        selectedRoadName = "NH-65 Highway (Vijayawada)",
        candidateRoads = listOf(
            CandidateRoad("NH-65 Highway", 92),
            CandidateRoad("Service Road East", 6),
            CandidateRoad("Local Access Way", 2)
        ),
        matchConfidencePercentage = 94,
        distanceFromRoadMeters = 4.2,
        candidateCount = 3
    )

    private fun createInitialAnalyticsState() = AnalyticsState(
        totalDistanceKm = 12.7, totalDurationSeconds = 1122L,
        outageCount = 3, totalOutageDurationSeconds = 134L,
        averageDriftMeters = 6.4, maxDriftMeters = 12.8,
        positionErrorMeters = 7.4, speedErrorKmh = 1.8, headingErrorDegrees = 3.1,
        aiSpeedRmseKmh = 2.3, mapMatchingAccuracyPercentage = 94,
        gnssRecoveryTimeSeconds = 2.4
    )
}
