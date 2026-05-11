package com.friendzone.android.presentation.map

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.friendzone.android.presentation.theme.AppBackground
import com.friendzone.android.presentation.zones.ZoneEditorDialog
import com.friendzone.android.presentation.zones.ZoneUi
import org.osmdroid.events.MapAdapter
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

private val MapChromeColor = Color(0xFF120D33)

@Composable
fun MapScreen(
    onOpenSettings: () -> Unit,
    viewModel: MapViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    var searchQuery by remember { mutableStateOf("") }
    var appliedFocusRequestId by remember { mutableLongStateOf(-1L) }

    LaunchedEffect(Unit) {
        viewModel.loadZones()
    }

    val visibleZones = remember(state.zones, state.showInactiveZones) {
        if (state.showInactiveZones) state.zones else state.zones.filter { it.isActive }
    }
    val selectedZone = state.selectedZoneId?.let { zoneId ->
        state.zones.firstOrNull { it.id == zoneId }
    }

    AppBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            FriendZoneMap(
                modifier = Modifier.fillMaxSize(),
                zones = visibleZones,
                focusTarget = state.focusTarget,
                appliedFocusRequestId = appliedFocusRequestId,
                onFocusApplied = { appliedFocusRequestId = it },
                onCenterChanged = viewModel::updateMapCenter,
                onZoneClick = { viewModel.selectZone(it) },
                onZoneDragEnd = viewModel::moveZone
            )

            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .background(MapChromeColor)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(Color(0xFFE3874F), CircleShape)
                )
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Центр создания зоны",
                    tint = Color(0xFF1B153F),
                    modifier = Modifier.size(52.dp)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                SearchPanel(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = {
                        focusManager.clearFocus()
                        viewModel.searchPlace(searchQuery)
                    },
                    onOpenSettings = onOpenSettings
                )

                if (state.isLoading) {
                    Card(
                        modifier = Modifier.padding(top = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            Text(
                                text = "Загружаем локальные зоны",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 12.dp)
                            )
                        }
                    }
                }

                state.message?.let { message ->
                    Card(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .clickable { viewModel.clearMessage() },
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f))
                    ) {
                        Text(
                            text = message,
                            color = Color(0xFF1B153F),
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }

    if (state.createDialogVisible) {
        ZoneEditorDialog(
            title = "Новая зона",
            confirmText = "Добавить",
            initialName = "",
            initialRadius = "300",
            initialIsActive = true,
            maxRadiusMeters = state.maxRadiusMeters,
            friends = state.friends,
            initialDetectorFriendIds = emptyList(),
            onDismiss = viewModel::dismissCreateDialog,
            onConfirm = { name, radius, _, detectorFriendIds ->
                viewModel.createZone(name, radius, detectorFriendIds)
            }
        )
    }

    if (selectedZone != null) {
        ZoneEditorDialog(
            title = "Редактирование зоны",
            confirmText = "Сохранить",
            initialName = selectedZone.name,
            initialRadius = selectedZone.radiusMeters.toInt().toString(),
            initialIsActive = selectedZone.isActive,
            maxRadiusMeters = state.maxRadiusMeters,
            friends = state.friends,
            initialDetectorFriendIds = selectedZone.detectorFriendIds,
            onDismiss = viewModel::clearSelectedZone,
            onConfirm = { name, radius, isActive, detectorFriendIds ->
                viewModel.updateZone(
                    selectedZone.copy(
                        name = name,
                        radiusMeters = radius,
                        isActive = isActive,
                        detectorFriendIds = detectorFriendIds
                    )
                )
            },
            onDelete = { viewModel.deleteZone(selectedZone.id) }
        )
    }
}

