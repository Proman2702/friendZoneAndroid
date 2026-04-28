package com.friendzone.android.presentation.zones

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.events.MapEventsReceiver
import androidx.compose.material3.ExperimentalMaterial3Api
import com.friendzone.android.presentation.theme.AppBackground
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ZoneEditScreen(
    zoneId: String?,
    onBack: () -> Unit,
    viewModel: ZoneEditViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(zoneId) {
        viewModel.load(zoneId)
    }

    AppBackground {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (zoneId == null) "Create zone" else "Edit zone",
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator()
                } else {
                    OutlinedTextField(
                        value = state.name,
                        onValueChange = { viewModel.updateName(it) },
                        label = { Text("Zone name") },
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                MapPicker(
                    lat = state.centerLat,
                    lon = state.centerLon,
                    radiusMeters = state.radiusMeters,
                    zones = state.zones,
                    onPick = { lat, lon -> viewModel.updateCenter(lat, lon) }
                )

                    OutlinedTextField(
                        value = state.radiusMeters.toString(),
                        onValueChange = { viewModel.updateRadius(it) },
                        label = { Text("Radius (meters)") },
                        modifier = Modifier.padding(vertical = 12.dp)
                    )

                    RowSwitch(
                        label = "Active",
                        checked = state.isActive,
                        onCheckedChange = { viewModel.updateActive(it) }
                    )

                    Button(
                        onClick = {
                            viewModel.save(
                                onSuccess = onBack,
                                onError = { }
                            )
                        },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(if (zoneId == null) "Create" else "Save")
                    }

                    if (zoneId != null) {
                        Button(
                            onClick = {
                                viewModel.delete(onSuccess = onBack, onError = { })
                            },
                            modifier = Modifier.padding(top = 12.dp)
                        ) {
                            Text("Delete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RowSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun MapPicker(
    lat: Double,
    lon: Double,
    radiusMeters: Double,
    zones: List<ZoneOverlayUi>,
    onPick: (Double, Double) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
            Text("Map center: ${"%.5f".format(lat)}, ${"%.5f".format(lon)}")
        AndroidView(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth()
                .height(240.dp),
            factory = { context ->
                MapView(context).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(12.0)
                    controller.setCenter(GeoPoint(lat, lon))

                    val marker = Marker(this).apply {
                        position = GeoPoint(lat, lon)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    overlays.add(marker)

                    val circle = Polygon().apply {
                        points = Polygon.pointsAsCircle(GeoPoint(lat, lon), radiusMeters)
                        fillPaint.color = android.graphics.Color.argb(60, 0, 122, 255)
                        outlinePaint.color = android.graphics.Color.argb(140, 0, 122, 255)
                    }
                    overlays.add(circle)

                    zones.forEach { zone ->
                        val zoneCircle = Polygon().apply {
                            title = "zoneOverlay"
                            points = Polygon.pointsAsCircle(
                                GeoPoint(zone.centerLat, zone.centerLon),
                                zone.radiusMeters
                            )
                            if (zone.isActive) {
                                fillPaint.color = android.graphics.Color.argb(40, 0, 191, 166)
                                outlinePaint.color = android.graphics.Color.argb(120, 0, 191, 166)
                            } else {
                                fillPaint.color = android.graphics.Color.argb(35, 120, 120, 120)
                                outlinePaint.color = android.graphics.Color.argb(120, 120, 120, 120)
                            }
                        }
                        overlays.add(zoneCircle)
                    }

                    val myLocation = MyLocationNewOverlay(GpsMyLocationProvider(context), this).apply {
                        enableMyLocation()
                    }
                    overlays.add(myLocation)

                    val receiver = object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                            marker.position = p
                            circle.points = Polygon.pointsAsCircle(p, radiusMeters)
                            controller.setCenter(p)
                            onPick(p.latitude, p.longitude)
                            invalidate()
                            return true
                        }

                        override fun longPressHelper(p: GeoPoint): Boolean = false
                    }
                    overlays.add(MapEventsOverlay(receiver))
                }
            },
            update = { mapView ->
                val point = GeoPoint(lat, lon)
                mapView.controller.setCenter(point)
                mapView.overlays.filterIsInstance<Marker>().firstOrNull()?.position = point
                mapView.overlays.filterIsInstance<Polygon>().firstOrNull()?.points =
                    Polygon.pointsAsCircle(point, radiusMeters)
                mapView.overlays.removeAll { overlay ->
                    overlay is Polygon && overlay.title == "zoneOverlay"
                }
                zones.forEach { zone ->
                    val zoneCircle = Polygon().apply {
                        title = "zoneOverlay"
                        points = Polygon.pointsAsCircle(
                            GeoPoint(zone.centerLat, zone.centerLon),
                            zone.radiusMeters
                        )
                        if (zone.isActive) {
                            fillPaint.color = android.graphics.Color.argb(40, 0, 191, 166)
                            outlinePaint.color = android.graphics.Color.argb(120, 0, 191, 166)
                        } else {
                            fillPaint.color = android.graphics.Color.argb(35, 120, 120, 120)
                            outlinePaint.color = android.graphics.Color.argb(120, 120, 120, 120)
                        }
                    }
                    mapView.overlays.add(zoneCircle)
                }
            }
        )
    }
}


