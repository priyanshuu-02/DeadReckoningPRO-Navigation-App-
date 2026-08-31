package nisargpatel.deadreckoning.ui.screens

import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.delay
import nisargpatel.deadreckoning.domain.model.NavigationMode
import nisargpatel.deadreckoning.domain.state.NavigationEvent
import nisargpatel.deadreckoning.ui.components.*
import nisargpatel.deadreckoning.ui.theme.*
import nisargpatel.deadreckoning.ui.viewmodel.NavigationViewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

private const val TAG = "LiveNavMap"
private const val APP_USER_AGENT = "DeadReckoningPro/1.0 (Android; nisargpatel.deadreckoning)"

@Composable
fun LiveNavigationScreen(
    viewModel: NavigationViewModel
) {
    val navState by viewModel.navigationState.collectAsState()
    val routeInfo by viewModel.selectedRoute.collectAsState()
    val mapState by viewModel.mapState.collectAsState()
    var potholeAlert by remember { mutableStateOf<String?>(null) }

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel.events) {
        viewModel.events.collect { event ->
            if (event is NavigationEvent.PotholeDetected) {
                potholeAlert = event.severity
                delay(4_000L)
                potholeAlert = null
            }
        }
    }

    // Bind OSMDroid MapView lifecycle to active Compose lifecycle
    DisposableEffect(lifecycleOwner, mapViewRef) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    Log.i(TAG, "Resuming OSMDroid MapView and street tile downloader threads")
                    mapViewRef?.onResume()
                }
                Lifecycle.Event.ON_PAUSE -> {
                    Log.i(TAG, "Pausing OSMDroid MapView")
                    mapViewRef?.onPause()
                }
                Lifecycle.Event.ON_DESTROY -> {
                    Log.i(TAG, "Detaching OSMDroid MapView")
                    mapViewRef?.onDetach()
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        mapViewRef?.onResume()

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapViewRef?.onPause()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(UberBlack)
    ) {
        // 1. Dominant OSMDroid MapView Area (Fullscreen Background)
        AndroidView(
            factory = { context ->
                Log.i(TAG, "Initializing MapView with MAPNIK tiles")

                val config = Configuration.getInstance()
                config.userAgentValue = APP_USER_AGENT

                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    setDestroyMode(false)
                    controller.setZoom(17.5)
                    onResume()

                    // Rotation gesture overlay
                    val rotationOverlay = RotationGestureOverlay(context, this)
                    rotationOverlay.isEnabled = true
                    overlays.add(rotationOverlay)

                    // Hardware Location Provider Overlay (Person icon hidden)
                    try {
                        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(context), this)
                        locationOverlay.enableMyLocation()
                        locationOverlay.enableFollowLocation()
                        val emptyBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                        locationOverlay.setPersonIcon(emptyBitmap)
                        locationOverlay.setDirectionIcon(emptyBitmap)
                        overlays.add(locationOverlay)
                        Log.i(TAG, "Hardware MyLocationNewOverlay enabled")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error initializing MyLocationNewOverlay", e)
                    }

                    // Official Survey of India Boundary Overlay
                    try {
                        nisargpatel.deadreckoning.util.IndiaBoundaryOverlayHelper.applyOfficialBoundary(context, this)
                        Log.i(TAG, "Official Survey of India boundary overlay applied")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error applying India boundary overlay", e)
                    }

                    mapViewRef = this
                }
            },
            update = { mapView ->
                mapViewRef = mapView

                fun updateTrack(id: String, points: List<GeoPoint>, color: Int, width: Float) {
                    val line = mapView.overlays.filterIsInstance<Polyline>().firstOrNull { it.id == id }
                        ?: Polyline().also { created ->
                            created.id = id
                            mapView.overlays.add(created)
                        }
                    line.outlinePaint.color = color
                    line.outlinePaint.strokeWidth = width
                    line.setPoints(points)
                }

                // Planned route is blue; observed GNSS is green; inertial DR is red.
                // Add the route first so the measured path remains readable above it.
                updateTrack("idr_planned_route", mapState.routePoints.ifEmpty { routeInfo.routePoints }, AndroidColor.parseColor("#2563EB"), 10f)
                updateTrack("idr_gnss_track", mapState.gnssTrajectory, AndroidColor.parseColor("#22C55E"), 12f)
                updateTrack("idr_dead_reckoning_track", mapState.drTrajectory, AndroidColor.parseColor("#EF4444"), 12f)

                if (navState.latitude != 0.0 || navState.longitude != 0.0) {
                    val currentPos = GeoPoint(navState.latitude, navState.longitude)
                    UberVehicleMarker.updateVehicleMarker(
                        mapView = mapView,
                        position = currentPos,
                        headingDegrees = navState.headingDegrees
                    )
                    mapView.controller.animateTo(currentPos)
                }
                mapView.invalidate()
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top Floating Uber Pickup & Destination Search Bar + Status Indicator
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 14.dp, end = 14.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModeIndicator(mode = navState.mode)
                ConfidenceIndicator(percentage = navState.confidencePercentage)
            }

            if (navState.mode == NavigationMode.AI_DEAD_RECKONING || navState.outageDurationSeconds > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                OutageBanner(outageSeconds = navState.outageDurationSeconds)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Uber Floating Search Bar
            UberSearchBar(
                currentRoute = routeInfo,
                onDestinationSelected = { name, point ->
                    Log.i(TAG, "User selected destination: $name ($point)")
                    viewModel.selectDestination(name, point)
                },
                onSearch = viewModel::searchDestinations
            )

            potholeAlert?.let { alert ->
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = WarningAmber.copy(alpha = 0.94f),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(14.dp)
                ) {
                    Text(
                        text = alert,
                        color = UberBlack,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }
        }

        // 3. Bottom Uber Turn-by-Turn Navigation HUD Card
        UberNavigationHUD(
            routeInfo = routeInfo,
            speedKmh = navState.speedKmh,
            headingDegrees = navState.headingDegrees,
            accuracyMeters = navState.accuracyMeters,
            isNavigating = navState.isNavigating,
            onToggleNavigation = {
                if (navState.isNavigating) {
                    Log.i(TAG, "User stopped navigation")
                    viewModel.stopNavigation()
                } else {
                    Log.i(TAG, "User started navigation")
                    viewModel.startNavigation()
                }
            },
            onRecenterMap = {
                if (navState.latitude != 0.0 || navState.longitude != 0.0) mapViewRef?.let { map ->
                    val pos = GeoPoint(navState.latitude, navState.longitude)
                    Log.i(TAG, "Recentering map on position: $pos")
                    map.controller.animateTo(pos)
                    map.controller.setZoom(18.5)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(14.dp)
        )
    }
}
