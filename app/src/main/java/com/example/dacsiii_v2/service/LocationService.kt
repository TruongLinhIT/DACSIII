package com.example.dacsiii_v2.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.dacsiii_v2.R
import com.example.dacsiii_v2.data.remote.RetrofitClient
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import io.socket.client.IO
import io.socket.client.Socket
import java.util.concurrent.atomic.AtomicBoolean

class LocationService : Service() {
    private var locationClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null
    private var socket: Socket? = null
    private var driverId: Int? = null
    private var lastLat: Double? = null
    private var lastLng: Double? = null
    private val socketOnlineSent = AtomicBoolean(false)

    override fun onCreate() {
        super.onCreate()
        locationClient = LocationServices.getFusedLocationProviderClient(this)
        socket = IO.socket(RetrofitClient.SOCKET_URL).apply {
            on(Socket.EVENT_CONNECT) {
                socketOnlineSent.set(false)
                val lat = lastLat
                val lng = lastLng
                if (lat != null && lng != null) {
                    emitLocationUpdate(lat, lng)
                }
            }
            connect()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_START -> {
                driverId = intent.getIntExtra(EXTRA_DRIVER_ID, 0).takeIf { it > 0 }
                startForeground(NOTIFICATION_ID, buildNotification())
                startLocationUpdates()
                return START_STICKY
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        locationCallback?.let { callback ->
            locationClient?.removeLocationUpdates(callback)
        }
        locationCallback = null
        locationClient = null
        socket?.disconnect()
        socket = null
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10_000)
            .setMinUpdateIntervalMillis(10_000)
            .setMinUpdateDistanceMeters(10f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                emitLocationUpdate(location.latitude, location.longitude)
            }
        }

        try {
            locationClient?.requestLocationUpdates(request, locationCallback!!, mainLooper)
        } catch (e: SecurityException) {
            stopSelf()
        }
    }

    private fun emitLocationUpdate(lat: Double, lng: Double) {
        val id = driverId ?: return
        lastLat = lat
        lastLng = lng
        if (!socketOnlineSent.get()) {
            socket?.emit(
                "driver:online",
                mapOf(
                    "driver_id" to id,
                    "lat" to lat,
                    "lng" to lng
                )
            )
            socketOnlineSent.set(true)
        }

        socket?.emit(
            "update_location",
            mapOf(
                "driver_id" to id,
                "lat" to lat,
                "lng" to lng,
                "is_online" to 1
            )
        )
    }

    private fun buildNotification(): Notification {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Tracking driver location")
            .setContentText("We are updating your location in the background")
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "driver_location_tracking"
        private const val NOTIFICATION_ID = 2001
        private const val ACTION_START = "com.example.dacsiii_v2.service.action.START"
        private const val ACTION_STOP = "com.example.dacsiii_v2.service.action.STOP"
        private const val EXTRA_DRIVER_ID = "extra_driver_id"

        fun start(context: Context, driverId: Int) {
            val intent = Intent(context, LocationService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_DRIVER_ID, driverId)
            }
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, LocationService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }
}
