package com.example.dacsiii_v2.ui.customer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import com.example.dacsiii_v2.ui.common.SectionCard
import com.example.dacsiii_v2.ui.common.IosPrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerMapPickerScreen(
    title: String,
    initialLat: Double?,
    initialLng: Double?,
    onPicked: (Double, Double) -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val defaultPoint = GeoPoint(initialLat ?: 16.047079, initialLng ?: 108.20623)
    var selectedPoint by remember {
        mutableStateOf<GeoPoint?>(
            if (initialLat != null && initialLng != null) GeoPoint(initialLat, initialLng) else null
        )
    }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AndroidView(
                factory = { ctx ->
                    val mapView = MapView(ctx)
                    mapView.setTileSource(TileSourceFactory.MAPNIK)
                    mapView.setMultiTouchControls(true)
                    mapView.controller.setZoom(15.0)
                    mapView.controller.setCenter(defaultPoint)

                    val marker = Marker(mapView).apply {
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        selectedPoint?.let { position = it }
                    }
                    if (selectedPoint != null) {
                        mapView.overlays.add(marker)
                    }

                    val receiver = object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                            selectedPoint = p
                            marker.position = p
                            if (!mapView.overlays.contains(marker)) {
                                mapView.overlays.add(marker)
                            }
                            mapView.invalidate()
                            return true
                        }

                        override fun longPressHelper(p: GeoPoint): Boolean {
                            selectedPoint = p
                            marker.position = p
                            if (!mapView.overlays.contains(marker)) {
                                mapView.overlays.add(marker)
                            }
                            mapView.invalidate()
                            return true
                        }
                    }

                    mapView.overlays.add(MapEventsOverlay(receiver))
                    mapView
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
            )

            val latText = selectedPoint?.latitude?.toString() ?: "-"
            val lngText = selectedPoint?.longitude?.toString() ?: "-"
            SectionCard(title = "Tọa độ đã chọn") {
                Text(text = "Lat: $latText")
                Text(text = "Lng: $lngText")
                IosPrimaryButton(
                    text = "Xác nhận vị trí",
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 12.dp),
                    onClick = {
                        val point = selectedPoint ?: defaultPoint
                        onPicked(point.latitude, point.longitude)
                    }
                )
            }
        }
    }
}