@Composable
private fun SearchPanel(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onOpenSettings: () -> Unit
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = { Text("Поиск места") },
        leadingIcon = {
            IconButton(onClick = onSearch) {
                Icon(Icons.Default.Search, contentDescription = "Искать место")
            }
        },
        trailingIcon = {
            IconButton(onClick = onOpenSettings) {
                Icon(Icons.Default.Settings, contentDescription = "Настройки")
            }
        },
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search,
            keyboardType = KeyboardType.Text
        ),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White.copy(alpha = 0.95f),
            unfocusedContainerColor = Color.White.copy(alpha = 0.95f),
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

@Composable
private fun FriendZoneMap(
    modifier: Modifier,
    zones: List<ZoneUi>,
    focusTarget: MapFocusTarget?,
    appliedFocusRequestId: Long,
    onFocusApplied: (Long) -> Unit,
    onCenterChanged: (Double, Double) -> Unit,
    onZoneClick: (String) -> Unit,
    onZoneDragEnd: (String, Double, Double) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(15.0)
                controller.setCenter(GeoPoint(55.751244, 37.618423))

                addMapListener(object : MapAdapter() {
                    override fun onScroll(event: ScrollEvent?): Boolean {
                        val center = mapCenter
                        onCenterChanged(center.latitude, center.longitude)
                        return true
                    }

                    override fun onZoom(event: ZoomEvent?): Boolean {
                        val center = mapCenter
                        onCenterChanged(center.latitude, center.longitude)
                        return true
                    }
                })

                val myLocation = MyLocationNewOverlay(GpsMyLocationProvider(context), this).apply {
                    enableMyLocation()
                }
                overlays.add(myLocation)
            }
        },
        update = { mapView ->
            if (focusTarget != null && focusTarget.requestId != appliedFocusRequestId) {
                mapView.controller.setZoom(focusTarget.zoom)
                mapView.controller.animateTo(GeoPoint(focusTarget.latitude, focusTarget.longitude))
                onFocusApplied(focusTarget.requestId)
            }

            mapView.overlays.removeAll { overlay ->
                overlay is Marker || (overlay is Polygon && overlay.title == "zoneOverlay")
            }

            val circles = mutableListOf<Polygon>()
            val markers = mutableListOf<Marker>()

            zones.forEach { zone ->
                val point = GeoPoint(zone.centerLat, zone.centerLon)
                val marker = Marker(mapView).apply {
                    id = zone.id
                    position = point
                    title = zone.name
                    subDescription = "${zone.radiusMeters.toInt()} м"
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    isDraggable = true
                    relatedObject = zone.id
                    setOnMarkerClickListener { clickedMarker, _ ->
                        val clickedId = clickedMarker.relatedObject as? String ?: return@setOnMarkerClickListener true
                        onZoneClick(clickedId)
                        true
                    }
                    setOnMarkerDragListener(object : Marker.OnMarkerDragListener {
                        override fun onMarkerDrag(marker: Marker?) = Unit

                        override fun onMarkerDragStart(marker: Marker?) = Unit

                        override fun onMarkerDragEnd(marker: Marker?) {
                            val draggedId = marker?.relatedObject as? String ?: return
                            val draggedPosition = marker.position
                            onZoneDragEnd(draggedId, draggedPosition.latitude, draggedPosition.longitude)
                        }
                    })
                }

                val circle = Polygon().apply {
                    title = "zoneOverlay"
                    points = Polygon.pointsAsCircle(point, zone.radiusMeters.coerceAtLeast(50.0))
                    if (zone.isActive) {
                        fillPaint.color = AndroidColor.argb(70, 227, 135, 79)
                        outlinePaint.color = AndroidColor.argb(170, 227, 135, 79)
                    } else {
                        fillPaint.color = AndroidColor.argb(45, 140, 140, 140)
                        outlinePaint.color = AndroidColor.argb(130, 140, 140, 140)
                    }
                }

                circles.add(circle)
                markers.add(marker)
            }

            mapView.overlays.addAll(circles)
            mapView.overlays.addAll(markers)

            mapView.invalidate()
        }
    )
}
